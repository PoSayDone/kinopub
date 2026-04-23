package io.github.posaydone.filmix.tv.ui.screen.homeScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ImmersiveContentUiState
import io.github.posaydone.filmix.core.model.KinopoiskCountry
import io.github.posaydone.filmix.core.model.KinopoiskGenre
import io.github.posaydone.filmix.core.model.Rating
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.Votes
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.ImmersiveBackground
import io.github.posaydone.filmix.tv.ui.common.ImmersiveDetails
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.common.gradientOverlay
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec
import io.github.posaydone.filmix.tv.ui.utils.Padding

val ParentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)

@Composable
fun rememberChildPadding(direction: LayoutDirection = LocalLayoutDirection.current): Padding {
    return remember {
        Padding(
            start = ParentPadding.calculateStartPadding(direction) + 8.dp,
            top = ParentPadding.calculateTopPadding(),
            end = ParentPadding.calculateEndPadding(direction) + 8.dp,
            bottom = ParentPadding.calculateBottomPadding()
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToShowDetails: (Int) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val immersiveContentState by viewModel.immersiveContentState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is HomeScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is HomeScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry)
        }

        is HomeScreenUiState.Done -> {
            Body(
                modifier = Modifier.fillMaxSize(),
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
                immersiveState = immersiveContentState,
                onImmersiveShowFocused = viewModel::onImmersiveShowFocused,
                navigateToShowDetails = navigateToShowDetails,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Body(
    modifier: Modifier = Modifier,
    lastSeenShows: ShowList,
    popularMovies: ShowList,
    newMovies: ShowList,
    popularSeries: ShowList,
    newSeries: ShowList,
    newConcerts: ShowList,
    new3d: ShowList,
    newDocumentaryFilms: ShowList,
    newDocumentarySeries: ShowList,
    newTvShows: ShowList,
    immersiveState: ImmersiveContentUiState,
    onImmersiveShowFocused: (Show) -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit,
) {
    val lazyColumnState = rememberLazyListState()
    val verticalBivs = remember { CustomBringIntoViewSpec(0.9f, 1.0f) }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val backdropHeight = screenHeightDp.dp - 32.dp
    val childPadding = rememberChildPadding()
    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    val immersiveHeightFraction = remember(screenHeightDp) {
        val spacerHeight = screenHeightDp - 324  
        val clipDp = spacerHeight + 56          
        (clipDp.toFloat() / screenHeightDp).coerceIn(0.5f, 0.85f)
    }

    val content = immersiveState as? ImmersiveContentUiState.Content

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalBringIntoViewSpec provides verticalBivs) {
            LazyColumn(
                modifier = Modifier
                    .focusRequester(lazyColumn)
                    .focusRestorer(firstItem),
                state = lazyColumnState,
                contentPadding = PaddingValues(bottom = 108.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.height(backdropHeight - 246.dp))
                }
                item(contentType = "LastSeenRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .focusRequester(firstItem),
                        showItemTitle = false,
                        showList = lastSeenShows,
                        title = stringResource(R.string.continue_watching),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "PopularMoviesRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = popularMovies,
                        title = stringResource(R.string.popular_movies),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "NewMoviesRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = newMovies,
                        title = stringResource(R.string.new_movies),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "PopularSeriesRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = popularSeries,
                        title = stringResource(R.string.popular_series),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "NewSeriesRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = newSeries,
                        title = stringResource(R.string.new_series),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "NewConcertsRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = newConcerts,
                        title = stringResource(R.string.new_concerts),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "New3dRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = new3d,
                        title = stringResource(R.string.new_3d),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "NewDocumentaryFilmsRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = newDocumentaryFilms,
                        title = stringResource(R.string.new_documentary_films),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "NewDocumentarySeriesRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = newDocumentarySeries,
                        title = stringResource(R.string.new_documentary_series),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }

                item(contentType = "NewTvShowsRow") {
                    ShowsRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        showItemTitle = false,
                        showList = newTvShows,
                        title = stringResource(R.string.new_tv_shows),
                        onShowSelected = { show ->
                            lazyColumn.saveFocusedChild()
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { onImmersiveShowFocused(it) }
                    )
                }
            }
        }

        // z=2 — immersive zone, clipped to the top portion of the screen.
        // Always renders (solid surface when no backdrop loaded) so card rows are never
        // visible in this area. Backdrop fades in via Crossfade once Content arrives.
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxSize(immersiveHeightFraction)
                .clipToBounds()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ImmersiveBackground(imageUrl = content?.fullShow?.backdropUrl)

            // Bottom-edge fade: backdrop blends into surface color over the lower 45% of the zone.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.55f to Color.Transparent,
                            0.82f to MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            1.0f to MaterialTheme.colorScheme.surface,
                        )
                    )
                )
            )
            Box(Modifier.fillMaxSize().gradientOverlay(MaterialTheme.colorScheme.surface))
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            (immersiveHeightFraction - 0.03f) to Color.Transparent,
                            immersiveHeightFraction to MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                            (immersiveHeightFraction + 0.13f) to Color.Transparent,
                            1f to Color.Transparent,
                        )
                    )
                )
        )

        // z=3 — title / metadata, only shown once full Content is available.
        if (content != null) {
            ImmersiveDetails(
                modifier = Modifier
                    .padding(start = childPadding.start, top = childPadding.top + 24.dp)
                    .fillMaxWidth(),
                logoUrl = content.fullShow.logoUrl,
                title = content.fullShow.title,
                description = content.fullShow.description ?: content.fullShow.shortDescription,
                rating = Rating(
                    kp = content.fullShow.ratingKp ?: 0.0,
                    imdb = content.fullShow.ratingImdb ?: 0.0,
                    filmCritics = 0.0,
                    russianFilmCritics = 0.0,
                    await = 0.0,
                ),
                votes = Votes(
                    kp = content.fullShow.votesKp ?: 0,
                    imdb = content.fullShow.votesImdb ?: 0,
                    filmCritics = 0,
                    russianFilmCritics = 0,
                    await = 0,
                ),
                genres = content.fullShow.genres.map { KinopoiskGenre(name = it) },
                countries = content.fullShow.countries.map { KinopoiskCountry(name = it) },
                year = content.fullShow.year,
                seriesLength = content.fullShow.seriesLength,
                movieLength = content.fullShow.movieLength,
                ageRating = content.fullShow.ageRating?.toString() ?: "",
            )
        }
    }
}
