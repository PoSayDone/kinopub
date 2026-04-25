package io.github.posaydone.filmix.tv.ui.common

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.withContext

@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    dynamicThemeEnabled: Boolean = true,
    onThemeSeedColorResolved: (Color) -> Unit = {},
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
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
                .gradientOverlay(surfaceColor),
            placeholder = ColorPainter(surfaceColor),
            error = ColorPainter(surfaceColor),
            fallback = ColorPainter(surfaceColor),
            onSuccess = { state ->
                if (!dynamicThemeEnabled) {
                    return@AsyncImage
                }

                val drawable = state.result.drawable
                scope.launch {
                    val themeSeedColor = withContext(Dispatchers.Default) {
                        drawable.extractThemeSeedColor()
                    }
                    themeSeedColor?.let(onThemeSeedColorResolved)
                }
            }
        )
    }
}

private fun Drawable.extractThemeSeedColor(): Color? {
    return try {
        val bitmap = toBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val palette = Palette.Builder(bitmap)
            .clearFilters()
            .maximumColorCount(8)
            .generate()
        val swatch = palette.vibrantSwatch ?: palette.dominantSwatch ?: palette.mutedSwatch

        bitmap.recycle()
        swatch?.rgb?.let(::Color)
    } catch (_: Exception) {
        null
    }
}
