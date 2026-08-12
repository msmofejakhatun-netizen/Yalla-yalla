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
    fun getRealtimeRestaurants(): Flow<List<FirestoreRestaurantItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                Log.e("FirestoreDebug", "Firestore instance is null in getRealtimeRestaurants")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val query = db.collection("restaurants")
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDebug", "Firestore restaurants listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docCount = snapshot?.documents?.size ?: 0
                Log.d("FirestoreDebug", "Fetched $docCount restaurants")

                if (snapshot != null && !snapshot.isEmpty) {
                    val rests = snapshot.documents.mapNotNull { doc ->
                        try {
                            var item = doc.toObject(FirestoreRestaurantItem::class.java)
                            val data = doc.data
                            if (item == null || data != null) {
                                val nameVal = (doc.getString("name") 
                                    ?: doc.getString("title") 
                                    ?: doc.getString("restaurantName") 
                                    ?: item?.name ?: "").ifBlank { "Restaurant ${doc.id}" }
                                val cuisineVal = (doc.getString("cuisine") 
                                    ?: doc.getString("category") 
                                    ?: item?.cuisine ?: "Multi-Cuisine")
                                val ratingVal = (doc.get("rating") as? Number)?.toDouble() ?: item?.rating ?: 4.5
                                val deliveryVal = (doc.get("deliveryTimeMins") as? Number)?.toInt() 
                                    ?: (doc.get("delivery_time") as? Number)?.toInt() 
                                    ?: (doc.get("deliveryTime") as? Number)?.toInt() 
                                    ?: item?.deliveryTimeMins ?: 25
                                val priceVal = (doc.get("priceForTwo") as? Number)?.toDouble() 
                                    ?: (doc.get("price_for_two") as? Number)?.toDouble() 
                                    ?: (doc.get("price") as? Number)?.toDouble() 
                                    ?: item?.priceForTwo ?: 300.0
                                val addressVal = (doc.getString("address") ?: doc.getString("location") ?: item?.address ?: "")
                                val imageVal = (doc.getString("imageUrl") ?: doc.getString("image") ?: doc.getString("bannerUrl") ?: item?.imageUrl ?: "")
                                val isPromotedVal = (doc.getBoolean("isPromoted") ?: doc.getBoolean("is_promoted") ?: item?.isPromoted ?: false)
                                val isOpenVal = (doc.getBoolean("isOpen") ?: doc.getBoolean("is_open") ?: doc.getBoolean("isActive") ?: doc.getBoolean("is_active") ?: item?.isOpen ?: true)

                                item = FirestoreRestaurantItem(
                                    id = doc.id,
                                    name = nameVal,
                                    cuisine = cuisineVal,
                                    rating = ratingVal,
                                    deliveryTimeMins = deliveryVal,
                                    priceForTwo = priceVal,
                                    address = addressVal,
                                    imageUrl = imageVal,
                                    isPromoted = isPromotedVal,
                                    isOpen = isOpenVal
                                )
                            } else {
                                item.apply {
                                    if (id.isEmpty()) id = doc.id
                                }
                            }
                            Log.d("FirestoreDebug", "Parsed restaurant: ${item.id} -> ${item.name}")
                            item
                        } catch (e: Exception) {
                            Log.e("FirestoreDebug", "Failed to deserialize restaurant doc ${doc.id}: ${e.message}", e)
                            val data = doc.data
                            if (data != null) {
                                FirestoreRestaurantItem(
                                    id = doc.id,
                                    name = (data["name"] as? String) ?: (data["title"] as? String) ?: "Restaurant ${doc.id}",
                                    cuisine = (data["cuisine"] as? String) ?: "Multi-Cuisine",
                                    rating = (data["rating"] as? Number)?.toDouble() ?: 4.5,
                                    deliveryTimeMins = (data["deliveryTimeMins"] as? Number)?.toInt() ?: 25,
                                    priceForTwo = (data["priceForTwo"] as? Number)?.toDouble() ?: 300.0,
                                    address = (data["address"] as? String) ?: "",
                                    imageUrl = (data["imageUrl"] as? String) ?: (data["image"] as? String) ?: "",
                                    isPromoted = (data["isPromoted"] as? Boolean) ?: false,
                                    isOpen = (data["isOpen"] as? Boolean) ?: true
                                )
                            } else null
                        }
                    }
                    Log.d("FirestoreDebug", "Successfully mapped ${rests.size} valid restaurants out of $docCount documents")
                    trySend(rests)
                } else {
                    Log.d("FirestoreDebug", "Snapshot is null or empty for /restaurants")
                    trySend(emptyList())
                }
            }
            restaurantListenerRegistration = listener
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Exception in getRealtimeRestaurants: ${e.message}", e)
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
     * Real-Time Firestore Menu Flow
     * Listens to `/restaurants/{restaurantId}/menu` with active addSnapshotListener
     */
    fun getRealtimeMenu(restaurantId: String = "rest_yalla_1"): Flow<List<FirestoreDishItem>> = callbackFlow {
        try {
            val db = firestore
            if (db == null) {
                Log.e("FirestoreDebug", "Firestore instance is null in getRealtimeMenu")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val targetRestId = if (restaurantId.isBlank()) "rest_yalla_1" else restaurantId
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
                            // Treat inStock == true or missing/null as valid available items.
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
                            null
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

