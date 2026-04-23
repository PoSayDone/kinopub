package io.github.posaydone.filmix.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridUiState
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.common.CircularProgressIndicator
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.PosterImage
import io.github.posaydone.filmix.tv.ui.common.ShowCard
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding

@Composable
fun ShowsGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: ShowsGridScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentQueryType by viewModel.currentQueryType.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ShowsGridUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is ShowsGridUiState.Error -> {
            Error(
                modifier = Modifier.fillMaxSize(), onRetry = { /* Handle retry */ })
        }

        is ShowsGridUiState.Success -> {
            ShowsGridContent(
                navigateToShowDetails = navigateToShowDetails,
                shows = state.shows,
                hasNextPage = state.hasNextPage,
                onLoadNext = { viewModel.loadNextPage() },
                queryType = currentQueryType
            )
        }
    }
}

@Composable
fun ShowsGridContent(
    navigateToShowDetails: (showId: Int) -> Unit,
    shows: ShowList,
    hasNextPage: Boolean,
    onLoadNext: () -> Unit,
    queryType: ShowsGridQueryType,
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(
            start = 100.dp, end = 100.dp, top = childPadding.top, bottom = childPadding.bottom
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = if (queryType == ShowsGridQueryType.FAVORITES) "Favorite Shows" else "History",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    vertical = childPadding.top,
                )
            )
        }
        items(shows, key = { it.id }) { show ->
            ShowCard(
                onClick = {
                    navigateToShowDetails(show.id)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(2f / 3f) // 2:3 aspect ratio
                ) {
                    PosterImage(
                        imageUrl = show.poster,
                        contentDescritpion = show.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (hasNextPage) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CircularProgressIndicator()
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