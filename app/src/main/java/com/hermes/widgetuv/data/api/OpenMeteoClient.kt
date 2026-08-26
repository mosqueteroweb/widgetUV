package com.hermes.widgetuv.data.api

import com.google.gson.Gson
import com.hermes.widgetuv.data.model.OpenMeteoResponse
import com.hermes.widgetuv.data.model.UvCurrentInfo
import com.hermes.widgetuv.data.model.UvHourlyPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class OpenMeteoClient {
    private val gson = Gson()

    suspend fun fetchUvData(lat: Double, lon: Double, locationLabel: String = "Mi Ubicación"): Result<Pair<UvCurrentInfo, List<UvHourlyPoint>>> =
        withContext(Dispatchers.IO) {
            try {
                val urlString = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&hourly=uv_index,is_day&daily=uv_index_max&timezone=auto&forecast_days=1",
                    lat,
                    lon
                )
                val url = URL(urlString)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/json")
                }

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val json = reader.readText()
                    reader.close()

                    val response = gson.fromJson(json, OpenMeteoResponse::class.java)
                    val result = parseUvInfo(response, locationLabel)
                    Result.success(result)
                } else {
                    Result.failure(Exception("HTTP error ${conn.responseCode}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseUvInfo(
        response: OpenMeteoResponse,
        locationLabel: String
    ): Pair<UvCurrentInfo, List<UvHourlyPoint>> {
        val now = LocalDateTime.now()
        val currentHour = now.hour

        val hourlyTimes = response.hourly.time
        val hourlyUv = response.hourly.uv_index
        val hourlyIsDay = response.hourly.is_day

        // Current UV at current hour
        val currentUv = if (currentHour in hourlyUv.indices) hourlyUv[currentHour] else 0.0
        val isDay = if (hourlyIsDay != null && currentHour in hourlyIsDay.indices) {
            hourlyIsDay[currentHour] == 1
        } else {
            currentUv > 0.0
        }

        // Daily Max UV and time
        val maxTodayUv = response.daily.uv_index_max.firstOrNull() ?: currentUv
        
        // Find hour of maximum UV
        var maxIndex = 0
        var maxVal = -1.0
        for (i in hourlyUv.indices) {
            if (hourlyUv[i] > maxVal) {
                maxVal = hourlyUv[i]
                maxIndex = i
            }
        }

        val maxHourStr = if (maxIndex in hourlyTimes.indices) {
            try {
                val parsed = LocalDateTime.parse(hourlyTimes[maxIndex])
                String.format(Locale.getDefault(), "%02d:00", parsed.hour)
            } catch (e: Exception) {
                String.format(Locale.getDefault(), "%02d:00", maxIndex)
            }
        } else "14:00"

        val currentInfo = UvCurrentInfo(
            currentUv = currentUv,
            maxTodayUv = maxTodayUv,
            maxTime = maxHourStr,
            isDay = isDay,
            locationName = locationLabel
        )

        val hourlyList = mutableListOf<UvHourlyPoint>()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        for (i in hourlyTimes.indices) {
            val uv = hourlyUv.getOrElse(i) { 0.0 }
            val label = try {
                val dt = LocalDateTime.parse(hourlyTimes[i])
                dt.format(formatter)
            } catch (e: Exception) {
                "$i:00"
            }
            hourlyList.add(UvHourlyPoint(hourlyTimes[i], label, uv))
        }

        return Pair(currentInfo, hourlyList)
    }
}
