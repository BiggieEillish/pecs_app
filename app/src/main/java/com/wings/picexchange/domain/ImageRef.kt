package com.wings.picexchange.domain

/**
 * A typed reference to a card/category image, stored as one string so built-in and custom
 * images share a single render path (resolved via Coil in M2):
 *  - "drawable:<name>"  → a built-in drawable bundled with the app (see [DrawableCatalog])
 *  - "file:<path>"      → a user-imported image in app-internal storage
 *
 * Pure/framework-free so it can be unit-tested on the JVM.
 */
sealed interface ImageRef {
    data class Drawable(val name: String) : ImageRef
    data class LocalFile(val path: String) : ImageRef

    fun encode(): String = when (this) {
        is Drawable -> "$DRAWABLE_PREFIX$name"
        is LocalFile -> "$FILE_PREFIX$path"
    }

    companion object {
        const val DRAWABLE_PREFIX = "drawable:"
        const val FILE_PREFIX = "file:"

        /** Parses a stored ref string, or returns null if it is blank/unrecognised. */
        fun parse(raw: String): ImageRef? = when {
            raw.startsWith(DRAWABLE_PREFIX) ->
                raw.removePrefix(DRAWABLE_PREFIX).takeIf { it.isNotBlank() }?.let(::Drawable)
            raw.startsWith(FILE_PREFIX) ->
                raw.removePrefix(FILE_PREFIX).takeIf { it.isNotBlank() }?.let(::LocalFile)
            else -> null
        }
    }
}
