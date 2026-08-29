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

    suspend fun getCurrentUvInfo(forceGpsSync: Boolean = false, slotTag: String = "MANUAL"): Pair<UvCurrentInfo, List<UvHourlyPoint>> {
        val todayStr = LocalDate.now().toString()
        val cachedJson = prefs.getString("day_forecast_v2", null)
        var forecast: CachedDayForecast? = null

        if (cachedJson != null) {
            try {
                val parsed = gson.fromJson(cachedJson, CachedDayForecast::class.java)
                if (parsed.dateIso == todayStr && parsed.hourly24Uv.size == 24) {
                    forecast = parsed
                }
            } catch (e: Exception) {
                forecast = null
            }
        }

        // If no cache for today or force sync requested, query GPS & API
        if (forecast == null || forceGpsSync) {
            val syncResult = syncWithGpsAndApi(slotTag)
            if (syncResult != null) {
                forecast = syncResult
            }
        }

        val currentHour = LocalDateTime.now().hour
        if (forecast != null && forecast.hourly24Uv.size == 24) {
            // Direct index matching: currentHour (0..23) corresponds exactly to hourly24Uv[currentHour]
            val currentUv = forecast.hourly24Uv.getOrElse(currentHour) { 0.0 }
            val isDay = currentUv > 0.0 || (currentHour in 7..21)

            val info = UvCurrentInfo(
                currentUv = currentUv,
                maxTodayUv = forecast.maxTodayUv,
                maxTime = forecast.maxTime,
                isDay = isDay,
                locationName = forecast.locationLabel,
                syncSlot = forecast.syncSlot,
                lastUpdated = forecast.lastSyncTimestamp
            )

            // UI Curve: filter 7:00 AM (hour 7) to 22:00 PM (hour 22)
            val displayList = mutableListOf<UvHourlyPoint>()
            for (hour in 7..22) {
                val uv = forecast.hourly24Uv.getOrElse(hour) { 0.0 }
                val label = forecast.hourly24Labels.getOrElse(hour) { String.format(Locale.US, "%02d:00", hour) }
                displayList.add(
                    UvHourlyPoint(
                        timeIso = "${forecast.dateIso}T$label",
                        hourLabel = label,
                        uv = uv
                    )
                )
            }

            return Pair(info, displayList)
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

    suspend fun syncWithGpsAndApi(slotTag: String): CachedDayForecast? {
        val location = locationHelper.getCurrentLocation()
        val result = apiClient.fetchUvData(location.latitude, location.longitude, location.label)

        if (result.isSuccess) {
            val parsed = result.getOrThrow()
            val todayStr = LocalDate.now().toString()

            val cached = CachedDayForecast(
                dateIso = todayStr,
                hourly24Uv = parsed.hourly24Uv,
                hourly24Labels = parsed.hourly24Labels,
                maxTodayUv = parsed.currentInfo.maxTodayUv,
                maxTime = parsed.currentInfo.maxTime,
                locationLabel = location.label,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncSlot = slotTag
            )

            prefs.edit()
                .putString("day_forecast_v2", gson.toJson(cached))
                .apply()

            return cached
        }

        return null
    }
}
