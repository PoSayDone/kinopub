@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.kinopub.mobile.ui.screen.showsGridScreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.CatalogPeriod
import io.github.posaydone.kinopub.core.common.sharedViewModel.CatalogSort
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.kinopub.core.common.sharedViewModel.WatchingFilter
import io.github.posaydone.kinopub.core.common.sharedViewModel.allContentTypes
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubCountry
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubGenre

internal enum class FilterPage { MAIN, CONTENT_TYPE, SORT, PERIOD, GENRE, COUNTRY }

@Composable
internal fun FilterBottomSheet(
    showSheet: Boolean,
    queryType: ShowsGridQueryType,
    filterPage: FilterPage,
    onFilterPageChange: (FilterPage) -> Unit,
    onDismiss: () -> Unit,
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
    if (!showSheet) return

    val isNestedFilterPage = filterPage != FilterPage.MAIN

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = !isNestedFilterPage,
        ),
    ) {
        BackHandler(enabled = isNestedFilterPage) {
            onFilterPageChange(FilterPage.MAIN)
        }

        AnimatedContent(
            targetState = filterPage,
            transitionSpec = {
                if (targetState != FilterPage.MAIN) {
                    (slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()) togetherWith
                            (slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeOut())
                } else {
                    (slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()) togetherWith
                            (slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeOut())
                }
            },
            label = "FilterPageTransition"
        ) { targetPage ->
            val sheetTitle = when (targetPage) {
                FilterPage.MAIN -> stringResource(R.string.filter_title)
                FilterPage.CONTENT_TYPE -> stringResource(R.string.filter_content_type)
                FilterPage.SORT -> stringResource(R.string.filter_sort)
                FilterPage.PERIOD -> stringResource(R.string.filter_period)
                FilterPage.GENRE -> stringResource(R.string.filter_genre)
                FilterPage.COUNTRY -> stringResource(R.string.filter_country)
            }
            val filterAll = stringResource(R.string.filter_all)
            val allGenres = stringResource(R.string.filter_all_genres)
            val allCountries = stringResource(R.string.filter_all_countries)
            val genreSelectedOne = stringResource(R.string.filter_genre_selected_one)
            val countrySelectedOne = stringResource(R.string.filter_country_selected_one)
            val selectedCount = stringResource(R.string.filter_selected_count)
            val contentTypeLabel = stringResource(R.string.filter_content_type)
            val sortLabel = stringResource(R.string.filter_sort)
            val periodLabel = stringResource(R.string.filter_period)
            val genreLabel = stringResource(R.string.filter_genre)
            val countryLabel = stringResource(R.string.filter_country)

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    if (targetPage != FilterPage.MAIN) {
                        IconButton(onClick = { onFilterPageChange(FilterPage.MAIN) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    }
                    Text(
                        text = sheetTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(
                            start = if (targetPage == FilterPage.MAIN) 12.dp else 4.dp,
                        ),
                    )
                }

                val showPeriod = catalogSort == CatalogSort.WATCHERS || catalogSort == CatalogSort.VIEWS

                LazyColumn {
                    when (queryType) {
                        ShowsGridQueryType.WATCHING -> {
                            items(WatchingFilter.entries) { filter ->
                                FilterOption(
                                    label = filter.label,
                                    selected = filter == watchingFilter,
                                    onClick = { onWatchingFilterChange(filter) },
                                )
                            }
                        }

                        ShowsGridQueryType.CATALOG -> when (targetPage) {
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
                                                genres.firstOrNull { it.id in selectedGenreIds }?.title
                                                    ?: genreSelectedOne

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
                                                countries.firstOrNull { it.id in selectedCountryIds }?.title
                                                    ?: countrySelectedOne

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
                                items(menuItems, key = { it.page.name }) { entry ->
                                    FilterMenuItem(
                                        label = entry.label,
                                        summary = entry.summary,
                                        onClick = { onFilterPageChange(entry.page) },
                                    )
                                }
                            }

                            FilterPage.CONTENT_TYPE -> {
                                items(allContentTypes) { option ->
                                    FilterOption(
                                        label = option.label,
                                        selected = option.apiValue == catalogContentType,
                                        onClick = { onCatalogContentTypeChange(option.apiValue) },
                                    )
                                }
                            }

                            FilterPage.SORT -> {
                                items(CatalogSort.entries) { sort ->
                                    FilterOption(
                                        label = sort.label,
                                        selected = sort == catalogSort,
                                        onClick = { onCatalogSortChange(sort) },
                                    )
                                }
                            }

                            FilterPage.PERIOD -> {
                                items(CatalogPeriod.entries) { period ->
                                    FilterOption(
                                        label = period.label,
                                        selected = period == catalogPeriod,
                                        onClick = { onCatalogPeriodChange(period) },
                                    )
                                }
                            }

                            FilterPage.GENRE -> {
                                item {
                                    FilterOption(
                                        label = allGenres,
                                        selected = selectedGenreIds.isEmpty(),
                                        onClick = { onGenreChange(null) },
                                    )
                                }
                                items(genres, key = { it.id }) { genre ->
                                    FilterOption(
                                        label = genre.title,
                                        selected = genre.id in selectedGenreIds,
                                        onClick = { onGenreChange(genre.id) },
                                    )
                                }
                            }

                            FilterPage.COUNTRY -> {
                                item {
                                    FilterOption(
                                        label = allCountries,
                                        selected = selectedCountryIds.isEmpty(),
                                        onClick = { onCountryChange(null) },
                                    )
                                }
                                items(countries, key = { it.id }) { country ->
                                    FilterOption(
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
        }
    }
}

@Composable
private fun FilterMenuItem(
    label: String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        headlineContent = { Text(label) },
        supportingContent = {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        modifier = Modifier
            .clickable(onClick = onClick),
    )
}

@Composable
private fun FilterOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        headlineContent = { Text(label) },
        trailingContent = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        modifier = Modifier
            .clickable(onClick = onClick),
    )
}

private data class FilterMenuEntry(
    val label: String,
    val summary: String,
    val page: FilterPage,
)
