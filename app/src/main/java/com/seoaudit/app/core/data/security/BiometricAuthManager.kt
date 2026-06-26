package com.seoaudit.app.core.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages biometric/PIN authentication flow for protecting access to credentials.
 *
 * Implements timeout logic: re-authentication is required after the app has been
 * in background for more than 5 minutes (AUTH_TIMEOUT_MS).
 *
 * Integrates with [CredentialRepository] to trigger verification before accessing
 * sensitive data.
 *
 * Requirements: 20.4, 20.5
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var lastAuthenticatedAt: Long = 0L

    companion object {
        const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    /**
     * Determines if re-authentication is required based on elapsed time
     * since last successful authentication.
     *
     * @return true if elapsed time exceeds AUTH_TIMEOUT_MS (5 minutes)
     */
    fun isAuthenticationRequired(): Boolean {
        val elapsed = System.currentTimeMillis() - lastAuthenticatedAt
        return elapsed > AUTH_TIMEOUT_MS
    }

    /**
     * Checks whether biometric or device credential authentication is available
     * on the current device.
     *
     * @return true if BIOMETRIC_STRONG or DEVICE_CREDENTIAL authenticators are available
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Records a successful authentication event, updating the timestamp.
     */
    fun onAuthenticationSuccess() {
        lastAuthenticatedAt = System.currentTimeMillis()
    }

    /**
     * Called when the app transitions to background.
     * The timeout is tracked via [lastAuthenticatedAt] — no additional action needed.
     */
    fun onAppBackgrounded() {
        // timestamp is already tracked via lastAuthenticatedAt
    }

    /**
     * Creates a [BiometricPrompt] instance bound to the given activity with
     * appropriate callbacks for success and error handling.
     *
     * @param activity The FragmentActivity hosting the biometric prompt
     * @param onSuccess Callback invoked on successful authentication
     * @param onError Callback invoked on authentication error with error message
     * @return Configured BiometricPrompt ready to authenticate
     */
    fun createBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onAuthenticationSuccess()
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                // User can retry — no action needed here
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Builds the [BiometricPrompt.PromptInfo] with localized title and subtitle,
     * allowing both biometric and device credential (PIN/pattern/password) authenticators.
     *
     * @return Configured PromptInfo for the biometric dialog
     */
    fun getPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación requerida")
            .setSubtitle("Verifique su identidad para acceder a las credenciales")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }
}
