package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.FoodMenuItem
import com.example.data.models.RestaurantItem
import com.example.ui.components.RadarMapTrackingView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ZomatoGreen
import com.example.ui.theme.ZomatoRed
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun LiveOrderAppScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val restaurants = viewModel.getSampleRestaurants()
    val currentRest = uiState.selectedRestaurant ?: restaurants.first()
    val activeOrd = uiState.activeOrder

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Tracking Banner if order exists
        if (activeOrd != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ZomatoRed.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Order: ${activeOrd.restaurantName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            StatusBadge(status = activeOrd.status)
                        }
                        Text(
                            text = "Total: ₹${activeOrd.totalAmount} • Rzp ID: ${activeOrd.razorpayOrderId}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

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
                                onClick = { viewModel.triggerInstantRefundForActiveOrder("Customer cancelled from live app") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZomatoRed)
                            ) {
                                Text("Cancel Order & Instant Refund")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Select Restaurant",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(restaurants) { rest ->
                    val isSelected = rest.id == currentRest.id
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { viewModel.selectRestaurant(rest) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ZomatoRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (isSelected) ZomatoRed else Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = rest.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = rest.cuisine,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("⭐ ${rest.rating}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Text("${rest.deliveryTimeMins} mins", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "${currentRest.name} Menu",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(currentRest.menu) { item ->
            val inCartCount = uiState.cart.find { it.item.id == item.id }?.quantity ?: 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (item.isBestseller) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        "BESTSELLER",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${item.price}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (inCartCount > 0) {
                            IconButton(onClick = { viewModel.removeFromCart(item) }) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = ZomatoRed)
                            }
                            Text(
                                text = "$inCartCount",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { viewModel.addToCart(item) }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ZomatoGreen)
                        }
                    }
                }
            }
        }

        // Cart Summary & Checkout
        item {
            val subtotal = uiState.cart.fold(0.0) { acc, c -> acc + (c.item.price * c.quantity) }
            val totalItemCount = uiState.cart.fold(0) { acc, c -> acc + c.quantity }

            if (subtotal > 0.0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ZomatoRed),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$totalItemCount items in Cart",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Subtotal: ₹$subtotal + ₹45 Delivery (Dunzo)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                                )
                            }
                            Button(
                                onClick = { viewModel.runFullPipelineSimulation() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ZomatoRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Pay & Order", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
