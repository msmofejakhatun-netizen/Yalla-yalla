package com.example.data.firebase

import android.location.Location
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
        // Initialization without auto GMS anonymous auth broker triggers
    }

    fun getCurrentUser() = auth?.currentUser

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error signing out: ${e.message}")
        }
    }

    suspend fun saveUserProfile(uid: String, phoneNumber: String, role: String = "CUSTOMER"): Boolean {
        val db = firestore ?: return false
        return try {
            val userMap = hashMapOf(
                "uid" to uid,
                "phoneNumber" to phoneNumber,
                "role" to role,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            db.collection("users").document(uid).set(userMap, com.google.firebase.firestore.SetOptions.merge()).await()
            Log.d("FirestoreDebug", "Successfully saved user profile to /users/$uid with role $role")
            true
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Failed to save user profile to /users/$uid: ${e.message}", e)
            false
        }
    }

    /**
     * Real-Time Firestore Promotions Flow `/promotions`
     */
    fun getRealtimePromotions(): Flow<List<FirestorePromotionItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                Log.e("FirestoreDebug", "Firestore instance is null in getRealtimePromotions")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val query = db.collection("promotions")
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDebug", "Firestore promotions listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docCount = snapshot?.documents?.size ?: 0
                Log.d("FirestoreDebug", "Fetched $docCount promotions")

                if (snapshot != null && !snapshot.isEmpty) {
                    val promos = snapshot.documents.mapNotNull { doc ->
                        try {
                            var item = doc.toObject(FirestorePromotionItem::class.java)
                            val data = doc.data
                            if (item == null || data != null) {
                                val titleVal = (doc.getString("title") ?: doc.getString("name") ?: item?.title ?: "").ifBlank { "Special Offer" }
                                val subVal = (doc.getString("subtitle") ?: item?.subtitle ?: "")
                                val codeVal = (doc.getString("code") ?: item?.code ?: "YALLA")
                                val discPercentVal = (doc.get("discountPercent") as? Number)?.toInt() ?: item?.discountPercent ?: 0
                                val discAmountVal = (doc.get("discountAmount") as? Number)?.toDouble() ?: item?.discountAmount ?: 0.0
                                val bannerVal = (doc.getString("bannerUrl") ?: doc.getString("image") ?: item?.bannerUrl ?: "")
                                val activeVal = (doc.getBoolean("isActive") ?: doc.getBoolean("is_active") ?: item?.isActive ?: true)

                                item = FirestorePromotionItem(
                                    id = doc.id,
                                    title = titleVal,
                                    subtitle = subVal,
                                    code = codeVal,
                                    discountPercent = discPercentVal,
                                    discountAmount = discAmountVal,
                                    bannerUrl = bannerVal,
                                    isActive = activeVal
                                )
                            } else {
                                item.apply {
                                    if (id.isEmpty()) id = doc.id
                                }
                            }
                            item
                        } catch (e: Exception) {
                            Log.e("FirestoreDebug", "Error parsing promo ${doc.id}: ${e.message}")
                            val data = doc.data
                            if (data != null) {
                                FirestorePromotionItem(
                                    id = doc.id,
                                    title = (data["title"] as? String) ?: "Special Offer",
                                    subtitle = (data["subtitle"] as? String) ?: "",
                                    code = (data["code"] as? String) ?: "YALLA",
                                    discountPercent = (data["discountPercent"] as? Number)?.toInt() ?: 0,
                                    discountAmount = (data["discountAmount"] as? Number)?.toDouble() ?: 0.0,
                                    bannerUrl = (data["bannerUrl"] as? String) ?: "",
                                    isActive = (data["isActive"] as? Boolean) ?: true
                                )
                            } else null
                        }
                    }
                    trySend(promos)
                } else {
                    trySend(emptyList())
                }
            }
            promoListenerRegistration = listener
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Exception in getRealtimePromotions: ${e.message}")
            trySend(emptyList())
            awaitClose { }
        }
    }

    /**
     * Real-Time Firestore Restaurants Flow `/restaurants`
     */
    fun getRealtimeRestaurants(userLat: Double = 0.0, userLng: Double = 0.0): Flow<List<FirestoreRestaurantItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                Log.e("FirestoreRestaurant", "Firestore instance is null in getRealtimeRestaurants")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val query = db.collection("restaurants")
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreRestaurant", "Firestore restaurants listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docCount = snapshot?.documents?.size ?: 0

                if (snapshot != null && !snapshot.isEmpty) {
                    val rests = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data
                            val nameVal = (doc.getString("name") 
                                ?: doc.getString("title") 
                                ?: doc.getString("restaurantName") 
                                ?: (data?.get("name") as? String) 
                                ?: "").ifBlank { "Restaurant ${doc.id}" }

                            val cuisineVal = (doc.getString("cuisine") 
                                ?: doc.getString("category") 
                                ?: (data?.get("cuisine") as? String) 
                                ?: "Multi-Cuisine").ifBlank { "Multi-Cuisine" }

                            val ratingVal = parseDoubleGracefully(
                                doc.get("rating") ?: data?.get("rating"),
                                4.5
                            )

                            val deliveryVal = (doc.get("deliveryTimeMins") as? Number)?.toInt() 
                                ?: (doc.get("delivery_time") as? Number)?.toInt() 
                                ?: (doc.get("deliveryTime") as? Number)?.toInt() 
                                ?: (data?.get("deliveryTimeMins") as? Number)?.toInt() 
                                ?: 25

                            val priceVal = parseDoubleGracefully(
                                doc.get("priceForTwo") ?: doc.get("price_for_two") ?: doc.get("price") ?: data?.get("priceForTwo"),
                                300.0
                            )

                            val addressVal = (doc.getString("address") 
                                ?: doc.getString("location") 
                                ?: (data?.get("address") as? String) 
                                ?: "")

                            val imageVal = (doc.getString("imageUrl") 
                                ?: doc.getString("image") 
                                ?: doc.getString("bannerUrl") 
                                ?: (data?.get("imageUrl") as? String) 
                                ?: "")

                            val isPromotedVal = parseBooleanGracefully(
                                doc.get("isPromoted") ?: doc.get("is_promoted") ?: data?.get("isPromoted"),
                                defaultVal = false
                            )

                            // Check for both isOpen == true OR isActive == true when parsing restaurant documents.
                            // If isOpen is true, treat the restaurant as active.
                            val rawIsOpen = doc.get("isOpen") ?: doc.get("is_open") ?: data?.get("isOpen")
                            val rawIsActive = doc.get("isActive") ?: doc.get("is_active") ?: data?.get("isActive")

                            val isOpenParsed = if (rawIsOpen != null) parseBooleanGracefully(rawIsOpen, true) else null
                            val isActiveParsed = if (rawIsActive != null) parseBooleanGracefully(rawIsActive, true) else null

                            val isRestaurantActive = when {
                                isOpenParsed != null && isActiveParsed != null -> isOpenParsed || isActiveParsed
                                isOpenParsed != null -> isOpenParsed
                                isActiveParsed != null -> isActiveParsed
                                else -> true
                            }

                            // Do NOT strictly hide restaurants if their latitude/longitude fields are missing, 0.0, or outside the radius.
                            val restLat = parseDoubleGracefully(doc.get("latitude") ?: doc.get("lat") ?: data?.get("latitude"), 0.0)
                            val restLng = parseDoubleGracefully(doc.get("longitude") ?: doc.get("lng") ?: data?.get("longitude"), 0.0)

                            // 2. GEOLOCATION DISTANCE LOGIC:
                            // Calculate distance dynamically using Android Location.distanceBetween().
                            // If distance calculation fails or coordinates are missing in Firestore, default distance display to "2.5 km • 25 mins" so the card renders safely.
                            var calculatedDistKm = 2.5
                            var formattedDistStr = "2.5 km • $deliveryVal mins"

                            if (userLat != 0.0 && userLng != 0.0 && restLat != 0.0 && restLng != 0.0) {
                                try {
                                    val results = FloatArray(1)
                                    Location.distanceBetween(
                                        userLat, userLng,
                                        restLat, restLng,
                                        results
                                    )
                                    val distMeters = results[0]
                                    calculatedDistKm = kotlin.math.round(distMeters / 100.0) / 10.0 // rounded to 1 decimal place
                                    if (calculatedDistKm < 0.1) calculatedDistKm = 0.5
                                    val estimatedMins = (deliveryVal + (calculatedDistKm * 3)).toInt()
                                    formattedDistStr = "$calculatedDistKm km • $estimatedMins mins"
                                } catch (e: Exception) {
                                    Log.w("FirestoreRestaurant", "Distance calculation error for ${doc.id}: ${e.message}")
                                    calculatedDistKm = 2.5
                                    formattedDistStr = "2.5 km • 25 mins"
                                }
                            } else {
                                calculatedDistKm = 2.5
                                formattedDistStr = "2.5 km • $deliveryVal mins"
                            }

                            FirestoreRestaurantItem(
                                id = doc.id,
                                name = nameVal,
                                cuisine = cuisineVal,
                                rating = ratingVal,
                                deliveryTimeMins = deliveryVal,
                                priceForTwo = priceVal,
                                address = addressVal,
                                imageUrl = imageVal,
                                isPromoted = isPromotedVal,
                                isOpen = isRestaurantActive,
                                latitude = restLat,
                                longitude = restLng,
                                isActive = isRestaurantActive,
                                distanceKm = calculatedDistKm,
                                formattedDistance = formattedDistStr
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreRestaurant", "Error parsing restaurant ${doc.id}: ${e.message}", e)
                            FirestoreRestaurantItem(
                                id = doc.id,
                                name = "Restaurant ${doc.id}",
                                cuisine = "Multi-Cuisine",
                                rating = 4.5,
                                deliveryTimeMins = 25,
                                priceForTwo = 300.0,
                                address = "",
                                imageUrl = "",
                                isPromoted = false,
                                isOpen = true
                            )
                        }
                    }

                    // 3. LOGGING & DEBUGGING:
                    Log.d("FirestoreRestaurant", "Fetched ${rests.size} restaurants for current location")
                    trySend(rests)
                } else {
                    Log.d("FirestoreRestaurant", "Fetched 0 restaurants for current location")
                    trySend(emptyList())
                }
            }
            restaurantListenerRegistration = listener
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirestoreRestaurant", "Exception in getRealtimeRestaurants: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
        }
    }

    private fun parseDoubleGracefully(value: Any?, defaultValue: Double = 0.0): Double {
        if (value == null) return defaultValue
        return when (value) {
            is Number -> value.toDouble()
            is String -> {
                if (value.isBlank()) return defaultValue
                val clean = value.replace(",", "").replace("[^0-9.]".toRegex(), "")
                clean.toDoubleOrNull() ?: defaultValue
            }
            else -> defaultValue
        }
    }

    private fun parseInStockGracefully(value: Any?): Boolean {
        // Do NOT filter out items if inStock is missing or null.
        // Treat inStock == true or missing/null as valid available items.
        if (value == null) return true
        return when (value) {
            is Boolean -> value
            is String -> value.isBlank() || value.equals("true", ignoreCase = true) || value.equals("1", ignoreCase = true) || value.equals("yes", ignoreCase = true)
            is Number -> value.toInt() != 0
            else -> true
        }
    }

    private fun parseBooleanGracefully(value: Any?, defaultVal: Boolean = true): Boolean {
        if (value == null) return defaultVal
        return when (value) {
            is Boolean -> value
            is String -> if (value.isBlank()) defaultVal else value.equals("true", ignoreCase = true) || value.equals("1", ignoreCase = true) || value.equals("yes", ignoreCase = true)
            is Number -> value.toInt() != 0
            else -> defaultVal
        }
    }

    /**
     * Real-Time Firestore Collection Group Query for Popular Dishes across all restaurants.
     * Queries /restaurants/{id}/menu via collectionGroup("menu")
     */
    fun getAllRealtimeDishes(): Flow<List<FirestoreDishItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                Log.e("FirestoreDebug", "Firestore instance is null in getAllRealtimeDishes")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            Log.d("FirestoreDebug", "Subscribing addSnapshotListener to collectionGroup('menu')")

            val query = db.collectionGroup("menu")
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDebug", "Firestore collectionGroup('menu') listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docCount = snapshot?.documents?.size ?: 0
                Log.d("FirestoreDebug", "CollectionGroup('menu') update: Fetched $docCount menu items across all restaurants")

                if (snapshot != null && !snapshot.isEmpty) {
                    val dishes = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data
                            val parentRestId = doc.reference.parent.parent?.id
                                ?: (data?.get("restaurantId") as? String)
                                ?: (data?.get("restId") as? String)
                                ?: ""

                            val nameVal = (doc.getString("name") 
                                ?: doc.getString("title") 
                                ?: doc.getString("dishName") 
                                ?: (data?.get("name") as? String) 
                                ?: (data?.get("title") as? String)
                                ?: "").ifBlank { "Dish ${doc.id}" }

                            val categoryVal = (doc.getString("category") 
                                ?: doc.getString("cuisine") 
                                ?: (data?.get("category") as? String) 
                                ?: "Popular").ifBlank { "Popular" }

                            val rawPrice = doc.get("price") ?: doc.get("amount") ?: doc.get("cost") ?: doc.get("rate") ?: data?.get("price")
                            val priceVal = parseDoubleGracefully(rawPrice, 0.0)

                            val isVegVal = parseBooleanGracefully(
                                doc.get("isVeg") ?: doc.get("is_veg") ?: doc.get("veg") ?: data?.get("isVeg"),
                                defaultVal = true
                            )

                            val inStockVal = parseInStockGracefully(
                                doc.get("inStock") ?: doc.get("in_stock") ?: doc.get("isAvailable") ?: doc.get("is_available") ?: data?.get("inStock")
                            )

                            val imageVal = (doc.getString("imageUrl") 
                                ?: doc.getString("image") 
                                ?: doc.getString("bannerUrl") 
                                ?: (data?.get("imageUrl") as? String) 
                                ?: "")

                            val descVal = (doc.getString("description") 
                                ?: (data?.get("description") as? String) 
                                ?: "")

                            val rawRating = doc.get("rating") ?: data?.get("rating")
                            val ratingVal = parseDoubleGracefully(rawRating, 4.8)

                            FirestoreDishItem(
                                id = doc.id,
                                restaurantId = parentRestId,
                                name = nameVal,
                                category = categoryVal,
                                price = priceVal,
                                isVeg = isVegVal,
                                inStock = inStockVal,
                                imageUrl = imageVal,
                                description = descVal,
                                rating = ratingVal
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreDebug", "Error parsing collectionGroup menu item ${doc.id}: ${e.message}", e)
                            FirestoreDishItem(
                                id = doc.id,
                                restaurantId = doc.reference.parent.parent?.id ?: "",
                                name = "Dish ${doc.id}",
                                category = "Popular",
                                price = 0.0,
                                isVeg = true,
                                inStock = true,
                                imageUrl = "",
                                description = "",
                                rating = 4.8
                            )
                        }
                    }
                    Log.d("FirestoreDebug", "Emitting ${dishes.size} dishes from collectionGroup('menu')")
                    trySend(dishes)
                } else {
                    Log.d("FirestoreDebug", "CollectionGroup('menu') snapshot is empty")
                    trySend(emptyList())
                }
            }

            awaitClose {
                Log.d("FirestoreDebug", "Closing snapshot listener for collectionGroup('menu')")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Exception in getAllRealtimeDishes: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
        }
    }

    /**
     * Real-Time Firestore Menu Flow for specific restaurant
     * Listens to `/restaurants/{restaurantId}/menu` with active addSnapshotListener
     */
    fun getRealtimeMenu(restaurantId: String = ""): Flow<List<FirestoreDishItem>> = callbackFlow {
        if (restaurantId.isBlank() || restaurantId == "ALL") {
            val job = this.launch {
                getAllRealtimeDishes().collect { trySend(it) }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        try {
            val db = firestore
            if (db == null) {
                Log.e("FirestoreDebug", "Firestore instance is null in getRealtimeMenu")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val targetRestId = restaurantId
            val query = db.collection("restaurants")
                .document(targetRestId)
                .collection("menu")

            Log.d("FirestoreDebug", "Subscribing addSnapshotListener to /restaurants/$targetRestId/menu")

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDebug", "Firestore menu listener error for $targetRestId: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docCount = snapshot?.documents?.size ?: 0
                Log.d("FirestoreDebug", "Realtime update: Fetched $docCount menu items for restaurant $targetRestId")

                if (snapshot != null && !snapshot.isEmpty) {
                    val dishes = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data
                            val nameVal = (doc.getString("name") 
                                ?: doc.getString("title") 
                                ?: doc.getString("dishName") 
                                ?: (data?.get("name") as? String) 
                                ?: "").ifBlank { "Dish ${doc.id}" }

                            val categoryVal = (doc.getString("category") 
                                ?: doc.getString("cuisine") 
                                ?: (data?.get("category") as? String) 
                                ?: "Popular").ifBlank { "Popular" }

                            // Gracefully parse price whether it comes as Int, Double, Long, or String
                            val rawPrice = doc.get("price") ?: doc.get("amount") ?: doc.get("cost") ?: doc.get("rate")
                            val priceVal = parseDoubleGracefully(rawPrice, 0.0)

                            val isVegVal = parseBooleanGracefully(
                                doc.get("isVeg") ?: doc.get("is_veg") ?: doc.get("veg"),
                                defaultVal = true
                            )

                            // Do NOT filter out items if inStock is missing or null.
                            val inStockVal = parseInStockGracefully(
                                doc.get("inStock") ?: doc.get("in_stock") ?: doc.get("isAvailable") ?: doc.get("is_available")
                            )

                            val imageVal = (doc.getString("imageUrl") 
                                ?: doc.getString("image") 
                                ?: doc.getString("bannerUrl") 
                                ?: (data?.get("imageUrl") as? String) 
                                ?: "")

                            val descVal = (doc.getString("description") 
                                ?: (data?.get("description") as? String) 
                                ?: "")

                            val rawRating = doc.get("rating")
                            val ratingVal = parseDoubleGracefully(rawRating, 4.8)

                            FirestoreDishItem(
                                id = doc.id,
                                restaurantId = targetRestId,
                                name = nameVal,
                                category = categoryVal,
                                price = priceVal,
                                isVeg = isVegVal,
                                inStock = inStockVal,
                                imageUrl = imageVal,
                                description = descVal,
                                rating = ratingVal
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreDebug", "Error parsing menu item doc ${doc.id}: ${e.message}", e)
                            FirestoreDishItem(
                                id = doc.id,
                                restaurantId = targetRestId,
                                name = "Dish ${doc.id}",
                                category = "Popular",
                                price = 0.0,
                                isVeg = true,
                                inStock = true,
                                imageUrl = "",
                                description = "",
                                rating = 4.8
                            )
                        }
                    }
                    Log.d("FirestoreDebug", "Emitting ${dishes.size} menu items for restaurant $targetRestId")
                    trySend(dishes)
                } else {
                    Log.d("FirestoreDebug", "Snapshot is empty for /restaurants/$targetRestId/menu")
                    trySend(emptyList())
                }
            }

            menuListenerRegistration = listener

            awaitClose {
                Log.d("FirestoreDebug", "Closing snapshot listener for /restaurants/$targetRestId/menu")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Exception in getRealtimeMenu: ${e.message}", e)
            trySend(emptyList())
            awaitClose { }
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


}

