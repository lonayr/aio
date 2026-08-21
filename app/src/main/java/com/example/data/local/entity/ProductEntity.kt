package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val unit: String = "قطعة", // قطعة، كرتون، كيلو، باقة، صندوق
    val imageUrl: String = "",
    val stockQuantity: Int = 100,
    val isAvailable: Boolean = true,
    val barcode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
