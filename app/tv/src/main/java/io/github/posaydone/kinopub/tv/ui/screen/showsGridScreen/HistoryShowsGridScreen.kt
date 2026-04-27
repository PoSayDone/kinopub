package io.github.posaydone.kinopub.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.model.HistoryShow
import io.github.posaydone.kinopub.tv.ui.common.CardTitleMode
import io.github.posaydone.kinopub.tv.ui.common.CircularProgressIndicator
import io.github.posaydone.kinopub.tv.ui.common.HistoryShowCard
import io.github.posaydone.kinopub.tv.ui.screen.homeScreen.rememberChildPadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val UnknownHistoryDateKey = "unknown"
private val HistoryGridMinWidth = 220.dp

@Composable
fun HistoryShowsGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    historyShows: List<HistoryShow>,
    hasNextPage: Boolean,
    onLoadNext: () -> Unit,
    title: String,
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()
    val horizontalPadding = 100.dp
    val locale = remember { Locale.getDefault() }
    val sections = remember(historyShows, locale) {
        historyShows.toHistorySections(locale)
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(HistoryGridMinWidth),
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
            Text(
                text = title,
                modifier = Modifier.padding(vertical = 16.dp),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        sections.forEach { section ->
            item(
                key = "history-header-${section.key}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = section.title,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            items(
                items = section.items,
                key = { "${section.key}-${it.id}-${it.seasonNumber}-${it.episodeNumber}-${it.watchedAtSeconds}" },
            ) { show ->
                HistoryShowCard(
                    show = show,
                    onClick = { navigateToShowDetails(show.id) },
                    modifier = Modifier.fillMaxWidth(),
                    titleMode = CardTitleMode.ALWAYS,
                )
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
