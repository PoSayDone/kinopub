package io.github.posaydone.kinopub.core.data

import com.google.gson.Gson
import io.github.posaydone.kinopub.core.model.AuthResponse
import io.github.posaydone.kinopub.core.model.DeviceAuthorizationStatus
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubAuthErrorResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubDeviceCodeResponse
import io.github.posaydone.kinopub.core.network.KinoPubAuthConfig
import io.github.posaydone.kinopub.core.network.service.AuthService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val authConfig: KinoPubAuthConfig,
) {
    private val gson = Gson()

    suspend fun requestDeviceCode(): KinoPubDeviceCodeResponse {
        authConfig.requireConfigured()
        val response = authService.requestDeviceCode(
            clientId = authConfig.clientId,
            clientSecret = authConfig.clientSecret,
        )
        check(response.isSuccessful && response.body() != null) {
            parseErrorMessage(response.errorBody()?.string()) ?: "Failed to start device authorization."
        }
        return response.body()!!
    }

    suspend fun pollDeviceCode(code: String): DeviceAuthorizationStatus {
        authConfig.requireConfigured()
        val response = authService.pollDeviceCode(
            code = code,
            clientId = authConfig.clientId,
            clientSecret = authConfig.clientSecret,
        )
        if (response.isSuccessful && response.body() != null) {
            return DeviceAuthorizationStatus.Authorized(response.body()!!)
        }
        val authError = parseAuthError(response.errorBody()?.string())
        return when (authError?.error) {
            "authorization_pending" -> DeviceAuthorizationStatus.Pending
            "slow_down" -> DeviceAuthorizationStatus.SlowDown(5)
            "expired_token", "expired_code" -> DeviceAuthorizationStatus.Expired
            else -> DeviceAuthorizationStatus.Failed(
                authError?.error_description ?: authError?.error ?: "Device authorization failed."
            )
        }
    }

    suspend fun refresh(refreshToken: String): AuthResponse {
        authConfig.requireConfigured()
        val response = authService.refresh(
            refreshToken = refreshToken,
            clientId = authConfig.clientId,
            clientSecret = authConfig.clientSecret,
        )
        check(response.isSuccessful && response.body() != null) {
            parseErrorMessage(response.errorBody()?.string()) ?: "Failed to refresh access token."
        }
        return response.body()!!
    }

    suspend fun logout() {
        authService.logout()
    }

    private fun parseAuthError(rawError: String?): KinoPubAuthErrorResponse? {
        if (rawError.isNullOrBlank()) return null
        return runCatching { gson.fromJson(rawError, KinoPubAuthErrorResponse::class.java) }.getOrNull()
    }

    private fun parseErrorMessage(rawError: String?): String? =
        parseAuthError(rawError)?.let { it.error_description ?: it.error }
}
