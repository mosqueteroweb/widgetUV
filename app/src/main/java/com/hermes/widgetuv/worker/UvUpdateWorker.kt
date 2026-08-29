package com.hermes.widgetuv.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hermes.widgetuv.data.repository.UvRepository
import com.hermes.widgetuv.widget.UvGlanceWidget
import com.hermes.widgetuv.widget.UvGlanceWidget2x1
import com.hermes.widgetuv.widget.UvGlanceWidget2x2

suspend fun updateAllGlanceWidgets(context: Context) {
    try { UvGlanceWidget().updateAll(context) } catch (e: Exception) {}
    try { UvGlanceWidget2x1().updateAll(context) } catch (e: Exception) {}
    try { UvGlanceWidget2x2().updateAll(context) } catch (e: Exception) {}
}

/**
 * Worker for GPS + Network Full Sync (Triggered at 7:00 AM and 2:00 PM)
 */
class UvSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = inputData.getString("SLOT_TAG") ?: "SCHEDULED"
        val repository = UvRepository(context)
        return try {
            repository.syncWithGpsAndApi(slot)
            updateAllGlanceWidgets(context)
            // Re-schedule next daily occurrences
            UvScheduleManager.scheduleDailySyncs(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Lightweight Local Widget Refresh (Updates hourly from cached curve without GPS or Network)
 */
class UvHourlyLocalRefreshWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            updateAllGlanceWidgets(context)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
