package io.github.posaydone.kinopub.core.common.services

import android.app.PendingIntent
import android.content.Intent
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
        val sessionBuilder = MediaSession.Builder(this, player)
        createSessionActivityPendingIntent()?.let { sessionBuilder.setSessionActivity(it) }
        mediaSession = sessionBuilder.build()
    }

    private fun createSessionActivityPendingIntent(): PendingIntent? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        } ?: return null
        return PendingIntent.getActivity(
            this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
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
