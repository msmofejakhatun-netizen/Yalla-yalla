package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.models.BackendCodeRepository
import com.example.ui.components.CodeBlockView
import com.example.ui.components.HeaderTitleCard
import com.example.ui.theme.ZomatoGreen
import com.example.ui.theme.ZomatoRed
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun BlueprintScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var selectedCodeTab by remember { mutableStateOf(0) } // 0: Razorpay Express, 1: Delivery Express

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderTitleCard(
                title = "Architectural Blueprint",
                subtitle = "End-to-End System Flow: Orders, Razorpay HMAC Webhook, Dunzo/Porter Aggregator Dispatcher",
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Architecture",
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
                        text = "System Execution Pipeline",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PipelineStepItem("1. Order Creation", "Client posts order -> Backend inserts 'INITIATED' state & creates Razorpay Order ID", true)
                    PipelineStepItem("2. Razorpay Payment", "User pays via UPI/Card -> Webhook receives payment.captured -> HMAC Signature checked", true)
                    PipelineStepItem("3. Merchant Prep", "Restaurant receives notification -> Accepts order & starts food preparation", true)
                    PipelineStepItem("4. Hyperlocal Dispatch", "Auto-triggers Dunzo API -> If unassigned, fails over to Porter API", true)
                    PipelineStepItem("5. Live Telemetry", "Logistics webhooks post GPS coordinates to time-series log table", true)
                    PipelineStepItem("6. Edge Case Safeguard", "Rider unavailable or payment fails? Automated INSTANT REFUND triggered", true)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.isSimulatingPipeline) {
                        LinearProgressIndicator(
                            progress = { uiState.simulationProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = ZomatoRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = uiState.simulationStepDescription,
                        style = MaterialTheme.typography.bodySmall.copy(color = ZomatoRed, fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runFullPipelineSimulation() },
                            enabled = !uiState.isSimulatingPipeline,
                            colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Run E2E Flow")
                        }

                        OutlinedButton(
                            onClick = { viewModel.runFullPipelineSimulation(forceRunnerFail = true) },
                            enabled = !uiState.isSimulatingPipeline,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate Failover")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Production Backend API Snippets",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TabRow(selectedTabIndex = selectedCodeTab) {
                        Tab(
                            selected = selectedCodeTab == 0,
                            onClick = { selectedCodeTab = 0 },
                            text = { Text("Razorpay & Webhooks") }
                        )
                        Tab(
                            selected = selectedCodeTab == 1,
                            onClick = { selectedCodeTab = 1 },
                            text = { Text("Dunzo/Porter Dispatcher") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedCodeTab) {
                        0 -> CodeBlockView(
                            codeText = BackendCodeRepository.razorpayBackendSnippet,
                            title = "Node.js / Express + TypeScript (Razorpay HMAC & Webhook)"
                        )
                        1 -> CodeBlockView(
                            codeText = BackendCodeRepository.deliveryBackendSnippet,
                            title = "Hyperlocal Aggregator & Failover Engine"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineStepItem(
    title: String,
    description: String,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted) ZomatoGreen else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(description, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        }
    }
}
