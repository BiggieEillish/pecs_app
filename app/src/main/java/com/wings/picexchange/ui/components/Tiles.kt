package com.wings.picexchange.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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

/** A category tile for the Home grid. Tap opens the library; long-press manages (edit/delete). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryTile(
    name: String,
    imageRef: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        TileContent(label = name, imageRef = imageRef, imageSize = 72)
    }
}

/** A picture / sentence-starter card. Tap adds it to the strip; long-press manages (edit/delete). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardTile(
    label: String,
    imageRef: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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
