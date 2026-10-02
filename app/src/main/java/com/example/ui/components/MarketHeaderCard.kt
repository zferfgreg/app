package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ExchangeItem
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechIndigo
import com.example.ui.theme.FintechRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun MarketHeaderCard(
    items: List<ExchangeItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usdItem = items.find { it.id == "USD" }
    val usdtItem = items.find { it.id == "USDT" }
    val btcItem = items.find { it.id == "BTC" }
    val goldItem = items.find { it.id == "GOLD_18K" }
    val coinItem = items.find { it.id == "SEKKE_EMAMI" }

    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background visual banner image with overlay
            Image(
                painter = painterResource(id = R.drawable.exchance_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                alpha = 0.28f
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                SurfaceCard.copy(alpha = 0.85f),
                                SurfaceCard
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top App Bar inside header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Refresh button
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceCardLight.copy(alpha = 0.8f))
                                .border(1.dp, SurfaceCardBorder, CircleShape)
                                .testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = FintechCyan,
                                modifier = Modifier
                                    .size(20.dp)
                                    .then(if (isRefreshing) Modifier.rotate(rotation) else Modifier)
                            )
                        }

                        // Telegram Support button (@ar1an00)
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ar1an00"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // ignore
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF229ED9).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF229ED9).copy(alpha = 0.6f), CircleShape)
                                .testTag("header_telegram_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "پشتیبانی تلگرام @ar1an00",
                                tint = Color(0xFF229ED9),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Live Status Pill with TGJU.org branding
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceCardLight.copy(alpha = 0.8f))
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "منبع: TGJU.org | زنده",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FintechGreen)
                        )
                    }

                    // Logo & App Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXCHANCE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "دلار و کریپتو",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = FintechGold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.exchance_icon),
                            contentDescription = "EXCHANCE",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dual Main Cards: US Dollar & Tether
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // USD Cash Card
                    HeroTickerCard(
                        titleFa = "دلار آمریکا (نقدی)",
                        titleEn = "USD / TOMAN",
                        priceToman = usdItem?.priceToman ?: 94800L,
                        changePercent = usdItem?.changePercent24h ?: 1.35,
                        accentColor = FintechGreen,
                        modifier = Modifier.weight(1f)
                    )

                    // USDT Tether Card
                    HeroTickerCard(
                        titleFa = "تتر (دلار دیجیتال)",
                        titleEn = "USDT / TOMAN",
                        priceToman = usdtItem?.priceToman ?: 94950L,
                        changePercent = usdtItem?.changePercent24h ?: 1.15,
                        accentColor = FintechCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom mini summary row: BTC, Gold 18K, Emami Coin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardLight.copy(alpha = 0.5f))
                        .border(1.dp, SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniStatItem(
                        title = "بیت‌کوین",
                        value = Formatters.formatUsd(btcItem?.priceUsd ?: 91613.0),
                        change = btcItem?.changePercent24h ?: 2.84
                    )
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(SurfaceCardBorder))
                    MiniStatItem(
                        title = "طلای ۱۸ عیار",
                        value = "۴,۵۲۰,۰۰۰ ت",
                        change = goldItem?.changePercent24h ?: 1.85
                    )
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(SurfaceCardBorder))
                    MiniStatItem(
                        title = "سکه امامی",
                        value = "۵۳,۴۰۰,۰۰۰ ت",
                        change = coinItem?.changePercent24h ?: 2.15
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroTickerCard(
    titleFa: String,
    titleEn: String,
    priceToman: Long,
    changePercent: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isPositive = changePercent >= 0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCardLight)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Change indicator
                Text(
                    text = Formatters.formatPercent(changePercent),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = if (isPositive) FintechGreen else FintechRed
                )
                Text(
                    text = titleFa,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = Formatters.formatToman(priceToman),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = titleEn,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = accentColor
            )
        }
    }
}

@Composable
private fun MiniStatItem(
    title: String,
    value: String,
    change: Double
) {
    val isPositive = change >= 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            ),
            color = TextPrimary
        )
        Text(
            text = Formatters.formatPercent(change),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isPositive) FintechGreen else FintechRed
        )
    }
}
