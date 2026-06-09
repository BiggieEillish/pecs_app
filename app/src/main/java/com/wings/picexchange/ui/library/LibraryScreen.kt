package com.wings.picexchange.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.data.settings.AppMode
import com.wings.picexchange.ui.LocalAppMode
import com.wings.picexchange.ui.components.CardTile
import com.wings.picexchange.ui.components.ConfirmDeleteDialog
import com.wings.picexchange.ui.components.ManageItemDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onCardClick: (CardEntity) -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isTeacher = LocalAppMode.current == AppMode.TEACHER
    val title = (state as? LibraryUiState.Content)?.categoryName.orEmpty()
    var managed by remember { mutableStateOf<CardEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<CardEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            if (isTeacher) {
                ExtendedFloatingActionButton(onClick = onAddCard) { Text("Add card") }
            }
        },
    ) { padding ->
        when (val s = state) {
            LibraryUiState.Loading -> Centered(padding) { CircularProgressIndicator() }
            is LibraryUiState.Content ->
                if (s.cards.isEmpty()) {
                    Centered(padding) {
                        Text(
                            text = if (isTeacher) "No cards yet.\nTap “Add card” to add one." else "No cards yet.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    CardGrid(
                        cards = s.cards,
                        onCardClick = onCardClick,
                        onCardLongClick = { if (isTeacher) managed = it },
                        padding = padding,
                    )
                }
        }
    }

    managed?.let { card ->
        ManageItemDialog(
            title = card.label,
            onEdit = { managed = null; onEditCard(card.id) },
            onDelete = { confirmDelete = card; managed = null },
            onDismiss = { managed = null },
        )
    }
    confirmDelete?.let { card ->
        ConfirmDeleteDialog(
            message = "Delete “${card.label}”?",
            onConfirm = { viewModel.deleteCard(card); confirmDelete = null },
            onDismiss = { confirmDelete = null },
        )
    }
}

@Composable
private fun CardGrid(
    cards: List<CardEntity>,
    onCardClick: (CardEntity) -> Unit,
    onCardLongClick: (CardEntity) -> Unit,
    padding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Tap a card to add it",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(cards, key = { it.id }) { card ->
            CardTile(
                label = card.label,
                imageRef = card.imageRef,
                seed = card.id,
                onClick = { onCardClick(card) },
                onLongClick = { onCardLongClick(card) },
            )
        }
    }
}

@Composable
private fun Centered(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) { content() }
}
