package com.seoaudit.app.feature.audit.data.checkpoint

import com.seoaudit.app.core.data.db.dao.AuditReportDao
import com.seoaudit.app.core.data.db.entities.AuditReportEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditCheckpointManager @Inject constructor(
    private val auditReportDao: AuditReportDao
) {
    data class Checkpoint(
        val siteUrl: String,
        val totalPages: Int,
        val completedPageIds: List<Long>,
        val partialResults: List<AuditReportEntity>,
        val savedAt: Long = System.currentTimeMillis()
    )

    private var currentCheckpoint: Checkpoint? = null

    fun saveCheckpoint(checkpoint: Checkpoint) {
        currentCheckpoint = checkpoint
    }

    fun hasCheckpoint(siteUrl: String): Boolean {
        return currentCheckpoint?.siteUrl == siteUrl
    }

    fun getCheckpoint(siteUrl: String): Checkpoint? {
        return currentCheckpoint?.takeIf { it.siteUrl == siteUrl }
    }

    fun clearCheckpoint() {
        currentCheckpoint = null
    }

    fun getCompletedPageIds(): List<Long> {
        return currentCheckpoint?.completedPageIds ?: emptyList()
    }
}
