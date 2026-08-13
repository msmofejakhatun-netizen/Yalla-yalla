package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.*
import com.example.data.location.LocationHelper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class FirebaseUiState(
    val promotions: List<FirestorePromotionItem> = emptyList(),
    val restaurants: List<FirestoreRestaurantItem> = emptyList(),
    val filteredRestaurants: List<FirestoreRestaurantItem> = emptyList(),
    val menuItems: List<FirestoreDishItem> = emptyList(),
    val filteredMenuItems: List<FirestoreDishItem> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",

    // Location State
    val deliveryLocation: String = "📍 Indiranagar 100ft Rd, Bengaluru",
    val currentArea: String = "Indiranagar 100ft Rd",
    val currentCity: String = "Bengaluru",
    val currentPincode: String = "560038",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFetchingLocation: Boolean = false,
    val savedAddresses: List<String> = listOf(
        "Indiranagar 100ft Rd, Bengaluru",
        "7th Block, Koramangala, Bengaluru",
        "MG Road, Shanthala Nagar, Bengaluru",
        "HSR Layout Sector 1, Bengaluru"
    ),
    val isLocationPickerOpen: Boolean = false,

    // Auth State
    val isUserLoggedIn: Boolean = false,
    val userPhone: String = "",
    val userUid: String = "",
    val userRole: String = "CUSTOMER", // CUSTOMER or RESTAURANT_OWNER
    val selectedUserRole: String = "CUSTOMER",
    val inputPhoneNumber: String = "",
    val formattedPhoneNumber: String = "",
    val otpVerificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val isSendingOtp: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val otpSent: Boolean = false,
    val authError: String? = null,

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
        checkAuthState()
        initRealtimeFirestoreListeners()
    }

    /**
     * Session Persistence Check:
     * If user is already authenticated via FirebaseAuth, bypass login and navigate to Home Screen
     */
    fun checkAuthState() {
        val user = firebaseRepo.getCurrentUser()
        if (user != null && !user.isAnonymous) {
            val phone = user.phoneNumber ?: "+91 9876543210"
            val uid = user.uid
            Log.d("AuthDebug", "Existing session found for user UID: $uid, Phone: $phone")
            _uiState.update {
                it.copy(
                    isUserLoggedIn = true,
                    userUid = uid,
                    userPhone = phone,
                    userRole = "CUSTOMER"
                )
            }
        } else {
            Log.d("AuthDebug", "No authenticated phone user session found. Prompting login or anonymous guest.")
        }
    }

    /**
     * Set User Role (CUSTOMER or RESTAURANT_OWNER)
     */
    fun setUserRole(role: String) {
        _uiState.update { it.copy(selectedUserRole = role) }
    }

    /**
     * Step 1: Send OTP via Firebase PhoneAuthProvider
     */
    fun sendOtp(phoneNumber: String, activity: Activity, onCodeSentCallback: (() -> Unit)? = null) {
        if (phoneNumber.length < 10) {
            _uiState.update { it.copy(authError = "Please enter a valid 10-digit mobile number") }
            return
        }

        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        _uiState.update {
            it.copy(
                isSendingOtp = true,
                authError = null,
                inputPhoneNumber = phoneNumber,
                formattedPhoneNumber = formattedNumber
            )
        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d("AuthDebug", "Phone verification completed automatically")
                    _uiState.update { it.copy(isSendingOtp = false) }
                    signInWithCredential(credential, onCodeSentCallback)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("AuthDebug", "Verification failed: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            authError = "OTP Verification Failed: ${e.message}"
                        )
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d("AuthDebug", "Code sent to $formattedNumber, verificationId: $verificationId")
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            otpSent = true,
                            otpVerificationId = verificationId,
                            resendToken = token,
                            authError = null,
                            statusMessage = "OTP sent to $formattedNumber"
                        )
                    }
                    onCodeSentCallback?.invoke()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Step 2: Verify OTP 6-Digit Code
     */
    fun verifyOtp(code: String, onSuccessCallback: (() -> Unit)? = null) {
        val verificationId = _uiState.value.otpVerificationId
        if (verificationId == null) {
            _uiState.update { it.copy(authError = "Verification ID missing. Please request a new OTP.") }
            return
        }
        if (code.length < 6) {
            _uiState.update { it.copy(authError = "Please enter valid 6-digit OTP code") }
            return
        }

        _uiState.update { it.copy(isVerifyingOtp = true, authError = null) }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential, onSuccessCallback)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential, onSuccessCallback: (() -> Unit)? = null) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: "user_${System.currentTimeMillis()}"
                    val phone = user?.phoneNumber ?: _uiState.value.formattedPhoneNumber
                    val role = "CUSTOMER"

                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            isUserLoggedIn = true,
                            userUid = uid,
                            userPhone = phone,
                            userRole = role,
                            selectedUserRole = role,
                            authError = null,
                            statusMessage = "Logged in successfully as $phone!"
                        )
                    }

                    // Save user UID & phone number to `/users/{uid}` in Firestore
                    viewModelScope.launch {
                        firebaseRepo.saveUserProfile(uid, phone, "CUSTOMER")
                    }

                    onSuccessCallback?.invoke()
                } else {
                    Log.e("AuthDebug", "Sign in failed: ${task.exception?.message}")
                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            authError = "Invalid OTP code or auth failed: ${task.exception?.message}"
                        )
                    }
                }
            }
    }

    fun resetOtpState() {
        _uiState.update {
            it.copy(
                otpSent = false,
                otpVerificationId = null,
                authError = null,
                isSendingOtp = false,
                isVerifyingOtp = false
            )
        }
    }

    fun bypassLoginForDemo() {
        val demoUid = "demo_guest_${System.currentTimeMillis().toString().takeLast(4)}"
        val demoPhone = "+91 9876543210"
        _uiState.update {
            it.copy(
                isUserLoggedIn = true,
                userUid = demoUid,
                userPhone = demoPhone,
                userRole = "CUSTOMER",
                statusMessage = "Logged in as Guest Demo user"
            )
        }
        viewModelScope.launch {
            firebaseRepo.saveUserProfile(demoUid, demoPhone, "CUSTOMER")
        }
    }

    fun signOut() {
        firebaseRepo.signOut()
        _uiState.update {
            it.copy(
                isUserLoggedIn = false,
                userUid = "",
                userPhone = "",
                otpSent = false,
                otpVerificationId = null,
                inputPhoneNumber = "",
                formattedPhoneNumber = "",
                statusMessage = "Signed out"
            )
        }
    }

    /**
     * Zomato-Style Live GPS Location Fetching
     */
    fun fetchLiveLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true) }
            val locationHelper = LocationHelper(context)

            // Check if device Location/GPS service is enabled
            if (!locationHelper.isLocationEnabled()) {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        statusMessage = "Location/GPS is turned off. Opening Location Settings..."
                    )
                }
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("YallaFirebaseVM", "Failed to open location settings: ${e.message}")
                }
                return@launch
            }

            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                _uiState.update {
                    it.copy(
                        deliveryLocation = loc.fullAddress,
                        currentArea = loc.area,
                        currentCity = loc.city,
                        currentPincode = loc.pincode,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        isFetchingLocation = false,
                        statusMessage = "Updated Location: ${loc.area}"
                    )
                }
                updateRestaurantDistances(loc.latitude, loc.longitude)
            } else {
                _uiState.update {
                    it.copy(
                        deliveryLocation = "📍 Koramangala 7th Block, Bengaluru - 560034",
                        currentArea = "Koramangala 7th Block",
                        currentCity = "Bengaluru",
                        currentPincode = "560034",
                        isFetchingLocation = false,
                        statusMessage = "Using Default Location: Koramangala"
                    )
                }
            }
        }
    }

    fun updateRestaurantDistances(userLat: Double, userLng: Double) {
        val currentRests = _uiState.value.restaurants
        if (currentRests.isEmpty()) return

        val updated = currentRests.map { rest ->
            var calculatedDistKm = 2.5
            var formattedDistStr = "2.5 km • ${rest.deliveryTimeMins} mins"

            if (userLat != 0.0 && userLng != 0.0 && rest.latitude != 0.0 && rest.longitude != 0.0) {
                try {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userLat, userLng,
                        rest.latitude, rest.longitude,
                        results
                    )
                    val distMeters = results[0]
                    calculatedDistKm = kotlin.math.round(distMeters / 100.0) / 10.0
                    if (calculatedDistKm < 0.1) calculatedDistKm = 0.5
                    val estimatedMins = (rest.deliveryTimeMins + (calculatedDistKm * 3)).toInt()
                    formattedDistStr = "$calculatedDistKm km • $estimatedMins mins"
                } catch (e: Exception) {
                    calculatedDistKm = 2.5
                    formattedDistStr = "2.5 km • 25 mins"
                }
            }
            rest.copy(
                distanceKm = calculatedDistKm,
                formattedDistance = formattedDistStr
            )
        }

        _uiState.update { state ->
            state.copy(
                restaurants = updated,
                filteredRestaurants = filterRestaurants(updated, state.selectedCategory, state.searchQuery)
            )
        }
    }

    fun selectAddress(address: String) {
        val formattedAddress = if (address.startsWith("📍")) address else "📍 $address"
        _uiState.update {
            it.copy(
                deliveryLocation = formattedAddress,
                isLocationPickerOpen = false,
                statusMessage = "Selected Address: $address"
            )
        }
    }

    fun toggleLocationPicker(open: Boolean) {
        _uiState.update { it.copy(isLocationPickerOpen = open) }
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
                            state.copy(promotions = promos)
                        }
                    }
            }

            // 2. Realtime Restaurants
            launch {
                val currentLat = _uiState.value.latitude ?: 0.0
                val currentLng = _uiState.value.longitude ?: 0.0
                firebaseRepo.getRealtimeRestaurants(currentLat, currentLng)
                    .catch { e ->
                        android.util.Log.e("FirestoreRestaurant", "ViewModel caught restaurants error: ${e.message}")
                        _uiState.update { it.copy(errorMessage = "Restaurants error: ${e.message}", isLoading = false) }
                    }
                    .collect { rests ->
                        android.util.Log.d("FirestoreRestaurant", "ViewModel received ${rests.size} restaurants")
                        _uiState.update { state ->
                            val updatedSelectedRest = state.selectedRestaurant ?: rests.firstOrNull()
                            state.copy(
                                restaurants = rests,
                                filteredRestaurants = filterRestaurants(rests, state.selectedCategory, state.searchQuery),
                                selectedRestaurant = updatedSelectedRest,
                                isLoading = false
                            )
                        }
                    }
            }

            // 3. Realtime Popular Dishes (Collection Group Query across all restaurants)
            launch {
                firebaseRepo.getAllRealtimeDishes()
                    .catch { e ->
                        android.util.Log.e("FirestoreDebug", "ViewModel caught collectionGroup dishes error: ${e.message}")
                        _uiState.update { it.copy(errorMessage = "Popular dishes error: ${e.message}") }
                    }
                    .collect { dishes ->
                        android.util.Log.d("FirestoreDebug", "ViewModel received ${dishes.size} popular dishes")
                        _uiState.update { state ->
                            state.copy(
                                menuItems = dishes,
                                filteredMenuItems = filterDishes(dishes, state.selectedCategory, state.searchQuery)
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
            val isRestaurantActive = rest.isOpen || rest.isActive
            if (!isRestaurantActive) return@filter false

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
        viewModelScope.launch {
            firebaseRepo.getRealtimeMenu(restaurant.id)
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = "Menu error: ${e.message}") }
                }
                .collect { dishes ->
                    _uiState.update { state ->
                        state.copy(
                            menuItems = dishes,
                            filteredMenuItems = filterDishes(dishes, state.selectedCategory, state.searchQuery)
                        )
                    }
                }
        }
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

    fun refreshFirestoreData() {
        initRealtimeFirestoreListeners()
        _uiState.update { it.copy(statusMessage = "Refreshed Live Firestore Streams") }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
