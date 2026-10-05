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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlertCondition
import com.example.data.local.PriceAlertEntity
import com.example.data.model.ExchangeItem
import com.example.service.FcmTokenManager
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechRed
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPriceAlertSheet(
    item: ExchangeItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveAlert: (PriceAlertEntity) -> Unit,
    onTestNotification: (PriceAlertEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    var selectedCondition by remember { mutableStateOf(AlertCondition.ABOVE) }
    var targetPriceInput by remember {
        val defaultTarget = when (selectedCondition) {
            AlertCondition.ABOVE -> (item.priceToman * 1.05).toLong()
            AlertCondition.BELOW -> (item.priceToman * 0.95).toLong()
            AlertCondition.PERCENT_CHANGE -> item.priceToman
        }
        mutableStateOf(defaultTarget.toString())
    }
    var percentInput by remember { mutableStateOf("5") }
    var isPushEnabled by remember { mutableStateOf(true) }
    var testFeedbackSent by remember { mutableStateOf(false) }

    val currentToman = item.priceToman
    val parsedTarget = targetPriceInput.toLongOrNull() ?: currentToman
    val diffToman = parsedTarget - currentToman
    val diffPercent = if (currentToman > 0) (diffToman.toDouble() / currentToman.toDouble()) * 100 else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null,
        modifier = modifier.testTag("custom_price_alert_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Header: Title, Dismiss & Asset Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "تنظیم هشدار سفارشی پوش (Firebase)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${item.nameFa} (${item.symbol})",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    AssetIconBadge(item = item, size = 42.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Price Banner & FCM Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceLight)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FintechGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سرویس FCM فعال",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "تاپیک: price_alert_${item.symbol.lowercase()}",
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "قیمت لحظه‌ای بازار",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                        Text(
                            text = Formatters.formatToman(currentToman),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = FintechGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Condition Selector (Above, Below, Volatility)
            Text(
                text = "شرط ماشه هشدار (Trigger Condition)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertCondition.values().forEach { condition ->
                    val isSelected = selectedCondition == condition
                    val activeColor = when (condition) {
                        AlertCondition.ABOVE -> FintechGreen
                        AlertCondition.BELOW -> FintechRed
                        AlertCondition.PERCENT_CHANGE -> FintechCyan
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.16f) else colors.surfaceLight
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) activeColor else colors.surfaceBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                selectedCondition = condition
                                targetPriceInput = when (condition) {
                                    AlertCondition.ABOVE -> (currentToman * 1.05).toLong().toString()
                                    AlertCondition.BELOW -> (currentToman * 0.95).toLong().toString()
                                    AlertCondition.PERCENT_CHANGE -> currentToman.toString()
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = condition.icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (condition) {
                                    AlertCondition.ABOVE -> "صعود به بالا"
                                    AlertCondition.BELOW -> "نزول به زیر"
                                    AlertCondition.PERCENT_CHANGE -> "نوسان شدید"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) activeColor else colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Preset % Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val presets = when (selectedCondition) {
                    AlertCondition.ABOVE -> listOf(2, 5, 10, 15)
                    AlertCondition.BELOW -> listOf(-2, -5, -10, -15)
                    AlertCondition.PERCENT_CHANGE -> listOf(3, 5, 7, 10)
                }

                Text(
                    text = "میانبر درصد:",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { pct ->
                        val label = if (pct > 0) "+$pct%" else "$pct%"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceLight)
                                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (selectedCondition == AlertCondition.PERCENT_CHANGE) {
                                        percentInput = kotlin.math.abs(pct).toString()
                                    } else {
                                        val factor = 1.0 + (pct / 100.0)
                                        val newTarget = (currentToman * factor).toLong()
                                        targetPriceInput = newTarget.toString()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pct > 0) FintechGreen else FintechRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Price Input
            if (selectedCondition != AlertCondition.PERCENT_CHANGE) {
                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { targetPriceInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("قیمت هدف مورد نظر (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechGold,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_price_threshold_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Calculated difference preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val diffSign = if (diffPercent >= 0) "+" else ""
                    val diffColor = if (diffPercent >= 0) FintechGreen else FintechRed
                    Text(
                        text = "فاصله با بازار: $diffSign${"%.2f".format(diffPercent)}٪ (${Formatters.formatToman(kotlin.math.abs(diffToman))})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = diffColor
                    )
                }
            } else {
                OutlinedTextField(
                    value = percentInput,
                    onValueChange = { percentInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("حداقل درصد نوسان ۲۴ ساعته (٪)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechCyan,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_percent_threshold_input"),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // High Priority Push Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceLight)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Switch(
                    checked = isPushEnabled,
                    onCheckedChange = { isPushEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FintechGold,
                        checkedTrackColor = FintechGold.copy(alpha = 0.35f)
                    )
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "پوش نوتیفیکیشن فوری (Heads-up Push)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "ارسال با سرویس FCM حتی در صورت بسته بودن برنامه",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test FCM Push Button
            Button(
                onClick = {
                    val alertEntity = PriceAlertEntity(
                        assetId = item.id,
                        assetSymbol = item.symbol,
                        assetNameFa = item.nameFa,
                        targetPriceToman = parsedTarget,
                        targetPriceUsd = item.priceUsd,
                        condition = selectedCondition,
                        percentThreshold = percentInput.toDoubleOrNull() ?: 5.0,
                        isEnabled = true,
                        fcmTopic = "price_alert_${item.symbol.lowercase()}"
                    )
                    onTestNotification(alertEntity)
                    testFeedbackSent = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_fcm_push_button"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, FintechCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = FintechCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (testFeedbackSent) "✅ پیام آزمایشی پوش ارسال شد!" else "تست ارسال نوتیفیکیشن پوش (Test FCM Push)",
                        color = FintechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Alert Button
            Button(
                onClick = {
                    val alertEntity = PriceAlertEntity(
                        assetId = item.id,
                        assetSymbol = item.symbol,
                        assetNameFa = item.nameFa,
                        targetPriceToman = parsedTarget,
                        targetPriceUsd = item.priceUsd,
                        condition = selectedCondition,
                        percentThreshold = percentInput.toDoubleOrNull() ?: 5.0,
                        isEnabled = isPushEnabled,
                        fcmTopic = "price_alert_${item.symbol.lowercase()}"
                    )
                    onSaveAlert(alertEntity)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_price_alert_fcm_button"),
                colors = ButtonDefaults.buttonColors(containerColor = FintechGold),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "ثبت هشدار قیمتی در Firebase Messaging",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}
