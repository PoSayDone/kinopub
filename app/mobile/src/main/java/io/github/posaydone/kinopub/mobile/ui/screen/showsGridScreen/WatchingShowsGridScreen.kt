package io.github.posaydone.kinopub.mobile.ui.screen.showsGridScreen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import io.github.posaydone.kinopub.core.model.ShowList

@Composable
internal fun WatchingShowsGridScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    shows: ShowList,
    paddingValues: PaddingValues,
) {
    ShowsCollectionGridScreen(
        navigateToShowDetails = navigateToShowDetails,
        shows = shows,
        hasNextPage = false,
        onLoadNext = {},
        paddingValues = paddingValues,
    )
}
