package com.wings.picexchange.ui.strip

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wings.picexchange.ui.components.ImageRefImage

/**
 * Persistent bottom strip. Tapped cards are assembled here in order; the chip currently being
 * spoken is highlighted. "Speak" reads the sentence aloud (toggles to "Stop" while speaking),
 * "Clear" empties the strip.
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
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(116.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (items.isEmpty()) {
                    Text(
                        text = "Tap a card to start a sentence",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        items(items, key = { it.instanceId }) { item ->
                            StripChip(
                                item = item,
                                isSpeaking = item.instanceId == speakingId,
                                onRemove = { onRemove(item.instanceId) },
                            )
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { if (isSpeaking) onStop() else onSpeak() },
                    enabled = items.isNotEmpty() && (canSpeak || isSpeaking),
                    modifier = Modifier.width(104.dp),
                ) {
                    Text(if (isSpeaking) "Stop" else "Speak")
                }
                TextButton(
                    onClick = onClear,
                    enabled = items.isNotEmpty(),
                    modifier = Modifier.width(104.dp),
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
private fun StripChip(
    item: StripItem,
    isSpeaking: Boolean,
    onRemove: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box {
        Card(
            modifier = Modifier
                .size(72.dp)
                .then(
                    if (isSpeaking) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
                    else Modifier,
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = if (isSpeaking) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
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
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clickable(onClick = onRemove),
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
