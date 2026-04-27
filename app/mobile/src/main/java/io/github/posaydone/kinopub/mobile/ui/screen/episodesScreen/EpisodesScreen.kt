package io.github.posaydone.kinopub.mobile.ui.screen.episodesScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.core.model.ShowProgress
import io.github.posaydone.kinopub.core.model.findEpisodeProgress
import io.github.posaydone.kinopub.mobile.ui.common.Error
import io.github.posaydone.kinopub.mobile.ui.common.Loading
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val EpisodeCardWidth = 220.dp

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

    Scaffold(
        modifier = modifier,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(
            NavigationBarDefaults.windowInsets.union(WindowInsets.statusBars)
        ),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.episodesString), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
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
                contentPadding = paddingValues,
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
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.episodesString),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        itemsIndexed(seasons, key = { _, season -> season.season }) { _, season ->
            SeasonRow(
                season = season,
                showProgress = showProgress,
                onEpisodeClick = onEpisodeClick,
            )
        }
    }
}

@Composable
private fun SeasonRow(
    season: Season,
    showProgress: ShowProgress,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.season, season.season),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(
                items = season.episodes,
                key = { _, episode -> "${season.season}_${episode.episode}" },
            ) { _, episode ->
                val progressItem = showProgress.findEpisodeProgress(
                    season = season.season,
                    episode = episode.episode,
                )
                EpisodeCard(
                    episode = episode,
                    watchedSeconds = progressItem?.time ?: 0L,
                    onClick = { onEpisodeClick(season.season, episode.episode) },
                )
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    watchedSeconds: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWatched = watchedSeconds > 0
    val hasSignificantProgress = watchedSeconds > 60

    Card(
        onClick = onClick,
        modifier = modifier.width(EpisodeCardWidth),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (episode.thumbnail != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .crossfade(true)
                            .data(episode.thumbnail)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = episode.episode.toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        ),
                    )
                }

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

                if (hasSignificantProgress) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((watchedSeconds % 3600).toFloat() / 3600f)
                                .height(3.dp)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
        }
    }
}
