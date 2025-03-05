package com.phule.assignmenttest.presentation.utils

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color

private val gradient = listOf(
    Color.Gray.copy(alpha = 0.6f), //darker grey (90% opacity)
    Color.Gray.copy(alpha = 0.3f), //lighter grey (30% opacity)
    Color.Gray.copy(alpha = 0.6f)
)

@Composable
fun LoadingImageEffect(index: Int, aspectRatio: Float) {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutLinearInEasing
            )
        ), label = ""
    )
    val brush = linearGradient(
        colors = gradient,
        start = Offset(200f, 200f),
        end = Offset(
            x = translateAnimation.value,
            y = translateAnimation.value
        )
    )
    ShimmerContainer(index, aspectRatio, brush)
}

@Composable
private fun ShimmerContainer(index: Int, aspectRatio: Float, brush: Brush) {
    val ratio = if (index == 0) 16 / 9f else aspectRatio
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush)
            .aspectRatio(ratio),
    )
}

@Composable
fun LoadingShimmerEffect(index: Int, aspectRatio: Float) {
    Card {
        LoadingImageEffect(index, aspectRatio)
    }
}