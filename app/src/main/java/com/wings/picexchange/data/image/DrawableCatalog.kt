package com.wings.picexchange.data.image

import androidx.annotation.DrawableRes
import com.wings.picexchange.R

/**
 * Maps a built-in drawable name (the part after "drawable:" in an [com.wings.picexchange.domain.ImageRef])
 * to a compile-time R.drawable id. Every icon is referenced by its direct R field, which keeps this
 * R8/resource-shrinker safe — unlike Resources.getIdentifier, which the shrinker cannot see.
 *
 * Unknown names fail loudly (rather than silently rendering a fallback) so a missing mapping is caught
 * immediately. Each seed concept maps to its own distinct, colourful vector here; when the finalised
 * Wings Melaka icon set lands, simply re-point each name at its R.drawable entry (architecture open #6).
 */
object DrawableCatalog {

    private val byName: Map<String, Int> = mapOf(
        // Categories
        "category_starters" to R.drawable.ic_cat_starters,
        "category_food" to R.drawable.ic_cat_food,
        "category_play" to R.drawable.ic_cat_play,
        "category_feelings" to R.drawable.ic_face_happy,
        "category_custom" to R.drawable.ic_cat_custom, // default icon for user-created categories
        // Sentence starters
        "starter_want" to R.drawable.ic_starter_want,
        "starter_see" to R.drawable.ic_starter_see,
        "starter_feel" to R.drawable.ic_starter_feel,
        "starter_need" to R.drawable.ic_starter_need,
        // Picture cards
        "card_water" to R.drawable.ic_card_water,
        "card_apple" to R.drawable.ic_card_apple,
        "card_milk" to R.drawable.ic_card_milk,
        "card_cookie" to R.drawable.ic_card_cookie,
        "card_ball" to R.drawable.ic_card_ball,
        "card_book" to R.drawable.ic_card_book,
        "card_blocks" to R.drawable.ic_card_blocks,
        "card_happy" to R.drawable.ic_face_happy,
        "card_sad" to R.drawable.ic_card_sad,
        "card_tired" to R.drawable.ic_card_tired,
    )

    @DrawableRes
    fun resId(name: String): Int =
        byName[name] ?: error("Unknown built-in drawable '$name' — add it to DrawableCatalog.byName")
}
