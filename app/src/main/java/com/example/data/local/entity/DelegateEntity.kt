package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delegates")
data class DelegateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val code: String, // e.g. DEL-101
    val area: String, // e.g. بغداد - الكرخ
    val notes: String = "",
    val isActive: Boolean = true,
    val totalOrdersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
