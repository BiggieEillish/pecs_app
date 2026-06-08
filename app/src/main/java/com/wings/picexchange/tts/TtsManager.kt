package com.wings.picexchange.tts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device [Speaker]. Process-scoped @Singleton: created once and kept for the whole process — it
 * must NEVER be shut down in a ViewModel.onCleared() (that would destroy the shared engine on the
 * first rotation). Speech is stopped when the app goes to the background via ProcessLifecycleOwner.
 *
 * Each strip item is spoken as its own utterance (QUEUE_FLUSH for the first, QUEUE_ADD after) so the
 * UtteranceProgressListener can report which item is currently being spoken for per-card highlight.
 * Engine callbacks are marshalled to the main thread before touching state.
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext context: Context,
) : Speaker {

    private val _status = MutableStateFlow<TtsStatus>(TtsStatus.Initializing)
    override val status: StateFlow<TtsStatus> = _status.asStateFlow()

    private val _speakingId = MutableStateFlow<String?>(null)
    override val speakingId: StateFlow<String?> = _speakingId.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastUtteranceId: String? = null
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, ::onInit)

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = onMain {
                _speakingId.value = utteranceId
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) = onMain {
                if (utteranceId == lastUtteranceId) {
                    _speakingId.value = null
                    _isSpeaking.value = false
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = onMain {
                _speakingId.value = null
                _isSpeaking.value = false
            }
        })

        // Stop speaking when the whole app goes to the background.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) = stop()
        })
    }

    private fun onInit(initStatus: Int) {
        if (initStatus != TextToSpeech.SUCCESS) {
            _status.value = TtsStatus.Unavailable(TtsUnavailableReason.NO_ENGINE)
            return
        }
        _status.value = if (setBestAvailableLanguage()) {
            TtsStatus.Ready
        } else {
            TtsStatus.Unavailable(TtsUnavailableReason.LANGUAGE_MISSING)
        }
    }

    /** Prefer the device locale, fall back to English. Returns false if neither has offline data. */
    private fun setBestAvailableLanguage(): Boolean {
        for (locale in listOf(Locale.getDefault(), Locale.ENGLISH)) {
            val result = tts.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                return true
            }
        }
        return false
    }

    override fun speak(items: List<SpeechItem>) {
        if (_status.value != TtsStatus.Ready || items.isEmpty()) return
        lastUtteranceId = items.last().id
        _isSpeaking.value = true
        items.forEachIndexed { index, item ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(item.text, queueMode, null, item.id)
        }
    }

    override fun stop() {
        tts.stop()
        onMain {
            _speakingId.value = null
            _isSpeaking.value = false
        }
    }

    private inline fun onMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post { block() }
    }
}
