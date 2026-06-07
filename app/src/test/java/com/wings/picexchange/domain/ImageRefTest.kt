package com.wings.picexchange.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImageRefTest {

    @Test
    fun parsesDrawableRef() {
        assertThat(ImageRef.parse("drawable:card_water"))
            .isEqualTo(ImageRef.Drawable("card_water"))
    }

    @Test
    fun parsesFileRef() {
        assertThat(ImageRef.parse("file:/data/user/0/app/files/abc.png"))
            .isEqualTo(ImageRef.LocalFile("/data/user/0/app/files/abc.png"))
    }

    @Test
    fun rejectsUnknownOrBlankRefs() {
        assertThat(ImageRef.parse("http://example.com/x.png")).isNull()
        assertThat(ImageRef.parse("drawable:")).isNull()
        assertThat(ImageRef.parse("file:")).isNull()
        assertThat(ImageRef.parse("")).isNull()
    }

    @Test
    fun encodeRoundTrips() {
        val drawable = ImageRef.Drawable("card_apple")
        assertThat(ImageRef.parse(drawable.encode())).isEqualTo(drawable)

        val file = ImageRef.LocalFile("/files/photo.png")
        assertThat(ImageRef.parse(file.encode())).isEqualTo(file)
    }

    @Test
    fun roundTripsFilePathContainingColons() {
        val file = ImageRef.LocalFile("/data/user/0/com.wings.picexchange/files/a:b.png")
        assertThat(ImageRef.parse(file.encode())).isEqualTo(file)
    }
}
