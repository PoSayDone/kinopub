@file:kotlin.OptIn(
    ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class
)

package io.github.posaydone.filmix.tv.ui.screen.playerScreen

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerDialogs
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerEffects
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerMediaTitle
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerOverlay
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerPulse
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerPulseState
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.rememberPlayerPulseState
import io.github.posaydone.filmix.tv.ui.utils.handleDPadKeyEvents

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
    showDetails: FullShow,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
) {
    val context = LocalContext.current
    val showType by viewModel.contentType.collectAsState()
    val hasPrevEpisode by viewModel.hasPrevEpisode.collectAsState()
    val hasNextEpisode by viewModel.hasNextEpisode.collectAsState()
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
        playerState = playerState,
        pulseState = pulseState,
        onShowControls = { viewModel.showControls(it) },
        saveProgress = { viewModel.saveProgress() },
        pause = { viewModel.pause() })

    Box(
        Modifier
            .dPadEvents(
                playerState = playerState,
                pulseState = pulseState,
                seekBack = { viewModel.seekBack() },
                seekForward = { viewModel.seekForward() },
                pause = { viewModel.pause() },
                isEpisodeSheetOpen = isEpisodeDialogOpen,
                onShowControls = { viewModel.showControls() },
                onEnterHold = { viewModel.enableSpeedUp() },
                onEnterHoldUp = { viewModel.disableSpeedUp() }
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


        PlayerOverlay(
            modifier = Modifier.fillMaxSize(),
            playerState = playerState,
            centerButton = { PlayerPulse(pulseState, playerState.isLoading) },
            subtitles = { /* TODO Implement subtitles */ },
            header = {
                PlayerMediaTitle(
                    showDetails = showDetails,
                    currentSeason = if (showType == ShowType.SERIES && selectedSeason != null) "Season ${selectedSeason!!.season}" else null,
                    currentEpisode = if (showType == ShowType.SERIES && selectedEpisode != null) "Episode ${selectedEpisode!!.episode}" else null
                )
            },
            controls = {
                PlayerControls(
                    showType = showType,
                    playerState = playerState,
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    onShowControls = { viewModel.showControls(seconds = 4) },
                    onHideControls = { viewModel.hideControls() },
                    openEpisodeSheet = {
                        viewModel.pause(); viewModel.hideControls(); isEpisodeDialogOpen = true
                    },
                    openAudioSheet = { isAudioDialogOpen = true },
                    openQualitySheet = { isQualityDialogOpen = true },
                    hasNextEpisode = hasNextEpisode,
                    hasPrevEpisode = hasPrevEpisode,
                    onPrevEpisodeClick = { viewModel.goToPrevEpisode() },
                    onNextEpisodeClick = { viewModel.goToNextEpisode() },
                    seekTo = { viewModel.seekTo(it) },
                    onPlayPauseToggle = { viewModel.onPlayPauseClick() })
            })


        PlayerDialogs(
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
