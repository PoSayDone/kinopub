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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerDialogs
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerMediaTitle
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerOverlay
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerPulse
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerPulseState
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.rememberVideoPlayerPulseState
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.rememberVideoPlayerState
import io.github.posaydone.filmix.tv.ui.utils.handleDPadKeyEvents

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


        PlayerOverlay(
            modifier = Modifier.fillMaxSize(),
            state = videoPlayerState,
            isPlaying = player.isPlaying,
            pulseState = pulseState,
            centerButton = { PlayerPulse(pulseState, playerState.isLoading) },
            subtitles = { /* TODO Implement subtitles */ },
            header = {
                PlayerMediaTitle(
                    showDetails = showDetails,
                    currentSeason = if (showType == ShowType.SERIES) "Season 1" else null,  // Will be updated with real data
                    currentEpisode = if (showType == ShowType.SERIES) "Episode 1" else null
                )
            },
            controls = {
                VideoPlayerControls(
                    isPlaying = player.isPlaying,
                    showType = showType,
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    player = player,
                    state = videoPlayerState,
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


private fun Modifier.dPadEvents(
    seekBack: () -> Unit,
    seekForward: () -> Unit,
    pause: () -> Unit,
    isEpisodeSheetOpen: Boolean,
    videoPlayerState: io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.VideoPlayerState,
    pulseState: VideoPlayerPulseState,
): Modifier = this.handleDPadKeyEvents(onLeft = {
    if (!videoPlayerState.controlsVisible && !isEpisodeSheetOpen) {
        seekBack()
        pulseState.setType(PlayerPulse.Type.BACK)
    }
}, onRight = {
    if (!videoPlayerState.controlsVisible && !isEpisodeSheetOpen) {
        seekForward()
        pulseState.setType(PlayerPulse.Type.FORWARD)
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
