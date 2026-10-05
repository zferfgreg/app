package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlertCondition
import com.example.data.local.PriceAlertEntity
import com.example.data.model.ExchangeItem
import com.example.data.model.PriceSource
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailSheet(
    item: ExchangeItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onToggleFavorite: (ExchangeItem) -> Unit,
    onSetAlert: (ExchangeItem, Long?) -> Unit,
    onOpenConverter: (ExchangeItem) -> Unit,
    onSelectSource: ((PriceSource) -> Unit)? = null,
    customAlerts: List<PriceAlertEntity> = emptyList(),
    onOpenCustomAlert: () -> Unit = {},
    onDeleteCustomAlert: (String) -> Unit = {},
    onToggleCustomAlert: (String, Boolean) -> Unit = { _, _ -> }
) {
    val colors = AppTheme.colors
    val isPositive = item.changePercent24h >= 0
    var alertInput by remember(item.alertPriceToman) {
        mutableStateOf(item.alertPriceToman?.toString() ?: "")
    }
    var showSavedFeedback by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Close, Star, Names & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = colors.textMuted
                        )
                    }

                    IconButton(
                        onClick = { onToggleFavorite(item) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("detail_fav_button")
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "نشان کردن",
                            tint = if (item.isFavorite) FintechGold else colors.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.nameFa,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = colors.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "منبع: ${item.selectedSource.shortName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = FintechGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.symbol} • ${item.nameEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AssetIconBadge(symbol = item.symbol, size = 48.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current Price & 24h Change Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceLight)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Change Percent Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPositive) FintechGreenBg else FintechRedBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isPositive) FintechGreen else FintechRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Formatters.formatPercent(item.changePercent24h),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isPositive) FintechGreen else FintechRed
                            )
                        }
                    }

                    // Price
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Formatters.formatToman(item.priceToman),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = Formatters.formatUsd(item.priceUsd),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Trend Chart (Area, Candlestick OHLC, Technical MA Indicators)
            FintechTrendChart(
                item = item,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 24h High / Low Stats Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceLight)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "بالاترین ۲۴ ساعت", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatToman(item.high24hToman),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FintechGreen
                    )
                    Text(text = Formatters.formatUsd(item.high24hUsd), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(colors.surfaceBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "پایین‌ترین ۲۴ ساعت", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatToman(item.low24hToman),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FintechRed
                    )
                    Text(text = Formatters.formatUsd(item.low24hUsd), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Multi-Source Price Selector Card ("از سایت های که قیمت میگیری بیشتر کن بزار انتخاب کرد تو ارز")
            if (item.sources.isNotEmpty()) {
                PriceSourceSelectorCard(
                    item = item,
                    onSelectSource = { source ->
                        onSelectSource?.invoke(source)
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Target Price Alert Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceLight)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (item.alertPriceToman != null) {
                        Text(
                            text = "هشدار فعال: ${Formatters.formatToman(item.alertPriceToman)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = FintechGold
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "هشدار قیمت هدف",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = FintechGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val parsed = alertInput.toLongOrNull()
                            onSetAlert(item, parsed)
                            showSavedFeedback = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_alert_button")
                    ) {
                        Text(text = "ثبت", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    if (item.alertPriceToman != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                alertInput = ""
                                onSetAlert(item, null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechRedBg),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "حذف", color = FintechRed)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = alertInput,
                        onValueChange = { alertInput = it },
                        placeholder = { Text("قیمت هدف به تومان...", fontSize = 12.sp, color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechGold,
                            unfocusedBorderColor = colors.surfaceBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("alert_price_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Firebase Push Notification Trigger Button
                Button(
                    onClick = onOpenCustomAlert,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_custom_fcm_alert_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechCyan.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FintechCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنظیم آستانه سفارشی و پوش Firebase (FCM)",
                            color = FintechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // List of Active Custom Price Alerts for this asset
                val assetAlerts = customAlerts.filter { it.assetId == item.id }
                if (assetAlerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "آستانه‌های فعال Firebase (${assetAlerts.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        assetAlerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onDeleteCustomAlert(alert.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف هشدار",
                                            tint = FintechRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Switch(
                                        checked = alert.isEnabled,
                                        onCheckedChange = { onToggleCustomAlert(alert.id, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = FintechGold,
                                            checkedTrackColor = FintechGold.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = when (alert.condition) {
                                                AlertCondition.ABOVE -> "صعود به ≥ ${Formatters.formatToman(alert.targetPriceToman)}"
                                                AlertCondition.BELOW -> "نزول به ≤ ${Formatters.formatToman(alert.targetPriceToman)}"
                                                AlertCondition.PERCENT_CHANGE -> "نوسان ≥ ${alert.percentThreshold}٪"
                                            },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (alert.condition) {
                                                AlertCondition.ABOVE -> FintechGreen
                                                AlertCondition.BELOW -> FintechRed
                                                AlertCondition.PERCENT_CHANGE -> FintechCyan
                                            }
                                        )
                                        Text(
                                            text = "تاپیک: ${alert.fcmTopic}",
                                            fontSize = 9.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = alert.condition.icon, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Open Converter
            Button(
                onClick = {
                    onDismiss()
                    onOpenConverter(item)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("open_converter_action"),
                colors = ButtonDefaults.buttonColors(containerColor = FintechCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "محاسبه و تبدیل این ارز (ماشین‌حساب)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
        }
    }
}
