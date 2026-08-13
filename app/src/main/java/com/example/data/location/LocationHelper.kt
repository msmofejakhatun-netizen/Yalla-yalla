package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class LocationData(
    val fullAddress: String,
    val area: String,
    val city: String,
    val pincode: String,
    val latitude: Double,
    val longitude: Double
)

class LocationHelper(private val context: Context) {

    /**
     * Checks whether Location/GPS service is enabled on the device.
     */
    fun isLocationEnabled(): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? = withContext(Dispatchers.IO) {
        var location: Location? = null

        // 1. Query Android System LocationManager across enabled providers
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )

                // Try cached last known locations first
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

                // If cached location is null, request a single location update with timeout
                if (location == null) {
                    val activeProvider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> null
                    }

                    if (activeProvider != null) {
                        location = withTimeoutOrNull(5000L) {
                            suspendCancellableCoroutine { continuation ->
                                val listener = object : LocationListener {
                                    override fun onLocationChanged(loc: Location) {
                                        try {
                                            locationManager.removeUpdates(this)
                                        } catch (e: Exception) { }
                                        if (continuation.isActive) continuation.resume(loc)
                                    }
                                    @Deprecated("Deprecated in API 29")
                                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                                    override fun onProviderEnabled(provider: String) {}
                                    override fun onProviderDisabled(provider: String) {
                                        if (continuation.isActive) continuation.resume(null)
                                    }
                                }

                                try {
                                    locationManager.requestSingleUpdate(activeProvider, listener, context.mainLooper)
                                } catch (e: Exception) {
                                    if (continuation.isActive) continuation.resume(null)
                                }

                                continuation.invokeOnCancellation {
                                    try {
                                        locationManager.removeUpdates(listener)
                                    } catch (e: Exception) { }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.w("LocationHelper", "LocationManager query error: ${e.message}")
        }

        // 2. Perform Reverse Geocoding on IO Dispatcher or return formatted location data
        if (location != null) {
            val address = reverseGeocode(location.latitude, location.longitude)
            if (address != null) {
                val area = address.subLocality
                    ?: address.thoroughfare
                    ?: address.subThoroughfare
                    ?: address.featureName
                    ?: "Current Location"
                val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Bengaluru"
                val pincode = address.postalCode ?: "560038"
                val fullAddress = if (pincode.isNotBlank()) "📍 $area, $city - $pincode" else "📍 $area, $city"
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
            Log.i("LocationHelper", "No GPS location returned, using default Bengaluru location")
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
            Log.e("LocationHelper", "Reverse geocoding error: ${e.message}")
            null
        }
    }
}
