package com.hermes.widgetuv.data.repository

import android.content.Context
import com.google.gson.Gson
import com.hermes.widgetuv.data.api.OpenMeteoClient
import com.hermes.widgetuv.data.location.LocationHelper
import com.hermes.widgetuv.data.model.CachedDayForecast
import com.hermes.widgetuv.data.model.UvCurrentInfo
import com.hermes.widgetuv.data.model.UvHourlyPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class UvRepository(private val context: Context) {
    private val apiClient = OpenMeteoClient()
    private val locationHelper = LocationHelper(context)
    private val prefs = context.getSharedPreferences("widget_uv_forecast_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Reads current UV:
     * - Evaluates local hourly cached curve if available for today.
     * - Does NOT activate GPS/Network unless explicitly forcing sync or scheduled slot (7am / 2pm).
     */
    suspend fun getCurrentUvInfo(forceGpsSync: Boolean = false, slotTag: String = "MANUAL"): Pair<UvCurrentInfo, List<UvHourlyPoint>> {
        val todayStr = LocalDate.now().toString()
        val cachedJson = prefs.getString("day_forecast", null)
        var forecast: CachedDayForecast? = null

        if (cachedJson != null) {
            try {
                val parsed = gson.fromJson(cachedJson, CachedDayForecast::class.java)
                if (parsed.dateIso == todayStr) {
                    forecast = parsed
                }
            } catch (e: Exception) {
                forecast = null
            }
        }

        // If no cache for today or force sync requested (7am, 2pm or manual button), query GPS & API
        if (forecast == null || forceGpsSync) {
            val syncResult = syncWithGpsAndApi(slotTag)
            if (syncResult != null) {
                forecast = syncResult
            }
        }

        val currentHour = LocalDateTime.now().hour
        if (forecast != null && forecast.hourlyUv.isNotEmpty()) {
            val currentUv = forecast.hourlyUv.getOrElse(currentHour) { 0.0 }
            val isDay = currentUv > 0.0 || (currentHour in 7..20)

            val info = UvCurrentInfo(
                currentUv = currentUv,
                maxTodayUv = forecast.maxTodayUv,
                maxTime = forecast.maxTime,
                isDay = isDay,
                locationName = forecast.locationLabel,
                syncSlot = forecast.syncSlot,
                lastUpdated = forecast.lastSyncTimestamp
            )

            val hourlyList = forecast.hourlyUv.mapIndexed { index, uv ->
                val label = forecast.hourlyLabels.getOrElse(index) { String.format(Locale.US, "%02d:00", index) }
                UvHourlyPoint(
                    timeIso = "${forecast.dateIso}T$label",
                    hourLabel = label,
                    uv = uv
                )
            }

            return Pair(info, hourlyList)
        }

        // Fallback if offline and no cache
        val fallback = UvCurrentInfo(
            currentUv = 0.0,
            maxTodayUv = 0.0,
            maxTime = "--:--",
            isDay = false,
            locationName = "Sin datos"
        )
        return Pair(fallback, emptyList())
    }

    /**
     * Executes the GPS reading & Open-Meteo API query.
     * Only called at 7:00 AM, 2:00 PM, or on manual refresh.
     */
    suspend fun syncWithGpsAndApi(slotTag: String): CachedDayForecast? {
        val location = locationHelper.getCurrentLocation()
        val result = apiClient.fetchUvData(location.latitude, location.longitude, location.label)

        if (result.isSuccess) {
            val (info, hourly) = result.getOrThrow()
            val todayStr = LocalDate.now().toString()

            val cached = CachedDayForecast(
                dateIso = todayStr,
                hourlyUv = hourly.map { it.uv },
                hourlyLabels = hourly.map { it.hourLabel },
                maxTodayUv = info.maxTodayUv,
                maxTime = info.maxTime,
                locationLabel = location.label,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncSlot = slotTag
            )

            prefs.edit()
                .putString("day_forecast", gson.toJson(cached))
                .apply()

            return cached
        }

        return null
    }
}
