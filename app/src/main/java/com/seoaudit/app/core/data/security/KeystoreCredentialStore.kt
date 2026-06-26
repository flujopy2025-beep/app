package com.seoaudit.app.core.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.seoaudit.app.core.domain.repository.CredentialRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [CredentialRepository] using Android Keystore and EncryptedSharedPreferences.
 * 
 * Credentials are encrypted using AES256-GCM via a MasterKey stored in Android Keystore,
 * providing hardware-backed encryption on supported devices (TEE/StrongBox).
 * 
 * Keys are encrypted with AES256-SIV scheme and values with AES256-GCM scheme.
 * All IO operations are performed on [Dispatchers.IO].
 */
class KeystoreCredentialStore @Inject constructor(
    @ApplicationContext private val context: Context
) : CredentialRepository {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun storeCredential(key: String, value: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                encryptedPrefs.edit().putString(key, value).apply()
            }
        }

    override suspend fun retrieveCredential(key: String): Result<String?> =
        withContext(Dispatchers.IO) {
            runCatching {
                encryptedPrefs.getString(key, null)
            }
        }

    override suspend fun deleteCredential(key: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                encryptedPrefs.edit().remove(key).apply()
            }
        }

    override suspend fun clearAll(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                encryptedPrefs.edit().clear().apply()
            }
        }

    override suspend fun exists(key: String): Boolean =
        withContext(Dispatchers.IO) {
            encryptedPrefs.contains(key)
        }

    companion object {
        private const val PREFS_FILE_NAME = "secure_credentials"
    }
}
