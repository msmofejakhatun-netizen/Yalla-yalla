package com.example.data.models

enum class DeliveryProvider {
    DUNZO, PORTER, FLASH_EXPRESS
}

enum class DeliveryAssignmentStatus {
    QUOTING,
    BOOKING_REQUESTED,
    RIDER_ASSIGNED,
    ARRIVED_AT_RESTAURANT,
    PICKED_UP_DISPATCHED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_NO_RUNNER
}

data class LocationCoordinates(
    val lat: Double,
    val lng: Double,
    val addressName: String
)

data class DeliveryQuote(
    val provider: DeliveryProvider,
    val costInInr: Double,
    val estimatedPickupTimeMins: Int,
    val estimatedDeliveryTimeMins: Int,
    val distanceKm: Double,
    val isAvailable: Boolean = true
)

data class RiderInfo(
    val name: String,
    val phone: String,
    val vehicleNumber: String,
    val rating: Double,
    val currentLat: Double,
    val currentLng: Double
)

data class DeliveryTelemetryUpdate(
    val taskId: String,
    val provider: DeliveryProvider,
    val status: DeliveryAssignmentStatus,
    val rider: RiderInfo?,
    val currentLat: Double,
    val currentLng: Double,
    val etaMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class FoodMenuItem(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val isBestseller: Boolean,
    val isVeg: Boolean = true,
    val rating: Double = 4.5,
    val voteCount: Int = 85,
    val imageUrl: String = ""
)

data class RestaurantItem(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val deliveryTimeMins: Int,
    val priceForTwo: Int,
    val imageUrl: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val menu: List<FoodMenuItem>,
    val distanceKm: String = "2.5 km",
    val offerTag: String = "50% OFF up to ₹100",
    val isPromoted: Boolean = false,
    val isGoldPartner: Boolean = true
)

data class FoodCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val badge: String? = null
)

data class OfferBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val discountCode: String,
    val bgGradientStart: Long,
    val bgGradientEnd: Long
)

