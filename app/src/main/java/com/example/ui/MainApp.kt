package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.OrderSuccessDialog
import com.example.ui.screens.*
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.WhatsAppHelper

sealed class Screen(val title: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Catalog : Screen("المنتجات", Icons.Default.Storefront, Icons.Outlined.Storefront)
    object Invoice : Screen("الفاتورة", Icons.Default.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Customers : Screen("الزبائن", Icons.Default.Groups, Icons.Outlined.Groups)
    object Orders : Screen("سجل الفواتير", Icons.Default.HistoryEdu, Icons.Outlined.HistoryEdu)
    object Admin : Screen("الإدارة والمخزن", Icons.Default.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: StoreViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Catalog) }
    val cartCount by viewModel.cartTotalCount.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val orderNotification by viewModel.orderNotification.collectAsState()

    val screens = listOf(
        Screen.Catalog,
        Screen.Invoice,
        Screen.Customers,
        Screen.Orders,
        Screen.Admin
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = settings.storeName.ifBlank { "نظام الفواتير والمبيعات" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Fast Invoice/Cart button in header
                    if (currentScreen != Screen.Invoice) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartCount")
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            IconButton(onClick = { currentScreen = Screen.Invoice }) {
                                Icon(
                                    imageVector = Icons.Outlined.ReceiptLong,
                                    contentDescription = "الفاتورة"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            if (screen == Screen.Invoice) {
                                BadgedBox(
                                    badge = {
                                        if (cartCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White
                                            ) {
                                                Text("$cartCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val screensOrder = listOf(
                        Screen.Catalog,
                        Screen.Invoice,
                        Screen.Customers,
                        Screen.Orders,
                        Screen.Admin
                    )
                    val initialIndex = screensOrder.indexOf(initialState)
                    val targetIndex = screensOrder.indexOf(targetState)
                    val direction = if (targetIndex >= initialIndex) 1 else -1

                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { fullWidth -> direction * (fullWidth / 3) }
                    ) + fadeIn(animationSpec = tween(220)))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(180),
                                targetOffsetX = { fullWidth -> -direction * (fullWidth / 3) }
                            ) + fadeOut(animationSpec = tween(180))
                        )
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.Catalog -> CatalogScreen(
                        viewModel = viewModel,
                        onNavigateToCart = { currentScreen = Screen.Invoice }
                    )
                    Screen.Invoice -> CartScreen(
                        viewModel = viewModel,
                        onNavigateToCatalog = { currentScreen = Screen.Catalog },
                        onNavigateToCustomers = { currentScreen = Screen.Customers }
                    )
                    Screen.Customers -> CustomersScreen(
                        viewModel = viewModel,
                        onNavigateToInvoice = { currentScreen = Screen.Invoice }
                    )
                    Screen.Orders -> OrdersHistoryScreen(
                        viewModel = viewModel
                    )
                    Screen.Admin -> AdminManagementScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        // Instant In-App Order Confirmation Alert Dialog
        orderNotification?.let { notif ->
            OrderSuccessDialog(
                info = notif,
                onDismiss = { viewModel.dismissNotification() },
                onOpenWhatsAppAgain = {
                    val message = "📦 طلب تجهيز برقم: ${notif.orderNumber}"
                    WhatsAppHelper.openWhatsApp(context, notif.whatsappNumber, message)
                    viewModel.dismissNotification()
                }
            )
        }
    }
}
