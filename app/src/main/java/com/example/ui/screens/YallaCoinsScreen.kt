package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

data class CoinVoucher(
    val id: String,
    val title: String,
    val description: String,
    val costInCoins: Int,
    val discountTag: String,
    val iconEmoji: String
)

@Composable
fun YallaCoinsScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val coinVouchers = listOf(
        CoinVoucher("v_1", "FLAT ₹100 OFF VOUCHER", "Valid on all Yalla Verified restaurant orders above ₹299", 500, "₹100 SAVINGS", "🎟️"),
        CoinVoucher("v_2", "FREE GOURMET DESSERT PASS", "Get a free dessert item on your next order", 350, "FREE DESSERT", "🍰"),
        CoinVoucher("v_3", "ZERO DELIVERY FEE PASS", "Free express delivery for 3 consecutive orders", 600, "FREE DELIVERY", "🚀"),
        CoinVoucher("v_4", "FLAT ₹250 MEGA CASHBACK", "Instant wallet credit on orders above ₹599", 1000, "₹250 CASHBACK", "💰")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YallaLightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2C1D0F), Color(0xFF140B04))
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
                                Text("🪙", fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = "YALLA COINS HUB",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = YallaGold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = "${uiState.yallaCoinsBalance}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = YallaGold
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = YallaGold
                            ) {
                                Text(
                                    text = "VIP MEMBER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Divider(color = Color.White.copy(alpha = 0.15f))

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Streak Banner Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = YallaOrange,
                            modifier = Modifier.clickable { viewModel.claimDailyYallaCoins() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🔥", fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = "Claim Daily Coin Streak!",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Tap to get +100 bonus Yalla Coins today",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                                        )
                                    }
                                }
                                Text("CLAIM ➔", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Redeem Vouchers Store
        item {
            Text(
                text = "REDEEM YALLA COIN VOUCHERS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = YallaTextSecondary,
                    letterSpacing = 1.sp
                )
            )
        }

        items(coinVouchers) { voucher ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, YallaBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = YallaGoldLight,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(voucher.iconEmoji, fontSize = 22.sp)
                            }
                        }

                        Column {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = YallaOrange.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = voucher.discountTag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = YallaOrange
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = voucher.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YallaTextPrimary
                                )
                            )

                            Text(
                                text = voucher.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary),
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.redeemYallaCoins(voucher.costInCoins, voucher.title) },
                        colors = ButtonDefaults.buttonColors(containerColor = YallaGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🪙", fontSize = 12.sp)
                            Text("${voucher.costInCoins}", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Coin Activity History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RECENT YALLA COIN TRANSACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = YallaTextSecondary,
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🎉", fontSize = 20.sp)
                            Column {
                                Text("Order #ORD_9812 Cashback", fontWeight = FontWeight.Bold, color = YallaTextPrimary)
                                Text("Earned 2X coins on Yalla Verified order", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                            }
                        }
                        Text("+120 🪙", fontWeight = FontWeight.Bold, color = YallaGreen)
                    }

                    Divider(color = Color(0xFFF0F0F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🔥", fontSize = 20.sp)
                            Column {
                                Text("Daily Login Streak", fontWeight = FontWeight.Bold, color = YallaTextPrimary)
                                Text("Day 5 streak bonus reward", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                            }
                        }
                        Text("+100 🪙", fontWeight = FontWeight.Bold, color = YallaGreen)
                    }
                }
            }
        }
    }
}
