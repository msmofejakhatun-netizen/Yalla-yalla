package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.UiState

@Composable
fun YallaHomeScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    var selectedCategory by remember { mutableStateOf(uiState.selectedCategory) }

    val allRestaurants = viewModel.getSampleRestaurants()
    val categories = viewModel.getSampleCategories()
    val offers = viewModel.getSampleOffers()

    val filteredRestaurants = remember(searchQuery, selectedCategory, allRestaurants) {
        allRestaurants.filter { restaurant ->
            val matchesCategory = if (selectedCategory == "All" || selectedCategory.isEmpty()) {
                true
            } else if (selectedCategory == "Yalla Specials") {
                restaurant.isPromoted || restaurant.rating >= 4.8
            } else {
                restaurant.cuisine.contains(selectedCategory, ignoreCase = true) ||
                        restaurant.menu.any { it.name.contains(selectedCategory, ignoreCase = true) }
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                restaurant.name.contains(searchQuery, ignoreCase = true) ||
                        restaurant.cuisine.contains(searchQuery, ignoreCase = true) ||
                        restaurant.menu.any { it.name.contains(searchQuery, ignoreCase = true) }
            }

            matchesCategory && matchesSearch
        }
    }

    Box(modifier = modifier.fillMaxSize().background(YallaLightBg)) {
        if (uiState.isViewingRestaurantMenu && uiState.selectedRestaurant != null) {
            // Restaurant Menu Detailed View
            YallaRestaurantMenuDetailView(
                restaurant = uiState.selectedRestaurant,
                viewModel = viewModel,
                uiState = uiState,
                onBack = { viewModel.closeRestaurantMenu() }
            )
        } else {
            // Home Main Feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    YallaHeader(
                        appName = "Yalla Yalla",
                        location = uiState.deliveryLocation,
                        yallaCoins = uiState.yallaCoinsBalance,
                        onLocationClick = { },
                        onProfileClick = { viewModel.selectTab(NavigationTab.YALLA_PROFILE) },
                        onCoinsClick = { viewModel.selectTab(NavigationTab.YALLA_COINS) }
                    )
                }

                // Search Bar
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        YallaSearchBar(
                            searchQuery = searchQuery,
                            onSearchChange = {
                                searchQuery = it
                                viewModel.setSearchQuery(it)
                            }
                        )
                    }
                }

                // Horizontal Categories Rail
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
                            selectedCategory = selectedCategory,
                            onCategorySelect = {
                                selectedCategory = it
                                viewModel.setCategory(it)
                            }
                        )
                    }
                }

                // Offers & Cashback Banner Carousel
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXCLUSIVE YALLA DEALS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = YallaTextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YallaOrange
                                )
                            )
                        }
                        ZomatoOfferCarousel(
                            offers = offers,
                            onClaimOffer = { offer: OfferBanner ->
                                viewModel.applyOfferCode(offer.discountCode)
                            }
                        )
                    }
                }

                // Section Header: Yalla Verified Restaurants
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "YALLA VERIFIED RESTAURANTS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = YallaTextPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = YallaGreenLight
                                ) {
                                    Text(
                                        text = "100% FRESH",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = YallaGreen,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Top-rated kitchens with 100% hygiene & superfast delivery",
                                style = MaterialTheme.typography.bodySmall.copy(color = YallaTextSecondary)
                            )
                        }
                    }
                }

                // Restaurant Cards List
                items(filteredRestaurants) { restaurant ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        YallaRestaurantCard(
                            restaurant = restaurant,
                            onClick = { viewModel.openRestaurantMenu(restaurant) }
                        )
                    }
                }
            }
        }

        // Floating Bottom Cart Bar with "Go to Cart" button
        val cartCount = uiState.cart.fold(0) { acc, item -> acc + item.quantity }
        val cartSubtotal = uiState.cart.fold(0.0) { acc, item -> acc + (item.item.price * item.quantity) }

        if (cartCount > 0 && !uiState.isViewingRestaurantMenu) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            ) {
                YallaCartBar(
                    cartItemsCount = cartCount,
                    cartTotal = cartSubtotal,
                    onGoToCartClick = onViewCartClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YallaRestaurantMenuDetailView(
    restaurant: RestaurantItem,
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    onBack: () -> Unit
) {
    val cartCount = uiState.cart.fold(0) { acc, item -> acc + item.quantity }
    val cartSubtotal = uiState.cart.fold(0.0) { acc, item -> acc + (item.item.price * item.quantity) }

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
        },
        bottomBar = {
            if (cartCount > 0) {
                Surface(
                    color = YallaOrange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$cartCount ITEMS • ₹${cartSubtotal.toInt()}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = "Extra discounts with Yalla Coins applied",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                            )
                        }

                        Button(
                            onClick = { viewModel.selectTab(NavigationTab.YALLA_HOME) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Go to Cart ➔", color = YallaOrange, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(YallaLightBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Restaurant Info Header
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Text("₹${restaurant.priceForTwo} for two", color = Color.Gray, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📍 ${restaurant.address}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }
                }
            }

            // Menu Section Title
            item {
                Text(
                    text = "RECOMMENDED MENU ITEMS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = YallaTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Food Items
            items(restaurant.menu) { item ->
                val quantityInCart = uiState.cart.find { it.item.id == item.id }?.quantity ?: 0
                ZomatoFoodItemCard(
                    item = item,
                    quantityInCart = quantityInCart,
                    onAddClick = { viewModel.addToCart(item) },
                    onRemoveClick = { viewModel.removeFromCart(item) }
                )
            }
        }
    }
}
