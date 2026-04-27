@file:OptIn(ExperimentalTvMaterial3Api::class)

package io.github.posaydone.filmix.tv.ui.screen.showDetailsScreen

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.core.model.KinopoiskCountry
import io.github.posaydone.filmix.core.model.KinopoiskGenre
import io.github.posaydone.filmix.core.model.Rating
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.Votes
import io.github.posaydone.filmix.core.model.latestProgressItem
import io.github.posaydone.filmix.core.model.latestSeriesProgress
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.ImmersiveBackground
import io.github.posaydone.filmix.tv.ui.common.ImmersiveDetails
import io.github.posaydone.filmix.tv.ui.common.LargeButton
import io.github.posaydone.filmix.tv.ui.common.LargeButtonStyle
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import kotlinx.coroutines.launch

private const val TAG = "ShowDetailsScreen"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowDetailsScreen(
    modifier: Modifier = Modifier,
    showId: Int,
    navigateToMoviePlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
    navigateToEpisodes: (showId: Int) -> Unit = {},
    viewModel: ShowDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState is ShowDetailsScreenUiState.Done) {
            viewModel.reload()
        }
    }

    when (val s = uiState) {
        is ShowDetailsScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is ShowDetailsScreenUiState.Error -> {
            Log.d(TAG, "ShowDetailsScreen: ${s.message}")
            Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry)
        }

        is ShowDetailsScreenUiState.Done -> {
            val playProgress = if (s.showDetails.isSeries) {
                s.showProgress.latestSeriesProgress()
            } else {
                s.showProgress.latestProgressItem()
            }
            val playButtonText = when {
                s.showDetails.isSeries && playProgress != null -> stringResource(
                    R.string.continueWatchingSeries,
                    playProgress.season,
                    playProgress.episode,
                )

                !s.showDetails.isSeries && playProgress != null -> stringResource(R.string.continueWatchingMovie)
                else -> stringResource(R.string.playString)
            }
            Details(
                showDetails = s.showDetails,
                toggleFavorites = s.toggleFavorites,
                goToMoviePlayer = {
                    navigateToMoviePlayer(
                        showId,
                        playProgress?.season ?: -1,
                        playProgress?.episode ?: -1,
                    )
                },
                playButtonText = playButtonText,
                goToEpisodes = if (s.showDetails.isSeries) {
                    { navigateToEpisodes(showId) }
                } else null,
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Details(
    showDetails: Show,
    toggleFavorites: () -> Unit,
    goToMoviePlayer: () -> Unit,
    playButtonText: String,
    goToEpisodes: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val hasExtraInfo = !showDetails.cast.isNullOrBlank()
        || !showDetails.director.isNullOrBlank()
        || !showDetails.voice.isNullOrBlank()
        || (showDetails.langs ?: 0) > 0
        || (showDetails.subtitlesCount ?: 0) > 0
        || formatQualityBadge(showDetails.quality) != null
        || showDetails.hasAc3 == true

    Box(
        modifier = Modifier
            .padding(start = 80.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        if (showDetails.backdropUrl != null) {
            ImmersiveBackground(imageUrl = showDetails.backdropUrl)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            // Main content — fills the screen height
            Column(
                modifier = Modifier
                    .height(screenHeight)
                    .fillMaxWidth()
                    .padding(
                        start = childPadding.start,
                        top = childPadding.top + 24.dp,
                        end = childPadding.end + 48.dp,
                        bottom = childPadding.bottom + 24.dp,
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                ImmersiveDetails(
                    logoUrl = null,
                    title = showDetails.title,
                    originalTitle = showDetails.originalTitle,
                    description = showDetails.description,
                    rating = Rating(
                        kp = showDetails.ratingKp,
                        imdb = showDetails.ratingImdb,
                        filmCritics = .0,
                        russianFilmCritics = .0,
                        await = .0
                    ),
                    votes = Votes(
                        kp = showDetails.votesKp,
                        imdb = showDetails.votesImdb,
                        filmCritics = 0,
                        russianFilmCritics = 0,
                        await = 0
                    ),
                    genres = showDetails.genres.map { KinopoiskGenre(it.name) },
                    countries = showDetails.countries.map { KinopoiskCountry(it.name) },
                    year = showDetails.year,
                    seriesLength = if (showDetails.isSeries) showDetails.maxEpisode?.episode else null,
                    movieLength = if (!showDetails.isSeries) showDetails.duration else null,
                    ageRating = showDetails.ageRating.takeIf { it > 0 }?.toString() ?: "",
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShowDetailsButtons(
                        modifier = Modifier.onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch { scrollState.animateScrollTo(0) }
                            }
                        },
                        goToMoviePlayer = goToMoviePlayer,
                        playButtonText = playButtonText,
                        goToEpisodes = goToEpisodes,
                        toggleFavorites = toggleFavorites,
                        isFavorite = showDetails.isFavorite == true,
                    )

                    if (hasExtraInfo) {
                        ScrollHintChevron()
                    }
                }
            }

            // Extra info section — revealed by scrolling down
            if (hasExtraInfo) {
                ShowExtraDetails(
                    show = showDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = childPadding.start,
                            end = childPadding.end + 48.dp,
                            top = 32.dp,
                            bottom = 48.dp,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ScrollHintChevron() {
    val infiniteTransition = rememberInfiniteTransition(label = "chevron")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "chevronOffset",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .offset(y = offsetY.dp)
                .alpha(0.6f),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShowExtraDetails(
    show: Show,
    modifier: Modifier = Modifier,
) {
    val qualityBadge = formatQualityBadge(show.quality)
    val hasAc3 = show.hasAc3 == true
    val hasBadges = qualityBadge != null || hasAc3
    val hasAudio = (show.langs ?: 0) > 0
    val hasSubtitles = (show.subtitlesCount ?: 0) > 0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (hasBadges || hasAudio || hasSubtitles) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (qualityBadge != null) TvInfoBadge(text = qualityBadge)
                if (hasAc3) TvInfoBadge(text = "AC-3")
                if (hasAudio) TvInfoBadge(text = stringResource(R.string.audio_count, show.langs!!))
                if (hasSubtitles) TvInfoBadge(text = stringResource(R.string.subtitles_count, show.subtitlesCount!!))
            }
        }

        if (!show.voice.isNullOrBlank()) {
            TvInfoRow(label = stringResource(R.string.voice_label), value = show.voice!!)
        }
        if (!show.director.isNullOrBlank()) {
            TvInfoRow(label = stringResource(R.string.director_label), value = show.director!!)
        }
        if (!show.cast.isNullOrBlank()) {
            TvInfoRow(label = stringResource(R.string.cast_label), value = show.cast!!)
        }
    }
}

@Composable
private fun TvInfoBadge(text: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TvInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatQualityBadge(quality: String?): String? {
    val q = quality?.filter(Char::isDigit)?.toIntOrNull() ?: return null
    return when {
        q >= 2160 -> "4K"
        q >= 1080 -> "1080p"
        q >= 720 -> "720p"
        q >= 480 -> "480p"
        q > 0 -> "${q}p"
        else -> null
    }
}

@Composable
private fun InfoItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun InfoItemPreview() {
    InfoItem(title = "Release date", value = "2021")
}

@Composable
private fun ShowDetailsButtons(
    modifier: Modifier = Modifier,
    goToMoviePlayer: () -> Unit,
    playButtonText: String,
    goToEpisodes: (() -> Unit)? = null,
    toggleFavorites: () -> Unit,
    isFavorite: Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LargeButton(
            onClick = goToMoviePlayer,
            style = LargeButtonStyle.FILLED,
            modifier = Modifier.focusRequester(focusRequester)
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = playButtonText,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (goToEpisodes != null) {
            LargeButton(
                onClick = goToEpisodes,
                style = LargeButtonStyle.OUTLINED,
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.ViewList,
                    contentDescription = null
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.episodesString),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        LargeButton(
            onClick = toggleFavorites,
            style = if (isFavorite) LargeButtonStyle.FILLED else LargeButtonStyle.OUTLINED
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = if (isFavorite) Icons.Rounded.BookmarkRemove else Icons.Rounded.BookmarkAdd,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
