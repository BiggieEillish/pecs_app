package com.wings.picexchange.data.image

import com.wings.picexchange.data.local.SeedData
import com.wings.picexchange.domain.ImageRef
import org.junit.Assert.assertThrows
import org.junit.Test

class DrawableCatalogTest {

    /** Every drawable name referenced by seed data must have a catalog entry (no silent fallback). */
    @Test
    fun everySeededDrawableNameIsMapped() {
        val names = (SeedData.categories.map { it.imageRef } + SeedData.cards.map { it.imageRef })
            .mapNotNull { ImageRef.parse(it) as? ImageRef.Drawable }
            .map { it.name }
        names.forEach { DrawableCatalog.resId(it) } // throws if any name is missing
    }

    @Test
    fun unknownNameFailsLoudly() {
        assertThrows(IllegalStateException::class.java) {
            DrawableCatalog.resId("does_not_exist")
        }
    }
}
