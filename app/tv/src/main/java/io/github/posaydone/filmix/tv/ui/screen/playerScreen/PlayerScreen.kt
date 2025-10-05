@file:kotlin.OptIn(
    ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class
)

package io.github.posaydone.filmix.tv.ui.screen.playerScreen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.Translation
import io.github.posaydone.filmix.core.model.VideoWithQualities
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.SideDialog
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerShowHeader
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.ScrollableTabRow
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.SettingsDialog
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerControlsIcon
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerOverlay
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerPulse
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerPulseState
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerSeeker
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerState
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.rememberVideoPlayerPulseState
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.rememberVideoPlayerState
import io.github.posaydone.filmix.tv.ui.utils.handleDPadKeyEvents
import kotlin.time.Duration.Companion.milliseconds

/**
 * [Work in progress] A composable screen for playing a video.
 *
 * @param onBackPressed The callback to invoke when the user presses the back button.
 * @param viewModel The view model for the video player screen.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    showId: Int,
    viewModel: PlayerScreenViewModel = hiltViewModel(),
) {
    val showDetails by viewModel.details.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val player = viewModel.playerController.collectAsState().value

    when (showDetails == null || player == null) {
        true -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        false -> {
            VideoPlayerScreenContent(
                player, viewModel, playerState, showDetails!!
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreenContent(
    player: MediaController,
    viewModel: PlayerScreenViewModel,
    playerState: PlayerState,
    showDetails: FullShow,
) {
    val showType by viewModel.contentType.collectAsState()

    val hasPrevEpisode by viewModel.hasPrevEpisode.collectAsState()
    val hasNextEpisode by viewModel.hasNextEpisode.collectAsState()

    val context = LocalContext.current

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.saveProgress()
                    viewModel.pause()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            viewModel.saveProgress()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val videoPlayerState = rememberVideoPlayerState(hideSeconds = 4)

    var isAudioDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isQualityDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isEpisodeDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val pulseState = rememberVideoPlayerPulseState()
    val focusRequester = remember { FocusRequester() }

    Box(
        Modifier
            .dPadEvents(
                seekBack = { viewModel.seekBack() },
                seekForward = { viewModel.seekForward() },
                pause = { viewModel.pause() },
                isEpisodeDialogOpen,
                videoPlayerState,
                pulseState
            )
            .fillMaxSize()
            .background(color = Color.Black)
            .focusable()
    ) {


        AndroidView(
            factory = {
                PlayerView(context).apply { useController = false }
            }, update = {
                it.player = player
                it.apply {
                    resizeMode = playerState.resizeMode
                    keepScreenOn = playerState.isPlaying
                }
            }, modifier = Modifier.fillMaxSize()
        )


        VideoPlayerOverlay(
            modifier = Modifier.fillMaxSize(),
            focusRequester = focusRequester,
            state = videoPlayerState,
            isPlaying = player.isPlaying,
            pulseState = pulseState,
            centerButton = { VideoPlayerPulse(pulseState, playerState.isLoading) },
            subtitles = { /* TODO Implement subtitles */ },
            header = {
                PlayerShowHeader(
                    showDetails = showDetails,
                    currentSeason = if (showType == ShowType.SERIES) "Season 1" else null,  // Will be updated with real data
                    currentEpisode = if (showType == ShowType.SERIES) "Episode 1" else null
                )
            },
            controls = {
                VideoPlayerControls(
                    isPlaying = player.isPlaying,
                    showType,
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    player = player,
                    videoPlayerState,
                    focusRequester,
                    changeSizing = {
                        viewModel.setResizeMode(if (playerState.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT)
                    },
                    openEpisodeSheet = {
                        player.pause(); videoPlayerState.hideControls(); isEpisodeDialogOpen = true
                    },
                    openAudioSheet = { isAudioDialogOpen = true },
                    openQualitySheet = { isQualityDialogOpen = true },
                    hasNextEpisode = hasNextEpisode,
                    hasPrevEpisode = hasPrevEpisode,
                    onPrevEpisodeClick = { viewModel.goToPrevEpisode() },
                    onNextEpisodeClick = { viewModel.goToNextEpisode() })
            })


        Dialogs(
            focusRequester,
            viewModel,
            isEpisodeDialogOpen,
            isAudioDialogOpen,
            isQualityDialogOpen,
            closeEpisodeSheet = { isEpisodeDialogOpen = false },
            closeAudioSheet = { isAudioDialogOpen = false },
            closeQualitySheet = { isQualityDialogOpen = false })

    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerControls(
    isPlaying: Boolean,
    showType: ShowType?,
    currentPosition: Long,
    duration: Long,
    player: MediaController,
    state: VideoPlayerState,
    focusRequester: FocusRequester,
    changeSizing: () -> Unit,
    openEpisodeSheet: () -> Unit,
    openAudioSheet: () -> Unit,
    openQualitySheet: () -> Unit,
    onPrevEpisodeClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    hasNextEpisode: Boolean,
    hasPrevEpisode: Boolean,
) {
    val onPlayPauseToggle = { shouldPlay: Boolean ->
        if (shouldPlay) {
            player.play()
        } else {
            player.pause()
        }
    }

    Column {
        VideoPlayerSeeker(
            state = state,
            onSeek = { player.seekTo(player.duration.times(it).toLong()) },
            contentProgress = currentPosition.milliseconds,
            contentDuration = duration.milliseconds
        )

        // New controls row below seeker
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Previous episode button
                if (showType == ShowType.SERIES && hasPrevEpisode) {
                    VideoPlayerControlsIcon(
                        icon = Icons.Default.SkipPrevious,
                        state = state,
                        isPlaying = isPlaying,
                        contentDescription = "Previous episode",
                        onClick = onPrevEpisodeClick,
                    )
                }

                // Play/Pause button
                VideoPlayerControlsIcon(
                    modifier = Modifier.focusRequester(focusRequester),
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    state = state,
                    isPlaying = isPlaying,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = { onPlayPauseToggle(!isPlaying) },
                )

                // Next episode button
                if (showType == ShowType.SERIES && hasNextEpisode) {
                    VideoPlayerControlsIcon(
                        icon = Icons.Default.SkipNext,
                        state = state,
                        isPlaying = isPlaying,
                        contentDescription = "Next episode",
                        onClick = onNextEpisodeClick,
                    )
                }

                // All episodes button (icon + text)
                if (showType == ShowType.SERIES) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { openEpisodeSheet() }
                            .padding(8.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesomeMotion,
                            contentDescription = "All episodes"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Episodes")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openAudioSheet() }
                        .padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack, contentDescription = "Audio tracks"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Audio")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openQualitySheet() }
                        .padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings, contentDescription = "Settings"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Settings")
                }
            }
        }
    }
}

@UnstableApi
@Composable
private fun EpisodeDialog(
    focusRequester: FocusRequester,
    viewModel: PlayerScreenViewModel,
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    isEpisodeDialogOpen: Boolean,
    onDismiss: () -> Unit,
) {
    val seasonsList = seasons.map { season ->
        stringResource(R.string.season, season.season)
    }

    var selectedTab by remember {
        mutableIntStateOf(
            selectedSeason!!.season.minus(1)
        )
    }

    SideDialog(
        modifier = Modifier.focusRequester(focusRequester),
        showDialog = isEpisodeDialogOpen,
        onDismissRequest = onDismiss,
        title = "Select Episode",
        description = null
    ) {
        Column {
            ScrollableTabRow(
                items = seasonsList,
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            val selectedSeasonEpisodes = seasons[selectedTab].episodes

            LazyColumn(
                modifier = Modifier.focusRequester(focusRequester),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                items(selectedSeasonEpisodes) { episode ->
                    SingleSelectionCard(
                        selectionOption = episode, selectedEpisode
                    ) {
                        viewModel.setSeason(seasons[selectedTab])
                        viewModel.setEpisode(episode)
                        onDismiss()
                    }
                }
            }
        }
    }
}


@ExperimentalTvMaterial3Api
@OptIn(UnstableApi::class)
@Composable
private fun <T> AudioDialog(
    focusRequester: FocusRequester,
    translations: List<T>,
    selectedTranslation: T?,
    viewModel: PlayerScreenViewModel,
    isAudioDialogOpen: Boolean,
    showType: ShowType?,
    onDismiss: () -> Unit,
) {

    SideDialog(
        modifier = Modifier.focusRequester(focusRequester),
        showDialog = isAudioDialogOpen,
        onDismissRequest = onDismiss,
        title = "Select Audio Track",
        description = null
    ) {
        LazyColumn(
            modifier = Modifier.focusRequester(focusRequester),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            items(translations) { item ->
                SingleSelectionCard(
                    selectionOption = item,
                    selectedTranslation,
                ) {
                    when (showType) {
                        ShowType.MOVIE -> viewModel.setMovieTranslation(item as VideoWithQualities)
                        ShowType.SERIES -> viewModel.setTranslation(item as Translation)
                        null -> {}
                    }
                    onDismiss()
                }
            }
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
private fun QualityDialog(
    focusRequester: FocusRequester,
    qualities: List<File>,
    selectedQuality: File?,
    viewModel: PlayerScreenViewModel,
    isQualitySheetOpen: Boolean,
    onDismiss: () -> Unit,
) {

    SideDialog(
        modifier = Modifier.focusRequester(focusRequester),
        showDialog = isQualitySheetOpen,
        onDismissRequest = onDismiss,
        title = "Select Quality",
        description = null
    ) {
        LazyColumn(
            modifier = Modifier.focusRequester(focusRequester),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            items(qualities) { item ->
                SingleSelectionCard(
                    selectionOption = item,
                    selectedQuality,
                ) {
                    viewModel.setQuality(item)
                    onDismiss()
                }
            }
        }
    }
}


@Composable
fun <T> SingleSelectionCard(selectionOption: T, selectedOption: T?, onOptionClicked: (T) -> Unit) {
    ListItem(
        headlineContent = { Text(text = selectionOption.toString()) },
        scale = ListItemDefaults.scale(focusedScale = 1.02f),
        selected = false,
        onClick = { onOptionClicked(selectionOption) },
        trailingContent = {
            if (selectedOption == selectionOption) {
                Icon(Icons.Default.Check, contentDescription = "Check")
            }
        })
}

@OptIn(UnstableApi::class)
@Composable
private fun Dialogs(
    focusRequester: FocusRequester,
    viewModel: PlayerScreenViewModel,
    isEpisodeSheetOpen: Boolean,
    isAudioSheetOpen: Boolean,
    isQualitySheetOpen: Boolean,
    closeEpisodeSheet: () -> Unit,
    closeAudioSheet: () -> Unit,
    closeQualitySheet: () -> Unit,
) {
    val seasons by viewModel.seasons.collectAsState()
    val moviePieces by viewModel.moviePieces.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val selectedTranslation by viewModel.selectedTranslation.collectAsState()
    val selectedMovieTranslation by viewModel.selectedMovieTranslation.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val showType by viewModel.contentType.collectAsState()

    if (showType == ShowType.SERIES) {
        if (seasons != null && selectedSeason != null) {
            EpisodeDialog(
                focusRequester = focusRequester,
                viewModel = viewModel,
                seasons = seasons!!,
                selectedSeason = selectedSeason,
                selectedEpisode = selectedEpisode,
                isEpisodeDialogOpen = isEpisodeSheetOpen,
                onDismiss = closeEpisodeSheet
            )
        }

        selectedEpisode?.translations?.let { translations ->
            AudioDialog(
                focusRequester = focusRequester,
                translations,
                selectedTranslation,
                viewModel,
                isAudioSheetOpen,
                showType,
                onDismiss = closeAudioSheet
            )
        }

        // Use SettingsDialog instead of QualityDialog
        selectedTranslation?.files?.let { qualities ->
            SettingsDialog(
                focusRequester = focusRequester,
                qualities = qualities,
                selectedQuality = selectedQuality,
                cropOptions = listOf("Fit", "Fill", "Zoom"), // Sample crop options
                selectedCrop = "Fit", // Sample selected crop
                viewModel = viewModel,
                isSettingsSheetOpen = isQualitySheetOpen,
                onDismiss = closeQualitySheet,
                onQualitySelected = { quality -> viewModel.setQuality(quality) },
                onCropSelected = { crop -> /* Handle crop selection */ })
        }
    } else {
        moviePieces?.let { translations ->
            AudioDialog(
                focusRequester = focusRequester,
                translations,
                selectedMovieTranslation,
                viewModel,
                isAudioSheetOpen,
                showType,
                onDismiss = closeAudioSheet
            )
        }

        // Use SettingsDialog instead of QualityDialog
        selectedMovieTranslation?.files?.let { qualities ->
            SettingsDialog(
                focusRequester = focusRequester,
                qualities = qualities,
                selectedQuality = selectedQuality,
                cropOptions = listOf("Fit", "Fill", "Zoom"), // Sample crop options
                selectedCrop = "Fit", // Sample selected crop
                viewModel = viewModel,
                isSettingsSheetOpen = isQualitySheetOpen,
                onDismiss = closeQualitySheet,
                onQualitySelected = { quality -> viewModel.setQuality(quality) },
                onCropSelected = { crop -> /* Handle crop selection */ })
        }
    }
}

private fun Modifier.dPadEvents(
    seekBack: () -> Unit,
    seekForward: () -> Unit,
    pause: () -> Unit,
    isEpisodeSheetOpen: Boolean,
    videoPlayerState: VideoPlayerState,
    pulseState: VideoPlayerPulseState,
): Modifier = this.handleDPadKeyEvents(onLeft = {
    if (!videoPlayerState.controlsVisible && !isEpisodeSheetOpen) {
        seekBack()
        pulseState.setType(VideoPlayerPulse.Type.BACK)
    }
}, onRight = {
    if (!videoPlayerState.controlsVisible && !isEpisodeSheetOpen) {
        seekForward()
        pulseState.setType(VideoPlayerPulse.Type.FORWARD)
    }
}, onUp = {
    if (!isEpisodeSheetOpen) videoPlayerState.showControls()
}, onDown = {
    if (!isEpisodeSheetOpen) videoPlayerState.showControls()
}, onEnter = {
    if (!isEpisodeSheetOpen) {
        pause()
        videoPlayerState.showControls()
    }
})
