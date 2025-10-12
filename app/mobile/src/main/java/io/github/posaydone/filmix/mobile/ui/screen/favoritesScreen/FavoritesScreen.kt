package io.github.posaydone.filmix.mobile.ui.screen.favoritesScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.FavoritesScreenViewModel
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.Loading
import io.github.posaydone.filmix.mobile.ui.common.ShowsRow
import org.w3c.dom.Text
import androidx.compose.ui.res.stringResource
import io.github.posaydone.filmix.core.common.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navigateToShowsGrid: (queryType: String) -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: FavoritesScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mine)) }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
    ) { paddingValues ->
        when (val s = uiState) {
            is FavoritesScreenUiState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            is FavoritesScreenUiState.Error -> {
                Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry)
            }

            is FavoritesScreenUiState.Done -> {
                FavoritesScreenContent(
                    modifier = Modifier.padding(paddingValues),
                    navigateToShowDetails = navigateToShowDetails,
                    navigateToShowsGrid = navigateToShowsGrid,
                    favoritesList = s.favoritesList,
                    historyList = s.historyList
                )
            }
        }
    }
}

@Composable
fun FavoritesScreenContent(
    modifier: Modifier = Modifier,
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToShowsGrid: (queryType: String) -> Unit,
    favoritesList: List<io.github.posaydone.filmix.core.model.Show>,
    historyList: List<io.github.posaydone.filmix.core.model.Show>,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        ShowsRow(
            title = stringResource(R.string.favorites),
            modifier = Modifier.fillMaxWidth(),
            showList = favoritesList,
            onShowClick = { show ->
                navigateToShowDetails(show.id)
            },
            onViewAll = {
                navigateToShowsGrid("FAVORITES")
            })

        ShowsRow(
            title = stringResource(R.string.history),
            modifier = Modifier.padding(top = 16.dp),
            showList = historyList,
            onShowClick = { show ->
                navigateToShowDetails(show.id)
            },
            onViewAll = {
                navigateToShowsGrid("HISTORY")
            })
    }
}