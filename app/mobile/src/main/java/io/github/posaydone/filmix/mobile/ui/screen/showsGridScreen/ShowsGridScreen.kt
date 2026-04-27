package io.github.posaydone.filmix.mobile.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridUiState
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.toShow
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.Loading
import io.github.posaydone.filmix.mobile.ui.common.ShowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowsGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: ShowsGridScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val queryType by viewModel.queryType.collectAsStateWithLifecycle()

    val title = viewModel.screenTitle.ifBlank {
        when (queryType) {
            ShowsGridQueryType.FAVORITES -> "Избранное"
            ShowsGridQueryType.HISTORY -> "История"
            ShowsGridQueryType.WATCHING -> "Я смотрю"
            ShowsGridQueryType.CATALOG -> ""
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { paddingValues ->
        when (val state = uiState) {
            is ShowsGridUiState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            is ShowsGridUiState.Error -> {
                Error(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = viewModel::retry,
                )
            }

            is ShowsGridUiState.ShowsSuccess -> {
                ShowsGridContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    navigateToShowDetails = navigateToShowDetails,
                    shows = state.shows,
                    hasNextPage = state.hasNextPage,
                    onLoadNext = { viewModel.loadNextPage() },
                )
            }

            is ShowsGridUiState.HistorySuccess -> {
                ShowsGridContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    navigateToShowDetails = navigateToShowDetails,
                    shows = state.historyShows.map { it.toShow() },
                    hasNextPage = state.hasNextPage,
                    onLoadNext = { viewModel.loadNextPage() },
                )
            }
        }
    }
}

@Composable
fun ShowsGridContent(
    modifier: Modifier = Modifier,
    navigateToShowDetails: (showId: Int) -> Unit,
    shows: ShowList,
    hasNextPage: Boolean,
    onLoadNext: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(shows) { show ->
            ShowCard(
                show = show, onClick = {
                    navigateToShowDetails(show.id)
                }, modifier = Modifier.fillMaxWidth()
            )
        }

        if (hasNextPage) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem =
                gridState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= gridState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore, hasNextPage) {
        if (shouldLoadMore && hasNextPage) {
            onLoadNext()
        }
    }
}
