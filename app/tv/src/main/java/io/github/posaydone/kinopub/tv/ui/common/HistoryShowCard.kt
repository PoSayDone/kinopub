package io.github.posaydone.kinopub.tv.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.HistoryShow

@Composable
fun HistoryShowCard(
    show: HistoryShow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleMode: CardTitleMode = CardTitleMode.ON_FOCUS,
    showTitle: Boolean = true,
    showOriginalTitle: Boolean = true,
    showYear: Boolean = true,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    val seasonNumber = show.seasonNumber
    val episodeNumber = show.episodeNumber

    val watchedSeconds = (show.watchedSeconds ?: 0).toFloat().coerceAtLeast(0f)
    val hasProgress = watchedSeconds > 60f
    val durationSeconds = (show.durationSeconds?.takeIf { it > 0 } ?: 3600).toFloat()
    val progress = (watchedSeconds / durationSeconds).coerceIn(0f, 1f)
    val imageUrl = show.thumbnail?.takeIf { it.isNotBlank() } ?: show.poster

    var isFocused by remember { mutableStateOf(false) }

    BaseCard(
        onClick = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        title = {
            HistoryShowCardInfo(
                show = show,
                isFocused = isFocused,
                titleMode = titleMode,
                showTitle = showTitle,
                showOriginalTitle = showOriginalTitle,
                showYear = showYear,
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_movie),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp),
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .crossfade(true)
                    .data(imageUrl)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            if (show.isSeries && seasonNumber != null && episodeNumber != null) {
                ShowCardBadge(
                    text = "С${seasonNumber}Э${episodeNumber}",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                )
            }

            badge?.invoke(this)

            if (hasProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryShowCardInfo(
    show: HistoryShow,
    isFocused: Boolean,
    titleMode: CardTitleMode,
    showTitle: Boolean,
    showOriginalTitle: Boolean,
    showYear: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!showTitle && !showOriginalTitle && !showYear) return

    val primaryTitle = show.title.trim()
    val originalTitle = show.originalTitle
        .trim()
        .takeIf { it.isNotEmpty() && !it.equals(primaryTitle, ignoreCase = true) }

    val alpha by animateFloatAsState(
        targetValue = if (titleMode == CardTitleMode.ALWAYS || isFocused) 1f else 0f,
        label = "",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showTitle) {
            Text(
                text = primaryTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showOriginalTitle && originalTitle != null) {
            Text(
                text = originalTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showYear && show.year > 0) {
            Text(
                text = show.year.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                maxLines = 1,
            )
        }
    }
}
