package com.wings.picexchange.data.settings

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PinStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val store = PinStore(context)

    @After
    fun tearDown() {
        // DataStore persists to a file; clear it so tests don't bleed into each other.
        File(context.filesDir, "datastore").deleteRecursively()
    }

    @Test
    fun noPinByDefault() = runBlocking {
        assertThat(store.isPinSet()).isFalse()
        assertThat(store.hasPin.first()).isFalse()
    }

    @Test
    fun setPinThenVerifyCorrect() = runBlocking {
        store.setPin("1234")
        assertThat(store.isPinSet()).isTrue()
        assertThat(store.verifyPin("1234")).isTrue()
    }

    @Test
    fun verifyRejectsWrongPin() = runBlocking {
        store.setPin("1234")
        assertThat(store.verifyPin("0000")).isFalse()
    }

    @Test
    fun verifyFailsWhenNoPinSet() = runBlocking {
        assertThat(store.verifyPin("1234")).isFalse()
    }
}
