@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.filmix.mobile.ui.screen.searchResults

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.SearchResultsViewModel
import io.github.posaydone.filmix.mobile.ui.common.ShowsGrid

@Composable
fun SearchResultsScreen(
    navigateBack: () -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit,
    viewModel: SearchResultsViewModel = hiltViewModel(),
) {
    val shows by viewModel.shows.collectAsStateWithLifecycle()
    

    Scaffold(
        topBar =
            {
                TopAppBar(
                    {},
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = {
                            navigateBack()
                        }) {
                            Icon(
                                contentDescription = stringResource(R.string.navback_icon),
                                painter = painterResource(R.drawable.ic_arrow_back)
                            )
                        }
                    })
            }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            ShowsGrid(shows, navigateToShowDetails)
        }
    }
}