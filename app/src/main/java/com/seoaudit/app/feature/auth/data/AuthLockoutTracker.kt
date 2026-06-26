package com.seoaudit.app.feature.auth.data

import com.seoaudit.app.core.data.db.dao.AuthLockoutDao
import com.seoaudit.app.core.data.db.entities.AuthLockoutEntity
import com.seoaudit.app.core.domain.model.AuthLockoutState
import com.seoaudit.app.core.domain.model.ServiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthLockoutTracker @Inject constructor(
    private val authLockoutDao: AuthLockoutDao
) {
    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }

    suspend fun recordFailure(service: ServiceType, now: Instant): AuthLockoutState =
        withContext(Dispatchers.IO) {
            val current = authLockoutDao.getLockout(service.name)
            val failedAttempts = (current?.failedAttempts ?: 0) + 1
            val lockedUntil = if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                now.plusMillis(LOCKOUT_DURATION_MS).toEpochMilli()
            } else null

            val entity = AuthLockoutEntity(
                service = service.name,
                failedAttempts = failedAttempts,
                lastAttemptAt = now.toEpochMilli(),
                lockedUntil = lockedUntil
            )
            authLockoutDao.insertOrUpdate(entity)

            AuthLockoutState(
                service = service,
                failedAttempts = failedAttempts,
                lastAttemptAt = now,
                lockedUntil = lockedUntil?.let { Instant.ofEpochMilli(it) }
            )
        }

    suspend fun isLocked(service: ServiceType, now: Instant): Boolean =
        withContext(Dispatchers.IO) {
            val lockout = authLockoutDao.getLockout(service.name) ?: return@withContext false
            val lockedUntil = lockout.lockedUntil ?: return@withContext false
            now.toEpochMilli() < lockedUntil
        }

    suspend fun remainingTime(service: ServiceType, now: Instant): java.time.Duration? =
        withContext(Dispatchers.IO) {
            val lockout = authLockoutDao.getLockout(service.name) ?: return@withContext null
            val lockedUntil = lockout.lockedUntil ?: return@withContext null
            val until = Instant.ofEpochMilli(lockedUntil)
            if (now < until) java.time.Duration.between(now, until) else null
        }

    suspend fun resetLockout(service: ServiceType) =
        withContext(Dispatchers.IO) {
            authLockoutDao.clearLockout(service.name)
        }
}
