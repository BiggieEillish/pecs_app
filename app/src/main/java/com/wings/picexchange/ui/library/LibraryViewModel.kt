package com.wings.picexchange.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wings.picexchange.data.PicExchangeRepository
import com.wings.picexchange.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PicExchangeRepository,
) : ViewModel() {

    private val categoryId: Long = checkNotNull(savedStateHandle[Routes.ARG_CATEGORY_ID]) {
        "LibraryViewModel requires a '${Routes.ARG_CATEGORY_ID}' navigation argument"
    }

    val uiState: StateFlow<LibraryUiState> = repository.observeCards(categoryId)
        .map { cards ->
            LibraryUiState.Content(
                categoryName = repository.getCategory(categoryId)?.name.orEmpty(),
                cards = cards,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState.Loading)
}
