package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import kotlin.time.Duration

@UnstableApi
@Composable
fun PlayerSeeker(
    modifier: Modifier = Modifier,
    onShowControls: () -> Unit,
    onSeek: (Float) -> Unit,
    contentProgress: Duration,
    contentDuration: Duration,
) {
    val contentProgressString = contentProgress.toComponents { h, m, s, _ ->
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
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    DefaultTimeBar(context).apply {
                        addListener(object : TimeBar.OnScrubListener {
                            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                                onShowControls()
                            }

                            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                            }

                            override fun onScrubStop(
                                timeBar: TimeBar,
                                position: Long,
                                canceled: Boolean,
                            ) {
                                onSeek(position.toFloat())
                            }
                        })
                    }
                },
                update = { timeBar ->
                    timeBar.setDuration(contentDuration.inWholeMilliseconds)
                    timeBar.setPosition(contentProgress.inWholeMilliseconds)
                }
            )
            Text(contentDurationString)
        }
    }
}

private fun Number.padStartWith0() = this.toString().padStart(2, '0')
