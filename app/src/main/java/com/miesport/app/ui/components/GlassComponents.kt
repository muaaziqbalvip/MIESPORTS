package com.miesport.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miesport.app.ui.theme.NeonGreen
import com.miesport.app.ui.theme.NeonGreenGlow
import com.miesport.app.ui.theme.SurfaceGlass
import com.miesport.app.ui.theme.SurfaceGlassBorder

/**
 * Frosted glass card used across the app for tournament cards, stat panels, etc.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = SurfaceGlassBorder,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceGlass, SurfaceGlass.copy(alpha = 0.04f))
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(1.dp)
    ) {
        content()
    }
}

/**
 * A soft neon glow blob placed behind content to simulate the app's ambient lighting.
 */
@Composable
fun NeonGlow(
    modifier: Modifier = Modifier,
    color: Color = NeonGreenGlow,
    size: Dp = 220.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(80.dp)
            .background(color, RoundedCornerShape(50))
    )
}

/** Extension so `size` reads naturally above (avoids importing layout.size ambiguity issues). */
private fun Modifier.size(size: Dp): Modifier = this.then(androidx.compose.foundation.layout.size(size))

/**
 * Pulsing "LIVE" glow dot used on live tournament/match indicators.
 */
@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE0304A)
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Box(
        modifier = modifier
            .size(10.dp)
            .background(color.copy(alpha = 0.5f), RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .padding((10.dp * (1 - scale / 1.4f)).coerceAtLeast(0.dp))
                .background(color, RoundedCornerShape(50))
        )
    }
}
