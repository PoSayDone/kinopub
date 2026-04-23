package io.github.posaydone.filmix.core.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("filmix_settings", Context.MODE_PRIVATE)

    companion object {
        private const val VIDEO_QUALITY_KEY = "video_quality"
        private const val DEFAULT_VIDEO_QUALITY = "auto"
        private const val VOICE_TRACK_KEY_PREFIX = "voice_track_"
        private const val HOME_IMMERSIVE_BACKGROUND_ENABLED_KEY = "home_immersive_background_enabled"
        private const val HOME_IMMERSIVE_GRADIENT_ENABLED_KEY = "home_immersive_gradient_enabled"
        private const val HOME_IMMERSIVE_DETAILS_ENABLED_KEY = "home_immersive_details_enabled"
        private const val DEFAULT_HOME_IMMERSIVE_BACKGROUND_ENABLED = true
        private const val DEFAULT_HOME_IMMERSIVE_GRADIENT_ENABLED = true
        private const val DEFAULT_HOME_IMMERSIVE_DETAILS_ENABLED = true
    }

    private val _videoQuality = MutableStateFlow(
        prefs.getString(VIDEO_QUALITY_KEY, DEFAULT_VIDEO_QUALITY) ?: DEFAULT_VIDEO_QUALITY
    )
    val videoQuality: StateFlow<String> = _videoQuality.asStateFlow()

    private val _homeImmersiveBackgroundEnabled = MutableStateFlow(
        prefs.getBoolean(
            HOME_IMMERSIVE_BACKGROUND_ENABLED_KEY,
            DEFAULT_HOME_IMMERSIVE_BACKGROUND_ENABLED
        )
    )
    val homeImmersiveBackgroundEnabled: StateFlow<Boolean> =
        _homeImmersiveBackgroundEnabled.asStateFlow()

    private val _homeImmersiveGradientEnabled = MutableStateFlow(
        if (prefs.contains(HOME_IMMERSIVE_GRADIENT_ENABLED_KEY)) {
            prefs.getBoolean(
                HOME_IMMERSIVE_GRADIENT_ENABLED_KEY,
                DEFAULT_HOME_IMMERSIVE_GRADIENT_ENABLED
            )
        } else {
            prefs.getBoolean(
                HOME_IMMERSIVE_BACKGROUND_ENABLED_KEY,
                DEFAULT_HOME_IMMERSIVE_GRADIENT_ENABLED
            )
        }
    )
    val homeImmersiveGradientEnabled: StateFlow<Boolean> =
        _homeImmersiveGradientEnabled.asStateFlow()

    private val _homeImmersiveDetailsEnabled = MutableStateFlow(
        prefs.getBoolean(
            HOME_IMMERSIVE_DETAILS_ENABLED_KEY,
            DEFAULT_HOME_IMMERSIVE_DETAILS_ENABLED
        )
    )
    val homeImmersiveDetailsEnabled: StateFlow<Boolean> =
        _homeImmersiveDetailsEnabled.asStateFlow()

    fun getVideoQuality(): String {
        return _videoQuality.value
    }

    fun setVideoQuality(quality: String) {
        prefs.edit().putString(VIDEO_QUALITY_KEY, quality).apply()
        _videoQuality.value = quality
    }

    fun isHomeImmersiveBackgroundEnabled(): Boolean {
        return _homeImmersiveBackgroundEnabled.value
    }

    fun setHomeImmersiveBackgroundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(HOME_IMMERSIVE_BACKGROUND_ENABLED_KEY, enabled).apply()
        _homeImmersiveBackgroundEnabled.value = enabled
    }

    fun isHomeImmersiveGradientEnabled(): Boolean {
        return _homeImmersiveGradientEnabled.value
    }

    fun setHomeImmersiveGradientEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(HOME_IMMERSIVE_GRADIENT_ENABLED_KEY, enabled).apply()
        _homeImmersiveGradientEnabled.value = enabled
    }

    fun isHomeImmersiveDetailsEnabled(): Boolean {
        return _homeImmersiveDetailsEnabled.value
    }

    fun setHomeImmersiveDetailsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(HOME_IMMERSIVE_DETAILS_ENABLED_KEY, enabled).apply()
        _homeImmersiveDetailsEnabled.value = enabled
    }

    fun saveVoiceTrack(showId: Int, voiceTrack: String) {
        prefs.edit().putString("$VOICE_TRACK_KEY_PREFIX$showId", voiceTrack).apply()
    }

    fun getSavedVoiceTrack(showId: Int): String? {
        return prefs.getString("$VOICE_TRACK_KEY_PREFIX$showId", null)
    }
}
