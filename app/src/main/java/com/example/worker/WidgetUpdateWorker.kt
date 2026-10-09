package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.widget.DollarAppWidgetProvider
import com.example.widget.GoldAppWidgetProvider
import com.example.widget.MarketAppWidgetProvider
import com.example.widget.WidgetRatesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker responsible for reliable 15-30 minute periodic
 * syncing of live currency, gold, and coin exchange rates, and updating all
 * active home-screen widgets (Market, Dollar, Gold).
 */
class WidgetUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch live rates from TGJU / APIs and update SharedPreferences cache
            WidgetRatesManager.fetchAndStoreLiveRates(appContext)

            // 2. Broadcast updates to all widgets on the home screen
            MarketAppWidgetProvider.updateAllWidgets(appContext)
            DollarAppWidgetProvider.updateAllDollarWidgets(appContext)
            GoldAppWidgetProvider.updateAllGoldWidgets(appContext)

            Result.success()
        } catch (e: Exception) {
            // Even if network fails on this turn, refresh widgets with whatever cache we have
            try {
                MarketAppWidgetProvider.updateAllWidgets(appContext)
                DollarAppWidgetProvider.updateAllDollarWidgets(appContext)
                GoldAppWidgetProvider.updateAllGoldWidgets(appContext)
            } catch (_: Exception) {}

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "periodic_widget_update_work"

        /**
         * Enqueues a periodic WorkManager task running every 15 minutes (Android WorkManager min period)
         * with network connectivity constraint to ensure battery-friendly reliable background updates.
         */
        fun enqueuePeriodicWork(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicWorkRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                    15, TimeUnit.MINUTES,
                    5, TimeUnit.MINUTES // flex interval
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
            } catch (e: Exception) {
                // Ignore any initial work setup issues
            }
        }

        /**
         * Triggers an immediate one-time sync task if needed.
         */
        fun triggerImmediateSync(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val oneTimeWork = androidx.work.OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueue(oneTimeWork)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
