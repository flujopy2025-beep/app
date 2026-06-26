package com.seoaudit.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seoaudit.app.core.data.db.entities.AuthLockoutEntity

@Dao
interface AuthLockoutDao {

    @Query("SELECT * FROM auth_lockout WHERE service = :service")
    suspend fun getLockout(service: String): AuthLockoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(lockout: AuthLockoutEntity)

    @Query("DELETE FROM auth_lockout WHERE service = :service")
    suspend fun clearLockout(service: String)
}
