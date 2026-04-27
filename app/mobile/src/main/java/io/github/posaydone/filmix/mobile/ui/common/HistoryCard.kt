package io.github.posaydone.filmix.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.filmix.core.model.HistoryShow

private val HistoryCardWidth = 220.dp

@Composable
fun HistoryCard(
    show: HistoryShow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showOriginalTitle: Boolean = true,
    showYear: Boolean = true,
) {
    val seasonNumber = show.seasonNumber
    val episodeNumber = show.episodeNumber
    val watchedSeconds = (show.watchedSeconds ?: 0).toFloat().coerceAtLeast(0f)
    val hasProgress = watchedSeconds > 60f
    val durationSeconds = (show.durationSeconds?.takeIf { it > 0 } ?: 3600).toFloat()
    val progress = (watchedSeconds / durationSeconds).coerceIn(0f, 1f)
    val imageUrl = show.thumbnail?.takeIf { it.isNotBlank() } ?: show.poster

    BaseCard(
        onClick = onClick,
        modifier = modifier.width(HistoryCardWidth),
        title = {
            ShowCardInfo(
                title = show.title,
                originalTitle = show.originalTitle,
                year = show.year,
                showOriginalTitle = showOriginalTitle,
                showYear = showYear,
            )
        },
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .crossfade(true)
                    .data(imageUrl)
                    .build(),
                contentDescription = show.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            if (show.isSeries && seasonNumber != null && episodeNumber != null) {
                HistoryCardBadge(
                    text = "С${seasonNumber}Э${episodeNumber}",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                )
            }

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
private fun HistoryCardBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}
