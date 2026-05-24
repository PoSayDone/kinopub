package io.github.posaydone.kinopub.core.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiUrlManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kinopub_network_settings", Context.MODE_PRIVATE)

    private val _apiUrl = MutableStateFlow(
        prefs.getString(API_URL_KEY, Constants.KINOPUB_API_URL) ?: Constants.KINOPUB_API_URL
    )
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    fun getApiUrl(): String = _apiUrl.value

    fun setApiUrl(url: String) {
        val normalized = normalizeUrl(url)
        prefs.edit().putString(API_URL_KEY, normalized).apply()
        _apiUrl.value = normalized
    }

    fun resetToDefault() {
        setApiUrl(Constants.KINOPUB_API_URL)
    }

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    companion object {
        private const val API_URL_KEY = "api_url"
    }
}
