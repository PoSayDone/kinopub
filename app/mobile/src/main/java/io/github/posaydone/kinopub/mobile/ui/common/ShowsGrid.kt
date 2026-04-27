package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.model.Show

@Composable
fun ShowsGrid(
    shows: List<Show>,
    navigateToShowDetails: (showId: Int) -> Unit,
) {
    LazyVerticalGrid(
        GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(shows) { show ->
            ShowCard(show) {
                navigateToShowDetails(show.id)
            }
        }
    }
}