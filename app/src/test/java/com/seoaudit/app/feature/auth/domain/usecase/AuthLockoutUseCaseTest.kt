package com.seoaudit.app.feature.auth.domain.usecase

import com.seoaudit.app.core.domain.model.AuthLockoutState
import com.seoaudit.app.core.domain.model.ServiceType
import com.seoaudit.app.feature.auth.data.AuthLockoutTracker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class AuthLockoutUseCaseTest {

    private lateinit var lockoutTracker: AuthLockoutTracker
    private lateinit var useCase: AuthLockoutUseCase

    @BeforeEach
    fun setUp() {
        lockoutTracker = mockk(relaxed = true)
        useCase = AuthLockoutUseCase(lockoutTracker)
    }

    @Test
    fun `recordFailedAttempt delegates to tracker with current time`() = runTest {
        val expectedState = AuthLockoutState(
            service = ServiceType.GSC,
            failedAttempts = 3,
            lastAttemptAt = Instant.now(),
            lockedUntil = null
        )
        coEvery { lockoutTracker.recordFailure(ServiceType.GSC, any()) } returns expectedState

        val result = useCase.recordFailedAttempt(ServiceType.GSC)

        assertEquals(expectedState, result)
        coVerify { lockoutTracker.recordFailure(ServiceType.GSC, any()) }
    }

    @Test
    fun `isLocked delegates to tracker with current time`() = runTest {
        coEvery { lockoutTracker.isLocked(ServiceType.WORDPRESS, any()) } returns true

        val result = useCase.isLocked(ServiceType.WORDPRESS)

        assertTrue(result)
        coVerify { lockoutTracker.isLocked(ServiceType.WORDPRESS, any()) }
    }

    @Test
    fun `isLocked returns false when not locked`() = runTest {
        coEvery { lockoutTracker.isLocked(ServiceType.GSC, any()) } returns false

        val result = useCase.isLocked(ServiceType.GSC)

        assertFalse(result)
    }

    @Test
    fun `getRemainingLockoutTime delegates to tracker`() = runTest {
        val expectedDuration = Duration.ofMinutes(3)
        coEvery { lockoutTracker.remainingTime(ServiceType.GSC, any()) } returns expectedDuration

        val result = useCase.getRemainingLockoutTime(ServiceType.GSC)

        assertEquals(expectedDuration, result)
        coVerify { lockoutTracker.remainingTime(ServiceType.GSC, any()) }
    }

    @Test
    fun `getRemainingLockoutTime returns null when not locked`() = runTest {
        coEvery { lockoutTracker.remainingTime(ServiceType.GSC, any()) } returns null

        val result = useCase.getRemainingLockoutTime(ServiceType.GSC)

        assertNull(result)
    }

    @Test
    fun `resetLockout delegates to tracker`() = runTest {
        useCase.resetLockout(ServiceType.WORDPRESS)

        coVerify { lockoutTracker.resetLockout(ServiceType.WORDPRESS) }
    }
}
