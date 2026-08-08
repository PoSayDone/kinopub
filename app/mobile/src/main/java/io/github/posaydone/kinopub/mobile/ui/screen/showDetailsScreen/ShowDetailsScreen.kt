@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.kinopub.mobile.ui.screen.showDetailsScreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesNavKey
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowDetailsScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.kinopub.core.common.utils.formatDuration
import io.github.posaydone.kinopub.core.common.utils.formatSeasonCount
import io.github.posaydone.kinopub.core.common.utils.formatVoteCount
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.core.model.Show
import io.github.posaydone.kinopub.core.model.latestProgressItem
import io.github.posaydone.kinopub.core.model.latestSeriesProgress
import io.github.posaydone.kinopub.mobile.ui.common.Error
import io.github.posaydone.kinopub.mobile.ui.common.LargeButton
import io.github.posaydone.kinopub.mobile.ui.common.LargeButtonStyle
import io.github.posaydone.kinopub.mobile.ui.common.Loading
import io.github.posaydone.kinopub.mobile.ui.common.ShowsRow
import io.github.posaydone.kinopub.mobile.ui.common.episodeListItems

const val TAG = "ShowDetailsScreen"

@Composable
fun ShowDetailsScreen(
    showId: Int,
    navigateToMoviePlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
    navigateBack: () -> Unit,
    navigateToShowDetails: (showId: Int) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ShowDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    // SurfaceView (the trailer's video surface) is composited on its own hardware layer
    // outside the normal View hierarchy, so it doesn't reliably hide in sync with Compose
    // navigation transitions — the last frame can keep showing on top of the next screen for
    // a couple of seconds otherwise. Detaching it eagerly the instant back navigation is
    // requested, rather than waiting for this composable to actually be disposed, makes it
    // disappear immediately instead.
    var isLeaving by remember { mutableStateOf(false) }
    var trailerPlayerView by remember { mutableStateOf<PlayerView?>(null) }
    val handleNavigateBack = {
        isLeaving = true
        trailerPlayerView?.player = null
        navigateBack()
    }

    BackHandler(enabled = !isLeaving, onBack = handleNavigateBack)

    Scaffold(
        topBar = {
            DynamicTopAppBar(
                title = (uiState as? ShowDetailsScreenUiState.Done)?.showDetails?.title ?: "",
                isScrolled = isScrolled,
                navigateBack = handleNavigateBack,
            )
        },
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
                    showId = showId,
                    showDetails = s.showDetails,
                    trailerUrl = s.trailerUrl,
                    toggleFavorites = s.toggleFavorites,
                    navigateToMoviePlayer = {
                        navigateToMoviePlayer(
                            showId,
                            playProgress?.season ?: -1,
                            playProgress?.episode ?: -1,
                        )
                    },
                    navigateToEpisodePlayer = { season, episode ->
                        navigateToMoviePlayer(showId, season, episode)
                    },
                    playButtonText = playButtonText,
                    continueSeason = playProgress?.season,
                    continueEpisode = playProgress?.episode,
                    similarShows = s.similarShows,
                    navigateToShowDetails = navigateToShowDetails,
                    lazyListState = lazyListState,
                    isLeaving = isLeaving,
                    onTrailerPlayerViewCreated = { trailerPlayerView = it },
                    modifier = modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

private enum class ShowDetailsTab { ABOUT, EPISODES }

@Composable
private fun Details(
    showId: Int,
    showDetails: Show,
    trailerUrl: String?,
    toggleFavorites: () -> Unit,
    navigateToMoviePlayer: () -> Unit,
    navigateToEpisodePlayer: (season: Int, episode: Int) -> Unit,
    playButtonText: String,
    continueSeason: Int?,
    continueEpisode: Int?,
    similarShows: List<Show> = emptyList(),
    navigateToShowDetails: (showId: Int) -> Unit = {},
    lazyListState: LazyListState,
    isLeaving: Boolean,
    onTrailerPlayerViewCreated: (PlayerView) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(ShowDetailsTab.ABOUT) }

    val episodesViewModel = if (showDetails.isSeries) {
        hiltViewModel<EpisodesScreenViewModel, EpisodesScreenViewModel.Factory>(
            creationCallback = { factory -> factory.create(EpisodesNavKey(showId = showId)) }
        )
    } else null
    val episodesUiState = episodesViewModel?.uiState?.collectAsStateWithLifecycle()?.value

    // Owned here, above the LazyColumn, so scrolling the trailer item out of the
    // composed window (which disposes and later recreates ShowTrailerHeader) doesn't
    // tear down the player and restart the trailer from the beginning.
    val trailerPlayer = rememberTrailerPlayer(trailerUrl)
    // Reflects only the user's explicit tap — leaving/re-entering the composed window
    // pauses/resumes playback on its own (see TrailerPlayerSurface) without touching this,
    // so scrolling the trailer back into view resumes it unless the user paused it themselves.
    var trailerPaused by remember(trailerUrl) { mutableStateOf(false) }
    var trailerMuted by remember(trailerUrl) { mutableStateOf(true) }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
    ) {
        item {
            ShowTrailerHeader(
                trailerPlayer = trailerPlayer,
                isPaused = trailerPaused,
                onTogglePause = { trailerPaused = !trailerPaused },
                isMuted = trailerMuted,
                onToggleMute = { trailerMuted = !trailerMuted },
                backdropUrl = showDetails.backdropUrl,
                posterUrl = showDetails.poster,
                isLeaving = isLeaving,
                onPlayerViewCreated = onTrailerPlayerViewCreated,
            )
        }

        item {
            ShowDetailsHeader(
                showDetails = showDetails,
                playButtonText = playButtonText,
                onPlayClick = navigateToMoviePlayer,
                isFavorite = showDetails.isFavorite,
                onToggleFavoritesClick = toggleFavorites,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            )
        }

        if (!showDetails.isSeries) {
            // Series get their divider from the TabRow underneath the tabs instead —
            // adding this one too would draw a double border.
            item {
                HorizontalDivider()
            }
        }

        if (showDetails.isSeries) {
            item {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    Tab(
                        selected = selectedTab == ShowDetailsTab.ABOUT,
                        onClick = { selectedTab = ShowDetailsTab.ABOUT },
                        text = { Text(stringResource(R.string.about_tab)) },
                    )
                    Tab(
                        selected = selectedTab == ShowDetailsTab.EPISODES,
                        onClick = { selectedTab = ShowDetailsTab.EPISODES },
                        text = { Text(stringResource(R.string.episodesString)) },
                    )
                }
            }
        }

        if (selectedTab == ShowDetailsTab.ABOUT || !showDetails.isSeries) {
            item {
                AboutTabContent(
                    show = showDetails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                )
            }

            if (similarShows.isNotEmpty()) {
                item {
                    ShowsRow(
                        showList = similarShows,
                        title = stringResource(R.string.similar_shows),
                        onShowClick = { show -> navigateToShowDetails(show.id) },
                    )
                }
            }
        } else {
            val seasons = (episodesUiState as? EpisodesScreenUiState.Done)?.seasons ?: emptyList()
            val selectedSeason = seasons.find { it.season == continueSeason }
            val selectedEpisode = selectedSeason?.episodes?.find { it.episode == continueEpisode }

            if (episodesUiState == null || episodesUiState is EpisodesScreenUiState.Loading) {
                item {
                    Loading(modifier = Modifier.fillMaxWidth())
                }
            } else if (seasons.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.episodesString),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(24.dp),
                    )
                }
            } else {
                episodeListItems(
                    seasons = seasons,
                    selectedSeason = selectedSeason,
                    selectedEpisode = selectedEpisode,
                    onEpisodeClick = { season, episode ->
                        navigateToEpisodePlayer(season.season, episode.episode)
                    },
                )
            }
        }
    }
}

/**
 * Owns the trailer's [ExoPlayer] for as long as the [Details] screen is alive — deliberately
 * remembered above the LazyColumn so scrolling the trailer item out of the composed window
 * doesn't release and recreate it, which would restart the trailer from the beginning the
 * next time it scrolls back into view.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun rememberTrailerPlayer(url: String?): ExoPlayer? {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        url?.let {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(it))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer?.release() }
    }

    return exoPlayer
}

@OptIn(UnstableApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun ShowTrailerHeader(
    trailerPlayer: ExoPlayer?,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    backdropUrl: String?,
    posterUrl: String,
    isLeaving: Boolean,
    onPlayerViewCreated: (PlayerView) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(backdropUrl ?: posterUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (trailerPlayer != null && !isLeaving) {
            TrailerPlayerSurface(
                exoPlayer = trailerPlayer,
                isPaused = isPaused,
                isMuted = isMuted,
                onPlayerViewCreated = onPlayerViewCreated,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTogglePause,
                    ),
            )

            if (isPaused) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.play),
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .padding(12.dp),
                )
            }

            IconButton(
                colors = IconButtonDefaults.iconButtonColors().copy(containerColor = Color.Black.copy(alpha = 0.45f)),
                onClick = onToggleMute,
                modifier = Modifier
                    .align(Alignment.BottomEnd).padding(12.dp)
                    .size(32.dp)
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = stringResource(if (isMuted) R.string.unmute else R.string.mute),
                    tint = Color.White,
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun TrailerPlayerSurface(
    exoPlayer: ExoPlayer,
    isPaused: Boolean,
    isMuted: Boolean,
    onPlayerViewCreated: (PlayerView) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Re-runs every time this surface (re)enters composition — including when it scrolls
    // back into view after being disposed while off-screen — so it resumes automatically
    // unless the user explicitly paused it (reflected in isPaused).
    LaunchedEffect(exoPlayer, isPaused) {
        if (isPaused) exoPlayer.pause() else exoPlayer.play()
    }

    LaunchedEffect(exoPlayer, isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // Leaving composition (scrolled off-screen) always pauses to avoid decoding video no one
    // can see; this doesn't touch the caller's paused state, so it resumes on its own above.
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.pause() }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }.also(onPlayerViewCreated)
        },
        update = { it.player = exoPlayer },
        modifier = modifier,
    )
}

@Composable
private fun ShowDetailsHeader(
    showDetails: Show,
    playButtonText: String,
    onPlayClick: () -> Unit,
    isFavorite: Boolean?,
    onToggleFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val secondaryTitle = showDetails.originalTitle
        .trim()
        .takeIf { it.isNotEmpty() && !it.equals(showDetails.title.trim(), ignoreCase = true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = showDetails.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
            )
            if (secondaryTitle != null) {
                Text(
                    text = secondaryTitle,
                    style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ShowMetaRow(
            ratingKp = showDetails.ratingKp,
            votesKp = showDetails.votesKp,
            year = showDetails.year,
            genres = showDetails.genres.map { it.name },
            countries = showDetails.countries.map { it.name },
            isSeries = showDetails.isSeries,
            seasonCount = showDetails.maxEpisode?.season?.takeIf { it > 0 },
            durationSeconds = showDetails.durationSeconds?.takeIf { it > 0 },
            ageRating = showDetails.ageRating.takeIf { it > 0 },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LargeButton(onClick = onPlayClick) {
                Icon(
                    contentDescription = stringResource(R.string.play),
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.PlayArrow,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = playButtonText)
            }
            if (isFavorite != null) {
                LargeButton(
                    style = LargeButtonStyle.OUTLINED,
                    onClick = onToggleFavoritesClick,
                    colors = if (isFavorite) ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) else ButtonDefaults.outlinedButtonColors(),
                ) {
                    Icon(
                        contentDescription = stringResource(R.string.favorite),
                        modifier = Modifier.size(28.dp),
                        imageVector = if (isFavorite) Icons.Rounded.BookmarkRemove else Icons.Rounded.BookmarkAdd,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShowMetaRow(
    ratingKp: Double?,
    votesKp: Int?,
    year: Int?,
    genres: List<String>,
    countries: List<String>,
    isSeries: Boolean,
    seasonCount: Int?,
    durationSeconds: Int?,
    ageRating: Int?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val parts = buildList {
        if ((ratingKp ?: 0.0) > 0) {
            add("%.1f".format(ratingKp) + " " + formatVoteCount(votesKp ?: 0))
        }
        year?.let { add(it.toString()) }
        if (genres.isNotEmpty()) add(genres.take(3).joinToString(", "))
        if (isSeries) {
            seasonCount?.let { add(formatSeasonCount(context, it)) }
        } else {
            durationSeconds?.let { add(formatDuration(context, it)) }
        }
        if (countries.isNotEmpty()) add(countries.take(2).joinToString(", "))
        ageRating?.let { add("$it+") }
    }
    if (parts.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        parts.forEach { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AboutTabContent(show: Show, modifier: Modifier = Modifier) {
    val detailsRows = buildDetailsRows(show)
    val castNames = show.cast
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(28.dp)) {
        if (!show.description.isNullOrBlank()) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeading(stringResource(R.string.description_label))
                DescriptionText(description = show.description!!)
            }
        }

        if (detailsRows.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeading(stringResource(R.string.details_label))
                DetailsTable(rows = detailsRows)
            }
        }

        if (castNames.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeading(
                    text = stringResource(R.string.cast_label),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                // The grid spans full width and pushes its own inset into contentPadding,
                // so items can scroll edge-to-edge under where the section padding would
                // otherwise clip them, instead of being boxed in by the parent's padding.
                CastCards(names = castNames, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, fontSize = 18.sp, letterSpacing = (-0.5).sp),
        modifier = modifier,
    )
}

private const val DescriptionCollapsedMaxLines = 3

@Composable
private fun DescriptionText(description: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var truncatable by remember { mutableStateOf(false) }
    var cutoffIndex by remember { mutableIntStateOf(0) }
    val readMoreText = stringResource(R.string.read_more)
    val readMoreColor = MaterialTheme.colorScheme.primary

    val displayedText = if (truncatable && !expanded) {
        buildAnnotatedString {
            val adjusted = description
                .substring(0, cutoffIndex)
                .dropLast(readMoreText.length + 2)
                .trimEnd(',', '.', ' ', '\n')
            append(adjusted)
            append("… ")
            withLink(
                LinkAnnotation.Clickable(tag = "read_more") { expanded = true }
            ) {
                withStyle(SpanStyle(color = readMoreColor, fontWeight = FontWeight.Medium)) {
                    append(readMoreText)
                }
            }
        }
    } else {
        AnnotatedString(description)
    }

    Text(
        text = displayedText,
        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = if (expanded) Int.MAX_VALUE else DescriptionCollapsedMaxLines,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (!expanded && !truncatable && result.hasVisualOverflow) {
                truncatable = true
                cutoffIndex = result.getLineEnd(DescriptionCollapsedMaxLines - 1)
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun buildDetailsRows(show: Show): List<Pair<String, String>> = buildList {
    formatQualityBadge(show.quality)?.let { add(stringResource(R.string.quality) to it) }
    if ((show.langs ?: 0) > 0) {
        val audioValue = show.langs.toString() + if (show.hasAc3 == true) " • AC-3" else ""
        add(stringResource(R.string.audio_tracks) to audioValue)
    }
    if ((show.subtitlesCount ?: 0) > 0) {
        add(stringResource(R.string.subtitles_label) to show.subtitlesCount.toString())
    }
    if (!show.voice.isNullOrBlank()) add(stringResource(R.string.voice_label) to show.voice!!)
    if (!show.director.isNullOrBlank()) add(stringResource(R.string.director_label) to show.director!!)
}

@Composable
private fun DetailsTable(rows: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rows.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    modifier = Modifier.weight(1.4f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private val CastCardWidth = 180.dp
private val CastAvatarSize = 40.dp
private val CastRowHeight = 56.dp

@Composable
private fun CastCards(names: List<String>, modifier: Modifier = Modifier) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(3),
        modifier = modifier.height(CastRowHeight * 3 + 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(names) { name -> CastCard(name = name) }
    }
}

@Composable
private fun CastCard(name: String) {
    Card(
        modifier = Modifier
            .width(CastCardWidth)
            .height(CastRowHeight),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(CastAvatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatQualityBadge(quality: String?): String? {
    val q = quality?.filter(Char::isDigit)?.toIntOrNull() ?: return null
    return when {
        q >= 2160 -> "4K"
        q >= 1080 -> "1080p"
        q >= 720 -> "720p"
        q >= 480 -> "480p"
        q > 0 -> "${q}p"
        else -> null
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
                Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }, navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    contentDescription = stringResource(R.string.back), imageVector = Icons.AutoMirrored.Filled.ArrowBack
                )
            }
        })
}
