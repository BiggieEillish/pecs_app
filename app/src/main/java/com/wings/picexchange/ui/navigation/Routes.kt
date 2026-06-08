package com.wings.picexchange.ui.navigation

/** Navigation route definitions and builders. */
object Routes {
    const val HOME = "home"

    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_CARD_ID = "cardId"

    const val LIBRARY = "library/{$ARG_CATEGORY_ID}"
    fun library(categoryId: Long): String = "library/$categoryId"

    // categoryId is required (path); cardId is optional (query) — absent/-1 means "new card".
    const val CARD_EDITOR = "cardEditor/{$ARG_CATEGORY_ID}?$ARG_CARD_ID={$ARG_CARD_ID}"
    fun cardEditor(categoryId: Long, cardId: Long? = null): String =
        "cardEditor/$categoryId" + (cardId?.let { "?$ARG_CARD_ID=$it" } ?: "")

    // categoryId is optional (query) — absent/-1 means "new category".
    const val CATEGORY_EDITOR = "categoryEditor?$ARG_CATEGORY_ID={$ARG_CATEGORY_ID}"
    fun categoryEditor(categoryId: Long? = null): String =
        "categoryEditor" + (categoryId?.let { "?$ARG_CATEGORY_ID=$it" } ?: "")
}
