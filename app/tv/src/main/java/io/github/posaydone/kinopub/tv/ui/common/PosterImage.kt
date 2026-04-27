package io.github.posaydone.kinopub.tv.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.posaydone.kinopub.core.model.Show

@Composable
fun PosterImage(
    contentDescritpion: String?,
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .crossfade(true)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(200, 200) // Specify exact size
            .build(),
        contentDescription = contentDescritpion,
        contentScale = ContentScale.Crop
    )
}