package com.example.facefit.ui.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.Blue1
import kotlinx.coroutines.delay
import kotlin.math.hypot


@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val circleRadius = remember { Animatable(0f) }
    var showText by remember { mutableStateOf(true) }

    val screenSize = LocalConfiguration.current
    val density = LocalDensity.current
    val maxRadius = remember(screenSize) {
        with(density) {
            hypot(
                screenSize.screenWidthDp.toFloat(),
                screenSize.screenHeightDp.toFloat()
            ) * density.density
        }
    }

    LaunchedEffect(Unit) {
        // 1. Show text for 1000ms
        delay(1000)
        showText = false

        // 2. Expand circle
        circleRadius.animateTo(
            targetValue = maxRadius,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // 3. Navigate
        onAnimationFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Circle animation
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Blue1,
                radius = circleRadius.value,
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }

        // Show FaceFit text
        if (showText) {
            Text(
                text = "FaceFit",
                fontSize = 60.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.05.em,
                color = Blue1
            )
        }
    }
}




