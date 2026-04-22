package io.github.posaydone.filmix.core.network

data class KinoPubAuthConfig(
    val clientId: String,
    val clientSecret: String,
) {
    fun requireConfigured() {
        check(clientId.isNotBlank() && clientSecret.isNotBlank()) {
            "Missing KinoPub OAuth credentials. Set KINOPUB_CLIENT_ID and KINOPUB_CLIENT_SECRET in local.properties, gradle.properties, or the environment."
        }
    }
}
