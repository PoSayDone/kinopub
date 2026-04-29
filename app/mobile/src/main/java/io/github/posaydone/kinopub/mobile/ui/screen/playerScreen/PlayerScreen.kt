@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowType
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.File
import io.github.posaydone.kinopub.core.model.ShowDetails
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.core.model.Show
import io.github.posaydone.kinopub.core.model.Translation
import io.github.posaydone.kinopub.mobile.ui.common.Loading
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerEffects
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerBottomControls
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerDialogs
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerGestureContainer
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerMediaTitle
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerMiddleControls
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerOverlay
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.rememberPlayerPulseState
import io.github.posaydone.kinopub.mobile.ui.theme.KinopubTheme

private var TAG = "PlayerScreen"

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
    val showType by viewModel.contentType.collectAsState()
    val hasPrevEpisode by viewModel.hasPrevEpisode.collectAsState()
    val hasNextEpisode by viewModel.hasNextEpisode.collectAsState()
    val isHls4AudioTrackSelectionEnabled by viewModel.isHls4AudioTrackSelectionEnabled.collectAsState()
    val context = LocalContext.current
    var isAudioDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isSettingsDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isEpisodeDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    PlayerEffects(playerState = playerState, pause = { viewModel.pause() }, saveProgress = {
        viewModel.saveProgress()
    })

    PlayerScreenContent(
        playerState = playerState,
        showDetails = showDetails,
        showType = showType,
        selectedSeason = selectedSeason,
        selectedEpisode = selectedEpisode,
        hasPrevEpisode = hasPrevEpisode,
        hasNextEpisode = hasNextEpisode,
        isAudioTrackSelectionEnabled = isHls4AudioTrackSelectionEnabled,
        onRetry = { viewModel.retryPlayback() },
        toggleControls = { viewModel.toggleControls() },
        setResizeMode = { viewModel.setResizeMode(it) },
        seekForward = { viewModel.seekForward() },
        seekBack = { viewModel.seekBack() },
        enableSpeedUp = { viewModel.enableSpeedUp() },
        disableSpeedUp = { viewModel.disableSpeedUp() },
        seekTo = { viewModel.seekTo(it) },
        onPlayPauseClick = { viewModel.onPlayPauseClick() },
        onNextEpisodeClick = { viewModel.goToNextEpisode() },
        onPrevEpisodeClick = { viewModel.goToPrevEpisode() },
        onShowControls = { viewModel.showControls(seconds = 4) },
        openSettingsDialog = { isSettingsDialogOpen = true },
        openEpisodeDialog = {
            player.pause()
            viewModel.hideControls()
            isEpisodeDialogOpen = true
        },
        openAudioDialog = {
            if (isHls4AudioTrackSelectionEnabled) {
                isAudioDialogOpen = true
            }
        },
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
                viewModel = viewModel,
                showDetails = showDetails,
                isEpisodeDialogOpen = isEpisodeDialogOpen,
                isAudioDialogOpen = isAudioDialogOpen,
                isSettingsDialogOpen = isSettingsDialogOpen,
                closeEpisodeDialog = { isEpisodeDialogOpen = false },
                closeAudioDialog = { isAudioDialogOpen = false },
                closeSettingsDialog = { isSettingsDialogOpen = false }
            )
        }
    )
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerScreenContent(
    playerState: PlayerState,
    showDetails: ShowDetails,
    showType: ShowType?,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    hasPrevEpisode: Boolean,
    hasNextEpisode: Boolean,
    isAudioTrackSelectionEnabled: Boolean,
    onRetry: () -> Unit = {},
    toggleControls: () -> Unit,
    setResizeMode: (Int) -> Unit,
    seekForward: () -> Unit,
    seekBack: () -> Unit,
    enableSpeedUp: () -> Unit,
    disableSpeedUp: () -> Unit,
    seekTo: (Long) -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    onPrevEpisodeClick: () -> Unit,
    onShowControls: () -> Unit,
    openSettingsDialog: () -> Unit,
    openEpisodeDialog: () -> Unit,
    openAudioDialog: () -> Unit,
    playerSurface: @Composable BoxScope.() -> Unit,
    dialogs: @Composable () -> Unit = {},
) {
    val pulseState = rememberPlayerPulseState()

    Box(
        Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .focusable()
    ) {
        playerSurface()

        PlayerGestureContainer(
            toggleControls = toggleControls,
            setResizeMode = setResizeMode,
            setPulseType = { pulseState.setType(it) },
            seekForward = seekForward,
            seekBack = seekBack,
            enableSpeedUp = enableSpeedUp,
            disableSpeedUp = disableSpeedUp
        )


        PlayerOverlay(
            modifier = Modifier.fillMaxSize(),
            playerState = playerState,
            pulseState = pulseState,
            onRetry = onRetry,
            subtitles = { /* TODO Implement subtitles */ },
            header = {
                PlayerMediaTitle(
                    showDetails = showDetails,
                    currentSeason = if (showType == ShowType.SERIES && selectedSeason != null) stringResource(
                        R.string.seasonString
                    ) + " " + selectedSeason.season else null,
                    currentEpisode = if (showType == ShowType.SERIES && selectedEpisode != null) stringResource(
                        R.string.episode,
                        selectedEpisode.episode
                    ) else null,
                    openSettingsDialog = openSettingsDialog
                )
            },
            middle = {
                PlayerMiddleControls(
                    showType = showType,
                    isPlaying = playerState.isPlaying,
                    isLoading = playerState.isLoading,
                    onPlayPauseClick = onPlayPauseClick,
                    hasNextEpisode = hasNextEpisode,
                    onNextEpisodeClick = onNextEpisodeClick,
                    hasPrevEpisode = hasPrevEpisode,
                    onPrevEpisodeClick = onPrevEpisodeClick,
                )
            },
            footer = {
                PlayerBottomControls(
                    showType = showType,
                    resizeMode = playerState.resizeMode,
                    setResizeMode = setResizeMode,
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    seekTo = seekTo,
                    onShowControls = onShowControls,
                    openEpisodeDialog = openEpisodeDialog,
                    isAudioTrackSelectionEnabled = isAudioTrackSelectionEnabled,
                    openAudioDialog = openAudioDialog,
                )
            })
    }
    dialogs()
}

@UnstableApi
@Preview(name = "Player Screen", widthDp = 960, heightDp = 540, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerScreenPreview() {
    val previewEpisode = Episode(
        episode = 4,
        ad_skip = 0,
        title = "The Test of Time",
        released = "2026-04-01",
        translations = mutableListOf(
            Translation(
                translation = "Original",
                files = listOf(
                    File(
                        url = "https://example.com/preview.m3u8",
                        quality = 1080,
                        proPlus = false
                    )
                )
            )
        ),
    )
    val previewSeason = Season(
        season = 2,
        episodes = mutableListOf(previewEpisode)
    )

    KinopubTheme(darkTheme = true) {
        PlayerScreenContent(
            playerState = PlayerState(
                isPlaying = true,
                isLoading = false,
                currentPosition = 1_548_000L,
                duration = 3_120_000L,
                controlsVisible = true
            ),
            showDetails = Show(
                id = 1,
                title = "Автар: Легенда об Аанге",
                originalTitle = "Avatar: The last air-bender",
                poster = "",
                backdropUrl = null,
                year = 2023,
                description = "Preview content",
                isSeries = true,
            ),
            showType = ShowType.SERIES,
            selectedSeason = previewSeason,
            selectedEpisode = previewEpisode,
            hasPrevEpisode = true,
            hasNextEpisode = true,
            isAudioTrackSelectionEnabled = true,
            toggleControls = {},
            setResizeMode = {},
            seekForward = {},
            seekBack = {},
            enableSpeedUp = {},
            disableSpeedUp = {},
            seekTo = {},
            onPlayPauseClick = {},
            onNextEpisodeClick = {},
            onPrevEpisodeClick = {},
            onShowControls = {},
            openSettingsDialog = {},
            openEpisodeDialog = {},
            openAudioDialog = {},
            playerSurface = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF10141A),
                                    Color(0xFF1A2634),
                                    Color(0xFF06080B)
                                )
                            )
                        )
                )
            }
        )
    }
}
