package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.CustomerPaymentDao
import com.example.data.local.dao.DelegateDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.CustomerPaymentEntity
import com.example.data.local.entity.DelegateEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        CustomerPaymentEntity::class,
        DelegateEntity::class,
        SupplierEntity::class,
        OrderEntity::class,
        AppSettingsEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerPaymentDao(): CustomerPaymentDao
    abstract fun delegateDao(): DelegateDao
    abstract fun supplierDao(): SupplierDao
    abstract fun orderDao(): OrderDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "souq_orders_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Initial Settings
            db.appSettingsDao().insertOrUpdate(
                AppSettingsEntity(
                    id = 1,
                    warehouseWhatsapp = "9647701234567",
                    storeName = "مخزن التجهيز العام",
                    currency = "د.ع",
                    defaultCustomerName = "مرتضى محمد",
                    defaultCustomerPhone = "07709876543",
                    defaultCustomerAddress = "بغداد - المنصور - شارع 14 رمضان",
                    currentRole = "ADMIN"
                )
            )

            // Initial Delegates (مندوبين مبيعات وتوزيع)
            val delegates = listOf(
                DelegateEntity(
                    name = "أحمد جاسم (الكرخ)",
                    phone = "07801122334",
                    code = "DEL-01",
                    area = "بغداد - الكرخ والمناطق المجاورة",
                    notes = "مندوب رئيسي لقطاع الكرخ"
                ),
                DelegateEntity(
                    name = "علي حسن (الرصافة)",
                    phone = "07705566778",
                    code = "DEL-02",
                    area = "بغداد - الرصافة والكرادة",
                    notes = "مندوب تجاري معتمد"
                ),
                DelegateEntity(
                    name = "سارة كريم (مبيعات الجملة)",
                    phone = "07509988776",
                    code = "DEL-03",
                    area = "بغداد وكافة المحافظات",
                    notes = "مسؤولة مبيعات المحلات التجارية"
                )
            )
            db.delegateDao().insertAll(delegates)

            // Initial Customers (دليل الزبائن والعملاء)
            val customers = listOf(
                CustomerEntity(
                    registryNumber = "CUS-101",
                    name = "محل البركة للمواد الغذائية",
                    phone = "07712345678",
                    address = "بغداد - الكرادة - شارع العرصات",
                    notes = "زبون جملة دائم، الدفع عند الاستلام"
                ),
                CustomerEntity(
                    registryNumber = "CUS-102",
                    name = "أسواق النور المركزية",
                    phone = "07809876543",
                    address = "بغداد - المنصور - ساحة الرواد",
                    notes = "طلبيات أسبوعية منتظمة"
                ),
                CustomerEntity(
                    registryNumber = "CUS-103",
                    name = "سوبرماركت الرافدين",
                    phone = "07501239876",
                    address = "بغداد - الكاظمية - شارع المحيط",
                    notes = "تجهيز أصناف المنظفات والغذائية"
                ),
                CustomerEntity(
                    registryNumber = "CUS-104",
                    name = "مطعم ومطبخ الضيافة",
                    phone = "07723456789",
                    address = "بغداد - الجادرية",
                    notes = "طلبيات أرز وزيوت بكميات كبيرة"
                )
            )
            db.customerDao().insertAll(customers)

            // Initial Suppliers (دليل المجهزين والموردين)
            val suppliers = listOf(
                SupplierEntity(
                    registryNumber = "SUP-201",
                    name = "شركة النورس للتجهيز الغذائي",
                    phone = "9647701234567",
                    companyName = "شركة النورس للتجارة العامة",
                    category = "مواد غذائية وحبوب",
                    address = "بغداد - جميلة - قرب علوة المواد الغذائية",
                    notes = "مجهز رئيسي للمواد التموينية والأرز والزيوت"
                ),
                SupplierEntity(
                    registryNumber = "SUP-202",
                    name = "مخزن الفرات للتجهيز العام",
                    phone = "9647802233445",
                    companyName = "مجموعة الفرات للتوزيع",
                    category = "منظفات ومواد استهلاكية",
                    address = "بغداد - الشورجة - عمارة الرافدين",
                    notes = "تجهيز سريع وتوصيل للمخازن مباشرة"
                ),
                SupplierEntity(
                    registryNumber = "SUP-203",
                    name = "شركة الأثير للأجهزة والأواني",
                    phone = "9647503344556",
                    companyName = "الأثير للاستيراد والتصدير",
                    category = "أجهزة كهربائية وأدوات منزلية",
                    address = "بغداد - الكرادة - شارع الصناعة",
                    notes = "وكيل معتمد للأجهزة والأدوات المنزلية"
                )
            )
            db.supplierDao().insertAll(suppliers)

            // Initial Customer Payments (سندات قبض وتسديدات سابقة)
            val payments = listOf(
                CustomerPaymentEntity(
                    customerId = 1,
                    customerName = "محل البركة للمواد الغذائية",
                    amount = 50000.0,
                    receiptNumber = "REC-101",
                    paymentMethod = "نقداً (Cash)",
                    notes = "تسديد دفعة حساب نقدية",
                    createdAt = System.currentTimeMillis() - 86400000L * 2
                ),
                CustomerPaymentEntity(
                    customerId = 2,
                    customerName = "أسواق النور المركزية",
                    amount = 75000.0,
                    receiptNumber = "REC-102",
                    paymentMethod = "زين كاش (ZainCash)",
                    notes = "تحويل دفعة من الحساب",
                    createdAt = System.currentTimeMillis() - 86400000L * 1
                )
            )
            db.customerPaymentDao().insertAll(payments)

            // Initial Products with high-value Arabic catalog
            val products = listOf(
                ProductEntity(
                    name = "أرز بسمتي عنبر فاخر",
                    description = "أرز حبة طويلة درجة أولى معبأ ومحكم الغلق، مناسب للمطاعم والمنازل",
                    price = 28000.0,
                    category = "مواد غذائية",
                    unit = "كيس 10 كغم",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 85
                ),
                ProductEntity(
                    name = "زيت طعام نباتي نقي 5 لتر",
                    description = "زيت ذرة نقي عالي الجودة للقلي والطبخ خالي من الكوليسترول",
                    price = 14500.0,
                    category = "مواد غذائية",
                    unit = "عبوة 5 لتر",
                    imageUrl = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 120
                ),
                ProductEntity(
                    name = "شاي سيلاني فاخر كرتون",
                    description = "شاي أسود حبيبات فريد النكهة والمذاق، كرتون يحتوي على 24 علبة",
                    price = 42000.0,
                    category = "مشروبات وشاي",
                    unit = "كرتون (24 علبة)",
                    imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 50
                ),
                ProductEntity(
                    name = "سائل غسيل الأطباق المركز",
                    description = "سائل تنظيف مضاد للدهون برائحة الليمون المنعشة، حجم اقتصادي",
                    price = 4500.0,
                    category = "منظفات ومعقمات",
                    unit = "عبوة 1.5 لتر",
                    imageUrl = "https://images.unsplash.com/photo-1585842378081-5c029306b986?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 200
                ),
                ProductEntity(
                    name = "مسحوق غسيل الملابس الأوتوماتيك",
                    description = "مسحوق تنظيف فائق الفعالية مع معطر الأقمشة طويل الأمد",
                    price = 18500.0,
                    category = "منظفات ومعقمات",
                    unit = "كيس 5 كغم",
                    imageUrl = "https://images.unsplash.com/photo-1610557892470-55d9e80c0bce?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 90
                ),
                ProductEntity(
                    name = "طقم أواني جرانيت غير لاصق 7 قطع",
                    description = "طقم قدور ومقالي جرانيت حراري مع أغطية زجاجية ومقابض عازلة للحرارة",
                    price = 75000.0,
                    category = "أدوات منزلية",
                    unit = "طقم 7 قطع",
                    imageUrl = "https://images.unsplash.com/photo-1584990347449-399097f59d5b?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 35
                ),
                ProductEntity(
                    name = "غلاية ماء ستانلس ستيل 2 لتر",
                    description = "غلاية كهربائية سريعة التسخين مع فصل أوتوماتيكي للأمان",
                    price = 16000.0,
                    category = "أجهزة وإلكترونيات",
                    unit = "جهاز قطعة",
                    imageUrl = "https://images.unsplash.com/photo-1594213114663-d94db9214470?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 60
                ),
                ProductEntity(
                    name = "خلاط ومفرمة طعام متعدد الاستخدام",
                    description = "محرك قوي 800 واط مع شفرات حادة من الستانلس ستيل ووعاء زجاجي كبير",
                    price = 38000.0,
                    category = "أجهزة وإلكترونيات",
                    unit = "قطعة",
                    imageUrl = "https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 45
                ),
                ProductEntity(
                    name = "مياه شرب نقية كرتون عبوات صغيرة",
                    description = "مياه معدنية معقمة ومطابقة للمواصفات الصحية، كرتون 40 عبوة 330 مل",
                    price = 6000.0,
                    category = "مشروبات وشاي",
                    unit = "كرتون (40 عبوة)",
                    imageUrl = "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 150
                ),
                ProductEntity(
                    name = "مناديل ورقية ناعمة عبوة التوفير",
                    description = "طبقتين فائقة النعومة والامتصاص، باقة تحتوي على 10 علب مناديل",
                    price = 8500.0,
                    category = "منظفات ومعقمات",
                    unit = "باقة (10 علب)",
                    imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&auto=format&fit=crop&q=60",
                    stockQuantity = 110
                )
            )
            db.productDao().insertAll(products)
        }
    }
}
