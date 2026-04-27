package io.github.posaydone.kinopub.tv.ui.screen.showsGridScreen

import androidx.compose.runtime.Composable
import io.github.posaydone.kinopub.core.model.ShowList

@Composable
fun WatchingShowsGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    shows: ShowList,
    title: String,
    onShowFilterDialog: () -> Unit,
) {
    ShowsCollectionGridScreen(
        navigateToShowDetails = navigateToShowDetails,
        shows = shows,
        hasNextPage = false,
        onLoadNext = {},
        title = title,
        hasFilters = true,
        onShowFilterDialog = onShowFilterDialog,
    )
}
