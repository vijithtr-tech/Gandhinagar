package com.gandhinagar.committee.export

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BackupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = try {
        ExcelExporter.export(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        const val WORK_NAME = "daily_backup_8pm"

        /** Schedules a daily backup at 20:00 (8 PM). */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }
            val initialDelay = next.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
