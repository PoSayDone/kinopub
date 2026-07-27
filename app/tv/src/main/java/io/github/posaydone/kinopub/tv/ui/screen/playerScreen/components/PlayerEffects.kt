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
    // isPlaying goes false while merely buffering (initial load, post-seek/skip
    // rebuffer) — not just on a real pause. Gate on !isLoading too, otherwise every
    // buffering spell (including "skip intro") pops the full controls overlay open on
    // top of the (separately rendered, always-independent) loading spinner.
    LaunchedEffect(playerState.isPlaying, playerState.isLoading) {
        if (!playerState.isPlaying && !playerState.isLoading && pulseState.type == PlayerPulse.Type.NONE) {
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