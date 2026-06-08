package com.wings.picexchange.ui

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wings.picexchange.data.settings.AppMode
import com.wings.picexchange.tts.TtsStatus
import com.wings.picexchange.tts.TtsUnavailableReason
import com.wings.picexchange.ui.components.PinDialog
import com.wings.picexchange.ui.navigation.PicExchangeNavHost
import com.wings.picexchange.ui.strip.SentenceStrip
import com.wings.picexchange.ui.strip.StripViewModel
import kotlinx.coroutines.launch

/**
 * App root. Owns the Activity-scoped [StripViewModel] and [AppModeViewModel], provides the current
 * [AppMode] app-wide via [LocalAppMode], and hosts the persistent strip, the Undo snackbar, the
 * TTS-unavailable banner, and the parental-PIN dialogs.
 */
@Composable
fun PicExchangeRoot() {
    val stripViewModel: StripViewModel = hiltViewModel()
    val appModeViewModel: AppModeViewModel = hiltViewModel()

    val items by stripViewModel.items.collectAsStateWithLifecycle()
    val speakingId by stripViewModel.speakingId.collectAsStateWithLifecycle()
    val isSpeaking by stripViewModel.isSpeaking.collectAsStateWithLifecycle()
    val ttsStatus by stripViewModel.ttsStatus.collectAsStateWithLifecycle()

    val appMode by appModeViewModel.mode.collectAsStateWithLifecycle()
    val hasPin by appModeViewModel.hasPin.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pinPrompt by remember { mutableStateOf<PinPrompt?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalAppMode provides appMode) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column {
                    (ttsStatus as? TtsStatus.Unavailable)?.let { TtsUnavailableBanner(it.reason) }
                    SentenceStrip(
                        items = items,
                        speakingId = speakingId,
                        isSpeaking = isSpeaking,
                        canSpeak = ttsStatus is TtsStatus.Ready,
                        onSpeak = stripViewModel::speak,
                        onStop = stripViewModel::stopSpeaking,
                        onRemove = { instanceId ->
                            val index = items.indexOfFirst { it.instanceId == instanceId }
                            val removed = items.getOrNull(index)
                            stripViewModel.remove(instanceId)
                            if (removed != null) {
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Removed “${removed.label}”",
                                        actionLabel = "Undo",
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        stripViewModel.insertAt(index, removed)
                                    }
                                }
                            }
                        },
                        onClear = {
                            val previous = stripViewModel.clear()
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Sentence cleared",
                                    actionLabel = "Undo",
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    stripViewModel.restore(previous)
                                }
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            PicExchangeNavHost(
                onCardClick = stripViewModel::add,
                onToggleMode = {
                    when {
                        appMode == AppMode.TEACHER -> appModeViewModel.lock()
                        hasPin -> { pinError = null; pinPrompt = PinPrompt.ENTER }
                        else -> { pinError = null; pinPrompt = PinPrompt.SET }
                    }
                },
                modifier = Modifier.padding(innerPadding),
            )
        }

        when (pinPrompt) {
            PinPrompt.SET -> PinDialog(
                title = "Set a parental PIN",
                confirmLabel = "Set & unlock",
                error = pinError,
                onSubmit = { pin -> appModeViewModel.setPinAndUnlock(pin); pinPrompt = null },
                onDismiss = { pinPrompt = null },
            )
            PinPrompt.ENTER -> PinDialog(
                title = "Enter parental PIN",
                confirmLabel = "Unlock",
                error = pinError,
                onSubmit = { pin ->
                    appModeViewModel.unlock(pin) { ok ->
                        if (ok) { pinPrompt = null; pinError = null } else pinError = "Incorrect PIN. Try again."
                    }
                },
                onDismiss = { pinPrompt = null },
            )
            null -> Unit
        }
    }
}

private enum class PinPrompt { SET, ENTER }

@Composable
private fun TtsUnavailableBanner(reason: TtsUnavailableReason) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (reason) {
                    TtsUnavailableReason.NO_ENGINE -> "No speech engine is installed on this device."
                    TtsUnavailableReason.LANGUAGE_MISSING -> "Speech needs voice data installed."
                    TtsUnavailableReason.INIT_FAILED -> "Speech is unavailable right now."
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (reason == TtsUnavailableReason.LANGUAGE_MISSING) {
                TextButton(onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                }) {
                    Text("Install")
                }
            }
        }
    }
}
