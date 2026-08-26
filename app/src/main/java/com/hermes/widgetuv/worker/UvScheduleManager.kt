package com.hermes.widgetuv.worker

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object UvScheduleManager {
    private const val WORK_7AM = "uv_sync_7am"
    private const val WORK_2PM = "uv_sync_2pm"
    private const val WORK_HOURLY = "uv_hourly_local_refresh"

    fun setupAllSchedules(context: Context) {
        scheduleDailySyncs(context)
        scheduleHourlyLocalRefresh(context)
    }

    fun scheduleDailySyncs(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // 1. Schedule 7:00 AM Worker
        val delay7am = calculateDelayToNext(7, 0)
        val request7am = OneTimeWorkRequestBuilder<UvSyncWorker>()
            .setInitialDelay(delay7am.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("SLOT_TAG" to "7:00 AM"))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(WORK_7AM, ExistingWorkPolicy.REPLACE, request7am)

        // 2. Schedule 2:00 PM (14:00) Worker
        val delay2pm = calculateDelayToNext(14, 0)
        val request2pm = OneTimeWorkRequestBuilder<UvSyncWorker>()
            .setInitialDelay(delay2pm.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("SLOT_TAG" to "2:00 PM"))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(WORK_2PM, ExistingWorkPolicy.REPLACE, request2pm)
    }

    private fun scheduleHourlyLocalRefresh(context: Context) {
        val workManager = WorkManager.getInstance(context)
        // Local widget updates every hour (no network constraint, no GPS)
        val request = PeriodicWorkRequestBuilder<UvHourlyLocalRefreshWorker>(1, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WORK_HOURLY,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun calculateDelayToNext(targetHour: Int, targetMinute: Int): Duration {
        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(targetHour, targetMinute, 0))
        if (now.isAfter(target)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target)
    }

    fun cancelAll(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_7AM)
        workManager.cancelUniqueWork(WORK_2PM)
        workManager.cancelUniqueWork(WORK_HOURLY)
    }
}
