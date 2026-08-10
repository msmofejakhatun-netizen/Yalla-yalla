package com.example.data.room.repository

import com.example.data.models.*
import com.example.data.room.OrderDao
import com.example.data.room.OrderEntity
import com.example.data.room.WebhookLogEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PlatformRepository(private val orderDao: OrderDao) {

    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allWebhooks: Flow<List<WebhookLogEntity>> = orderDao.getAllWebhooks()

    // Sample Categories
    val sampleCategories = listOf(
        FoodCategory("cat_yalla_specials", "Yalla Specials", "⚡", "EXCLUSIVES"),
        FoodCategory("cat_biryani", "Biryani", "🍲", "🔥 HOT"),
        FoodCategory("cat_pizza", "Pizza", "🍕", "50% OFF"),
        FoodCategory("cat_burger", "Burger", "🍔"),
        FoodCategory("cat_healthy", "Healthy", "🥗", "FIT"),
        FoodCategory("cat_cake", "Cake & Bakery", "🍰"),
        FoodCategory("cat_north_indian", "North Indian", "🥘"),
        FoodCategory("cat_chinese", "Chinese", "🥢"),
        FoodCategory("cat_thali", "Thali", "🍱")
    )

    // Sample Offers
    val sampleOffers = listOf(
        OfferBanner("off_1", "50% OFF UP TO ₹100", "Use promo code YALLA50 on your favorite meals", "YALLA50", 0xFFFF5E00, 0xFFD94B00),
        OfferBanner("off_2", "EARN 2X YALLA COINS", "Get double coin cashback on all Yalla Verified orders", "YALLACOINS", 0xFF00A86B, 0xFF006B44),
        OfferBanner("off_3", "FLAT ₹125 INSTANT CASHBACK", "Pay using Razorpay UPI or Yalla Wallet at checkout", "YALLARZP", 0xFFFFB800, 0xFFD99B00)
    )

    // Sample Restaurants Data
    val sampleRestaurants = listOf(
        RestaurantItem(
            id = "rest_biryani_blues_01",
            name = "Biryani Blues",
            cuisine = "Hyderabadi Dum Biryani • Kebabs",
            rating = 4.8,
            deliveryTimeMins = 25,
            priceForTwo = 450,
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500",
            address = "Koramangala 5th Block, Bengaluru",
            lat = 12.9352,
            lng = 77.6245,
            distanceKm = "2.1 km",
            offerTag = "50% OFF up to ₹100",
            isPromoted = true,
            isGoldPartner = true,
            menu = listOf(
                FoodMenuItem("item_01", "Chicken Dum Biryani (Large)", 340.0, "Classic fragrant basmati rice with marinated chicken pieces", isBestseller = true, isVeg = false, rating = 4.8, voteCount = 340),
                FoodMenuItem("item_02", "Mutton Galouti Kebab (4 pcs)", 280.0, "Melt-in-mouth kebabs served with mint chutney", isBestseller = true, isVeg = false, rating = 4.7, voteCount = 190),
                FoodMenuItem("item_03", "Paneer Tikka Biryani", 290.0, "Spiced cottage cheese tikka layered with dum rice", isBestseller = false, isVeg = true, rating = 4.6, voteCount = 120),
                FoodMenuItem("item_04", "Phirni Dessert Pot", 90.0, "Traditional saffron rice pudding with silver foil", isBestseller = false, isVeg = true, rating = 4.9, voteCount = 80)
            )
        ),
        RestaurantItem(
            id = "rest_truffles_02",
            name = "Truffles Gourmet Burgers",
            cuisine = "American • Gourmet Burgers • Shakes",
            rating = 4.9,
            deliveryTimeMins = 20,
            priceForTwo = 600,
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500",
            address = "Indiranagar 100ft Road, Bengaluru",
            lat = 12.9784,
            lng = 77.6408,
            distanceKm = "3.4 km",
            offerTag = "FLAT ₹125 OFF with Razorpay",
            isPromoted = false,
            isGoldPartner = true,
            menu = listOf(
                FoodMenuItem("item_05", "All American Lamb Burger", 360.0, "Juicy double patty, cheddar, caramelized onions, brioche bun", isBestseller = true, isVeg = false, rating = 4.9, voteCount = 520),
                FoodMenuItem("item_06", "Crispy Peri Peri Fries", 140.0, "Hand-cut potato fries with spicy peri-peri seasoning", isBestseller = false, isVeg = true, rating = 4.8, voteCount = 310),
                FoodMenuItem("item_07", "Belgian Chocolate Thickshake", 190.0, "Rich dark chocolate ganache blended thickshake", isBestseller = true, isVeg = true, rating = 4.9, voteCount = 410)
            )
        ),
        RestaurantItem(
            id = "rest_punjab_grill_03",
            name = "Punjab Grill & Co.",
            cuisine = "North Indian • Butter Chicken • Naan",
            rating = 4.7,
            deliveryTimeMins = 30,
            priceForTwo = 700,
            imageUrl = "https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?w=500",
            address = "MG Road, Bengaluru",
            lat = 12.9716,
            lng = 77.5946,
            distanceKm = "1.8 km",
            offerTag = "FREE DELIVERY",
            isPromoted = false,
            isGoldPartner = false,
            menu = listOf(
                FoodMenuItem("item_08", "Murgh Makhani (Butter Chicken)", 380.0, "Creamy tomato gravy with tender clay-oven roasted chicken", isBestseller = true, isVeg = false, rating = 4.8, voteCount = 610),
                FoodMenuItem("item_09", "Garlic Butter Naan (2 pcs)", 80.0, "Leavened flatbread topped with garlic and fresh butter", isBestseller = false, isVeg = true, rating = 4.7, voteCount = 450),
                FoodMenuItem("item_10", "Dal Makhani Slow Cooked", 260.0, "Black lentils simmered overnight with cream and butter", isBestseller = true, isVeg = true, rating = 4.9, voteCount = 380)
            )
        )
    )

    suspend fun createInitialOrder(
        restaurantName: String,
        itemsSummary: String,
        subtotal: Double,
        deliveryFee: Double,
        rzpSecretKey: String = "rzp_secret_test_key_889"
    ): OrderEntity {
        val orderId = "ord_" + UUID.randomUUID().toString().take(8)
        val rzpOrderId = "order_" + UUID.randomUUID().toString().take(10)
        val totalAmount = subtotal + deliveryFee + 10.0 // 10 platform fee

        val order = OrderEntity(
            id = orderId,
            restaurantName = restaurantName,
            itemsSummary = itemsSummary,
            subtotalAmount = subtotal,
            deliveryFee = deliveryFee,
            totalAmount = totalAmount,
            status = "PAYMENT_PENDING",
            razorpayOrderId = rzpOrderId,
            createdAtTimestamp = System.currentTimeMillis()
        )

        orderDao.insertOrUpdateOrder(order)
        return order
    }

    suspend fun simulateRazorpayPayment(
        order: OrderEntity,
        paymentMethod: PaymentMethodType,
        secretKey: String,
        simulateFailure: Boolean = false
    ): Pair<OrderEntity, WebhookLogEntity?> {
        if (simulateFailure) {
            val failedOrder = order.copy(status = "PAYMENT_FAILED")
            orderDao.insertOrUpdateOrder(failedOrder)

            val failureWebhook = WebhookLogEntity(
                eventId = "evt_fail_" + UUID.randomUUID().toString().take(8),
                sourceProvider = "RAZORPAY",
                eventType = "payment.failed",
                payloadJson = """{"event": "payment.failed", "order_id": "${order.razorpayOrderId}", "error_code": "BAD_REQUEST_PAYMENT_DECLINED"}"""
            )
            orderDao.insertWebhookLog(failureWebhook)

            return Pair(failedOrder, failureWebhook)
        }

        val paymentId = "pay_" + UUID.randomUUID().toString().take(10)
        val calculatedSignature = RazorpayCryptoUtils.calculateHmacSha256(
            "${order.razorpayOrderId}|$paymentId",
            secretKey
        )

        val updatedOrder = order.copy(
            status = "PAYMENT_SUCCESS",
            razorpayPaymentId = paymentId,
            paymentMethod = paymentMethod.name
        )
        orderDao.insertOrUpdateOrder(updatedOrder)

        val successWebhook = WebhookLogEntity(
            eventId = "evt_success_" + UUID.randomUUID().toString().take(8),
            sourceProvider = "RAZORPAY",
            eventType = "payment.captured",
            payloadJson = """
                {
                  "event": "payment.captured",
                  "event_id": "evt_${UUID.randomUUID().toString().take(6)}",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "$paymentId",
                        "order_id": "${order.razorpayOrderId}",
                        "method": "${paymentMethod.name.lowercase()}",
                        "amount": ${(order.totalAmount * 100).toLong()},
                        "signature": "$calculatedSignature"
                      }
                    }
                  }
                }
            """.trimIndent()
        )
        orderDao.insertWebhookLog(successWebhook)

        return Pair(updatedOrder, successWebhook)
    }

    suspend fun simulateDeliveryDispatch(
        order: OrderEntity,
        primaryProvider: DeliveryProvider = DeliveryProvider.DUNZO,
        forceRunnerUnavailability: Boolean = false
    ): OrderEntity {
        if (forceRunnerUnavailability) {
            // Dunzo fails -> Porter fails -> Order Cancelled + Refund
            val failoverWebhook1 = WebhookLogEntity(
                eventId = "evt_dunzo_fail_" + UUID.randomUUID().toString().take(6),
                sourceProvider = "DUNZO",
                eventType = "task.unassigned",
                payloadJson = """{"status": "NO_RUNNER_AVAILABLE", "provider": "DUNZO"}"""
            )
            val failoverWebhook2 = WebhookLogEntity(
                eventId = "evt_porter_fail_" + UUID.randomUUID().toString().take(6),
                sourceProvider = "PORTER",
                eventType = "order.cancelled",
                payloadJson = """{"status": "NO_RUNNER_AVAILABLE", "provider": "PORTER"}"""
            )
            orderDao.insertWebhookLog(failoverWebhook1)
            orderDao.insertWebhookLog(failoverWebhook2)

            val refundId = "rfnd_" + UUID.randomUUID().toString().take(8)
            val cancelledOrder = order.copy(
                status = "CANCELLED_REFUNDED",
                refundId = refundId,
                refundAmount = order.totalAmount
            )
            orderDao.insertOrUpdateOrder(cancelledOrder)

            val refundWebhook = WebhookLogEntity(
                eventId = "evt_rfnd_" + UUID.randomUUID().toString().take(6),
                sourceProvider = "RAZORPAY",
                eventType = "refund.processed",
                payloadJson = """{"event": "refund.processed", "refund_id": "$refundId", "amount": ${(order.totalAmount * 100).toLong()}, "speed": "instant"}"""
            )
            orderDao.insertWebhookLog(refundWebhook)

            return cancelledOrder
        }

        val taskId = "${primaryProvider.name.lowercase()}_task_" + UUID.randomUUID().toString().take(8)
        val riderNames = listOf("Ramesh Kumar", "Vikram Singh", "Abdul Rahman", "Suresh Patel")
        val riderName = riderNames.random()
        val riderPhone = "+91 98765 ${ (10000..99999).random() }"

        val dispatchedOrder = order.copy(
            status = "OUT_FOR_DELIVERY",
            deliveryProvider = primaryProvider.name,
            deliveryTaskId = taskId,
            riderName = riderName,
            riderPhone = riderPhone,
            etaMinutes = (12..25).random(),
            riderLat = 12.9352 + (Math.random() - 0.5) * 0.02,
            riderLng = 77.6245 + (Math.random() - 0.5) * 0.02
        )

        orderDao.insertOrUpdateOrder(dispatchedOrder)

        val dispatchWebhook = WebhookLogEntity(
            eventId = "evt_dispatch_" + UUID.randomUUID().toString().take(6),
            sourceProvider = primaryProvider.name,
            eventType = "order.dispatched",
            payloadJson = """
                {
                  "task_id": "$taskId",
                  "status": "OUT_FOR_DELIVERY",
                  "runner": {
                    "name": "$riderName",
                    "phone": "$riderPhone",
                    "lat": ${dispatchedOrder.riderLat},
                    "lng": ${dispatchedOrder.riderLng}
                  }
                }
            """.trimIndent()
        )
        orderDao.insertWebhookLog(dispatchWebhook)

        return dispatchedOrder
    }

    suspend fun markOrderDelivered(order: OrderEntity): OrderEntity {
        val deliveredOrder = order.copy(status = "DELIVERED", etaMinutes = 0)
        orderDao.insertOrUpdateOrder(deliveredOrder)

        val deliveredWebhook = WebhookLogEntity(
            eventId = "evt_deliv_" + UUID.randomUUID().toString().take(6),
            sourceProvider = order.deliveryProvider ?: "DUNZO",
            eventType = "order.completed",
            payloadJson = """{"task_id": "${order.deliveryTaskId}", "status": "DELIVERED", "delivered_at": "${System.currentTimeMillis()}"}"""
        )
        orderDao.insertWebhookLog(deliveredWebhook)

        return deliveredOrder
    }

    suspend fun cancelAndRefundOrder(order: OrderEntity, reason: String): OrderEntity {
        val refundId = "rfnd_inst_" + UUID.randomUUID().toString().take(8)
        val cancelledOrder = order.copy(
            status = "CANCELLED_REFUNDED",
            refundId = refundId,
            refundAmount = order.totalAmount
        )
        orderDao.insertOrUpdateOrder(cancelledOrder)

        val refundWebhook = WebhookLogEntity(
            eventId = "evt_rfnd_user_" + UUID.randomUUID().toString().take(6),
            sourceProvider = "RAZORPAY",
            eventType = "refund.processed",
            payloadJson = """
                {
                  "event": "refund.processed",
                  "refund_id": "$refundId",
                  "payment_id": "${order.razorpayPaymentId ?: "pay_simulated"}",
                  "amount": ${(order.totalAmount * 100).toLong()},
                  "speed": "instant",
                  "reason": "$reason"
                }
            """.trimIndent()
        )
        orderDao.insertWebhookLog(refundWebhook)

        return cancelledOrder
    }
}
