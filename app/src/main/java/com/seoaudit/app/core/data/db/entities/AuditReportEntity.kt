package com.seoaudit.app.core.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_reports")
data class AuditReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "page_url") val pageUrl: String,
    @ColumnInfo(name = "page_title") val pageTitle: String,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "issues_json") val issuesJson: String,
    @ColumnInfo(name = "metrics_json") val metricsJson: String?,
    @ColumnInfo(name = "audited_at") val auditedAt: Long
)
