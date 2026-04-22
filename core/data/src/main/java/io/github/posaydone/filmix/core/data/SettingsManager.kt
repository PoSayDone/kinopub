package io.github.posaydone.filmix.core.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("filmix_settings", Context.MODE_PRIVATE)

    companion object {
        private const val VIDEO_QUALITY_KEY = "video_quality"
        private const val DEFAULT_VIDEO_QUALITY = "auto"
        private const val VOICE_TRACK_KEY_PREFIX = "voice_track_"
    }

    fun getVideoQuality(): String {
        return prefs.getString(VIDEO_QUALITY_KEY, DEFAULT_VIDEO_QUALITY) ?: DEFAULT_VIDEO_QUALITY
    }

    fun setVideoQuality(quality: String) {
        prefs.edit().putString(VIDEO_QUALITY_KEY, quality).apply()
    }

    fun saveVoiceTrack(showId: Int, voiceTrack: String) {
        prefs.edit().putString("$VOICE_TRACK_KEY_PREFIX$showId", voiceTrack).apply()
    }

    fun getSavedVoiceTrack(showId: Int): String? {
        return prefs.getString("$VOICE_TRACK_KEY_PREFIX$showId", null)
    }
}