package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeBlockView
import com.example.ui.components.HeaderTitleCard
import com.example.ui.theme.ZomatoRed
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun EdgeCasesScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderTitleCard(
                title = "Error Handling & Edge Cases Suite",
                subtitle = "Interactive Playground for Payment Failures, Signature Mismatches, Runner Failovers, & Webhook Idempotency",
                icon = {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Edge Cases",
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
                        text = "1. Failed Payment & Bank Decline Rollback",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Simulates user bank declining payment. Order remains in 'PAYMENT_FAILED' state and no restaurant order is triggered.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.runFullPipelineSimulation(forcePaymentFail = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                        enabled = !uiState.isSimulatingPipeline
                    ) {
                        Text("Trigger Payment Failure Flow")
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
                        text = "2. Runner Unavailability & Auto Instant Refund",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Dunzo fails -> Porter fallback fails -> System cancels order and issues Razorpay INSTANT REFUND automatically.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.runFullPipelineSimulation(forceRunnerFail = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                        enabled = !uiState.isSimulatingPipeline
                    ) {
                        Text("Trigger Runner Unavailability Failover")
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
                        text = "3. Webhook Idempotency & Replay Protection",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Guarantees duplicate webhooks with the same event_id return HTTP 200 without duplicate database inserts or double refunds.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CodeBlockView(
                        codeText = """
                            // Idempotency check snippet:
                            const existing = await db.query('SELECT id FROM webhook_events WHERE event_id = $1', [eventId]);
                            if (existing.rows.length > 0) {
                              return res.status(200).json({ status: 'already_processed' });
                            }
                        """.trimIndent(),
                        title = "Idempotency Protection Logic"
                    )
                }
            }
        }
    }
}
