package com.wings.picexchange.data.image

import androidx.annotation.DrawableRes
import com.wings.picexchange.R

/**
 * Maps a built-in drawable name (the part after "drawable:" in an [com.wings.picexchange.domain.ImageRef])
 * to a compile-time R.drawable id. Every icon is referenced by its direct R field, which keeps this
 * R8/resource-shrinker safe — unlike Resources.getIdentifier, which the shrinker cannot see.
 *
 * Unknown names fail loudly (rather than silently rendering a fallback) so a missing mapping is caught
 * immediately. Seed cards currently share three PLACEHOLDER icons; when the finalised Wings Melaka icon
 * set lands, point each name at its own R.drawable entry here (architecture open question #6).
 */
object DrawableCatalog {

    private val byName: Map<String, Int> = mapOf(
        // Categories
        "category_starters" to R.drawable.ic_seed_category,
        "category_food" to R.drawable.ic_seed_category,
        "category_play" to R.drawable.ic_seed_category,
        "category_feelings" to R.drawable.ic_seed_category,
        // Sentence starters
        "starter_want" to R.drawable.ic_seed_starter,
        "starter_see" to R.drawable.ic_seed_starter,
        "starter_feel" to R.drawable.ic_seed_starter,
        "starter_need" to R.drawable.ic_seed_starter,
        // Picture cards
        "card_water" to R.drawable.ic_seed_card,
        "card_apple" to R.drawable.ic_seed_card,
        "card_milk" to R.drawable.ic_seed_card,
        "card_cookie" to R.drawable.ic_seed_card,
        "card_ball" to R.drawable.ic_seed_card,
        "card_book" to R.drawable.ic_seed_card,
        "card_blocks" to R.drawable.ic_seed_card,
        "card_happy" to R.drawable.ic_seed_card,
        "card_sad" to R.drawable.ic_seed_card,
        "card_tired" to R.drawable.ic_seed_card,
    )

    @DrawableRes
    fun resId(name: String): Int =
        byName[name] ?: error("Unknown built-in drawable '$name' — add it to DrawableCatalog.byName")
}
