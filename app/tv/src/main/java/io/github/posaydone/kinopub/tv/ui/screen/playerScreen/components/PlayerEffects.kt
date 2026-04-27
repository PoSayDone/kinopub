package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerState
import kotlinx.coroutines.runBlocking

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