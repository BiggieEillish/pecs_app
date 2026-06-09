package com.wings.picexchange.ui.home

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import com.wings.picexchange.data.local.entity.CategoryEntity
import com.wings.picexchange.data.settings.AppMode
import com.wings.picexchange.ui.LocalAppMode
import com.wings.picexchange.ui.components.CategoryTile
import com.wings.picexchange.ui.components.ConfirmDeleteDialog
import com.wings.picexchange.ui.components.ManageItemDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (Long) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Long) -> Unit,
    onToggleMode: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isTeacher = LocalAppMode.current == AppMode.TEACHER
    var managed by remember { mutableStateOf<CategoryEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("PicExchange") },
                actions = {
                    FilledTonalButton(
                        onClick = onToggleMode,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(if (isTeacher) "Lock" else "Teacher")
                    }
                },
            )
        },
        floatingActionButton = {
            if (isTeacher) {
                ExtendedFloatingActionButton(onClick = onAddCategory) { Text("Add category") }
            }
        },
    ) { padding ->
        when (val s = state) {
            HomeUiState.Loading -> Centered(padding) { CircularProgressIndicator() }
            HomeUiState.Empty -> Centered(padding) {
                Text(
                    text = if (isTeacher) {
                        "No categories yet.\nTap “Add category” to begin."
                    } else {
                        "No categories yet."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            is HomeUiState.Content -> CategoryGrid(
                categories = s.categories,
                onCategoryClick = onCategoryClick,
                onCategoryLongClick = { if (isTeacher) managed = it },
                padding = padding,
            )
        }
    }

    managed?.let { category ->
        ManageItemDialog(
            title = category.name,
            onEdit = { managed = null; onEditCategory(category.id) },
            onDelete = { confirmDelete = category; managed = null },
            onDismiss = { managed = null },
        )
    }
    confirmDelete?.let { category ->
        ConfirmDeleteDialog(
            message = "Delete “${category.name}” and all of its cards?",
            onConfirm = { viewModel.deleteCategory(category); confirmDelete = null },
            onDismiss = { confirmDelete = null },
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    onCategoryClick: (Long) -> Unit,
    onCategoryLongClick: (CategoryEntity) -> Unit,
    padding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Pick a category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(categories, key = { it.id }) { category ->
            CategoryTile(
                name = category.name,
                imageRef = category.imageRef,
                seed = category.id,
                onClick = { onCategoryClick(category.id) },
                onLongClick = { onCategoryLongClick(category) },
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
