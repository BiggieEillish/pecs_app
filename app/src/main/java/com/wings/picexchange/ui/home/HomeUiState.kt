package com.wings.picexchange.ui.home

import com.wings.picexchange.data.local.entity.CategoryEntity

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Content(val categories: List<CategoryEntity>) : HomeUiState
}
