package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.Season

/**
 * Every season's episodes rendered as one continuous list — scrolling from the first
 * episode of the first season straight through to the last episode of the last season,
 * with a season header interleaved between each season's run of episodes. There is no
 * per-season tab/scroll state to switch between; this is the one list shared by the
 * player's episode picker and the show details screen's Episodes tab.
 */
@Composable
fun EpisodeList(
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    onEpisodeClick: (Season, Episode) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
    ) {
        episodeListItems(
            seasons = seasons,
            selectedSeason = selectedSeason,
            selectedEpisode = selectedEpisode,
            onEpisodeClick = onEpisodeClick,
        )
    }
}

/**
 * The same continuous season-header/episode-card items [EpisodeList] renders, exposed as a
 * [LazyListScope] extension so a screen that already owns an outer `LazyColumn` (the show
 * details screen's Episodes tab) can splice them in directly instead of nesting a second
 * scrollable list inside the first.
 */
fun LazyListScope.episodeListItems(
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    onEpisodeClick: (Season, Episode) -> Unit,
) {
    seasons.forEach { season ->
        item(key = "season-${season.season}") {
            SeasonHeader(season = season.season)
        }
        items(season.episodes, key = { "episode-${season.season}-${it.episode}" }) { episode ->
            EpisodeSelectionCard(
                episode = episode,
                selected = season.season == selectedSeason?.season && episode == selectedEpisode,
                onClick = { onEpisodeClick(season, episode) },
            )
        }
    }
}

/**
 * The flat item index of the selected episode (or its season header, if nothing is
 * selected yet) within the list [EpisodeList] renders, so callers can seed
 * `rememberLazyListState(initialFirstVisibleItemIndex = ...)` and land on the right
 * spot instead of always starting at season 1.
 */
fun episodeListInitialIndex(
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
): Int {
    var index = 0
    seasons.forEach { season ->
        if (season.season == selectedSeason?.season) {
            val episodeIndex = season.episodes.indexOf(selectedEpisode)
            return if (episodeIndex >= 0) index + 1 + episodeIndex else index
        }
        index += 1 + season.episodes.size
    }
    return 0
}

@Composable
private fun SeasonHeader(season: Int) {
    Text(
        text = stringResource(R.string.season, season),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun EpisodeSelectionCard(
    episode: Episode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(4.dp))
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
            }
        },
        headlineContent = {
            Text(
                text = episode.title.ifBlank { stringResource(R.string.episode, episode.episode) },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.episode, episode.episode),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        },
        trailingContent = {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
            }
        })
}
