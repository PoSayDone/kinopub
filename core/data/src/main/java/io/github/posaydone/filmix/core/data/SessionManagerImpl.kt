package io.github.posaydone.filmix.core.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.auth0.android.jwt.JWT
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.posaydone.filmix.core.model.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

const val TAG = "SessionManager"

private fun extractExpirationTimeFromJwt(token: String): Long? {
    return try {
        val jwt = JWT(token)
        jwt.expiresAt?.time
    } catch (e: Exception) {
        null
    }
}

@Singleton
class SessionManagerImpl @Inject constructor(@ApplicationContext context: Context) :
    SessionManager {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("FilmixProxyPrefs", Context.MODE_PRIVATE)

    companion object {
        const val IS_LOGGED_IN = "is_logged_in"
        const val USER_TOKEN = "user_token"
        const val USER_REFRESH_TOKEN = "user_refresh_token"
        const val USER_TOKEN_EXPIRES_IN = "user_token_expires_in"
        const val USERNAME = "username"
    }

    override fun saveAccessToken(
        token: String?,
        fallbackExpiration: Long,
    ) {
        val expiration = token?.let { extractExpirationTimeFromJwt(it) } ?: fallbackExpiration
        Log.d(TAG, "saveAccessToken: ${expiration}, $fallbackExpiration")
        prefs.edit() {
            putString(USER_TOKEN, token)
            putLong(USER_TOKEN_EXPIRES_IN, expiration)
        }
    }

    override fun saveRefreshToken(refresh: String) {
        prefs.edit() {
            putString(USER_REFRESH_TOKEN, refresh)
        }
    }

    override fun saveUsername(username: String) {
        prefs.edit() {
            putString(USERNAME, username)
        }
    }

    override fun fetchAccessToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    override fun fetchRefreshToken(): String? {
        return prefs.getString(USER_REFRESH_TOKEN, null)
    }

    override fun fetchUsername(): String? {
        return prefs.getString(USERNAME, null)
    }

    override fun logout() {
        prefs.edit().clear().apply()
    }

    fun fetchTokenExpiresIn(): Long {
        return prefs.getLong(USER_TOKEN_EXPIRES_IN, Long.MIN_VALUE)
    }

    override fun isAccessTokenExpired(): Boolean {
        val currentTimeMills = System.currentTimeMillis()
        return fetchAccessToken() != null && currentTimeMills >= fetchTokenExpiresIn()
    }

    override fun isLoggedIn(): Boolean {
        return !(fetchRefreshToken().isNullOrBlank())
    }
}
