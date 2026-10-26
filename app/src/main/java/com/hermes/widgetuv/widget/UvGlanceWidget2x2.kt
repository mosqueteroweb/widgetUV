package com.hermes.widgetuv.widget

import com.hermes.widgetuv.data.model.formatUv
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.hermes.widgetuv.data.LanguageManager
import com.hermes.widgetuv.data.model.UvCurrentInfo
import com.hermes.widgetuv.data.model.UvHourlyPoint
import com.hermes.widgetuv.data.model.UvRiskLevel
import com.hermes.widgetuv.data.repository.UvRepository
import com.hermes.widgetuv.ui.MainActivity
import com.hermes.widgetuv.worker.UvScheduleManager
import java.time.LocalDateTime
import java.util.Locale

class UvGlanceWidget2x2 : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = UvRepository(context)
        val (info, hourly) = repository.getCurrentUvInfo(forceGpsSync = false)

        provideContent {
            val prefs = currentState<Preferences>()
            val isEnglish = prefs[WidgetKeys.IS_ENGLISH] ?: LanguageManager.isEnglish(context)
            UvWidget2x2Content(isEnglish = isEnglish, info = info, hourlyList = hourly)
        }
    }

    @Composable
    private fun UvWidget2x2Content(isEnglish: Boolean, info: UvCurrentInfo, hourlyList: List<UvHourlyPoint>) {
        val risk = info.riskLevel
        val riskColor = Color(android.graphics.Color.parseColor(risk.hexColor))
        val darkCardBg = Color(0xDD121316)
        val textSecondary = Color(0xFF94A3B8)
        val currentHour = LocalDateTime.now().hour

        // Center 5 hours around the current hour
        val currentIndex = hourlyList.indexOfFirst {
            val h = try { it.hourLabel.split(":")[0].toInt() } catch (e: Exception) { -1 }
            h == currentHour
        }

        val startIndex = when {
            currentIndex < 0 -> 0
            currentIndex - 2 < 0 -> 0
            currentIndex + 3 > hourlyList.size -> (hourlyList.size - 5).coerceAtLeast(0)
            else -> currentIndex - 2
        }
        val displayHours = hourlyList.drop(startIndex).take(5)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(darkCardBg))
                .cornerRadius(24.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header (Province + Current Time)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${info.locationName}",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = String.format(Locale.US, "%02d:00", currentHour),
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Center Value & Risk Badge
                Text(
                    text = formatUv(info.currentUv, isEnglish),
                    style = TextStyle(
                        color = ColorProvider(riskColor),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(riskColor))
                        .cornerRadius(6.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = risk.getLabel(isEnglish).uppercase(Locale.getDefault()),
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                val peakPrefix = if (isEnglish) "Peak:" else "Pico hoy:"
                Text(
                    text = "$peakPrefix ${formatUv(info.maxTodayUv, isEnglish)} (${info.maxTime})",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Hourly Mini Evolution Row Centered on Current Hour
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayHours.forEach { item ->
                        val itemRisk = UvRiskLevel.fromValue(item.uv)
                        val itemColor = Color(android.graphics.Color.parseColor(itemRisk.hexColor))
                        val isThisHour = item.hourLabel.startsWith(String.format(Locale.US, "%02d", currentHour))
                        val hourShort = item.hourLabel.split(":")[0] + "h"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier
                                .padding(horizontal = 3.dp)
                                .then(
                                    if (isThisHour) GlanceModifier
                                        .background(ColorProvider(Color(0x33F59E0B)))
                                        .cornerRadius(6.dp)
                                        .padding(horizontal = 3.dp, vertical = 2.dp)
                                    else GlanceModifier
                                )
                        ) {
                            Text(
                                text = hourShort,
                                style = TextStyle(
                                    color = if (isThisHour) ColorProvider(Color(0xFFF59E0B)) else ColorProvider(textSecondary),
                                    fontSize = 9.sp,
                                    fontWeight = if (isThisHour) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Box(
                                modifier = GlanceModifier
                                    .size(6.dp)
                                    .background(ColorProvider(itemColor))
                                    .cornerRadius(3.dp)
                            ) {}
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = formatUv(item.uv, isEnglish),
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Bottom Protection Tip Capsule
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color(0x33FFFFFF)))
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    val tip = if (isEnglish) {
                        when (risk) {
                            UvRiskLevel.LOW -> "🟢 Safe outdoors with no risk"
                            UvRiskLevel.MODERATE -> "🟡 Wear SPF 30 & sunglasses"
                            UvRiskLevel.HIGH -> "🟠 SPF 50+ & seek midday shade"
                            UvRiskLevel.VERY_HIGH, UvRiskLevel.EXTREME -> "🔴 Avoid direct midday sun exposure"
                        }
                    } else {
                        when (risk) {
                            UvRiskLevel.LOW -> "🟢 Sin riesgo al aire libre"
                            UvRiskLevel.MODERATE -> "🟡 Usar FPS 30 y gafas"
                            UvRiskLevel.HIGH -> "🟠 FPS 50+ y buscar sombra"
                            UvRiskLevel.VERY_HIGH, UvRiskLevel.EXTREME -> "🔴 Evitar sol del mediodía"
                        }
                    }
                    Text(
                        text = tip,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

class UvWidget2x2Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UvGlanceWidget2x2()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        UvScheduleManager.setupAllSchedules(context)
    }
}
