package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier,
    imageUrl: String?,
) {
    Crossfade(
        targetState = imageUrl,
        label = "BackgroundCrossfade",
        animationSpec = tween(durationMillis = 500)
    ) { image ->
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
    }
}
