package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.entity.DelegateEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.model.CartItem
import com.example.data.model.StatementRecord
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppHelper {

    fun formatPrice(amount: Double, currency: String = "د.ع"): String {
        val formatter = NumberFormat.getNumberInstance(Locale("ar", "IQ"))
        return "${formatter.format(amount)} $currency"
    }

    fun generateOrderNumber(): String {
        val timePart = SimpleDateFormat("yyMMddHHmm", Locale.US).format(Date())
        val randomPart = (100..999).random()
        return "INV-$timePart-$randomPart"
    }

    fun buildCustomerInvoiceText(
        invoiceNumber: String,
        storeName: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        customerNotes: String,
        cartItems: List<CartItem>,
        subtotalAmount: Double,
        discount: Double = 0.0,
        deliveryFee: Double = 0.0,
        netTotalAmount: Double,
        currency: String = "د.ع"
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val dateString = dateFormat.format(Date())

        val builder = StringBuilder()
        builder.append("🧾 *فاتورة مبيعات - $storeName* 🧾\n")
        builder.append("🔖 *رقم الفاتورة:* `$invoiceNumber`\n")
        builder.append("🕒 *التاريخ:* $dateString\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("👤 *الزبون:* $customerName\n")
        if (customerPhone.isNotBlank()) builder.append("📞 *رقم الهاتف:* $customerPhone\n")
        if (customerAddress.isNotBlank()) builder.append("📍 *العنوان:* $customerAddress\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📋 *تفاصيل المنتجات:*\n\n")

        cartItems.forEachIndexed { index, item ->
            val num = index + 1
            builder.append("$num. *${item.product.name}*\n")
            builder.append("   • الكمية: ${item.quantity} ${item.product.unit}\n")
            builder.append("   • السعر المفرد: ${formatPrice(item.product.price, currency)}\n")
            builder.append("   • الإجمالي: *${formatPrice(item.subtotal, currency)}*\n")
            if (item.itemNote.isNotBlank()) {
                builder.append("   • ملاحظة: _${item.itemNote}_\n")
            }
            builder.append("\n")
        }

        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("💵 *المجموع الفرعي:* ${formatPrice(subtotalAmount, currency)}\n")
        if (discount > 0) {
            builder.append("🏷️ *الخصم:* -${formatPrice(discount, currency)}\n")
        }
        if (deliveryFee > 0) {
            builder.append("🚚 *أجور التوصيل:* +${formatPrice(deliveryFee, currency)}\n")
        }
        builder.append("💰 *الصافي المطلوب:* *${formatPrice(netTotalAmount, currency)}*\n")

        if (customerNotes.isNotBlank()) {
            builder.append("━━━━━━━━━━━━━━━━━━━━\n")
            builder.append("📝 *ملاحظات الفاتورة:* $customerNotes\n")
        }

        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("🙏 شكراً لتعاملكم معنا! نتمنى لكم يوماً سعيداً ✨")

        return builder.toString()
    }

    fun shareText(context: Context, title: String, text: String) {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_TITLE, title)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, title)
            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر مشاركة الفاتورة", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildWhatsAppOrderMessage(
        orderNumber: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        customerNotes: String,
        delegate: DelegateEntity?,
        supplier: SupplierEntity? = null,
        cartItems: List<CartItem>,
        totalAmount: Double,
        currency: String = "د.ع"
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val dateString = dateFormat.format(Date())

        val builder = StringBuilder()
        builder.append("📦 *طلب تجهيز بضاعة جديد* 📦\n")
        builder.append("🔖 *رقم الطلب:* `$orderNumber`\n")
        builder.append("🕒 *التاريخ والتوقيت:* $dateString\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")

        if (supplier != null) {
            builder.append("🏭 *المجهز المستلم:* ${supplier.name}")
            if (supplier.companyName.isNotBlank()) builder.append(" (${supplier.companyName})")
            if (supplier.registryNumber.isNotBlank()) builder.append(" [قيد: ${supplier.registryNumber}]")
            builder.append("\n")
            if (supplier.phone.isNotBlank()) builder.append("📞 *هاتف المجهز:* ${supplier.phone}\n")
            builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        }
        
        builder.append("👤 *معلومات الزبون/المستلم:*\n")
        builder.append("• *الاسم:* $customerName\n")
        if (customerPhone.isNotBlank()) builder.append("• *رقم الهاتف:* $customerPhone\n")
        if (customerAddress.isNotBlank()) builder.append("• *العنوان:* $customerAddress\n")

        if (delegate != null) {
            builder.append("👔 *المندوب المسؤول:* ${delegate.name} (رقم قيد: ${delegate.code})\n")
            builder.append("📞 *هاتف المندوب:* ${delegate.phone}\n")
        } else {
            builder.append("👔 *المندوب:* طلب مباشر من العميل\n")
        }

        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📋 *قائمة المواد للتجهيز بالمخزن:*\n\n")

        cartItems.forEachIndexed { index, item ->
            val emojiNumber = when (index + 1) {
                1 -> "1️⃣"
                2 -> "2️⃣"
                3 -> "3️⃣"
                4 -> "4️⃣"
                5 -> "5️⃣"
                6 -> "6️⃣"
                7 -> "7️⃣"
                8 -> "8️⃣"
                9 -> "9️⃣"
                10 -> "🔟"
                else -> "▫️"
            }
            builder.append("$emojiNumber *${item.product.name}*\n")
            builder.append("   • الوحدة: ${item.product.unit}\n")
            builder.append("   • الكمية المطلوبة: *${item.quantity}*\n")
            builder.append("   • السعر المفرد: ${formatPrice(item.product.price, currency)}\n")
            builder.append("   • المجموع الفرعي: *${formatPrice(item.subtotal, currency)}*\n")
            if (item.itemNote.isNotBlank()) {
                builder.append("   • ملاحظة خاصة للصنف: _${item.itemNote}_\n")
            }
            builder.append("\n")
        }

        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        if (customerNotes.isNotBlank()) {
            builder.append("📝 *ملاحظات التجهيز والشحن:* \n")
            builder.append("$customerNotes\n")
            builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        }

        val totalQuantity = cartItems.sumOf { it.quantity }
        builder.append("📊 *إجمالي عدد القطع/الوحدات:* $totalQuantity\n")
        builder.append("💰 *المجموع الكلي المطلوب:* *${formatPrice(totalAmount, currency)}*\n")
        builder.append("🚚 *طريقة الدفع:* بدون دفع إلكتروني (دفع عند الاستلام/التجهيز)\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("✨ _تم إرسال هذا الطلب عبر تطبيق تجهيز وسوق المندوبين_")

        return builder.toString()
    }

    fun buildCustomerStatementText(
        storeName: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        openingBalance: Double,
        totalDebit: Double, // إجمالي المبيعات / الفواتير
        totalCredit: Double, // إجمالي التسديدات / المقبوضات
        finalBalance: Double, // الرصيد المتبقي
        entries: List<StatementRecord>,
        customerRegistryNumber: String = "",
        currency: String = "د.ع"
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val dateOnlyFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
        val dateString = dateFormat.format(Date())

        val builder = StringBuilder()
        builder.append("📊 *كشف حساب زبون - $storeName* 📊\n")
        builder.append("🕒 *تاريخ الاستخراج:* $dateString\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("👤 *الزبون:* $customerName")
        if (customerRegistryNumber.isNotBlank()) builder.append(" [رقم القيد: $customerRegistryNumber]")
        builder.append("\n")
        if (customerPhone.isNotBlank()) builder.append("📞 *الهاتف:* $customerPhone\n")
        if (customerAddress.isNotBlank()) builder.append("📍 *العنوان:* $customerAddress\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📈 *الملخص المالي:* \n")
        if (openingBalance > 0) {
            builder.append("• الرصيد الافتتاحي السابـق: ${formatPrice(openingBalance, currency)}\n")
        }
        builder.append("• إجمالي الفواتير الصادرة (مدين +): *${formatPrice(totalDebit, currency)}*\n")
        builder.append("• إجمالي المبالغ المسددة (دائن -): *${formatPrice(totalCredit, currency)}*\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        val balanceLabel = if (finalBalance > 0) "🔴 الرصيد المتبقي المطلوب ذمة:" else if (finalBalance < 0) "🟢 رصيد دائن لصالح الزبون:" else "⚪ الحساب خالص ومسدد بالكامل:"
        builder.append("$balanceLabel *${formatPrice(Math.abs(finalBalance), currency)}*\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("📋 *سجل الحركات والتفاصيل:*\n\n")

        if (entries.isEmpty()) {
            builder.append("لا توجد حركات مسجلة حالياً.\n")
        } else {
            entries.forEachIndexed { index, entry ->
                val num = index + 1
                val entryDate = dateOnlyFormat.format(Date(entry.timestamp))
                val icon = if (entry.isInvoice) "🧾" else "💵"
                val typeName = if (entry.isInvoice) "فاتورة مبيعات" else "سند قبض / تسديد"
                val amountStr = if (entry.isInvoice) "+${formatPrice(entry.amount, currency)}" else "-${formatPrice(entry.amount, currency)}"

                builder.append("$num. $icon *$typeName* ($entryDate)\n")
                builder.append("   • الرقم المرجعي: `${entry.refNumber}`\n")
                builder.append("   • البيان: ${entry.description}\n")
                builder.append("   • المبلغ: *$amountStr*\n")
                builder.append("   • الرصيد بعدها: ${formatPrice(entry.runningBalance, currency)}\n\n")
            }
        }

        builder.append("━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("يرجى مراجعة الحساب والمطابقة، نشكر حسن تعاونكم الدائم 🙏✨")

        return builder.toString()
    }

    fun openWhatsApp(
        context: Context,
        phoneNumber: String,
        message: String
    ): Boolean {
        return try {
            // Clean phone number (remove spaces, dashes, leading +, leading zeros if needed)
            var cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
            if (cleanPhone.startsWith("00")) {
                cleanPhone = cleanPhone.substring(2)
            }
            // If local Iraqi number like 0770..., prepend 964
            if (cleanPhone.startsWith("07") && cleanPhone.length == 11) {
                cleanPhone = "964" + cleanPhone.substring(1)
            }

            val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.name())
            val url = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                // Fallback to browser or any app that can open the WhatsApp link
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر فتح تطبيق الواتساب", Toast.LENGTH_LONG).show()
            false
        }
    }
}
