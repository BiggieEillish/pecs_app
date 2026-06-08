package com.wings.picexchange.ui.strip

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.wings.picexchange.data.local.CardType
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.domain.spokenSentence
import org.junit.Test

class StripViewModelTest {

    private fun viewModel() = StripViewModel(SavedStateHandle())

    private fun card(id: Long, label: String, type: CardType = CardType.PICTURE) =
        CardEntity(id = id, label = label, type = type, imageRef = "drawable:card_$label", categoryId = 1, position = 0)

    @Test
    fun addsCardsInOrder() {
        val vm = viewModel()
        vm.add(card(1, "I want", CardType.SENTENCE_STARTER))
        vm.add(card(2, "drink"))
        assertThat(vm.items.value.map { it.label }).containsExactly("I want", "drink").inOrder()
    }

    @Test
    fun snapshotsCardDetailsAtAddTime() {
        val vm = viewModel()
        vm.add(card(5, "apple"))
        val item = vm.items.value.single()
        assertThat(item.cardId).isEqualTo(5)
        assertThat(item.label).isEqualTo("apple")
        assertThat(item.imageRef).isEqualTo("drawable:card_apple")
    }

    @Test
    fun sameCardCanAppearTwiceWithDistinctInstanceIds() {
        val vm = viewModel()
        vm.add(card(2, "drink"))
        vm.add(card(2, "drink"))
        val instanceIds = vm.items.value.map { it.instanceId }
        assertThat(instanceIds).hasSize(2)
        assertThat(instanceIds.toSet()).hasSize(2)
    }

    @Test
    fun removesByInstanceId() {
        val vm = viewModel()
        vm.add(card(1, "I want", CardType.SENTENCE_STARTER))
        vm.add(card(2, "drink"))
        val firstId = vm.items.value.first().instanceId
        vm.remove(firstId)
        assertThat(vm.items.value.map { it.label }).containsExactly("drink")
    }

    @Test
    fun clearEmptiesAndUndoRestores() {
        val vm = viewModel()
        vm.add(card(2, "drink"))
        val previous = vm.clear()
        assertThat(vm.items.value).isEmpty()
        vm.restore(previous)
        assertThat(vm.items.value.map { it.label }).containsExactly("drink")
    }

    @Test
    fun stripLabelsFeedSpokenSentence() {
        val vm = viewModel()
        vm.add(card(1, "I want", CardType.SENTENCE_STARTER))
        vm.add(card(2, "drink"))
        assertThat(spokenSentence(vm.items.value.map { it.label })).isEqualTo("I want drink")
    }
}
