package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@UnstableApi
@Composable
fun PlayerSeeker(
    modifier: Modifier = Modifier,
    onShowControls: () -> Unit,
    onSeek: (Long) -> Unit,
    contentProgress: Duration,
    contentDuration: Duration,
) {
    var seekingPosition by remember { mutableStateOf<Long?>(null) }

    val contentProgressString = (seekingPosition?.milliseconds ?: contentProgress).toComponents { h, m, s, _ ->
        if (h > 0) {
            "$h:${m.padStartWith0()}:${s.padStartWith0()}"
        } else {
            "${m.padStartWith0()}:${s.padStartWith0()}"
        }
    }
    val contentDurationString = contentDuration.toComponents { h, m, s, _ ->
        if (h > 0) {
            "$h:${m.padStartWith0()}:${s.padStartWith0()}"
        } else {
            "${m.padStartWith0()}:${s.padStartWith0()}"
        }
    }


    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Row {
            Text(contentProgressString)
            AndroidView(modifier = Modifier
                .fillMaxWidth()
                .weight(1f), factory = { context ->
                DefaultTimeBar(context).apply {
                    addListener(object : TimeBar.OnScrubListener {
                        override fun onScrubStart(timeBar: TimeBar, position: Long) {
                            onShowControls()
                            seekingPosition = position
                        }

                        override fun onScrubMove(timeBar: TimeBar, position: Long) {
                            seekingPosition = position
                        }

                        override fun onScrubStop(
                            timeBar: TimeBar,
                            position: Long,
                            canceled: Boolean,
                        ) {
                            seekingPosition = null
                            if (!canceled) {
                                onSeek(position)
                            }
                        }
                    })
                }
            }, update = { timeBar ->
                timeBar.setDuration(contentDuration.inWholeMilliseconds)
                timeBar.setPosition(seekingPosition ?: contentProgress.inWholeMilliseconds)
            })
            Text(contentDurationString)
        }
    }
}

private fun Number.padStartWith0() = this.toString().padStart(2, '0')
