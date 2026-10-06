package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import kotlin.math.abs

/**
 * Calculates real-time scroll velocity and direction for motion blur calculations.
 */
@Composable
fun rememberScrollVelocity(listState: LazyListState): State<Float> {
    var prevIndex by remember { mutableIntStateOf(listState.firstVisibleItemIndex) }
    var prevOffset by remember { mutableIntStateOf(listState.firstVisibleItemScrollOffset) }
    var rawVelocity by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val indexDiff = (listState.firstVisibleItemIndex - prevIndex) * 180
        val offsetDiff = listState.firstVisibleItemScrollOffset - prevOffset
        val delta = abs(indexDiff + offsetDiff).toFloat()

        prevIndex = listState.firstVisibleItemIndex
        prevOffset = listState.firstVisibleItemScrollOffset

        // Normalize delta to a velocity range between 0f and 16f
        rawVelocity = (delta / 14f).coerceIn(0f, 16f)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            rawVelocity = 0f
        }
    }

    return animateFloatAsState(
        targetValue = if (listState.isScrollInProgress) rawVelocity else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
        label = "motion_velocity"
    )
}

/**
 * Motion Blur Modifier Extension
 * Applies dynamic Gaussian motion blur to composables proportional to current scroll velocity.
 */
fun Modifier.scrollMotionBlur(velocity: Float): Modifier {
    val blurRadius = (velocity * 0.75f).coerceIn(0f, 12f)
    return if (blurRadius > 0.4f) {
        this.blur(radius = blurRadius.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        this
    }
}

/**
 * Motion Blur Effect Component
 * Designed to replace static buttons with modern, sleek Motion Blur aesthetics:
 * 1. Bottom Frosted Glass Vignette with Dynamic Motion Blur that expands with scroll speed.
 * 2. Top Edge Motion Blur Dispersion for incoming items during fast scrolls.
 * 3. Kinetic Velocity Streaks: Dynamic glowing speed lines that appear during fast flings.
 * 4. Pulsing Horizon Line with velocity-reactive luminescence.
 * 
 * Note: The previous floating scroll-down button has been completely removed as requested!
 */
@Composable
fun BoxScope.ScrollMotionBlurEffect(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 86.dp
) {
    val colors = AppTheme.colors
    val velocity by rememberScrollVelocity(listState)

    // Determine scroll progress
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }

    // Dynamic blur height expanding with velocity
    val bottomBlurHeight by animateDpAsState(
        targetValue = if (isScrolled) {
            (70f + (velocity * 4.5f)).coerceIn(70f, 130f).dp
        } else {
            36.dp
        },
        animationSpec = spring(stiffness = 300f),
        label = "motion_blur_height"
    )

    // Dynamic blur intensity based on velocity
    val dynamicBlurRadius by animateDpAsState(
        targetValue = (12f + (velocity * 1.2f)).coerceIn(12f, 26f).dp,
        animationSpec = spring(stiffness = 300f),
        label = "dynamic_blur_radius"
    )

    // Shimmering streak animation
    val infiniteTransition = rememberInfiniteTransition(label = "motion_streak")
    val streakPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "streak_phase"
    )

    // 1. Top Edge Motion Blur Dispersion (appears when scrolled down and moving)
    AnimatedVisibility(
        visible = isScrolled,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(250)),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((24f + (velocity * 2.5f)).coerceIn(24f, 50f).dp)
                .blur(radius = dynamicBlurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.bg.copy(alpha = (0.75f + (velocity * 0.02f)).coerceIn(0.75f, 0.95f)),
                            colors.surface.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }

    // 2. Bottom Dynamic Motion Blur Frosted Glass Vignette
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(bottomBlurHeight)
            .padding(bottom = (bottomPadding - 24.dp).coerceAtLeast(0.dp))
            .blur(radius = dynamicBlurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        colors.surface.copy(alpha = (0.40f + (velocity * 0.03f)).coerceIn(0.40f, 0.70f)),
                        colors.bg.copy(alpha = (0.85f + (velocity * 0.015f)).coerceIn(0.85f, 0.98f))
                    )
                )
            )
            .testTag("motion_blur_bottom_vignette")
    )

    // 3. Kinetic Velocity Streaks (Speed Lines) when scrolling fast
    if (velocity > 1.2f) {
        val streakAlpha = (velocity / 12f).coerceIn(0.2f, 0.75f)
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(42.dp)
                .padding(bottom = bottomPadding)
        ) {
            val width = size.width
            val height = size.height

            // Cyan velocity streak
            val x1 = (width * ((streakPhase + 0.2f) % 1f))
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        FintechCyan.copy(alpha = streakAlpha),
                        Color.Transparent
                    )
                ),
                start = Offset(x1 - 120f, height * 0.4f),
                end = Offset(x1 + 120f, height * 0.4f),
                strokeWidth = 1.5.dp.toPx()
            )

            // Gold velocity streak
            val x2 = (width * ((streakPhase + 0.65f) % 1f))
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        FintechGold.copy(alpha = streakAlpha * 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(x2 - 90f, height * 0.7f),
                end = Offset(x2 + 90f, height * 0.7f),
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }

    // 4. Subtle Glowing Horizon Line across bottom
    AnimatedVisibility(
        visible = isScrolled,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(250)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = bottomPadding + 2.dp)
    ) {
        val horizonAlpha = (0.25f + (velocity * 0.04f)).coerceIn(0.25f, 0.65f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            FintechCyan.copy(alpha = horizonAlpha * 0.7f),
                            FintechGold.copy(alpha = horizonAlpha),
                            FintechCyan.copy(alpha = horizonAlpha * 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Backward-compatible delegating function for existing calls.
 * Implements the newly designed Motion Blur effect while completely omitting the scroll button.
 */
@Composable
fun BoxScope.ScrollDownBlurEffect(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 86.dp,
    customScrollDownLabel: String = "",
    customScrollUpLabel: String = ""
) {
    ScrollMotionBlurEffect(
        listState = listState,
        modifier = modifier,
        bottomPadding = bottomPadding
    )
}
