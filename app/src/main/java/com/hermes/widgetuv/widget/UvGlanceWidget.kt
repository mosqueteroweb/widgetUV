package com.hermes.widgetuv.widget

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
import com.hermes.widgetuv.data.repository.UvRepository
import com.hermes.widgetuv.ui.MainActivity
import java.util.Locale

class UvGlanceWidget : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = UvRepository(context)
        val (info, _) = repository.getCurrentUvInfo(forceGpsSync = false)

        provideContent {
            val prefs = currentState<Preferences>()
            val isEnglish = prefs[WidgetKeys.IS_ENGLISH] ?: LanguageManager.isEnglish(context)
            UvWidgetContent(isEnglish = isEnglish, info = info)
        }
    }

    @Composable
    private fun UvWidgetContent(isEnglish: Boolean, info: UvCurrentInfo) {
        val risk = info.riskLevel
        val riskColor = Color(android.graphics.Color.parseColor(risk.hexColor))
        val darkCardBg = Color(0xDD121316)
        val textSecondary = Color(0xFF94A3B8)

        // 1x1 Minimalist Layout for One UI / Android
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(darkCardBg))
                .cornerRadius(22.dp)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header (Icon + Title)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
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

                Spacer(modifier = GlanceModifier.height(1.dp))

                // UV Value (Large Number)
                Text(
                    text = String.format(Locale.US, "%.1f", info.currentUv),
                    style = TextStyle(
                        color = ColorProvider(riskColor),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                // Risk Level Badge
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(riskColor))
                        .cornerRadius(6.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = risk.getLabel(isEnglish).uppercase(Locale.getDefault()),
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(2.dp))

                // Peak UV
                val maxPrefix = if (isEnglish) "Peak" else "Máx"
                Text(
                    text = "$maxPrefix ${String.format(Locale.US, "%.1f", info.maxTodayUv)} (${info.maxTime})",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
