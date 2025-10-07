package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

import android.app.Activity
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState


/**
 * Handles the visibility and animation of the controls.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerOverlay(
    modifier: Modifier = Modifier,
    playerState: PlayerState,
    centerButton: @Composable () -> Unit = {},
    subtitles: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    controls: @Composable () -> Unit = {},
) {

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(playerState.controlsVisible, Modifier, fadeIn(), fadeOut()) {
                CinematicBackground(Modifier.fillMaxSize())
            }

            AnimatedVisibility(
                visible = playerState.isSpeedUpActive,
                modifier = Modifier
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
                Pill()
            }

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
                        controls()
                    }
                }
            }
            centerButton()
        }
    }
}

@Composable
fun Pill() {
    val transition = rememberInfiniteTransition()
    val offsetX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Row(
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clipToBounds()
        ) {
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .offset(x = offsetX.dp)
            )
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .offset(x = offsetX.dp - 16.dp)
            )
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .offset(x = offsetX.dp - 32.dp)
            )
        }
        Text("2x", color = Color.White)
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
