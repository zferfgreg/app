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

    // Ambient breathing animations
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_mesh")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )

    val floatingDrift by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_drift"
    )

    val particles = remember {
        listOf(
            FloatingParticle(0.12f, 0.15f, 1.4f, "$", 22f, 0.65f),
            FloatingParticle(0.85f, 0.22f, 1.1f, "€", 19f, 0.60f),
            FloatingParticle(0.75f, 0.40f, 1.6f, "₿", 26f, 0.70f),
            FloatingParticle(0.18f, 0.55f, 1.2f, "¥", 20f, 0.55f),
            FloatingParticle(0.88f, 0.68f, 1.5f, "£", 22f, 0.60f),
            FloatingParticle(0.35f, 0.78f, 0.9f, "💎", 20f, 0.70f),
            FloatingParticle(0.14f, 0.90f, 1.3f, "🪙", 22f, 0.65f),
            FloatingParticle(0.60f, 0.30f, 0.8f, "₮", 24f, 0.68f),
            FloatingParticle(0.45f, 0.60f, 1.35f, "📈", 18f, 0.60f),
            FloatingParticle(0.80f, 0.85f, 1.1f, "⚡", 18f, 0.65f)
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

                // Direct parallax translation tied to scroll
                val scrollParallax1 = -scrollOffset * 0.38f
                val scrollParallax2 = -scrollOffset * 0.24f
                val scrollParallax3 = -scrollOffset * 0.48f

                // Horizontal subtle drift on scroll
                val lateralDrift1 = sin(scrollOffset * 0.002f) * 45f
                val lateralDrift2 = -sin(scrollOffset * 0.0018f) * 55f

                // --- 1. Vibrant Luminous Aurora Glow Orbs ---
                // Orb 1: Neon Cyan Top-Right
                val orb1X = width * 0.45f + cos(pulsePhase) * 40f + lateralDrift1
                val orb1Y = height * 0.18f + sin(pulsePhase * 0.8f) * 30f + scrollParallax1
                val orb1Radius = width * 0.60f

                // Orb 2: Luxury Fintech Gold Mid-Left
                val orb2X = width * 0.70f + sin(pulsePhase * 0.9f) * 35f + lateralDrift2
                val orb2Y = height * 0.52f + cos(pulsePhase * 0.7f) * 35f + scrollParallax2
                val orb2Radius = width * 0.62f

                // Orb 3: Cosmic Violet/Indigo Lower
                val orb3X = width * 0.30f + sin(pulsePhase * 1.1f) * 30f
                val orb3Y = height * 0.85f + cos(pulsePhase * 0.9f) * 25f + scrollParallax3
                val orb3Radius = width * 0.55f

                // Orb 4: Emerald Green Accent
                val orb4X = width * 0.60f - cos(pulsePhase * 0.6f) * 30f
                val orb4Y = height * 1.20f + scrollParallax1
                val orb4Radius = width * 0.50f

                if (isDarkTheme) {
                    // Dark theme: High-contrast glowing neon gradients
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.24f),
                                Color(0xFF00B0FF).copy(alpha = 0.12f),
                                Color(0xFF00E676).copy(alpha = 0.04f),
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
                                Color(0xFFFFB800).copy(alpha = 0.22f),
                                Color(0xFFFFD54F).copy(alpha = 0.10f),
                                Color(0xFFFF2A55).copy(alpha = 0.03f),
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
                                Color(0xFF7C4DFF).copy(alpha = 0.20f),
                                Color(0xFF00E5FF).copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = orb3Radius
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = orb3Radius
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E676).copy(alpha = 0.18f),
                                Color(0xFF00E5FF).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(orb4X, orb4Y),
                            radius = orb4Radius
                        ),
                        center = Offset(orb4X, orb4Y),
                        radius = orb4Radius
                    )
                } else {
                    // Light theme: Rich modern pastel electric glows
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00B0FF).copy(alpha = 0.25f),
                                Color(0xFF69F0AE).copy(alpha = 0.10f),
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
                                Color(0xFFFFB800).copy(alpha = 0.22f),
                                Color(0xFF8B5CF6).copy(alpha = 0.09f),
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
                                Color(0xFF6366F1).copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = orb3Radius
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = orb3Radius
                    )
                }

                // --- 2. Live Neon Market Trend Waves (Active Scroll Movement) ---
                drawFlowingTrendWave(
                    width = width,
                    height = height,
                    progress = waveProgress + (scrollOffset * 0.0006f),
                    offsetY = height * 0.35f + (-scrollOffset * 0.30f),
                    color = if (isDarkTheme) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color(0xFF00B0FF).copy(alpha = 0.25f),
                    strokeWidthPx = 3.dp.toPx()
                )

                drawFlowingTrendWave(
                    width = width,
                    height = height,
                    progress = (waveProgress * 1.3f) + (scrollOffset * 0.0009f),
                    offsetY = height * 0.65f + (-scrollOffset * 0.42f),
                    color = if (isDarkTheme) Color(0xFFFFB800).copy(alpha = 0.18f) else Color(0xFFFFB800).copy(alpha = 0.20f),
                    strokeWidthPx = 2.dp.toPx()
                )

                // --- 3. Geometric Trading Grid Mesh (Moves with Scroll) ---
                drawDynamicTradingGrid(
                    width = width,
                    height = height,
                    scrollOffset = scrollOffset,
                    isDark = isDarkTheme
                )

                // --- 4. Floating Currency & Market Symbols (3D Depth Parallax) ---
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                particles.forEach { p ->
                    val posX = p.initialX * width + floatingDrift * (p.speed * 0.4f)
                    val scrollShift = scrollOffset * p.speed * 0.45f
                    val rawY = (p.initialY * height - scrollShift)
                    val posY = (rawY % (height * 1.5f) + (height * 1.5f)) % (height * 1.5f) - (height * 0.25f)

                    paint.textSize = p.size * density
                    paint.color = if (isDarkTheme) {
                        android.graphics.Color.argb(
                            (p.alphaFactor * 90).toInt(),
                            255, 255, 255
                        )
                    } else {
                        android.graphics.Color.argb(
                            (p.alphaFactor * 105).toInt(),
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

        // Screen content seamlessly rendered above the moving background
        content()
    }
}

private fun DrawScope.drawFlowingTrendWave(
    width: Float,
    height: Float,
    progress: Float,
    offsetY: Float,
    color: Color,
    strokeWidthPx: Float
) {
    val path = Path()
    val waveAmp = 34f
    val waveLength = width * 0.80f

    path.moveTo(0f, offsetY)

    var x = 0f
    val step = 12f
    while (x <= width) {
        val y = offsetY + sin((x / waveLength + progress) * 6.28318f) * waveAmp
        path.lineTo(x, y)
        x += step
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidthPx)
    )
}

private fun DrawScope.drawDynamicTradingGrid(
    width: Float,
    height: Float,
    scrollOffset: Float,
    isDark: Boolean
) {
    val spacing = 72f
    val gridAlpha = if (isDark) 0.06f else 0.08f
    val gridColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF00B0FF)

    val scrollY = (-scrollOffset * 0.35f) % spacing
    var y = scrollY
    while (y < height) {
        if (y >= 0) {
            drawLine(
                color = gridColor.copy(alpha = gridAlpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        y += spacing
    }

    // Vertical subtle grid pillars
    var x = spacing
    while (x < width) {
        drawLine(
            color = gridColor.copy(alpha = gridAlpha * 0.6f),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
        x += spacing * 1.5f
    }
}
