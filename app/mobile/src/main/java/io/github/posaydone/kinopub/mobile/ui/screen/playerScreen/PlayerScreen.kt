@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowType
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.ShowDetails
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.mobile.ui.common.Loading
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerEffects
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerBottomControls
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerDialogs
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerGestureContainer
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerMediaTitle
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerMiddleControls
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.PlayerOverlay
import io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.rememberPlayerPulseState

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
    val pulseState = rememberPlayerPulseState()
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

    Box(
        Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .focusable()
    ) {
        AndroidView(
            factory = { PlayerView(context).apply { useController = false } }, update = {
                it.player = player
                it.apply {
                    resizeMode = playerState.resizeMode
                    keepScreenOn = playerState.isPlaying
                }
            }, modifier = Modifier.fillMaxSize()
        )

        PlayerGestureContainer(
            toggleControls = { viewModel.toggleControls() },
            setResizeMode = { viewModel.setResizeMode(it) },
            setPulseType = { pulseState.setType(it) },
            seekForward = { viewModel.seekForward() },
            seekBack = { viewModel.seekBack() },
            enableSpeedUp = { viewModel.enableSpeedUp() },
            disableSpeedUp = { viewModel.disableSpeedUp() }
        )


        PlayerOverlay(
            modifier = Modifier.fillMaxSize(),
            playerState = playerState,
            pulseState = pulseState,
            subtitles = { /* TODO Implement subtitles */ },
            header = {
                PlayerMediaTitle(
                    showDetails = showDetails,
                    currentSeason = if (showType == ShowType.SERIES && selectedSeason != null) stringResource(
                        R.string.seasonString
                    ) + " " + selectedSeason!!.season.toString() else null,
                    currentEpisode = if (showType == ShowType.SERIES && selectedEpisode != null) stringResource(
                        R.string.episode,
                        selectedEpisode!!.episode
                    ) else null,
                    openSettingsDialog = { isSettingsDialogOpen = true }
                )
            },
            middle = {
                PlayerMiddleControls(
                    showType = showType,
                    isPlaying = playerState.isPlaying,
                    isLoading = playerState.isLoading,
                    onPlayPauseClick = { viewModel.onPlayPauseClick() },
                    hasNextEpisode = hasNextEpisode,
                    onNextEpisodeClick = { viewModel.goToNextEpisode() },
                    hasPrevEpisode = hasPrevEpisode,
                    onPrevEpisodeClick = { viewModel.goToPrevEpisode() },
                )
            },
            footer = {
                PlayerBottomControls(
                    showType = showType,
                    resizeMode = playerState.resizeMode,
                    setResizeMode = { viewModel.setResizeMode(it) },
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    seekTo = { viewModel.seekTo(it) },
                    onShowControls = { viewModel.showControls(seconds = 4) },
                    openEpisodeDialog = {
                        player.pause(); viewModel.hideControls(); isEpisodeDialogOpen = true
                    },
                    isAudioTrackSelectionEnabled = isHls4AudioTrackSelectionEnabled,
                    openAudioDialog = {
                        if (isHls4AudioTrackSelectionEnabled) {
                            isAudioDialogOpen = true
                        }
                    },
                )
            })
    }
    PlayerDialogs(
        viewModel = viewModel,
        showDetails = showDetails,
        isEpisodeDialogOpen = isEpisodeDialogOpen,
        isAudioDialogOpen = isAudioDialogOpen,
        isSettingsDialogOpen = isSettingsDialogOpen,
        closeEpisodeDialog = { isEpisodeDialogOpen = false },
        closeAudioDialog = { isAudioDialogOpen = false },
        closeSettingsDialog = { isSettingsDialogOpen = false })
}
