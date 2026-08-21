package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CustomerPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerPaymentDao {

    @Query("SELECT * FROM customer_payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getPaymentsByCustomerId(customerId: Long): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: Long): CustomerPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CustomerPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<CustomerPaymentEntity>)

    @Update
    suspend fun updatePayment(payment: CustomerPaymentEntity)

    @Delete
    suspend fun deletePayment(payment: CustomerPaymentEntity)

    @Query("DELETE FROM customer_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}
