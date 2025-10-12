package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.gradientOverlay(gradientColor: Color): Modifier = drawWithCache {
    val horizontalGradient = Brush.Companion.horizontalGradient(
        colors = listOf(
            gradientColor, Color.Companion.Transparent
        ), startX = size.width.times(0.2f), endX = size.width.times(0.5f)
    )
    val verticalGradient = Brush.Companion.verticalGradient(
        colors = listOf(
            Color.Companion.Transparent, gradientColor
        ), endY = size.width.times(0.5f)
    )
    val linearGradient = Brush.Companion.linearGradient(
        colors = listOf(
            gradientColor, Color.Companion.Transparent
        ), start = Offset(
            size.width.times(0.2f), size.height.times(0.3f)
        ), end = Offset(
            size.width.times(0.9f), 0f
        )
    )

    onDrawWithContent {
        drawContent()
        drawRect(horizontalGradient)
        drawRect(verticalGradient)
        drawRect(linearGradient)
    }
}