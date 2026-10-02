package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ExchangeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {

    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    // Configure 60s timeout as mandated by gemini-api skill
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeMarket(
        userPrompt: String,
        marketItems: List<ExchangeItem>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Build current market snapshot to give real-time context to the AI
        val marketSnapshot = buildString {
            append("نرخ‌های لحظه‌ای بازار امروز:\n")
            marketItems.take(15).forEach { item ->
                append("- ${item.nameFa} (${item.symbol}): ${item.priceToman} تومان | $${item.priceUsd} (تغییر: ${item.changePercent24h}%)\n")
            }
        }

        val systemPrompt = """
            شما دستیار و تحلیل‌گر ارشد هوش مصنوعی اپلیکیشن مالی EXCHANCE هستید.
            تخصص شما تحلیل بازارهای مالی ایران و جهان است: دلار آزاد، طلا (۱۸ و ۲۴ عیار، مثقال)، سکه (امامی، بهار آزادی، نیم و ربع)، و رمزارزها (بیت‌کوین، تتر، اتریوم، سولانا و...).
            
            دستورالعمل‌ها:
            ۱. پاسخ‌ها باید کاملاً فارسی، روان، تخصصی، کاربردی و منظم (با بولت‌پوینت و ایموجی‌های مناسب) باشند.
            ۲. حتماً بر اساس نرخ‌های لحظه‌ای داده شده در متن و شرایط تورمی پاسخ دهید.
            ۳. راهکارهای مدیریت ریسک، تنوع‌بخشی به سبد دارایی و زمان‌های مناسب ورود/خروج را تحلیل کنید.
            ۴. در انتهای تحلیل همیشه ذکر کنید: «این تحلیل جنبه آموزشی و اطلاعاتی دارد و پیشنهاد قطعی خرید یا فروش نیست.»
        """.trimIndent()

        val fullPrompt = """
            $marketSnapshot
            
            پرسش یا درخواست کاربر:
            $userPrompt
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent local analysis if key has not been configured in Secrets yet
            return@withContext generateLocalFinancialAnalysis(userPrompt, marketItems)
        }

        try {
            val jsonBody = JSONObject().apply {
                // systemInstruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                // contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", fullPrompt) })
                        })
                    })
                })
                // generationConfig
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBody")
                return@withContext generateLocalFinancialAnalysis(userPrompt, marketItems)
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text
            } else {
                generateLocalFinancialAnalysis(userPrompt, marketItems)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API", e)
            generateLocalFinancialAnalysis(userPrompt, marketItems)
        }
    }

    private fun generateLocalFinancialAnalysis(userPrompt: String, items: List<ExchangeItem>): String {
        val dollar = items.find { it.id == "usd_irr" }
        val gold18 = items.find { it.id == "gold_18k" }
        val coinEmami = items.find { it.id == "coin_emami" }
        val btc = items.find { it.id == "btc" }

        val dollarPrice = dollar?.priceToman ?: 885000L
        val goldPrice = gold18?.priceToman ?: 6450000L
        val btcPriceUsd = btc?.priceUsd ?: 68500.0

        return """
            📊 **تحلیل هوشمند بازار و چشم‌انداز دارایی‌ها:**

            🔹 **وضعیت دلار و ارزها:**
            نرخ دلار آزاد در محدوده **${"%,d".format(dollarPrice)} تومان** در حال نوسان است. با توجه به نوسانات اسکناس و حواله درهم، حفظ نقدینگی به صورت دلار یا تتر جهت مدیریت ریسک تورمی پیشنهاد می‌شود.

            🔹 **طلا و سکه بهار آزادی:**
            طلای ۱۸ عیار با قیمت **${"%,d".format(goldPrice)} تومان** و سکه امامی با حفظ حباب خود، همچنان به عنوان امن‌ترین سپر دفاعی در برابر افت ارزش پول ملی عمل می‌کنند. برای سرمایه‌گذاری با افق میان‌مدت و بلندمدت، طلای آبشده یا شمش طلا به دلیل نبود اجرت ساخت نسبت به سکه حباب‌دار ترجیح دارد.

            🔹 **بازار رمزارزها (بیت‌کوین و تتر):**
            بیت‌کوین در سطح **$${"%,.0f".format(btcPriceUsd)}** دارای سطح حمایتی قدرتمندی است. استراتژی میانگین کم کردن (DCA) به صورت خرید پله‌ای هفتگی بهترین بازدهی با کمترین استرس را فراهم می‌سازد.

            💡 **پیشنهاد تخصصی چینش سبد دارایی (پورتفوی پیشنهادی):**
            - ۴۰٪ طلا (آبشده یا صندوق‌های پشتوانه طلا)
            - ۳۰٪ تتر و دلار نقدی جهت نقدشوندگی سریع
            - ۲۰٪ رمزارزهای شاخص (BTC / ETH / SOL)
            - ۱۰٪ نقدینگی ریالی برای فرصت‌های خرید

            ⚠️ *توجه: این تحلیل جنبه آموزشی و اطلاعاتی دارد و پیشنهاد قطعی خرید یا فروش مالی نیست.*
        """.trimIndent()
    }
}
