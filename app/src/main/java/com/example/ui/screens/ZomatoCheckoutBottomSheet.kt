package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.models.DeliveryProvider
import com.example.ui.components.VegNonVegIcon
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZomatoCheckoutBottomSheet(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    onDismiss: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val subtotal = uiState.cart.fold(0.0) { acc, c -> acc + (c.item.price * c.quantity) }
    val deliveryFee = if (uiState.selectedDeliveryProvider == DeliveryProvider.DUNZO) 45.0 else 48.0
    val promoDiscount = if (subtotal >= 200.0) 100.0 else 0.0
    val grandTotal = (subtotal + deliveryFee - promoDiscount).coerceAtLeast(0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YOUR ZOMATO CART",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ZomatoTextPrimary
                        )
                    )
                    Text(
                        text = "From ${uiState.selectedRestaurant?.name ?: "Biryani Blues"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZomatoTextSecondary)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cart Items List
                items(uiState.cart) { cartItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ZomatoLightBg, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            VegNonVegIcon(isVeg = cartItem.item.isVeg)
                            Column {
                                Text(
                                    text = cartItem.item.name,
                                    fontWeight = FontWeight.Bold,
                                    color = ZomatoTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "₹${cartItem.item.price.toInt()} each",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ZomatoTextSecondary)
                                )
                            }
                        }

                        // Qty Selector
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, ZomatoRed)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { viewModel.removeFromCart(cartItem.item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold, color = ZomatoRed)
                                }
                                Text(
                                    text = "${cartItem.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { viewModel.addToCart(cartItem.item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold, color = ZomatoRed)
                                }
                            }
                        }
                    }
                }

                // Coupon Applied Banner
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, ZomatoGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = ZomatoGreen)
                                Column {
                                    Text("ZOMATO50 APPLIED", fontWeight = FontWeight.Bold, color = ZomatoGreen, fontSize = 13.sp)
                                    Text("₹100 discount unlocked on this order", style = MaterialTheme.typography.bodySmall.copy(color = ZomatoGreen))
                                }
                            }
                            Text("✓", fontWeight = FontWeight.Black, color = ZomatoGreen)
                        }
                    }
                }

                // Delivery Provider Selector
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("SELECT HYPERLOCAL DELIVERY PARTNER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ZomatoTextSecondary))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.selectedDeliveryProvider == DeliveryProvider.DUNZO,
                            onClick = { viewModel.setDeliveryProvider(DeliveryProvider.DUNZO) },
                            label = { Text("Dunzo Direct (₹45)") },
                            leadingIcon = { Icon(Icons.Default.Moped, contentDescription = null, tint = DunzoGreen) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.selectedDeliveryProvider == DeliveryProvider.PORTER,
                            onClick = { viewModel.setDeliveryProvider(DeliveryProvider.PORTER) },
                            label = { Text("Porter Express (₹48)") },
                            leadingIcon = { Icon(Icons.Default.Moped, contentDescription = null, tint = PorterBlue) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Bill Details Breakdown
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ZomatoLightBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("BILL DETAILS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, color = ZomatoTextSecondary))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Item Total", color = ZomatoTextSecondary, fontSize = 13.sp)
                                Text("₹${subtotal.toInt()}", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Partner Fee", color = ZomatoTextSecondary, fontSize = 13.sp)
                                Text("₹${deliveryFee.toInt()}", fontWeight = FontWeight.Bold)
                            }
                            if (promoDiscount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Promo Discount (ZOMATO50)", color = ZomatoGreen, fontSize = 13.sp)
                                    Text("-₹${promoDiscount.toInt()}", fontWeight = FontWeight.Bold, color = ZomatoGreen)
                                }
                            }
                            Divider(color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("₹${grandTotal.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = ZomatoRed))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pay & Place Order Button
            Button(
                onClick = {
                    onOrderPlaced()
                    viewModel.runFullPipelineSimulation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "PAY ₹${grandTotal.toInt()} WITH RAZORPAY  ➔",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                )
            }
        }
    }
}
