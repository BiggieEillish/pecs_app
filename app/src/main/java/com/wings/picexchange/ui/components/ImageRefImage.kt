package com.wings.picexchange.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.wings.picexchange.data.image.DrawableCatalog
import com.wings.picexchange.domain.ImageRef
import java.io.File

/**
 * Renders an [ImageRef]-encoded image string through a single Coil path:
 *  - "drawable:<name>" resolves to a bundled R.drawable id via [DrawableCatalog]
 *  - "file:<path>" resolves to a [File] in app-internal storage
 * Both built-in and (future) custom images flow through the same [AsyncImage].
 */
@Composable
fun ImageRefImage(
    imageRef: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val model: Any? = remember(imageRef) {
        when (val ref = ImageRef.parse(imageRef)) {
            is ImageRef.Drawable -> DrawableCatalog.resId(ref.name)
            is ImageRef.LocalFile -> File(ref.path)
            null -> null
        }
    }
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
