package com.wings.picexchange.ui.navigation

/** Navigation route definitions and builders. */
object Routes {
    const val HOME = "home"

    const val ARG_CATEGORY_ID = "categoryId"
    const val LIBRARY = "library/{$ARG_CATEGORY_ID}"

    fun library(categoryId: Long): String = "library/$categoryId"
}
