package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationData(
    val fullAddress: String,
    val area: String,
    val city: String,
    val pincode: String,
    val latitude: Double,
    val longitude: Double
)

class LocationHelper(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? = withContext(Dispatchers.IO) {
        var location: android.location.Location? = null

        // Pure Android framework LocationManager (no GMS GoogleApiManager dependencies or broker errors)
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )
                for (provider in providers) {
                    try {
                        if (locationManager.isProviderEnabled(provider)) {
                            val lastLoc = locationManager.getLastKnownLocation(provider)
                            if (lastLoc != null) {
                                if (location == null || lastLoc.accuracy < location!!.accuracy) {
                                    location = lastLoc
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("LocationHelper", "Error checking provider $provider: ${e.message}")
                    }
                }
            }
        } catch (e: Throwable) {
            Log.w("LocationHelper", "LocationManager error: ${e.message}")
        }

        // Resolve address or return default Bengaluru hub
        if (location != null) {
            val address = reverseGeocode(location.latitude, location.longitude)
            if (address != null) {
                val area = address.subLocality ?: address.thoroughfare ?: address.featureName ?: "Indiranagar 100ft Rd"
                val city = address.locality ?: address.subAdminArea ?: "Bengaluru"
                val pincode = address.postalCode ?: "560038"
                val fullAddress = "📍 $area, $city - $pincode"
                LocationData(
                    fullAddress = fullAddress,
                    area = area,
                    city = city,
                    pincode = pincode,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                LocationData(
                    fullAddress = "📍 Koramangala 7th Block, Bengaluru - 560034",
                    area = "Koramangala 7th Block",
                    city = "Bengaluru",
                    pincode = "560034",
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        } else {
            Log.i("LocationHelper", "Returning default Bengaluru location")
            LocationData(
                fullAddress = "📍 Indiranagar 100ft Rd, Bengaluru - 560038",
                area = "Indiranagar 100ft Rd",
                city = "Bengaluru",
                pincode = "560038",
                latitude = 12.9784,
                longitude = 77.6408
            )
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): Address? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) addresses[0] else null
        } catch (e: Exception) {
            Log.e("LocationHelper", "Geocoding error: ${e.message}")
            null
        }
    }
}
