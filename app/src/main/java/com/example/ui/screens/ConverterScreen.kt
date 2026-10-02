package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.ExchangeItem
import com.example.ui.components.AssetIconBadge
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    items: List<ExchangeItem>,
    fromItem: ExchangeItem?,
    toItem: ExchangeItem?,
    amountString: String,
    onAmountChange: (String) -> Unit,
    onSwap: () -> Unit,
    onSelectFrom: (ExchangeItem) -> Unit,
    onSelectTo: (ExchangeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSelectingFrom by remember { mutableStateOf(false) }
    var isSelectingTo by remember { mutableStateOf(false) }
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sourceItem = fromItem ?: items.firstOrNull()
    val targetItem = toItem ?: items.getOrNull(1)

    // Calculation logic:
    // Amount in Source Asset -> Total Tomans = amount * source.priceToman
    // Result in Target Asset = Total Tomans / target.priceToman
    val inputAmount = amountString.toDoubleOrNull() ?: 0.0
    val totalToman = if (sourceItem != null) (inputAmount * sourceItem.priceToman).toLong() else 0L
    val targetUnits = if (targetItem != null && targetItem.priceToman > 0) {
        (inputAmount * sourceItem!!.priceToman) / targetItem.priceToman.toDouble()
    } else 0.0

    val totalUsd = if (sourceItem != null) inputAmount * sourceItem.priceUsd else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "ماشین‌حساب و تبدیل ارز هوشمند",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "تبدیل لحظه‌ای انواع دلار، تتر، کریپتو، طلا و سکه به تومان و برعکس",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.End
                )
            }
        }

        // Amount Input Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مقدار اولیه جهت تبدیل",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountString,
                        onValueChange = { newVal ->
                            // Allow numbers and decimal point
                            if (newVal.isEmpty() || newVal.matches(Regex("^\\d*\\.?\\d*$"))) {
                                onAmountChange(newVal)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_amount_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FintechCyan,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = {
                            Text(
                                "مثلاً 100",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = TextMuted
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCardLight,
                            unfocusedContainerColor = SurfaceCardLight,
                            focusedBorderColor = FintechCyan,
                            unfocusedBorderColor = SurfaceCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Preset Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1", "10", "100", "500", "1000", "5000").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceCardLight)
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { onAmountChange(preset) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Converter Source Card (از ارز)
        item {
            CurrencySelectCard(
                title = "از ارز (مبدأ)",
                item = sourceItem,
                amountFormatted = amountString,
                onClick = { isSelectingFrom = true },
                tag = "source_currency_selector"
            )
        }

        // Swap Button in Center
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(FintechCyan)
                        .testTag("swap_currencies_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "جابجایی ارزها",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Converter Target Card (به ارز)
        item {
            val formattedResult = if (targetUnits >= 100) {
                DecimalFormat("#,##0.00").format(targetUnits)
            } else if (targetUnits >= 0.001) {
                DecimalFormat("#,##0.0000").format(targetUnits)
            } else {
                DecimalFormat("0.000000").format(targetUnits)
            }

            CurrencySelectCard(
                title = "به ارز (مقصد)",
                item = targetItem,
                amountFormatted = formattedResult,
                onClick = { isSelectingTo = true },
                tag = "target_currency_selector",
                isResult = true
            )
        }

        // Comprehensive Conversion Summary Card
        item {
            if (sourceItem != null && targetItem != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "خلاصه و معادل ارزش بازار",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = FintechGold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SummaryRow(label = "ارزش کل به تومان", value = Formatters.formatToman(totalToman))
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow(label = "ارزش کل به ریال", value = Formatters.formatToman(totalToman, inRials = true))
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow(label = "معادل دلاری (USD)", value = Formatters.formatUsd(totalUsd))
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceCardBorder))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "نرخ برابری: ۱ ${sourceItem.symbol} = ${Formatters.formatToman(sourceItem.priceToman)} | ۱ ${targetItem.symbol} = ${Formatters.formatToman(targetItem.priceToman)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextMuted,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Currency Selector Bottom Sheet
    if (isSelectingFrom || isSelectingTo) {
        ModalBottomSheet(
            onDismissRequest = {
                isSelectingFrom = false
                isSelectingTo = false
            },
            sheetState = pickerSheetState,
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isSelectingFrom) "انتخاب ارز مبدأ" else "انتخاب ارز مقصد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCardLight)
                                .clickable {
                                    if (isSelectingFrom) {
                                        onSelectFrom(item)
                                        isSelectingFrom = false
                                    } else {
                                        onSelectTo(item)
                                        isSelectingTo = false
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Formatters.formatToman(item.priceToman),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )

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
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                AssetIconBadge(item = item, modifier = Modifier.size(34.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencySelectCard(
    title: String,
    item: ExchangeItem?,
    amountFormatted: String,
    onClick: () -> Unit,
    tag: String,
    isResult: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Amount
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = amountFormatted,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = if (isResult) FintechGreen else TextPrimary
                )
                if (item != null) {
                    Text(
                        text = "۱ ${item.symbol} = ${Formatters.formatToman(item.priceToman)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Asset Picker Trigger
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = item?.nameFa ?: "انتخاب ارز",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = item?.symbol ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FintechCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (item != null) {
                    AssetIconBadge(item = item, modifier = Modifier.size(38.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}
