@file:kotlin.OptIn(
    ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class
)

package io.github.posaydone.kinopub.tv.ui.screen.playerScreen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel.Companion.SHOW_CONTROLS_TIME
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowType
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.File
import io.github.posaydone.kinopub.core.model.Show
import io.github.posaydone.kinopub.core.model.ShowDetails
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.core.model.Translation
import io.github.posaydone.kinopub.tv.ui.common.Loading
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.PlayerDialogs
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.PlayerEffects
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.PlayerMediaTitle
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.PlayerOverlay
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.PlayerPulse
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.PlayerPulseState
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.rememberPlayerPulseState
import io.github.posaydone.kinopub.tv.ui.theme.KinopubTheme
import io.github.posaydone.kinopub.tv.ui.utils.handleDPadKeyEvents

/**
 * @param onBackPressed The callback to invoke when the user presses the back button.
 * @param viewModel The view model for the video player screen.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerScreenViewModel = hiltViewModel(),
) {
    val showDetails by viewModel.details.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val player = viewModel.playerController.collectAsState().value
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()

    when (showDetails == null || player == null) {
        true -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        false -> {
            VideoPlayerScreenContent(
                player, viewModel, playerState, showDetails!!, selectedSeason, selectedEpisode
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
    showDetails: ShowDetails,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val showType by viewModel.contentType.collectAsState()
    val hasPrevEpisode by viewModel.hasPrevEpisode.collectAsState()
    val hasNextEpisode by viewModel.hasNextEpisode.collectAsState()
    val isHls4AudioTrackSelectionEnabled by viewModel.isHls4AudioTrackSelectionEnabled.collectAsState()
    var isAudioDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isQualityDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isEpisodeDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val pulseState = rememberPlayerPulseState()


    PlayerEffects(
        lifecycleOwner = lifecycleOwner,
        playerState = playerState,
        pulseState = pulseState,
        onShowControls = { viewModel.showControls(it) },
        saveProgress = { viewModel.saveProgress() },
        pause = { viewModel.pause() })

    PlayerScreenContent(
        playerState = playerState,
        pulseState = pulseState,
        showDetails = showDetails,
        showType = showType,
        selectedSeason = selectedSeason,
        selectedEpisode = selectedEpisode,
        hasPrevEpisode = hasPrevEpisode,
        hasNextEpisode = hasNextEpisode,
        isAudioTrackSelectionEnabled = isHls4AudioTrackSelectionEnabled,
        isEpisodeDialogOpen = isEpisodeDialogOpen,
        onRetry = { viewModel.retryPlayback() },
        seekBack = { viewModel.seekBack() },
        seekForward = { viewModel.seekForward() },
        pause = { viewModel.pause() },
        onShowControls = { viewModel.showControls(it) },
        onEnterHold = { viewModel.enableSpeedUp() },
        onEnterHoldUp = { viewModel.disableSpeedUp() },
        onHideControls = { viewModel.hideControls() },
        openEpisodeSheet = {
            viewModel.pause()
            viewModel.hideControls()
            isEpisodeDialogOpen = true
        },
        openAudioSheet = {
            if (isHls4AudioTrackSelectionEnabled) {
                isAudioDialogOpen = true
            }
        },
        openQualitySheet = { isQualityDialogOpen = true },
        onPrevEpisodeClick = { viewModel.goToPrevEpisode() },
        onNextEpisodeClick = { viewModel.goToNextEpisode() },
        seekTo = { viewModel.seekTo(it) },
        onPlayPauseToggle = { viewModel.onPlayPauseClick() },
        playerSurface = {
            AndroidView(
                factory = { PlayerView(context).apply { useController = false } },
                update = {
                    it.player = player
                    it.apply {
                        resizeMode = playerState.resizeMode
                        keepScreenOn = playerState.isPlaying
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        },
        dialogs = {
            PlayerDialogs(
                viewModel,
                isEpisodeDialogOpen,
                isAudioDialogOpen,
                isQualityDialogOpen,
                closeEpisodeSheet = { isEpisodeDialogOpen = false },
                closeAudioSheet = { isAudioDialogOpen = false },
                closeQualitySheet = { isQualityDialogOpen = false }
            )
        }
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerScreenContent(
    playerState: PlayerState,
    pulseState: PlayerPulseState,
    showDetails: ShowDetails,
    showType: ShowType?,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    hasPrevEpisode: Boolean,
    hasNextEpisode: Boolean,
    isAudioTrackSelectionEnabled: Boolean,
    isEpisodeDialogOpen: Boolean,
    onRetry: () -> Unit = {},
    seekBack: () -> Unit,
    seekForward: () -> Unit,
    pause: () -> Unit,
    onShowControls: (Int) -> Unit,
    onEnterHold: () -> Unit,
    onEnterHoldUp: () -> Unit,
    onHideControls: () -> Unit,
    openEpisodeSheet: () -> Unit,
    openAudioSheet: () -> Unit,
    openQualitySheet: () -> Unit,
    onPrevEpisodeClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    seekTo: (Long) -> Unit,
    onPlayPauseToggle: () -> Unit,
    playerSurface: @Composable BoxScope.() -> Unit,
    dialogs: @Composable () -> Unit = {},
) {
    Box(
        Modifier
            .dPadEvents(
                playerState = playerState,
                pulseState = pulseState,
                seekBack = seekBack,
                seekForward = seekForward,
                pause = pause,
                isEpisodeSheetOpen = isEpisodeDialogOpen,
                onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                onEnterHold = onEnterHold,
                onEnterHoldUp = onEnterHoldUp
            )
            .fillMaxSize()
            .background(color = Color.Black)
            .focusable()
    ) {
        playerSurface()

        PlayerOverlay(
            modifier = Modifier.fillMaxSize(),
            playerState = playerState,
            onRetry = onRetry,
            centerButton = { PlayerPulse(pulseState, playerState.isLoading) },
            subtitles = { /* TODO Implement subtitles */ },
            header = {
                PlayerMediaTitle(
                    showDetails = showDetails,
                    currentSeason = if (showType == ShowType.SERIES && selectedSeason != null) stringResource(
                        R.string.seasonString
                    ) + " " + selectedSeason.season else null,
                    currentEpisode = if (showType == ShowType.SERIES && selectedEpisode != null) stringResource(
                        R.string.episode, selectedEpisode.episode
                    ) else null,
                )
            },
            controls = {
                PlayerControls(
                    showType = showType,
                    playerState = playerState,
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    onShowControls = onShowControls,
                    onHideControls = onHideControls,
                    openEpisodeSheet = openEpisodeSheet,
                    isAudioTrackSelectionEnabled = isAudioTrackSelectionEnabled,
                    openAudioSheet = openAudioSheet,
                    openQualitySheet = openQualitySheet,
                    hasNextEpisode = hasNextEpisode,
                    hasPrevEpisode = hasPrevEpisode,
                    onPrevEpisodeClick = onPrevEpisodeClick,
                    onNextEpisodeClick = onNextEpisodeClick,
                    seekTo = seekTo,
                    onPlayPauseToggle = onPlayPauseToggle
                )
            }
        )

        dialogs()
    }
}

@UnstableApi
@Preview(
    name = "TV Player Screen",
    widthDp = 1280,
    heightDp = 720,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun PlayerScreenPreview() {
    val previewPulseState = rememberPlayerPulseState()
    val previewEpisode = Episode(
        episode = 6,
        ad_skip = 0,
        title = "Safe Passage",
        released = "2026-04-15",
        translations = mutableListOf(
            Translation(
                translation = "Original",
                files = listOf(
                    File(
                        url = "https://example.com/tv-preview.m3u8",
                        quality = 2160,
                        proPlus = false
                    )
                )
            )
        )
    )
    val previewSeason = Season(
        season = 1,
        episodes = mutableListOf(previewEpisode)
    )

    KinopubTheme {
        PlayerScreenContent(
            playerState = PlayerState(
                isPlaying = true,
                isLoading = false,
                currentPosition = 2_185_000L,
                duration = 3_420_000L,
                controlsVisible = true
            ),
            pulseState = previewPulseState,
            showDetails = Show(
                id = 1,
                title = "Автар: Легенда об Аанге",
                originalTitle = "Avatar: The last air-bender",
                poster = "",
                backdropUrl = null,
                year = 2025,
                description = "Preview content",
                isSeries = true,
            ),
            showType = ShowType.SERIES,
            selectedSeason = previewSeason,
            selectedEpisode = previewEpisode,
            hasPrevEpisode = true,
            hasNextEpisode = true,
            isAudioTrackSelectionEnabled = true,
            isEpisodeDialogOpen = false,
            seekBack = {},
            seekForward = {},
            pause = {},
            onShowControls = {},
            onEnterHold = {},
            onEnterHoldUp = {},
            onHideControls = {},
            openEpisodeSheet = {},
            openAudioSheet = {},
            openQualitySheet = {},
            onPrevEpisodeClick = {},
            onNextEpisodeClick = {},
            seekTo = {},
            onPlayPauseToggle = {},
            playerSurface = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF090B0F),
                                    Color(0xFF183047),
                                    Color(0xFF030405)
                                )
                            )
                        )
                )
            }
        )
    }
}


@OptIn(UnstableApi::class)
private fun Modifier.dPadEvents(
    seekBack: () -> Unit,
    seekForward: () -> Unit,
    pause: () -> Unit,
    isEpisodeSheetOpen: Boolean,
    playerState: PlayerState,
    onShowControls: () -> Unit,
    pulseState: PlayerPulseState,
    onEnterHold: () -> Unit,
    onEnterHoldUp: () -> Unit,
): Modifier = this.handleDPadKeyEvents(onLeft = {
    if (!playerState.controlsVisible && !isEpisodeSheetOpen) {
        seekBack()
        pulseState.setType(PlayerPulse.Type.BACK)
    }
}, onRight = {
    if (!playerState.controlsVisible && !isEpisodeSheetOpen) {
        seekForward()
        pulseState.setType(PlayerPulse.Type.FORWARD)
    }
}, onUp = {
    if (!isEpisodeSheetOpen) onShowControls()
}, onDown = {
    if (!isEpisodeSheetOpen) onShowControls()
}, onEnter = {
    if (!isEpisodeSheetOpen) {
        pause()
        onShowControls()
    }
}, onEnterHold = {
    if (!isEpisodeSheetOpen) {
        onEnterHold()
    }
}, onEnterHoldUp = {
    if (!isEpisodeSheetOpen) {
        onEnterHoldUp()
    }
})
