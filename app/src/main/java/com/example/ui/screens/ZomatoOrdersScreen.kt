package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.room.OrderEntity
import com.example.ui.components.RadarMapTrackingView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun ZomatoOrdersScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    allOrders: List<OrderEntity>,
    modifier: Modifier = Modifier
) {
    val activeOrd = uiState.activeOrder

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ZomatoLightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Active Order Live Tracking Radar Section
        item {
            Text(
                text = "ACTIVE LIVE ORDER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = ZomatoTextSecondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (activeOrd != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ZomatoRed.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeOrd.restaurantName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Order #${activeOrd.id} • ₹${activeOrd.totalAmount}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                            StatusBadge(status = activeOrd.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        RadarMapTrackingView(
                            riderName = activeOrd.riderName ?: "Assigning Rider...",
                            provider = activeOrd.deliveryProvider ?: "DUNZO",
                            etaMins = activeOrd.etaMinutes,
                            status = activeOrd.status
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (activeOrd.status != "CANCELLED_REFUNDED" && activeOrd.status != "DELIVERED") {
                            OutlinedButton(
                                onClick = { viewModel.triggerInstantRefundForActiveOrder("Customer cancelled via Orders Tab") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZomatoRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel Order & Request Instant Refund")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ZomatoGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "No Active Orders",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Place an order from the Delivery screen to view real-time rider GPS tracking!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                }
            }
        }

        // Past Orders History Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PAST ORDERS HISTORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = ZomatoTextSecondary,
                    letterSpacing = 1.sp
                )
            )
        }

        if (allOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No past orders recorded yet. Complete a checkout simulation to populate your order history!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            }
        } else {
            items(allOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.restaurantName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZomatoTextPrimary
                                )
                            )
                            StatusBadge(status = order.status)
                        }

                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodySmall.copy(color = ZomatoTextSecondary),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Bill: ₹${order.totalAmount}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ZomatoTextPrimary
                                )
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.selectTab(com.example.ui.viewmodel.NavigationTab.ZOMATO_DELIVERY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Reorder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
