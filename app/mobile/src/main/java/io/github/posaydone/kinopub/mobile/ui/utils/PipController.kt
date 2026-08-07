package io.github.posaydone.kinopub.mobile.ui.utils

import android.util.Rational
import android.view.View
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Exposes the Activity-owned Picture-in-Picture session (backed by androidx.core:core-pip) to
 * the composition, so the player screen can hand off its state without knowing about the
 * Activity itself.
 */
interface PipController {
    /** True while the Activity is currently shown as a Picture-in-Picture window. */
    val isInPip: Boolean

    /**
     * Updates the Picture-in-Picture session parameters.
     *
     * @param enabled whether the Activity is allowed to auto-enter PiP right now.
     * @param playerView the video surface tracked for the PiP source rect hint, or null to stop
     *   tracking.
     * @param aspectRatio the desired PiP window aspect ratio, or null to use the default.
     */
    fun updatePictureInPicture(
        enabled: Boolean,
        playerView: View?,
        aspectRatio: Rational? = null,
    )
}

private object NoOpPipController : PipController {
    override val isInPip: Boolean = false

    override fun updatePictureInPicture(enabled: Boolean, playerView: View?, aspectRatio: Rational?) {
    }
}

val LocalPipController = staticCompositionLocalOf<PipController> { NoOpPipController }
