package com.hermes.widgetuv.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double,
    val label: String
)

class LocationHelper(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // Fallback default coordinates (Madrid, Spain)
    val defaultLocation = GeoCoordinates(40.4168, -3.7038, "Madrid")

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoCoordinates {
        if (!hasLocationPermission()) {
            return getSavedOrDefault()
        }

        return try {
            val location: Location? = withTimeoutOrNull(5000) {
                // Try last known location first for instantaneous widget refresh
                val lastKnown = fusedClient.lastLocation.await()
                if (lastKnown != null && (System.currentTimeMillis() - lastKnown.time) < 15 * 60 * 1000) {
                    lastKnown
                } else {
                    val cts = CancellationTokenSource()
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cts.token
                    ).await()
                }
            }

            if (location != null) {
                GeoCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    label = "Ubicación actual"
                )
            } else {
                getSavedOrDefault()
            }
        } catch (e: Exception) {
            getSavedOrDefault()
        }
    }

    private fun getSavedOrDefault(): GeoCoordinates {
        val prefs = context.getSharedPreferences("widget_uv_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("custom_lat", defaultLocation.latitude.toFloat()).toDouble()
        val lon = prefs.getFloat("custom_lon", defaultLocation.longitude.toFloat()).toDouble()
        val label = prefs.getString("custom_label", defaultLocation.label) ?: defaultLocation.label
        return GeoCoordinates(lat, lon, label)
    }

    fun saveCustomLocation(lat: Double, lon: Double, label: String) {
        val prefs = context.getSharedPreferences("widget_uv_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat("custom_lat", lat.toFloat())
            .putFloat("custom_lon", lon.toFloat())
            .putString("custom_label", label)
            .apply()
    }
}
