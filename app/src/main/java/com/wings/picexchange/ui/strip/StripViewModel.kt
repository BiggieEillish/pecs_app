package com.wings.picexchange.ui.strip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wings.picexchange.data.local.entity.CardEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Holds the transient sentence strip. Obtained ONCE at the app root (Activity-scoped) so the same
 * instance is shared by the bottom strip and every screen — the in-progress sentence survives
 * Home <-> Library navigation. Backed by [SavedStateHandle] so it also survives rotation /
 * process death (StripItem is Parcelable). It is never persisted to Room.
 */
@HiltViewModel
class StripViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val items: StateFlow<List<StripItem>> =
        savedStateHandle.getStateFlow(KEY_ITEMS, emptyList())

    fun add(card: CardEntity) {
        savedStateHandle[KEY_ITEMS] = ArrayList(items.value + StripItem.from(card))
    }

    fun remove(instanceId: String) {
        savedStateHandle[KEY_ITEMS] = ArrayList(items.value.filterNot { it.instanceId == instanceId })
    }

    /** Clears the strip and returns the prior contents so the caller can offer an Undo action. */
    fun clear(): List<StripItem> {
        val previous = items.value
        savedStateHandle[KEY_ITEMS] = ArrayList<StripItem>()
        return previous
    }

    fun restore(previous: List<StripItem>) {
        savedStateHandle[KEY_ITEMS] = ArrayList(previous)
    }

    private companion object {
        const val KEY_ITEMS = "strip_items"
    }
}
