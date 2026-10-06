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
import com.example.data.local.AppDatabase
import com.example.data.repository.ExchangeRepository
import com.example.util.Formatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = ExchangeRepository(db.watchlistDao(), db.priceAlertDao())
                    repo.refreshRates()
                } catch (e: Exception) {
                    // ignore
                }
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.action.REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MarketAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            }
            return false
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_market_card)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = "بروزرسانی: ${timeFormat.format(Date())}"
            views.setTextViewText(R.id.widget_last_updated, timeString)

            // Intent to open app
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Intent to refresh widget
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

            // Load latest cached rates from Room or default
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = ExchangeRepository(db.watchlistDao(), db.priceAlertDao())
                    val items = repo.getCurrentItems()

                    val usd = items.find { it.id == "USD" }
                    val gold = items.find { it.id == "GOLD_18K" }
                    val coin = items.find { it.id == "SEKKE_EMAMI" }
                    val usdt = items.find { it.id == "USDT" }
                    val btc = items.find { it.id == "BTC" }

                    // USD Row
                    val usdPrice = usd?.priceToman ?: 94800L
                    val usdChange = usd?.changePercent24h ?: 1.2
                    views.setTextViewText(R.id.widget_price_usd, Formatters.formatToman(usdPrice))
                    views.setTextViewText(R.id.widget_change_usd, Formatters.formatPercent(usdChange))
                    views.setTextColor(
                        R.id.widget_change_usd,
                        if (usdChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                    )

                    // Gold Row
                    val goldPrice = gold?.priceToman ?: 25694400L
                    val goldChange = gold?.changePercent24h ?: 0.8
                    views.setTextViewText(R.id.widget_price_gold, Formatters.formatToman(goldPrice))
                    views.setTextViewText(R.id.widget_change_gold, Formatters.formatPercent(goldChange))
                    views.setTextColor(
                        R.id.widget_change_gold,
                        if (goldChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                    )

                    // Coin Row
                    val coinPrice = coin?.priceToman ?: 304500000L
                    val coinChange = coin?.changePercent24h ?: 1.5
                    views.setTextViewText(R.id.widget_price_coin, Formatters.formatToman(coinPrice))
                    views.setTextViewText(R.id.widget_change_coin, Formatters.formatPercent(coinChange))
                    views.setTextColor(
                        R.id.widget_change_coin,
                        if (coinChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                    )

                    // Crypto Row (USDT & BTC)
                    val usdtPrice = usdt?.priceToman ?: 95100L
                    val btcUsd = btc?.priceUsd ?: 85000.0
                    val cryptoChange = usdt?.changePercent24h ?: 0.9
                    views.setTextViewText(
                        R.id.widget_price_crypto,
                        "${Formatters.formatToman(usdtPrice)} | $${Formatters.formatCompact(btcUsd.toLong())}"
                    )
                    views.setTextViewText(R.id.widget_change_crypto, Formatters.formatPercent(cryptoChange))
                    views.setTextColor(
                        R.id.widget_change_crypto,
                        if (cryptoChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
                    )

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
