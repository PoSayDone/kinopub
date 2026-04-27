package io.github.posaydone.kinopub.mobile.ui.screen.exploreScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.mobile.ui.common.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navigateToSearchResultsScreen: (query: String) -> Unit,
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
        topBar = {
            ExploreSearchBar { navigateToSearchResultsScreen(it) }
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                title = stringResource(R.string.explore_empty_title),
                subtitle = stringResource(R.string.explore_empty_subtitle),
            )
        }
    }
}
