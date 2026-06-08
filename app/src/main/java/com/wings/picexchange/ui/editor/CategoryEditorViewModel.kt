package com.wings.picexchange.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wings.picexchange.data.PicExchangeRepository
import com.wings.picexchange.data.image.ImageStore
import com.wings.picexchange.data.local.entity.CategoryEntity
import com.wings.picexchange.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryEditorState(
    val loaded: Boolean = false,
    val name: String = "",
    val imageRef: String? = null,
    val importing: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = name.isNotBlank() && !importing
}

@HiltViewModel
class CategoryEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PicExchangeRepository,
    private val imageStore: ImageStore,
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>(Routes.ARG_CATEGORY_ID) ?: NEW
    val isEditing: Boolean = categoryId > 0

    private val _state = MutableStateFlow(CategoryEditorState())
    val state: StateFlow<CategoryEditorState> = _state.asStateFlow()

    private var originalImageRef: String? = null
    private var pendingCamera: ImageStore.CameraTarget? = null

    init {
        if (isEditing) {
            viewModelScope.launch {
                repository.getCategory(categoryId)?.let { category ->
                    originalImageRef = category.imageRef
                    _state.update { it.copy(loaded = true, name = category.name, imageRef = category.imageRef) }
                } ?: _state.update { it.copy(loaded = true) }
            }
        } else {
            _state.update { it.copy(loaded = true) }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }

    fun onGalleryResult(uri: Uri?) {
        if (uri == null) return
        importImage { imageStore.importFromUri(uri) }
    }

    fun newCameraTarget(): Uri {
        val target = imageStore.createCameraTarget()
        pendingCamera = target
        return target.uri
    }

    fun onCameraResult(success: Boolean) {
        val target = pendingCamera ?: return
        pendingCamera = null
        if (!success) {
            target.file.delete()
            return
        }
        importImage { imageStore.finalizeCameraCapture(target.file) }
    }

    private fun importImage(block: suspend () -> Result<String>) {
        viewModelScope.launch {
            _state.update { it.copy(importing = true, error = null) }
            block().fold(
                onSuccess = { ref ->
                    val previous = _state.value.imageRef
                    if (previous != null && previous != originalImageRef) imageStore.deleteIfLocal(previous)
                    _state.update { it.copy(imageRef = ref, importing = false) }
                },
                onFailure = { _state.update { it.copy(importing = false, error = "Couldn't use that image. Try another.") } },
            )
        }
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.name.isBlank()) return
        val imageRef = current.imageRef ?: DEFAULT_ICON
        viewModelScope.launch {
            if (isEditing) {
                val existing = repository.getCategory(categoryId) ?: return@launch
                if (existing.imageRef != imageRef) imageStore.deleteIfLocal(existing.imageRef)
                repository.updateCategory(existing.copy(name = current.name.trim(), imageRef = imageRef))
            } else {
                repository.addCategory(
                    CategoryEntity(
                        name = current.name.trim(),
                        imageRef = imageRef,
                        position = repository.nextCategoryPosition(),
                        isBuiltIn = false,
                    ),
                )
            }
            originalImageRef = imageRef
            onSaved()
        }
    }

    private companion object {
        const val NEW = -1L
        const val DEFAULT_ICON = "drawable:category_custom"
    }
}
