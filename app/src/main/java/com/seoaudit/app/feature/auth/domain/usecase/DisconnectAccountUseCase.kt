package com.seoaudit.app.feature.auth.domain.usecase

import com.seoaudit.app.core.domain.model.ServiceType
import com.seoaudit.app.core.domain.repository.CredentialRepository
import com.seoaudit.app.feature.gsc.data.GscTokenManager
import com.seoaudit.app.feature.wordpress.data.cache.WpCacheManager
import javax.inject.Inject

/**
 * Use case for disconnecting external service accounts.
 * Handles token revocation, credential cleanup, and cache clearing.
 *
 * Validates: Requirements 8.3, 24.6
 */
class DisconnectAccountUseCase @Inject constructor(
    private val credentialRepository: CredentialRepository,
    private val gscTokenManager: GscTokenManager,
    private val wpCacheManager: WpCacheManager
) {
    /**
     * Disconnects Google Search Console account.
     * Revokes OAuth token and clears stored credentials.
     */
    suspend fun disconnectGsc(): Result<Unit> = runCatching {
        gscTokenManager.clearTokens()
        credentialRepository.deleteCredential(ServiceType.GSC.credentialKey)
    }

    /**
     * Disconnects WordPress site.
     * Removes stored credentials and clears page cache.
     */
    suspend fun disconnectWordPress(siteUrl: String): Result<Unit> = runCatching {
        credentialRepository.deleteCredential(ServiceType.WORDPRESS.credentialKey)
        wpCacheManager.clearSiteCache(siteUrl)
    }

    /**
     * Disconnects all services and clears all credentials.
     */
    suspend fun disconnectAll(): Result<Unit> = runCatching {
        gscTokenManager.clearTokens()
        credentialRepository.clearAll()
    }
}
