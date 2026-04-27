package io.github.posaydone.kinopub.tv.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.model.ShowList
import io.github.posaydone.kinopub.tv.ui.common.CircularProgressIndicator
import io.github.posaydone.kinopub.tv.ui.common.ShowCard
import io.github.posaydone.kinopub.tv.ui.common.ShowCardBadge
import androidx.compose.ui.res.stringResource
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.tv.ui.common.primaryRating
import io.github.posaydone.kinopub.tv.ui.screen.homeScreen.rememberChildPadding
import java.util.Locale

@Composable
fun ShowsCollectionGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    shows: ShowList,
    hasNextPage: Boolean,
    onLoadNext: () -> Unit,
    title: String,
    hasFilters: Boolean,
    onShowFilterDialog: () -> Unit,
) {
    val childPadding = rememberChildPadding()
    val gridState = rememberLazyGridState()
    val horizontalPadding = 100.dp

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
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    Spacer(Modifier)
                }

                if (hasFilters) {
                    Button(onClick = onShowFilterDialog) {
                        Text(stringResource(R.string.filter_title))
                    }
                }
            }
        }

        items(shows, key = { it.id }) { show ->
            ShowCard(
                show = show,
                onClick = { navigateToShowDetails(show.id) },
                modifier = Modifier.fillMaxWidth(),
                badge = show.primaryRating()?.let { rating ->
                    {
                        ShowCardBadge(
                            text = String.format(Locale.US, "%.1f", rating),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                        )
                    }
                },
            )
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
        if (shouldLoadMore && hasNextPage) {
            onLoadNext()
        }
    }
}
