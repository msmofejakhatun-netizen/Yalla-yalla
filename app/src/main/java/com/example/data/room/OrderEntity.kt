package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulated_orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val restaurantName: String,
    val itemsSummary: String,
    val subtotalAmount: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String, // CREATED, PAYMENT_PENDING, PAYMENT_SUCCESS, PREPARING, DISPATCHED, DELIVERED, CANCELLED_REFUNDED
    val razorpayOrderId: String,
    val razorpayPaymentId: String? = null,
    val paymentMethod: String? = null,
    val deliveryProvider: String? = null, // DUNZO, PORTER
    val deliveryTaskId: String? = null,
    val riderName: String? = null,
    val riderPhone: String? = null,
    val riderLat: Double = 12.9716, // Bangalore default coords
    val riderLng: Double = 77.5946,
    val etaMinutes: Int = 20,
    val refundId: String? = null,
    val refundAmount: Double = 0.0,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "simulated_webhooks")
data class WebhookLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val sourceProvider: String, // RAZORPAY, DUNZO, PORTER
    val eventType: String,
    val payloadJson: String,
    val processed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
