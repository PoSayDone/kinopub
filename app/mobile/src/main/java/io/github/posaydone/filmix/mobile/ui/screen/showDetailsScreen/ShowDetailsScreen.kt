@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.filmix.mobile.ui.screen.showDetailsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.core.common.utils.formatDuration
import io.github.posaydone.filmix.core.common.utils.formatVoteCount
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowTrailers
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.LargeButton
import io.github.posaydone.filmix.mobile.ui.common.LargeButtonStyle
import io.github.posaydone.filmix.mobile.ui.common.Loading
import kotlin.math.max

val TAG = "ShowDetailsScreen"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShowDetailsScreen(
    showId: Int,
    navigateToMoviePlayer: (showId: Int) -> Unit,
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
                Details(
                    showDetails = s.showDetails,
                    fullShow = s.fullShow,
                    showProgress = s.showProgress,
                    showImages = s.showImages,
                    showTrailers = s.showTrailers,
                    toggleFavorites = s.toggleFavorites,
                    navigateToMoviePlayer = { navigateToMoviePlayer(showId) },
                    navigateToEpisodes = if (s.fullShow.isSeries) {
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
    showDetails: ShowDetails,
    fullShow: FullShow,
    showProgress: ShowProgress,
    showImages: ShowImages,
    showTrailers: ShowTrailers,
    toggleFavorites: () -> Unit,
    navigateToMoviePlayer: () -> Unit,
    navigateBack: () -> Unit,
    navigateToEpisodes: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val headerHeight = 420.dp
    val headerHeightPx = with(receiver = LocalDensity.current) { headerHeight.toPx() }

    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    Box(modifier = modifier) {
        ShowPoster(
            backdropUrl = fullShow.backdropUrl,
            posterUrl = fullShow.posterUrl,
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
                    title = fullShow.title,
                    logoUrl = fullShow.logoUrl,
                    ratingKp = fullShow.ratingKp,
                    votesKp = fullShow.votesKp,
                    originalTitle = fullShow.originalTitle,
                    year = fullShow.year,
                    genres = fullShow.genres,
                    countries = fullShow.countries,
                    totalMinutes = max(
                        fullShow.movieLength ?: 0, fullShow.seriesLength ?: 0
                    ).takeIf { it > 0 } ?: showDetails.duration ?: 0,
                    ageRating = fullShow.ageRating.takeIf { it > 0 },
                    isFavorite = showDetails.isFavorite,
                    onPlayClick = navigateToMoviePlayer,
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
                        DescriptionSection(
                            description = fullShow.description ?: fullShow.shortDescription
                            ?: showDetails.shortStory
                        )
                    }
                }
            }
        }

        DynamicTopAppBar(
            title = fullShow.title, isScrolled = isScrolled, navigateBack = navigateBack
        )
    }
}

/**
 * A reusable component that displays the main content of a show banner,
 * including title, metadata, and action buttons.
 */
@Composable
fun ShowBannerContent(
    modifier: Modifier = Modifier,
    title: String,
    logoUrl: String?,
    ratingKp: Double?,
    votesKp: Int?,
    originalTitle: String?,
    year: Int?,
    genres: List<String>,
    countries: List<String>,
    totalMinutes: Int? = null,
    ageRating: Int?,
    onPlayClick: () -> Unit,
    isFavorite: Boolean? = null,
    onToggleFavoritesClick: (() -> Unit)? = null,
    onEpisodesClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleSection(
            title = title, logoUrl = logoUrl
        )
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MetadataColumn(
                ratingKp = ratingKp,
                votesKp = votesKp,
                originalTitle = originalTitle,
                title = title,
                year = year,
                genres = genres,
                countries = countries,
                totalMinutes = totalMinutes,
                ageRating = ageRating
            )

            ActionButtons(
                navigateToMoviePlayer = onPlayClick,
                toggleFavorites = onToggleFavoritesClick,
                isFavorite = isFavorite,
                navigateToEpisodes = onEpisodesClick,
            )
        }
    }
}


@Composable
fun TitleSection(
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    title: String,
    logoUrl: String?,
    forceTextTitle: Boolean = false,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
   
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.background
                    )
                )
            )
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.1f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.background
                    )
                )
            )
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.2f to Color.Transparent, 1.0f to MaterialTheme.colorScheme.background
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (logoUrl != null && !forceTextTitle) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(logoUrl).build(),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.sizeIn(
                    maxWidth = screenWidth * 0.6f, maxHeight = screenHeight * 0.32f
                )
            )
        } else {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun MetadataColumn(
    ratingKp: Double?,
    votesKp: Int?,
    originalTitle: String?,
    title: String,
    year: Int?,
    genres: List<String>,
    countries: List<String>,
    totalMinutes: Int?,
    ageRating: Int?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "%.1f".format(
                    ratingKp ?: 0.0
                ) + " (${formatVoteCount(votesKp ?: 0)})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (originalTitle != null && originalTitle.isNotBlank() && originalTitle != title) {
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = originalTitle!!,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = year?.toString() ?: "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = genres.take(2).joinToString(", "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = countries.take(2).joinToString(", "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (totalMinutes != null) Text(
                text = formatDuration(context, totalMinutes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ageRating?.let {
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$it+",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    navigateToMoviePlayer: () -> Unit,
    toggleFavorites: (() -> Unit)? = null,
    isFavorite: Boolean? = null,
    playButtonText: String = stringResource(R.string.playString),
    navigateToEpisodes: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LargeButton(
            onClick = navigateToMoviePlayer
        ) {
            Icon(
                contentDescription = stringResource(R.string.play),
                modifier = Modifier.size(28.dp),
                imageVector = Icons.Rounded.PlayArrow,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = playButtonText)
        }
        if (navigateToEpisodes != null) {
            LargeButton(
                style = LargeButtonStyle.OUTLINED,
                onClick = navigateToEpisodes,
            ) {
                Icon(
                    contentDescription = stringResource(R.string.episodesString),
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.ViewList,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.episodesString))
            }
        }
        if (toggleFavorites != null && isFavorite != null) LargeButton(
            style = LargeButtonStyle.OUTLINED,
            onClick = toggleFavorites,
            colors = if (isFavorite) ButtonDefaults.buttonColors().copy(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) else ButtonDefaults.outlinedButtonColors()
        ) {
            Icon(
                contentDescription = stringResource(R.string.favorite),
                modifier = Modifier.size(28.dp),
                imageVector = if (isFavorite) Icons.Rounded.BookmarkRemove else Icons.Rounded.BookmarkAdd,
            )
        }
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
fun ShowPoster(
    modifier: Modifier = Modifier,
    backdropUrl: String? = null,
    posterUrl: String,
    height: Dp,
) {
    if (backdropUrl != null) AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(backdropUrl!!).crossfade(true)
            .build(),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
    else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(posterUrl).crossfade(true)
                    .build(),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(2 / 3f)
            )
        }
    }
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

