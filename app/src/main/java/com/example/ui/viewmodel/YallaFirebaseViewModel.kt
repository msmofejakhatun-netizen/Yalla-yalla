package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FirebaseUiState(
    val promotions: List<FirestorePromotionItem> = emptyList(),
    val restaurants: List<FirestoreRestaurantItem> = emptyList(),
    val filteredRestaurants: List<FirestoreRestaurantItem> = emptyList(),
    val menuItems: List<FirestoreDishItem> = emptyList(),
    val filteredMenuItems: List<FirestoreDishItem> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val deliveryLocation: String = "7th Block, Koramangala, Bengaluru (Live GPS)",
    val selectedRestaurant: FirestoreRestaurantItem? = null,
    val isViewingRestaurantMenu: Boolean = false,
    val cart: List<FirestoreCartItem> = emptyList(),
    val activeOrder: FirestoreOrderItem? = null,
    val appliedPromoCode: String? = null,
    val appliedDiscountAmount: Double = 0.0,
    val isLoading: Boolean = true,
    val isPlacingOrder: Boolean = false,
    val isFirebaseConnected: Boolean = true,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val subtotal: Double
        get() = cart.sumOf { it.price * it.quantity }

    val deliveryFee: Double
        get() = if (cart.isEmpty()) 0.0 else 30.0

    val platformFee: Double
        get() = if (cart.isEmpty()) 0.0 else 10.0

    val totalAmount: Double
        get() = (subtotal + deliveryFee + platformFee - appliedDiscountAmount).coerceAtLeast(0.0)

    val cartTotalCount: Int
        get() = cart.sumOf { it.quantity }
}

class YallaFirebaseViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepo = FirebaseRepository()

    private val _uiState = MutableStateFlow(FirebaseUiState())
    val uiState: StateFlow<FirebaseUiState> = _uiState.asStateFlow()

    init {
        initRealtimeFirestoreListeners()
    }

    private fun initRealtimeFirestoreListeners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Realtime Promotions
            launch {
                firebaseRepo.getRealtimePromotions()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Promotions error: ${e.message}") }
                    }
                    .collect { promos ->
                        _uiState.update { state ->
                            val updated = if (promos.isEmpty()) firebaseRepo.getFallbackSamplePromotions() else promos
                            state.copy(promotions = updated)
                        }
                    }
            }

            // 2. Realtime Restaurants
            launch {
                firebaseRepo.getRealtimeRestaurants()
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Restaurants error: ${e.message}") }
                    }
                    .collect { rests ->
                        _uiState.update { state ->
                            val updatedRests = if (rests.isEmpty()) firebaseRepo.getFallbackSampleRestaurants() else rests
                            state.copy(
                                restaurants = updatedRests,
                                filteredRestaurants = filterRestaurants(updatedRests, state.selectedCategory, state.searchQuery),
                                isLoading = false
                            )
                        }
                    }
            }

            // 3. Realtime Menu Items
            launch {
                firebaseRepo.getRealtimeMenu("rest_yalla_1")
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = "Menu error: ${e.message}") }
                    }
                    .collect { dishes ->
                        _uiState.update { state ->
                            val updatedDishes = if (dishes.isEmpty()) firebaseRepo.getFallbackSampleMenu() else dishes
                            state.copy(
                                menuItems = updatedDishes,
                                filteredMenuItems = filterDishes(updatedDishes, state.selectedCategory, state.searchQuery)
                            )
                        }
                    }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredRestaurants = filterRestaurants(state.restaurants, state.selectedCategory, query),
                filteredMenuItems = filterDishes(state.menuItems, state.selectedCategory, query)
            )
        }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                filteredRestaurants = filterRestaurants(state.restaurants, category, state.searchQuery),
                filteredMenuItems = filterDishes(state.menuItems, category, state.searchQuery)
            )
        }
    }

    private fun filterRestaurants(
        rests: List<FirestoreRestaurantItem>,
        category: String,
        query: String
    ): List<FirestoreRestaurantItem> {
        return rests.filter { rest ->
            val matchesCat = if (category == "All" || category.isEmpty()) {
                true
            } else if (category == "Yalla Specials") {
                rest.isPromoted || rest.rating >= 4.8
            } else {
                rest.cuisine.contains(category, ignoreCase = true)
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                rest.name.contains(query, ignoreCase = true) ||
                        rest.cuisine.contains(query, ignoreCase = true)
            }

            matchesCat && matchesQuery
        }
    }

    private fun filterDishes(
        dishes: List<FirestoreDishItem>,
        category: String,
        query: String
    ): List<FirestoreDishItem> {
        return dishes.filter { dish ->
            val matchesCat = if (category == "All" || category.isEmpty()) {
                true
            } else {
                dish.category.equals(category, ignoreCase = true)
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                dish.name.contains(query, ignoreCase = true) ||
                        dish.category.contains(query, ignoreCase = true) ||
                        dish.description.contains(query, ignoreCase = true)
            }

            matchesCat && matchesQuery
        }
    }

    fun openRestaurantMenu(restaurant: FirestoreRestaurantItem) {
        _uiState.update { it.copy(selectedRestaurant = restaurant, isViewingRestaurantMenu = true) }
    }

    fun closeRestaurantMenu() {
        _uiState.update { it.copy(isViewingRestaurantMenu = false) }
    }

    fun addToCart(dish: FirestoreDishItem) {
        _uiState.update { state ->
            val existing = state.cart.find { it.itemId == dish.id }
            val updatedCart = if (existing != null) {
                state.cart.map {
                    if (it.itemId == dish.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.cart + FirestoreCartItem(
                    itemId = dish.id,
                    name = dish.name,
                    price = dish.price,
                    quantity = 1,
                    isVeg = dish.isVeg,
                    imageUrl = dish.imageUrl
                )
            }
            state.copy(cart = updatedCart, statusMessage = "Added ${dish.name} to Cart")
        }
    }

    fun updateCartQuantity(itemId: String, delta: Int) {
        _uiState.update { state ->
            val updatedCart = state.cart.mapNotNull { item ->
                if (item.itemId == itemId) {
                    val newQty = item.quantity + delta
                    if (newQty > 0) item.copy(quantity = newQty) else null
                } else item
            }
            state.copy(cart = updatedCart)
        }
    }

    fun applyPromoCode(code: String) {
        val promo = _uiState.value.promotions.find { it.code.equals(code, ignoreCase = true) }
        if (promo != null) {
            val discount = if (promo.discountAmount > 0) promo.discountAmount else (_uiState.value.subtotal * promo.discountPercent / 100.0)
            _uiState.update { 
                it.copy(
                    appliedPromoCode = promo.code,
                    appliedDiscountAmount = discount,
                    statusMessage = "🎉 Promo Code ${promo.code} Applied! Saved ₹${discount.toInt()}"
                ) 
            }
        } else {
            _uiState.update { it.copy(errorMessage = "Invalid Promo Code: $code") }
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = emptyList(), appliedPromoCode = null, appliedDiscountAmount = 0.0) }
    }

    /**
     * Pushes order to Firestore `/orders`
     */
    fun placeOrderToFirestore(onSuccess: (String) -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState.cart.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Cart is empty. Add dishes before placing order.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null) }

            val restName = currentState.selectedRestaurant?.name ?: "Yalla Yalla Central Kitchen"
            val restId = currentState.selectedRestaurant?.id ?: "rest_yalla_1"

            val newOrder = FirestoreOrderItem(
                orderId = "ord_yf_" + System.currentTimeMillis().toString().takeLast(6),
                customerId = "cust_yalla_001",
                restaurantId = restId,
                restaurantName = restName,
                items = currentState.cart,
                subtotal = currentState.subtotal,
                deliveryFee = currentState.deliveryFee,
                totalAmount = currentState.totalAmount,
                paymentStatus = "PAID",
                orderStatus = "PAID",
                createdAt = System.currentTimeMillis()
            )

            val result = firebaseRepo.placeOrder(newOrder)
            if (result.isSuccess) {
                val orderId = result.getOrNull() ?: newOrder.orderId
                _uiState.update { 
                    it.copy(
                        isPlacingOrder = false,
                        cart = emptyList(),
                        appliedPromoCode = null,
                        appliedDiscountAmount = 0.0,
                        activeOrder = newOrder.copy(orderId = orderId),
                        statusMessage = "🎉 Order #$orderId placed directly in Firestore!"
                    ) 
                }
                observeActiveOrder(orderId)
                onSuccess(orderId)
            } else {
                _uiState.update { 
                    it.copy(
                        isPlacingOrder = false,
                        cart = emptyList(),
                        activeOrder = newOrder,
                        statusMessage = "Order created locally (Firestore sync pending)"
                    ) 
                }
                onSuccess(newOrder.orderId)
            }
        }
    }

    fun observeActiveOrder(orderId: String) {
        viewModelScope.launch {
            firebaseRepo.observeOrder(orderId).collect { updatedOrder ->
                if (updatedOrder != null) {
                    _uiState.update { it.copy(activeOrder = updatedOrder) }
                }
            }
        }
    }

    fun advanceOrderStatusInFirestore(nextStatus: String) {
        val currentOrder = _uiState.value.activeOrder ?: return
        viewModelScope.launch {
            val success = firebaseRepo.updateOrderStatus(currentOrder.orderId, nextStatus)
            if (!success) {
                _uiState.update { state ->
                    state.copy(activeOrder = currentOrder.copy(orderStatus = nextStatus))
                }
            }
        }
    }

    fun seedFirestoreData() {
        viewModelScope.launch {
            firebaseRepo.seedSampleMenuToFirestore("rest_yalla_1")
            firebaseRepo.seedSamplePromotionsToFirestore()
            firebaseRepo.seedSampleRestaurantsToFirestore()
            _uiState.update { it.copy(statusMessage = "All sample collections seeded to Firestore!") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
