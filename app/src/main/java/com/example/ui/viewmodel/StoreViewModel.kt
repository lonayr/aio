package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.DelegateEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.model.CartItem
import com.example.data.repository.StoreRepository
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderSuccessInfo(
    val orderNumber: String,
    val totalAmount: Double,
    val itemsCount: Int,
    val customerName: String,
    val whatsappNumber: String
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoreRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StoreRepository(database)
    }

    // UI state for search and filtering
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")

    // Products from Repository
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        searchQuery,
        selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)

            val matchesCategory = category == "الكل" || product.category == category

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories
    val categories: StateFlow<List<String>> = repository.allCategories
        .map { list -> listOf("الكل") + list.filter { it.isNotBlank() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("الكل"))

    // Delegates
    val delegates: StateFlow<List<DelegateEntity>> = repository.allDelegates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDelegates: StateFlow<List<DelegateEntity>> = repository.activeDelegates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders History
    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allOrders: StateFlow<List<OrderEntity>> = orders

    // App Settings
    val settings: StateFlow<AppSettingsEntity> = repository.settingsFlow
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    // Customers (قائمة وسجل الزبائن)
    val customerSearchQuery = MutableStateFlow("")
    val allCustomers: StateFlow<List<com.example.data.local.entity.CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCustomers: StateFlow<List<com.example.data.local.entity.CustomerEntity>> = combine(
        allCustomers,
        customerSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.registryNumber.contains(query, ignoreCase = true) ||
            it.phone.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCustomer = MutableStateFlow<com.example.data.local.entity.CustomerEntity?>(null)

    // Suppliers (المجهزون والموردون)
    val allSuppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selectedSupplier = MutableStateFlow<SupplierEntity?>(null)

    fun selectSupplier(supplier: SupplierEntity?) {
        selectedSupplier.value = supplier
    }

    // Customer Payments (سندات القبض وتسديدات الزبائن)
    val allCustomerPayments: StateFlow<List<com.example.data.local.entity.CustomerPaymentEntity>> = repository.allCustomerPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-memory Shopping Cart / Invoice items
    val cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    // Invoice Form & Customer Details
    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val customerAddress = MutableStateFlow("")
    val customerNotes = MutableStateFlow("")
    val invoiceDiscount = MutableStateFlow(0.0)
    val invoiceDeliveryFee = MutableStateFlow(0.0)
    val selectedDelegate = MutableStateFlow<DelegateEntity?>(null)

    // Order confirmation alert / notification
    val orderNotification = MutableStateFlow<OrderSuccessInfo?>(null)

    init {
        // Load default customer info from settings once available
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { s ->
                s?.let {
                    if (customerName.value.isEmpty()) customerName.value = it.defaultCustomerName
                    if (customerPhone.value.isEmpty()) customerPhone.value = it.defaultCustomerPhone
                    if (customerAddress.value.isEmpty()) customerAddress.value = it.defaultCustomerAddress
                }
            }
        }
    }

    // Cart calculations
    val cartTotalCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartTotalAmount: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val invoiceNetTotal: StateFlow<Double> = combine(
        cartTotalAmount,
        invoiceDiscount,
        invoiceDeliveryFee
    ) { subtotal, discount, delivery ->
        (subtotal - discount + delivery).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Customer Selection & Management
    fun selectCustomer(customer: com.example.data.local.entity.CustomerEntity) {
        selectedCustomer.value = customer
        customerName.value = customer.name
        customerPhone.value = customer.phone
        customerAddress.value = customer.address
        if (customer.notes.isNotBlank() && customerNotes.value.isBlank()) {
            customerNotes.value = customer.notes
        }
    }

    fun clearSelectedCustomer() {
        selectedCustomer.value = null
        customerName.value = ""
        customerPhone.value = ""
        customerAddress.value = ""
    }

    fun saveCustomer(
        name: String,
        phone: String = "",
        address: String = "",
        notes: String = "",
        openingBalance: Double = 0.0,
        registryNumber: String = "",
        id: Long = 0
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val regNumber = registryNumber.trim().ifEmpty {
                val nextNum = (allCustomers.value.size + 1).toString().padStart(3, '0')
                "CUS-$nextNum"
            }
            val customer = com.example.data.local.entity.CustomerEntity(
                id = id,
                registryNumber = regNumber,
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                notes = notes.trim(),
                openingBalance = openingBalance
            )
            if (id == 0L) {
                val newId = repository.insertCustomer(customer)
                selectedCustomer.value = customer.copy(id = newId)
            } else {
                repository.updateCustomer(customer)
                if (selectedCustomer.value?.id == id) {
                    selectedCustomer.value = customer
                }
            }
        }
    }

    fun deleteCustomer(customer: com.example.data.local.entity.CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            if (selectedCustomer.value?.id == customer.id) {
                selectedCustomer.value = null
            }
        }
    }

    fun saveCurrentInvoiceCustomerToDirectory() {
        val name = customerName.value.trim()
        if (name.isBlank()) return
        saveCustomer(
            name = name,
            phone = customerPhone.value.trim(),
            address = customerAddress.value.trim(),
            notes = customerNotes.value.trim()
        )
    }

    // Cart actions
    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        if (product.stockQuantity <= 0) return
        val currentList = cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val maxAllowed = product.stockQuantity
            val newQty = (existing.quantity + quantity).coerceAtMost(maxAllowed)
            currentList[existingIndex] = existing.copy(quantity = newQty)
        } else {
            val finalQty = quantity.coerceAtMost(product.stockQuantity)
            currentList.add(CartItem(product = product, quantity = finalQty))
        }
        cartItems.value = currentList
    }

    fun updateCartQuantity(productId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentList = cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val maxStock = item.product.stockQuantity
            val cappedQuantity = if (maxStock > 0) newQuantity.coerceAtMost(maxStock) else newQuantity
            currentList[index] = item.copy(quantity = cappedQuantity)
            cartItems.value = currentList
        }
    }

    fun updateCartItemNote(productId: Long, note: String) {
        val currentList = cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(itemNote = note)
            cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        cartItems.value = cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        cartItems.value = emptyList()
        customerNotes.value = ""
    }

    // WhatsApp Dispatch and Order Persistence
    fun submitOrderToWhatsApp(context: Context): Boolean {
        val currentItems = cartItems.value
        if (currentItems.isEmpty()) return false

        val name = customerName.value.trim().ifEmpty { "عميل مباشر" }
        val phone = customerPhone.value.trim().ifEmpty { "غير محدد" }
        val address = customerAddress.value.trim().ifEmpty { "الاستلام من المخزن" }
        val notes = customerNotes.value.trim()
        val currentSettings = settings.value
        val total = cartTotalAmount.value
        val orderNumber = WhatsAppHelper.generateOrderNumber()
        val delegate = selectedDelegate.value
        val supplier = selectedSupplier.value

        // Build item summary string
        val itemsSummary = currentItems.joinToString(separator = "، ") { "${it.product.name} (x${it.quantity})" }
        val itemsJson = currentItems.joinToString(separator = "\n") { "- ${it.product.name} [الكمية: ${it.quantity} ${it.product.unit}] = ${it.subtotal} ${currentSettings.currency}" }

        // Build WhatsApp Message
        val message = WhatsAppHelper.buildWhatsAppOrderMessage(
            orderNumber = orderNumber,
            customerName = name,
            customerPhone = phone,
            customerAddress = address,
            customerNotes = notes,
            delegate = delegate,
            supplier = supplier,
            cartItems = currentItems,
            totalAmount = total,
            currency = currentSettings.currency
        )

        // Save order into database
        val newOrder = OrderEntity(
            orderNumber = orderNumber,
            customerName = name,
            customerPhone = phone,
            customerAddress = address,
            customerCity = delegate?.area ?: "",
            customerRegistryNumber = selectedCustomer.value?.registryNumber ?: "",
            delegateId = delegate?.id,
            delegateName = delegate?.name ?: "طلب مباشر",
            delegateRegistryNumber = delegate?.code ?: "",
            supplierId = supplier?.id,
            supplierName = supplier?.name ?: "",
            supplierPhone = supplier?.phone ?: "",
            supplierRegistryNumber = supplier?.registryNumber ?: "",
            itemsSummary = itemsSummary,
            itemsDetailedJson = itemsJson,
            totalAmount = total,
            itemsCount = currentItems.sumOf { it.quantity },
            customerNotes = notes,
            status = "PREPARING",
            whatsappSent = true
        )

        viewModelScope.launch {
            repository.insertOrder(newOrder)
            // Deduct stock for all ordered products
            currentItems.forEach { item ->
                repository.deductStock(item.product.id, item.quantity)
            }
            // Persist updated default customer details
            repository.updateCustomerInfo(name, phone, address)
        }

        // Determine destination whatsapp (Selected Supplier phone or Warehouse WhatsApp)
        val targetWhatsApp = supplier?.phone?.trim()?.takeIf { it.isNotBlank() } ?: currentSettings.warehouseWhatsapp

        // Open WhatsApp
        val dispatched = WhatsAppHelper.openWhatsApp(
            context = context,
            phoneNumber = targetWhatsApp,
            message = message
        )

        // Set instant in-app notification & confirmation
        orderNotification.value = OrderSuccessInfo(
            orderNumber = orderNumber,
            totalAmount = total,
            itemsCount = currentItems.sumOf { it.quantity },
            customerName = name,
            whatsappNumber = targetWhatsApp
        )

        // Clear cart
        clearCart()
        return dispatched
    }

    fun resendOrderToWhatsApp(context: Context, order: OrderEntity) {
        val currentSettings = settings.value
        val message = "🔄 *إعادة إرسال طلب تجهيز سابق* 🔄\n" +
                "🔖 *رقم الطلب:* `${order.orderNumber}`\n" +
                (if (order.supplierName.isNotBlank()) "🏭 *المجهز:* ${order.supplierName}\n" else "") +
                "👤 *العميل:* ${order.customerName}\n" +
                "📞 *الهاتف:* ${order.customerPhone}\n" +
                "📍 *العنوان:* ${order.customerAddress}\n" +
                "👔 *المندوب:* ${order.delegateName}\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "📋 *قائمة المواد:*\n${order.itemsDetailedJson}\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                (if (order.customerNotes.isNotBlank()) "📝 *ملاحظات:* ${order.customerNotes}\n━━━━━━━━━━━━━━━━━━━━\n" else "") +
                "💰 *المجموع:* ${WhatsAppHelper.formatPrice(order.totalAmount, currentSettings.currency)}\n" +
                "🚚 *حالة الطلب:* ${if (order.status == "PREPARING") "قيد التجهيز" else if (order.status == "COMPLETED") "مكتمل" else "ملغي"}"

        val targetPhone = order.supplierPhone.trim().takeIf { it.isNotBlank() } ?: currentSettings.warehouseWhatsapp
        WhatsAppHelper.openWhatsApp(context, targetPhone, message)
    }

    fun dismissNotification() {
        orderNotification.value = null
    }

    // Role management
    fun switchRole(role: String, delegate: DelegateEntity? = null) {
        viewModelScope.launch {
            repository.updateActiveRole(
                role = role,
                delegateId = delegate?.id,
                delegateName = delegate?.name ?: ""
            )
        }
    }

    // Product Management (Admin)
    fun saveProduct(
        id: Long = 0,
        name: String,
        description: String,
        price: Double,
        category: String,
        unit: String,
        imageUrl: String,
        stockQuantity: Int,
        isAvailable: Boolean
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                name = name.trim(),
                description = description.trim(),
                price = price,
                category = category.trim(),
                unit = unit.trim().ifEmpty { "قطعة" },
                imageUrl = imageUrl.trim(),
                stockQuantity = stockQuantity,
                isAvailable = isAvailable
            )
            if (id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Delegate Management (Admin)
    fun saveDelegate(
        id: Long = 0,
        name: String,
        phone: String,
        code: String,
        area: String,
        notes: String
    ) {
        viewModelScope.launch {
            val delegate = DelegateEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                code = code.trim().ifEmpty { "DEL-${(10..99).random()}" },
                area = area.trim(),
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertDelegate(delegate)
            } else {
                repository.updateDelegate(delegate)
            }
        }
    }

    fun deleteDelegate(delegate: DelegateEntity) {
        viewModelScope.launch {
            repository.deleteDelegate(delegate)
        }
    }

    // Supplier Management (المجهزون والموردون)
    fun saveSupplier(
        id: Long = 0,
        name: String,
        phone: String,
        companyName: String = "",
        category: String = "",
        address: String = "",
        notes: String = "",
        registryNumber: String = ""
    ) {
        viewModelScope.launch {
            val regNumber = registryNumber.trim().ifEmpty {
                val nextNum = (allSuppliers.value.size + 1).toString().padStart(3, '0')
                "SUP-$nextNum"
            }
            val supplier = SupplierEntity(
                id = id,
                registryNumber = regNumber,
                name = name.trim(),
                phone = phone.trim(),
                companyName = companyName.trim(),
                category = category.trim(),
                address = address.trim(),
                notes = notes.trim()
            )
            if (id == 0L) {
                val newId = repository.insertSupplier(supplier)
                selectedSupplier.value = supplier.copy(id = newId)
            } else {
                repository.updateSupplier(supplier)
                if (selectedSupplier.value?.id == id) {
                    selectedSupplier.value = supplier
                }
            }
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            if (selectedSupplier.value?.id == supplier.id) {
                selectedSupplier.value = null
            }
            repository.deleteSupplier(supplier)
        }
    }

    // Order status update (Admin)
    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    // Settings update
    fun saveSettings(settings: AppSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    fun updateWarehouseWhatsapp(newNumber: String) {
        viewModelScope.launch {
            repository.updateWhatsappNumber(newNumber.trim())
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    // Customer Payments & Receipts (سندات القبض وتسديدات الزبائن)
    fun saveCustomerPayment(
        id: Long = 0,
        customerId: Long,
        customerName: String,
        amount: Double,
        receiptNumber: String = "",
        paymentMethod: String = "نقداً (Cash)",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val payment = com.example.data.local.entity.CustomerPaymentEntity(
                id = id,
                customerId = customerId,
                customerName = customerName.trim(),
                amount = amount,
                receiptNumber = receiptNumber.trim().ifEmpty { "REC-${(100..999).random()}" },
                paymentMethod = paymentMethod.trim(),
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertPayment(payment)
            } else {
                repository.updatePayment(payment)
            }
        }
    }

    fun deleteCustomerPayment(payment: com.example.data.local.entity.CustomerPaymentEntity) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    // Generate accurate chronological customer ledger statement
    fun buildCustomerStatement(
        customer: com.example.data.local.entity.CustomerEntity,
        orders: List<OrderEntity>,
        payments: List<com.example.data.local.entity.CustomerPaymentEntity>
    ): CustomerAccountStatementData {
        // Match orders for this customer (by name or phone)
        val customerOrders = orders.filter {
            it.customerName.trim().equals(customer.name.trim(), ignoreCase = true) ||
            (customer.phone.isNotBlank() && it.customerPhone.trim() == customer.phone.trim())
        }

        // Match payments for this customer
        val customerPayments = payments.filter {
            it.customerId == customer.id ||
            it.customerName.trim().equals(customer.name.trim(), ignoreCase = true)
        }

        // Interleave chronologically
        data class RawTransaction(
            val timestamp: Long,
            val isInvoice: Boolean,
            val refNumber: String,
            val description: String,
            val amount: Double
        )

        val rawList = mutableListOf<RawTransaction>()

        customerOrders.forEach { ord ->
            rawList.add(
                RawTransaction(
                    timestamp = ord.createdAt,
                    isInvoice = true,
                    refNumber = ord.orderNumber,
                    description = ord.itemsSummary.ifBlank { "فاتورة مبيعات (${ord.itemsCount} صنف)" },
                    amount = ord.totalAmount
                )
            )
        }

        customerPayments.forEach { pay ->
            rawList.add(
                RawTransaction(
                    timestamp = pay.createdAt,
                    isInvoice = false,
                    refNumber = pay.receiptNumber,
                    description = "${pay.paymentMethod}${if (pay.notes.isNotBlank()) " - ${pay.notes}" else ""}",
                    amount = pay.amount
                )
            )
        }

        // Sort ascending by time to calculate accurate running balance
        val sortedAscending = rawList.sortedBy { it.timestamp }

        var currentBalance = customer.openingBalance
        val records = mutableListOf<com.example.data.model.StatementRecord>()

        sortedAscending.forEachIndexed { idx, item ->
            if (item.isInvoice) {
                currentBalance += item.amount // Debit (adds to customer debt)
            } else {
                currentBalance -= item.amount // Credit (deducts from customer debt)
            }
            records.add(
                com.example.data.model.StatementRecord(
                    id = "REC-$idx-${item.timestamp}",
                    timestamp = item.timestamp,
                    refNumber = item.refNumber,
                    isInvoice = item.isInvoice,
                    description = item.description,
                    amount = item.amount,
                    runningBalance = currentBalance
                )
            )
        }

        val totalDebit = customerOrders.sumOf { it.totalAmount }
        val totalCredit = customerPayments.sumOf { it.amount }
        val netBalanceDue = (customer.openingBalance + totalDebit) - totalCredit

        return CustomerAccountStatementData(
            customer = customer,
            openingBalance = customer.openingBalance,
            totalDebitInvoices = totalDebit,
            totalCreditPayments = totalCredit,
            netBalanceDue = netBalanceDue,
            invoicesCount = customerOrders.size,
            paymentsCount = customerPayments.size,
            records = records.reversed() // Display latest first in UI
        )
    }

    fun updateProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.updateCustomerInfo(name.trim(), phone.trim(), address.trim())
        }
    }
}

data class CustomerAccountStatementData(
    val customer: com.example.data.local.entity.CustomerEntity,
    val openingBalance: Double,
    val totalDebitInvoices: Double,
    val totalCreditPayments: Double,
    val netBalanceDue: Double,
    val invoicesCount: Int,
    val paymentsCount: Int,
    val records: List<com.example.data.model.StatementRecord>
)
