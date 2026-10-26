package com.hermes.widgetuv.widget

import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
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

class UvGlanceWidget2x1 : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = UvRepository(context)
        val (info, hourly) = repository.getCurrentUvInfo(forceGpsSync = false)

        provideContent {
            val prefs = currentState<Preferences>()
            val isEnglish = prefs[WidgetKeys.IS_ENGLISH] ?: LanguageManager.isEnglish(context)
            UvWidget2x1Content(isEnglish = isEnglish, info = info, hourlyList = hourly)
        }
    }

    @Composable
    private fun UvWidget2x1Content(isEnglish: Boolean, info: UvCurrentInfo, hourlyList: List<UvHourlyPoint>) {
        val risk = info.riskLevel
        val riskColor = Color(android.graphics.Color.parseColor(risk.hexColor))
        val darkCardBg = Color(0xDD121316)
        val textSecondary = Color(0xFF94A3B8)
        val currentHour = LocalDateTime.now().hour

        // Filter upcoming 4 hours starting from current hour
        val upcomingHours = hourlyList.filter {
            val hour = try { it.hourLabel.split(":")[0].toInt() } catch (e: Exception) { -1 }
            hour >= currentHour
        }.take(4)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(darkCardBg))
                .cornerRadius(22.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity<MainActivity>())
                .semantics {
                    contentDescription = risk.spokenDescription(formatUv(info.currentUv, isEnglish), isEnglish) +
                        (if (isEnglish) ". Peak ${formatUv(info.maxTodayUv, isEnglish)} at ${info.maxTime}" else ". Máximo ${formatUv(info.maxTodayUv, isEnglish)} a las ${info.maxTime}")
                }
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Block: Current UV Value and Risk
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "☀️ UV",
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Text(
                        text = formatUv(info.currentUv, isEnglish),
                        style = TextStyle(
                            color = ColorProvider(risk.textColor(onDark = true)),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )

                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(riskColor))
                            .cornerRadius(6.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = risk.getLabel(isEnglish).uppercase(Locale.getDefault()),
                            style = TextStyle(
                                color = ColorProvider(risk.badgeTextColor),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Vertical Divider
                Spacer(
                    modifier = GlanceModifier
                        .width(1.dp)
                        .height(55.dp)
                        .background(ColorProvider(Color(0x33FFFFFF)))
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Right Block: Upcoming Mini Forecast + Daily Peak
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 4 Mini Hourly Pills
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayItems = if (upcomingHours.isNotEmpty()) upcomingHours else hourlyList.take(4)
                        displayItems.forEach { item ->
                            val itemRisk = UvRiskLevel.fromValue(item.uv)
                            val itemColor = Color(android.graphics.Color.parseColor(itemRisk.hexColor))
                            val hourShort = item.hourLabel.split(":")[0] + "h"

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = GlanceModifier.padding(horizontal = 3.dp)
                            ) {
                                Text(
                                    text = hourShort,
                                    style = TextStyle(
                                        color = ColorProvider(textSecondary),
                                        fontSize = 10.sp
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
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Peak Info
                    val peakLabel = if (isEnglish) "Peak" else "Máx"
                    Text(
                        text = "$peakLabel ${formatUv(info.maxTodayUv, isEnglish)} (${info.maxTime})",
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

class UvWidget2x1Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UvGlanceWidget2x1()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        UvScheduleManager.setupAllSchedules(context)
    }
}
