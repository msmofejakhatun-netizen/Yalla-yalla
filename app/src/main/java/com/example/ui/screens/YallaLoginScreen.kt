package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FirebaseUiState
import com.example.ui.viewmodel.YallaFirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YallaLoginScreen(
    firebaseViewModel: YallaFirebaseViewModel,
    uiState: FirebaseUiState,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var phoneNumberInput by remember { mutableStateOf(uiState.inputPhoneNumber) }
    var otpCodeInput by remember { mutableStateOf("") }

    // Trust & Clean Neutral App Canvas (#FAFAFA)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        YallaOrange.copy(alpha = 0.08f),
                        YallaLightBg,
                        YallaLightBg
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Branding Logo with Royal Orange Accent
            Surface(
                shape = CircleShape,
                color = YallaOrange,
                shadowElevation = 8.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "⚡", fontSize = 42.sp)
                }
            }

            // Trust Anchor Header in Deep Navy Blue (#0F172A)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Yalla Yalla",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = YallaNavy, // Deep Navy Blue Trust Anchor
                        letterSpacing = (-1).sp
                    )
                )
                Text(
                    text = "Lightning-Fast Food & Grocery Delivery",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = YallaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Success / Verification Badge in Emerald Green (#10B981)
                Surface(
                    color = YallaGreenLight,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, YallaGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = YallaGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "100% Secure & Instant OTP Login",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = YallaGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Error Display Card
            AnimatedVisibility(
                visible = uiState.authError != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = Color(0xFFFFF0F0),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red
                        )
                        Text(
                            text = uiState.authError ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (!uiState.otpSent) {
                // STAGE 1: PHONE NUMBER INPUT STAGE
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = YallaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Enter Mobile Number",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = YallaNavy // Deep Navy Blue Title
                            )
                        )

                        // Direct High-Contrast Phone Input Field
                        OutlinedTextField(
                            value = phoneNumberInput,
                            onValueChange = { input ->
                                if (input.length <= 10 && input.all { it.isDigit() }) {
                                    phoneNumberInput = input
                                }
                            },
                            textStyle = LocalTextStyle.current.copy(
                                color = YallaCharcoal, // Deep Charcoal #1E293B for full visibility
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 14.dp, end = 10.dp)
                                ) {
                                    Text(
                                        text = "🇮🇳 +91",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = YallaNavy
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(22.dp)
                                            .background(YallaBorder)
                                    )
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter 10-digit mobile number",
                                    color = YallaTextMuted, // #94A3B8
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YallaCharcoal,       // #1E293B
                                unfocusedTextColor = YallaCharcoal,     // #1E293B
                                focusedContainerColor = YallaInputBg,   // #F8FAFC
                                unfocusedContainerColor = YallaInputBg, // #F8FAFC
                                disabledContainerColor = YallaInputBg,
                                cursorColor = YallaOrange,              // Royal Orange #FF5E00
                                focusedBorderColor = YallaOrange,        // Royal Orange #FF5E00
                                unfocusedBorderColor = YallaBorder,     // #E2E8F0
                                focusedPlaceholderColor = YallaTextMuted,
                                unfocusedPlaceholderColor = YallaTextMuted,
                                focusedLeadingIconColor = YallaNavy,
                                unfocusedLeadingIconColor = YallaNavy
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Royal Orange CTA Button
                        Button(
                            onClick = {
                                if (activity != null) {
                                    firebaseViewModel.sendOtp(phoneNumberInput, activity)
                                }
                            },
                            enabled = phoneNumberInput.length == 10 && !uiState.isSendingOtp,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YallaOrange,
                                disabledContainerColor = YallaOrange.copy(alpha = 0.4f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 1.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (uiState.isSendingOtp) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending OTP...", color = Color.White)
                            } else {
                                Text(
                                    text = "Send Verification OTP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // STAGE 2: 6-DIGIT OTP VERIFICATION STAGE
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = YallaSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { firebaseViewModel.resetOtpState() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = YallaOrange
                                )
                            }
                            Text(
                                text = "Enter 6-Digit OTP Code",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YallaNavy
                                )
                            )
                        }

                        Text(
                            text = "OTP sent to +91 ${uiState.formattedPhoneNumber}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = YallaTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )

                        // High Contrast OTP Code Input
                        OutlinedTextField(
                            value = otpCodeInput,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    otpCodeInput = input
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "• • • • • •",
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center,
                                    color = YallaTextMuted
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                letterSpacing = 6.sp,
                                color = YallaCharcoal // Deep Charcoal #1E293B
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = YallaCharcoal,       // #1E293B
                                unfocusedTextColor = YallaCharcoal,     // #1E293B
                                focusedContainerColor = YallaInputBg,   // #F8FAFC
                                unfocusedContainerColor = YallaInputBg, // #F8FAFC
                                disabledContainerColor = YallaInputBg,
                                cursorColor = YallaOrange,              // Royal Orange #FF5E00
                                focusedBorderColor = YallaOrange,        // Royal Orange #FF5E00
                                unfocusedBorderColor = YallaBorder,     // #E2E8F0
                                focusedPlaceholderColor = YallaTextMuted,
                                unfocusedPlaceholderColor = YallaTextMuted
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                firebaseViewModel.verifyOtp(otpCodeInput) {
                                    onLoginSuccess()
                                }
                            },
                            enabled = otpCodeInput.length == 6 && !uiState.isVerifyingOtp,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YallaOrange,
                                disabledContainerColor = YallaOrange.copy(alpha = 0.4f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 1.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (uiState.isVerifyingOtp) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying...", color = Color.White)
                            } else {
                                Text(
                                    text = "Verify & Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                if (activity != null) {
                                    firebaseViewModel.sendOtp(phoneNumberInput, activity)
                                }
                            }
                        ) {
                            Text("Resend OTP Code", color = YallaOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Demo Guest Skip button
            TextButton(
                onClick = {
                    firebaseViewModel.bypassLoginForDemo()
                    onLoginSuccess()
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "⚡", fontSize = 14.sp)
                    Text(
                        text = "Continue as Guest for Demo",
                        color = YallaTextSecondary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
