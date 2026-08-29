package com.hermes.widgetuv.data.model

import androidx.compose.ui.graphics.Color

enum class UvRiskLevel(
    val label: String,
    val hexColor: String,
    val color: Color,
    val advice: String
) {
    LOW("Bajo", "#43A047", Color(0xFF43A047), "Sin riesgo. Seguro al aire libre."),
    MODERATE("Moderado", "#FDD835", Color(0xFFFDD835), "Usa protector solar y gafas."),
    HIGH("Alto", "#FB8C00", Color(0xFFFB8C00), "Busca sombra en horas centrales."),
    VERY_HIGH("Muy Alto", "#E53935", Color(0xFFE53935), "Evita el sol del mediodía."),
    EXTREME("Extremo", "#8E24AA", Color(0xFF8E24AA), "Peligro severo. Quédate en interiores.");

    companion object {
        fun fromValue(value: Double): UvRiskLevel = when {
            value < 3.0 -> LOW
            value < 6.0 -> MODERATE
            value < 8.0 -> HIGH
            value < 11.0 -> VERY_HIGH
            else -> EXTREME
        }
    }
}

data class UvHourlyPoint(
    val timeIso: String,
    val hourLabel: String,
    val uv: Double
)

data class CachedDayForecast(
    val dateIso: String,
    val hourly24Uv: List<Double>,
    val hourly24Labels: List<String>,
    val maxTodayUv: Double,
    val maxTime: String,
    val locationLabel: String,
    val lastSyncTimestamp: Long,
    val syncSlot: String
)

data class UvCurrentInfo(
    val currentUv: Double,
    val maxTodayUv: Double,
    val maxTime: String,
    val isDay: Boolean,
    val locationName: String,
    val syncSlot: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val riskLevel: UvRiskLevel
        get() = UvRiskLevel.fromValue(currentUv)

    val maxRiskLevel: UvRiskLevel
        get() = UvRiskLevel.fromValue(maxTodayUv)
}

data class OpenMeteoHourly(
    val time: List<String>,
    val uv_index: List<Double>,
    val is_day: List<Int>?
)

data class OpenMeteoDaily(
    val time: List<String>,
    val uv_index_max: List<Double>
)

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: OpenMeteoHourly,
    val daily: OpenMeteoDaily
)

data class OpenMeteoParsedResult(
    val currentInfo: UvCurrentInfo,
    val hourly24Uv: List<Double>,
    val hourly24Labels: List<String>,
    val displayList: List<UvHourlyPoint>
)
