package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.github.posaydone.kinopub.core.common.R

@Composable
fun PlayerMiddleControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPauseClick: () -> Unit,
    seekForward: () -> Unit,
    seekBack: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 48.dp, alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.clickable(
                onClick = seekBack,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp)
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Replay10,
                contentDescription = stringResource(R.string.seek_back),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Box(
            modifier = Modifier.clickable(
                onClick = onPlayPauseClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 32.dp)
            ),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                Spacer(Modifier.size(48.dp)) // Keeps layout consistent
            }
            this@Row.AnimatedVisibility(visible = !isLoading, enter = fadeIn(), exit = fadeOut()) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Box(
            modifier = Modifier.clickable(
                onClick = seekForward,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp)
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = stringResource(R.string.seek_forward),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
