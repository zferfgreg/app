package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechPurple
import com.example.ui.theme.FintechRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ExchangeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: ExchangeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val autoRefreshSec by viewModel.autoRefreshSec.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val volatilityAlert by viewModel.highVolatilityAlert.collectAsStateWithLifecycle()
    val aiPersona by viewModel.aiPersona.collectAsStateWithLifecycle()
    val goldWage by viewModel.defaultGoldWage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cacheClearedNotification by remember { mutableStateOf(false) }

    val telegramId = "@ar1an00"
    val telegramUsername = "ar1an00"

    fun openTelegram() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$telegramUsername"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در باز کردن تلگرام", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyTelegramId() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Telegram ID", telegramId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "آیدی $telegramId در کلیپ‌بورد کپی شد", Toast.LENGTH_SHORT).show()
    }

    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    🚀 با اپلیکیشن EXCHANCE، قیمت لحظه‌ای دلار، انواع طلا و سکه، ارزهای جهانی و رمزارزها رو با نمودارهای تحلیلی و هوش مصنوعی دنبال کنید!
                    📲 ارتباط و دریافت: https://t.me/$telegramUsername
                    """.trimIndent()
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری برنامه با دوستان"))
        } catch (e: Exception) {
            // Ignored
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 95.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "تنظیمات پیشرفته",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FintechCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "شخصی‌سازی کامل هوش مصنوعی، نرخ‌ها و هشدارهای بازار",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Section 1: Currency Unit Preference
        item {
            SettingsCard(
                title = "واحد پول پایه",
                icon = Icons.Default.Paid,
                iconTint = FintechGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Switch(
                        checked = uiState.useRials,
                        onCheckedChange = { viewModel.toggleUseRials() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FintechCyan,
                            checkedTrackColor = FintechCyan.copy(alpha = 0.35f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceCardLight
                        ),
                        modifier = Modifier.testTag("toggle_rials_switch")
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (uiState.useRials) "واحد: ریال (۱۰ برابر تومان)" else "واحد: تومان",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.useRials) "تمام نرخ‌های ارز، طلا و رمزارز با ریال نمایش داده می‌شوند" else "نرخ‌ها مطابق عرف بازار ایران به تومان هستند",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Section 2: Auto-Refresh Interval
        item {
            SettingsCard(
                title = "سرعت بروزرسانی خودکار نرخ‌ها",
                icon = Icons.Default.Speed,
                iconTint = FintechCyan
            ) {
                Column {
                    Text(
                        text = "بازه زمانی دریافت آخرین قیمت‌ها از سرور TGJU و بایننس:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val refreshOptions = listOf(
                        15 to "۱۵ ثانیه",
                        30 to "۳۰ ثانیه",
                        60 to "۱ دقیقه",
                        300 to "۵ دقیقه"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        refreshOptions.forEach { (sec, label) ->
                            val isSelected = autoRefreshSec == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) FintechCyan.copy(alpha = 0.2f) else SurfaceCardLight)
                                    .border(
                                        1.dp,
                                        if (isSelected) FintechCyan else SurfaceCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setAutoRefreshSec(sec) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FintechCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: AI Market Analyst Persona
        item {
            SettingsCard(
                title = "سبک تحلیل هوش مصنوعی (Gemini)",
                icon = Icons.Default.AutoAwesome,
                iconTint = FintechPurple
            ) {
                Column {
                    Text(
                        text = "رویکرد هوش مصنوعی در پاسخ به پرسش‌ها و تحلیل پورتفوی شما:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val personas = listOf(
                        "متعادل و منطقی" to "⚖️ متعادل",
                        "تحلیلگر تکنیکال" to "📈 تکنیکال",
                        "محافظه‌کار و امن" to "🛡️ ضد تورم"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        personas.forEach { (key, label) ->
                            val isSelected = aiPersona == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) FintechPurple.copy(alpha = 0.2f) else SurfaceCardLight)
                                    .border(
                                        1.dp,
                                        if (isSelected) FintechPurple else SurfaceCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setAiPersona(key) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FintechPurple else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Market Volatility & Haptic Feedback
        item {
            SettingsCard(
                title = "هشدارها و بازخورد لمسی",
                icon = Icons.Default.NotificationsActive,
                iconTint = FintechRed
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Volatility Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Switch(
                            checked = volatilityAlert,
                            onCheckedChange = { viewModel.toggleVolatilityAlert() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FintechRed,
                                checkedTrackColor = FintechRed.copy(alpha = 0.35f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceCardLight
                            )
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "هشدار نوسانات شدید بازار",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "نمایش نوتیفیکیشن در صورت تغییر بیش از ۳٪ در ۲۴ ساعت",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    // Haptic Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { viewModel.toggleHaptic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FintechCyan,
                                checkedTrackColor = FintechCyan.copy(alpha = 0.35f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceCardLight
                            )
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "لرزش هپتیک دکمه‌ها (Haptic)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "فیدبک لمسی سبک هنگام لمس نرخ‌ها و جابجایی تب‌ها",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Gold Wage / Fee Calculator Setting
        item {
            SettingsCard(
                title = "محاسبات اجرت و حباب طلا",
                icon = Icons.Default.Paid,
                iconTint = FintechGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Counter Controls (- / +)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (goldWage > 1) viewModel.setDefaultGoldWage(goldWage - 1) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceCardLight)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "کاهش", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$goldWage%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FintechGold
                        )

                        IconButton(
                            onClick = { if (goldWage < 30) viewModel.setDefaultGoldWage(goldWage + 1) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceCardLight)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "افزایش", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "اجرت ساخت پیش‌فرض طلا",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "مبنای محاسبه در مبدل هوشمند و ماشین‌حساب طلا",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Section 6: Data & Storage
        item {
            SettingsCard(
                title = "مدیریت حافظه و پایگاه داده",
                icon = Icons.Default.DeleteSweep,
                iconTint = FintechGreen
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearCache()
                                cacheClearedNotification = true
                                coroutineScope.launch {
                                    delay(3000)
                                    cacheClearedNotification = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                        ) {
                            Text(text = "پاک‌سازی حافظه موقت", color = FintechCyan, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "تازه‌سازی و پاک کردن کش",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "حذف داده‌های موقت و دانلود مجدد آخرین نرخ‌ها",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = cacheClearedNotification,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FintechGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "حافظه موقت پاک شد و تمام نرخ‌ها بازخوانی شدند ✔",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FintechGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FintechGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section 7: Telegram Support & Sharing (@ar1an00)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, FintechCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FintechCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "پشتیبانی ۲۴ ساعته",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FintechCyan
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "پشتیبانی و ارتباط تلگرام",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ارتباط مستقیم با توسعه‌دهنده در تلگرام",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF2AABEE), Color(0xFF229ED9))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "تلگرام",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Telegram ID highlight box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBg)
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { copyTelegramId() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "کپی آیدی",
                                tint = FintechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = telegramId,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = FintechCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "آیدی تلگرام:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { shareApp() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "معرفی برنامه", color = TextSecondary, fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = { openTelegram() },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9))
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "گفتگو در تلگرام", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 8: About & Server Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.exchance_icon),
                        contentDescription = "EXCHANCE",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "EXCHANCE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = TextPrimary
                    )

                    Text(
                        text = "نسخه ۱.۰.۰ • مانیتورینگ هوشمند بازار مالی",
                        style = MaterialTheme.typography.labelSmall,
                        color = FintechGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCardLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FintechGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "پایدار و متصل", fontSize = 11.sp, color = FintechGreen, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "وضعیت وب‌سرویس TGJU و بایننس", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}
