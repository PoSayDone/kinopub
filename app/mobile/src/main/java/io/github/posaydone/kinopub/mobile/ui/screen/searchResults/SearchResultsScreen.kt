@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.kinopub.mobile.ui.screen.searchResults

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.SearchResultsViewModel
import io.github.posaydone.kinopub.mobile.ui.common.EmptyState
import io.github.posaydone.kinopub.mobile.ui.common.ShowCard

@Composable
fun SearchResultsScreen(
    navigateBack: () -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: SearchResultsViewModel = hiltViewModel(),
) {
    val shows by viewModel.shows.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val query = viewModel.navKey.query

    Scaffold(
        topBar = {
            TopAppBar(
                {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            contentDescription = stringResource(R.string.navback_icon),
                            painter = painterResource(R.drawable.ic_arrow_back)
                        )
                    }
                })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    EmptyState(
                        title = stringResource(R.string.search_error_title),
                        subtitle = stringResource(R.string.search_error_subtitle),
                    )
                }

                shows.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.search_no_results, query),
                        subtitle = stringResource(R.string.search_no_results_subtitle),
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(shows, key = { it.id }) { show ->
                            ShowCard(
                                show = show,
                                onClick = { navigateToShowDetails(show.id) },
                                showOriginalTitle = true,
                                showYear = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
