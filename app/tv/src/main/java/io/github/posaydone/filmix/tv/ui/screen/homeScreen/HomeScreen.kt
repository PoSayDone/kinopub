package io.github.posaydone.filmix.tv.ui.screen.homeScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.HomeScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.KinopoiskCountry
import io.github.posaydone.filmix.core.model.toShow
import io.github.posaydone.filmix.core.model.kinopub.KinoPubContentType
import io.github.posaydone.filmix.core.model.kinopub.KinoPubPeriod
import io.github.posaydone.filmix.core.model.kinopub.KinoPubSort
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.core.model.KinopoiskGenre
import io.github.posaydone.filmix.core.model.Rating
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.ShowStatus
import io.github.posaydone.filmix.core.model.Votes
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.HistoryShowsRow
import io.github.posaydone.filmix.tv.ui.common.ImmersiveBackground
import io.github.posaydone.filmix.tv.ui.common.ImmersiveDetails
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.ShowsRow
import io.github.posaydone.filmix.tv.ui.theme.KinopubTheme
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
    navigateToPlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit = { _, _, _ -> },
    navigateToShowsGrid: (MainGraphData.ShowsGrid) -> Unit = {},
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showImmersiveBackground by viewModel.showImmersiveBackground.collectAsStateWithLifecycle()
    val showImmersiveGradient by viewModel.showImmersiveGradient.collectAsStateWithLifecycle()
    val showImmersiveDetails by viewModel.showImmersiveDetails.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState is HomeScreenUiState.Done) {
            viewModel.retry()
        }
    }

    when (val s = uiState) {
        is HomeScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry)
        is HomeScreenUiState.Done -> Body(
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
            showImmersiveBackground = showImmersiveBackground,
            showImmersiveGradient = showImmersiveGradient,
            showImmersiveDetails = showImmersiveDetails,
            navigateToShowDetails = navigateToShowDetails,
            navigateToPlayer = navigateToPlayer,
            navigateToShowsGrid = navigateToShowsGrid,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    val previewShowList = remember {
        listOf(
            previewShow(id = 1, title = "The Last Horizon", year = 2024),
            previewShow(id = 2, title = "Northline", year = 2023),
            previewShow(id = 3, title = "Static Echo", year = 2022),
            previewShow(id = 4, title = "Glass Harbor", year = 2021),
            previewShow(id = 5, title = "Satellite City", year = 2020),
        )
    }

    KinopubTheme()
    {
        Body(
            modifier = Modifier
                .fillMaxSize(),
            lastSeenShows = emptyList(),
            popularMovies = previewShowList,
            newMovies = previewShowList,
            popularSeries = previewShowList,
            newSeries = previewShowList,
            newConcerts = previewShowList,
            new3d = previewShowList,
            newDocumentaryFilms = previewShowList,
            newDocumentarySeries = previewShowList,
            newTvShows = previewShowList,
            showImmersiveBackground = true,
            showImmersiveGradient = true,
            showImmersiveDetails = true,
            navigateToShowDetails = {},
            navigateToPlayer = { _, _, _ -> },
        )
    }
}

private fun previewShow(
    id: Int,
    title: String,
    year: Int,
) = Show(
    id = id,
    title = title,
    originalTitle = "",
    poster = "",
    year = year,
    quality = "4K",
    status = ShowStatus(status_text = "Released"),
    votesNeg = 24,
    votesPos = 742,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Body(
    modifier: Modifier = Modifier,
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
    showImmersiveBackground: Boolean,
    showImmersiveGradient: Boolean,
    showImmersiveDetails: Boolean,
    navigateToShowDetails: (showId: Int) -> Unit,
    navigateToPlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
    navigateToShowsGrid: (MainGraphData.ShowsGrid) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val debounceJob = remember { arrayOfNulls<Job>(1) }
    val availableShows = remember(
        lastSeenShows,
        popularMovies,
        newMovies,
        popularSeries,
        newSeries,
        newConcerts,
        new3d,
        newDocumentaryFilms,
        newDocumentarySeries,
        newTvShows,
    ) {
        buildList {
            addAll(lastSeenShows.map(HistoryShow::toShow))
            addAll(popularMovies)
            addAll(newMovies)
            addAll(popularSeries)
            addAll(newSeries)
            addAll(newConcerts)
            addAll(new3d)
            addAll(newDocumentaryFilms)
            addAll(newDocumentarySeries)
            addAll(newTvShows)
        }
    }
    val fallbackFocusedShow = availableShows.firstOrNull()
    var focusedShowId by rememberSaveable { mutableStateOf(fallbackFocusedShow?.id) }
    val focusedShow = remember(availableShows, fallbackFocusedShow, focusedShowId) {
        availableShows.firstOrNull { it.id == focusedShowId } ?: fallbackFocusedShow
    }
    val lazyColumnState = rememberLazyListState()
    val verticalBivs = remember { CustomBringIntoViewSpec(0.9f, 1.0f) }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val backdropHeight = screenHeightDp.dp - 32.dp
    val childPadding = rememberChildPadding()
    val lazyColumn = remember { FocusRequester() }
    val restoredRow = remember { FocusRequester() }
    var lastFocusedRowKey by rememberSaveable { mutableStateOf("LastSeenRow") }

    LaunchedEffect(Unit) {
        runCatching { lazyColumn.requestFocus() }
    }

    val immersiveBackdropUrl = focusedShow?.backdropUrl

    val immersiveHeightFraction = remember(screenHeightDp) {
        val spacerHeight = screenHeightDp - 324
        val clipDp = spacerHeight + 56
        (clipDp.toFloat() / screenHeightDp).coerceIn(0.5f, 0.85f)
    }

    val rowsClipShape = remember(immersiveHeightFraction) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ) =
                Outline.Rectangle(
                    Rect(0f, size.height * immersiveHeightFraction, size.width, size.height)
                )
        }
    }

    val hasImmersiveArea = showImmersiveBackground || showImmersiveDetails
    val rowsModifier = Modifier
        .focusRequester(lazyColumn)
        .focusProperties { onEnter = { runCatching { restoredRow.requestFocus() } } }
        .let { baseModifier ->
            if (hasImmersiveArea) {
                baseModifier.clip(rowsClipShape)
            } else {
                baseModifier
            }
        }

    val onShowFocused: (Show) -> Unit = { show ->
        lazyColumn.saveFocusedChild()
        if (show.id != focusedShowId) {
            debounceJob[0]?.cancel()
            debounceJob[0] = scope.launch {
                delay(300L)
                focusedShowId = show.id
            }
        }
    }


    Box(modifier = modifier) {
        Crossfade(
            targetState = if (showImmersiveBackground) immersiveBackdropUrl else null,
            animationSpec = tween(durationMillis = 400),
            label = "ImmersiveBackground",
        ) { backdropUrl ->
            if (backdropUrl != null) {
                ImmersiveBackground(imageUrl = backdropUrl)
            }
        }


        CompositionLocalProvider(LocalBringIntoViewSpec provides verticalBivs) {
            LazyColumn(
                modifier = rowsModifier,
                state = lazyColumnState,
                contentPadding = PaddingValues(bottom = 108.dp),
            ) {
                if (hasImmersiveArea) {
                    item {
                        Spacer(modifier = Modifier.height((backdropHeight - 246.dp).coerceAtLeast(0.dp)))
                    }
                }
                item(contentType = "LastSeenRow") {
                    HistoryShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "LastSeenRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "LastSeenRow" },
                        historyList = lastSeenShows,
                        showItemTitle = false,
                        showItemOriginalTitle = false,
                        showItemYear = false,
                        title = stringResource(R.string.continue_watching),
                        onShowSelected = { show ->
                            navigateToPlayer(
                                show.id,
                                show.seasonNumber ?: -1,
                                show.episodeNumber ?: -1
                            )
                        },
                        onShowFocused = { show -> onShowFocused(show.toShow()) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.HISTORY.name,
                                    title = "История просмотра"
                                )
                            )
                        },
                    )
                }

                item(contentType = "PopularMoviesRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "PopularMoviesRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "PopularMoviesRow" },
                        showItemTitle = false,
                        showList = popularMovies,
                        title = stringResource(R.string.popular_movies),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Популярные фильмы",
                                    KinoPubContentType.MOVIE,
                                    KinoPubSort.VIEWS,
                                    KinoPubPeriod.MONTH
                                )
                            )
                        },
                    )
                }

                item(contentType = "NewMoviesRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "NewMoviesRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "NewMoviesRow" },
                        showItemTitle = false,
                        showList = newMovies,
                        title = stringResource(R.string.new_movies),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Новые фильмы",
                                    KinoPubContentType.MOVIE,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }

                item(contentType = "PopularSeriesRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "PopularSeriesRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "PopularSeriesRow" },
                        showItemTitle = false,
                        showList = popularSeries,
                        title = stringResource(R.string.popular_series),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Популярные сериалы",
                                    KinoPubContentType.SERIAL,
                                    KinoPubSort.WATCHERS,
                                    KinoPubPeriod.THREE_MONTHS
                                )
                            )
                        },
                    )
                }

                item(contentType = "NewSeriesRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "NewSeriesRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "NewSeriesRow" },
                        showItemTitle = false,
                        showList = newSeries,
                        title = stringResource(R.string.new_series),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Новые сериалы",
                                    KinoPubContentType.SERIAL,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }

                item(contentType = "NewConcertsRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "NewConcertsRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "NewConcertsRow" },
                        showItemTitle = false,
                        showList = newConcerts,
                        title = stringResource(R.string.new_concerts),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Концерты",
                                    KinoPubContentType.CONCERT,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }

                item(contentType = "New3dRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "New3dRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "New3dRow" },
                        showItemTitle = false,
                        showList = new3d,
                        title = stringResource(R.string.new_3d),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "3D фильмы",
                                    KinoPubContentType.FILM_3D,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }

                item(contentType = "NewDocumentaryFilmsRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "NewDocumentaryFilmsRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "NewDocumentaryFilmsRow" },
                        showItemTitle = false,
                        showList = newDocumentaryFilms,
                        title = stringResource(R.string.new_documentary_films),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Документальные фильмы",
                                    KinoPubContentType.DOCUMOVIE,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }

                item(contentType = "NewDocumentarySeriesRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "NewDocumentarySeriesRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "NewDocumentarySeriesRow" },
                        showItemTitle = false,
                        showList = newDocumentarySeries,
                        title = stringResource(R.string.new_documentary_series),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "Документальные сериалы",
                                    KinoPubContentType.DOCUSERIAL,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }

                item(contentType = "NewTvShowsRow") {
                    ShowsRow(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .then(if (lastFocusedRowKey == "NewTvShowsRow") Modifier.focusRequester(restoredRow) else Modifier)
                            .onFocusChanged { if (it.hasFocus) lastFocusedRowKey = "NewTvShowsRow" },
                        showItemTitle = false,
                        showList = newTvShows,
                        title = stringResource(R.string.new_tv_shows),
                        onShowSelected = { show ->
                            navigateToShowDetails(show.id)
                        },
                        onShowFocused = { show -> onShowFocused(show) },
                        onViewAll = {
                            navigateToShowsGrid(
                                MainGraphData.ShowsGrid(
                                    ShowsGridQueryType.CATALOG.name,
                                    "ТВ Шоу",
                                    KinoPubContentType.TVSHOW,
                                    KinoPubSort.CREATED
                                )
                            )
                        },
                    )
                }
            }
        }


        Crossfade(
            targetState = if (showImmersiveDetails) focusedShow else null,
            animationSpec = tween(durationMillis = 200),
            label = "ImmersiveDetails",
        ) { show ->
            if (show != null) {
                ImmersiveDetails(
                    modifier = Modifier
                        .padding(start = childPadding.start, top = childPadding.top + 24.dp)
                        .fillMaxWidth(),
                    logoUrl = null,
                    title = show.title,
                    originalTitle = show.originalTitle,
                    description = show.description,
                    rating = Rating(
                        kp = show.ratingKp,
                        imdb = show.ratingImdb,
                        filmCritics = .0,
                        russianFilmCritics = .0,
                        await = .0
                    ),
                    votes = Votes(
                        kp = show.votesKp,
                        imdb = show.votesImdb,
                        filmCritics = 0,
                        russianFilmCritics = 0,
                        await = 0
                    ),
                    genres = show.genres.map { KinopoiskGenre(name = it.name) },
                    countries = show.countries.map { KinopoiskCountry(name = it.name) },
                    year = show.year,
                    seriesLength = if (show.isSeries) show.maxEpisode?.episode else null,
                    movieLength = if (!show.isSeries) show.duration else null,
                    ageRating = show.ageRating.takeIf { it > 0 }?.toString() ?: "",
                )
            }
        }
    }
}
