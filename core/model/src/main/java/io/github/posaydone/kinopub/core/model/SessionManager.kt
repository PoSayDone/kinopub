package io.github.posaydone.kinopub.core.model

interface SessionManager {
    fun saveAccessToken(token: String?, fallbackExpiration: Long)
    fun saveRefreshToken(refresh: String)
    fun saveUsername(username: String)
    fun fetchAccessToken(): String?
    fun fetchRefreshToken(): String?
    fun fetchUsername(): String?
    fun isAccessTokenExpired(): Boolean
    fun isLoggedIn(): Boolean
    fun logout()
}
