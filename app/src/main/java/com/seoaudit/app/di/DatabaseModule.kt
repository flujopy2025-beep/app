package com.seoaudit.app.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.seoaudit.app.core.data.db.SeoAuditDatabase
import com.seoaudit.app.core.data.db.dao.AuditReportDao
import com.seoaudit.app.core.data.db.dao.AuthLockoutDao
import com.seoaudit.app.core.data.db.dao.WpPageCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module responsible for providing database-related dependencies.
 * Configures Room with SQLCipher encryption for secure local data storage.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSupportSQLiteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory {
        val passphrase = SQLiteDatabase.getBytes("seo_audit_passphrase".toCharArray())
        return SupportFactory(passphrase)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        factory: SupportSQLiteOpenHelper.Factory
    ): SeoAuditDatabase {
        return Room.databaseBuilder(
            context,
            SeoAuditDatabase::class.java,
            "seo_audit_db"
        )
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideAuditReportDao(db: SeoAuditDatabase): AuditReportDao = db.auditReportDao()

    @Provides
    fun provideWpPageCacheDao(db: SeoAuditDatabase): WpPageCacheDao = db.wpPageCacheDao()

    @Provides
    fun provideAuthLockoutDao(db: SeoAuditDatabase): AuthLockoutDao = db.authLockoutDao()
}
