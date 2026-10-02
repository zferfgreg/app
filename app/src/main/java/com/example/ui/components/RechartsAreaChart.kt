package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeItem
import com.example.data.model.HistoricalPoint
import com.example.data.model.TimeFrame
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ChartDataGenerator
import com.example.util.Formatters

@Composable
fun RechartsAreaChart(
    item: ExchangeItem,
    modifier: Modifier = Modifier
) {
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.H24) }
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
    val gradientColor = if (isPositive) FintechGreenBg else FintechRedBg

    // Animate path reveal on timeframe change
    val animProgress = remember(selectedTimeFrame) { Animatable(0f) }
    LaunchedEffect(selectedTimeFrame) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(500))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
            .testTag("recharts_trend_chart_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Timeframe Selector Buttons & Library Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // TimeFrame Selector Tabs (24H, 7D, 1M, 3M, 1Y)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBg)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeFrame.values().forEach { tf ->
                        val isSelected = selectedTimeFrame == tf
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FintechCyan else Color.Transparent)
                                .clickable {
                                    selectedTimeFrame = tf
                                    activePointIndex = null
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("timeframe_${tf.labelEn}")
                        ) {
                            Text(
                                text = tf.labelEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                // Chart Title & Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "نمودار تحلیلی قیمت",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "روند قیمتی ${selectedTimeFrame.labelFa}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-style Tooltip / Interactive Value Display Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBg)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Return / Change Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
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
                        text = "بازدهی",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                // Selected Point Timestamp and Price
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Formatters.formatUsd(activePoint?.priceUsd ?: item.priceUsd),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatToman(activePoint?.priceToman ?: item.priceToman),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = activePoint?.timeLabel ?: "اکنون",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = FintechCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Recharts Area Canvas with Scrubbing / Crosshairs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkBg)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
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
                            onDragEnd = {
                                activePointIndex = null
                            },
                            onDragCancel = {
                                activePointIndex = null
                            }
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
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 10.dp)) {
                    if (points.size < 2) return@Canvas

                    val minVal = points.minOf { it.priceToman }.toDouble()
                    val maxVal = points.maxOf { it.priceToman }.toDouble()
                    val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

                    val w = size.width
                    val h = size.height
                    val volumeAreaHeight = h * 0.22f
                    val chartHeight = h * 0.74f

                    val stepX = w / (points.size - 1)

                    // 1. Draw Volume Bars at bottom (Recharts ComposedChart style)
                    points.forEachIndexed { i, p ->
                        val barHeight = p.volumeNormalized * volumeAreaHeight
                        val barX = i * stepX
                        val barY = h - barHeight
                        drawLine(
                            color = mainColor.copy(alpha = 0.25f),
                            start = Offset(barX, h),
                            end = Offset(barX, barY),
                            strokeWidth = (stepX * 0.6f).coerceIn(2f, 12f),
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Draw Dotted Reference Lines (High & Low)
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    // High reference line (y = 0)
                    drawLine(
                        color = FintechGreen.copy(alpha = 0.35f),
                        start = Offset(0f, 10f),
                        end = Offset(w, 10f),
                        strokeWidth = 1.5f,
                        pathEffect = dashedEffect
                    )

                    // Low reference line (y = chartHeight)
                    drawLine(
                        color = FintechRed.copy(alpha = 0.35f),
                        start = Offset(0f, chartHeight),
                        end = Offset(w, chartHeight),
                        strokeWidth = 1.5f,
                        pathEffect = dashedEffect
                    )

                    // 3. Draw Bezier Curve and Gradient Fill
                    val path = Path()
                    val fillPath = Path()
                    val visibleCount = ((points.size - 1) * animProgress.value).toInt().coerceAtLeast(1)

                    for (i in 0..visibleCount) {
                        val normY = ((points[i].priceToman - minVal) / range).toFloat()
                        val y = chartHeight - (normY * (chartHeight * 0.85f))
                        val x = i * stepX

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, chartHeight + 10f)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevNormY = ((points[i - 1].priceToman - minVal) / range).toFloat()
                            val prevY = chartHeight - (prevNormY * (chartHeight * 0.85f))

                            val cX1 = prevX + (x - prevX) / 2f
                            val cY1 = prevY
                            val cX2 = prevX + (x - prevX) / 2f
                            val cY2 = y

                            path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                            fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                        }
                    }

                    val lastVisibleX = visibleCount * stepX
                    fillPath.lineTo(lastVisibleX, chartHeight + 10f)
                    fillPath.close()

                    // Draw Gradient Area under Curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(mainColor.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = chartHeight + 10f
                        )
                    )

                    // Draw Main Price Curve Line
                    drawPath(
                        path = path,
                        color = mainColor,
                        style = Stroke(
                            width = 3.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 4. Draw Interactive Cursor and Dot if user is touching/scrubbing
                    activePointIndex?.let { idx ->
                        val clampedIdx = idx.coerceIn(0, points.size - 1)
                        val normY = ((points[clampedIdx].priceToman - minVal) / range).toFloat()
                        val cursorX = clampedIdx * stepX
                        val cursorY = chartHeight - (normY * (chartHeight * 0.85f))

                        // Vertical dashed crosshair line
                        drawLine(
                            color = FintechCyan.copy(alpha = 0.7f),
                            start = Offset(cursorX, 0f),
                            end = Offset(cursorX, h),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )

                        // Outer glowing pulse ring
                        drawCircle(
                            color = FintechCyan.copy(alpha = 0.3f),
                            radius = 12f,
                            center = Offset(cursorX, cursorY)
                        )

                        // Inner solid dot
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(cursorX, cursorY)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Period Statistics Grid (High, Low, Average)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minPoint = points.minByOrNull { it.priceToman }
                val maxPoint = points.maxByOrNull { it.priceToman }
                val avgPrice = if (points.isNotEmpty()) points.map { it.priceToman }.average().toLong() else item.priceToman

                StatBox(label = "کف دوره", value = Formatters.formatToman(minPoint?.priceToman ?: item.low24hToman), color = FintechRed)
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceCardBorder))
                StatBox(label = "میانگین دوره", value = Formatters.formatToman(avgPrice), color = TextSecondary)
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceCardBorder))
                StatBox(label = "سقف دوره", value = Formatters.formatToman(maxPoint?.priceToman ?: item.high24hToman), color = FintechGreen)
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = color
        )
    }
}
