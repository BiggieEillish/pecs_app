package com.wings.picexchange.ui.strip

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wings.picexchange.R
import com.wings.picexchange.ui.components.ImageRefImage
import com.wings.picexchange.ui.theme.tileColorsFor

/**
 * Persistent bottom strip. Tapped cards are assembled here in order, each as a colourful chip.
 * Chips fade/slide in when added and out when removed; the strip auto-scrolls to the newest word.
 * The chip currently being spoken gently pulses and is ringed. "Speak" reads the sentence aloud
 * (toggles to "Stop" while speaking); "Clear" empties the strip.
 */
@Composable
fun SentenceStrip(
    items: List<StripItem>,
    speakingId: String?,
    isSpeaking: Boolean,
    canSpeak: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // Keep the most recently added word in view.
    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) listState.animateScrollToItem(items.lastIndex)
    }

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 10.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(120.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (items.isEmpty()) {
                    Text(
                        text = "Tap a card to start a sentence",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 6.dp),
                    ) {
                        items(items, key = { it.instanceId }) { item ->
                            StripChip(
                                item = item,
                                isSpeaking = item.instanceId == speakingId,
                                onRemove = { onRemove(item.instanceId) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SpeakButton(
                    isSpeaking = isSpeaking,
                    enabled = items.isNotEmpty() && (canSpeak || isSpeaking),
                    onSpeak = onSpeak,
                    onStop = onStop,
                )
                TextButton(
                    onClick = onClear,
                    enabled = items.isNotEmpty(),
                    modifier = Modifier.width(124.dp),
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun SpeakButton(
    isSpeaking: Boolean,
    enabled: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
) {
    // A slow, calm pulse while speaking — a gentle "I'm talking" cue, never a flash.
    val transition = rememberInfiniteTransition(label = "speakPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "speakPulseScale",
    )
    Button(
        onClick = { if (isSpeaking) onStop() else onSpeak() },
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier
            .width(124.dp)
            .height(52.dp)
            .graphicsLayer {
                val s = if (isSpeaking) pulse else 1f
                scaleX = s
                scaleY = s
            },
    ) {
        Icon(
            painter = painterResource(if (isSpeaking) R.drawable.ic_stop else R.drawable.ic_volume_up),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(if (isSpeaking) "Stop" else "Speak", maxLines = 1)
    }
}

@Composable
private fun LazyItemScope.StripChip(
    item: StripItem,
    isSpeaking: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = tileColorsFor(item.cardId)
    val shape = RoundedCornerShape(16.dp)

    val transition = rememberInfiniteTransition(label = "chipPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "chipPulseScale",
    )

    Box(modifier = modifier) {
        Surface(
            shape = shape,
            color = if (isSpeaking) MaterialTheme.colorScheme.primaryContainer else colors.container,
            contentColor = if (isSpeaking) MaterialTheme.colorScheme.onPrimaryContainer else colors.onContainer,
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer {
                    val s = if (isSpeaking) pulse else 1f
                    scaleX = s
                    scaleY = s
                }
                .then(
                    if (isSpeaking) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
                    else Modifier,
                )
                .clip(shape)
                .clickable(onClickLabel = "Remove ${item.label}", onClick = onRemove),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ImageRefImage(
                    imageRef = item.imageRef,
                    contentDescription = item.label,
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // Visual hint that tapping the chip removes it (the whole chip is the touch target).
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp),
        ) {
            Text(
                text = "×",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
