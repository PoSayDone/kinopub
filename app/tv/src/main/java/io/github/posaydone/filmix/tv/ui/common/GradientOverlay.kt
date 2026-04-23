package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform

fun Modifier.gradientOverlay(gradientColor: Color): Modifier = drawWithCache {
    val cx = size.width * 0.95f
    val cy = 0f
    val centerOffset = Offset(cx, cy)

    val scaleY = if (cx > 0f) size.height / cx else 1f

    val radius = if (cx > 0f) cx / 0.707f else size.width

    val radialGradient = Brush.radialGradient(
        0.0f to gradientColor.copy(alpha = 0.1f),
        0.707f to gradientColor,
        1.0f to gradientColor,
        center = centerOffset,
        radius = radius.coerceAtLeast(1f) 
    )

    val rectSize = Size(size.width, if (scaleY > 0f) size.height / scaleY else size.height)

    onDrawWithContent {
        drawContent()

        withTransform({
            scale(scaleX = 1f, scaleY = scaleY, pivot = centerOffset)
        }) {
            drawRect(
                brush = radialGradient,
                topLeft = Offset.Zero,
                size = rectSize
            )
        }
    }
}