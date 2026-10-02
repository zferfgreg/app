package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg

@Composable
fun SparklineChart(
    points: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    showGradient: Boolean = true,
    strokeWidth: Float = 4f
) {
    val lineColor = if (isPositive) FintechGreen else FintechRed
    val gradientColor = if (isPositive) FintechGreenBg else FintechRedBg

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val minVal = points.minOrNull() ?: 0.0
        val maxVal = points.maxOrNull() ?: 1.0
        val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

        val w = size.width
        val h = size.height
        val stepX = w / (points.size - 1)

        val path = Path()
        val fillPath = Path()

        for (i in points.indices) {
            val normY = ((points[i] - minVal) / range).toFloat()
            // Invert Y so highest price is at top (y=0)
            val y = h - (normY * (h * 0.8f) + (h * 0.1f))
            val x = i * stepX

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevNormY = ((points[i - 1] - minVal) / range).toFloat()
                val prevY = h - (prevNormY * (h * 0.8f) + (h * 0.1f))

                val cX1 = prevX + (x - prevX) / 2f
                val cY1 = prevY
                val cX2 = prevX + (x - prevX) / 2f
                val cY2 = y

                path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
            }
        }

        if (showGradient) {
            fillPath.lineTo(w, h)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColor, Color.Transparent),
                    startY = 0f,
                    endY = h
                )
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
