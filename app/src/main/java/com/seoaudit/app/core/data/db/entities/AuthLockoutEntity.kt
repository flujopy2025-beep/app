package com.seoaudit.app.core.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_lockout")
data class AuthLockoutEntity(
    @PrimaryKey val service: String,
    @ColumnInfo(name = "failed_attempts") val failedAttempts: Int,
    @ColumnInfo(name = "last_attempt_at") val lastAttemptAt: Long?,
    @ColumnInfo(name = "locked_until") val lockedUntil: Long?
)
