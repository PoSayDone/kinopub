package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerState

@OptIn(UnstableApi::class)
@Composable
fun PlayerEffects(playerState: PlayerState, saveProgress: () -> Unit, pause: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val window = activity.window
    val insetsController = remember(view, window) {
        WindowCompat.getInsetsController(window, view)
    }
    val previousOrientation = remember { activity.requestedOrientation }

    LaunchedEffect(playerState.controlsVisible) {
        // The status bar should never reappear while the player is on screen; only the
        // navigation pill is allowed to follow the controls' visibility.
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (!playerState.controlsVisible) {
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            activity.requestedOrientation = previousOrientation
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Entering Picture-in-Picture always pauses the Activity but
            // only stops it when PiP isn't available, so this naturally skips pausing playback
            // while the video keeps running in the PiP window.
            if (event == Lifecycle.Event.ON_STOP && !activity.isInPictureInPictureMode) {
                saveProgress()
                pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            saveProgress()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
