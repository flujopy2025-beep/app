package com.seoaudit.app.core.data.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lifecycle observer that tracks when the app goes to background.
 *
 * When the app's process lifecycle reaches [onStop], this observer notifies the
 * [BiometricAuthManager] so it can enforce re-authentication if the app remains
 * in background beyond the configured timeout (5 minutes).
 *
 * This observer should be registered with the ProcessLifecycleOwner in the Application class.
 *
 * Requirements: 20.4
 */
@Singleton
class AppLifecycleObserver @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        biometricAuthManager.onAppBackgrounded()
    }
}
