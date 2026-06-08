package com.wings.picexchange.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** A tappable category tile for the Home grid. */
@Composable
fun CategoryTile(
    name: String,
    imageRef: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .clickable(onClick = onClick),
    ) {
        TileContent(label = name, imageRef = imageRef, imageSize = 72)
    }
}

/** A read-only picture / sentence-starter card for the Library grid (tap-to-add arrives in M3). */
@Composable
fun CardTile(
    label: String,
    imageRef: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .clickable(onClick = onClick),
    ) {
        TileContent(label = label, imageRef = imageRef, imageSize = 64)
    }
}

@Composable
private fun TileContent(label: String, imageRef: String, imageSize: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ImageRefImage(
            imageRef = imageRef,
            contentDescription = label,
            modifier = Modifier.size(imageSize.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}
