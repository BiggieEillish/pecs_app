package com.wings.picexchange.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wings.picexchange.ui.theme.tileColorsFor

private val TileShape = RoundedCornerShape(24.dp)

/**
 * A category tile for the Home grid. Tap opens the library; long-press manages (edit/delete).
 * [seed] (the category id) picks a stable, distinct colour so each category is recognisable.
 */
@Composable
fun CategoryTile(
    name: String,
    imageRef: String,
    seed: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tile(
        label = name,
        imageRef = imageRef,
        seed = seed,
        badgeSize = 76,
        iconSize = 46,
        minHeight = 156,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    )
}

/** A picture / sentence-starter card. Tap adds it to the strip; long-press manages (edit/delete). */
@Composable
fun CardTile(
    label: String,
    imageRef: String,
    seed: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tile(
        label = label,
        imageRef = imageRef,
        seed = seed,
        badgeSize = 68,
        iconSize = 42,
        minHeight = 148,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    )
}

@Composable
private fun Tile(
    label: String,
    imageRef: String,
    seed: Long,
    badgeSize: Int,
    iconSize: Int,
    minHeight: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = tileColorsFor(seed)
    Surface(
        shape = TileShape,
        color = colors.container,
        contentColor = colors.onContainer,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .clip(TileShape)
            .bounceClick(onClick = onClick, onLongClick = onLongClick, onClickLabel = label),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            Surface(
                shape = CircleShape,
                color = colors.badge,
                shadowElevation = 1.dp,
                modifier = Modifier.size(badgeSize.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ImageRefImage(
                        imageRef = imageRef,
                        contentDescription = label,
                        modifier = Modifier.size(iconSize.dp),
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
