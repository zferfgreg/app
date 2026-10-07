package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.util.Formatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class GoldAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_GOLD_WIDGET) {
            CoroutineScope(Dispatchers.IO).launch {
                WidgetRatesManager.fetchAndStoreLiveRates(context)
                updateAllGoldWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_GOLD_WIDGET = "com.example.action.REFRESH_GOLD_WIDGET"

        fun updateAllGoldWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, GoldAppWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (widgetId in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        fun requestPinGoldWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val myProvider = ComponentName(context, GoldAppWidgetProvider::class.java)
                    if (appWidgetManager.isRequestPinAppWidgetSupported) {
                        val successCallback = PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, GoldAppWidgetProvider::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        return appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                    }
                } catch (e: Exception) {
                    return false
                }
            }
            return false
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val initialRates = WidgetRatesManager.getRates(context)
                val views = buildRemoteViews(context, initialRates)
                appWidgetManager.updateAppWidget(appWidgetId, views)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val liveRates = WidgetRatesManager.fetchAndStoreLiveRates(context)
                        val updatedViews = buildRemoteViews(context, liveRates)
                        appWidgetManager.updateAppWidget(appWidgetId, updatedViews)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        private fun buildRemoteViews(context: Context, rates: WidgetRates): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_gold_card)
            views.setTextViewText(R.id.gold_widget_last_updated, "بروزرسانی: ${rates.lastUpdated}")

            // Intent to open app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.gold_widget_root, openAppPendingIntent)

            // Intent to refresh gold widget
            val refreshIntent = Intent(context, GoldAppWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_GOLD_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                3,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.gold_widget_btn_refresh, refreshPendingIntent)

            // 1. Gold 18K
            views.setTextViewText(R.id.gold_widget_price, Formatters.formatToman(rates.goldPrice))
            views.setTextViewText(R.id.gold_widget_change, Formatters.formatPercent(rates.goldChange))
            views.setTextColor(
                R.id.gold_widget_change,
                if (rates.goldChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            )

            // 2. Emami Coin
            views.setTextViewText(R.id.gold_widget_coin, "سکه امامی: ${Formatters.formatToman(rates.coinPrice)}")

            // 3. Gold Ounce
            views.setTextViewText(R.id.gold_widget_ounce, "انس: $${String.format(Locale.US, "%,.0f", rates.ounceUsd)}")

            // 4. High / Low Range
            views.setTextViewText(
                R.id.gold_widget_range,
                "سقف: ${Formatters.formatToman(rates.goldHigh)} | کف: ${Formatters.formatToman(rates.goldLow)}"
            )

            return views
        }
    }
}
