package io.github.posaydone.filmix.tv.ui.common

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.request.SuccessResult

/**
 * A reusable composable for displaying a full-screen background image with a crossfade animation.
 */
@Composable
fun ImmersiveBackground(
    modifier: Modifier = Modifier.Companion,
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
                .crossfade(true)
                .size(Size.ORIGINAL)
                .allowHardware(false) // Sometimes hardware bitmaps can cause issues
                .listener(
                    onStart = { _ -> 
                        Log.d("ImmersiveBackground", "Started loading image: $image")
                    },
                    onSuccess = { _, _ ->
                        Log.d("ImmersiveBackground", "Successfully loaded image: $image")
                    },
                    onError = { _, error ->
                        Log.d("ImmersiveBackground", "Failed to load image: $image, error: ${error.throwable.message}")
                    }
                )
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Companion.Crop,
            modifier = modifier
                .aspectRatio(16 / 9f)
                .padding(start = 160.dp)
                .fillMaxSize()
        )
    }
}