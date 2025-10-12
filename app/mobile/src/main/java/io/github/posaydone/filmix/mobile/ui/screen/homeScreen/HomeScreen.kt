package io.github.posaydone.filmix.mobile.ui.screen.homeScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenViewModel
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.Loading
import io.github.posaydone.filmix.mobile.ui.common.ShowsRow
import io.github.posaydone.filmix.mobile.ui.screen.homeScreen.components.HomeBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToMoviePlayer: (showId: Int) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(
            NavigationBarDefaults.windowInsets.union(WindowInsets.statusBars)
        )
    ) { paddingValues ->
        when (val s = uiState) {
            is HomeScreenUiState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            is HomeScreenUiState.Error -> {
                Column {
                    Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry, children = {
                        Button(onClick = {
                            s.sessionManager.saveAccessToken(
                                s.sessionManager.fetchAccessToken(),
                                System.currentTimeMillis() - 1000
                            )
                        }) {
                            Text(stringResource(R.string.clear_expiration_time))
                        }
                        Button(onClick = {
                            s.sessionManager.saveAccessToken(
                                null, System.currentTimeMillis() - 1000
                            )
                        }) {
                            Text(stringResource(R.string.remove_token))
                        }
                        Button(onClick = {
                            s.sessionManager.saveAccessToken(
                                "adsfjskjdfkaksjf", System.currentTimeMillis() + 10 * 60 * 1000
                            )
                        }) {
                            Text(stringResource(R.string.save_wrong_token))
                        }
                    })
                }
            }

            is HomeScreenUiState.Done -> {
                Body(
                    modifier = modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .padding(paddingValues),
                    featuredShow = s.featuredShow,
                    lastSeenShows = s.lastSeenShows,
                    viewingShows = s.viewingShows,
                    popularMovies = s.popularMovies,
                    popularSeries = s.popularSeries,
                    popularCartoons = s.popularCartoons,
                    navigateToMoviePlayer = navigateToMoviePlayer,
                    navigateToShowDetails = navigateToShowDetails,
                    reload = { viewModel.retry() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Body(
    modifier: Modifier = Modifier,
    featuredShow: io.github.posaydone.filmix.core.model.FullShow,
    lastSeenShows: ShowList,
    viewingShows: ShowList,
    popularMovies: ShowList,
    popularSeries: ShowList,
    popularCartoons: ShowList,
    navigateToMoviePlayer: (showId: Int) -> Unit,
    navigateToShowDetails: (Int) -> Unit,
    reload: () -> Unit,
) {
    val refreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(state = refreshState, isRefreshing = isRefreshing, onRefresh = {
        coroutineScope.launch {
            isRefreshing = true
            reload()
            delay(1.seconds)
            isRefreshing = false
        }
    }) {

        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeBanner(
                featuredShow = featuredShow, navigateToMoviePlayer = navigateToMoviePlayer
            ) { }
            ShowsRow(
                showList = lastSeenShows,
                title = stringResource(R.string.continue_watching),
                onShowClick = { show ->
                    navigateToShowDetails(show.id)
                })
            ShowsRow(
                showList = viewingShows,
                title = stringResource(R.string.watching_now),
                onShowClick = { show ->
                    navigateToShowDetails(show.id)
                })
            ShowsRow(
                showList = popularMovies,
                title = stringResource(R.string.popular_movies),
                onShowClick = { show ->
                    navigateToShowDetails(show.id)
                })
            ShowsRow(
                showList = popularSeries,
                title = stringResource(R.string.popular_series),
                onShowClick = { show ->
                    navigateToShowDetails(show.id)
                })
            ShowsRow(
                showList = popularCartoons,
                title = stringResource(R.string.popular_cartoons),
                onShowClick = { show ->
                    navigateToShowDetails(show.id)
                })
        }
    }
}

