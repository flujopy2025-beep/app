package com.seoaudit.app.feature.auth.domain.usecase

import com.seoaudit.app.core.domain.model.AuthLockoutState
import com.seoaudit.app.core.domain.model.ServiceType
import com.seoaudit.app.feature.auth.data.AuthLockoutTracker
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class AuthLockoutUseCase @Inject constructor(
    private val lockoutTracker: AuthLockoutTracker
) {
    suspend fun recordFailedAttempt(service: ServiceType): AuthLockoutState {
        return lockoutTracker.recordFailure(service, Instant.now())
    }

    suspend fun isLocked(service: ServiceType): Boolean {
        return lockoutTracker.isLocked(service, Instant.now())
    }

    suspend fun getRemainingLockoutTime(service: ServiceType): Duration? {
        return lockoutTracker.remainingTime(service, Instant.now())
    }

    suspend fun resetLockout(service: ServiceType) {
        lockoutTracker.resetLockout(service)
    }
}
