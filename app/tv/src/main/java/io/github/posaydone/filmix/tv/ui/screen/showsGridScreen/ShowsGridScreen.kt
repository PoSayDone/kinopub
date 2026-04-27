package io.github.posaydone.filmix.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridUiState
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.SideDialog

@Composable
fun ShowsGridScreen(
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

    var showFilterDialog by remember { mutableStateOf(false) }
    var filterPage by remember { mutableStateOf(FilterPage.MAIN) }
    var mainFilterFocusTarget by remember { mutableStateOf(MainFilterFocusTarget.CONTENT_TYPE) }
    val hasFilters = queryType == ShowsGridQueryType.CATALOG || queryType == ShowsGridQueryType.WATCHING
    val title = viewModel.screenTitle.ifBlank {
        when (queryType) {
            ShowsGridQueryType.FAVORITES -> stringResource(R.string.favorites)
            ShowsGridQueryType.HISTORY -> stringResource(R.string.watch_history_title)
            ShowsGridQueryType.WATCHING -> stringResource(R.string.watching_list)
            ShowsGridQueryType.CATALOG -> ""
        }
    }

    if (hasFilters) {
        val dialogTitle = when (filterPage) {
            FilterPage.MAIN -> stringResource(R.string.filter_title)
            FilterPage.CONTENT_TYPE -> stringResource(R.string.filter_content_type)
            FilterPage.SORT -> stringResource(R.string.filter_sort)
            FilterPage.PERIOD -> stringResource(R.string.filter_period)
            FilterPage.GENRE -> stringResource(R.string.filter_genre)
            FilterPage.COUNTRY -> stringResource(R.string.filter_country)
        }
        SideDialog(
            showDialog = showFilterDialog,
            onDismissRequest = {
                showFilterDialog = false
                filterPage = FilterPage.MAIN
                mainFilterFocusTarget = MainFilterFocusTarget.CONTENT_TYPE
            },
            title = dialogTitle,
            description = null,
            onBack = if (filterPage != FilterPage.MAIN) {
                { filterPage = FilterPage.MAIN }
            } else {
                null
            },
        ) {
            FilterDialogContent(
                queryType = queryType,
                filterPage = filterPage,
                mainFocusTarget = mainFilterFocusTarget,
                onNavigateTo = {
                    mainFilterFocusTarget = it.toMainFilterFocusTarget() ?: mainFilterFocusTarget
                    filterPage = it
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
                    showFilterDialog = false
                },
            )
        }
    }

    when (val state = uiState) {
        is ShowsGridUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is ShowsGridUiState.Error -> Error(
            modifier = Modifier.fillMaxSize(),
            onRetry = viewModel::retry,
        )

        is ShowsGridUiState.ShowsSuccess -> {
            when (queryType) {
                ShowsGridQueryType.WATCHING -> WatchingShowsGridScreen(
                    navigateToShowDetails = navigateToShowDetails,
                    shows = state.shows,
                    title = title,
                    onShowFilterDialog = { showFilterDialog = true },
                )

                ShowsGridQueryType.CATALOG,
                ShowsGridQueryType.FAVORITES -> ShowsCollectionGridScreen(
                    navigateToShowDetails = navigateToShowDetails,
                    shows = state.shows,
                    hasNextPage = state.hasNextPage,
                    onLoadNext = viewModel::loadNextPage,
                    title = title,
                    hasFilters = hasFilters,
                    onShowFilterDialog = { showFilterDialog = true },
                )

                ShowsGridQueryType.HISTORY -> Unit
            }
        }

        is ShowsGridUiState.HistorySuccess -> HistoryShowsGridScreen(
            navigateToShowDetails = navigateToShowDetails,
            historyShows = state.historyShows,
            hasNextPage = state.hasNextPage,
            onLoadNext = viewModel::loadNextPage,
            title = title,
        )
    }
}
