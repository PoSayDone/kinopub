package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType

@Composable
fun PlayerMiddleControls(
    showType: ShowType?,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPauseClick: () -> Unit,
    hasNextEpisode: Boolean,
    onNextEpisodeClick: () -> Unit,
    hasPrevEpisode: Boolean,
    onPrevEpisodeClick: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 48.dp, alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showType != ShowType.MOVIE) {
            Box(
                modifier = Modifier.then(
                    if (hasPrevEpisode) {
                        Modifier.clickable(
                            onClick = onPrevEpisodeClick,
                            role = Role.Button,
                            interactionSource = interactionSource,
                            indication = ripple(bounded = false, radius = 24.dp)
                        )
                    } else {
                        Modifier.semantics { disabled() }
                    }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous episode",
                    tint = Color.White.copy(alpha = if (hasPrevEpisode) 1f else 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }
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
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        if (showType != ShowType.MOVIE) {
            Box(
                modifier = Modifier.then(
                    if (hasNextEpisode) {
                        Modifier.clickable(
                            onClick = onNextEpisodeClick,
                            role = Role.Button,
                            interactionSource = interactionSource,
                            indication = ripple(bounded = false, radius = 24.dp)
                        )
                    } else {
                        Modifier.semantics { disabled() }
                    }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next episode",
                    tint = Color.White.copy(alpha = if (hasNextEpisode) 1f else 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
