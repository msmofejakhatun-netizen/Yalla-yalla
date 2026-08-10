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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.UiState

@Composable
fun ZomatoProfileScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ZomatoLightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // User Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .background(ZomatoRed.copy(alpha = 0.1f))
                            .border(2.dp, ZomatoRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JS",
                            fontWeight = FontWeight.ExtraBold,
                            color = ZomatoRed,
                            fontSize = 22.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Jaspreet Singh",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZomatoTextPrimary
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE5A93C)
                            ) {
                                Text(
                                    text = "GOLD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "kjass7577@gmail.com • +91 9876543210",
                            style = MaterialTheme.typography.bodySmall.copy(color = ZomatoTextSecondary)
                        )
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = ZomatoRed)
                        Column {
                            Text("Home (Selected)", fontWeight = FontWeight.Bold, color = ZomatoTextPrimary)
                            Text("Indiranagar 100ft Road, Bengaluru - 560038", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }

                    Divider(color = Color(0xFFF0F0F0))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Work, contentDescription = null, tint = Color.Gray)
                        Column {
                            Text("Work", fontWeight = FontWeight.Bold, color = ZomatoTextPrimary)
                            Text("Embassy Tech Village, Outer Ring Road, Bengaluru", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }
                }
            }
        }

        // DEVELOPER ARCHITECTURE DASHBOARD SWITCHER
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "DEVELOPER & ARCHITECTURE DASHBOARD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = ZomatoRed,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                border = BorderStroke(1.dp, ZomatoRed)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = ZomatoRed)
                        Text(
                            text = "Switch to Backend Architecture Mode",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Inspect Database Schemas, Razorpay Webhooks, Dunzo/Porter Dispatch Engine, and Failure Recovery specs.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA6ADC8))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectTab(NavigationTab.BLUEPRINT) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Blueprint", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.selectTab(NavigationTab.SCHEMA) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313244)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Schema", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.selectTab(NavigationTab.RAZORPAY_SANDBOX) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RazorpayCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Razorpay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
