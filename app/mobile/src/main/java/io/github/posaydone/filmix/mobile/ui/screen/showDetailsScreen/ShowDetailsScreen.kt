@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.filmix.mobile.ui.screen.showDetailsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.latestProgressItem
import io.github.posaydone.filmix.core.model.latestSeriesProgress
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.Loading
import io.github.posaydone.filmix.mobile.ui.common.ShowBannerContent
import io.github.posaydone.filmix.mobile.ui.common.ShowPoster

const val TAG = "ShowDetailsScreen"

@Composable
fun ShowDetailsScreen(
    showId: Int,
    navigateToMoviePlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
    navigateBack: () -> Unit,
    navigateToEpisodes: (showId: Int) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ShowDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(
            NavigationBarDefaults.windowInsets.union(WindowInsets.statusBars)
        )
    ) { paddingValues ->
        when (val s = uiState) {
            is ShowDetailsScreenUiState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            is ShowDetailsScreenUiState.Error -> {
                Error(modifier = Modifier.fillMaxSize(), onRetry = s.onRetry)
            }


            is ShowDetailsScreenUiState.Done -> {
                val playProgress = if (s.showDetails.isSeries) {
                    s.showProgress.latestSeriesProgress()
                } else {
                    s.showProgress.latestProgressItem()
                }
                val playButtonText = when {
                    s.showDetails.isSeries && playProgress != null -> stringResource(
                        R.string.continueWatchingSeries,
                        playProgress.season,
                        playProgress.episode,
                    )

                    !s.showDetails.isSeries && playProgress != null -> stringResource(R.string.continueWatchingMovie)
                    else -> stringResource(R.string.playString)
                }
                Details(
                    showDetails = s.showDetails,
                    toggleFavorites = s.toggleFavorites,
                    navigateToMoviePlayer = {
                        navigateToMoviePlayer(
                            showId,
                            playProgress?.season ?: -1,
                            playProgress?.episode ?: -1,
                        )
                    },
                    playButtonText = playButtonText,
                    navigateToEpisodes = if (s.showDetails.isSeries) {
                        { navigateToEpisodes(showId) }
                    } else null,
                    navigateBack = navigateBack,
                    modifier = modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun Details(
    showDetails: Show,
    toggleFavorites: () -> Unit,
    navigateToMoviePlayer: () -> Unit,
    playButtonText: String,
    navigateBack: () -> Unit,
    navigateToEpisodes: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val headerHeight = 460.dp
    val headerHeightPx = with(receiver = LocalDensity.current) { headerHeight.toPx() }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    Box(modifier = modifier) {
        ShowPoster(
            backdropUrl = showDetails.backdropUrl,
            posterUrl = showDetails.poster,
            height = headerHeight,
            modifier = Modifier.graphicsLayer {
                val scrollOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                alpha =
                    if (lazyListState.firstVisibleItemIndex > 0) 0f else (1f - (scrollOffset / (headerHeightPx / 2))).coerceIn(
                        0f,
                        1f
                    )
            }
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Spacer(modifier = Modifier.height(headerHeight - 100.dp))
            }

            item {
                ShowBannerContent(
                    title = showDetails.title,
                    logoUrl = null,
                    ratingKp = showDetails.ratingKp,
                    votesKp = showDetails.votesKp,
                    originalTitle = showDetails.originalTitle,
                    year = showDetails.year,
                    genres = showDetails.genres.map { it.name },
                    countries = showDetails.countries.map { it.name },
                    totalMinutes = if (showDetails.isSeries) {
                        showDetails.maxEpisode?.episode?.takeIf { it > 0 }
                    } else {
                        showDetails.duration?.takeIf { it > 0 }
                    },
                    ageRating = showDetails.ageRating.takeIf { it > 0 },
                    isFavorite = showDetails.isFavorite,
                    onPlayClick = navigateToMoviePlayer,
                    playButtonText = playButtonText,
                    onToggleFavoritesClick = toggleFavorites,
                    onEpisodesClick = navigateToEpisodes,
                )

                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth(),
                ) {
                    HorizontalDivider()

                    Column(
                        modifier = Modifier.padding(24.dp),
                    ) {
                        if (!showDetails.description.isNullOrBlank()) {
                            DescriptionSection(description = showDetails.description!!)
                        }
                    }
                }
            }
        }

        DynamicTopAppBar(
            title = showDetails.title, isScrolled = isScrolled, navigateBack = navigateBack
        )
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DynamicTopAppBar(
    title: String,
    isScrolled: Boolean,
    navigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = isScrolled, enter = fadeIn(), exit = fadeOut()
            ) {
                Text(text = title)
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isScrolled) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ), navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    contentDescription = "Back", imageVector = Icons.AutoMirrored.Filled.ArrowBack
                )
            }
        })
}
