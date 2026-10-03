package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class WorldCity(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val flag: String,
    val timeZoneId: String,
    val marketName: String,
    val marketOpenHour: Int, // 24h format
    val marketCloseHour: Int
)

@Composable
fun WorldClocksCard(
    modifier: Modifier = Modifier
) {
    // List of requested cities: London, Tehran, Vancouver, plus major financial hubs
    val cities = remember {
        listOf(
            WorldCity("tehran", "تهران", "Tehran", "🇮🇷", "Asia/Tehran", "بازار طلا و بورس تهران", 9, 17),
            WorldCity("london", "لندن", "London", "🇬🇧", "Europe/London", "بورس لندن (LSE)", 8, 16),
            WorldCity("vancouver", "ونکوور", "Vancouver", "🇨🇦", "America/Vancouver", "بازار غرب کانادا (TSX)", 9, 16),
            WorldCity("new_york", "نیویورک", "New York", "🇺🇸", "America/New_York", "وال‌استریت (NYSE)", 9, 16),
            WorldCity("dubai", "دبی", "Dubai", "🇦🇪", "Asia/Dubai", "بازار مالی و حواله دبی", 10, 18),
            WorldCity("tokyo", "توکیو", "Tokyo", "🇯🇵", "Asia/Tokyo", "بورس توکیو (TSE)", 9, 15),
            WorldCity("frankfurt", "فرانکفورت", "Frankfurt", "🇩🇪", "Europe/Berlin", "بورس فرانکفورت (DAX)", 9, 17),
            WorldCity("sydney", "سیدنی", "Sydney", "🇦🇺", "Australia/Sydney", "بورس استرالیا (ASX)", 10, 16)
        )
    }

    // Live Clock State ticking every 1 second
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
            .testTag("world_clocks_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "سشن‌های مالی جهانی",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ساعت جهانی و بازارهای مالی",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "لندن، تهران، ونکوور و مراکز پولی جهان",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FintechCyan.copy(alpha = 0.15f))
                            .border(1.dp, FintechCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Carousel of City Clocks
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(cities, key = { it.id }) { city ->
                    CityClockTile(city = city, currentMillis = currentTimeMillis)
                }
            }
        }
    }
}

@Composable
private fun CityClockTile(
    city: WorldCity,
    currentMillis: Long
) {
    val tz = remember(city.timeZoneId) { TimeZone.getTimeZone(city.timeZoneId) }
    val calendar = remember(currentMillis, tz) {
        Calendar.getInstance(tz).apply { timeInMillis = currentMillis }
    }

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val isDayTime = hour in 6..18

    // Market status: Open if between open and close hours on weekdays (Mon-Fri or Sat-Wed for Tehran)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val isTehran = city.id == "tehran"
    val isDubai = city.id == "dubai"

    val isWeekend = when {
        isTehran -> dayOfWeek == Calendar.THURSDAY || dayOfWeek == Calendar.FRIDAY
        isDubai -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        else -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    val isMarketOpen = !isWeekend && (hour in city.marketOpenHour until city.marketCloseHour)

    val timeFormatted = String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)

    // Calculate time diff relative to Tehran
    val tehranTz = remember { TimeZone.getTimeZone("Asia/Tehran") }
    val diffMillis = tz.getOffset(currentMillis) - tehranTz.getOffset(currentMillis)
    val diffHours = diffMillis / (1000 * 60 * 60)
    val diffMins = kotlin.math.abs((diffMillis / (1000 * 60)) % 60)
    val diffString = if (diffHours == 0 && diffMins == 0) {
        "ساعت مبنا"
    } else {
        val sign = if (diffMillis >= 0) "+" else "-"
        "$sign${kotlin.math.abs(diffHours)}:${String.format(Locale.US, "%02d", diffMins)} نسبت به تهران"
    }

    Box(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardLight)
            .border(
                1.dp,
                if (isMarketOpen) FintechGreen.copy(alpha = 0.4f) else SurfaceCardBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
            .testTag("world_clock_tile_${city.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // City Header: Flag, Name, Sun/Moon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = city.flag, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = city.nameFa,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = city.nameEn,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }

                Icon(
                    imageVector = if (isDayTime) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                    contentDescription = null,
                    tint = if (isDayTime) FintechGold else FintechPurple,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Digital Time with Glowing effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (isMarketOpen) FintechCyan else TextPrimary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Market Session Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isMarketOpen) FintechGreen else FintechRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMarketOpen) "🟢 بازار باز است" else "🔴 بازار بسته است",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMarketOpen) FintechGreen else TextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Difference relative to Tehran
            Text(
                text = diffString,
                fontSize = 8.5.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}
