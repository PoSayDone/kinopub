package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowType
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun PlayerBottomControls(
    showType: ShowType?,
    resizeMode: Int,
    setResizeMode: (resizeMode: Int) -> Unit,
    currentPosition: Long,
    duration: Long,
    seekTo: (time: Long) -> Unit,
    onShowControls: () -> Unit,
    openEpisodeDialog: () -> Unit,
    isAudioTrackSelectionEnabled: Boolean,
    openAudioDialog: () -> Unit,
) {
    Column {
        PlayerSeeker(
            modifier = Modifier.Companion.weight(1f),
            onShowControls = onShowControls,
            onSeek = { seekTo(it) },
            contentProgress = currentPosition.milliseconds,
            contentDuration = duration.milliseconds
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showType == ShowType.SERIES) {
                    PlayerControlsButton(
                        icon = Icons.Rounded.AutoAwesomeMotion,
                        contentDescription = stringResource(R.string.all_episodes),
                        text = stringResource(R.string.episodesString),
                        onClick = openEpisodeDialog,
                    )
                }
                if (isAudioTrackSelectionEnabled) {
                    PlayerControlsButton(
                        icon = Icons.Default.Audiotrack,
                        contentDescription = stringResource(R.string.audio_tracks),
                        text = stringResource(R.string.audioString),
                        onClick = openAudioDialog,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                PlayerControlsButton(
                    icon = Icons.Default.AspectRatio,
                    contentDescription = stringResource(R.string.aspect_ratio),
                    onClick = {
                        when (resizeMode) {
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> {
                                setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
                            }

                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                                setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                            }

                            else -> {
                                setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                            }
                        }
                    },
                )
            }
        }
    }
}
