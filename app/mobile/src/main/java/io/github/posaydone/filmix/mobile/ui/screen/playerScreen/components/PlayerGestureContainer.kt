package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.annotation.OptIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout

@OptIn(UnstableApi::class)
@Composable
fun PlayerGestureContainer(
    modifier: Modifier = Modifier,
    toggleControls: () -> Unit,
    setResizeMode: (resizeMode: Int) -> Unit,
    setPulseType: (type: PlayerPulse.Type) -> Unit,
    seekForward: () -> Unit,
    seekBack: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom > 1) {
                        setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                    } else if (zoom < 1) {
                        setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { toggleControls() }, onDoubleTap = { offset ->
                    val screenWidth = size.width
                    if (offset.x < screenWidth / 2) {
                        seekBack()
                        setPulseType(PlayerPulse.Type.BACK)
                    } else {
                        seekForward()
                        setPulseType(PlayerPulse.Type.FORWARD)
                    }
                })
            })
}