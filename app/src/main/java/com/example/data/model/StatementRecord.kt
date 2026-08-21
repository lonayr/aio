package com.example.data.model

data class StatementRecord(
    val id: String,
    val timestamp: Long,
    val refNumber: String,
    val isInvoice: Boolean, // true for Invoice (Debit/مدين), false for Payment (Credit/دائن)
    val description: String,
    val amount: Double,
    val runningBalance: Double
)
