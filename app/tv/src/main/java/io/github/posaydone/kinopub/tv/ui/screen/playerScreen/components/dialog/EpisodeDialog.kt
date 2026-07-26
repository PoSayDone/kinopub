package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.tv.ui.common.ScrollableTabRow
import io.github.posaydone.kinopub.tv.ui.common.SideDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@UnstableApi
@Composable
fun EpisodeDialog(
    viewModel: PlayerScreenViewModel,
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    isEpisodeDialogOpen: Boolean,
    onDismiss: () -> Unit,
) {
    // Same idea as HomeScreen's outer LazyColumn of rows, just horizontal: each season
    // is its own persistent page with its own independent focus state (see
    // SeasonPage below, which mirrors ShowsRow internally). Switching seasons is plain
    // LazyRow item-to-item focus movement — no custom key handling needed — so each
    // season keeps its own remembered episode position exactly like ShowsRow keeps a
    // separate lastFocusedIndex per row.
    val seasonsList = seasons.map { season -> stringResource(R.string.season, season.season) }

    val initialSeasonIndex = remember(seasons) {
        selectedSeason?.let { s -> seasons.indexOfFirst { it.season == s.season } }
            ?.takeIf { it >= 0 } ?: 0
    }

    val outerListState = rememberLazyListState(initialFirstVisibleItemIndex = initialSeasonIndex)
    val outerFocusRequester = remember { FocusRequester() }
    val restoredSeasonFocusRequester = remember { FocusRequester() }
    var lastFocusedSeasonIndex by rememberSaveable { mutableIntStateOf(initialSeasonIndex) }
    val scope = rememberCoroutineScope()

    SideDialog(
        showDialog = isEpisodeDialogOpen,
        onDismissRequest = onDismiss,
        width = 480.dp,
        title = stringResource(R.string.select_episode),
        description = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .focusable(false)
        ) {
            // Purely a visual/navigational aid layered on top of the LazyRow below —
            // it only mirrors lastFocusedSeasonIndex and scrolls the page list into
            // view. It never requests focus itself, so it can't race the page-level
            // onEnter/restoredSeasonFocusRequester mechanism that actually owns focus.
            ScrollableTabRow(
                items = seasonsList,
                selectedTabIndex = lastFocusedSeasonIndex,
                onTabSelected = { index ->
                    lastFocusedSeasonIndex = index
                    scope.launch { outerListState.animateScrollToItem(index) }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                state = outerListState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusable(false)
                    .focusRequester(outerFocusRequester)
                    .focusProperties {
                        onEnter = { runCatching { restoredSeasonFocusRequester.requestFocus() } }
                    },
                contentPadding = PaddingValues(0.dp),
            ) {
                itemsIndexed(seasons, key = { _, season -> season.season }) { index, season ->
                    val itemModifier = if (index == lastFocusedSeasonIndex) {
                        Modifier.focusRequester(restoredSeasonFocusRequester)
                    } else {
                        Modifier
                    }

                    SeasonPage(
                        season = season,
                        isActiveSeason = season.season == selectedSeason?.season,
                        selectedEpisode = selectedEpisode,
                        onEpisodeSelected = { episode ->
                            viewModel.setSeason(season)
                            viewModel.setEpisode(episode)
                            onDismiss()
                        },
                        modifier = itemModifier
                            .fillParentMaxWidth()
                            .fillMaxSize()
                            .onFocusChanged { if (it.hasFocus) lastFocusedSeasonIndex = index },
                    )
                }
            }
        }
    }

    // Land on the current season/episode when the dialog first opens, mirroring
    // HomeScreen's `LaunchedEffect(Unit) { lazyColumn.requestFocus() }` — request focus
    // on the group itself and let `onEnter` decide which page to land on.
    LaunchedEffect(isEpisodeDialogOpen) {
        if (isEpisodeDialogOpen) runCatching { outerFocusRequester.requestFocus() }
    }
}

/**
 * One season's episode list. Structurally identical to ShowsRow: owns its own
 * lastFocusedIndex (seeded from the actively-playing episode when this is that
 * season, saved/restored per-season automatically since this whole composable is
 * keyed to a single season and stays mounted as long as it's near the LazyRow's
 * viewport), and its own onEnter-based restoration.
 */
@Composable
private fun SeasonPage(
    season: Season,
    isActiveSeason: Boolean,
    selectedEpisode: Episode?,
    onEpisodeSelected: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialEpisodeIndex = remember(season) {
        if (isActiveSeason && selectedEpisode != null) {
            season.episodes.indexOf(selectedEpisode).takeIf { it >= 0 } ?: 0
        } else {
            0
        }
    }
    var lastFocusedEpisodeIndex by rememberSaveable(season.season) {
        mutableIntStateOf(initialEpisodeIndex)
    }

    val innerListState = rememberLazyListState(initialFirstVisibleItemIndex = initialEpisodeIndex)
    val innerFocusRequester = remember { FocusRequester() }
    val restoredEpisodeFocusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {

        LazyColumn(
            state = innerListState,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(innerFocusRequester)
                .focusProperties {
                    onEnter = { runCatching { restoredEpisodeFocusRequester.requestFocus() } }
                },
            contentPadding = PaddingValues(0.dp),
        ) {
            itemsIndexed(season.episodes, key = { _, episode -> episode.episode }) { index, episode ->
                val isSelectedEpisode = isActiveSeason && episode == selectedEpisode
                val itemModifier = if (index == lastFocusedEpisodeIndex) {
                    Modifier.focusRequester(restoredEpisodeFocusRequester)
                } else {
                    Modifier
                }

                EpisodeSelectionCard(
                    modifier = itemModifier
                        .onFocusChanged { if (it.isFocused) lastFocusedEpisodeIndex = index },
                    episode = episode,
                    selected = isSelectedEpisode,
                    onClick = { onEpisodeSelected(episode) })
            }
        }
    }
}

@Composable
private fun EpisodeSelectionCard(
    episode: Episode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        scale = ListItemDefaults.scale(focusedScale = 1.02f),
        selected = selected,
        onClick = onClick,
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
                style = LocalTextStyle.current.copy(lineHeight = LocalTextStyle.current.fontSize),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.episode, episode.episode),
                style = MaterialTheme.typography.labelSmall,
                color = LocalContentColor.current.copy(alpha = 0.6f),
            )
        },
        trailingContent = {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
            }
        })
}
