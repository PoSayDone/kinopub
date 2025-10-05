package io.github.posaydone.filmix.tv.ui.screen.favoritesScreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding

@Composable
fun FavoritesScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToShowsGrid: (queryType: String) -> Unit,
    viewModel: FavoritesScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is FavoritesScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is FavoritesScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry)
        }

        is FavoritesScreenUiState.Done -> {
            FavoritesScreenContent(
                navigateToShowDetails = navigateToShowDetails,
                navigateToShowsGrid = navigateToShowsGrid,
                favoritesList = s.favoritesList,
                historyList = s.historyList
            )
        }
    }
}

@Composable
fun FavoritesScreenContent(
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToShowsGrid: (queryType: String) -> Unit,
    favoritesList: ShowList,
    historyList: ShowList,
) {
    val childPadding = rememberChildPadding()
    val lazyListState = rememberLazyListState()

    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    val TAG = "FAVORITE"

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(lazyColumn)
            .focusRestorer(firstItem),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                modifier = Modifier.padding(
                    top = 24.dp + childPadding.top, bottom = 24.dp, start = childPadding.start
                ),
                text = "Favorites",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        item {
            ShowsRow(
                title = "Favorite",
                modifier = Modifier.focusRequester(firstItem),
                showList = favoritesList,
                onShowSelected = { show ->
                    lazyColumn.saveFocusedChild()
                    navigateToShowDetails(show.id)
                },
                onViewAll = {
                    navigateToShowsGrid(
                        ShowsGridQueryType.FAVORITES.name
                    )
                })
        }
        item {
            ShowsRow(
                title = "History", modifier = Modifier.padding(
                    bottom = childPadding.bottom
                ), showList = historyList, onShowSelected = { show ->
                    lazyColumn.saveFocusedChild()
                    navigateToShowDetails(show.id)
                }, onViewAll = {
                    navigateToShowsGrid(
                        ShowsGridQueryType.HISTORY.name
                    )
                })
        }
    }
}