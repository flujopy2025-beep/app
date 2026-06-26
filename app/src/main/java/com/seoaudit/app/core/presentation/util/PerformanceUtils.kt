package com.seoaudit.app.core.presentation.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Performance utilities for maintaining 60 FPS and optimizing resource usage.
 */
object PerformanceUtils {
    /** Max RAM budget for active analysis */
    const val MAX_RAM_MB = 256

    /** Cold start target */
    const val COLD_START_TARGET_MS = 2000L

    /** Batch size for network operations to optimize battery */
    const val NETWORK_BATCH_SIZE = 10

    /** Ensure heavy computation runs on Default dispatcher */
    suspend fun <T> computeOnDefault(block: suspend () -> T): T =
        withContext(Dispatchers.Default) { block() }

    /** Ensure IO runs on IO dispatcher */
    suspend fun <T> ioOperation(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}
