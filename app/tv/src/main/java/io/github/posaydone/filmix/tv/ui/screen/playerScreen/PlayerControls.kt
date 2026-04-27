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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel.Companion.SHOW_CONTROLS_TIME
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerControlsButton
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.PlayerSeeker
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun PlayerControls(
    showType: ShowType?,
    currentPosition: Long,
    playerState: PlayerState,
    duration: Long,
    seekTo: (Long) -> Unit,
    onPlayPauseToggle: () -> Unit,
    onShowControls: (seconds: Int) -> Unit,
    onHideControls: () -> Unit,
    openEpisodeSheet: () -> Unit,
    isAudioTrackSelectionEnabled: Boolean,
    openAudioSheet: () -> Unit,
    openQualitySheet: () -> Unit,
    onPrevEpisodeClick: () -> Unit,
    onNextEpisodeClick: () -> Unit,
    hasNextEpisode: Boolean,
    hasPrevEpisode: Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(
        playerState.controlsVisible
    ) {
        if (playerState.controlsVisible) focusRequester.requestFocus()
    }

    Column {
        PlayerSeeker(
            onShowControls = { onShowControls(it) },
            onSeek = { seekTo(duration.times(it).toLong()) },
            contentProgress = currentPosition.milliseconds,
            contentDuration = duration.milliseconds
        )

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
                if (showType == ShowType.SERIES && hasPrevEpisode) {
                    PlayerControlsButton(
                        icon = Icons.Default.SkipPrevious,
                        onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                        isPlaying = playerState.isPlaying,
                        contentDescription = stringResource(R.string.previous_episode),
                        onClick = onPrevEpisodeClick,
                    )
                }

                // Play/Pause button
                PlayerControlsButton(
                    modifier = Modifier.focusRequester(focusRequester),
                    icon = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                    isPlaying = playerState.isPlaying,
                    contentDescription = if (playerState.isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                    onClick = { onPlayPauseToggle() },
                )

                // Next episode button
                if (showType == ShowType.SERIES && hasNextEpisode) {
                    PlayerControlsButton(
                        icon = Icons.Default.SkipNext,
                        onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                        isPlaying = playerState.isPlaying,
                        contentDescription = stringResource(R.string.next_episode),
                        onClick = onNextEpisodeClick,
                    )
                }

                // All episodes button (icon + text)
                if (showType == ShowType.SERIES) {
                    PlayerControlsButton(
                        icon = Icons.Rounded.AutoAwesomeMotion,
                        onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                        isPlaying = playerState.isPlaying,
                        contentDescription = stringResource(R.string.all_episodes),
                        text = stringResource(R.string.episodesString),
                        onClick = openEpisodeSheet,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAudioTrackSelectionEnabled) {
                    PlayerControlsButton(
                        icon = Icons.Default.Audiotrack,
                        onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                        isPlaying = playerState.isPlaying,
                        contentDescription = stringResource(R.string.audio_tracks),
                        text = stringResource(R.string.audioString),
                        onClick = openAudioSheet,
                    )
                }

                PlayerControlsButton(
                    icon = Icons.Default.Settings,
                    onShowControls = { onShowControls(SHOW_CONTROLS_TIME) },
                    isPlaying = playerState.isPlaying,
                    contentDescription = stringResource(R.string.settings),
                    text = "Settings",
                    onClick = openQualitySheet,
                )
            }
        }
    }
}
