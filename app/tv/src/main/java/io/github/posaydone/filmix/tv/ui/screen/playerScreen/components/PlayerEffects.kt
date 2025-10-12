package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerState
import kotlinx.coroutines.runBlocking

private val TAG = "EFFECTS"

@OptIn(UnstableApi::class)
@Composable
fun PlayerEffects(
    lifecycleOwner: LifecycleOwner,
    playerState: PlayerState,
    pulseState: PlayerPulseState,
    onShowControls: (Int) -> Unit,
    saveProgress: () -> Unit,
    pause: () -> Unit,
) {
    LaunchedEffect(playerState.isPlaying) {
        if (!playerState.isPlaying && pulseState.type == PlayerPulse.Type.NONE) {
            onShowControls(Int.MAX_VALUE)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "PlayerEffects: pause")
                    saveProgress()
                    pause()
                    Log.d(TAG, "PlayerEffects: pause finished")
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d(TAG, "PlayerEffects: Dispose")
            saveProgress()
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d(TAG, "PlayerEffects: Dispose finished")
        }
    }
}