package com.hermes.widgetuv.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.hermes.widgetuv.worker.UvScheduleManager

class UvWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UvGlanceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        UvScheduleManager.setupAllSchedules(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        UvScheduleManager.cancelAll(context)
    }
}
