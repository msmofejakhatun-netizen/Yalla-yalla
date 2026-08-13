package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.firebase.*
import com.example.data.models.FoodCategory
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.YallaFirebaseViewModel

@Composable
fun YallaHomeScreen(
    viewModel: ArchitectureViewModel,
    firebaseViewModel: YallaFirebaseViewModel,
    uiState: UiState,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val firebaseUiState by firebaseViewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            firebaseViewModel.fetchLiveLocation(context)
        }
    }

    val requestLocationAction = {
        val fineCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineCheck || coarseCheck) {
            firebaseViewModel.fetchLiveLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // AUTO PERMISSION PROMPT ON APP LAUNCH:
    // As soon as Home Screen loads, automatically check for location permissions or show permission dialog box.
    LaunchedEffect(Unit) {
        requestLocationAction()
    }

    val categories = listOf(
        FoodCategory("1", "All", "🍽️", null),
        FoodCategory("2", "Biryani", "🍲", "HOT"),
        FoodCategory("3", "Pizza", "🍕", "50% OFF"),
        FoodCategory("4", "Burger", "🍔", "POPULAR"),
        FoodCategory("5", "North Indian", "🥘", null),
        FoodCategory("6", "Healthy", "🥗", "FRESH"),
        FoodCategory("7", "Desserts", "🍰", null)
    )

    Box(modifier = modifier.fillMaxSize().background(YallaLightBg)) {
        if (firebaseUiState.isViewingRestaurantMenu && firebaseUiState.selectedRestaurant != null) {
            // Restaurant Menu Detailed View (Firestore Synced)
            YallaRestaurantMenuDetailView(
                restaurant = firebaseUiState.selectedRestaurant!!,
                firebaseViewModel = firebaseViewModel,
                dishes = firebaseUiState.filteredMenuItems,
                cart = firebaseUiState.cart,
                onBack = { firebaseViewModel.closeRestaurantMenu() },
                onViewCartClick = onViewCartClick
            )
        } else {
            // Main Home Feed (100% Firestore Synced)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Dynamic Header with Firestore Address & Live GPS Status
                item {
                    YallaHeader(
                        appName = "Yalla Yalla",
                        location = firebaseUiState.deliveryLocation,
                        yallaCoins = uiState.yallaCoinsBalance,
                        onLocationClick = { firebaseViewModel.toggleLocationPicker(true) },
                        onProfileClick = { viewModel.selectTab(NavigationTab.YALLA_PROFILE) },
                        onCoinsClick = { viewModel.selectTab(NavigationTab.YALLA_COINS) }
                    )
                }

                // 2. Search Bar
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        YallaSearchBar(
                            searchQuery = firebaseUiState.searchQuery,
                            onSearchChange = { firebaseViewModel.setSearchQuery(it) }
                        )
                    }
                }

                // 3. Dynamic Category Rail
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "EXPLORE CATEGORIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = YallaTextSecondary,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        YallaCategoryRail(
                            categories = categories,
                            selectedCategory = firebaseUiState.selectedCategory,
                            onCategorySelect = { firebaseViewModel.setCategoryFilter(it) }
                        )
                    }
                }

                // 4. Firestore Promotions & Deals Carousel
                item {
                    if (firebaseUiState.promotions.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FIRESTORE PROMOTIONS & DEALS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = YallaTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Active Code: ${firebaseUiState.appliedPromoCode ?: "None"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = YallaOrange
                                    )
                                )
                            }

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(firebaseUiState.promotions) { promo ->
                                    FirestorePromoCard(
                                        promo = promo,
                                        isApplied = firebaseUiState.appliedPromoCode == promo.code,
                                        onApply = { firebaseViewModel.applyPromoCode(promo.code) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Loading Indicator / Empty State
                if (firebaseUiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = YallaOrange)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Connecting to Firestore...", color = YallaTextSecondary)
                            }
                        }
                    }
                }

                // 6. Section Header: Restaurants
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "RESTAURANTS NEAR YOU",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = YallaTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Order from top cloud kitchens and local favorites",
                            style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                        )
                    }
                }

                // 7. Restaurant Cards Feed or Empty State
                if (firebaseUiState.filteredRestaurants.isEmpty() && !firebaseUiState.isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, YallaBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = YallaTextSecondary
                                )
                                Text(
                                    text = "No restaurants available right now",
                                    fontWeight = FontWeight.Bold,
                                    color = YallaTextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Restaurants added in Firebase Admin Portal will appear here in real time.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                                )
                            }
                        }
                    }
                } else {
                    items(firebaseUiState.filteredRestaurants) { rest ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            FirestoreRestaurantCard(
                                restaurant = rest,
                                onClick = { firebaseViewModel.openRestaurantMenu(rest) }
                            )
                        }
                    }
                }

                // 8. Dynamic Menu & Dish Feed or Empty State
                item {
                    Text(
                        text = "POPULAR DISHES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = YallaTextSecondary,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                }

                if (firebaseUiState.filteredMenuItems.isEmpty() && !firebaseUiState.isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, YallaBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = YallaTextSecondary
                                )
                                Text(
                                    text = "No dishes available right now",
                                    fontWeight = FontWeight.Bold,
                                    color = YallaTextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Dishes added to Firestore will sync and display here instantly.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                                )
                            }
                        }
                    }
                } else {
                    items(firebaseUiState.filteredMenuItems) { dish ->
                        val quantity = firebaseUiState.cart.find { it.itemId == dish.id }?.quantity ?: 0
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            FirestoreDishCard(
                                dish = dish,
                                quantityInCart = quantity,
                                onAddClick = { firebaseViewModel.addToCart(dish) },
                                onRemoveClick = { firebaseViewModel.updateCartQuantity(dish.id, -1) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Bottom Cart Bar with exact Firestore cart total cleanly floating above bottom nav bar
        if (firebaseUiState.cartTotalCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                YallaCartBar(
                    cartItemsCount = firebaseUiState.cartTotalCount,
                    cartTotal = firebaseUiState.totalAmount,
                    onGoToCartClick = onViewCartClick
                )
            }
        }

        // Location Picker Bottom Sheet
        if (firebaseUiState.isLocationPickerOpen) {
            YallaLocationBottomSheet(
                firebaseViewModel = firebaseViewModel,
                uiState = firebaseUiState,
                onRequestLocation = requestLocationAction,
                onDismiss = { firebaseViewModel.toggleLocationPicker(false) }
            )
        }
    }
}

/**
 * Dynamic Firestore Promotion Card Component
 */
@Composable
private fun FirestorePromoCard(
    promo: FirestorePromotionItem,
    isApplied: Boolean,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(YallaOrange, YallaOrangeDark)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "CODE: ${promo.code}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = promo.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    Text(
                        text = promo.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) YallaGreen else Color.White,
                        contentColor = if (isApplied) Color.White else YallaOrange
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (isApplied) "✓ Applied" else "Apply Code",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

/**
 * Dynamic Firestore Restaurant Card Component
 */
@Composable
private fun FirestoreRestaurantCard(
    restaurant: FirestoreRestaurantItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, YallaBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (restaurant.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = restaurant.imageUrl,
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(YallaOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡ ${restaurant.name}", fontWeight = FontWeight.Bold, color = YallaOrange)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 80f
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 20.dp),
                    color = YallaGreen,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        Text(
                            text = "Yalla Verified",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YallaTextPrimary,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = YallaGreen
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${restaurant.rating}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.cuisine,
                    style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = YallaOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = restaurant.formattedDistance.ifBlank { "${restaurant.deliveryTimeMins} mins" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = YallaTextPrimary
                        )
                    )
                    Text("•", color = Color.Gray)
                    Text(
                        text = "₹${restaurant.priceForTwo.toInt()} for two",
                        style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                    )
                }
            }
        }
    }
}

/**
 * Dynamic Firestore Dish Card Component
 */
@Composable
private fun FirestoreDishCard(
    dish: FirestoreDishItem,
    quantityInCart: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, YallaBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegNonVegIcon(isVeg = dish.isVeg)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = YallaGoldLight
                    ) {
                        Text(
                            text = dish.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = YallaOrangeDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YallaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "₹${dish.price.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = YallaTextPrimary
                    )
                )

                if (dish.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dish.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = YallaTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier.size(width = 105.dp, height = 105.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = YallaLightBg,
                    modifier = Modifier.fillMaxSize().padding(bottom = 12.dp)
                ) {
                    if (dish.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = dish.imageUrl,
                            contentDescription = dish.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(YallaGoldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (dish.isVeg) "🥗" else "🍗", fontSize = 32.sp)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, YallaOrange),
                    modifier = Modifier.height(34.dp).width(88.dp)
                ) {
                    if (quantityInCart == 0) {
                        Box(
                            modifier = Modifier.fillMaxSize().clickable { onAddClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ADD +",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = YallaOrange
                                )
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight().clickable { onRemoveClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, color = YallaOrange, fontSize = 18.sp)
                            }
                            Text(
                                text = "$quantityInCart",
                                fontWeight = FontWeight.ExtraBold,
                                color = YallaOrange,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight().clickable { onAddClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, color = YallaOrange, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YallaRestaurantMenuDetailView(
    restaurant: FirestoreRestaurantItem,
    firebaseViewModel: YallaFirebaseViewModel,
    dishes: List<FirestoreDishItem>,
    cart: List<FirestoreCartItem>,
    onBack: () -> Unit,
    onViewCartClick: () -> Unit
) {
    val totalCount = cart.sumOf { it.quantity }
    val totalAmount = cart.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = restaurant.cuisine,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YallaGreen,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "✓ Yalla Verified",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(YallaLightBg)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, YallaBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = YallaGreen) {
                                    Text(
                                        text = "${restaurant.rating} ★",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text("• ${restaurant.deliveryTimeMins} mins delivery", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Text("₹${restaurant.priceForTwo.toInt()} for two", color = Color.Gray, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📍 ${restaurant.address}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }
                }
            }

            item {
                Text(
                    text = "MENU ITEMS (REALTIME FIRESTORE)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = YallaTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(dishes) { dish ->
                val quantityInCart = cart.find { it.itemId == dish.id }?.quantity ?: 0
                FirestoreDishCard(
                    dish = dish,
                    quantityInCart = quantityInCart,
                    onAddClick = { firebaseViewModel.addToCart(dish) },
                    onRemoveClick = { firebaseViewModel.updateCartQuantity(dish.id, -1) }
                )
            }
        }
    }
}
