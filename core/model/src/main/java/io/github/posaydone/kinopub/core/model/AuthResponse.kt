package io.github.posaydone.kinopub.core.model

data class AuthResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long,
    val token_type: String? = null,
    val scope: String? = null,
)

sealed interface DeviceAuthorizationStatus {
    data object Pending : DeviceAuthorizationStatus
    data class SlowDown(val retryAfterSeconds: Long) : DeviceAuthorizationStatus
    data class Authorized(val response: AuthResponse) : DeviceAuthorizationStatus
    data object Expired : DeviceAuthorizationStatus
    data class Failed(val message: String) : DeviceAuthorizationStatus
}
