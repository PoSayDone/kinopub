package io.github.posaydone.filmix.tv.ui.common

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier,
    imageUrl: String?,
) {
    val defaultSurface = MaterialTheme.colorScheme.surface
    var gradientColor by remember { mutableStateOf(defaultSurface) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .crossfade(400)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .size(1280, 720)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(16/9f)
                .align(Alignment.TopEnd)
                .gradientOverlay(gradientColor),
            placeholder = ColorPainter(defaultSurface),
            error = ColorPainter(defaultSurface),
            fallback = ColorPainter(defaultSurface),
            onSuccess = { state ->
                val drawable = state.result.drawable
                scope.launch(Dispatchers.Default) {
                    try {
                        // Create a software copy of the bitmap to bypass Palette's hardware bitmap limitations
                        // without needing to use `allowHardware(false)` on the Coil ImageRequest.
                        val bitmap = drawable.toBitmap().copy(Bitmap.Config.ARGB_8888, true)

                        if (bitmap != null) {
                            val palette = Palette.Builder(bitmap)
                                .clearFilters()
                                .maximumColorCount(8)
                                .generate()

                            // Emulate Material 3 content-based dynamic color by adjusting HSL
                            // 0.06f sets it perfectly into a dark background format ideal for TV gradients
                            palette.dominantSwatch?.hsl?.let { hsl ->
                                gradientColor = Color.hsl(
                                    hue = hsl[0],
                                    saturation = 0.9f,
                                    lightness = 0.06f,
                                    alpha = 1f
                                )
                            }

                            // Clean up our copied software bitmap to prevent memory spikes
                            bitmap.recycle()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace() // Fallback remains defaultSurface
                    }
                }
            }
        )
    }
}