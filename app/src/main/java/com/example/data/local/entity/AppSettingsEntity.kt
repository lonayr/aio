package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val warehouseWhatsapp: String = "9647701234567",
    val storeName: String = "مخزن التجهيز المركزي",
    val currency: String = "د.ع",
    val defaultCustomerName: String = "",
    val defaultCustomerPhone: String = "",
    val defaultCustomerAddress: String = "",
    val currentRole: String = "ADMIN", // ADMIN, DELEGATE, CUSTOMER
    val activeDelegateId: Long? = null,
    val activeDelegateName: String = ""
)
