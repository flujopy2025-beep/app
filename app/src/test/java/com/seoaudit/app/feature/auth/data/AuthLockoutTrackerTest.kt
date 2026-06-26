package com.seoaudit.app.feature.auth.data

import com.seoaudit.app.core.data.db.dao.AuthLockoutDao
import com.seoaudit.app.core.data.db.entities.AuthLockoutEntity
import com.seoaudit.app.core.domain.model.ServiceType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class AuthLockoutTrackerTest {

    private lateinit var authLockoutDao: AuthLockoutDao
    private lateinit var tracker: AuthLockoutTracker

    @BeforeEach
    fun setUp() {
        authLockoutDao = mockk(relaxed = true)
        tracker = AuthLockoutTracker(authLockoutDao)
    }

    @Test
    fun `recordFailure increments failed attempts from zero`() = runTest {
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns null

        val result = tracker.recordFailure(ServiceType.GSC, Instant.ofEpochMilli(1000000))

        assertEquals(1, result.failedAttempts)
        assertEquals(ServiceType.GSC, result.service)
        assertNull(result.lockedUntil)
    }

    @Test
    fun `recordFailure increments existing failed attempts`() = runTest {
        coEvery { authLockoutDao.getLockout(ServiceType.WORDPRESS.name) } returns AuthLockoutEntity(
            service = ServiceType.WORDPRESS.name,
            failedAttempts = 3,
            lastAttemptAt = 900000L,
            lockedUntil = null
        )

        val result = tracker.recordFailure(ServiceType.WORDPRESS, Instant.ofEpochMilli(1000000))

        assertEquals(4, result.failedAttempts)
        assertNull(result.lockedUntil)
    }

    @Test
    fun `recordFailure locks account at 5 failed attempts`() = runTest {
        val now = Instant.ofEpochMilli(1000000)
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns AuthLockoutEntity(
            service = ServiceType.GSC.name,
            failedAttempts = 4,
            lastAttemptAt = 900000L,
            lockedUntil = null
        )

        val result = tracker.recordFailure(ServiceType.GSC, now)

        assertEquals(5, result.failedAttempts)
        assertNotNull(result.lockedUntil)
        assertEquals(
            now.plusMillis(AuthLockoutTracker.LOCKOUT_DURATION_MS),
            result.lockedUntil
        )
    }

    @Test
    fun `recordFailure persists entity to dao`() = runTest {
        val entitySlot = slot<AuthLockoutEntity>()
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns null
        coEvery { authLockoutDao.insertOrUpdate(capture(entitySlot)) } returns Unit

        val now = Instant.ofEpochMilli(5000000)
        tracker.recordFailure(ServiceType.GSC, now)

        val saved = entitySlot.captured
        assertEquals(ServiceType.GSC.name, saved.service)
        assertEquals(1, saved.failedAttempts)
        assertEquals(now.toEpochMilli(), saved.lastAttemptAt)
        assertNull(saved.lockedUntil)
    }

    @Test
    fun `isLocked returns false when no lockout exists`() = runTest {
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns null

        val result = tracker.isLocked(ServiceType.GSC, Instant.now())

        assertFalse(result)
    }

    @Test
    fun `isLocked returns false when lockedUntil is null`() = runTest {
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns AuthLockoutEntity(
            service = ServiceType.GSC.name,
            failedAttempts = 3,
            lastAttemptAt = 1000000L,
            lockedUntil = null
        )

        val result = tracker.isLocked(ServiceType.GSC, Instant.now())

        assertFalse(result)
    }

    @Test
    fun `isLocked returns true when current time is before lockedUntil`() = runTest {
        val lockedUntil = Instant.now().plusMillis(60000).toEpochMilli()
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns AuthLockoutEntity(
            service = ServiceType.GSC.name,
            failedAttempts = 5,
            lastAttemptAt = 1000000L,
            lockedUntil = lockedUntil
        )

        val result = tracker.isLocked(ServiceType.GSC, Instant.now())

        assertTrue(result)
    }

    @Test
    fun `isLocked returns false when lockout has expired`() = runTest {
        val lockedUntil = Instant.now().minusMillis(60000).toEpochMilli()
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns AuthLockoutEntity(
            service = ServiceType.GSC.name,
            failedAttempts = 5,
            lastAttemptAt = 1000000L,
            lockedUntil = lockedUntil
        )

        val result = tracker.isLocked(ServiceType.GSC, Instant.now())

        assertFalse(result)
    }

    @Test
    fun `remainingTime returns null when no lockout exists`() = runTest {
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns null

        val result = tracker.remainingTime(ServiceType.GSC, Instant.now())

        assertNull(result)
    }

    @Test
    fun `remainingTime returns duration when locked`() = runTest {
        val now = Instant.ofEpochMilli(1000000)
        val lockedUntil = now.plusMillis(120000).toEpochMilli() // 2 minutes from now
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns AuthLockoutEntity(
            service = ServiceType.GSC.name,
            failedAttempts = 5,
            lastAttemptAt = 900000L,
            lockedUntil = lockedUntil
        )

        val result = tracker.remainingTime(ServiceType.GSC, now)

        assertNotNull(result)
        assertEquals(120000L, result!!.toMillis())
    }

    @Test
    fun `remainingTime returns null when lockout has expired`() = runTest {
        val now = Instant.ofEpochMilli(2000000)
        val lockedUntil = now.minusMillis(60000).toEpochMilli() // 1 minute ago
        coEvery { authLockoutDao.getLockout(ServiceType.GSC.name) } returns AuthLockoutEntity(
            service = ServiceType.GSC.name,
            failedAttempts = 5,
            lastAttemptAt = 900000L,
            lockedUntil = lockedUntil
        )

        val result = tracker.remainingTime(ServiceType.GSC, now)

        assertNull(result)
    }

    @Test
    fun `resetLockout clears the lockout from dao`() = runTest {
        tracker.resetLockout(ServiceType.WORDPRESS)

        coVerify { authLockoutDao.clearLockout(ServiceType.WORDPRESS.name) }
    }

    @Test
    fun `lockout duration is 5 minutes`() {
        assertEquals(5 * 60 * 1000L, AuthLockoutTracker.LOCKOUT_DURATION_MS)
    }

    @Test
    fun `max failed attempts threshold is 5`() {
        assertEquals(5, AuthLockoutTracker.MAX_FAILED_ATTEMPTS)
    }
}
