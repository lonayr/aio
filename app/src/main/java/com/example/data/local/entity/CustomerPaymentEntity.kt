package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_payments")
data class CustomerPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val amount: Double,
    val receiptNumber: String = "",
    val paymentMethod: String = "نقداً (Cash)",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
