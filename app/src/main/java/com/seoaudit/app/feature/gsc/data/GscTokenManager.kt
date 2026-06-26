package com.seoaudit.app.feature.gsc.data

import com.seoaudit.app.core.domain.repository.CredentialRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the OAuth 2.0 access token for Google Search Console.
 *
 * Caches the token in-memory and refreshes it silently before expiration.
 * Tokens are considered expired at 55 minutes (Google tokens expire at 60 min).
 */
@Singleton
class GscTokenManager @Inject constructor(
    private val credentialRepository: CredentialRepository,
    private val clock: Clock
) {
    private var cachedToken: String? = null
    private var expiresAt: Instant = Instant.EPOCH

    companion object {
        /** Token validity window: refresh at 55 min to avoid edge cases. */
        private const val TOKEN_VALIDITY_SECONDS = 55L * 60L
        private const val REFRESH_TOKEN_KEY = "gsc_refresh_token"
    }

    /**
     * Returns a valid access token, refreshing if necessary.
     * @throws AuthTokenException if unable to obtain a valid token.
     */
    suspend fun getValidToken(): String {
        val now = clock.instant()
        val token = cachedToken
        if (token != null && now.isBefore(expiresAt)) {
            return token
        }
        return refreshToken()
    }

    /**
     * Updates the cached token after a successful sign-in.
     */
    fun updateToken(accessToken: String) {
        cachedToken = accessToken
        expiresAt = clock.instant().plusSeconds(TOKEN_VALIDITY_SECONDS)
    }

    /**
     * Checks whether the current token is expired.
     */
    fun isTokenExpired(): Boolean {
        return clock.instant() >= expiresAt
    }

    /**
     * Stores the refresh token securely.
     */
    suspend fun storeRefreshToken(refreshToken: String) {
        credentialRepository.storeCredential(REFRESH_TOKEN_KEY, refreshToken)
    }

    /**
     * Clears all cached tokens and stored refresh tokens.
     */
    suspend fun clearTokens() {
        cachedToken = null
        expiresAt = Instant.EPOCH
        credentialRepository.deleteCredential(REFRESH_TOKEN_KEY)
    }

    /**
     * Attempts silent refresh using the stored refresh token.
     * This method is intended to be overridden or delegated to
     * the Google Sign-In silent sign-in mechanism.
     */
    private suspend fun refreshToken(): String {
        val refreshToken = credentialRepository
            .retrieveCredential(REFRESH_TOKEN_KEY)
            .getOrNull()
            ?: throw AuthTokenException("No refresh token available")

        // In production, this would call Google's token endpoint.
        // The actual silent sign-in is triggered from the UI layer.
        throw AuthTokenException(
            "Silent refresh required. Trigger sign-in from UI."
        )
    }
}

/**
 * Exception thrown when token operations fail.
 */
class AuthTokenException(message: String) : Exception(message)
