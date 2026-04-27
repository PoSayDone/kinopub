package io.github.posaydone.filmix.tv.ui.screen.favoritesScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.HistoryShowsRow
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding


@Composable
fun FavoritesScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToPlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
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
            navigateToPlayer = navigateToPlayer,
            navigateToShowsGrid = navigateToShowsGrid,
            watchingList = s.watchingList,
            historyList = s.historyList,
        )
    }
}

@Composable
private fun FavoritesScreenContent(
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToPlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
    navigateToShowsGrid: (queryType: String) -> Unit,
    watchingList: ShowList,
    historyList: List<HistoryShow>,
) {
    val childPadding = rememberChildPadding()
    val lazyListState = rememberLazyListState()
    val lazyColumn = remember { FocusRequester() }
    val restoredRow = remember { FocusRequester() }
    var lastFocusedRowKey by rememberSaveable { mutableStateOf("WatchingRow") }

    LaunchedEffect(Unit) {
        runCatching { lazyColumn.requestFocus() }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .focusRequester(lazyColumn)
            .focusProperties { onEnter = { runCatching { restoredRow.requestFocus() } } }
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                modifier = Modifier.padding(
                    top = 24.dp + childPadding.top,
                    bottom = 24.dp,
                    start = childPadding.start,
                ),
                text = stringResource(R.string.watching_list),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            ShowsRow(
                title = stringResource(R.string.watching_list),
                modifier = Modifier
                    .then(if (lastFocusedRowKey == "WatchingRow") Modifier.focusRequester(restoredRow) else Modifier)
                    .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "WatchingRow" },
                showList = watchingList,
                onShowSelected = { show ->
                    navigateToShowDetails(show.id)
                },
                onViewAll = {
                    navigateToShowsGrid(ShowsGridQueryType.WATCHING.name)
                },
            )
        }
        item {
            HistoryShowsRow(
                title = stringResource(R.string.history),
                modifier = Modifier
                    .padding(bottom = childPadding.bottom)
                    .then(if (lastFocusedRowKey == "HistoryRow") Modifier.focusRequester(restoredRow) else Modifier)
                    .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "HistoryRow" },
                historyList = historyList,
                onShowSelected = { show ->
                    navigateToPlayer(show.id, show.seasonNumber ?: -1, show.episodeNumber ?: -1)
                },
                onViewAll = {
                    navigateToShowsGrid(ShowsGridQueryType.HISTORY.name)
                },
            )
        }
    }
}

