package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import android.app.Activity
import android.view.WindowManager
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerError
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerState

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun PlayerOverlay(
    modifier: Modifier = Modifier,
    playerState: PlayerState,
    pulseState: PlayerPulseState,
    onRetry: () -> Unit = {},
    middle: @Composable () -> Unit = {},
    subtitles: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
) {
    val context = LocalContext.current


    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(playerState.controlsVisible, Modifier, fadeIn(), fadeOut()) {
            CinematicBackground(Modifier.fillMaxSize())
        }

        PlayerPulse(pulseState)

        AnimatedVisibility(
            visible = playerState.isSpeedUpActive,
            modifier = Modifier.align(alignment = Alignment.TopCenter),
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
                    .padding(top = 24.dp, bottom = 8.dp),
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
                        .padding(bottom = 24.dp, top = 8.dp)
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
            visible = playerState.isLoading && playerState.playerError == null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }
        }

        if (playerState.playerError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = stringResource(
                            if (playerState.playerError == PlayerError.NETWORK)
                                R.string.player_error_network
                            else
                                R.string.player_error_generic
                        ),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.player_retry))
                    }
                }
            }
        }
    }
}

@Composable
fun CinematicBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color.Black.copy(alpha = 0.18f),
                    0.45f to Color.Black.copy(alpha = 0.58f),
                    1.0f to Color.Black.copy(alpha = 0.96f),
                )
            )
        )
    )
}


@Composable
fun Pill() {
    val transition = rememberInfiniteTransition()
    val offsetX by transition.animateFloat(
        initialValue = 0f, targetValue = 16f, animationSpec = infiniteRepeatable(
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
                modifier = Modifier.offset(x = offsetX.dp)
            )
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.offset(x = offsetX.dp - 16.dp)
            )
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.offset(x = offsetX.dp - 32.dp)
            )
        }
        Text(stringResource(R.string.speed_2x), color = Color.White)
    }
}

@Preview
@Composable
fun Test() {
    Pill()
}
