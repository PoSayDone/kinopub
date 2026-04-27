package io.github.posaydone.kinopub.tv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme

/**
 * A composable that creates a gradient mask to hide content scrolling underneath it.
 * It fades from fully transparent to the solid surface color.
 *
 * @param modifier The modifier to be applied to the mask.
 * @param height The height of the gradient fade area.
 * @param startY The vertical position where the mask should start.
 */
@Composable
fun BackdropMask(
    modifier: Modifier = Modifier,
    height: Dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(0.90f to MaterialTheme.colorScheme.surface, 1.0f to Color.Transparent)
                )
            )
    )
}