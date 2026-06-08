package com.wings.picexchange.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wings.picexchange.data.PicExchangeRepository
import com.wings.picexchange.data.image.ImageStore
import com.wings.picexchange.data.local.CardType
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardEditorState(
    val loaded: Boolean = false,
    val label: String = "",
    val imageRef: String? = null,
    val type: CardType = CardType.PICTURE,
    val importing: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = label.isNotBlank() && imageRef != null && !importing
}

@HiltViewModel
class CardEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PicExchangeRepository,
    private val imageStore: ImageStore,
) : ViewModel() {

    private val categoryId: Long = checkNotNull(savedStateHandle[Routes.ARG_CATEGORY_ID])
    private val cardId: Long = savedStateHandle.get<Long>(Routes.ARG_CARD_ID) ?: NEW
    val isEditing: Boolean = cardId > 0

    private val _state = MutableStateFlow(CardEditorState())
    val state: StateFlow<CardEditorState> = _state.asStateFlow()

    /** The image that belongs to the saved card (must not be deleted when re-picking). */
    private var originalImageRef: String? = null

    private var pendingCamera: ImageStore.CameraTarget? = null

    init {
        if (isEditing) {
            viewModelScope.launch {
                repository.getCard(cardId)?.let { card ->
                    originalImageRef = card.imageRef
                    _state.update {
                        it.copy(loaded = true, label = card.label, imageRef = card.imageRef, type = card.type)
                    }
                } ?: _state.update { it.copy(loaded = true) }
            }
        } else {
            _state.update { it.copy(loaded = true) }
        }
    }

    fun onLabelChange(value: String) = _state.update { it.copy(label = value) }

    fun onGalleryResult(uri: Uri?) {
        if (uri == null) return
        importImage { imageStore.importFromUri(uri) }
    }

    /** Creates the FileProvider target for the camera and returns its uri to launch TakePicture. */
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
                onSuccess = { ref -> replaceImage(ref) },
                onFailure = { _state.update { it.copy(importing = false, error = "Couldn't use that image. Try another.") } },
            )
        }
    }

    private fun replaceImage(ref: String) {
        val previous = _state.value.imageRef
        // delete a throwaway import that was never saved (but never the original saved image)
        if (previous != null && previous != originalImageRef) imageStore.deleteIfLocal(previous)
        _state.update { it.copy(imageRef = ref, importing = false) }
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        val imageRef = current.imageRef ?: return
        if (current.label.isBlank()) return
        viewModelScope.launch {
            if (isEditing) {
                val existing = repository.getCard(cardId) ?: return@launch
                if (existing.imageRef != imageRef) imageStore.deleteIfLocal(existing.imageRef)
                repository.updateCard(existing.copy(label = current.label.trim(), imageRef = imageRef, type = current.type))
            } else {
                repository.addCard(
                    CardEntity(
                        label = current.label.trim(),
                        type = current.type,
                        imageRef = imageRef,
                        categoryId = categoryId,
                        position = repository.nextCardPosition(categoryId),
                        isBuiltIn = false,
                    ),
                )
            }
            originalImageRef = imageRef // adopted; don't clean it up
            onSaved()
        }
    }

    private companion object {
        const val NEW = -1L
    }
}
