package com.example.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.e("FirebaseRepo", "FirebaseAuth getInstance failed: ${e.message}")
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.e("FirebaseRepo", "FirebaseFirestore getInstance failed: ${e.message}")
            null
        }

    private var menuListenerRegistration: ListenerRegistration? = null
    private var orderListenerRegistration: ListenerRegistration? = null
    private var promoListenerRegistration: ListenerRegistration? = null
    private var restaurantListenerRegistration: ListenerRegistration? = null

    init {
        ensureFirebaseAuth()
    }

    private fun ensureFirebaseAuth() {
        try {
            val a = auth ?: return
            if (a.currentUser == null) {
                a.signInAnonymously().addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            Log.d("FirebaseRepo", "Anonymous Auth Successful: ${a.currentUser?.uid}")
                        } else {
                            Log.w("FirebaseRepo", "Anonymous Auth Failed", task.exception)
                        }
                    } catch (t: Throwable) {
                        Log.w("FirebaseRepo", "Auth complete listener caught exception: ${t.message}")
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e("FirebaseRepo", "Firebase Auth initialization skipped or failed: ${e.message}")
        }
    }

    /**
     * Real-Time Firestore Promotions Flow `/promotions`
     */
    fun getRealtimePromotions(): Flow<List<FirestorePromotionItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                trySend(getFallbackSamplePromotions())
                awaitClose { }
                return@callbackFlow
            }
            val query = db.collection("promotions").whereEqualTo("isActive", true)
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRepo", "Firestore promotions listener error: ${error.message}")
                    trySend(getFallbackSamplePromotions())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val promos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestorePromotionItem::class.java)?.apply {
                            if (id.isEmpty()) id = doc.id
                        }
                    }
                    trySend(promos)
                } else {
                    seedSamplePromotionsToFirestore()
                    trySend(getFallbackSamplePromotions())
                }
            }
            promoListenerRegistration = listener
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Exception in getRealtimePromotions: ${e.message}")
            trySend(getFallbackSamplePromotions())
            awaitClose { }
        }
    }

    /**
     * Real-Time Firestore Restaurants Flow `/restaurants`
     */
    fun getRealtimeRestaurants(): Flow<List<FirestoreRestaurantItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                trySend(getFallbackSampleRestaurants())
                awaitClose { }
                return@callbackFlow
            }
            val query = db.collection("restaurants")
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRepo", "Firestore restaurants listener error: ${error.message}")
                    trySend(getFallbackSampleRestaurants())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val rests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreRestaurantItem::class.java)?.apply {
                            if (id.isEmpty()) id = doc.id
                        }
                    }
                    trySend(rests)
                } else {
                    seedSampleRestaurantsToFirestore()
                    trySend(getFallbackSampleRestaurants())
                }
            }
            restaurantListenerRegistration = listener
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Exception in getRealtimeRestaurants: ${e.message}")
            trySend(getFallbackSampleRestaurants())
            awaitClose { }
        }
    }

    /**
     * Real-Time Firestore Menu Flow
     * Listens to `/restaurants/{restaurantId}/menu` where `inStock == true`
     */
    fun getRealtimeMenu(restaurantId: String = "rest_yalla_1"): Flow<List<FirestoreDishItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                trySend(getFallbackSampleMenu())
                awaitClose { }
                return@callbackFlow
            }
            val query = db.collection("restaurants")
                .document(restaurantId)
                .collection("menu")
                .whereEqualTo("inStock", true)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRepo", "Firestore menu listener error: ${error.message}")
                    // Emit sample fallback items if Firestore query fails
                    trySend(getFallbackSampleMenu())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val dishes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreDishItem::class.java)?.apply {
                            if (id.isEmpty()) id = doc.id
                        }
                    }
                    trySend(dishes)
                } else {
                    // Seed initial sample data to Firestore if empty, then emit fallback
                    seedSampleMenuToFirestore(restaurantId)
                    trySend(getFallbackSampleMenu())
                }
            }

            menuListenerRegistration = listener

            awaitClose {
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Exception in getRealtimeMenu: ${e.message}")
            trySend(getFallbackSampleMenu())
            awaitClose { }
        }
    }

    /**
     * Seed initial menu to Firestore if empty
     */
    fun seedSampleMenuToFirestore(restaurantId: String = "rest_yalla_1") {
        try {
            val db = firestore ?: return
            val menuRef = db.collection("restaurants").document(restaurantId).collection("menu")
            getFallbackSampleMenu().forEach { item ->
                menuRef.document(item.id).set(item)
            }
        } catch (e: Exception) {
            Log.w("FirebaseRepo", "Could not seed menu to Firestore: ${e.message}")
        }
    }

    /**
     * Place new Order in Firestore `/orders` collection
     */
    suspend fun placeOrder(order: FirestoreOrderItem): Result<String> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val currentUserId = auth?.currentUser?.uid ?: "user_yalla_99"
            val orderToSave = order.copy(
                customerId = currentUserId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val docRef = if (orderToSave.orderId.isNotEmpty()) {
                db.collection("orders").document(orderToSave.orderId)
            } else {
                db.collection("orders").document()
            }

            val finalOrderId = docRef.id
            val finalOrder = orderToSave.copy(orderId = finalOrderId)

            docRef.set(finalOrder).await()
            Log.d("FirebaseRepo", "Order successfully placed in Firestore: $finalOrderId")
            Result.success(finalOrderId)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error placing order in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Real-time Listener attached to `/orders/{orderId}`
     */
    fun observeOrder(orderId: String): Flow<FirestoreOrderItem?> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }
            val docRef = db.collection("orders").document(orderId)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRepo", "Firestore order listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val order = snapshot.toObject(FirestoreOrderItem::class.java)
                    trySend(order)
                } else {
                    trySend(null)
                }
            }

            orderListenerRegistration = listener

            awaitClose {
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Exception observing order: ${e.message}")
            trySend(null)
            awaitClose { }
        }
    }

    /**
     * Update order status in Firestore (e.g. 'PREPARING' -> 'OUT_FOR_DELIVERY' -> 'DELIVERED')
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean {
        return try {
            val db = firestore ?: return false
            db.collection("orders").document(orderId)
                .update("orderStatus", newStatus, "updatedAt", System.currentTimeMillis())
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Failed to update order status in Firestore: ${e.message}")
            false
        }
    }

    /**
     * Fallback menu data for offline or non-initialized Firestore state
     */
    fun getFallbackSampleMenu(): List<FirestoreDishItem> {
        return listOf(
            FirestoreDishItem(
                id = "item_yalla_01",
                name = "Yalla Special Hyderabadi Biryani",
                category = "Biryani",
                price = 340.0,
                isVeg = false,
                inStock = true,
                imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500",
                description = "Fragrant basmati rice layered with juicy marinated chicken, saffronic herbs and fried onions.",
                rating = 4.9
            ),
            FirestoreDishItem(
                id = "item_yalla_02",
                name = "Paneer Dum Biryani",
                category = "Biryani",
                price = 290.0,
                isVeg = true,
                inStock = true,
                imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500",
                description = "Soft marinated cottage cheese blocks simmered in rich spices and dum rice.",
                rating = 4.7
            ),
            FirestoreDishItem(
                id = "item_yalla_03",
                name = "Yalla Loaded Cheese Pizza",
                category = "Pizza",
                price = 390.0,
                isVeg = true,
                inStock = true,
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                description = "10-inch hand-tossed sourdough pizza loaded with mozzarella, jalapenos, and bell peppers.",
                rating = 4.8
            ),
            FirestoreDishItem(
                id = "item_yalla_04",
                name = "Smokey Lamb Smash Burger",
                category = "Burger",
                price = 320.0,
                isVeg = false,
                inStock = true,
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500",
                description = "Double smashed lamb patty with melted cheddar, gherkins and secret house sauce.",
                rating = 4.9
            ),
            FirestoreDishItem(
                id = "item_yalla_05",
                name = "Butter Chicken Tender Bowl",
                category = "North Indian",
                price = 360.0,
                isVeg = false,
                inStock = true,
                imageUrl = "https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?w=500",
                description = "Creamy tomato silk gravy served with boneless chicken tenders and garlic butter naan.",
                rating = 4.8
            ),
            FirestoreDishItem(
                id = "item_yalla_06",
                name = "Avocado Quinoa Power Salad",
                category = "Healthy",
                price = 280.0,
                isVeg = true,
                inStock = true,
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500",
                description = "Fresh avocado, roasted chickpeas, organic quinoa and lemon mustard dressing.",
                rating = 4.6
            )
        )
    }

    fun seedSamplePromotionsToFirestore() {
        try {
            val db = firestore ?: return
            val ref = db.collection("promotions")
            getFallbackSamplePromotions().forEach { promo ->
                ref.document(promo.id).set(promo)
            }
        } catch (e: Exception) {
            Log.w("FirebaseRepo", "Could not seed promotions: ${e.message}")
        }
    }

    fun seedSampleRestaurantsToFirestore() {
        try {
            val db = firestore ?: return
            val ref = db.collection("restaurants")
            getFallbackSampleRestaurants().forEach { rest ->
                ref.document(rest.id).set(rest)
            }
        } catch (e: Exception) {
            Log.w("FirebaseRepo", "Could not seed restaurants: ${e.message}")
        }
    }

    fun getFallbackSamplePromotions(): List<FirestorePromotionItem> {
        return listOf(
            FirestorePromotionItem(
                id = "promo_1",
                title = "FLAT ₹120 OFF",
                subtitle = "On orders above ₹399 • Code YALLA120",
                code = "YALLA120",
                discountPercent = 0,
                discountAmount = 120.0,
                bannerUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=600",
                isActive = true
            ),
            FirestorePromotionItem(
                id = "promo_2",
                title = "50% CRAZY DISCOUNT",
                subtitle = "Max ₹150 OFF on Biryani • Code BIRYANI50",
                code = "BIRYANI50",
                discountPercent = 50,
                discountAmount = 150.0,
                bannerUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600",
                isActive = true
            ),
            FirestorePromotionItem(
                id = "promo_3",
                title = "FREE DELIVERY",
                subtitle = "On all Yalla Prime Kitchen orders",
                code = "YALLAFREE",
                discountPercent = 0,
                discountAmount = 30.0,
                bannerUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600",
                isActive = true
            )
        )
    }

    fun getFallbackSampleRestaurants(): List<FirestoreRestaurantItem> {
        return listOf(
            FirestoreRestaurantItem(
                id = "rest_yalla_1",
                name = "Yalla Yalla Central Kitchen",
                cuisine = "Hyderabadi Biryani, Kebabs, North Indian",
                rating = 4.9,
                deliveryTimeMins = 22,
                priceForTwo = 350.0,
                address = "7th Block, Koramangala, Bengaluru",
                imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500",
                isPromoted = true,
                isOpen = true
            ),
            FirestoreRestaurantItem(
                id = "rest_yalla_2",
                name = "Biryani Blues Express",
                cuisine = "Authentic Dum Biryani, Mughlai",
                rating = 4.8,
                deliveryTimeMins = 25,
                priceForTwo = 400.0,
                address = "Indiranagar 100ft Road, Bengaluru",
                imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500",
                isPromoted = true,
                isOpen = true
            ),
            FirestoreRestaurantItem(
                id = "rest_yalla_3",
                name = "Sourdough Pizza Co.",
                cuisine = "Gourmet Woodfired Pizzas, Pasta",
                rating = 4.7,
                deliveryTimeMins = 30,
                priceForTwo = 500.0,
                address = "HSR Layout Sector 3, Bengaluru",
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500",
                isPromoted = false,
                isOpen = true
            ),
            FirestoreRestaurantItem(
                id = "rest_yalla_4",
                name = "Smash Burger Club",
                cuisine = "American Artisanal Burgers, Shakes",
                rating = 4.8,
                deliveryTimeMins = 20,
                priceForTwo = 350.0,
                address = "Koramangala 4th Block, Bengaluru",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500",
                isPromoted = false,
                isOpen = true
            )
        )
    }
}

