package io.github.posaydone.filmix.tv.ui.screen.favoritesScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowCard
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec

private val HistoryCardWidth = 220.dp

@Composable
fun FavoritesScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToShowsGrid: (queryType: String) -> Unit,
    viewModel: FavoritesScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is FavoritesScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is FavoritesScreenUiState.Error -> Error(
            modifier = Modifier.fillMaxSize(),
            onRetry = s.onRetry
        )

        is FavoritesScreenUiState.Done -> FavoritesScreenContent(
            navigateToShowDetails = navigateToShowDetails,
            navigateToShowsGrid = navigateToShowsGrid,
            watchingList = s.watchingList,
            historyList = s.historyList,
        )
    }
}

@Composable
private fun FavoritesScreenContent(
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToShowsGrid: (queryType: String) -> Unit,
    watchingList: ShowList,
    historyList: List<HistoryShow>,
) {
    val childPadding = rememberChildPadding()
    val lazyListState = rememberLazyListState()
    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(lazyColumn)
            .focusRestorer(firstItem),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                modifier = Modifier.padding(
                    top = 24.dp + childPadding.top,
                    bottom = 24.dp,
                    start = childPadding.start,
                ),
                text = "Я смотрю",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            ShowsRow(
                title = "Я смотрю",
                modifier = Modifier.focusRequester(firstItem),
                showList = watchingList,
                onShowSelected = { show ->
                    lazyColumn.saveFocusedChild()
                    navigateToShowDetails(show.id)
                },
                onViewAll = {
                    navigateToShowsGrid(ShowsGridQueryType.WATCHING.name)
                },
            )
        }
        item {
            HistoryRow(
                title = stringResource(R.string.history),
                modifier = Modifier.padding(bottom = childPadding.bottom),
                historyList = historyList,
                onShowSelected = { show ->
                    lazyColumn.saveFocusedChild()
                    navigateToShowDetails(show.id)
                },
                onViewAll = {
                    navigateToShowsGrid(ShowsGridQueryType.HISTORY.name)
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(
    historyList: List<HistoryShow>,
    title: String,
    modifier: Modifier = Modifier,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    onShowSelected: (HistoryShow) -> Unit = {},
    onViewAll: (() -> Unit)? = null,
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }
    val horizontalBivs = remember { CustomBringIntoViewSpec(0.4f, 0f) }

    CompositionLocalProvider(LocalBringIntoViewSpec provides horizontalBivs) {
        Column(
            modifier = modifier.focusable(false)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                ),
                modifier = Modifier.padding(start = startPadding, top = 16.dp, bottom = 16.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(start = startPadding, end = endPadding),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem),
            ) {
                if (onViewAll != null) {
                    item {
                        HistoryViewAllCard(
                            onClick = onViewAll,
                            modifier = Modifier.focusRequester(firstItem),
                        )
                    }
                }

                itemsIndexed(historyList, key = { _, show -> show.id }) { index, show ->
                    HistoryCard(
                        show = show,
                        onClick = {
                            lazyRow.saveFocusedChild()
                            onShowSelected(show)
                        },
                        modifier = if (index == 0 && onViewAll == null) {
                            Modifier.focusRequester(firstItem)
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    show: HistoryShow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryTitle = show.title.substringBefore('/').trim()

    var episodeLabel: String?
    val episodeTitle: String

    if (show.isSeries && show.seasonNumber != null && show.episodeNumber != null) {
        val position = stringResource(
            R.string.continueWatchingSeries,
            show.seasonNumber!!,
            show.episodeNumber!!
        )
        episodeLabel = show.episodeTitle?.let { "$position: $it" } ?: position
        episodeTitle = primaryTitle
    } else if (show.isSeries && show.episodeTitle != null) {
        episodeLabel = show.episodeTitle!!
        episodeTitle = primaryTitle
    } else {
        episodeLabel = null
        episodeTitle = primaryTitle
    }

    val watchedSeconds = (show.watchedSeconds ?: 0).toFloat().coerceAtLeast(0f)
    val hasProgress = watchedSeconds > 60f
    val durationSeconds = (show.durationSeconds?.takeIf { it > 0 } ?: 3600).toFloat()
    val progress = (watchedSeconds / durationSeconds).coerceIn(0f, 1f)
    val imageUrl = show.thumbnail?.takeIf { it.isNotBlank() } ?: show.poster

    ShowCard(
        onClick = onClick,
        modifier = Modifier
            .width(HistoryCardWidth)
            .then(modifier),
        title = {
            Column(
                modifier = Modifier
                    .width(HistoryCardWidth)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (!episodeLabel.isNullOrBlank()) {
                    Text(
                        text = episodeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = episodeTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .crossfade(true)
                    .data(imageUrl)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

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
private fun HistoryViewAllCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowCard(
        onClick = onClick,
        modifier = Modifier
            .width(HistoryCardWidth)
            .then(modifier),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
