package io.github.posaydone.filmix.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.CatalogPeriod
import io.github.posaydone.filmix.core.common.sharedViewModel.CatalogSort
import io.github.posaydone.filmix.core.common.sharedViewModel.ContentTypeOption
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.WatchingFilter
import io.github.posaydone.filmix.core.common.sharedViewModel.allContentTypes
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
    val queryType by viewModel.queryType.collectAsStateWithLifecycle()
    val catalogContentType by viewModel.catalogContentType.collectAsStateWithLifecycle()
    val catalogSort by viewModel.catalogSort.collectAsStateWithLifecycle()
    val catalogPeriod by viewModel.catalogPeriod.collectAsStateWithLifecycle()
    val watchingFilter by viewModel.watchingFilter.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ShowsGridUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is ShowsGridUiState.Error -> Error(modifier = Modifier.fillMaxSize(), onRetry = {})
        is ShowsGridUiState.Success -> ShowsGridContent(
            navigateToShowDetails = navigateToShowDetails,
            shows = state.shows,
            hasNextPage = state.hasNextPage,
            onLoadNext = viewModel::loadNextPage,
            queryType = queryType,
            title = viewModel.screenTitle,
            catalogContentType = catalogContentType,
            catalogSort = catalogSort,
            catalogPeriod = catalogPeriod,
            onCatalogContentTypeChange = viewModel::setCatalogContentType,
            onCatalogSortChange = viewModel::setCatalogSort,
            onCatalogPeriodChange = viewModel::setCatalogPeriod,
            watchingFilter = watchingFilter,
            onWatchingFilterChange = viewModel::setWatchingFilter,
        )
    }
}

@Composable
private fun ShowsGridContent(
    navigateToShowDetails: (showId: Int) -> Unit,
    shows: ShowList,
    hasNextPage: Boolean,
    onLoadNext: () -> Unit,
    queryType: ShowsGridQueryType,
    title: String,
    catalogContentType: String?,
    catalogSort: CatalogSort,
    catalogPeriod: CatalogPeriod,
    onCatalogContentTypeChange: (String?) -> Unit,
    onCatalogSortChange: (CatalogSort) -> Unit,
    onCatalogPeriodChange: (CatalogPeriod) -> Unit,
    watchingFilter: WatchingFilter,
    onWatchingFilterChange: (WatchingFilter) -> Unit,
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()
    val horizontalPadding = 100.dp

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter bar
        when (queryType) {
            ShowsGridQueryType.WATCHING -> WatchingFilterBar(
                selected = watchingFilter,
                onSelect = onWatchingFilterChange,
                horizontalPadding = horizontalPadding,
            )
            ShowsGridQueryType.CATALOG -> CatalogFilterBar(
                contentType = catalogContentType,
                sort = catalogSort,
                period = catalogPeriod,
                onContentTypeChange = onCatalogContentTypeChange,
                onSortChange = onCatalogSortChange,
                onPeriodChange = onCatalogPeriodChange,
                horizontalPadding = horizontalPadding,
            )
            else -> Unit
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = if (queryType == ShowsGridQueryType.FAVORITES || queryType == ShowsGridQueryType.HISTORY)
                    childPadding.top else 16.dp,
                bottom = childPadding.bottom,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                val headerText = title.ifBlank {
                    when (queryType) {
                        ShowsGridQueryType.FAVORITES -> "Избранное"
                        ShowsGridQueryType.HISTORY -> "История просмотра"
                        ShowsGridQueryType.WATCHING -> "Я смотрю"
                        ShowsGridQueryType.CATALOG -> ""
                    }
                }
                if (headerText.isNotBlank()) {
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = childPadding.top),
                    )
                }
            }

            items(shows, key = { it.id }) { show ->
                ShowCard(
                    onClick = { navigateToShowDetails(show.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(modifier = Modifier.fillMaxSize().aspectRatio(2f / 3f)) {
                        PosterImage(
                            imageUrl = show.poster,
                            contentDescritpion = show.title,
                            modifier = Modifier.fillMaxSize(),
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
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisible.index >= gridState.layoutInfo.totalItemsCount - 5
        }
    }
    LaunchedEffect(shouldLoadMore, hasNextPage) {
        if (shouldLoadMore && hasNextPage) onLoadNext()
    }
}

// — Filter bars —

@Composable
private fun WatchingFilterBar(
    selected: WatchingFilter,
    onSelect: (WatchingFilter) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(WatchingFilter.entries) { filter ->
            FilterChip(
                label = filter.label,
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun CatalogFilterBar(
    contentType: String?,
    sort: CatalogSort,
    period: CatalogPeriod,
    onContentTypeChange: (String?) -> Unit,
    onSortChange: (CatalogSort) -> Unit,
    onPeriodChange: (CatalogPeriod) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    Column {
        // Content type row
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allContentTypes) { option: ContentTypeOption ->
                FilterChip(
                    label = option.label,
                    selected = option.apiValue == contentType,
                    onClick = { onContentTypeChange(option.apiValue) },
                )
            }
        }
        // Sort row
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(CatalogSort.entries) { s ->
                FilterChip(
                    label = s.label,
                    selected = s == sort,
                    onClick = { onSortChange(s) },
                )
            }
        }
        // Period row — only relevant when sorting by views
        if (sort == CatalogSort.VIEWS) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(CatalogPeriod.entries) { p ->
                    FilterChip(
                        label = p.label,
                        selected = p == period,
                        onClick = { onPeriodChange(p) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            focusedContentColor = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface,
        ),
        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
