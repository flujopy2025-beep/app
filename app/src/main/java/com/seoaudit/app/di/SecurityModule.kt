package com.seoaudit.app.di

import com.seoaudit.app.core.data.security.KeystoreCredentialStore
import com.seoaudit.app.core.domain.repository.CredentialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module responsible for providing security-related dependencies.
 * Binds the [KeystoreCredentialStore] implementation to the [CredentialRepository] interface,
 * providing encrypted credential storage backed by Android Keystore and EncryptedSharedPreferences.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindCredentialRepository(
        impl: KeystoreCredentialStore
    ): CredentialRepository
}
