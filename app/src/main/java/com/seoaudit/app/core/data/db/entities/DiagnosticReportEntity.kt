package com.seoaudit.app.core.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostic_reports")
data class DiagnosticReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "site_url") val siteUrl: String,
    @ColumnInfo(name = "problems_json") val problemsJson: String,
    @ColumnInfo(name = "generated_at") val generatedAt: Long
)
