package com.seoaudit.app.core.domain.repository

/**
 * Repository interface for secure credential storage operations.
 * Provides methods to store, retrieve, delete and manage credentials
 * backed by encrypted storage (Android Keystore + EncryptedSharedPreferences).
 */
interface CredentialRepository {
    /**
     * Stores a credential value associated with the given key.
     * If a credential with the same key already exists, it will be overwritten.
     *
     * @param key The unique identifier for the credential
     * @param value The credential value to store
     * @return Result.success(Unit) if stored successfully, Result.failure otherwise
     */
    suspend fun storeCredential(key: String, value: String): Result<Unit>

    /**
     * Retrieves the credential value associated with the given key.
     *
     * @param key The unique identifier for the credential
     * @return Result containing the credential value, or null if not found
     */
    suspend fun retrieveCredential(key: String): Result<String?>

    /**
     * Deletes the credential associated with the given key.
     *
     * @param key The unique identifier for the credential to delete
     * @return Result.success(Unit) if deleted successfully, Result.failure otherwise
     */
    suspend fun deleteCredential(key: String): Result<Unit>

    /**
     * Clears all stored credentials.
     *
     * @return Result.success(Unit) if cleared successfully, Result.failure otherwise
     */
    suspend fun clearAll(): Result<Unit>

    /**
     * Checks whether a credential with the given key exists in storage.
     *
     * @param key The unique identifier to check
     * @return true if a credential with the key exists, false otherwise
     */
    suspend fun exists(key: String): Boolean
}
