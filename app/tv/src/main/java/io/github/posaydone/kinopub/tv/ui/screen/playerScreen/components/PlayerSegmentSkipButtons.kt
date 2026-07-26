package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.SegmentType
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

/**
 * Floating "watch"/"skip" buttons shown over the video while the controls overlay is
 * hidden. Intro/recap group anchors bottom-left, outro bottom-right, per the source
 * request. Not rendered at all once the segment ends — activeSegment naturally goes
 * null as playback position moves past the range, so no manual dismissal timer needed.
 * Sized to match the overlay's PlayerControlsButton (56dp pill) so both look consistent.
 */
@Composable
fun BoxScope.PlayerSegmentSkipButtons(
    activeSegment: SegmentType?,
    autoNextEpisodeProgress: Float,
    onWatch: () -> Unit,
    onSkip: () -> Unit,
    onNextEpisode: () -> Unit,
) {
    if (activeSegment == null) return
    val isOutro = activeSegment == SegmentType.OUTRO

    val actionFocusRequester = remember(activeSegment) { FocusRequester() }

    LaunchedEffect(activeSegment) {
        runCatching { actionFocusRequester.requestFocus() }
    }

    Row(
        modifier = Modifier
            .align(if (isOutro) Alignment.BottomEnd else Alignment.BottomStart)
            .padding(horizontal = 56.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SegmentActionButton(
            text = stringResource(R.string.watch_segment),
            icon = Icons.Default.PlayArrow,
            onClick = onWatch,
        )
        SegmentActionButton(
            modifier = Modifier.focusRequester(actionFocusRequester),
            text = stringResource(if (isOutro) R.string.next_episode else R.string.skip_segment),
            icon = if (isOutro) Icons.Default.SkipNext else Icons.Default.FastForward,
            progress = if (isOutro) autoNextEpisodeProgress else 0f,
            onClick = if (isOutro) onNextEpisode else onSkip,
        )
    }
}

@Composable
private fun SegmentActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
) {
    Surface(
        modifier = modifier.height(56.dp),
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
    ) {
        Box {
            // The pill sizes itself to the Row below, so the progress fill can't size
            // itself directly off the incoming (unbounded) constraints — that made
            // fillMaxWidth(progress) grow the whole button as progress increased.
            // matchParentSize() first pins it to the Row's already-resolved size, then
            // fillMaxWidth(progress) inside that fixed size is a true fraction of it.
            if (progress > 0f) {
                Box(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    modifier = Modifier.size(28.dp),
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalContentColor.current
                )
            }
        }
    }
}
