package io.github.posaydone.filmix.tv.ui.screen.playerScreen

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerControlsButton
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerSeeker
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    showType: ShowType?,
    currentPosition: Long,
    duration: Long,
    player: MediaController,
    onShowControls: () -> Unit,
    onHideControls: () -> Unit,
    openEpisodeSheet: () -> Unit,
    openAudioSheet: () -> Unit,
    openQualitySheet: () -> Unit,
    onPrevEpisodeClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    hasNextEpisode: Boolean,
    hasPrevEpisode: Boolean,
) {
    val focusRequester = remember { FocusRequester() }
    
    val onPlayPauseToggle = { shouldPlay: Boolean ->
        if (shouldPlay) {
            player.play()
        } else {
            player.pause()
        }
    }

    Column {
        PlayerSeeker(
            onShowControls = onShowControls,
            onSeek = { player.seekTo(player.duration.times(it).toLong()) },
            contentProgress = currentPosition.milliseconds,
            contentDuration = duration.milliseconds
        )

        // New controls row below seeker
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Previous episode button
                if (showType == ShowType.SERIES && hasPrevEpisode) {
                    PlayerControlsButton(
                        icon = Icons.Default.SkipPrevious,
                        onShowControls = onShowControls,
                        isPlaying = isPlaying,
                        contentDescription = "Previous episode",
                        onClick = onPrevEpisodeClick,
                    )
                }

                // Play/Pause button
                PlayerControlsButton(
                    modifier = Modifier.focusRequester(focusRequester),
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    onShowControls = onShowControls,
                    isPlaying = isPlaying,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = { onPlayPauseToggle(!isPlaying) },
                )

                // Next episode button
                if (showType == ShowType.SERIES && hasNextEpisode) {
                    PlayerControlsButton(
                        icon = Icons.Default.SkipNext,
                        onShowControls = onShowControls,
                        isPlaying = isPlaying,
                        contentDescription = "Next episode",
                        onClick = onNextEpisodeClick,
                    )
                }

                // All episodes button (icon + text)
                if (showType == ShowType.SERIES) {
                    PlayerControlsButton(
                        icon = Icons.Rounded.AutoAwesomeMotion,
                        onShowControls = onShowControls,
                        isPlaying = isPlaying,
                        contentDescription = "All episodes",
                        text = "Episodes",
                        onClick = openEpisodeSheet,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayerControlsButton(
                    icon = Icons.Default.Audiotrack,
                    onShowControls = onShowControls,
                    isPlaying = isPlaying,
                    contentDescription = "Audio tracks",
                    text = "Audio",
                    onClick = openAudioSheet,
                )

                PlayerControlsButton(
                    icon = Icons.Default.Settings,
                    onShowControls = onShowControls,
                    isPlaying = isPlaying,
                    contentDescription = "Settings",
                    text = "Settings",
                    onClick = openQualitySheet,
                )
            }
        }
    }
}