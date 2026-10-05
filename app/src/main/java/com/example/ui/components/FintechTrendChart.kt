package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChartType
import com.example.data.model.ExchangeItem
import com.example.data.model.HistoricalPoint
import com.example.data.model.TimeFrame
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.util.ChartDataGenerator
import com.example.util.Formatters
import kotlin.math.max
import kotlin.math.min

/**
 * Advanced Interactive Financial Chart Component
 * Visualizes historical price trends for currencies, commodities, and cryptos with:
 * 1. Spline / Area Chart with gradient fill & pulsing live indicator
 * 2. Japanese Candlestick (OHLC) charting mode
 * 3. Technical Indicators (MA7 & MA25 Moving Averages + Volume bars)
 * 4. Interactive touch scrubbing with crosshairs and precision tooltip
 * 5. Full Material 3 Light/Dark adaptive theming
 */
@Composable
fun FintechTrendChart(
    item: ExchangeItem,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.H24) }
    var selectedChartType by remember { mutableStateOf(ChartType.AREA) }

    val points = remember(item.id, item.priceToman, selectedTimeFrame) {
        ChartDataGenerator.generatePoints(item, selectedTimeFrame)
    }

    var activePointIndex by remember { mutableStateOf<Int?>(null) }
    val activePoint = activePointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()

    val firstPrice = points.firstOrNull()?.priceToman ?: 1L
    val currentPrice = activePoint?.priceToman ?: item.priceToman
    val periodChangePercent = if (firstPrice > 0) {
        ((currentPrice - firstPrice).toDouble() / firstPrice.toDouble()) * 100.0
    } else 0.0

    val isPositive = periodChangePercent >= 0
    val mainColor = if (isPositive) FintechGreen else FintechRed

    // Animated path entrance progress
    val animProgress = remember(selectedTimeFrame, selectedChartType) { Animatable(0f) }
    LaunchedEffect(selectedTimeFrame, selectedChartType) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
    }

    // Pulsing dot on live price point
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(22.dp))
            .testTag("fintech_trend_chart_card"),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Chart Type Switcher & Timeframe selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Chart Type Switcher (Area, Candlestick, MA Indicators)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartType.values().forEach { cType ->
                        val isSelected = selectedChartType == cType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) FintechCyan.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clickable {
                                    selectedChartType = cType
                                    activePointIndex = null
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("chart_type_${cType.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = cType.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cType.labelFa,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) FintechCyan else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Title & Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "نمودار تحلیل قیمت",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "روند ${selectedTimeFrame.labelFa}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedChartType) {
                                ChartType.AREA -> Icons.Default.ShowChart
                                ChartType.CANDLESTICK -> Icons.Default.CandlestickChart
                                ChartType.INDICATORS -> Icons.Default.QueryStats
                            },
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TimeFrame Selector Tabs (1H, 24H, 7D, 1M, 3M, 1Y, ALL)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeFrame.values().forEach { tf ->
                    val isSelected = selectedTimeFrame == tf
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) FintechGold else Color.Transparent)
                            .clickable {
                                selectedTimeFrame = tf
                                activePointIndex = null
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("timeframe_${tf.labelEn}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.labelEn,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Point / Tooltip Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Change indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = mainColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Formatters.formatPercent(periodChangePercent),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = mainColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "بازدهی دوره",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }

                // Selected Point Timestamp and Price
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Formatters.formatUsd(activePoint?.priceUsd ?: item.priceUsd),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatToman(activePoint?.priceToman ?: item.priceToman),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = activePoint?.timeLabel ?: "اکنون",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = FintechCyan
                    )
                }
            }

            // MA Indicator Legend (if in indicators mode)
            if (selectedChartType == ChartType.INDICATORS) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(FintechCyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MA(25): ${Formatters.formatToman(activePoint?.ma25Toman ?: 0L)}",
                            fontSize = 10.sp,
                            color = FintechCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(FintechGold))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MA(7): ${Formatters.formatToman(activePoint?.ma7Toman ?: 0L)}",
                            fontSize = 10.sp,
                            color = FintechGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Interactive Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
                    .pointerInput(points) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                                activePointIndex = index
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                val index = (change.position.x / stepX).toInt().coerceIn(0, points.size - 1)
                                activePointIndex = index
                            },
                            onDragEnd = { activePointIndex = null },
                            onDragCancel = { activePointIndex = null }
                        )
                    }
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                            activePointIndex = index
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (points.isEmpty()) return@Canvas

                    val progress = animProgress.value
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 34f
                    val topPadding = 24f
                    val chartHeight = height - bottomPadding - topPadding

                    // Determine min/max values
                    val allHighs = points.map { it.highToman }
                    val allLows = points.map { it.lowToman }
                    val minPrice = allLows.minOrNull() ?: 1L
                    val maxPrice = allHighs.maxOrNull() ?: (minPrice + 100)
                    val priceRange = (maxPrice - minPrice).coerceAtLeast(1L).toFloat()

                    // Draw subtle grid lines (3 horizontal levels)
                    val gridColor = colors.surfaceBorder.copy(alpha = 0.5f)
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    for (step in 1..3) {
                        val gridY = topPadding + (chartHeight * (step / 4f))
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f,
                            pathEffect = dashedEffect
                        )
                    }

                    // Draw Volume bars at the bottom
                    val barWidth = (width / points.size).coerceAtLeast(2f)
                    points.forEachIndexed { i, pt ->
                        val x = i * (width / (points.size - 1).coerceAtLeast(1))
                        val volHeight = (pt.volumeNormalized * 28f) * progress
                        val isUp = pt.closeToman >= pt.openToman
                        val volColor = if (isUp) FintechGreen.copy(alpha = 0.28f) else FintechRed.copy(alpha = 0.28f)

                        drawRect(
                            color = volColor,
                            topLeft = Offset(x - (barWidth / 2), height - volHeight - 4f),
                            size = Size(barWidth * 0.8f, volHeight)
                        )
                    }

                    // Render chosen Chart Type
                    when (selectedChartType) {
                        ChartType.CANDLESTICK -> {
                            // Render Japanese Candlesticks
                            val candleWidth = ((width / points.size) * 0.65f).coerceIn(3f, 14f)

                            points.forEachIndexed { i, pt ->
                                val x = i * (width / (points.size - 1).coerceAtLeast(1))
                                val isBullish = pt.closeToman >= pt.openToman
                                val candleColor = if (isBullish) FintechGreen else FintechRed

                                val highY = topPadding + (chartHeight * (1f - ((pt.highToman - minPrice) / priceRange))) * progress
                                val lowY = topPadding + (chartHeight * (1f - ((pt.lowToman - minPrice) / priceRange))) * progress

                                val openY = topPadding + (chartHeight * (1f - ((pt.openToman - minPrice) / priceRange))) * progress
                                val closeY = topPadding + (chartHeight * (1f - ((pt.closeToman - minPrice) / priceRange))) * progress

                                val topBody = min(openY, closeY)
                                val bottomBody = max(openY, closeY)
                                val bodyHeight = (bottomBody - topBody).coerceAtLeast(2f)

                                // Draw wick
                                drawLine(
                                    color = candleColor,
                                    start = Offset(x, highY),
                                    end = Offset(x, lowY),
                                    strokeWidth = 1.5f
                                )

                                // Draw candle body
                                drawRect(
                                    color = candleColor,
                                    topLeft = Offset(x - (candleWidth / 2), topBody),
                                    size = Size(candleWidth, bodyHeight)
                                )
                            }
                        }

                        ChartType.AREA, ChartType.INDICATORS -> {
                            // Calculate Coordinates for Spline
                            val coords = points.mapIndexed { i, pt ->
                                val x = i * (width / (points.size - 1).coerceAtLeast(1))
                                val yNorm = 1f - ((pt.priceToman - minPrice) / priceRange)
                                val y = topPadding + (chartHeight * yNorm)
                                Offset(x, y)
                            }

                            // Build smooth curve path
                            val curvePath = Path()
                            val fillPath = Path()

                            if (coords.isNotEmpty()) {
                                curvePath.moveTo(coords[0].x, coords[0].y)
                                fillPath.moveTo(coords[0].x, height)
                                fillPath.lineTo(coords[0].x, coords[0].y)

                                for (i in 0 until coords.size - 1) {
                                    val current = coords[i]
                                    val next = coords[i + 1]
                                    val midX = (current.x + next.x) / 2f
                                    curvePath.cubicTo(
                                        midX, current.y,
                                        midX, next.y,
                                        next.x, next.y
                                    )
                                    fillPath.cubicTo(
                                        midX, current.y,
                                        midX, next.y,
                                        next.x, next.y
                                    )
                                }

                                fillPath.lineTo(width, height)
                                fillPath.close()

                                // Draw Area Gradient Fill
                                val gradientBrush = Brush.verticalGradient(
                                    colors = listOf(
                                        mainColor.copy(alpha = 0.35f * progress),
                                        mainColor.copy(alpha = 0.05f * progress),
                                        Color.Transparent
                                    ),
                                    startY = topPadding,
                                    endY = height
                                )
                                drawPath(path = fillPath, brush = gradientBrush, style = Fill)

                                // Draw Main Spline Line
                                drawPath(
                                    path = curvePath,
                                    color = mainColor.copy(alpha = progress),
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                // Draw Technical Overlays (MA7 and MA25) if in Indicators mode
                                if (selectedChartType == ChartType.INDICATORS) {
                                    // MA7 path
                                    val ma7Path = Path()
                                    points.forEachIndexed { i, pt ->
                                        val x = i * (width / (points.size - 1).coerceAtLeast(1))
                                        val maVal = pt.ma7Toman ?: pt.priceToman
                                        val y = topPadding + (chartHeight * (1f - ((maVal - minPrice) / priceRange)))
                                        if (i == 0) ma7Path.moveTo(x, y) else ma7Path.lineTo(x, y)
                                    }
                                    drawPath(
                                        path = ma7Path,
                                        color = FintechGold.copy(alpha = 0.9f * progress),
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // MA25 path
                                    val ma25Path = Path()
                                    points.forEachIndexed { i, pt ->
                                        val x = i * (width / (points.size - 1).coerceAtLeast(1))
                                        val maVal = pt.ma25Toman ?: pt.priceToman
                                        val y = topPadding + (chartHeight * (1f - ((maVal - minPrice) / priceRange)))
                                        if (i == 0) ma25Path.moveTo(x, y) else ma25Path.lineTo(x, y)
                                    }
                                    drawPath(
                                        path = ma25Path,
                                        color = FintechCyan.copy(alpha = 0.9f * progress),
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }
                    }

                    // Draw Peak (High) and Trough (Low) Markers
                    val peakIndex = points.indexOfFirst { it.highToman == maxPrice }
                    val troughIndex = points.indexOfFirst { it.lowToman == minPrice }

                    if (peakIndex != -1 && selectedChartType != ChartType.CANDLESTICK) {
                        val peakX = peakIndex * (width / (points.size - 1).coerceAtLeast(1))
                        drawCircle(
                            color = FintechGreen,
                            radius = 3.5.dp.toPx(),
                            center = Offset(peakX, topPadding)
                        )
                    }

                    if (troughIndex != -1 && selectedChartType != ChartType.CANDLESTICK) {
                        val troughX = troughIndex * (width / (points.size - 1).coerceAtLeast(1))
                        drawCircle(
                            color = FintechRed,
                            radius = 3.5.dp.toPx(),
                            center = Offset(troughX, topPadding + chartHeight)
                        )
                    }

                    // Active Crosshair & Scrubbing Indicator
                    if (activePointIndex != null) {
                        val idx = activePointIndex!!
                        val crossX = idx * (width / (points.size - 1).coerceAtLeast(1))
                        val pt = points[idx]
                        val crossY = topPadding + (chartHeight * (1f - ((pt.priceToman - minPrice) / priceRange)))

                        // Vertical Crosshair Line
                        drawLine(
                            color = FintechCyan.copy(alpha = 0.85f),
                            start = Offset(crossX, 0f),
                            end = Offset(crossX, height),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Outer pulsing glow dot
                        drawCircle(
                            color = FintechCyan.copy(alpha = 0.35f),
                            radius = pulseRadius.dp.toPx(),
                            center = Offset(crossX, crossY)
                        )
                        // Inner solid dot
                        drawCircle(
                            color = FintechCyan,
                            radius = 4.5.dp.toPx(),
                            center = Offset(crossX, crossY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(crossX, crossY)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Peak / Trough & Volume Summary Bar
            val maxToman = points.maxOfOrNull { it.highToman } ?: item.high24hToman
            val minToman = points.minOfOrNull { it.lowToman } ?: item.low24hToman

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Low stat
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FintechRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "کف دوره: ", fontSize = 10.sp, color = colors.textMuted)
                    Text(text = Formatters.formatToman(minToman), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = FintechRed)
                }

                // Volume label
                Text(
                    text = "حجم تعاملی • زنده",
                    fontSize = 10.sp,
                    color = colors.textMuted
                )

                // High stat
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FintechGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "سقف دوره: ", fontSize = 10.sp, color = colors.textMuted)
                    Text(text = Formatters.formatToman(maxToman), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = FintechGreen)
                }
            }
        }
    }
}
