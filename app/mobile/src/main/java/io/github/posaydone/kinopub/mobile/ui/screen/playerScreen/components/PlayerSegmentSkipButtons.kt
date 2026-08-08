package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.SegmentType

/**
 * Floating "watch"/"skip" buttons shown over the video. Intro/recap group anchors
 * bottom-left, outro bottom-right, mirroring TV's PlayerSegmentSkipButtons. Always
 * visible while a segment is active — when the main controls overlay becomes visible
 * the buttons slide up so they don't overlap the bottom controls row. Not rendered at
 * all once the segment ends — activeSegment naturally goes null as playback position
 * moves past the range.
 */
@Composable
fun BoxScope.PlayerSegmentSkipButtons(
    activeSegment: SegmentType?,
    autoNextEpisodeProgress: Float,
    controlsVisible: Boolean,
    onWatch: () -> Unit,
    onSkip: () -> Unit,
    onNextEpisode: () -> Unit,
) {
    if (activeSegment == null) return
    val isOutro = activeSegment == SegmentType.OUTRO

    val slideOffset by animateDpAsState(
        targetValue = if (controlsVisible) (-144).dp else 0.dp,
        animationSpec = tween(durationMillis = 250),
        label = "segmentButtonsSlide",
    )

    Row(
        modifier = Modifier
            .align(if (isOutro) Alignment.BottomEnd else Alignment.BottomStart)
            .padding(horizontal = 56.dp, vertical = 12.dp)
            .offset(y = slideOffset),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SegmentActionButton(
            text = stringResource(R.string.watch_segment),
            icon = Icons.Default.PlayArrow,
            onClick = onWatch,
        )
        SegmentActionButton(
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
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.1f),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Box {
                // The pill sizes itself to the Row below, so the progress fill can't size
                // itself directly off the incoming (unbounded) constraints — matchParentSize()
                // first pins it to the already-resolved size, then fillMaxWidth(progress)
                // inside that fixed size is a true fraction of it.
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
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        icon,
                        modifier = Modifier.size(20.dp),
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
}
