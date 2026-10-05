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
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AppTheme.colors

    // 1. Infinite transition for ambient orbs
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

    // Cached particle points
    val particles = remember {
        listOf(
            FloatingParticle(0.15f, 0.20f, 1.2f, "$", 18f, 0.45f),
            FloatingParticle(0.85f, 0.15f, 0.9f, "€", 16f, 0.40f),
            FloatingParticle(0.70f, 0.45f, 1.4f, "₿", 22f, 0.50f),
            FloatingParticle(0.25f, 0.65f, 1.0f, "¥", 17f, 0.35f),
            FloatingParticle(0.88f, 0.75f, 1.3f, "£", 18f, 0.40f),
            FloatingParticle(0.40f, 0.85f, 0.8f, "💎", 15f, 0.45f),
            FloatingParticle(0.10f, 0.90f, 1.1f, "🪙", 16f, 0.40f),
            FloatingParticle(0.55f, 0.30f, 0.7f, "₮", 19f, 0.42f)
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

                // --- 1. Floating Glowing Radial Orbs (Aurora Effect) ---
                val orb1X = width * (0.3f + 0.18f * cos(pulsePhase))
                val orb1Y = height * (0.25f + 0.12f * sin(pulsePhase * 0.8f))
                val orb1Radius = width * 0.65f

                val orb2X = width * (0.75f + 0.15f * sin(pulsePhase * 0.9f))
                val orb2Y = height * (0.7f + 0.14f * cos(pulsePhase * 0.7f))
                val orb2Radius = width * 0.70f

                val orb3X = width * (0.2f + 0.12f * sin(pulsePhase * 1.1f))
                val orb3Y = height * (0.8f + 0.1f * cos(pulsePhase * 0.9f))
                val orb3Radius = width * 0.55f

                if (isDarkTheme) {
                    // Dark theme cosmic neon glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.08f),
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
                                Color(0xFFB388FF).copy(alpha = 0.07f),
                                Color(0xFFFFB800).copy(alpha = 0.03f),
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
                                Color(0xFF00E676).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = orb3Radius
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = orb3Radius
                    )
                } else {
                    // White theme elegant pastel aurora
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00B0FF).copy(alpha = 0.09f),
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
                                Color(0xFF8B5CF6).copy(alpha = 0.04f),
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
                                Color(0xFF6366F1).copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(orb3X, orb3Y),
                            radius = orb3Radius
                        ),
                        center = Offset(orb3X, orb3Y),
                        radius = orb3Radius
                    )
                }

                // --- 2. Financial Chart Wave Ambient Curves ---
                drawAnimatedMarketWave(
                    width = width,
                    height = height,
                    progress = waveScroll,
                    isDark = isDarkTheme
                )

                // --- 3. Floating Subtle Currency Particles ---
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                particles.forEach { p ->
                    val posX = p.initialX * width + floatingDrift * (p.speed * 0.3f)
                    val rawY = (p.initialY * height - (pulsePhase * 25f * p.speed))
                    val posY = (rawY % height + height) % height

                    paint.textSize = p.size * density
                    paint.color = if (isDarkTheme) {
                        android.graphics.Color.argb(
                            (p.alphaFactor * 45).toInt(),
                            255, 255, 255
                        )
                    } else {
                        android.graphics.Color.argb(
                            (p.alphaFactor * 55).toInt(),
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

        // Screen content rendered cleanly on top
        content()
    }
}

private fun DrawScope.drawAnimatedMarketWave(
    width: Float,
    height: Float,
    progress: Float,
    isDark: Boolean
) {
    val path = Path()
    val baseWaveY = height * 0.42f
    val waveAmp = 22f
    val waveLength = width * 0.85f

    path.moveTo(0f, baseWaveY)

    var x = 0f
    val step = 15f
    while (x <= width) {
        val y = baseWaveY + sin((x / waveLength + progress) * 6.28318f) * waveAmp
        path.lineTo(x, y)
        x += step
    }

    val waveColor = if (isDark) {
        Color(0xFF00E5FF).copy(alpha = 0.05f)
    } else {
        Color(0xFF00B0FF).copy(alpha = 0.07f)
    }

    drawPath(
        path = path,
        color = waveColor,
        style = Stroke(width = 2.dp.toPx())
    )
}
