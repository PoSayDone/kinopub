package io.github.posaydone.kinopub.mobile.ui.screen.showsGridScreen

import android.util.Log
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowsGridUiState
import io.github.posaydone.kinopub.mobile.ui.common.Error
import io.github.posaydone.kinopub.mobile.ui.common.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowsGridScreen(
    navigateBack: () -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: ShowsGridScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val queryType by viewModel.queryType.collectAsStateWithLifecycle()
    val catalogContentType by viewModel.catalogContentType.collectAsStateWithLifecycle()
    val catalogSort by viewModel.catalogSort.collectAsStateWithLifecycle()
    val catalogPeriod by viewModel.catalogPeriod.collectAsStateWithLifecycle()
    val watchingFilter by viewModel.watchingFilter.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val countries by viewModel.countries.collectAsStateWithLifecycle()
    val selectedGenreIds by viewModel.selectedGenreIds.collectAsStateWithLifecycle()
    val selectedCountryIds by viewModel.selectedCountryIds.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    var filterPage by remember { mutableStateOf(FilterPage.MAIN) }

    val hasFilters =
        queryType == ShowsGridQueryType.CATALOG || queryType == ShowsGridQueryType.WATCHING
    val title = viewModel.screenTitle.ifBlank {
        when (queryType) {
            ShowsGridQueryType.FAVORITES -> stringResource(R.string.favorites)
            ShowsGridQueryType.HISTORY -> stringResource(R.string.history)
            ShowsGridQueryType.WATCHING -> stringResource(R.string.watching_list)
            ShowsGridQueryType.CATALOG -> ""
        }
    }

    if (hasFilters) {
        FilterBottomSheet(
            showSheet = showFilterSheet,
            queryType = queryType,
            filterPage = filterPage,
            onFilterPageChange = { filterPage = it },
            onDismiss = {
                showFilterSheet = false
                filterPage = FilterPage.MAIN
            },
            catalogContentType = catalogContentType,
            catalogSort = catalogSort,
            catalogPeriod = catalogPeriod,
            genres = genres,
            countries = countries,
            selectedGenreIds = selectedGenreIds,
            selectedCountryIds = selectedCountryIds,
            watchingFilter = watchingFilter,
            onCatalogContentTypeChange = { type ->
                viewModel.setCatalogContentType(type)
                filterPage = FilterPage.MAIN
            },
            onCatalogSortChange = { sort ->
                viewModel.setCatalogSort(sort)
                filterPage = FilterPage.MAIN
            },
            onCatalogPeriodChange = { period ->
                viewModel.setCatalogPeriod(period)
                filterPage = FilterPage.MAIN
            },
            onGenreChange = viewModel::setGenre,
            onCountryChange = viewModel::setCountry,
            onWatchingFilterChange = { filter ->
                viewModel.setWatchingFilter(filter)
                showFilterSheet = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    if (hasFilters) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.filter_title),
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            contentDescription = stringResource(R.string.back),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack
                        )
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
    ) { paddingValues ->
        when (val state = uiState) {
            is ShowsGridUiState.Loading -> Loading(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            is ShowsGridUiState.Error -> Error(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onRetry = viewModel::retry,
            )

            is ShowsGridUiState.ShowsSuccess -> when (queryType) {
                ShowsGridQueryType.WATCHING -> WatchingShowsGridScreen(
                    navigateToShowDetails = navigateToShowDetails,
                    shows = state.shows,
                    paddingValues = paddingValues,
                )

                ShowsGridQueryType.CATALOG,
                ShowsGridQueryType.FAVORITES -> ShowsCollectionGridScreen(
                    navigateToShowDetails = navigateToShowDetails,
                    shows = state.shows,
                    hasNextPage = state.hasNextPage,
                    onLoadNext = viewModel::loadNextPage,
                    paddingValues = paddingValues,
                )

                ShowsGridQueryType.HISTORY -> Unit
            }

            is ShowsGridUiState.HistorySuccess -> HistoryShowsGridScreen(
                navigateToShowDetails = navigateToShowDetails,
                historyShows = state.historyShows,
                hasNextPage = state.hasNextPage,
                onLoadNext = viewModel::loadNextPage,
                paddingValues = paddingValues,
            )
        }
    }
}
