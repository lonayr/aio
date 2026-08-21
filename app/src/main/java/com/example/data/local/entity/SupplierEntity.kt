package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registryNumber: String = "", // رقم قيد المجهز e.g. SUP-101
    val name: String,             // اسم المجهز / الشخص المسؤول
    val phone: String,            // رقم هاتف / واتساب المجهز للتجهيز
    val companyName: String = "", // اسم الشركة المجهزة أو المخزن
    val category: String = "",    // اختصاص التجهيز (مواد غذائية، منظفات، أدوات، عام)
    val address: String = "",     // عنوان المخزن أو الشركة
    val notes: String = "",       // ملاحظات
    val isActive: Boolean = true,
    val ordersCount: Int = 0,     // عدد الطلبات المجهزة من خلاله
    val createdAt: Long = System.currentTimeMillis()
)
