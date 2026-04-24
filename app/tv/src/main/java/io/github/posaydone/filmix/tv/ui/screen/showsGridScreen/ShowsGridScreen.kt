package io.github.posaydone.filmix.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.CatalogPeriod
import io.github.posaydone.filmix.core.common.sharedViewModel.CatalogSort
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.WatchingFilter
import io.github.posaydone.filmix.core.common.sharedViewModel.allContentTypes
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.kinopub.KinoPubCountry
import io.github.posaydone.filmix.core.model.kinopub.KinoPubGenre
import io.github.posaydone.filmix.tv.ui.common.CircularProgressIndicator
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.PosterImage
import io.github.posaydone.filmix.tv.ui.common.ShowCard
import io.github.posaydone.filmix.tv.ui.common.SideDialog
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import java.util.Locale

private enum class FilterPage { MAIN, CONTENT_TYPE, SORT, PERIOD, GENRE, COUNTRY }

private enum class MainFilterFocusTarget { CONTENT_TYPE, SORT, PERIOD, GENRE, COUNTRY }

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

    if (hasFilters) {
        val dialogTitle = when (filterPage) {
            FilterPage.MAIN -> "Фильтры"
            FilterPage.CONTENT_TYPE -> "Тип контента"
            FilterPage.SORT -> "Сортировка"
            FilterPage.PERIOD -> "Период"
            FilterPage.GENRE -> "Жанр"
            FilterPage.COUNTRY -> "Страна"
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
            } else null,
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
        is ShowsGridUiState.Error -> Error(modifier = Modifier.fillMaxSize(), onRetry = {})
        is ShowsGridUiState.Success -> ShowsGridContent(
            navigateToShowDetails = navigateToShowDetails,
            shows = state.shows,
            hasNextPage = state.hasNextPage,
            onLoadNext = viewModel::loadNextPage,
            queryType = queryType,
            title = viewModel.screenTitle,
            hasFilters = hasFilters,
            onShowFilterDialog = { showFilterDialog = true },
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
    hasFilters: Boolean,
    onShowFilterDialog: () -> Unit,
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()
    val horizontalPadding = 100.dp

    val headerText = title.ifBlank {
        when (queryType) {
            ShowsGridQueryType.FAVORITES -> "Избранное"
            ShowsGridQueryType.HISTORY -> "История просмотра"
            ShowsGridQueryType.WATCHING -> "Я смотрю"
            ShowsGridQueryType.CATALOG -> ""
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            top = childPadding.top,
            bottom = childPadding.bottom,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (headerText.isNotBlank()) {
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    Spacer(Modifier)
                }
                if (hasFilters) {
                    Button(onClick = onShowFilterDialog) {
                        Text("Фильтры")
                    }
                }
            }
        }

        items(shows, key = { it.id }) { show ->
            ShowCard(
                onClick = { navigateToShowDetails(show.id) },
                modifier = Modifier.fillMaxWidth(),
                title = {
                    ShowsGridCardInfo(
                        show = show,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxSize().aspectRatio(2f / 3f)) {
                    PosterImage(
                        imageUrl = show.poster,
                        contentDescritpion = show.title,
                        modifier = Modifier.fillMaxSize(),
                    )

                    show.primaryRating()?.let { rating ->
                        ShowRatingBadge(
                            rating = rating,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        )
                    }
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
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisible.index >= gridState.layoutInfo.totalItemsCount - 5
        }
    }
    LaunchedEffect(shouldLoadMore, hasNextPage) {
        if (shouldLoadMore && hasNextPage) onLoadNext()
    }
}

@Composable
private fun ShowsGridCardInfo(
    show: Show,
    modifier: Modifier = Modifier,
) {
    val slashIndex = show.title.indexOf('/')
    val primaryTitle = if (slashIndex != -1) {
        show.title.substring(0, slashIndex).trim()
    } else {
        show.title
    }
    val originalTitle = if (slashIndex != -1) {
        show.title.substring(slashIndex + 1).trim().takeIf { it.isNotEmpty() }
    } else {
        null
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = primaryTitle,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (originalTitle != null) {
            Text(
                text = originalTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (show.year > 0) {
            Text(
                text = show.year.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ShowRatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

private fun Show.primaryRating(): Double? = ratingKp ?: ratingImdb

// — Filter dialog content —

@Composable
private fun FilterDialogContent(
    queryType: ShowsGridQueryType,
    filterPage: FilterPage,
    mainFocusTarget: MainFilterFocusTarget,
    onNavigateTo: (FilterPage) -> Unit,
    catalogContentType: String?,
    catalogSort: CatalogSort,
    catalogPeriod: CatalogPeriod,
    genres: List<KinoPubGenre>,
    countries: List<KinoPubCountry>,
    selectedGenreIds: Set<Int>,
    selectedCountryIds: Set<Int>,
    watchingFilter: WatchingFilter,
    onCatalogContentTypeChange: (String?) -> Unit,
    onCatalogSortChange: (CatalogSort) -> Unit,
    onCatalogPeriodChange: (CatalogPeriod) -> Unit,
    onGenreChange: (Int?) -> Unit,
    onCountryChange: (Int?) -> Unit,
    onWatchingFilterChange: (WatchingFilter) -> Unit,
) {
    val showPeriod = catalogSort == CatalogSort.WATCHERS || catalogSort == CatalogSort.VIEWS
    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(filterPage, queryType, mainFocusTarget) {
        initialFocusRequester.requestFocus()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when (queryType) {
            ShowsGridQueryType.WATCHING -> {
                val initialWatchingFilter = watchingFilter

                items(WatchingFilter.entries) { filter ->
                    FilterOption(
                        modifier = if (filter == initialWatchingFilter) {
                            Modifier.focusRequester(initialFocusRequester)
                        } else {
                            Modifier
                        },
                        label = filter.label,
                        selected = filter == watchingFilter,
                        onClick = { onWatchingFilterChange(filter) },
                    )
                }
            }
            ShowsGridQueryType.CATALOG -> when (filterPage) {
                FilterPage.MAIN -> {
                    val menuItems = buildList {
                        add(
                            FilterMenuEntry(
                                label = "Тип контента",
                                summary = allContentTypes.find { it.apiValue == catalogContentType }?.label
                                    ?: "Все",
                                page = FilterPage.CONTENT_TYPE,
                            )
                        )
                        add(
                            FilterMenuEntry(
                                label = "Сортировка",
                                summary = catalogSort.label,
                                page = FilterPage.SORT,
                            )
                        )
                        if (showPeriod) {
                            add(
                                FilterMenuEntry(
                                    label = "Период",
                                    summary = catalogPeriod.label,
                                    page = FilterPage.PERIOD,
                                )
                            )
                        }
                        if (genres.isNotEmpty()) {
                            val genreSummary = when {
                                selectedGenreIds.isEmpty() -> "Все"
                                selectedGenreIds.size == 1 ->
                                    genres.firstOrNull { it.id in selectedGenreIds }?.title ?: "1 выбран"
                                else -> "${selectedGenreIds.size} выбрано"
                            }
                            add(
                                FilterMenuEntry(
                                    label = "Жанр",
                                    summary = genreSummary,
                                    page = FilterPage.GENRE,
                                )
                            )
                        }
                        if (countries.isNotEmpty()) {
                            val countrySummary = when {
                                selectedCountryIds.isEmpty() -> "Все"
                                selectedCountryIds.size == 1 ->
                                    countries.firstOrNull { it.id in selectedCountryIds }?.title ?: "1 выбрана"
                                else -> "${selectedCountryIds.size} выбрано"
                            }
                            add(
                                FilterMenuEntry(
                                    label = "Страна",
                                    summary = countrySummary,
                                    page = FilterPage.COUNTRY,
                                )
                            )
                        }
                    }

                    val initialMainPage = mainFocusTarget.toFilterPage().takeIf { targetPage ->
                        menuItems.any { it.page == targetPage }
                    } ?: menuItems.firstOrNull()?.page

                    items(menuItems, key = { it.page.name }) { entry ->
                        FilterMenuItem(
                            modifier = if (entry.page == initialMainPage) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = entry.label,
                            summary = entry.summary,
                            onClick = { onNavigateTo(entry.page) },
                        )
                    }
                }
                FilterPage.CONTENT_TYPE -> {
                    val initialContentType = catalogContentType
                    items(allContentTypes) { option ->
                        FilterOption(
                            modifier = if (option.apiValue == initialContentType ||
                                (initialContentType == null && option == allContentTypes.firstOrNull())
                            ) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = option.label,
                            selected = option.apiValue == catalogContentType,
                            onClick = { onCatalogContentTypeChange(option.apiValue) },
                        )
                    }
                }
                FilterPage.SORT -> {
                    val initialSort = catalogSort
                    items(CatalogSort.entries) { sort ->
                        FilterOption(
                            modifier = if (sort == initialSort) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = sort.label,
                            selected = sort == catalogSort,
                            onClick = { onCatalogSortChange(sort) },
                        )
                    }
                }
                FilterPage.PERIOD -> {
                    val initialPeriod = catalogPeriod
                    items(CatalogPeriod.entries) { period ->
                        FilterOption(
                            modifier = if (period == initialPeriod) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = period.label,
                            selected = period == catalogPeriod,
                            onClick = { onCatalogPeriodChange(period) },
                        )
                    }
                }
                FilterPage.GENRE -> {
                    val initialGenreId = genres.firstOrNull { it.id in selectedGenreIds }?.id

                    item {
                        FilterOption(
                            modifier = if (selectedGenreIds.isEmpty()) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = "Все жанры",
                            selected = selectedGenreIds.isEmpty(),
                            onClick = { onGenreChange(null) },
                        )
                    }
                    items(genres, key = { it.id }) { genre ->
                        FilterOption(
                            modifier = if (genre.id == initialGenreId) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = genre.title,
                            selected = genre.id in selectedGenreIds,
                            onClick = { onGenreChange(genre.id) },
                        )
                    }
                }
                FilterPage.COUNTRY -> {
                    val initialCountryId = countries.firstOrNull { it.id in selectedCountryIds }?.id

                    item {
                        FilterOption(
                            modifier = if (selectedCountryIds.isEmpty()) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = "Все страны",
                            selected = selectedCountryIds.isEmpty(),
                            onClick = { onCountryChange(null) },
                        )
                    }
                    items(countries, key = { it.id }) { country ->
                        FilterOption(
                            modifier = if (country.id == initialCountryId) {
                                Modifier.focusRequester(initialFocusRequester)
                            } else {
                                Modifier
                            },
                            label = country.title,
                            selected = country.id in selectedCountryIds,
                            onClick = { onCountryChange(country.id) },
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun FilterMenuItem(
    modifier: Modifier = Modifier,
    label: String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(label) },
        supportingContent = {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        scale = ListItemDefaults.scale(focusedScale = 1.02f),
        selected = false,
        onClick = onClick,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun FilterOption(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(label) },
        scale = ListItemDefaults.scale(focusedScale = 1.02f),
        selected = selected,
        onClick = onClick,
        trailingContent = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

private data class FilterMenuEntry(
    val label: String,
    val summary: String,
    val page: FilterPage,
)

private fun FilterPage.toMainFilterFocusTarget(): MainFilterFocusTarget? = when (this) {
    FilterPage.CONTENT_TYPE -> MainFilterFocusTarget.CONTENT_TYPE
    FilterPage.SORT -> MainFilterFocusTarget.SORT
    FilterPage.PERIOD -> MainFilterFocusTarget.PERIOD
    FilterPage.GENRE -> MainFilterFocusTarget.GENRE
    FilterPage.COUNTRY -> MainFilterFocusTarget.COUNTRY
    FilterPage.MAIN -> null
}

private fun MainFilterFocusTarget.toFilterPage(): FilterPage = when (this) {
    MainFilterFocusTarget.CONTENT_TYPE -> FilterPage.CONTENT_TYPE
    MainFilterFocusTarget.SORT -> FilterPage.SORT
    MainFilterFocusTarget.PERIOD -> FilterPage.PERIOD
    MainFilterFocusTarget.GENRE -> FilterPage.GENRE
    MainFilterFocusTarget.COUNTRY -> FilterPage.COUNTRY
}
