package com.example.data.firebase

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class FirestorePromotionItem(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var code: String = "",
    var discountPercent: Int = 0,
    var discountAmount: Double = 0.0,
    var bannerUrl: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
)

@IgnoreExtraProperties
data class FirestoreRestaurantItem(
    var id: String = "",
    var name: String = "",
    var cuisine: String = "North Indian, Biryani",
    var rating: Double = 4.8,
    var deliveryTimeMins: Int = 25,
    var priceForTwo: Double = 400.0,
    var address: String = "Koramangala, Bengaluru",
    var imageUrl: String = "",
    @get:PropertyName("isPromoted") @set:PropertyName("isPromoted") var isPromoted: Boolean = false,
    @get:PropertyName("isOpen") @set:PropertyName("isOpen") var isOpen: Boolean = true
)

@IgnoreExtraProperties
data class FirestoreDishItem(
    var id: String = "",
    var restaurantId: String = "rest_yalla_1",
    var name: String = "",
    var category: String = "Biryani",
    var price: Double = 0.0,
    @get:PropertyName("isVeg") @set:PropertyName("isVeg") var isVeg: Boolean = true,
    @get:PropertyName("inStock") @set:PropertyName("inStock") var inStock: Boolean = true,
    var imageUrl: String = "",
    var description: String = "",
    var rating: Double = 4.8
)

@IgnoreExtraProperties
data class FirestoreCartItem(
    var itemId: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var quantity: Int = 1,
    @get:PropertyName("isVeg") @set:PropertyName("isVeg") var isVeg: Boolean = true,
    var imageUrl: String = ""
)

@IgnoreExtraProperties
data class FirestoreOrderItem(
    var orderId: String = "",
    var customerId: String = "user_yalla_99",
    var restaurantId: String = "rest_yalla_1",
    var restaurantName: String = "Yalla Yalla Central Kitchen",
    var items: List<FirestoreCartItem> = emptyList(),
    var subtotal: Double = 0.0,
    var deliveryFee: Double = 30.0,
    var totalAmount: Double = 0.0,
    var paymentStatus: String = "PAID",
    var orderStatus: String = "PAID", // PAID -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class FirestoreUserProfile(
    var uid: String = "",
    var phoneNumber: String = "",
    var role: String = "CUSTOMER", // "CUSTOMER" or "RESTAURANT_OWNER"
    var createdAt: Long = System.currentTimeMillis()
)

