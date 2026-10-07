package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin

private data class FloatingParticle(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val symbol: String,
    val size: Float,
    val alphaFactor: Float
)

@Composable
fun AnimatedFintechBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    isAnimationEnabled: Boolean = true,
    scrollOffset: Float = 0f,
    scrollVelocity: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AppTheme.colors

    // 1. Infinite transition for ambient floating breathing effect
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_mesh")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    val waveScroll by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scroll"
    )

    val floatingDrift by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_drift"
    )

    // Cached particle points representing major market assets
    val particles = remember {
        listOf(
            FloatingParticle(0.12f, 0.18f, 1.25f, "$", 19f, 0.50f),
            FloatingParticle(0.88f, 0.14f, 0.95f, "€", 17f, 0.45f),
            FloatingParticle(0.72f, 0.42f, 1.45f, "₿", 23f, 0.55f),
            FloatingParticle(0.22f, 0.62f, 1.05f, "¥", 18f, 0.40f),
            FloatingParticle(0.85f, 0.72f, 1.35f, "£", 19f, 0.45f),
            FloatingParticle(0.38f, 0.82f, 0.85f, "💎", 16f, 0.50f),
            FloatingParticle(0.15f, 0.88f, 1.15f, "🪙", 17f, 0.45f),
            FloatingParticle(0.58f, 0.28f, 0.75f, "₮", 20f, 0.48f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .testTag("animated_fintech_background")
    ) {
        if (isAnimationEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // --- 1. Scroll-Reactive Parallax Aurora Orbs ---
                // Parallax shift calculation based on user scroll offset
                val parallaxY1 = -scrollOffset * 0.26f
                val parallaxY2 = -scrollOffset * 0.42f
                val parallaxY3 = -scrollOffset * 0.16f
                val parallaxX1 = sin(scrollOffset * 0.0028f) * 36f
                val parallaxX2 = -sin(scrollOffset * 0.0024f) * 44f

                // Top-Right Cyan Glow Orb
                val orb1X = width * (0.35f + 0.16f * cos(pulsePhase)) + parallaxX1
                val baseOrb1Y = height * (0.22f + 0.10f * sin(pulsePhase * 0.8f)) + (parallaxY1 % (height * 1.6f))
                val orb1Y = (baseOrb1Y % height + height) % height
                val orb1Radius = width * 0.68f

                // Mid-Left Fintech Gold Glow Orb
                val orb2X = width * (0.78f + 0.14f * sin(pulsePhase * 0.9f)) + parallaxX2
                val baseOrb2Y = height * (0.65f + 0.12f * cos(pulsePhase * 0.7f)) + (parallaxY2 % (height * 1.6f))
                val orb2Y = (baseOrb2Y % height + height) % height
                val orb2Radius = width * 0.72f

                // Bottom Center Violet Glow Orb
                val orb3X = width * (0.25f + 0.10f * sin(pulsePhase * 1.1f))
                val baseOrb3Y = height * (0.82f + 0.08f * cos(pulsePhase * 0.9f)) + (parallaxY3 % (height * 1.6f))
                val orb3Y = (baseOrb3Y % height + height) % height
                val orb3Radius = width * 0.58f

                if (isDarkTheme) {
                    // Dark Theme: Radiant Deep Neon Fintech Aurora Glows
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.10f),
                                Color(0xFF00E676).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(orb1X, orb1Y),
                            radius = orb1Radius
                        ),
                        center = Offset(orb1X, orb1Y),
                        radius = orb1Radius
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB800).copy(alpha = 0.08f),
                                Color(0xFFB388FF).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(orb2X, orb2Y),
                            radius = orb2Radius
                        ),
                        center = Offset(orb2X, orb2Y),
                        radius = orb2Radius
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7C4DFF).copy(alpha = 0.08f),
                                Color(0xFF00E5FF).copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = orb3Radius
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = orb3Radius
                    )
                } else {
                    // Light Theme: Soft Luminous Pastel Fintech Atmosphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00B0FF).copy(alpha = 0.11f),
                                Color(0xFF00E676).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(orb1X, orb1Y),
                            radius = orb1Radius
                        ),
                        center = Offset(orb1X, orb1Y),
                        radius = orb1Radius
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB800).copy(alpha = 0.09f),
                                Color(0xFF8B5CF6).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(orb2X, orb2Y),
                            radius = orb2Radius
                        ),
                        center = Offset(orb2X, orb2Y),
                        radius = orb2Radius
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.07f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = orb3Radius
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = orb3Radius
                    )
                }

                // --- 2. Scroll-Moving Ambient Market Waves ---
                drawAnimatedMarketWave(
                    width = width,
                    height = height,
                    progress = waveScroll + (scrollOffset * 0.0005f),
                    scrollOffsetY = -scrollOffset * 0.32f,
                    isDark = isDarkTheme
                )

                drawSecondaryMarketWave(
                    width = width,
                    height = height,
                    progress = (waveScroll * 1.35f) + (scrollOffset * 0.0008f),
                    scrollOffsetY = -scrollOffset * 0.18f,
                    isDark = isDarkTheme
                )

                // --- 3. Subtle Parallax Grid Lines ---
                drawParallaxGrid(
                    width = width,
                    height = height,
                    scrollOffset = scrollOffset,
                    isDark = isDarkTheme
                )

                // --- 4. Floating Currency Particles (3D Parallax Depth) ---
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                particles.forEach { p ->
                    val posX = p.initialX * width + floatingDrift * (p.speed * 0.3f) + sin((scrollOffset + p.initialY * 600f) * 0.0025f) * 22f
                    val scrollShift = scrollOffset * p.speed * 0.38f
                    val rawY = (p.initialY * height - (pulsePhase * 25f * p.speed) - scrollShift)
                    val posY = (rawY % height + height) % height

                    paint.textSize = p.size * density
                    paint.color = if (isDarkTheme) {
                        android.graphics.Color.argb(
                            (p.alphaFactor * 48).toInt(),
                            255, 255, 255
                        )
                    } else {
                        android.graphics.Color.argb(
                            (p.alphaFactor * 58).toInt(),
                            15, 23, 42
                        )
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        p.symbol,
                        posX,
                        posY,
                        paint
                    )
                }
            }
        }

        // Screen content rendered on top
        content()
    }
}

private fun DrawScope.drawAnimatedMarketWave(
    width: Float,
    height: Float,
    progress: Float,
    scrollOffsetY: Float,
    isDark: Boolean
) {
    val path = Path()
    val rawBaseWaveY = height * 0.38f + scrollOffsetY
    val baseWaveY = (rawBaseWaveY % height + height) % height
    val waveAmp = 26f
    val waveLength = width * 0.88f

    path.moveTo(0f, baseWaveY)

    var x = 0f
    val step = 14f
    while (x <= width) {
        val y = baseWaveY + sin((x / waveLength + progress) * 6.28318f) * waveAmp
        path.lineTo(x, y)
        x += step
    }

    val waveColor = if (isDark) {
        Color(0xFF00E5FF).copy(alpha = 0.07f)
    } else {
        Color(0xFF00B0FF).copy(alpha = 0.08f)
    }

    drawPath(
        path = path,
        color = waveColor,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawSecondaryMarketWave(
    width: Float,
    height: Float,
    progress: Float,
    scrollOffsetY: Float,
    isDark: Boolean
) {
    val path = Path()
    val rawBaseWaveY = height * 0.68f + scrollOffsetY
    val baseWaveY = (rawBaseWaveY % height + height) % height
    val waveAmp = 20f
    val waveLength = width * 0.75f

    path.moveTo(0f, baseWaveY)

    var x = 0f
    val step = 16f
    while (x <= width) {
        val y = baseWaveY + cos((x / waveLength + progress) * 6.28318f) * waveAmp
        path.lineTo(x, y)
        x += step
    }

    val waveColor = if (isDark) {
        Color(0xFFFFB800).copy(alpha = 0.05f)
    } else {
        Color(0xFFFFB800).copy(alpha = 0.07f)
    }

    drawPath(
        path = path,
        color = waveColor,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun DrawScope.drawParallaxGrid(
    width: Float,
    height: Float,
    scrollOffset: Float,
    isDark: Boolean
) {
    val gridSpacing = 64f
    val lineAlpha = if (isDark) 0.035f else 0.045f
    val lineColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF00B0FF)

    val offsetY = -(scrollOffset * 0.25f) % gridSpacing
    var y = offsetY
    while (y < height) {
        if (y >= 0) {
            drawLine(
                color = lineColor.copy(alpha = lineAlpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        y += gridSpacing
    }
}
