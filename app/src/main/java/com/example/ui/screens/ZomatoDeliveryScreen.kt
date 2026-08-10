package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FoodMenuItem
import com.example.data.models.RestaurantItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArchitectureViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZomatoDeliveryScreen(
    viewModel: ArchitectureViewModel,
    uiState: UiState,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleRestaurants = viewModel.getSampleRestaurants()
    val categories = viewModel.getSampleCategories()
    val offerBanners = viewModel.getSampleOffers()

    // Filter restaurants based on category and search
    val filteredRestaurants = remember(uiState.selectedCategory, uiState.searchQuery) {
        sampleRestaurants.filter { rest ->
            val matchesCategory = if (uiState.selectedCategory == "All") true
            else rest.cuisine.contains(uiState.selectedCategory, ignoreCase = true) ||
                    rest.name.contains(uiState.selectedCategory, ignoreCase = true) ||
                    rest.menu.any { it.name.contains(uiState.selectedCategory, ignoreCase = true) }

            val matchesSearch = if (uiState.searchQuery.isBlank()) true
            else rest.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    rest.cuisine.contains(uiState.searchQuery, ignoreCase = true) ||
                    rest.menu.any { it.name.contains(uiState.searchQuery, ignoreCase = true) }

            matchesCategory && matchesSearch
        }
    }

    val cartTotal = uiState.cart.fold(0.0) { acc, c -> acc + (c.item.price * c.quantity) }
    val cartCount = uiState.cart.sumOf { it.quantity }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        if (uiState.isViewingRestaurantMenu && uiState.selectedRestaurant != null) {
            // Restaurant Menu View
            RestaurantMenuView(
                restaurant = uiState.selectedRestaurant,
                cart = uiState.cart,
                onBack = { viewModel.closeRestaurantMenu() },
                onAdd = { viewModel.addToCart(it) },
                onRemove = { viewModel.removeFromCart(it) }
            )
        } else {
            // Main Delivery Feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Top Header: Delivery Location + Profile + Search Bar
                item {
                    ZomatoHeader(
                        location = uiState.deliveryLocation,
                        searchQuery = uiState.searchQuery,
                        onLocationClick = { /* Handled via location dialog */ },
                        onProfileClick = { viewModel.selectTab(com.example.ui.viewmodel.NavigationTab.ZOMATO_PROFILE) },
                        onSearchChange = { viewModel.setSearchQuery(it) }
                    )
                }

                // Horizontal Categories Rail
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "WHAT'S ON YOUR MIND?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = ZomatoTextSecondary,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    ZomatoCategoriesRail(
                        categories = categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelect = { viewModel.setSelectedCategory(it) }
                    )
                }

                // Offer Banners Carousel
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ZomatoOfferCarousel(
                        offers = offerBanners,
                        onClaimOffer = {
                            viewModel.setSelectedCategory("All")
                        }
                    )
                }

                // Section Header: Restaurant Feed
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALL RESTAURANTS NEAR YOU",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = ZomatoTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "${filteredRestaurants.size} restaurants",
                            style = MaterialTheme.typography.bodySmall.copy(color = ZomatoTextSecondary)
                        )
                    }
                }

                // Restaurant Cards List
                items(filteredRestaurants) { restaurant ->
                    PaddingValues(horizontal = 16.dp, vertical = 8.dp).let { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            ZomatoRestaurantCard(
                                restaurant = restaurant,
                                onClick = { viewModel.selectRestaurant(restaurant) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Cart Bar (Appears when cart has items)
        if (cartCount > 0) {
            ZomatoCartBar(
                cartItemsCount = cartCount,
                cartTotal = cartTotal,
                onViewCartClick = onViewCartClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Zomato Restaurant Menu Screen View
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantMenuView(
    restaurant: RestaurantItem,
    cart: List<com.example.ui.viewmodel.CartItem>,
    onBack: () -> Unit,
    onAdd: (FoodMenuItem) -> Unit,
    onRemove: (FoodMenuItem) -> Unit
) {
    var pureVegOnly by remember { mutableStateOf(false) }
    var bestsellersOnly by remember { mutableStateOf(false) }

    val filteredMenu = remember(pureVegOnly, bestsellersOnly) {
        restaurant.menu.filter { item ->
            (!pureVegOnly || item.isVeg) && (!bestsellersOnly || item.isBestseller)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Restaurant Header & Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZomatoLightBg)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ZomatoTextPrimary
                        )
                    }

                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Search, contentDescription = "Search menu")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = ZomatoTextPrimary
                    )
                )

                Text(
                    text = restaurant.cuisine,
                    style = MaterialTheme.typography.bodyMedium.copy(color = ZomatoTextSecondary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZomatoGreen
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${restaurant.rating} ★",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text("•", color = Color.Gray)
                    Text("${restaurant.deliveryTimeMins} mins", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("•", color = Color.Gray)
                    Text(restaurant.distanceKm, color = ZomatoTextSecondary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Offer Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ZomatoRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, ZomatoRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = ZomatoRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = restaurant.offerTag,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ZomatoRed
                            )
                        )
                    }
                }
            }
        }

        // Filter Toggles Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = pureVegOnly,
                    onClick = { pureVegOnly = !pureVegOnly },
                    label = { Text("Pure Veg") },
                    leadingIcon = { VegNonVegIcon(isVeg = true) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ZomatoVegGreen.copy(alpha = 0.15f))
                )

                FilterChip(
                    selected = bestsellersOnly,
                    onClick = { bestsellersOnly = !bestsellersOnly },
                    label = { Text("Bestsellers") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Menu Section Title
        item {
            Text(
                text = "RECOMMENDED DISHES (${filteredMenu.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = ZomatoTextPrimary,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Food Items List
        items(filteredMenu) { item ->
            val qty = cart.find { it.item.id == item.id }?.quantity ?: 0
            PaddingValues(horizontal = 16.dp, vertical = 6.dp).let { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    ZomatoFoodItemCard(
                        item = item,
                        quantityInCart = qty,
                        onAddClick = { onAdd(item) },
                        onRemoveClick = { onRemove(item) }
                    )
                }
            }
        }
    }
}
