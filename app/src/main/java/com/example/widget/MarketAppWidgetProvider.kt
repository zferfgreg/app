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

class MarketAppWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == ACTION_REFRESH_WIDGET) {
            CoroutineScope(Dispatchers.IO).launch {
                WidgetRatesManager.fetchAndStoreLiveRates(context)
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.action.REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, MarketAppWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (widgetId in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
                DollarAppWidgetProvider.updateAllDollarWidgets(context)
                GoldAppWidgetProvider.updateAllGoldWidgets(context)
            } catch (e: Exception) {
                // ignore
            }
        }

        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val myProvider = ComponentName(context, MarketAppWidgetProvider::class.java)
                    if (appWidgetManager.isRequestPinAppWidgetSupported) {
                        val successCallback = PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, MarketAppWidgetProvider::class.java),
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
                // 1. Immediately apply latest stored rates so widget is instantaneous
                val initialRates = WidgetRatesManager.getRates(context)
                val views = buildRemoteViews(context, initialRates)
                appWidgetManager.updateAppWidget(appWidgetId, views)

                // 2. Asynchronously fetch fresh live rates from TGJU/API and update
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
            val views = RemoteViews(context.packageName, R.layout.widget_market_card)
            views.setTextViewText(R.id.widget_last_updated, "بروزرسانی: ${rates.lastUpdated}")

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
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Intent to refresh widget rates
            val refreshIntent = Intent(context, MarketAppWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // 1. USD Row
            views.setTextViewText(R.id.widget_price_usd, Formatters.formatToman(rates.usdPrice))
            views.setTextViewText(R.id.widget_change_usd, Formatters.formatPercent(rates.usdChange))
            views.setTextColor(
                R.id.widget_change_usd,
                if (rates.usdChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            )

            // 2. Gold Row
            views.setTextViewText(R.id.widget_price_gold, Formatters.formatToman(rates.goldPrice))
            views.setTextViewText(R.id.widget_change_gold, Formatters.formatPercent(rates.goldChange))
            views.setTextColor(
                R.id.widget_change_gold,
                if (rates.goldChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            )

            // 3. Coin Row
            views.setTextViewText(R.id.widget_price_coin, Formatters.formatToman(rates.coinPrice))
            views.setTextViewText(R.id.widget_change_coin, Formatters.formatPercent(rates.coinChange))
            views.setTextColor(
                R.id.widget_change_coin,
                if (rates.coinChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            )

            // 4. Crypto Row
            views.setTextViewText(
                R.id.widget_price_crypto,
                "${Formatters.formatToman(rates.usdtPrice)} | $${Formatters.formatCompact(rates.btcUsd.toLong())}"
            )
            views.setTextViewText(R.id.widget_change_crypto, Formatters.formatPercent(rates.usdtChange))
            views.setTextColor(
                R.id.widget_change_crypto,
                if (rates.usdtChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            )

            return views
        }
    }
}
