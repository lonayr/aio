package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import com.example.data.local.entity.CustomerPaymentEntity
import com.example.data.model.StatementRecord
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerStatementDialog(
    customer: CustomerEntity,
    viewModel: StoreViewModel,
    onDismiss: () -> Unit,
    onNavigateToInvoice: () -> Unit
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsState()
    val allPayments by viewModel.allCustomerPayments.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("ALL") } // ALL, INVOICES, PAYMENTS
    var paymentToDelete by remember { mutableStateOf<CustomerPaymentEntity?>(null) }

    // Calculate customer ledger statement
    val statementData = remember(customer, allOrders, allPayments) {
        viewModel.buildCustomerStatement(customer, allOrders, allPayments)
    }

    val filteredRecords = remember(statementData.records, filterType) {
        when (filterType) {
            "INVOICES" -> statementData.records.filter { it.isInvoice }
            "PAYMENTS" -> statementData.records.filter { !it.isInvoice }
            else -> statementData.records
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }
    val dateOnlyFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar")) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header with Close & Title
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "كشف حساب الزبون",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = customer.name,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable Statement Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Customer Quick Info Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (customer.phone.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("رقم الهاتف / الواتساب:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(customer.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (customer.address.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("العنوان:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(customer.address, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Financial KPIs Summary Cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Net Balance Due Banner (الرصيد المتبقي)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (statementData.netBalanceDue > 0) Color(0xFFFEF2F2)
                                    else if (statementData.netBalanceDue < 0) Color(0xFFF0FDF4)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (statementData.netBalanceDue > 0) "🔴 الرصيد المتبقي بذمة الزبون (المطلوب سداده)"
                                        else if (statementData.netBalanceDue < 0) "🟢 رصيد دائن لصالح الزبون (فائض مسدد)"
                                        else "⚪ الحساب خالص ومسدد بالكامل",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (statementData.netBalanceDue > 0) Color(0xFFB91C1C)
                                        else if (statementData.netBalanceDue < 0) Color(0xFF15803D)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = WhatsAppHelper.formatPrice(Math.abs(statementData.netBalanceDue), settings.currency),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (statementData.netBalanceDue > 0) Color(0xFFDC2626)
                                        else if (statementData.netBalanceDue < 0) Color(0xFF16A34A)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Sub-stats (Invoices vs Payments)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total Invoices (Debit)
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text("إجمالي المبيعات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = WhatsAppHelper.formatPrice(statementData.totalDebitInvoices, settings.currency),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${statementData.invoicesCount} فاتورة صادرة",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Total Payments (Credit)
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                            Text("إجمالي المسدد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = WhatsAppHelper.formatPrice(statementData.totalCreditPayments, settings.currency),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                        Text(
                                            text = "${statementData.paymentsCount} دفعة / سند قبض",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons: Record Payment & Filter Chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAddPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تسجيل دفعة / سند قبض", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.selectCustomer(customer)
                                    onDismiss()
                                    onNavigateToInvoice()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فاتورة جديدة", fontSize = 13.sp)
                            }
                        }
                    }

                    // Transaction Filter Tabs
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = filterType == "ALL",
                                onClick = { filterType = "ALL" },
                                label = { Text("جميع الحركات (${statementData.records.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filterType == "INVOICES",
                                onClick = { filterType = "INVOICES" },
                                label = { Text("🧾 الفواتير (${statementData.invoicesCount})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filterType == "PAYMENTS",
                                onClick = { filterType = "PAYMENTS" },
                                label = { Text("💵 التسديدات (${statementData.paymentsCount})", fontSize = 11.sp) }
                            )
                        }
                    }

                    // Ledger Records List Header
                    item {
                        Text(
                            text = "سجل حركة كشف الحساب التفصيلي:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (filteredRecords.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد حركات مسجلة ضمن الفلتر المختار",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredRecords, key = { it.id }) { record ->
                            StatementRecordCard(
                                record = record,
                                currency = settings.currency,
                                dateOnlyFormat = dateOnlyFormat
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Bottom Action Bar: Share to WhatsApp or Other apps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send Statement to Customer WhatsApp
                    Button(
                        onClick = {
                            val text = WhatsAppHelper.buildCustomerStatementText(
                                storeName = settings.storeName,
                                customerName = customer.name,
                                customerPhone = customer.phone,
                                customerAddress = customer.address,
                                openingBalance = customer.openingBalance,
                                totalDebit = statementData.totalDebitInvoices,
                                totalCredit = statementData.totalCreditPayments,
                                finalBalance = statementData.netBalanceDue,
                                entries = statementData.records,
                                currency = settings.currency
                            )
                            val targetPhone = customer.phone.ifBlank { settings.warehouseWhatsapp }
                            val sent = WhatsAppHelper.openWhatsApp(context, targetPhone, text)
                            if (sent) {
                                Toast.makeText(context, "جاري إرسال كشف الحساب عبر الواتساب", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إرسال الكشف للزبون عبر واتساب",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Share Statement text (Print / General share)
                    OutlinedButton(
                        onClick = {
                            val text = WhatsAppHelper.buildCustomerStatementText(
                                storeName = settings.storeName,
                                customerName = customer.name,
                                customerPhone = customer.phone,
                                customerAddress = customer.address,
                                openingBalance = customer.openingBalance,
                                totalDebit = statementData.totalDebitInvoices,
                                totalCredit = statementData.totalCreditPayments,
                                finalBalance = statementData.netBalanceDue,
                                entries = statementData.records,
                                currency = settings.currency
                            )
                            WhatsAppHelper.shareText(context, "كشف حساب زبون - ${customer.name}", text)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة / طباعة", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Add Payment Dialog
    if (showAddPaymentDialog) {
        AddCustomerPaymentDialog(
            customer = customer,
            onDismiss = { showAddPaymentDialog = false },
            onSave = { amount, receiptNo, method, notes ->
                viewModel.saveCustomerPayment(
                    customerId = customer.id,
                    customerName = customer.name,
                    amount = amount,
                    receiptNumber = receiptNo,
                    paymentMethod = method,
                    notes = notes
                )
                showAddPaymentDialog = false
                Toast.makeText(context, "تم تسجيل سند القبض والدفعة بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun StatementRecordCard(
    record: StatementRecord,
    currency: String,
    dateOnlyFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isInvoice) MaterialTheme.colorScheme.surface
            else Color(0xFFF0FDF4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (record.isInvoice) MaterialTheme.colorScheme.primaryContainer
                                else Color(0xFFDCFCE7)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (record.isInvoice) Icons.Default.Receipt else Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (record.isInvoice) MaterialTheme.colorScheme.primary else Color(0xFF16A34A),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (record.isInvoice) "فاتورة مبيعات (#${record.refNumber})" else "سند قبض / تسديد (#${record.refNumber})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateOnlyFormat.format(Date(record.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount
                Text(
                    text = if (record.isInvoice) "+${WhatsAppHelper.formatPrice(record.amount, currency)}"
                    else "-${WhatsAppHelper.formatPrice(record.amount, currency)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (record.isInvoice) MaterialTheme.colorScheme.error else Color(0xFF16A34A)
                )
            }

            if (record.description.isNotBlank()) {
                Text(
                    text = "• البيان: ${record.description}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الرصيد بعد الحركة:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = WhatsAppHelper.formatPrice(record.runningBalance, currency),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerPaymentDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (amount: Double, receiptNumber: String, paymentMethod: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var receiptNumber by remember { mutableStateOf("REC-${(100..999).random()}") }
    var paymentMethod by remember { mutableStateOf("نقداً (Cash)") }
    var notes by remember { mutableStateOf("") }
    var isAmountError by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        "نقداً (Cash)",
        "زين كاش (ZainCash)",
        "تحويل بنكي / ماستر كارد",
        "شيك مصرفي",
        "أخرى"
    )
    var expandedMethodDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AddCard, contentDescription = null, tint = Color(0xFF16A34A))
                Text("تسجيل سند قبض / تسديد مالي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "الزبون: ${customer.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        if (isAmountError && (it.toDoubleOrNull() ?: 0.0) > 0) isAmountError = false
                    },
                    label = { Text("المبلغ المسدد *") },
                    placeholder = { Text("مثال: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isAmountError,
                    supportingText = { if (isAmountError) Text("يرجى إدخال مبلغ صحيح أكبر من صفر") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("رقم الوصل / سند القبض") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Method Selector
                ExposedDropdownMenuBox(
                    expanded = expandedMethodDropdown,
                    onExpandedChange = { expandedMethodDropdown = !expandedMethodDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("طريقة الدفع") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethodDropdown) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMethodDropdown,
                        onDismissRequest = { expandedMethodDropdown = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    expandedMethodDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات السند (اختياري)") },
                    placeholder = { Text("مثال: دفعة من حساب الفاتورة السابقة...") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        isAmountError = true
                    } else {
                        onSave(amount, receiptNumber, paymentMethod, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ السند والدفعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
