package com.wings.picexchange.data.settings

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

private val Context.parentalLockStore: DataStore<Preferences> by preferencesDataStore(name = "parental_lock")

/**
 * Persists a parental PIN as a per-install salted SHA-256 hash. This is a local CHILD-LOCK
 * (obfuscation, NOT authentication): there are no accounts and no recovery beyond clearing app
 * data. It exists only to stop a child from flipping into Teacher mode and editing content.
 */
@Singleton
class PinStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store get() = context.parentalLockStore

    val hasPin: Flow<Boolean> = store.data.map { it[KEY_HASH] != null }

    suspend fun isPinSet(): Boolean = store.data.first()[KEY_HASH] != null

    suspend fun setPin(pin: String) {
        store.edit { prefs ->
            val salt = prefs[KEY_SALT] ?: newSalt().also { prefs[KEY_SALT] = it }
            prefs[KEY_HASH] = hash(pin, salt)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = store.data.first()
        val salt = prefs[KEY_SALT] ?: return false
        val stored = prefs[KEY_HASH] ?: return false
        return stored == hash(pin, salt)
    }

    private fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((salt + pin).toByteArray())
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun newSalt(): String {
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private companion object {
        val KEY_HASH = stringPreferencesKey("pin_hash")
        val KEY_SALT = stringPreferencesKey("pin_salt")
    }
}
