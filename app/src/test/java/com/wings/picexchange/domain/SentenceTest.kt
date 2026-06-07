package com.wings.picexchange.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SentenceTest {

    @Test
    fun joinsLabelsWithSingleSpaces() {
        assertThat(spokenSentence(listOf("I want", "drink"))).isEqualTo("I want drink")
    }

    @Test
    fun trimsLabelsAndDropsBlankOnes() {
        assertThat(spokenSentence(listOf("  I want ", "", "   ", "toy"))).isEqualTo("I want toy")
    }

    @Test
    fun emptyStripProducesEmptyString() {
        assertThat(spokenSentence(emptyList())).isEqualTo("")
    }
}
