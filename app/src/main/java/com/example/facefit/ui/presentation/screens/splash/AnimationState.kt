package com.example.facefit.ui.presentation.screens.splash
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.example.facefit.ui.theme.Blue1
import kotlin.math.hypot

object AnimationState {
    var shrinkPlayed = false
}
@Composable
fun ShrinkOverlay(onAnimationEnd: () -> Unit = {}) {
    if (AnimationState.shrinkPlayed) return  // ✅ لا تعيدي الانكماش لو حصل قبل كده

    val circleRadius = remember { Animatable(initialValue = 2000f) }
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
        circleRadius.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = EaseOutBack)
        )
        AnimationState.shrinkPlayed = true // ✅ نحفظ إن الانكماش حصل مرة
        onAnimationEnd()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Blue1,
            radius = circleRadius.value,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}

