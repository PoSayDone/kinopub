package io.github.posaydone.filmix.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.CatalogPeriod
import io.github.posaydone.filmix.core.common.sharedViewModel.CatalogSort
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.WatchingFilter
import io.github.posaydone.filmix.core.common.sharedViewModel.allContentTypes
import io.github.posaydone.filmix.core.model.kinopub.KinoPubCountry
import io.github.posaydone.filmix.core.model.kinopub.KinoPubGenre
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.res.stringResource
import io.github.posaydone.filmix.core.common.R

internal enum class FilterPage { MAIN, CONTENT_TYPE, SORT, PERIOD, GENRE, COUNTRY }

internal enum class MainFilterFocusTarget { CONTENT_TYPE, SORT, PERIOD, GENRE, COUNTRY }

@Composable
internal fun FilterDialogContent(
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

    val contentTypeLabel = stringResource(R.string.filter_content_type)
    val sortLabel = stringResource(R.string.filter_sort)
    val periodLabel = stringResource(R.string.filter_period)
    val genreLabel = stringResource(R.string.filter_genre)
    val countryLabel = stringResource(R.string.filter_country)
    val filterAll = stringResource(R.string.filter_all)
    val allGenres = stringResource(R.string.filter_all_genres)
    val allCountries = stringResource(R.string.filter_all_countries)
    val genreSelectedOne = stringResource(R.string.filter_genre_selected_one)
    val countrySelectedOne = stringResource(R.string.filter_country_selected_one)
    val selectedCount = stringResource(R.string.filter_selected_count)

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
                                label = contentTypeLabel,
                                summary = allContentTypes.find { it.apiValue == catalogContentType }?.label
                                    ?: filterAll,
                                page = FilterPage.CONTENT_TYPE,
                            )
                        )
                        add(
                            FilterMenuEntry(
                                label = sortLabel,
                                summary = catalogSort.label,
                                page = FilterPage.SORT,
                            )
                        )
                        if (showPeriod) {
                            add(
                                FilterMenuEntry(
                                    label = periodLabel,
                                    summary = catalogPeriod.label,
                                    page = FilterPage.PERIOD,
                                )
                            )
                        }
                        if (genres.isNotEmpty()) {
                            val genreSummary = when {
                                selectedGenreIds.isEmpty() -> filterAll
                                selectedGenreIds.size == 1 ->
                                    genres.firstOrNull { it.id in selectedGenreIds }?.title ?: genreSelectedOne

                                else -> String.format(selectedCount, selectedGenreIds.size)
                            }
                            add(
                                FilterMenuEntry(
                                    label = genreLabel,
                                    summary = genreSummary,
                                    page = FilterPage.GENRE,
                                )
                            )
                        }
                        if (countries.isNotEmpty()) {
                            val countrySummary = when {
                                selectedCountryIds.isEmpty() -> filterAll
                                selectedCountryIds.size == 1 ->
                                    countries.firstOrNull { it.id in selectedCountryIds }?.title ?: countrySelectedOne

                                else -> String.format(selectedCount, selectedCountryIds.size)
                            }
                            add(
                                FilterMenuEntry(
                                    label = countryLabel,
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
                            label = allGenres,
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
                            label = allCountries,
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

            else -> Unit
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

internal fun FilterPage.toMainFilterFocusTarget(): MainFilterFocusTarget? = when (this) {
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
