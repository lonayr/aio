package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DelegateEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.ui.components.ProductImageThumbnail
import com.example.ui.components.SupplierCard
import com.example.ui.components.SupplierEditorDialog
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val delegates by viewModel.delegates.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Delegates, 1: Suppliers, 2: Products, 3: Warehouse Settings

    var showAddDelegateDialog by remember { mutableStateOf(false) }
    var delegateToEdit by remember { mutableStateOf<DelegateEntity?>(null) }

    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<SupplierEntity?>(null) }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    var warehouseWhatsappInput by remember { mutableStateOf(settings.warehouseWhatsapp) }
    var storeNameInput by remember { mutableStateOf(settings.storeName) }

    LaunchedEffect(settings) {
        warehouseWhatsappInput = settings.warehouseWhatsapp
        storeNameInput = settings.storeName
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("المندوبين (${delegates.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("المجهزين (${suppliers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("المنتجات (${products.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("إعدادات المخزن", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Delegates Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إدارة حسابات المندوبين", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("إضافة ومتابعة مندوبي المبيعات والتوزيع", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                delegateToEdit = null
                                showAddDelegateDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة مندوب")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (delegates.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا يوجد مندوبين حالياً. اضغط على 'إضافة مندوب' لإضافة حساب جديد.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(delegates, key = { it.id }) { delegate ->
                                DelegateCard(
                                    delegate = delegate,
                                    onEdit = {
                                        delegateToEdit = delegate
                                        showAddDelegateDialog = true
                                    },
                                    onDelete = { viewModel.deleteDelegate(delegate) }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Suppliers Tab (المجهزين والموردين)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("دليل وقائمة المجهزين", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("إدارة الشركات والموردين وأرقام التجهيز", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                supplierToEdit = null
                                showAddSupplierDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة مجهز")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (suppliers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لا يوجد مجهزين مسجلين حالياً. اضغط على 'إضافة مجهز' لتسجيل أول مجهز.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(suppliers, key = { it.id }) { sup ->
                                SupplierCard(
                                    supplier = sup,
                                    onEdit = {
                                        supplierToEdit = sup
                                        showAddSupplierDialog = true
                                    },
                                    onDelete = { viewModel.deleteSupplier(sup) }
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Products Management Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إدارة قائمة المنتجات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("إضافة، تعديل الأسعار، وتحديث الصور", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                productToEdit = null
                                showAddProductDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة منتج")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            AdminProductCard(
                                product = product,
                                currency = settings.currency,
                                onEdit = {
                                    productToEdit = product
                                    showAddProductDialog = true
                                },
                                onDelete = { viewModel.deleteProduct(product) }
                            )
                        }
                    }
                }
            }
            3 -> {
                // Warehouse Settings Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(WhatsAppGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = WhatsAppGreen)
                                    }
                                    Column {
                                        Text("رقم واتساب مخزن التجهيز الافتراضي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("الرقم الافتراضي الذي تُرسل إليه الطلبات عند عدم تحديد مجهز خاص", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                OutlinedTextField(
                                    value = warehouseWhatsappInput,
                                    onValueChange = { warehouseWhatsappInput = it },
                                    label = { Text("رقم الواتساب مع المفتاح الدولي (مثال: 9647701234567)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = storeNameInput,
                                    onValueChange = { storeNameInput = it },
                                    label = { Text("اسم المخزن أو الشركة") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        viewModel.updateWarehouseWhatsapp(warehouseWhatsappInput)
                                        viewModel.saveSettings(
                                            settings.copy(
                                                warehouseWhatsapp = warehouseWhatsappInput.trim(),
                                                storeName = storeNameInput.trim()
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("حفظ الإعدادات")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💡 آلية عمل وتوجيه طلبات التجهيز", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    "1. يمكنك إضافة أي عدد من المجهزين والموردين من تبويب 'المجهزين' مع أرقام واتساب كل مجهز.\n" +
                                            "2. عند إنشاء وقص أي فاتورة في شاشة السلة، يمكنك تحديد المجهز المستلم مباشرة من القائمة أو إضافة مجهز جديد فوراً.\n" +
                                            "3. عند الضغط على زر إرسال الطلب، يتوجه أمر التجهيز مباشرة إلى رقم واتساب المجهز المختار مع كافة الأصناف والكميات والملاحظات.\n" +
                                            "4. في حال عدم تحديد مجهز، يُرسل الطلب تلقائياً إلى رقم المخزن الافتراضي المحدد أعلاه.",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Supplier Dialog
    if (showAddSupplierDialog) {
        SupplierEditorDialog(
            supplier = supplierToEdit,
            onDismiss = { showAddSupplierDialog = false },
            onSave = { name, phone, company, category, address, notes ->
                viewModel.saveSupplier(
                    id = supplierToEdit?.id ?: 0L,
                    name = name,
                    phone = phone,
                    companyName = company,
                    category = category,
                    address = address,
                    notes = notes
                )
                showAddSupplierDialog = false
            }
        )
    }

    // Add/Edit Delegate Dialog
    if (showAddDelegateDialog) {
        DelegateEditorModal(
            delegate = delegateToEdit,
            onDismiss = { showAddDelegateDialog = false },
            onSave = { name, phone, code, area, notes ->
                viewModel.saveDelegate(
                    id = delegateToEdit?.id ?: 0L,
                    name = name,
                    phone = phone,
                    code = code,
                    area = area,
                    notes = notes
                )
                showAddDelegateDialog = false
            }
        )
    }

    // Add/Edit Product Dialog
    if (showAddProductDialog) {
        ProductEditorModal(
            product = productToEdit,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, desc, price, cat, unit, imgUrl, stock ->
                viewModel.saveProduct(
                    id = productToEdit?.id ?: 0L,
                    name = name,
                    description = desc,
                    price = price,
                    category = cat,
                    unit = unit,
                    imageUrl = imgUrl,
                    stockQuantity = stock,
                    isAvailable = true
                )
                showAddProductDialog = false
            }
        )
    }
}

@Composable
fun DelegateCard(
    delegate: DelegateEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Text(delegate.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("كود: ${delegate.code} • ${delegate.area}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("هاتف: ${delegate.phone} • الطلبات: ${delegate.totalOrdersCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminProductCard(
    product: ProductEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(54.dp)) {
                ProductImageThumbnail(imageUrl = product.imageUrl, contentDescription = product.name, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${WhatsAppHelper.formatPrice(product.price, currency)} / ${product.unit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("التصنيف: ${product.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (product.stockQuantity <= 0) "⚠️ نفدت الكمية بالمخزن (0)" else "📦 المخزون المتبقي: ${product.stockQuantity} ${product.unit}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.stockQuantity <= 0) Color(0xFFDC2626) else if (product.stockQuantity <= 10) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DelegateEditorModal(
    delegate: DelegateEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, code: String, area: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(delegate?.name ?: "") }
    var phone by remember { mutableStateOf(delegate?.phone ?: "") }
    var code by remember { mutableStateOf(delegate?.code ?: "") }
    var area by remember { mutableStateOf(delegate?.area ?: "") }
    var notes by remember { mutableStateOf(delegate?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (delegate == null) "إضافة مندوب جديد" else "تعديل بيانات المندوب", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المندوب الكامل") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف المندوب") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("كود المندوب (مثال: DEL-04)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("المنطقة / قطاع التوزيع") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, phone, code, area, notes) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ المندوب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ProductEditorModal(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, price: Double, cat: String, unit: String, imgUrl: String, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.toLong()?.toString() ?: "") }
    var cat by remember { mutableStateOf(product?.category ?: "مواد غذائية") }
    var unit by remember { mutableStateOf(product?.unit ?: "قطعة") }
    var imgUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var stockText by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "100") }

    val presetCategories = listOf("مواد غذائية", "مشروبات وشاي", "منظفات ومعقمات", "أدوات منزلية", "أجهزة وإلكترونيات", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "إضافة منتج جديد" else "تعديل المنتج", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المنتج") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("السعر (مثال: 15000)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة (قطعة، كرتون، كيس 10 كغم، عبوة 5 لتر)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = cat,
                        onValueChange = { cat = it },
                        label = { Text("التصنيف") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = imgUrl,
                        onValueChange = { imgUrl = it },
                        label = { Text("رابط صورة المنتج (URL اختياري)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("وصف المنتج وتفاصيله باللغة العربية") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toIntOrNull() ?: 50
                    if (name.isNotBlank()) {
                        onSave(name, desc, price, cat, unit, imgUrl, stock)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ المنتج")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
