package io.github.posaydone.filmix.tv.ui.screen.homeScreen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import coil.request.Tags
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ImmersiveContentUiState
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.common.BackdropMask
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.ImmersiveBackground
import io.github.posaydone.filmix.tv.ui.common.ImmersiveDetails
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.common.gradientOverlay
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec
import io.github.posaydone.filmix.tv.ui.utils.Padding

val ParentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)

private val TAG = "HOME"

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
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(),
                lastSeenShows = s.lastSeenShows,
                viewingShows = s.viewingShows,
                popularMovies = s.popularMovies,
                popularSeries = s.popularSeries,
                popularCartoons = s.popularCartoons,
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
    viewingShows: ShowList,
    popularMovies: ShowList,
    popularSeries: ShowList,
    popularCartoons: ShowList,
    immersiveState: ImmersiveContentUiState,
    onImmersiveShowFocused: (Show) -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit,
) {
    val lazyColumnState = rememberLazyListState()
    val verticalBivs = remember { CustomBringIntoViewSpec(0.9f, 1.0f) }

    val backdropHeight = LocalConfiguration.current.run { screenHeightDp.dp } - 32.dp
    val childPadding = rememberChildPadding()

    AnimatedVisibility(
        visible = true, enter = fadeIn(), exit = fadeOut()
    ) {
        if (immersiveState is ImmersiveContentUiState.Content) {
            ImmersiveBackground(
                imageUrl = immersiveState.fullShow.backdropUrl
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .gradientOverlay(MaterialTheme.colorScheme.surface)
            )

            ImmersiveDetails(
                modifier = Modifier
                    .padding(
                        start = childPadding.start, top = childPadding.top + 24.dp
                    )
                    .fillMaxWidth(),
                logoUrl = immersiveState.fullShow.logoUrl,
                title = immersiveState.fullShow.title,
                description = immersiveState.fullShow.description
                    ?: immersiveState.fullShow.shortDescription,
                rating = io.github.posaydone.filmix.core.model.Rating(
                    kp = immersiveState.fullShow.ratingKp ?: 0.0,
                    imdb = immersiveState.fullShow.ratingImdb ?: 0.0,
                    filmCritics = 0.0,
                    russianFilmCritics = 0.0,
                    await = 0.0
                ),
                votes = io.github.posaydone.filmix.core.model.Votes(
                    kp = immersiveState.fullShow.votesKp ?: 0,
                    imdb = immersiveState.fullShow.votesImdb ?: 0,
                    filmCritics = 0,
                    russianFilmCritics = 0,
                    await = 0
                ),
                genres = immersiveState.fullShow.genres.map {
                    io.github.posaydone.filmix.core.model.KinopoiskGenre(
                        name = it
                    )
                },
                countries = immersiveState.fullShow.countries.map {
                    io.github.posaydone.filmix.core.model.KinopoiskCountry(
                        name = it
                    )
                },
                year = immersiveState.fullShow.year,
                seriesLength = immersiveState.fullShow.seriesLength,
                movieLength = immersiveState.fullShow.movieLength,
                ageRating = immersiveState.fullShow.ageRating.toString()
            )
        } else if (immersiveState is ImmersiveContentUiState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {}
        }
    }

    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    CompositionLocalProvider(LocalBringIntoViewSpec provides verticalBivs) {
        LazyColumn(
            modifier = Modifier
                .focusRequester(lazyColumn)
                .focusRestorer(firstItem)
                .focusProperties {
                    onEnter = {
                        Log.d(TAG, "Enter focus event in home column")
                    }
                    onExit = {
                        Log.d(TAG, "Exit focus event in home column")
                    }
                }
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(0.48f to Color.Transparent, 0.5f to Color.Black),
                        ), blendMode = BlendMode.DstIn
                    )
                },
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
                    onShowFocused = {
                        onImmersiveShowFocused(it)
                    }
                )
            }

            item(contentType = "ViewingRow") {
                ShowsRow(
                    modifier = Modifier.padding(bottom = 16.dp),
                    showItemTitle = false,
                    showList = viewingShows,
                    title = stringResource(R.string.watching_now),
                    onShowSelected = { show ->
                        lazyColumn.saveFocusedChild()
                        navigateToShowDetails(show.id)
                    },
                    onShowFocused = {
                        onImmersiveShowFocused(it)
                    }
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
                    onShowFocused = {
                        onImmersiveShowFocused(it)
                    }
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
                    onShowFocused = {
                        onImmersiveShowFocused(it)
                    }
                )
            }

            item(contentType = "PopularCartoonsRow") {
                ShowsRow(
                    modifier = Modifier.padding(bottom = 16.dp),
                    showItemTitle = false,
                    showList = popularCartoons,
                    title = stringResource(R.string.popular_cartoons),
                    onShowSelected = { show ->
                        lazyColumn.saveFocusedChild()
                        navigateToShowDetails(show.id)
                    },
                    onShowFocused = {
                        onImmersiveShowFocused(it)
                    }
                )
            }
        }
    }
}
