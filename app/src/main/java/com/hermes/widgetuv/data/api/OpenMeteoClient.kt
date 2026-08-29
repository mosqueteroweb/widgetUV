package com.hermes.widgetuv.data.api

import com.google.gson.Gson
import com.hermes.widgetuv.data.model.OpenMeteoParsedResult
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

    suspend fun fetchUvData(lat: Double, lon: Double, locationLabel: String = "Mi Ubicación"): Result<OpenMeteoParsedResult> =
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
    ): OpenMeteoParsedResult {
        val now = LocalDateTime.now()
        val currentHour = now.hour

        val hourlyTimes = response.hourly.time
        val hourlyUv = response.hourly.uv_index
        val hourlyIsDay = response.hourly.is_day

        val hourly24Uv = mutableListOf<Double>()
        val hourly24Labels = mutableListOf<String>()
        val displayList = mutableListOf<UvHourlyPoint>()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        var maxVal = -1.0
        var maxHour = 14

        // Populate full 24h day arrays
        for (i in 0 until 24) {
            val uv = if (i in hourlyUv.indices) hourlyUv[i] else 0.0
            val timeStr = if (i in hourlyTimes.indices) hourlyTimes[i] else String.format(Locale.US, "%02d:00", i)
            val label = try {
                val dt = LocalDateTime.parse(timeStr)
                dt.format(formatter)
            } catch (e: Exception) {
                String.format(Locale.US, "%02d:00", i)
            }

            hourly24Uv.add(uv)
            hourly24Labels.add(label)

            if (uv > maxVal) {
                maxVal = uv
                maxHour = i
            }

            // Filter for UI display: 7:00 to 22:00
            if (i in 7..22) {
                displayList.add(UvHourlyPoint(timeStr, label, uv))
            }
        }

        val currentUv = hourly24Uv.getOrElse(currentHour) { 0.0 }
        val maxTodayUv = if (maxVal >= 0.0) maxVal else (response.daily.uv_index_max.firstOrNull() ?: currentUv)
        val maxHourStr = String.format(Locale.getDefault(), "%02d:00", maxHour)

        val isDay = if (hourlyIsDay != null && currentHour in hourlyIsDay.indices) {
            hourlyIsDay[currentHour] == 1
        } else {
            currentUv > 0.0 || (currentHour in 7..21)
        }

        val currentInfo = UvCurrentInfo(
            currentUv = currentUv,
            maxTodayUv = maxTodayUv,
            maxTime = maxHourStr,
            isDay = isDay,
            locationName = locationLabel
        )

        return OpenMeteoParsedResult(
            currentInfo = currentInfo,
            hourly24Uv = hourly24Uv,
            hourly24Labels = hourly24Labels,
            displayList = displayList
        )
    }
}
