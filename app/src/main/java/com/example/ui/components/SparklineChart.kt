package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
    strokeWidth: Float = 3.5f
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
        var lastX = 0f
        var lastY = 0f

        for (i in points.indices) {
            val normY = ((points[i] - minVal) / range).toFloat()
            val y = h - (normY * (h * 0.76f) + (h * 0.12f))
            val x = i * stepX

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevNormY = ((points[i - 1] - minVal) / range).toFloat()
                val prevY = h - (prevNormY * (h * 0.76f) + (h * 0.12f))

                val cX1 = prevX + (x - prevX) / 2f
                val cY1 = prevY
                val cX2 = prevX + (x - prevX) / 2f
                val cY2 = y

                path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
            }

            if (i == points.size - 1) {
                lastX = x
                lastY = y
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

        // Subtle glowing dot at the end
        if (points.isNotEmpty()) {
            drawCircle(
                color = lineColor.copy(alpha = 0.35f),
                radius = strokeWidth * 2.2f,
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
            drawCircle(
                color = lineColor,
                radius = strokeWidth * 1.1f,
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
        }
    }
}
