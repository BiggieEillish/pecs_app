package com.wings.picexchange.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wings.picexchange.data.PicExchangeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: PicExchangeRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observeCategories()
        .map { categories ->
            if (categories.isEmpty()) HomeUiState.Empty else HomeUiState.Content(categories)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
