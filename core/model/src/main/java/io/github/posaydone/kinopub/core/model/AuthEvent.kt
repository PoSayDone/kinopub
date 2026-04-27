package io.github.posaydone.kinopub.core.model

/**
 * Defines global authentication events that can be broadcast across the app.
 */
sealed interface AuthEvent {
    /**
     * Fired when a token refresh fails and the user must be logged out and sent to the auth screen.
     */
    object ForceLogout : AuthEvent
}
