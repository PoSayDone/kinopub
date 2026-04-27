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
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.HistoryShowsRow
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
    navigateToMoviePlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
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
                    featuredShowProgress = s.featuredShowProgress,
                    lastSeenShows = s.lastSeenShows,
                    popularMovies = s.popularMovies,
                    newMovies = s.newMovies,
                    popularSeries = s.popularSeries,
                    newSeries = s.newSeries,
                    newConcerts = s.newConcerts,
                    new3d = s.new3d,
                    newDocumentaryFilms = s.newDocumentaryFilms,
                    newDocumentarySeries = s.newDocumentarySeries,
                    newTvShows = s.newTvShows,
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
    featuredShow: Show,
    featuredShowProgress: ShowProgress,
    lastSeenShows: List<HistoryShow>,
    popularMovies: ShowList,
    newMovies: ShowList,
    popularSeries: ShowList,
    newSeries: ShowList,
    newConcerts: ShowList,
    new3d: ShowList,
    newDocumentaryFilms: ShowList,
    newDocumentarySeries: ShowList,
    newTvShows: ShowList,
    navigateToMoviePlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
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
                featuredShow = featuredShow,
                featuredShowProgress = featuredShowProgress,
                navigateToMoviePlayer = navigateToMoviePlayer,
            ) { }
            HistoryShowsRow(
                historyList = lastSeenShows,
                title = stringResource(R.string.continue_watching),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = popularMovies,
                title = stringResource(R.string.popular_movies),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = newMovies,
                title = stringResource(R.string.new_movies),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = popularSeries,
                title = stringResource(R.string.popular_series),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = newSeries,
                title = stringResource(R.string.new_series),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = newConcerts,
                title = stringResource(R.string.new_concerts),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = new3d,
                title = stringResource(R.string.new_3d),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = newDocumentaryFilms,
                title = stringResource(R.string.new_documentary_films),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = newDocumentarySeries,
                title = stringResource(R.string.new_documentary_series),
                onShowClick = { show -> navigateToShowDetails(show.id) })
            ShowsRow(
                showList = newTvShows,
                title = stringResource(R.string.new_tv_shows),
                onShowClick = { show -> navigateToShowDetails(show.id) })
        }
    }
}
