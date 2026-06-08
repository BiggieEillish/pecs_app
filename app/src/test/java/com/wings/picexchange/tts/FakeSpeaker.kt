package com.wings.picexchange.tts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Test double for [Speaker] that records calls instead of touching the Android TTS engine. */
class FakeSpeaker : Speaker {

    val spokenBatches = mutableListOf<List<SpeechItem>>()
    var stopCount = 0

    private val _status = MutableStateFlow<TtsStatus>(TtsStatus.Ready)
    override val status: StateFlow<TtsStatus> = _status.asStateFlow()

    private val _speakingId = MutableStateFlow<String?>(null)
    override val speakingId: StateFlow<String?> = _speakingId.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    override fun speak(items: List<SpeechItem>) {
        spokenBatches.add(items)
    }

    override fun stop() {
        stopCount++
    }
}
