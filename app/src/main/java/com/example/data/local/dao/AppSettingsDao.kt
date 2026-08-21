package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET warehouseWhatsapp = :whatsapp WHERE id = 1")
    suspend fun updateWhatsapp(whatsapp: String)

    @Query("UPDATE app_settings SET currentRole = :role, activeDelegateId = :delegateId, activeDelegateName = :delegateName WHERE id = 1")
    suspend fun updateActiveRole(role: String, delegateId: Long?, delegateName: String)

    @Query("UPDATE app_settings SET defaultCustomerName = :name, defaultCustomerPhone = :phone, defaultCustomerAddress = :address WHERE id = 1")
    suspend fun updateCustomerInfo(name: String, phone: String, address: String)
}
