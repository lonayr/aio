package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.DelegateEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.model.CartItem
import com.example.ui.components.ProductImageThumbnail
import com.example.ui.components.SupplierEditorDialog
import com.example.ui.components.SuppliersSelectionDialog
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: StoreViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val totalCount by viewModel.cartTotalCount.collectAsState()
    val totalAmount by viewModel.cartTotalAmount.collectAsState()
    val netTotal by viewModel.invoiceNetTotal.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val delegates by viewModel.activeDelegates.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()

    var customerName by remember { mutableStateOf(viewModel.customerName.value) }
    var customerPhone by remember { mutableStateOf(viewModel.customerPhone.value) }
    var customerAddress by remember { mutableStateOf(viewModel.customerAddress.value) }
    var customerNotes by remember { mutableStateOf(viewModel.customerNotes.value) }
    var discountText by remember { mutableStateOf(if (viewModel.invoiceDiscount.value > 0) viewModel.invoiceDiscount.value.toString() else "") }
    var deliveryFeeText by remember { mutableStateOf(if (viewModel.invoiceDeliveryFee.value > 0) viewModel.invoiceDeliveryFee.value.toString() else "") }
    var selectedDelegate by remember { mutableStateOf<DelegateEntity?>(viewModel.selectedDelegate.value) }

    var expandedCustomerDropdown by remember { mutableStateOf(false) }
    var expandedDelegateDropdown by remember { mutableStateOf(false) }
    var showInvoicePreview by remember { mutableStateOf(false) }
    var showCustomerStatementDialog by remember { mutableStateOf(false) }
    var showSupplierSelectionDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    // Sync external selection into local text states
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let { cust ->
            customerName = cust.name
            customerPhone = cust.phone
            customerAddress = cust.address
        }
    }

    // Keep VM synced
    LaunchedEffect(customerName) { viewModel.customerName.value = customerName }
    LaunchedEffect(customerPhone) { viewModel.customerPhone.value = customerPhone }
    LaunchedEffect(customerAddress) { viewModel.customerAddress.value = customerAddress }
    LaunchedEffect(customerNotes) { viewModel.customerNotes.value = customerNotes }
    LaunchedEffect(selectedDelegate) { viewModel.selectedDelegate.value = selectedDelegate }

    LaunchedEffect(discountText) {
        viewModel.invoiceDiscount.value = discountText.toDoubleOrNull() ?: 0.0
    }
    LaunchedEffect(deliveryFeeText) {
        viewModel.invoiceDeliveryFee.value = deliveryFeeText.toDoubleOrNull() ?: 0.0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "لا توجد منتجات بالفاتورة حالياً",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "اختر المنتجات من المتجر لإضافتها للفاتورة وكتابة اسم الزبون وإصدار فاتورته فوراً",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToCatalog,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة منتجات للفاتورة")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Customer Information & Directory Selection Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonPin,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "بيانات الزبون صاحب الفاتورة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val matchedCustomer = selectedCustomer ?: allCustomers.find { it.name.trim().equals(customerName.trim(), ignoreCase = true) }
                                    if (matchedCustomer != null) {
                                        FilledTonalButton(
                                            onClick = { showCustomerStatementDialog = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Assessment,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("كشف الحساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (allCustomers.isNotEmpty()) {
                                        TextButton(
                                            onClick = { expandedCustomerDropdown = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Contacts,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("دليل الزبائن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Quick Dropdown selector for saved customers
                            if (allCustomers.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedCustomerDropdown,
                                    onExpandedChange = { expandedCustomerDropdown = !expandedCustomerDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedCustomer?.name ?: if (customerName.isNotBlank()) "زبون مخصص: $customerName" else "اختر زبون من المسجلين أو اكتب اسمه بالأسفل...",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("قائمة الزبائن المحفوظين") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomerDropdown) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedCustomerDropdown,
                                        onDismissRequest = { expandedCustomerDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("✍️ كتابة زبون جديد يدوياً") },
                                            onClick = {
                                                viewModel.clearSelectedCustomer()
                                                customerName = ""
                                                customerPhone = ""
                                                customerAddress = ""
                                                expandedCustomerDropdown = false
                                            }
                                        )
                                        Divider()
                                        allCustomers.forEach { cust ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(cust.name, fontWeight = FontWeight.Bold)
                                                        if (cust.phone.isNotBlank() || cust.address.isNotBlank()) {
                                                            Text(
                                                                "${cust.phone} • ${cust.address}",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.selectCustomer(cust)
                                                    expandedCustomerDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Editable Customer Name
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = {
                                    customerName = it
                                    if (selectedCustomer != null && selectedCustomer?.name != it) {
                                        viewModel.selectedCustomer.value = null
                                    }
                                },
                                label = { Text("اسم الزبون / المحل التجاري *") },
                                placeholder = { Text("اكتب اسم الزبون هنا...") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = {
                                    if (customerName.isNotBlank() && allCustomers.none { it.name.trim() == customerName.trim() }) {
                                        IconButton(
                                            onClick = {
                                                viewModel.saveCustomer(
                                                    name = customerName,
                                                    phone = customerPhone,
                                                    address = customerAddress,
                                                    notes = customerNotes
                                                )
                                                Toast.makeText(context, "تم حفظ الزبون ($customerName) في دليلك!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.BookmarkAdd,
                                                contentDescription = "حفظ الزبون",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Save customer pill button if not saved
                            if (customerName.isNotBlank() && allCustomers.none { it.name.trim() == customerName.trim() }) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.saveCustomer(
                                            name = customerName,
                                            phone = customerPhone,
                                            address = customerAddress,
                                            notes = customerNotes
                                        )
                                        Toast.makeText(context, "تم حفظ الزبون ($customerName) في دليلك!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.BookmarkAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "اضغط هنا لحفظ ($customerName) في دليل زبائنك للرجوع له لاحقاً",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Customer Phone
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("رقم هاتف الزبون / الواتساب") },
                                placeholder = { Text("مثال: 07701234567") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Customer Address
                            OutlinedTextField(
                                value = customerAddress,
                                onValueChange = { customerAddress = it },
                                label = { Text("عنوان الزبون / المدينة والموقع") },
                                placeholder = { Text("مثال: بغداد - الكرادة - قرب ساحة التحريات") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Delegate Selector Dropdown
                            if (delegates.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedDelegateDropdown,
                                    onExpandedChange = { expandedDelegateDropdown = !expandedDelegateDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedDelegate?.let { "${it.name} (${it.code})" } ?: "فاتورة مباشرة (بدون مندوب)",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("المندوب المسجل عليه الطلب") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDelegateDropdown) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedDelegateDropdown,
                                        onDismissRequest = { expandedDelegateDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("فاتورة مباشرة (بدون مندوب)") },
                                            onClick = {
                                                selectedDelegate = null
                                                expandedDelegateDropdown = false
                                            }
                                        )
                                        delegates.forEach { del ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(del.name, fontWeight = FontWeight.Bold)
                                                        Text("${del.code} • ${del.area}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                },
                                                onClick = {
                                                    selectedDelegate = del
                                                    expandedDelegateDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Supplier & Warehouse Destination (المجهز ومخزن التجهيز)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedSupplier != null) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = if (selectedSupplier != null) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "المجهز المستلم لأمر التجهيز",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "تحديد المجهز/المورد لإرسال الطلب لواتسابه فوراً",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { showSupplierSelectionDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("دليل المجهزين", fontSize = 11.sp)
                                    }
                                    FilledTonalButton(
                                        onClick = { showAddSupplierDialog = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ مجهز جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Current selected supplier display
                            if (selectedSupplier != null) {
                                val sup = selectedSupplier!!
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(sup.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    if (sup.companyName.isNotBlank()) {
                                                        Text("(${sup.companyName})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                                Text(
                                                    "📱 واتساب التجهيز: ${sup.phone}",
                                                    fontSize = 11.sp,
                                                    color = WhatsAppGreen,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (sup.address.isNotBlank()) {
                                                    Text("📍 ${sup.address}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.selectSupplier(null) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "إلغاء المجهز",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().clickable { showSupplierSelectionDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "المخزن العام الافتراضي (${settings.warehouseWhatsapp})",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                "لم يتم اختيار مجهز خاص، سيتم إرسال الطلب إلى رقم المخزن الافتراضي. اضغط لتغيير المجهز.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Title: Invoice Products Table / Items
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "أصناف الفاتورة (${cartItems.size} صنف / $totalCount وحدة)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Row {
                            TextButton(onClick = onNavigateToCatalog) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة أصناف", fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.clearCart() }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تفريغ", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Cart / Invoice Items
                items(cartItems, key = { it.product.id }) { cartItem ->
                    CartItemCard(
                        cartItem = cartItem,
                        currency = settings.currency,
                        onQuantityChange = { newQty -> viewModel.updateCartQuantity(cartItem.product.id, newQty) },
                        onNoteChange = { note -> viewModel.updateCartItemNote(cartItem.product.id, note) },
                        onRemove = { viewModel.removeFromCart(cartItem.product.id) }
                    )
                }

                // Invoice Financial Summary & Adjustments (Discounts / Delivery)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "حسابات الفاتورة والخصم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المجموع الفرعي للأصناف:", fontSize = 13.sp)
                                Text(
                                    WhatsAppHelper.formatPrice(totalAmount, settings.currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Discount & Delivery fee rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = discountText,
                                    onValueChange = { discountText = it },
                                    label = { Text("قيمة الخصم (${settings.currency})") },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = deliveryFeeText,
                                    onValueChange = { deliveryFeeText = it },
                                    label = { Text("أجور التوصيل (${settings.currency})") },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Invoice Note
                            OutlinedTextField(
                                value = customerNotes,
                                onValueChange = { customerNotes = it },
                                label = { Text("ملاحظات الفاتورة أو شروط الدفع والتسليم") },
                                placeholder = { Text("مثال: الدفع نقداً عند الاستلام، تسليم خلال 24 ساعة...") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Net Total Highlight
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("صافي الفاتورة الكلي:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    if (discountText.toDoubleOrNull() ?: 0.0 > 0) {
                                        Text(
                                            "تم خصم ${WhatsAppHelper.formatPrice(discountText.toDoubleOrNull() ?: 0.0, settings.currency)}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }
                                Text(
                                    text = WhatsAppHelper.formatPrice(netTotal, settings.currency),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Action Buttons for Invoice
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        // 1. Preview & Print Official Invoice
                        OutlinedButton(
                            onClick = { showInvoicePreview = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("معاينة الفاتورة الرسمية (طباعة ومشاركة)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // 2. Send Invoice directly to Customer WhatsApp
                        Button(
                            onClick = {
                                val invoiceNumber = WhatsAppHelper.generateOrderNumber()
                                val invoiceText = WhatsAppHelper.buildCustomerInvoiceText(
                                    invoiceNumber = invoiceNumber,
                                    storeName = settings.storeName,
                                    customerName = customerName.ifBlank { "عميل مباشر" },
                                    customerPhone = customerPhone,
                                    customerAddress = customerAddress,
                                    customerNotes = customerNotes,
                                    cartItems = cartItems,
                                    subtotalAmount = totalAmount,
                                    discount = discountText.toDoubleOrNull() ?: 0.0,
                                    deliveryFee = deliveryFeeText.toDoubleOrNull() ?: 0.0,
                                    netTotalAmount = netTotal,
                                    currency = settings.currency
                                )

                                // Auto save customer if not in directory
                                if (customerName.isNotBlank()) {
                                    viewModel.saveCurrentInvoiceCustomerToDirectory()
                                }

                                val targetPhone = customerPhone.ifBlank { settings.warehouseWhatsapp }
                                val sent = WhatsAppHelper.openWhatsApp(context, targetPhone, invoiceText)
                                if (sent) {
                                    Toast.makeText(context, "جاري إرسال الفاتورة عبر واتساب", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرسال الفاتورة للزبون عبر الواتساب",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // 3. Dispatch Order to Warehouse / Supplier
                        Button(
                            onClick = {
                                if (customerName.isNotBlank()) {
                                    viewModel.saveCurrentInvoiceCustomerToDirectory()
                                }
                                viewModel.submitOrderToWhatsApp(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSupplier != null) Color(0xFF0D9488) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (selectedSupplier != null) Icons.Default.LocalShipping else Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedSupplier != null)
                                    "إرسال أمر التجهيز للمجهز (${selectedSupplier!!.name})"
                                else
                                    "إرسال طلب التجهيز للمخزن وتحديث المخزون",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Invoice Preview Dialog
    if (showInvoicePreview) {
        val invoiceNumber = remember { WhatsAppHelper.generateOrderNumber() }
        InvoicePreviewDialog(
            invoiceNumber = invoiceNumber,
            storeName = settings.storeName,
            customerName = customerName.ifBlank { "عميل مباشر" },
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            customerNotes = customerNotes,
            cartItems = cartItems,
            subtotal = totalAmount,
            discount = discountText.toDoubleOrNull() ?: 0.0,
            deliveryFee = deliveryFeeText.toDoubleOrNull() ?: 0.0,
            netTotal = netTotal,
            currency = settings.currency,
            onDismiss = { showInvoicePreview = false },
            onShare = {
                val text = WhatsAppHelper.buildCustomerInvoiceText(
                    invoiceNumber = invoiceNumber,
                    storeName = settings.storeName,
                    customerName = customerName.ifBlank { "عميل مباشر" },
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    customerNotes = customerNotes,
                    cartItems = cartItems,
                    subtotalAmount = totalAmount,
                    discount = discountText.toDoubleOrNull() ?: 0.0,
                    deliveryFee = deliveryFeeText.toDoubleOrNull() ?: 0.0,
                    netTotalAmount = netTotal,
                    currency = settings.currency
                )
                WhatsAppHelper.shareText(context, "فاتورة مبيعات $invoiceNumber", text)
            },
            onSendWhatsApp = {
                val text = WhatsAppHelper.buildCustomerInvoiceText(
                    invoiceNumber = invoiceNumber,
                    storeName = settings.storeName,
                    customerName = customerName.ifBlank { "عميل مباشر" },
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    customerNotes = customerNotes,
                    cartItems = cartItems,
                    subtotalAmount = totalAmount,
                    discount = discountText.toDoubleOrNull() ?: 0.0,
                    deliveryFee = deliveryFeeText.toDoubleOrNull() ?: 0.0,
                    netTotalAmount = netTotal,
                    currency = settings.currency
                )
                val target = customerPhone.ifBlank { settings.warehouseWhatsapp }
                WhatsAppHelper.openWhatsApp(context, target, text)
            }
        )
    }

    // Customer Statement Dialog from Cart
    if (showCustomerStatementDialog) {
        val targetCustomer = selectedCustomer ?: allCustomers.find { it.name.trim().equals(customerName.trim(), ignoreCase = true) }
        targetCustomer?.let { cust ->
            CustomerStatementDialog(
                customer = cust,
                viewModel = viewModel,
                onDismiss = { showCustomerStatementDialog = false },
                onNavigateToInvoice = { showCustomerStatementDialog = false }
            )
        }
    }

    // Suppliers Selection Dialog
    if (showSupplierSelectionDialog) {
        SuppliersSelectionDialog(
            suppliers = suppliers,
            selectedSupplier = selectedSupplier,
            onSelectSupplier = { sup ->
                viewModel.selectSupplier(sup)
                showSupplierSelectionDialog = false
            },
            onAddNewSupplier = {
                showSupplierSelectionDialog = false
                showAddSupplierDialog = true
            },
            onDismiss = { showSupplierSelectionDialog = false }
        )
    }

    // Add New Supplier Dialog
    if (showAddSupplierDialog) {
        SupplierEditorDialog(
            supplier = null,
            onDismiss = { showAddSupplierDialog = false },
            onSave = { name, phone, company, category, address, notes ->
                viewModel.saveSupplier(
                    name = name,
                    phone = phone,
                    companyName = company,
                    category = category,
                    address = address,
                    notes = notes
                )
                showAddSupplierDialog = false
                Toast.makeText(context, "تم حفظ وتعيين المجهز ($name) للفاتورة بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun InvoicePreviewDialog(
    invoiceNumber: String,
    storeName: String,
    customerName: String,
    customerPhone: String,
    customerAddress: String,
    customerNotes: String,
    cartItems: List<CartItem>,
    subtotal: Double,
    discount: Double,
    deliveryFee: Double,
    netTotal: Double,
    currency: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }
    val dateString = remember { dateFormat.format(Date()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header of Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "معاينة الفاتورة الرسمية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                    }
                }

                Divider(color = Color(0xFFEEEEEE))

                // Scrollable Bill Paper
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Invoice Store Header
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = storeName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "فاتورة مبيعات نقدية",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "رقم الفاتورة: $invoiceNumber",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "التاريخ: $dateString",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Customer Details Box
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("اسم الزبون:", fontSize = 12.sp, color = Color.Gray)
                                    Text(customerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                if (customerPhone.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("رقم الهاتف:", fontSize = 12.sp, color = Color.Gray)
                                        Text(customerPhone, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                    }
                                }
                                if (customerAddress.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("العنوان:", fontSize = 12.sp, color = Color.Gray)
                                        Text(customerAddress, fontSize = 12.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    // Items Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الصنف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(2f))
                            Text("الكمية", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("السعر", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            Text("الإجمالي", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                        }
                    }

                    // Items List
                    items(cartItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(item.product.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                if (item.itemNote.isNotBlank()) {
                                    Text("(${item.itemNote})", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Text("${item.quantity} ${item.product.unit}", fontSize = 11.sp, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(WhatsAppHelper.formatPrice(item.product.price, currency), fontSize = 11.sp, color = Color.Black, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            Text(WhatsAppHelper.formatPrice(item.subtotal, currency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                        }
                        Divider(color = Color(0xFFF1F5F9))
                    }

                    // Financial Calculations
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المجموع الفرعي:", fontSize = 12.sp, color = Color.Gray)
                                Text(WhatsAppHelper.formatPrice(subtotal, currency), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            }
                            if (discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الخصم الممنوح:", fontSize = 12.sp, color = Color(0xFF16A34A))
                                    Text("-${WhatsAppHelper.formatPrice(discount, currency)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                            }
                            if (deliveryFee > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("أجور التوصيل:", fontSize = 12.sp, color = Color.Gray)
                                    Text("+${WhatsAppHelper.formatPrice(deliveryFee, currency)}", fontSize = 12.sp, color = Color.Black)
                                }
                            }
                            Divider(color = Color(0xFFCBD5E1), modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الصافي الإجمالي:", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                Text(
                                    WhatsAppHelper.formatPrice(netTotal, currency),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (customerNotes.isNotBlank()) {
                        item {
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "ملاحظات: $customerNotes",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Modal Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSendWhatsApp,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرسال واتساب", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    currency: String,
    onQuantityChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var showNoteField by remember { mutableStateOf(cartItem.itemNote.isNotEmpty()) }
    var itemNoteText by remember { mutableStateOf(cartItem.itemNote) }
    val maxStock = cartItem.product.stockQuantity
    val isMaxReached = maxStock > 0 && cartItem.quantity >= maxStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Product Thumbnail
                Box(modifier = Modifier.size(64.dp)) {
                    ProductImageThumbnail(
                        imageUrl = cartItem.product.imageUrl,
                        contentDescription = cartItem.product.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Info: Name & Unit Price
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = cartItem.product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                    Text(
                        text = "${WhatsAppHelper.formatPrice(cartItem.product.price, currency)} / ${cartItem.product.unit}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "المجموع: ${WhatsAppHelper.formatPrice(cartItem.subtotal, currency)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Stock indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = if (isMaxReached) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isMaxReached) "وصلت للحد الأقصى بالمخزن ($maxStock ${cartItem.product.unit})"
                            else "المتوفر بالمخزن: $maxStock ${cartItem.product.unit}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isMaxReached) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity - 1) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                            contentDescription = "تقليل",
                            tint = if (cartItem.quantity == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedContent(
                        targetState = cartItem.quantity,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(120)) togetherWith fadeOut(animationSpec = tween(120))
                        },
                        label = "CartQuantityAnim"
                    ) { qty ->
                        Text(
                            text = "$qty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (!isMaxReached) onQuantityChange(cartItem.quantity + 1) },
                        enabled = !isMaxReached,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (!isMaxReached) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "زيادة",
                            tint = if (!isMaxReached) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Note toggle & field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showNoteField = !showNoteField },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (showNoteField) Icons.Default.EditNote else Icons.Outlined.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (itemNoteText.isNotEmpty()) "تعديل الملاحظة للصنف" else "+ إضافة ملاحظة للصنف",
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "حذف الصنف",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showNoteField) {
                OutlinedTextField(
                    value = itemNoteText,
                    onValueChange = {
                        itemNoteText = it
                        onNoteChange(it)
                    },
                    placeholder = { Text("مثال: يرجى اختيار تاريخ إنتاج جديد...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
