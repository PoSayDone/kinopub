@file:OptIn(ExperimentalTvMaterial3Api::class)

package io.github.posaydone.filmix.tv.ui.screen.episodesScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.findEpisodeProgress
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.ImmersiveBackground
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowCard
import io.github.posaydone.filmix.tv.ui.common.gradientOverlay
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding

private val EpisodeCardWidth = 220.dp

@Composable
fun EpisodesScreen(
    showId: Int,
    navigateToPlayer: (showId: Int, season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EpisodesScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is EpisodesScreenUiState.Loading -> Loading(modifier = modifier.fillMaxSize())

        is EpisodesScreenUiState.Error -> Error(
            modifier = modifier.fillMaxSize(),
            onRetry = s.onRetry,
        )

        is EpisodesScreenUiState.Done -> Box(modifier = modifier.fillMaxSize()) {
            ImmersiveBackground(imageUrl = s.fullShow.backdropUrl ?: s.fullShow.posterUrl)
            Box(
                Modifier
                    .fillMaxSize()
                    .gradientOverlay(MaterialTheme.colorScheme.surface),
            )
            EpisodesContent(
                seasons = s.seasons,
                showProgress = s.showProgress,
                onEpisodeClick = { season, episode -> navigateToPlayer(showId, season, episode) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun EpisodesContent(
    seasons: List<Season>,
    showProgress: ShowProgress,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val (outerColumn, firstRow) = remember { FocusRequester.createRefs() }

    if (seasons.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.episodesString),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .focusRequester(outerColumn)
            .focusRestorer(firstRow),
        contentPadding = PaddingValues(
            top = childPadding.top + 24.dp,
            bottom = childPadding.bottom + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.episodesString),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    start = childPadding.start + 48.dp,
                    bottom = 8.dp,
                ),
            )
        }

        itemsIndexed(seasons) { seasonIndex, season ->
            SeasonRow(
                season = season,
                showProgress = showProgress,
                onEpisodeClick = onEpisodeClick,
                startPadding = childPadding.start + 48.dp,
                endPadding = childPadding.end + 48.dp,
                modifier = if (seasonIndex == 0) Modifier.focusRequester(firstRow) else Modifier,
            )
        }
    }

    LaunchedEffect(Unit) {
        firstRow.requestFocus()
    }
}

@Composable
private fun SeasonRow(
    season: Season,
    showProgress: ShowProgress,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    startPadding: androidx.compose.ui.unit.Dp,
    endPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val (lazyRow, firstCard) = remember { FocusRequester.createRefs() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.season, season.season),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = startPadding),
        )

        LazyRow(
            contentPadding = PaddingValues(start = startPadding, end = endPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .focusRequester(lazyRow)
                .focusRestorer(firstCard),
        ) {
            itemsIndexed(
                items = season.episodes,
                key = { _, ep -> "${season.season}_${ep.episode}" },
            ) { index, episode ->
                val progressItem = showProgress.findEpisodeProgress(
                    season = season.season,
                    episode = episode.episode,
                )
                val isWatched = (progressItem?.time ?: 0L) > 0
                EpisodeCard(
                    episode = episode,
                    isWatched = isWatched,
                    onClick = {
                        lazyRow.saveFocusedChild()
                        onEpisodeClick(season.season, episode.episode)
                    },
                    modifier = if (index == 0) Modifier.focusRequester(firstCard) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    isWatched: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowCard(
        onClick = onClick,
        modifier = Modifier
            .width(EpisodeCardWidth)
            .then(modifier),
        title = {
            Column(
                modifier = Modifier
                    .width(EpisodeCardWidth)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.episode, episode.episode),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = episode.title.ifBlank {
                        stringResource(R.string.episode, episode.episode)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = episode.episode.toString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                ),
            )

            if (isWatched) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp),
                )
            }
        }
    }
}
