package com.wings.picexchange.ui.library

import com.wings.picexchange.data.local.entity.CardEntity

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Content(val categoryName: String, val cards: List<CardEntity>) : LibraryUiState
}
