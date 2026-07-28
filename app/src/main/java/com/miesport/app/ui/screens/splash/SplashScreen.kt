package com.miesport.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miesport.app.R
import com.miesport.app.ui.theme.AccentRedGlow
import com.miesport.app.ui.theme.BackgroundBlack
import com.miesport.app.ui.theme.NeonGreen
import com.miesport.app.ui.theme.NeonGreenGlow
import com.miesport.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Pro-level animated splash: logo pops in with a spring-like scale + fade,
 * ambient neon glows pulse gently in the background, and a slim progress
 * bar gives a sense of fast, real-time loading before auto-navigating.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash-glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, animationSpec = tween(650, easing = EaseOutBack))
        logoAlpha.animateTo(1f, animationSpec = tween(500))
        taglineAlpha.animateTo(1f, animationSpec = tween(400))
        delay(900) // brief real hold so the brand registers before navigating
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        // Ambient pulsing glows echoing the logo's green/red halves
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-70).dp, y = (-40).dp)
                .size(220.dp)
                .scale(glowPulse)
                .blur(90.dp)
                .clip(CircleShape)
                .background(NeonGreenGlow)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 70.dp, y = 40.dp)
                .size(220.dp)
                .scale(glowPulse)
                .blur(90.dp)
                .clip(CircleShape)
                .background(AccentRedGlow)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "MI ESPORT",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "MI ESPORT",
                style = MaterialTheme.typography.displayLarge,
                color = NeonGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Play. Compete. Win.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(Modifier.height(36.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(120.dp)
                    .alpha(taglineAlpha.value),
                color = NeonGreen,
                trackColor = NeonGreen.copy(alpha = 0.15f)
            )
        }
    }
}
