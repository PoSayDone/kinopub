package io.github.posaydone.kinopub.mobile.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.model.HistoryShow
import io.github.posaydone.kinopub.mobile.ui.common.HistoryCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val UnknownHistoryDateKey = "unknown"

@Composable
internal fun HistoryShowsGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    historyShows: List<HistoryShow>,
    hasNextPage: Boolean,
    onLoadNext: () -> Unit,
    paddingValues: PaddingValues,
) {
    val gridState = rememberLazyGridState()
    val locale = remember { Locale.getDefault() }
    val sections = remember(historyShows, locale) {
        historyShows.toHistorySections(locale)
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        sections.forEach { section ->
            item(
                key = "history-header-${section.key}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }

            items(
                items = section.items,
                key = { "${section.key}-${it.id}-${it.seasonNumber}-${it.episodeNumber}-${it.watchedAtSeconds}" },
            ) { show ->
                HistoryCard(
                    show = show,
                    onClick = { navigateToShowDetails(show.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (hasNextPage) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisible.index >= gridState.layoutInfo.totalItemsCount - 6
        }
    }

    LaunchedEffect(shouldLoadMore, hasNextPage) {
        if (shouldLoadMore && hasNextPage) {
            onLoadNext()
        }
    }
}

private data class HistorySection(
    val key: String,
    val title: String,
    val items: List<HistoryShow>,
)

private fun List<HistoryShow>.toHistorySections(locale: Locale): List<HistorySection> {
    val titleFormatter = SimpleDateFormat("d MMMM yyyy", locale)
    val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return groupBy { show ->
        show.watchedAtSeconds?.let { watchedAt ->
            keyFormatter.format(Date(watchedAt * 1000))
        } ?: UnknownHistoryDateKey
    }.map { (key, items) ->
        HistorySection(
            key = key,
            title = items.firstOrNull()?.watchedAtSeconds?.let { watchedAt ->
                titleFormatter.format(Date(watchedAt * 1000))
            } ?: "Без даты",
            items = items,
        )
    }
}
