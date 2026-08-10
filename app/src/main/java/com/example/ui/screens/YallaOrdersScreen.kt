package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.room.OrderEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.UiState

@Composable
fun YallaOrdersScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    allOrders: List<OrderEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YallaLightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Section: Active Order Tracker Banner
        item {
            Text(
                text = "ACTIVE YALLA DELIVERIES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = YallaTextSecondary,
                    letterSpacing = 1.sp
                )
            )
        }

        if (uiState.activeOrder != null) {
            item {
                val activeOrder = uiState.activeOrder
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, YallaOrange),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(8.dp), color = YallaOrangeLight) {
                                    Icon(
                                        imageVector = Icons.Default.Moped,
                                        contentDescription = null,
                                        tint = YallaOrange,
                                        modifier = Modifier.padding(6.dp).size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = activeOrder.restaurantName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Order #${activeOrder.id.takeLast(6)}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (activeOrder.status) {
                                    "DELIVERED" -> YallaGreen
                                    "CANCELLED_REFUNDED" -> Color.Red
                                    else -> YallaOrange
                                }
                            ) {
                                Text(
                                    text = activeOrder.status,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = activeOrder.itemsSummary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (activeOrder.status != "DELIVERED" && activeOrder.status != "CANCELLED_REFUNDED") {
                            LinearProgressIndicator(
                                progress = { if (uiState.isSimulatingPipeline) uiState.simulationProgress else 0.8f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = YallaOrange,
                                trackColor = YallaOrangeLight
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = uiState.simulationStepDescription,
                                style = MaterialTheme.typography.bodySmall.copy(color = YallaOrangeDark, fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Paid: ₹${activeOrder.totalAmount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = YallaTextPrimary)
                            )

                            OutlinedButton(
                                onClick = { viewModel.triggerInstantRefundForActiveOrder("Customer cancelled via app") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Text("Cancel & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛵", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active delivery right now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Order from Yalla Verified restaurants to track live dispatch!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }

        // Section: Past Orders History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PAST ORDER HISTORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = YallaTextSecondary,
                    letterSpacing = 1.sp
                )
            )
        }

        if (allOrders.isEmpty()) {
            item {
                Text(
                    text = "No past orders placed yet. Add items to cart and place your first Yalla Yalla order!",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        } else {
            items(allOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, YallaBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = order.restaurantName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = YallaTextPrimary
                                    )
                                )
                                Text(
                                    text = order.itemsSummary,
                                    style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                                )
                            }

                            Text(
                                text = "₹${order.totalAmount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = YallaOrange
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = YallaGreenLight
                            ) {
                                Text(
                                    text = "✓ DELIVERED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = YallaGreen
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Button(
                                onClick = { viewModel.selectTab(NavigationTab.YALLA_HOME) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = YallaOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Reorder ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
