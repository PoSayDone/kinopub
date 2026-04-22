package io.github.posaydone.filmix.core.model.kinopub

data class KinoPubDeviceCodeResponse(
    val code: String,
    val user_code: String,
    val verification_uri: String,
    val expires_in: Long,
    val interval: Long,
)

data class KinoPubAuthErrorResponse(
    val error: String,
    val error_description: String? = null,
)
