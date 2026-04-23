package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier,
    imageUrl: String?,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .crossfade(500)
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
                .gradientOverlay(MaterialTheme.colorScheme.surface),
            placeholder = ColorPainter(MaterialTheme.colorScheme.surface),
            error = ColorPainter(MaterialTheme.colorScheme.surface),
            fallback = ColorPainter(MaterialTheme.colorScheme.surface),
        )

    }
}
