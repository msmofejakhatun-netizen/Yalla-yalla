package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.room.AppDatabase
import com.example.data.room.OrderEntity
import com.example.data.room.WebhookLogEntity
import com.example.data.room.repository.PlatformRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NavigationTab {
    YALLA_HOME,
    YALLA_FIREBASE,
    YALLA_ORDERS,
    YALLA_COINS,
    YALLA_PROFILE,
    ZOMATO_DELIVERY,
    ZOMATO_ORDERS,
    ZOMATO_MONEY,
    ZOMATO_PROFILE,
    BLUEPRINT,
    SCHEMA,
    RAZORPAY_SANDBOX,
    DELIVERY_ENGINE,
    EDGE_CASES
}

data class CartItem(
    val item: FoodMenuItem,
    val quantity: Int
)

data class UiState(
    val activeTab: NavigationTab = NavigationTab.YALLA_HOME,
    val selectedRestaurant: RestaurantItem? = null,
    val isViewingRestaurantMenu: Boolean = false,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val deliveryLocation: String = "Indiranagar 100ft Rd, Bengaluru",
    val cart: List<CartItem> = emptyList(),
    val activeOrder: OrderEntity? = null,
    val yallaCoinsBalance: Int = 1450,
    val rzpSecretKey: String = "rzp_sec_live_98124801284",
    val selectedDeliveryProvider: DeliveryProvider = DeliveryProvider.DUNZO,
    val isSimulatingPipeline: Boolean = false,
    val simulationStepDescription: String = "Idle - Ready to launch order workflow",
    val simulationProgress: Float = 0.0f,
    val rzpWebhookSecret: String = "wh_sec_zomato_39102",
    val customVerificationMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ArchitectureViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlatformRepository
    val allOrders: StateFlow<List<OrderEntity>>
    val allWebhooks: StateFlow<List<WebhookLogEntity>>

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PlatformRepository(database.orderDao())

        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allWebhooks = repository.allWebhooks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        _uiState.update { it.copy(selectedRestaurant = repository.sampleRestaurants.first()) }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(activeTab = tab, errorMessage = null, successMessage = null) }
    }

    fun selectRestaurant(restaurant: RestaurantItem) {
        _uiState.update { it.copy(selectedRestaurant = restaurant, isViewingRestaurantMenu = true) }
    }

    fun openRestaurantMenu(restaurant: RestaurantItem) {
        selectRestaurant(restaurant)
    }

    fun closeRestaurantMenu() {
        _uiState.update { it.copy(isViewingRestaurantMenu = false) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setCategory(category: String) {
        setSelectedCategory(category)
    }

    fun applyOfferCode(code: String) {
        _uiState.update {
            it.copy(successMessage = "🎉 Offer Code '$code' Applied Successfully!")
        }
    }

    fun setDeliveryLocation(location: String) {
        _uiState.update { it.copy(deliveryLocation = location) }
    }

    fun addToCart(item: FoodMenuItem) {
        _uiState.update { state ->
            val existing = state.cart.find { it.item.id == item.id }
            val updatedCart = if (existing != null) {
                state.cart.map {
                    if (it.item.id == item.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.cart + CartItem(item, 1)
            }
            state.copy(cart = updatedCart)
        }
    }

    fun removeFromCart(item: FoodMenuItem) {
        _uiState.update { state ->
            val existing = state.cart.find { it.item.id == item.id }
            val updatedCart = if (existing != null && existing.quantity > 1) {
                state.cart.map {
                    if (it.item.id == item.id) it.copy(quantity = it.quantity - 1) else it
                }
            } else {
                state.cart.filter { it.item.id != item.id }
            }
            state.copy(cart = updatedCart)
        }
    }

    fun setDeliveryProvider(provider: DeliveryProvider) {
        _uiState.update { it.copy(selectedDeliveryProvider = provider) }
    }

    fun setRzpSecretKey(key: String) {
        _uiState.update { it.copy(rzpSecretKey = key) }
    }

    /**
     * Executes complete E2E Food Delivery State Machine Pipeline in real time!
     */
    fun runFullPipelineSimulation(forcePaymentFail: Boolean = false, forceRunnerFail: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSimulatingPipeline = true,
                    simulationProgress = 0.1f,
                    simulationStepDescription = "Step 1/5: Client creates order. Calling Backend POST /api/v1/orders..."
                )
            }
            delay(1200)

            val restaurantName = _uiState.value.selectedRestaurant?.name ?: "Biryani Blues"
            val calculatedSub = _uiState.value.cart.fold(0.0) { acc, c -> acc + (c.item.price * c.quantity) }
            val subtotal = if (calculatedSub == 0.0) 420.0 else calculatedSub
            val deliveryFee = if (_uiState.value.selectedDeliveryProvider == DeliveryProvider.DUNZO) 45.0 else 48.0

            val order = repository.createInitialOrder(
                restaurantName = restaurantName,
                itemsSummary = "2x Chicken Dum Biryani, 1x Galouti Kebab",
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                rzpSecretKey = _uiState.value.rzpSecretKey
            )

            _uiState.update {
                it.copy(
                    activeOrder = order,
                    simulationProgress = 0.3f,
                    simulationStepDescription = "Step 2/5: Razorpay Checkout opened. Initiating payment authorization..."
                )
            }
            delay(1500)

            if (forcePaymentFail) {
                val (failedOrder, _) = repository.simulateRazorpayPayment(
                    order, PaymentMethodType.UPI, _uiState.value.rzpSecretKey, simulateFailure = true
                )
                _uiState.update {
                    it.copy(
                        activeOrder = failedOrder,
                        isSimulatingPipeline = false,
                        simulationProgress = 1.0f,
                        simulationStepDescription = "❌ Pipeline Stopped: Razorpay Payment Failed or Declined by User Bank."
                    )
                }
                return@launch
            }

            val (successOrder, webhook) = repository.simulateRazorpayPayment(
                order, PaymentMethodType.UPI, _uiState.value.rzpSecretKey, simulateFailure = false
            )

            _uiState.update {
                it.copy(
                    activeOrder = successOrder,
                    simulationProgress = 0.5f,
                    simulationStepDescription = "Step 3/5: Webhook verified (HMAC SHA-256). Merchant received & confirmed order!"
                )
            }
            delay(1500)

            _uiState.update {
                it.copy(
                    simulationProgress = 0.7f,
                    simulationStepDescription = "Step 4/5: Dispatching Hyperlocal Rider via ${_uiState.value.selectedDeliveryProvider.name} API..."
                )
            }
            delay(1500)

            if (forceRunnerFail) {
                val refundedOrder = repository.simulateDeliveryDispatch(
                    successOrder, _uiState.value.selectedDeliveryProvider, forceRunnerUnavailability = true
                )
                _uiState.update {
                    it.copy(
                        activeOrder = refundedOrder,
                        isSimulatingPipeline = false,
                        simulationProgress = 1.0f,
                        simulationStepDescription = "⚠️ Runner Unavailable on Dunzo & Porter! Automated INSTANT REFUND triggered: ${refundedOrder.refundId}"
                    )
                }
                return@launch
            }

            val dispatchedOrder = repository.simulateDeliveryDispatch(
                successOrder, _uiState.value.selectedDeliveryProvider, forceRunnerUnavailability = false
            )

            _uiState.update {
                it.copy(
                    activeOrder = dispatchedOrder,
                    simulationProgress = 0.9f,
                    simulationStepDescription = "Step 5/5: Rider Assigned (${dispatchedOrder.riderName}). Out for delivery with GPS tracking..."
                )
            }
            delay(2000)

            val finalDelivered = repository.markOrderDelivered(dispatchedOrder)

            _uiState.update {
                it.copy(
                    activeOrder = finalDelivered,
                    isSimulatingPipeline = false,
                    simulationProgress = 1.0f,
                    simulationStepDescription = "🎉 Pipeline Completed Successfully! Order Delivered to Customer."
                )
            }
        }
    }

    fun verifySignatureInSandbox(orderId: String, paymentId: String, signature: String, secret: String) {
        val isValid = RazorpayCryptoUtils.verifyPaymentSignature(orderId, paymentId, signature, secret)
        val msg = if (isValid) {
            "✅ VALID SIGNATURE: HMAC-SHA256 matches secret key perfectly!"
        } else {
            "❌ INVALID SIGNATURE: Signature mismatch! Potential tampering or invalid secret key."
        }
        _uiState.update { it.copy(customVerificationMessage = msg) }
    }

    fun triggerInstantRefundForActiveOrder(reason: String) {
        val currentOrder = _uiState.value.activeOrder ?: return
        viewModelScope.launch {
            val refunded = repository.cancelAndRefundOrder(currentOrder, reason)
            _uiState.update {
                it.copy(
                    activeOrder = refunded,
                    successMessage = "Instant Refund of ₹${refunded.refundAmount} processed! Refund ID: ${refunded.refundId}"
                )
            }
        }
    }

    fun claimDailyYallaCoins() {
        val reward = (50..150).random()
        _uiState.update {
            it.copy(
                yallaCoinsBalance = it.yallaCoinsBalance + reward,
                successMessage = "🎉 You claimed +$reward Yalla Coins today!"
            )
        }
    }

    fun redeemYallaCoins(cost: Int, rewardDescription: String) {
        if (_uiState.value.yallaCoinsBalance >= cost) {
            _uiState.update {
                it.copy(
                    yallaCoinsBalance = it.yallaCoinsBalance - cost,
                    successMessage = "✨ Redeemed '$rewardDescription'! Voucher added to your account."
                )
            }
        } else {
            _uiState.update {
                it.copy(errorMessage = "Insufficient Yalla Coins. You need $cost coins!")
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, customVerificationMessage = null) }
    }

    fun getSampleRestaurants() = repository.sampleRestaurants
    fun getSampleCategories() = repository.sampleCategories
    fun getSampleOffers() = repository.sampleOffers
}
