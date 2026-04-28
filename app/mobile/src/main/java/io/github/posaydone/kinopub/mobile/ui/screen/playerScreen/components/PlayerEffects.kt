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
        if (!playerState.controlsVisible) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            activity.requestedOrientation = previousOrientation
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
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
