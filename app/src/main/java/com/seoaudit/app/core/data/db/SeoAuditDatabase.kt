package com.seoaudit.app.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.seoaudit.app.core.data.db.dao.AuditReportDao
import com.seoaudit.app.core.data.db.dao.AuthLockoutDao
import com.seoaudit.app.core.data.db.dao.WpPageCacheDao
import com.seoaudit.app.core.data.db.entities.AuditReportEntity
import com.seoaudit.app.core.data.db.entities.AuthLockoutEntity
import com.seoaudit.app.core.data.db.entities.DiagnosticReportEntity
import com.seoaudit.app.core.data.db.entities.WpPageCacheEntity

@Database(
    entities = [
        AuditReportEntity::class,
        WpPageCacheEntity::class,
        DiagnosticReportEntity::class,
        AuthLockoutEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SeoAuditDatabase : RoomDatabase() {
    abstract fun auditReportDao(): AuditReportDao
    abstract fun wpPageCacheDao(): WpPageCacheDao
    abstract fun authLockoutDao(): AuthLockoutDao
}
