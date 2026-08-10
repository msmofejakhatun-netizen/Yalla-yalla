package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.YallaBottomBar
import com.example.ui.components.ZomatoCustomerBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.YallaOrange
import com.example.ui.theme.YallaTheme
import com.example.ui.theme.ZomatoRed
import com.example.ui.theme.ZomatoTheme
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    private val viewModel: ArchitectureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YallaTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
                val allWebhooks by viewModel.allWebhooks.collectAsStateWithLifecycle()

                var isCheckoutSheetOpen by remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.successMessage) {
                    uiState.successMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearMessages()
                    }
                }

                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearMessages()
                    }
                }

                val isCustomerTab = uiState.activeTab in listOf(
                    NavigationTab.YALLA_HOME,
                    NavigationTab.YALLA_ORDERS,
                    NavigationTab.YALLA_COINS,
                    NavigationTab.YALLA_PROFILE,
                    NavigationTab.ZOMATO_DELIVERY,
                    NavigationTab.ZOMATO_ORDERS,
                    NavigationTab.ZOMATO_MONEY,
                    NavigationTab.ZOMATO_PROFILE
                )

                Scaffold(
                    topBar = {
                        if (!isCustomerTab) {
                            DeveloperTopBar(
                                currentTab = uiState.activeTab,
                                onReturnToApp = { viewModel.selectTab(NavigationTab.YALLA_HOME) }
                            )
                        }
                    },
                    bottomBar = {
                        if (isCustomerTab) {
                            YallaBottomBar(
                                selectedTab = uiState.activeTab,
                                yallaCoinsCount = uiState.yallaCoinsBalance,
                                onTabSelect = { viewModel.selectTab(it) }
                            )
                        } else {
                            DeveloperBottomBar(
                                selectedTab = uiState.activeTab,
                                onTabSelect = { viewModel.selectTab(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.activeTab) {
                            // Yalla Yalla Customer UI Screens
                            NavigationTab.YALLA_HOME, NavigationTab.ZOMATO_DELIVERY -> YallaHomeScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onViewCartClick = { isCheckoutSheetOpen = true }
                            )
                            NavigationTab.YALLA_ORDERS, NavigationTab.ZOMATO_ORDERS -> YallaOrdersScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                allOrders = allOrders
                            )
                            NavigationTab.YALLA_COINS, NavigationTab.ZOMATO_MONEY -> YallaCoinsScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                            NavigationTab.YALLA_PROFILE, NavigationTab.ZOMATO_PROFILE -> YallaProfileScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )

                            // Developer Architecture Specs Screens
                            NavigationTab.BLUEPRINT -> BlueprintScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                            NavigationTab.SCHEMA -> SchemaScreen()
                            NavigationTab.RAZORPAY_SANDBOX -> RazorpaySandboxScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                allOrders = allOrders,
                                allWebhooks = allWebhooks
                            )
                            NavigationTab.DELIVERY_ENGINE -> DeliveryEngineScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                            NavigationTab.EDGE_CASES -> EdgeCasesScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                        }

                        // Yalla Yalla Cart & Payment Checkout Bottom Sheet
                        if (isCheckoutSheetOpen) {
                            YallaCheckoutBottomSheet(
                                viewModel = viewModel,
                                uiState = uiState,
                                onDismiss = { isCheckoutSheetOpen = false },
                                onOrderPlaced = {
                                    isCheckoutSheetOpen = false
                                    viewModel.selectTab(NavigationTab.YALLA_ORDERS)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeveloperTopBar(
    currentTab: NavigationTab,
    onReturnToApp: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "ARCH SPECS: ${currentTab.name}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ZomatoRed
                    )
                )
                Text(
                    text = "Developer Architecture Dashboard",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onReturnToApp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Zomato App",
                    tint = ZomatoRed
                )
            }
        },
        actions = {
            TextButton(onClick = onReturnToApp) {
                Text("Return to Zomato UI", color = ZomatoRed, fontWeight = FontWeight.Bold)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
private fun DeveloperBottomBar(
    selectedTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == NavigationTab.BLUEPRINT,
            onClick = { onTabSelect(NavigationTab.BLUEPRINT) },
            icon = { Icon(Icons.Default.AccountTree, contentDescription = "Blueprint") },
            label = { Text("Blueprint") }
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.SCHEMA,
            onClick = { onTabSelect(NavigationTab.SCHEMA) },
            icon = { Icon(Icons.Default.Storage, contentDescription = "Schema") },
            label = { Text("Schema") }
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.RAZORPAY_SANDBOX,
            onClick = { onTabSelect(NavigationTab.RAZORPAY_SANDBOX) },
            icon = { Icon(Icons.Default.Payment, contentDescription = "Razorpay") },
            label = { Text("Razorpay") }
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.DELIVERY_ENGINE,
            onClick = { onTabSelect(NavigationTab.DELIVERY_ENGINE) },
            icon = { Icon(Icons.Default.Moped, contentDescription = "Delivery") },
            label = { Text("Delivery") }
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.EDGE_CASES,
            onClick = { onTabSelect(NavigationTab.EDGE_CASES) },
            icon = { Icon(Icons.Default.BugReport, contentDescription = "Edge Cases") },
            label = { Text("Failover") }
        )
    }
}
