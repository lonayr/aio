package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DelegateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DelegateDao {
    @Query("SELECT * FROM delegates ORDER BY createdAt DESC")
    fun getAllDelegates(): Flow<List<DelegateEntity>>

    @Query("SELECT * FROM delegates WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveDelegates(): Flow<List<DelegateEntity>>

    @Query("SELECT * FROM delegates WHERE id = :id LIMIT 1")
    suspend fun getDelegateById(id: Long): DelegateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelegate(delegate: DelegateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(delegates: List<DelegateEntity>)

    @Update
    suspend fun updateDelegate(delegate: DelegateEntity)

    @Query("UPDATE delegates SET totalOrdersCount = totalOrdersCount + 1 WHERE id = :delegateId")
    suspend fun incrementOrderCount(delegateId: Long)

    @Delete
    suspend fun deleteDelegate(delegate: DelegateEntity)

    @Query("DELETE FROM delegates WHERE id = :id")
    suspend fun deleteDelegateById(id: Long)

    @Query("SELECT COUNT(*) FROM delegates")
    suspend fun getCount(): Int
}
