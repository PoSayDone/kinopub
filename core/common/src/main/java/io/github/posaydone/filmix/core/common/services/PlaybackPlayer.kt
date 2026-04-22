package io.github.posaydone.filmix.core.common.services

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.OkHttpClient

@UnstableApi
internal fun buildPlaybackPlayer(
    context: Context,
    okHttpClient: OkHttpClient,
): ExoPlayer {
    val dataSourceFactory = DefaultDataSource.Factory(
        context,
        OkHttpDataSource.Factory(okHttpClient),
    )
    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        .setSeekForwardIncrementMs(10000)
        .setSeekBackIncrementMs(10000)
        .build()
        .apply {
            setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }
}
