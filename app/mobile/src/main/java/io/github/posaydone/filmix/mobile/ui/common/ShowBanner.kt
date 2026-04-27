package io.github.posaydone.filmix.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.utils.formatDuration
import io.github.posaydone.filmix.core.common.utils.formatVoteCount

@Composable
fun ShowBannerContent(
    modifier: Modifier = Modifier,
    title: String,
    logoUrl: String?,
    ratingKp: Double?,
    votesKp: Int?,
    originalTitle: String?,
    year: Int?,
    genres: List<String>,
    countries: List<String>,
    durationSeconds: Int? = null,
    ageRating: Int?,
    description: String? = null,
    maxDescriptionLines: Int = Int.MAX_VALUE,
    onPlayClick: () -> Unit,
    playButtonText: String,
    isFavorite: Boolean? = null,
    onToggleFavoritesClick: (() -> Unit)? = null,
    onEpisodesClick: (() -> Unit)? = null,
    showMetadata: Boolean = true,
    showDescription: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleSection(
            title = title,
            originalTitle = originalTitle,
            logoUrl = logoUrl,
        )
        if (showMetadata) {
            MetadataColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                ratingKp = ratingKp,
                votesKp = votesKp,
                year = year,
                genres = genres,
                countries = countries,
                durationSeconds = durationSeconds,
                ageRating = ageRating,
            )
        }
        if (!description.isNullOrBlank() && showDescription) {
            Text(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = maxDescriptionLines,
            )
        }
        ActionButtons(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 12.dp),
            navigateToMoviePlayer = onPlayClick,
            toggleFavorites = onToggleFavoritesClick,
            isFavorite = isFavorite,
            playButtonText = playButtonText,
            navigateToEpisodes = onEpisodesClick,
        )
    }
}

@Composable
fun TitleSection(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    title: String,
    originalTitle: String? = null,
    logoUrl: String?,
    forceTextTitle: Boolean = false,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val secondaryTitle = originalTitle
        ?.trim()
        ?.takeIf { it.isNotEmpty() && !it.equals(title, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.background
                    )
                )
            )
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.1f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.background
                    )
                )
            )
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.2f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (logoUrl != null && !forceTextTitle) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(logoUrl).build(),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.sizeIn(
                        maxWidth = screenWidth * 0.6f, maxHeight = screenHeight * 0.32f
                    )
                )
                if (secondaryTitle != null) {
                    Text(
                        text = secondaryTitle,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                )
                if (secondaryTitle != null) {
                    Text(
                        text = secondaryTitle,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    navigateToMoviePlayer: () -> Unit,
    toggleFavorites: (() -> Unit)? = null,
    isFavorite: Boolean? = null,
    playButtonText: String = stringResource(R.string.playString),
    navigateToEpisodes: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterHorizontally
        ),
    ) {
        LargeButton(onClick = navigateToMoviePlayer) {
            Icon(
                contentDescription = stringResource(R.string.play),
                modifier = Modifier.size(28.dp),
                imageVector = Icons.Rounded.PlayArrow,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = playButtonText)
        }
        if (navigateToEpisodes != null) {
            LargeButton(
                style = LargeButtonStyle.OUTLINED,
                onClick = navigateToEpisodes,
            ) {
                Icon(
                    contentDescription = stringResource(R.string.episodesString),
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.AutoMirrored.Rounded.ViewList,
                )
            }
        }
        if (toggleFavorites != null && isFavorite != null) {
            LargeButton(
                style = LargeButtonStyle.OUTLINED,
                onClick = toggleFavorites,
                colors = if (isFavorite) ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) else ButtonDefaults.outlinedButtonColors(),
            ) {
                Icon(
                    contentDescription = stringResource(R.string.favorite),
                    modifier = Modifier.size(28.dp),
                    imageVector = if (isFavorite) Icons.Rounded.BookmarkRemove else Icons.Rounded.BookmarkAdd,
                )
            }
        }
    }
}

@Composable
fun ShowPoster(
    modifier: Modifier = Modifier,
    backdropUrl: String? = null,
    posterUrl: String,
    height: Dp,
) {
    if (backdropUrl != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(backdropUrl).crossfade(true)
                .build(),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(height),
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(posterUrl).crossfade(true)
                    .build(),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(2 / 3f),
            )
        }
    }
}

@Composable
private fun MetadataColumn(
    ratingKp: Double?,
    votesKp: Int?,
    year: Int?,
    genres: List<String>,
    countries: List<String>,
    durationSeconds: Int?,
    ageRating: Int?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "%.1f".format(ratingKp ?: 0.0) + " (${formatVoteCount(votesKp ?: 0)})",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = year?.toString() ?: "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = genres.take(2).joinToString(", "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = countries.take(2).joinToString(", "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (durationSeconds != null) {
                Text(
                    text = formatDuration(context, durationSeconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ageRating?.let {
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$it+",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
