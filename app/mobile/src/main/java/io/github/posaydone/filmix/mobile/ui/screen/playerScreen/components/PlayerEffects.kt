package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
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
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState

@OptIn(UnstableApi::class)
@Composable
fun PlayerEffects(playerState: PlayerState, saveProgress: () -> Unit, pause: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)
    val previousOrientation = remember { activity?.requestedOrientation }


    if (!view.isInEditMode) {
        if (!playerState.controlsVisible) {
            insetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            insetsController.apply {
                show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }


    LaunchedEffect(playerState.isPlaying) {
        if (!playerState.isPlaying) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(Unit) {
        // Lock to landscape mode (both landscape-left and landscape-right)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            // Restore previous orientation
            previousOrientation?.let {
                activity?.requestedOrientation = it
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    saveProgress()
                    pause()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            saveProgress()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}