package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.DelegateEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class StoreRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val customerPaymentDao = database.customerPaymentDao()
    private val delegateDao = database.delegateDao()
    private val supplierDao = database.supplierDao()
    private val orderDao = database.orderDao()
    private val appSettingsDao = database.appSettingsDao()

    // Suppliers (المجهزون والموردون)
    val allSuppliers: Flow<List<com.example.data.local.entity.SupplierEntity>> = supplierDao.getAllSuppliers()

    suspend fun insertSupplier(supplier: com.example.data.local.entity.SupplierEntity): Long =
        supplierDao.insert(supplier)

    suspend fun updateSupplier(supplier: com.example.data.local.entity.SupplierEntity) =
        supplierDao.update(supplier)

    suspend fun deleteSupplier(supplier: com.example.data.local.entity.SupplierEntity) =
        supplierDao.delete(supplier)

    // Customers (الزبائن والعملاء)
    val allCustomers: Flow<List<com.example.data.local.entity.CustomerEntity>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<com.example.data.local.entity.CustomerEntity>> =
        customerDao.searchCustomers(query)

    suspend fun insertCustomer(customer: com.example.data.local.entity.CustomerEntity): Long =
        customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: com.example.data.local.entity.CustomerEntity) =
        customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: com.example.data.local.entity.CustomerEntity) =
        customerDao.deleteCustomer(customer)

    // Customer Payments & Receipts (سندات القبض وتسديدات الزبائن)
    val allCustomerPayments: Flow<List<com.example.data.local.entity.CustomerPaymentEntity>> = customerPaymentDao.getAllPayments()

    fun getPaymentsByCustomerId(customerId: Long): Flow<List<com.example.data.local.entity.CustomerPaymentEntity>> =
        customerPaymentDao.getPaymentsByCustomerId(customerId)

    suspend fun insertPayment(payment: com.example.data.local.entity.CustomerPaymentEntity): Long =
        customerPaymentDao.insertPayment(payment)

    suspend fun updatePayment(payment: com.example.data.local.entity.CustomerPaymentEntity) =
        customerPaymentDao.updatePayment(payment)

    suspend fun deletePayment(payment: com.example.data.local.entity.CustomerPaymentEntity) =
        customerPaymentDao.deletePayment(payment)

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val availableProducts: Flow<List<ProductEntity>> = productDao.getAvailableProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        if (category == "الكل" || category.isBlank()) productDao.getAllProducts()
        else productDao.getProductsByCategory(category)

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        productDao.searchProducts(query)

    suspend fun deductStock(productId: Long, quantity: Int) =
        productDao.deductStock(productId, quantity)

    suspend fun updateStock(productId: Long, newStock: Int) =
        productDao.updateStock(productId, newStock)

    suspend fun insertProduct(product: ProductEntity): Long =
        productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) =
        productDao.deleteProduct(product)

    // Delegates
    val allDelegates: Flow<List<DelegateEntity>> = delegateDao.getAllDelegates()
    val activeDelegates: Flow<List<DelegateEntity>> = delegateDao.getActiveDelegates()

    suspend fun insertDelegate(delegate: DelegateEntity): Long =
        delegateDao.insertDelegate(delegate)

    suspend fun updateDelegate(delegate: DelegateEntity) =
        delegateDao.updateDelegate(delegate)

    suspend fun deleteDelegate(delegate: DelegateEntity) =
        delegateDao.deleteDelegate(delegate)

    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrdersByDelegate(delegateId: Long): Flow<List<OrderEntity>> =
        orderDao.getOrdersByDelegate(delegateId)

    suspend fun insertOrder(order: OrderEntity): Long {
        val orderId = orderDao.insertOrder(order)
        order.delegateId?.let { delegateId ->
            delegateDao.incrementOrderCount(delegateId)
        }
        order.supplierId?.let { supplierId ->
            supplierDao.incrementOrdersCount(supplierId)
        }
        return orderId
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) =
        orderDao.updateOrderStatus(orderId, status)

    suspend fun deleteOrder(order: OrderEntity) =
        orderDao.deleteOrder(order)

    // App Settings
    val settingsFlow: Flow<AppSettingsEntity?> = appSettingsDao.getSettingsFlow()

    suspend fun getSettings(): AppSettingsEntity? =
        appSettingsDao.getSettings()

    suspend fun saveSettings(settings: AppSettingsEntity) =
        appSettingsDao.insertOrUpdate(settings)

    suspend fun updateWhatsappNumber(whatsapp: String) =
        appSettingsDao.updateWhatsapp(whatsapp)

    suspend fun updateActiveRole(role: String, delegateId: Long?, delegateName: String) =
        appSettingsDao.updateActiveRole(role, delegateId, delegateName)

    suspend fun updateCustomerInfo(name: String, phone: String, address: String) =
        appSettingsDao.updateCustomerInfo(name, phone, address)
}
