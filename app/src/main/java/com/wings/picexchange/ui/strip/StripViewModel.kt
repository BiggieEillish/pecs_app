package com.wings.picexchange.ui.strip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.tts.Speaker
import com.wings.picexchange.tts.SpeechItem
import com.wings.picexchange.tts.TtsStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Holds the transient sentence strip. Obtained ONCE at the app root (Activity-scoped) so the same
 * instance is shared by the bottom strip and every screen — the in-progress sentence survives
 * Home <-> Library navigation. Backed by [SavedStateHandle] so it also survives rotation /
 * process death (StripItem is Parcelable). It is never persisted to Room.
 *
 * Speech state ([speakingId], [isSpeaking], [ttsStatus]) is sourced from the process-scoped
 * [Speaker]; this ViewModel only forwards it.
 */
@HiltViewModel
class StripViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val speaker: Speaker,
) : ViewModel() {

    val items: StateFlow<List<StripItem>> =
        savedStateHandle.getStateFlow(KEY_ITEMS, emptyList())

    val speakingId: StateFlow<String?> = speaker.speakingId
    val isSpeaking: StateFlow<Boolean> = speaker.isSpeaking
    val ttsStatus: StateFlow<TtsStatus> = speaker.status

    fun add(card: CardEntity) {
        savedStateHandle[KEY_ITEMS] = ArrayList(items.value + StripItem.from(card))
    }

    fun remove(instanceId: String) {
        savedStateHandle[KEY_ITEMS] = ArrayList(items.value.filterNot { it.instanceId == instanceId })
    }

    /** Clears the strip and returns the prior contents so the caller can offer an Undo action. */
    fun clear(): List<StripItem> {
        val previous = items.value
        savedStateHandle[KEY_ITEMS] = ArrayList<StripItem>()
        return previous
    }

    fun restore(previous: List<StripItem>) {
        savedStateHandle[KEY_ITEMS] = ArrayList(previous)
    }

    /** Re-inserts an item at [index] (used to undo a single removal). */
    fun insertAt(index: Int, item: StripItem) {
        val current = items.value.toMutableList()
        current.add(index.coerceIn(0, current.size), item)
        savedStateHandle[KEY_ITEMS] = ArrayList(current)
    }

    /** Speaks the current strip aloud, in order. */
    fun speak() {
        speaker.speak(items.value.map { SpeechItem(id = it.instanceId, text = it.label) })
    }

    fun stopSpeaking() {
        speaker.stop()
    }

    private companion object {
        const val KEY_ITEMS = "strip_items"
    }
}
