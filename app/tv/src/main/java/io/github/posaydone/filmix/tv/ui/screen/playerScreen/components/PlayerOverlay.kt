package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme


/**
 * Handles the visibility and animation of the controls.
 */
@Composable
fun PlayerOverlay(
    isPlaying: Boolean,
    pulseState: VideoPlayerPulseState,
    modifier: Modifier = Modifier,
    state: VideoPlayerState = rememberVideoPlayerState(),
    centerButton: @Composable () -> Unit = {},
    subtitles: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    controls: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val window = (context as Activity).window

    LaunchedEffect(isPlaying) {
        if (!isPlaying && pulseState.type == PlayerPulse.Type.NONE) {
            state.showControls(seconds = Int.MAX_VALUE)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(state.controlsVisible, Modifier, fadeIn(), fadeOut()) {
            CinematicBackground(Modifier.fillMaxSize())
        }

        AnimatedVisibility(
            state.controlsVisible,
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.TopCenter),
            enter = slideInVertically(
                initialOffsetY = {
                    it / 2
                },
            ),
            exit = slideOutVertically(
                targetOffsetY = {
                    it / 2
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
                state.controlsVisible,
                Modifier,
                slideInVertically { it },
                slideOutVertically { it }) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 56.dp)
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    controls()
                }
            }
        }
        centerButton()
    }
}

@Composable
fun CinematicBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier.background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        )
    )
}

