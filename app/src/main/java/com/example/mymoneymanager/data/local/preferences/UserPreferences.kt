package com.example.mymoneymanager.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mymoneymanager_settings"
)

class UserPreferences(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    private val safeData: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val currencyCode: Flow<String> =
        safeData.map { it[Keys.CURRENCY_CODE] ?: DEFAULT_CURRENCY_CODE }.distinctUntilChanged()

    val currencySymbol: Flow<String> =
        safeData.map { it[Keys.CURRENCY_SYMBOL] ?: DEFAULT_CURRENCY_SYMBOL }.distinctUntilChanged()

    val isAppLockEnabled: Flow<Boolean> =
        safeData.map { it[Keys.APP_LOCK_ENABLED] ?: false }.distinctUntilChanged()

    val hasPin: Flow<Boolean> =
        safeData.map { it[Keys.PIN_HASH] != null }.distinctUntilChanged()

    val isOnboardingCompleted: Flow<Boolean> =
        safeData.map { it[Keys.ONBOARDING_DONE] ?: false }.distinctUntilChanged()

    suspend fun setCurrency(code: String, symbol: String) {
        dataStore.edit {
            it[Keys.CURRENCY_CODE] = code
            it[Keys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }

    suspend fun setPin(pin: String) = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        dataStore.edit {
            it[Keys.PIN_SALT] = Base64.getEncoder().encodeToString(salt)
            it[Keys.PIN_HASH] = Base64.getEncoder().encodeToString(hash)
            it[Keys.APP_LOCK_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        val prefs = safeData.first()
        val saltB64 = prefs[Keys.PIN_SALT] ?: return@withContext false
        val hashB64 = prefs[Keys.PIN_HASH] ?: return@withContext false
        val expected = Base64.getDecoder().decode(hashB64)
        val actual = hashPin(pin, Base64.getDecoder().decode(saltB64))
        MessageDigest.isEqual(expected, actual)
    }

    suspend fun clearPin() {
        dataStore.edit {
            it.remove(Keys.PIN_HASH)
            it.remove(Keys.PIN_SALT)
            it[Keys.APP_LOCK_ENABLED] = false
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private object Keys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    private companion object {
        const val DEFAULT_CURRENCY_CODE = "IDR"
        const val DEFAULT_CURRENCY_SYMBOL = "Rp"
        const val SALT_BYTES = 16
        const val PBKDF2_ITERATIONS = 100_000
        const val HASH_BITS = 256
    }
}
