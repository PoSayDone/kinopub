package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel.Companion.SHOW_CONTROLS_TIME
import io.github.posaydone.filmix.tv.ui.utils.handleDPadKeyEvents
import io.github.posaydone.filmix.tv.ui.utils.ifElse

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun RowScope.PlayerSeekPill(
    progress: Float,
    onSeek: (seekProgress: Float) -> Unit,
    onShowControls: (seconds: Int) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isSelected by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color by rememberUpdatedState(
        newValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
    )
    val animatedIndicatorHeight by animateDpAsState(
        targetValue = 4.dp.times((if (isFocused) 2.5f else 1f))
    )
    var seekProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            onShowControls(Int.MAX_VALUE)
        } else {
            onShowControls(SHOW_CONTROLS_TIME)
        }
    }

    LaunchedEffect(isFocused) {
        if (!isFocused && isSelected) {
            isSelected = false
            seekProgress = progress
            onShowControls(SHOW_CONTROLS_TIME)
        }
    }

    val handleSeekEventModifier = Modifier.handleDPadKeyEvents(onEnter = {
        isSelected = !isSelected
        onSeek(seekProgress)
    }, onLeft = {
        seekProgress = (seekProgress - 0.025f).coerceAtLeast(0f)
    }, onRight = {
        seekProgress = (seekProgress + 0.025f).coerceAtMost(1f)
    })

    val handleDpadCenterClickModifier = Modifier.handleDPadKeyEvents(
        onEnter = {
            seekProgress = progress
            isSelected = !isSelected
        })

    Canvas(
        modifier = Modifier
            .weight(1f)
            .height(animatedIndicatorHeight)
            .padding(horizontal = 4.dp)
            .ifElse(
                condition = isSelected,
                ifTrueModifier = handleSeekEventModifier,
                ifFalseModifier = handleDpadCenterClickModifier
            )
            .focusable(interactionSource = interactionSource), onDraw = {
            val yOffset = size.height.div(2)
            drawLine(
                color = color.copy(alpha = 0.24f),
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = size.width, y = yOffset),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color, start = Offset(x = 0f, y = yOffset), end = Offset(
                    x = size.width.times(if (isSelected) seekProgress else progress), y = yOffset
                ), strokeWidth = size.height, cap = StrokeCap.Round
            )
        })
}
