package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FirebaseUiState
import com.example.ui.viewmodel.YallaFirebaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YallaLocationBottomSheet(
    firebaseViewModel: YallaFirebaseViewModel,
    uiState: FirebaseUiState,
    onRequestLocation: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customAddressInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Location",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = YallaTextPrimary
                        )
                    )
                    Text(
                        text = "Live GPS & Saved Addresses",
                        style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Current Active Location Card
            Surface(
                color = YallaOrange.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, YallaOrange.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Current Location",
                        tint = YallaOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE LOCATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = YallaOrange
                            )
                        )
                        Text(
                            text = uiState.deliveryLocation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = YallaTextPrimary
                            )
                        )
                    }
                }
            }

            // Button to Re-trigger Live GPS Fetch
            Button(
                onClick = {
                    if (onRequestLocation != null) {
                        onRequestLocation()
                    } else {
                        firebaseViewModel.fetchLiveLocation(context)
                    }
                },
                enabled = !uiState.isFetchingLocation,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YallaOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isFetchingLocation) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetching Live GPS...")
                } else {
                    Icon(imageVector = Icons.Default.GpsFixed, contentDescription = "GPS")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Current GPS Location", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()

            // Manual Address Entry
            OutlinedTextField(
                value = customAddressInput,
                onValueChange = { customAddressInput = it },
                placeholder = { Text("Search or type custom address...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (customAddressInput.isNotEmpty()) {
                        IconButton(onClick = {
                            firebaseViewModel.selectAddress(customAddressInput)
                            onDismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Apply",
                                tint = YallaOrange
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YallaOrange,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Saved Addresses Header
            Text(
                text = "SAVED & POPULAR LOCATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = YallaTextSecondary
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                items(uiState.savedAddresses) { address ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YallaLightBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                firebaseViewModel.selectAddress(address)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location",
                                tint = Color.Gray
                            )
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = YallaTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.deliveryLocation.contains(address, ignoreCase = true)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = YallaOrange
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
