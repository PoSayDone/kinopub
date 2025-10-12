package io.github.posaydone.filmix.tv.ui.screen.exploreScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import io.github.posaydone.filmix.core.common.R
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.tv.ui.common.CircularProgressIndicator
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.common.TextField
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding

@Composable
fun ExploreScreen(
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: ExploreScreenViewModel = hiltViewModel(),
) {
    val focusRequester = remember { FocusRequester() }
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val currentSearchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val childPadding = rememberChildPadding()

    Column(
        modifier = Modifier
            .padding(top = childPadding.top, bottom = childPadding.bottom)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search_icon)
                )
            },
            contentPadding = PaddingValues(horizontal = 24.dp),
            value = currentSearchQuery,
            onValueChange = { updatedQuery ->
                viewModel.updateSearchQuery(updatedQuery)
            },
            modifier = Modifier
                .padding(vertical = 24.dp)
                .width(500.dp)
                .height(64.dp)
                .focusRequester(focusRequester),
            placeholderText = "${stringResource(R.string.search)}...",
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                autoCorrectEnabled = false, imeAction = ImeAction.Search
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = {
                    viewModel.updateSearchQuery(currentSearchQuery)
                })
        )

        when (val s = searchState) {
            is SearchState.Initial -> {
            }

            is SearchState.Searching -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SearchState.Done -> {
                val showList = s.showList
                if (showList.isEmpty()) {
                    // Show placeholder when nothing is found
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    ShowsRow(
                        title = "Search Results",
                        modifier = Modifier
                            .padding(horizontal = childPadding.start)
                            .fillMaxWidth(),
                        showList = showList,
                        onShowSelected = { show -> navigateToShowDetails(show.id) }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

