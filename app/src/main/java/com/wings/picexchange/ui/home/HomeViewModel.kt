package com.wings.picexchange.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wings.picexchange.data.PicExchangeRepository
import com.wings.picexchange.data.image.ImageStore
import com.wings.picexchange.data.local.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PicExchangeRepository,
    private val imageStore: ImageStore,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observeCategories()
        .map { categories ->
            if (categories.isEmpty()) HomeUiState.Empty else HomeUiState.Content(categories)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    /** Deletes a category, its cards (FK cascade), and any user-imported image files they used. */
    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.cardsIn(category.id).forEach { imageStore.deleteIfLocal(it.imageRef) }
            imageStore.deleteIfLocal(category.imageRef)
            repository.deleteCategory(category)
        }
    }
}
