package com.hermes.widgetuv.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.hermes.widgetuv.data.model.UvCurrentInfo
import com.hermes.widgetuv.data.repository.UvRepository
import com.hermes.widgetuv.ui.MainActivity
import java.util.Locale

class UvGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = UvRepository(context)
        val (info, _) = repository.getCurrentUvInfo(forceGpsSync = false)

        provideContent {
            UvWidgetContent(context = context, info = info)
        }
    }

    @Composable
    private fun UvWidgetContent(context: Context, info: UvCurrentInfo) {
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
                        fontSize = 26.sp,
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
                        text = risk.label.uppercase(Locale.getDefault()),
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(3.dp))

                // Peak info of today
                Text(
                    text = "Máx ${String.format(Locale.US, "%.1f", info.maxTodayUv)} (${info.maxTime})",
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
