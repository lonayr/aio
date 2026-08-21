package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerRegistryNumber: String = "",
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val customerCity: String = "",
    val delegateId: Long? = null,
    val delegateName: String = "",
    val delegateRegistryNumber: String = "",
    val supplierId: Long? = null,
    val supplierName: String = "",
    val supplierPhone: String = "",
    val supplierRegistryNumber: String = "",
    val itemsSummary: String, // e.g. "أرز بسمتي (2)، زيت طعام (1)"
    val itemsDetailedJson: String, // Full JSON list of items
    val totalAmount: Double,
    val itemsCount: Int,
    val customerNotes: String = "",
    val status: String = "PREPARING", // PREPARING, COMPLETED, CANCELLED
    val whatsappSent: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
