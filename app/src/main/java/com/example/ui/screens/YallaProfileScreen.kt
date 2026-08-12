package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.YallaFirebaseViewModel

@Composable
fun YallaProfileScreen(
    viewModel: ArchitectureViewModel,
    firebaseViewModel: YallaFirebaseViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val firebaseUiState by firebaseViewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YallaLightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Profile Header Card with Live Auth Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, YallaBorder)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(YallaOrangeLight)
                            .border(2.dp, YallaOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (firebaseUiState.userPhone.length > 3) firebaseUiState.userPhone.takeLast(2) else "YY",
                            fontWeight = FontWeight.ExtraBold,
                            color = YallaOrange,
                            fontSize = 22.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (firebaseUiState.isUserLoggedIn) "Customer Account" else "Guest User",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YallaTextPrimary
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = YallaGold
                            ) {
                                Text(
                                    text = firebaseUiState.userRole,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (firebaseUiState.userPhone.isNotEmpty()) firebaseUiState.userPhone else "+91 9876543210",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = YallaOrange)
                        )
                        Text(
                            text = "UID: ${if (firebaseUiState.userUid.isNotEmpty()) firebaseUiState.userUid else "Anonymous"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }

        // Yalla Coins Quick Balance Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTab(NavigationTab.YALLA_COINS) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, YallaGold)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🪙", fontSize = 24.sp)
                        Column {
                            Text("Yalla Coins Balance", fontWeight = FontWeight.Bold, color = YallaTextPrimary)
                            Text("${uiState.yallaCoinsBalance} Coins Available", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB47D00), fontWeight = FontWeight.Bold))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = YallaGold
                    ) {
                        Text("REDEEM ➔", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }

        // Saved Delivery Addresses Section
        item {
            Text(
                text = "SAVED DELIVERY ADDRESSES",
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, YallaBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = YallaOrange)
                        Column {
                            Text("Current Location", fontWeight = FontWeight.Bold, color = YallaTextPrimary)
                            Text(firebaseUiState.deliveryLocation, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Work, contentDescription = null, tint = Color.Gray)
                        Column {
                            Text("Work", fontWeight = FontWeight.Bold, color = YallaTextPrimary)
                            Text("Embassy Tech Village, Outer Ring Road, Bengaluru", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }
                }
            }
        }

        // Account & Quick Settings Section
        item {
            Text(
                text = "ACCOUNT & QUICK SETTINGS",
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, YallaBorder)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // My Orders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectTab(NavigationTab.YALLA_ORDERS) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(YallaOrangeLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = YallaOrange, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("My Orders", fontWeight = FontWeight.Bold, color = YallaTextPrimary, fontSize = 15.sp)
                                Text("View past orders & live tracking", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }

                    HorizontalDivider(color = Color(0xFFF2F2F2), modifier = Modifier.padding(horizontal = 16.dp))

                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { firebaseViewModel.signOut() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Logout / Switch Account", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 15.sp)
                                Text("Sign out securely and return to Phone OTP screen", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }
}
