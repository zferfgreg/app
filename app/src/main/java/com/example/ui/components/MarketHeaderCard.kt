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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechIndigo
import com.example.ui.theme.FintechRed
import com.example.util.Formatters

@Composable
fun MarketHeaderCard(
    items: List<ExchangeItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    unreadNotificationsCount: Int = 0,
    onOpenNotifications: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    isAnimationEnabled: Boolean = true,
    onToggleAnimation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val usdItem = items.find { it.id == "USD" }
    val usdtItem = items.find { it.id == "USDT" }
    val btcItem = items.find { it.id == "BTC" }
    val goldItem = items.find { it.id == "GOLD_18K" }
    val coinItem = items.find { it.id == "SEKKE_EMAMI" }

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
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                alpha = if (isDarkTheme) 0.28f else 0.12f
            )

            // Dynamic gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                colors.surface.copy(alpha = 0.85f),
                                colors.surface
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top App Bar inside header - Clean, luxury & decluttered
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Actions on Left: Theme switch, Animation toggle, Notification Bell, Refresh
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Theme Toggle Button (White Theme / Dark Theme)
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceLight.copy(alpha = 0.9f))
                                .border(1.dp, colors.surfaceBorder, CircleShape)
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = if (isDarkTheme) "تم سفید (روشن)" else "تم تاریک",
                                tint = if (isDarkTheme) FintechGold else FintechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 2. Animated Background Toggle Button
                        IconButton(
                            onClick = onToggleAnimation,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAnimationEnabled) FintechCyan.copy(alpha = 0.2f) else colors.surfaceLight)
                                .border(
                                    1.dp,
                                    if (isAnimationEnabled) FintechCyan else colors.surfaceBorder,
                                    CircleShape
                                )
                                .testTag("animation_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "پس‌زمینه متحرک",
                                tint = if (isAnimationEnabled) FintechCyan else colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 3. Smart Notification Button with Badge
                        Box {
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceLight.copy(alpha = 0.9f))
                                .border(1.dp, if (unreadNotificationsCount > 0) FintechGold else colors.surfaceBorder, CircleShape)
                                .testTag("notification_bell_button")
                            ) {
                                Icon(
                                    imageVector = if (unreadNotificationsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "اعلان‌های هوشمند",
                                    tint = if (unreadNotificationsCount > 0) FintechGold else colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(FintechRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unreadNotificationsCount",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 4. Refresh button
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceLight.copy(alpha = 0.9f))
                                .border(1.dp, colors.surfaceBorder, CircleShape)
                                .testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                tint = FintechCyan,
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(if (isRefreshing) Modifier.rotate(rotation) else Modifier)
                            )
                        }
                    }

                    // Logo & App Name with Live Status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(FintechGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EXCHANCE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "دلار • طلا • کریپتو (زنده)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
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
                                .size(32.dp)
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
                        priceToman = usdItem?.priceToman ?: 258465L,
                        changePercent = usdItem?.changePercent24h ?: 1.45,
                        accentColor = FintechGreen,
                        modifier = Modifier.weight(1f)
                    )

                    // USDT Tether Card
                    HeroTickerCard(
                        titleFa = "تتر (دلار دیجیتال)",
                        titleEn = "USDT / TOMAN",
                        priceToman = usdtItem?.priceToman ?: 259000L,
                        changePercent = usdtItem?.changePercent24h ?: 1.25,
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
                        .background(colors.surfaceLight.copy(alpha = 0.7f))
                        .border(1.dp, colors.surfaceBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniStatItem(
                        title = "بیت‌کوین",
                        value = Formatters.formatUsd(btcItem?.priceUsd ?: 84200.0),
                        change = btcItem?.changePercent24h ?: 2.85
                    )
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(colors.surfaceBorder))
                    MiniStatItem(
                        title = "طلای ۱۸ عیار",
                        value = if (goldItem != null) Formatters.formatToman(goldItem.priceToman) else "۲۵,۶۹۴,۴۰۰ ت",
                        change = goldItem?.changePercent24h ?: 0.15
                    )
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(colors.surfaceBorder))
                    MiniStatItem(
                        title = "سکه امامی",
                        value = if (coinItem != null) Formatters.formatToman(coinItem.priceToman) else "۲۶۰,۳۹۵,۰۰۰ ت",
                        change = coinItem?.changePercent24h ?: 0.25
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
    val colors = AppTheme.colors
    val isPositive = changePercent >= 0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceLight)
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
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
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = Formatters.formatToman(priceToman),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                ),
                color = colors.textPrimary
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
    val colors = AppTheme.colors
    val isPositive = change >= 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = colors.textMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            ),
            color = colors.textPrimary
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
