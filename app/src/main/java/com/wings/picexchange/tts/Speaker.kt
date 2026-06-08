package com.wings.picexchange.tts

import kotlinx.coroutines.flow.StateFlow

/** One thing to say. [id] is the strip item's instanceId so the UI can highlight it while spoken. */
data class SpeechItem(val id: String, val text: String)

sealed interface TtsStatus {
    data object Initializing : TtsStatus
    data object Ready : TtsStatus
    data class Unavailable(val reason: TtsUnavailableReason) : TtsStatus
}

enum class TtsUnavailableReason { NO_ENGINE, LANGUAGE_MISSING, INIT_FAILED }

/**
 * Abstraction over the on-device text-to-speech engine. Behind an interface so the speak flow is
 * unit-testable with a fake (the real [TtsManager] needs the Android TTS engine).
 */
interface Speaker {
    /** Engine readiness. */
    val status: StateFlow<TtsStatus>

    /** instanceId of the item currently being spoken, or null when idle — drives per-card highlight. */
    val speakingId: StateFlow<String?>

    /** True from the moment speaking starts until the last utterance finishes or it is stopped. */
    val isSpeaking: StateFlow<Boolean>

    /** Speaks each item in order. Each item's [SpeechItem.id] is reported back via [speakingId]. */
    fun speak(items: List<SpeechItem>)

    fun stop()
}
