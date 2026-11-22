package com.example.mymoneymanager.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneymanager.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val ForestBrand = Color(0xFF2D5A27)

@Composable
fun SplashScreen(
    onPreload: suspend () -> Unit,
    onSplashFinished: () -> Unit,
) {
    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }
    val pulseAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            )
        }
        launch {
            delay(500.milliseconds)
            pulseAnim.animateTo(
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            onPreload()
        } catch (_: Exception) {
            // Safe fallback if preload errors
        }
        val elapsed = System.currentTimeMillis() - startTime
        val remainingDelay = (1500L - elapsed).coerceAtLeast(200L)
        delay(remainingDelay.milliseconds)

        // Fade out slightly before moving
        alphaAnim.animateTo(0f, animationSpec = tween(300))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scaleAnim.value * pulseAnim.value)
                .alpha(alphaAnim.value),
        ) {
            // Visual Centerpiece: The MyMoneyManager custom logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ForestBrand.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mymoneymanager_logo),
                    contentDescription = "MyMoneyManager Logo",
                    modifier = Modifier.size(92.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App title in clean typography
            Text(
                text = "MyMoneyManager",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtext/Tagline
            Text(
                text = "Kelola Keuangan Lebih Bijak",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ForestBrand,
                letterSpacing = 0.2.sp,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Subtle indefinite circular progress indicator
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = ForestBrand,
                strokeWidth = 2.5.dp,
                trackColor = ForestBrand.copy(alpha = 0.15f),
            )
        }
    }
}
