package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun PlayerOverlay(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    playerState: PlayerState, // Updated to use shared PlayerState
    pulseState: PlayerPulseState,
    onShowControls: () -> Unit = {}, // Callback to show controls
    onHideControls: () -> Unit = {}, // Callback to hide controls
    middle: @Composable () -> Unit = {},
    subtitles: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val window = (context as Activity).window

    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            onShowControls() // Show controls when not playing
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(playerState.controlsVisible, Modifier, fadeIn(), fadeOut()) {
            CinematicBackground(Modifier.fillMaxSize())
        }

        PlayerPulse(pulseState)

        AnimatedVisibility(
            playerState.controlsVisible,
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.TopCenter),
            enter = slideInVertically(
                initialOffsetY = {
                    -it
                },
            ),
            exit = slideOutVertically(
                targetOffsetY = {
                    -it
                },
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp)
                    .padding(top = 32.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                header()
            }
        }

        Column {
            Box(
                Modifier.weight(1f), contentAlignment = Alignment.BottomCenter
            ) {
                subtitles()
            }

            AnimatedVisibility(
                playerState.controlsVisible,
                Modifier,
                slideInVertically { it },
                slideOutVertically { it }) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 56.dp)
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    footer()
                }
            }
        }

        AnimatedVisibility(
            visible = playerState.controlsVisible, enter = fadeIn(), exit = fadeOut()
        ) {
            middle()
        }

        AnimatedVisibility(
            visible = playerState.isLoading, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun CinematicBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.9f)
                )
            )
        )
    )
}

