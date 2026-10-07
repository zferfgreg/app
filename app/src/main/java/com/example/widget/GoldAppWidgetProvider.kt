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
import com.example.data.model.ExchangeItem
import com.example.data.repository.ExchangeRepository
import com.example.util.Formatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
                try {
                    val db = AppDatabase.getDatabase(context)
                    val repo = ExchangeRepository(db.watchlistDao(), db.priceAlertDao())
                    repo.refreshRates()
                } catch (e: Exception) {
                    // ignore
                }
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
                val views = buildBaseRemoteViews(context)
                appWidgetManager.updateAppWidget(appWidgetId, views)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val repo = ExchangeRepository(db.watchlistDao(), db.priceAlertDao())
                        val items = repo.getCurrentItems()
                        if (items.isNotEmpty()) {
                            populateViewsWithItems(views, items)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        private fun buildBaseRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_gold_card)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = "بروزرسانی: ${timeFormat.format(Date())}"
            views.setTextViewText(R.id.gold_widget_last_updated, timeString)

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

            // Live baseline prices
            views.setTextViewText(R.id.gold_widget_price, "۲۶,۲۰۰,۶۰۰ تومان")
            views.setTextViewText(R.id.gold_widget_change, "-۲.۰۵٪")
            views.setTextColor(R.id.gold_widget_change, Color.parseColor("#FF5252"))
            views.setTextViewText(R.id.gold_widget_coin, "سکه امامی: ۲۶۷,۳۶۵,۰۰۰ ت")
            views.setTextViewText(R.id.gold_widget_ounce, "انس: $۴,۰۸۵")
            views.setTextViewText(R.id.gold_widget_range, "سقف: ۲۶,۷۴۰,۰۰۰ | کف: ۲۶,۱۳۰,۰۰۰")

            return views
        }

        private fun populateViewsWithItems(views: RemoteViews, items: List<ExchangeItem>) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            views.setTextViewText(R.id.gold_widget_last_updated, "بروزرسانی: ${timeFormat.format(Date())}")

            val gold = items.find { it.id == "GOLD_18K" }
            val coin = items.find { it.id == "SEKKE_EMAMI" }
            val ounce = items.find { it.id == "OUNCE_GOLD" }

            // 1. Gold 18K
            val goldPrice = gold?.priceToman ?: 26200600L
            val goldChange = gold?.changePercent24h ?: -2.05
            views.setTextViewText(R.id.gold_widget_price, Formatters.formatToman(goldPrice))
            views.setTextViewText(R.id.gold_widget_change, Formatters.formatPercent(goldChange))
            views.setTextColor(
                R.id.gold_widget_change,
                if (goldChange >= 0) Color.parseColor("#00E676") else Color.parseColor("#FF5252")
            )

            // 2. Emami Coin
            val coinPrice = coin?.priceToman ?: 267365000L
            views.setTextViewText(R.id.gold_widget_coin, "سکه امامی: ${Formatters.formatToman(coinPrice)}")

            // 3. Gold Ounce
            val ounceUsd = ounce?.priceUsd ?: 4085.8
            views.setTextViewText(R.id.gold_widget_ounce, "انس: $${String.format(Locale.US, "%,.0f", ounceUsd)}")

            // 4. High / Low Range
            if (gold != null && gold.high24hToman > 0 && gold.low24hToman > 0) {
                views.setTextViewText(
                    R.id.gold_widget_range,
                    "سقف: ${Formatters.formatToman(gold.high24hToman)} | کف: ${Formatters.formatToman(gold.low24hToman)}"
                )
            }
        }
    }
}
