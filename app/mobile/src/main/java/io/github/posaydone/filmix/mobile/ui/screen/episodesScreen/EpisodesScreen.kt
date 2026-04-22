package io.github.posaydone.filmix.mobile.ui.screen.episodesScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.findEpisodeProgress
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesScreen(
    showId: Int,
    navigateBack: () -> Unit,
    navigateToPlayer: (showId: Int, season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EpisodesScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    when (val s = uiState) {
                        is EpisodesScreenUiState.Done -> Text(
                            text = stringResource(R.string.episodesString),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        else -> Text(stringResource(R.string.episodesString))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        when (val s = uiState) {
            is EpisodesScreenUiState.Loading -> Loading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            is EpisodesScreenUiState.Error -> Error(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onRetry = s.onRetry,
            )

            is EpisodesScreenUiState.Done -> EpisodesContent(
                seasons = s.seasons,
                showProgress = s.showProgress,
                onEpisodeClick = { season, episode -> navigateToPlayer(showId, season, episode) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    val seasonListState = rememberLazyListState()

    if (seasons.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.episodesString),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(modifier = modifier) {
        LazyRow(
            state = seasonListState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(seasons) { index, season ->
                FilterChip(
                    selected = index == selectedSeasonIndex,
                    onClick = { selectedSeasonIndex = index },
                    label = {
                        Text(
                            text = stringResource(R.string.season, season.season),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }

        val currentSeason = seasons.getOrNull(selectedSeasonIndex)
        if (currentSeason != null) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = currentSeason.episodes,
                    key = { episode -> "${currentSeason.season}_${episode.episode}" },
                ) { episode ->
                    val progressItem = showProgress.findEpisodeProgress(
                        season = currentSeason.season,
                        episode = episode.episode,
                    )
                    EpisodeCard(
                        episode = episode,
                        seasonNumber = currentSeason.season,
                        watchedSeconds = progressItem?.time ?: 0L,
                        onClick = { onEpisodeClick(currentSeason.season, episode.episode) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    seasonNumber: Int,
    watchedSeconds: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWatched = watchedSeconds > 0
    val hasSignificantProgress = watchedSeconds > 60

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isWatched)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WatchStatusIcon(isWatched = isWatched)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.episode, episode.episode),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = episode.title.ifBlank {
                        stringResource(R.string.episode, episode.episode)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (episode.released.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = episode.released,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (hasSignificantProgress) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (watchedSeconds % 3600).toFloat() / 3600f },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = stringResource(R.string.play),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun WatchStatusIcon(isWatched: Boolean) {
    if (isWatched) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.Circle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}
