package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlertCondition
import com.example.data.local.PriceAlertEntity
import com.example.data.model.ExchangeItem
import com.example.ui.components.AssetIconBadge
import com.example.ui.components.ExchangeItemCard
import com.example.ui.components.ScrollDownBlurEffect
import com.example.ui.components.ScrollMotionBlurEffect
import com.example.ui.components.rememberScrollVelocity
import com.example.ui.components.scrollMotionBlur
import com.example.ui.components.WatchedAssetsHeatmap
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters
import kotlin.math.abs

@Composable
fun WatchlistScreen(
    items: List<ExchangeItem>,
    onItemClick: (ExchangeItem) -> Unit,
    onToggleFavorite: (ExchangeItem) -> Unit,
    onNavigateToMarket: () -> Unit,
    customAlerts: List<PriceAlertEntity> = emptyList(),
    onDeleteCustomAlert: (String) -> Unit = {},
    onToggleCustomAlert: (String, Boolean) -> Unit = { _, _ -> },
    onTestCustomAlert: (PriceAlertEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val listState = rememberLazyListState()
    val motionVelocity by rememberScrollVelocity(listState)
    val favoriteItems = items.filter { it.isFavorite }
    val alertItems = items.filter { it.alertPriceToman != null }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .scrollMotionBlur(motionVelocity),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "دیده‌بان و هشدارهای من",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FintechGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "ارزهای برگزیده، کریپتو و هشدارهای تعیین شده قیمت",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.End
                )
            }
        }

        // Color-Coded Heatmap Data Visualization Component
        item {
            WatchedAssetsHeatmap(
                items = items,
                onAssetClick = onItemClick
            )
        }

        // Active Alerts Section (if any)
        if (alertItems.isNotEmpty()) {
            item {
                Text(
                    text = "هشدارهای قیمت هدف فعال (${alertItems.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = FintechGold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            items(alertItems, key = { "alert_${it.id}" }) { item ->
                val target = item.alertPriceToman ?: 0L
                val current = item.priceToman
                val diffPercent = if (target > 0) ((current - target).toDouble() / target.toDouble()) * 100 else 0.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, FintechGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item) }
                        .testTag("alert_card_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "قیمت هدف: ${Formatters.formatToman(target)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = FintechGold
                            )
                            Text(
                                text = "قیمت فعلی: ${Formatters.formatToman(current)} (${if (diffPercent >= 0) "+" else ""}${String.format("%.1f", diffPercent)}٪ فاصله)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (abs(diffPercent) < 2.0) FintechGreen else TextMuted
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = item.nameFa,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = item.symbol,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            AssetIconBadge(item = item, modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }

        // Custom Firebase Price Alerts (FCM Push) Section
        if (customAlerts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "سرویس پوش FCM",
                            fontSize = 10.5.sp,
                            color = FintechCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "هشدارهای سفارشی Firebase (${customAlerts.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            items(customAlerts, key = { "custom_fcm_alert_${it.id}" }) { alert ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, if (alert.isEnabled) FintechCyan.copy(alpha = 0.4f) else colors.surfaceBorder, RoundedCornerShape(16.dp))
                        .testTag("custom_alert_card_${alert.id}"),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onDeleteCustomAlert(alert.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف هشدار",
                                        tint = FintechRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onTestCustomAlert(alert) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "تست پوش",
                                        tint = FintechCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Switch(
                                    checked = alert.isEnabled,
                                    onCheckedChange = { onToggleCustomAlert(alert.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = FintechCyan,
                                        checkedTrackColor = FintechCyan.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = alert.assetNameFa,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = alert.assetSymbol,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = alert.condition.icon, fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تاپیک: ${alert.fcmTopic}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )

                            Text(
                                text = when (alert.condition) {
                                    AlertCondition.ABOVE -> "صعود به بالای ${Formatters.formatToman(alert.targetPriceToman)}"
                                    AlertCondition.BELOW -> "نزول به زیر ${Formatters.formatToman(alert.targetPriceToman)}"
                                    AlertCondition.PERCENT_CHANGE -> "نوسان بیش از ${alert.percentThreshold}٪"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = when (alert.condition) {
                                    AlertCondition.ABOVE -> FintechGreen
                                    AlertCondition.BELOW -> FintechRed
                                    AlertCondition.PERCENT_CHANGE -> FintechCyan
                                }
                            )
                        }
                    }
                }
            }
        }

        // Pinned Favorites Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (favoriteItems.isNotEmpty()) {
                    Text(
                        text = "${favoriteItems.size} ارز نشان شده",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = "ارزهای نشان شده (محبوب‌ها)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        // Empty state if no favorites
        if (favoriteItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SurfaceCardLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = FintechGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "دیده‌بان شما خالی است!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "با ضربه زدن روی آیکون ستاره در لیست بازار، ارزها و رمزارزهای مورد علاقه خود را ذخیره کنید تا همیشه جلوی چشمتان باشد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToMarket,
                            colors = ButtonDefaults.buttonColors(containerColor = FintechCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "مشاهده لیست بازار", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(favoriteItems, key = { it.id }) { item ->
                ExchangeItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onToggleFavorite = { onToggleFavorite(item) }
                )
            }
        }
    }

    // Dynamic Motion Blur Effect (Replaced scroll down button)
    ScrollMotionBlurEffect(
        listState = listState,
        bottomPadding = 90.dp
    )
    }
}
