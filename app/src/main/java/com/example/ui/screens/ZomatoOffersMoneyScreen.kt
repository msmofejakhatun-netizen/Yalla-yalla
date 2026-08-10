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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun ZomatoOffersMoneyScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val offers = viewModel.getSampleOffers()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ZomatoLightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Zomato Gold Membership Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2C2210), Color(0xFF141009))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFE5A93C), modifier = Modifier.size(28.dp))
                                Text(
                                    text = "ZOMATO GOLD",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFE5A93C),
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE5A93C)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Zero Delivery Fee on all orders above ₹199 within 7 km",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Text(
                            text = "Up to 40% OFF at top dining restaurants in Bengaluru",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD1C4E9))
                        )
                    }
                }
            }
        }

        // Section Title: Exclusive Coupons
        item {
            Text(
                text = "AVAILABLE PROMO COUPONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = ZomatoTextSecondary,
                    letterSpacing = 1.sp
                )
            )
        }

        items(offers) { offer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFEFEFEF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ZomatoRed.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = offer.discountCode,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ZomatoRed
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = offer.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZomatoTextPrimary
                            )
                        )

                        Text(
                            text = offer.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = ZomatoTextSecondary)
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.selectTab(com.example.ui.viewmodel.NavigationTab.ZOMATO_DELIVERY) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZomatoRed),
                        border = BorderStroke(1.dp, ZomatoRed)
                    ) {
                        Text("TAP TO USE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section: Wallet & Payment Methods
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ZOMATO MONEY & PAYMENT METHODS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = ZomatoTextSecondary,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = RazorpayCyan, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = "Zomato Edition / Razorpay Wallet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Balance: ₹450.00",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ZomatoGreen, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = RazorpayCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Add Money", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
