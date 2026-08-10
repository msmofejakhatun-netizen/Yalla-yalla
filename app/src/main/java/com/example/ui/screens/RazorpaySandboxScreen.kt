package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.PaymentMethodType
import com.example.data.models.RazorpayCryptoUtils
import com.example.data.room.OrderEntity
import com.example.data.room.WebhookLogEntity
import com.example.ui.components.CodeBlockView
import com.example.ui.components.HeaderTitleCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RazorpayCyan
import com.example.ui.theme.ZomatoRed
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun RazorpaySandboxScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    allOrders: List<OrderEntity>,
    allWebhooks: List<WebhookLogEntity>,
    modifier: Modifier = Modifier
) {
    var orderIdInput by remember { mutableStateOf("order_Lz9x811a") }
    var paymentIdInput by remember { mutableStateOf("pay_Pz923140") }
    var signatureInput by remember { mutableStateOf("") }
    var secretInput by remember { mutableStateOf(uiState.rzpSecretKey) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethodType.UPI) }

    // Auto calculate initial valid signature
    LaunchedEffect(orderIdInput, paymentIdInput, secretInput) {
        signatureInput = RazorpayCryptoUtils.calculateHmacSha256("$orderIdInput|$paymentIdInput", secretInput)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderTitleCard(
                title = "Razorpay Gateway Sandbox",
                subtitle = "Test HMAC SHA-256 Signature Verification, Webhook Capture, UPI/Card/NetBanking Flows, & Instant Refunds",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Payment",
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
                        text = "HMAC SHA-256 Signature Calculator & Verification",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "generated_signature = HMAC_SHA256(order_id + '|' + payment_id, secret)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = orderIdInput,
                        onValueChange = { orderIdInput = it },
                        label = { Text("Razorpay Order ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = paymentIdInput,
                        onValueChange = { paymentIdInput = it },
                        label = { Text("Razorpay Payment ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = secretInput,
                        onValueChange = { secretInput = it },
                        label = { Text("Webhook Secret Key") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signatureInput,
                        onValueChange = { signatureInput = it },
                        label = { Text("Calculated / Input Signature") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.verifySignatureInSandbox(orderIdInput, paymentIdInput, signatureInput, secretInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RazorpayCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Verify Signature")
                        }

                        OutlinedButton(
                            onClick = {
                                signatureInput += "tampered_fake_suffix"
                                viewModel.verifySignatureInSandbox(orderIdInput, paymentIdInput, signatureInput, secretInput)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate Tampering")
                        }
                    }

                    if (uiState.customVerificationMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.customVerificationMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
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
                        text = "Instant Refund Trigger",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Refunds speed = 'instant' back to customer UPI / Bank Account",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeOrd = uiState.activeOrder
                    if (activeOrd != null) {
                        Text(
                            text = "Active Order: ${activeOrd.id} (${activeOrd.restaurantName})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Amount: ₹${activeOrd.totalAmount} • Status: ${activeOrd.status}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.triggerInstantRefundForActiveOrder("User cancelled from app") },
                            colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                            enabled = activeOrd.status != "CANCELLED_REFUNDED"
                        ) {
                            Text("Trigger Instant Refund")
                        }
                    } else {
                        Text(
                            text = "No active order in session. Place an order or run pipeline first.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Recorded Webhook Events Audit Trail",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (allWebhooks.isEmpty()) {
            item {
                Text("No webhooks logged yet. Run a pipeline simulation above.", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
        } else {
            items(allWebhooks) { webhook ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${webhook.sourceProvider} • ${webhook.eventType}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            StatusBadge(status = if (webhook.processed) "CAPTURED" else "PENDING")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        CodeBlockView(codeText = webhook.payloadJson, title = "Event ID: ${webhook.eventId}")
                    }
                }
            }
        }
    }
}
