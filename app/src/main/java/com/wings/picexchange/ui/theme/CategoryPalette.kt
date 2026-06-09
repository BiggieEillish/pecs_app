package com.wings.picexchange.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * A friendly, distinct accent set for one category/card tile.
 *
 * @param container the soft tile background
 * @param onContainer text/label colour on top of [container]
 * @param badge the round icon-badge background (a clean surface the colourful icon sits on)
 * @param accent the default tint applied to built-in (monochrome) icons on this tile
 */
data class TileColors(
    val container: Color,
    val onContainer: Color,
    val badge: Color,
    val accent: Color,
)

// Six soft, cheerful swatches that sit harmoniously with the teal theme. Tiles are coloured
// deterministically by a stable seed (category/card id) so a given item always keeps its colour.
private val LightSwatches = listOf(
    TileColors(Color(0xFFCFF2E6), Color(0xFF00382E), Color(0xFFFFFFFF), Color(0xFF1A9E86)), // mint
    TileColors(Color(0xFFD6E7FF), Color(0xFF0B2A45), Color(0xFFFFFFFF), Color(0xFF3D7DD6)), // sky
    TileColors(Color(0xFFFFE0D2), Color(0xFF4A2113), Color(0xFFFFFFFF), Color(0xFFF2724B)), // coral
    TileColors(Color(0xFFEADDFF), Color(0xFF271046), Color(0xFFFFFFFF), Color(0xFF8A6BE0)), // lavender
    TileColors(Color(0xFFFFEFC2), Color(0xFF4A3500), Color(0xFFFFFFFF), Color(0xFFD79A00)), // sunshine
    TileColors(Color(0xFFFFD9E6), Color(0xFF4A0E2A), Color(0xFFFFFFFF), Color(0xFFD64D86)), // rose
)

private val DarkSwatches = listOf(
    TileColors(Color(0xFF16463B), Color(0xFFA8EFDB), Color(0xFF0E2C25), Color(0xFF5BD8BC)),
    TileColors(Color(0xFF193A57), Color(0xFFBBD9F7), Color(0xFF102538), Color(0xFF7FB4F0)),
    TileColors(Color(0xFF5A2A1A), Color(0xFFFFD2C0), Color(0xFF3A1A0F), Color(0xFFFF9576)),
    TileColors(Color(0xFF342458), Color(0xFFDDD0FF), Color(0xFF22163A), Color(0xFFB49BF0)),
    TileColors(Color(0xFF4A3912), Color(0xFFFFE7A6), Color(0xFF30260B), Color(0xFFF0C45A)),
    TileColors(Color(0xFF54203A), Color(0xFFFFD0E0), Color(0xFF381425), Color(0xFFF080AB)),
)

/**
 * Returns a stable [TileColors] for the given [seed] (use a category/card id), automatically
 * switching between the light and dark swatch sets based on the active theme.
 */
@Composable
@ReadOnlyComposable
fun tileColorsFor(seed: Long): TileColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val swatches = if (dark) DarkSwatches else LightSwatches
    val index = ((seed % swatches.size) + swatches.size).toInt() % swatches.size
    return swatches[index]
}
