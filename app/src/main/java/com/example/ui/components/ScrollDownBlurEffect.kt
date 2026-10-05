package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import kotlinx.coroutines.launch

/**
 * Animated Scroll Down & Blur Effect Component
 * Provides:
 * 1. Bottom frosted glass blur vignette that dynamically responds to scrolling down.
 * 2. Animated floating frosted-glass action pill (FAB) with bounce animation and blur effect,
 *    allowing instant smooth scrolling down or back to top.
 */
@Composable
fun BoxScope.ScrollDownBlurEffect(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 86.dp,
    customScrollDownLabel: String = "اسکرول به پایین",
    customScrollUpLabel: String = "بازگشت به بالا"
) {
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()

    // Determine scroll states
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30
        }
    }

    val canScrollForward by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }

    val isNearBottom by remember {
        derivedStateOf {
            !listState.canScrollForward && isScrolled
        }
    }

    // Animated blur height and alpha when scrolling
    val blurHeight by animateDpAsState(
        targetValue = if (isScrolled) 90.dp else 40.dp,
        animationSpec = spring(stiffness = 250f),
        label = "blur_height"
    )

    val blurAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0.4f,
        animationSpec = tween(350),
        label = "blur_alpha"
    )

    // Bouncing animation for scroll indicator pill
    val infiniteTransition = rememberInfiniteTransition(label = "scroll_bounce")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_y"
    )

    // Shimmering glow on the pill border
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // 1. Dynamic Bottom Blur Backdrop
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(blurHeight)
            .padding(bottom = bottomPadding - 24.dp)
            .blur(radius = 16.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        colors.surface.copy(alpha = 0.45f * blurAlpha),
                        colors.bg.copy(alpha = 0.90f * blurAlpha)
                    )
                )
            )
    )

    // 2. Subtle Glowing Horizon Line across bottom
    AnimatedVisibility(
        visible = isScrolled,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = bottomPadding + 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            FintechCyan.copy(alpha = 0.25f),
                            FintechGold.copy(alpha = 0.35f),
                            FintechCyan.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
    }

    // 3. Floating Frosted Blur Glassmorphic Scroll Pill (FAB)
    AnimatedVisibility(
        visible = isScrolled || canScrollForward,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = 300f)
        ) + fadeIn(tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = 300f)
        ) + fadeOut(tween(200)),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = bottomPadding + 10.dp)
    ) {
        val pillShape = RoundedCornerShape(24.dp)

        Box(
            modifier = Modifier
                .offset(y = bounceY.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = pillShape,
                    spotColor = FintechCyan.copy(alpha = 0.35f),
                    ambientColor = Color.Black.copy(alpha = 0.25f)
                )
                .clip(pillShape)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            FintechCyan.copy(alpha = glowAlpha),
                            FintechGold.copy(alpha = glowAlpha * 0.8f)
                        )
                    ),
                    shape = pillShape
                )
                .background(colors.surface.copy(alpha = 0.88f))
                .clickable {
                    coroutineScope.launch {
                        if (isNearBottom) {
                            // Scroll back to top
                            listState.animateScrollToItem(0)
                        } else {
                            // Smoothly scroll down by 5 items or to end
                            val target = (listState.firstVisibleItemIndex + 5)
                            listState.animateScrollToItem(target)
                        }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 9.dp)
                .testTag("scroll_down_blur_fab"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Animated Icon with pulse
                val iconVector = if (isNearBottom) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNearBottom) FintechGold.copy(alpha = 0.2f) else FintechCyan.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = if (isNearBottom) customScrollUpLabel else customScrollDownLabel,
                        tint = if (isNearBottom) FintechGold else FintechCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isNearBottom) customScrollUpLabel else customScrollDownLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Small pulsing dot indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNearBottom) FintechGold else FintechGreen
                        )
                )
            }
        }
    }
}
