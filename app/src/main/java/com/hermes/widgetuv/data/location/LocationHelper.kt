package com.hermes.widgetuv.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

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
        return coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoCoordinates {
        if (!hasLocationPermission()) {
            return getSavedOrDefault()
        }

        return try {
            val location: Location? = withTimeoutOrNull(6000) {
                // Fetch fresh high-accuracy / balanced location
                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).await() ?: fusedClient.lastLocation.await()
            }

            if (location != null) {
                // Rounded to ~1 km before it is stored or sent to Open-Meteo / BigDataCloud: UV does not change within a town.
                val lat = toKilometre(location.latitude)
                val lon = toKilometre(location.longitude)
                val provinceName = getProvinceName(lat, lon)
                val coords = GeoCoordinates(
                    latitude = lat,
                    longitude = lon,
                    label = provinceName
                )
                saveCustomLocation(coords.latitude, coords.longitude, coords.label)
                coords
            } else {
                getSavedOrDefault()
            }
        } catch (e: Exception) {
            getSavedOrDefault()
        }
    }

    suspend fun getProvinceName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        // 1. Try Android Native Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("es", "ES"))
                val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var resultList: List<Address>? = null
                    val lock = Object()
                    var isDone = false
                    geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            resultList = addresses
                            synchronized(lock) {
                                isDone = true
                                lock.notifyAll()
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            synchronized(lock) {
                                isDone = true
                                lock.notifyAll()
                            }
                        }
                    })
                    synchronized(lock) {
                        if (!isDone) {
                            lock.wait(2500)
                        }
                    }
                    resultList
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lon, 1)
                }

                val address = addresses?.firstOrNull()
                if (address != null) {
                    val subAdmin = address.subAdminArea // Province in Spain (e.g. "Madrid", "Sevilla", "Barcelona")
                    val locality = address.locality
                    val admin = address.adminArea

                    val resolved = when {
                        !subAdmin.isNullOrBlank() -> subAdmin
                        !locality.isNullOrBlank() -> locality
                        !admin.isNullOrBlank() -> admin
                        else -> null
                    }
                    if (!resolved.isNullOrBlank()) {
                        return@withContext resolved
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to HTTP reverse geocoder
        }

        // 2. HTTP Reverse Geocoder Fallback (BigDataCloud Free Client API)
        try {
            val urlString = String.format(
                Locale.US,
                "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=%.2f&longitude=%.2f&localityLanguage=es",
                lat,
                lon
            )
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonStr = reader.readText()
                reader.close()
                val json = JSONObject(jsonStr)

                val principalSubdivision = json.optString("principalSubdivision", "")
                val city = json.optString("city", "")
                val locality = json.optString("locality", "")

                val result = when {
                    principalSubdivision.isNotBlank() -> principalSubdivision
                    city.isNotBlank() -> city
                    locality.isNotBlank() -> locality
                    else -> ""
                }
                if (result.isNotBlank()) {
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            // Ignore fallback failure
        }

        return@withContext "Ubicación actual"
    }

    private fun toKilometre(value: Double): Double = Math.round(value * 100.0) / 100.0

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
