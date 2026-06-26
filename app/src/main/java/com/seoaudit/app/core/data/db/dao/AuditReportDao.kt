package com.seoaudit.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seoaudit.app.core.data.db.entities.AuditReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditReportDao {

    @Query("SELECT * FROM audit_reports WHERE page_url = :pageUrl ORDER BY audited_at DESC LIMIT 1")
    suspend fun getLatestForPage(pageUrl: String): AuditReportEntity?

    @Query("SELECT * FROM audit_reports ORDER BY audited_at DESC")
    fun observeAll(): Flow<List<AuditReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: AuditReportEntity): Long

    @Query("DELETE FROM audit_reports WHERE audited_at < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}
