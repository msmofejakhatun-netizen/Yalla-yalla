package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.firebase.FirestoreDishItem
import com.example.data.firebase.FirestoreOrderItem
import com.example.ui.components.VegNonVegIcon
import com.example.ui.components.YallaCartBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.YallaFirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YallaFirebaseScreen(
    firebaseViewModel: YallaFirebaseViewModel,
    modifier: Modifier = Modifier
) {
    val firebaseUiState by firebaseViewModel.uiState.collectAsState()
    val categories = listOf("All", "Biryani", "Pizza", "Burger", "Healthy", "North Indian")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YallaLightBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FIREBASE STATUS & BRAND HEADER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, YallaOrange),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = YallaGreen,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                                Text(
                                    text = "FIREBASE FIRESTORE LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = YallaGreen,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            Button(
                                onClick = { firebaseViewModel.refreshFirestoreData() },
                                colors = ButtonDefaults.buttonColors(containerColor = YallaYellow),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = YallaTextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Refresh", color = YallaTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Yalla Yalla Cloud Kitchen",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = YallaTextPrimary)
                        )
                        Text(
                            text = "Listening real-time to /restaurants/rest_yalla_1/menu (inStock == true)",
                            style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                        )
                    }
                }
            }

            // ACTIVE FIRESTORE ORDER LIVE TRACKING CARD
            if (firebaseUiState.activeOrder != null) {
                item {
                    val order = firebaseUiState.activeOrder!!
                    FirestoreLiveOrderTrackingCard(
                        order = order,
                        onUpdateStatus = { nextStatus ->
                            firebaseViewModel.advanceOrderStatusInFirestore(nextStatus)
                        }
                    )
                }
            }

            // CATEGORIES FILTER RAIL
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "FILTER FIRESTORE MENU BY CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = YallaTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = firebaseUiState.selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { firebaseViewModel.setCategoryFilter(category) },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YallaOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = YallaTextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) YallaOrange else YallaBorder,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // DISHES LISTING
            item {
                Text(
                    text = "REAL-TIME DISHES (${firebaseUiState.filteredMenuItems.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = YallaTextSecondary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (firebaseUiState.isLoading && firebaseUiState.filteredMenuItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = YallaOrange)
                    }
                }
            } else {
                items(firebaseUiState.filteredMenuItems, key = { it.id }) { dish ->
                    val quantityInCart = firebaseUiState.cart.find { it.itemId == dish.id }?.quantity ?: 0
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        FirestoreDishCard(
                            dish = dish,
                            quantityInCart = quantityInCart,
                            onAddClick = { firebaseViewModel.addToCart(dish) },
                            onUpdateQuantity = { delta -> firebaseViewModel.updateCartQuantity(dish.id, delta) }
                        )
                    }
                }
            }
        }

        // FLOATING CART BAR
        if (firebaseUiState.cartTotalCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                YallaCartBar(
                    cartItemsCount = firebaseUiState.cartTotalCount,
                    cartTotal = firebaseUiState.totalAmount,
                    onGoToCartClick = { firebaseViewModel.placeOrderToFirestore() }
                )
            }
        }
    }
}

@Composable
fun FirestoreDishCard(
    dish: FirestoreDishItem,
    quantityInCart: Int,
    onAddClick: () -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, YallaBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegNonVegIcon(isVeg = dish.isVeg)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = YallaYellow.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = dish.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = YallaTextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YallaTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "₹${dish.price.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = YallaOrange
                    )
                )

                if (dish.description.isNotEmpty()) {
                    Text(
                        text = dish.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (dish.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = dish.imageUrl,
                        contentDescription = dish.name,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (quantityInCart == 0) {
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = YallaOrangeLight),
                        border = BorderStroke(1.dp, YallaOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("ADD +", color = YallaOrange, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(YallaOrange, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { onUpdateQuantity(-1) }
                        )
                        Text(
                            text = "$quantityInCart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "+",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { onUpdateQuantity(1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FirestoreLiveOrderTrackingCard(
    order: FirestoreOrderItem,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, YallaOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Moped, contentDescription = null, tint = YallaOrange)
                    Column {
                        Text(
                            text = "LIVE FIRESTORE TRACKER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = YallaOrange)
                        )
                        Text(
                            text = "Order #${order.orderId.takeLast(6)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.orderStatus) {
                        "DELIVERED" -> YallaGreen
                        "OUT_FOR_DELIVERY" -> YallaOrange
                        else -> YallaYellow
                    }
                ) {
                    Text(
                        text = order.orderStatus,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = if (order.orderStatus == "PAID" || order.orderStatus == "PREPARING") YallaTextPrimary else Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DYNAMIC PROGRESS BAR
            val progress = when (order.orderStatus) {
                "PAID" -> 0.25f
                "PREPARING" -> 0.50f
                "OUT_FOR_DELIVERY" -> 0.80f
                "DELIVERED" -> 1.0f
                else -> 0.25f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = YallaOrange,
                trackColor = YallaOrangeLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when (order.orderStatus) {
                    "PAID" -> "✓ Order received in Firestore. Kitchen notified!"
                    "PREPARING" -> "🍳 Kitchen is preparing your Yalla meals..."
                    "OUT_FOR_DELIVERY" -> "🛵 Delivery executive is en-route to your location!"
                    "DELIVERED" -> "🎉 Order delivered! Enjoy your Yalla meal!"
                    else -> "Processing order in Firestore..."
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = YallaTextPrimary)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // FIRESTORE SIMULATION CONTROLS
            Text(
                text = "TEST FIRESTORE REAL-TIME UPDATES:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = YallaTextSecondary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateStatus("PREPARING") },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("PREPARING", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onUpdateStatus("OUT_FOR_DELIVERY") },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DISPATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onUpdateStatus("DELIVERED") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = YallaGreen),
                    contentPadding = PaddingValues(2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DELIVERED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
