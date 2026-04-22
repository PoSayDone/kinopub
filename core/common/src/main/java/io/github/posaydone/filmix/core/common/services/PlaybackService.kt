package io.github.posaydone.filmix.core.common.services

import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "PlaybackService"

@AndroidEntryPoint
@UnstableApi
class PlaybackService : MediaSessionService() {
    @Inject
    @Named("playbackOkHttpClient")
    lateinit var okHttpClient: OkHttpClient

    private var mediaSession: MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onCreate() {
        super.onCreate()
        val player = buildPlaybackPlayer(this, okHttpClient)
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
