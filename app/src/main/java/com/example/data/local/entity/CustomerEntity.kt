package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registryNumber: String = "", // رقم قيد الزبون e.g. CUS-101
    val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val openingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
