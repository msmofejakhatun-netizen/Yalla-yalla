package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.DeliveryProvider
import com.example.ui.components.HeaderTitleCard
import com.example.ui.components.RadarMapTrackingView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DunzoGreen
import com.example.ui.theme.PorterBlue
import com.example.ui.theme.ZomatoRed
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun DeliveryEngineScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val activeOrd = uiState.activeOrder

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderTitleCard(
                title = "Hyperlocal Delivery Engine",
                subtitle = "Dunzo & Porter API Aggregator, Fee Estimation, Automated Dispatching, & Live GPS Radar Telemetry",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Moped,
                        contentDescription = "Delivery",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Aggregator Logistics Quote Comparison",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Distance: 3.8 km • Pickup: Koramangala -> Drop: Indiranagar",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dunzo Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.selectedDeliveryProvider == DeliveryProvider.DUNZO) DunzoGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                2.dp,
                                if (uiState.selectedDeliveryProvider == DeliveryProvider.DUNZO) DunzoGreen else Color.LightGray
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("DUNZO", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DunzoGreen))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹45.00", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("ETA: 22 mins", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                RadioButton(
                                    selected = uiState.selectedDeliveryProvider == DeliveryProvider.DUNZO,
                                    onClick = { viewModel.setDeliveryProvider(DeliveryProvider.DUNZO) }
                                )
                            }
                        }

                        // Porter Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.selectedDeliveryProvider == DeliveryProvider.PORTER) PorterBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                2.dp,
                                if (uiState.selectedDeliveryProvider == DeliveryProvider.PORTER) PorterBlue else Color.LightGray
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("PORTER", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PorterBlue))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹48.00", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text("ETA: 25 mins", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                RadioButton(
                                    selected = uiState.selectedDeliveryProvider == DeliveryProvider.PORTER,
                                    onClick = { viewModel.setDeliveryProvider(DeliveryProvider.PORTER) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Live GPS Radar Telemetry & Order Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            RadarMapTrackingView(
                riderName = activeOrd?.riderName ?: "Searching Nearest Partner...",
                provider = activeOrd?.deliveryProvider ?: uiState.selectedDeliveryProvider.name,
                etaMins = activeOrd?.etaMinutes ?: 20,
                status = activeOrd?.status ?: "WAITING_DISPATCH"
            )
        }
    }
}
