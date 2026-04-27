package io.github.posaydone.filmix.tv.ui.common

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.posaydone.filmix.tv.ui.theme.DefaultBorderSize

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(all = 14.dp),
    placeholderText: String = "Enter text...",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.large,
) {
    val focusManager = LocalFocusManager.current
    val tfFocusRequester = remember { FocusRequester() }

    Surface(
        shape = ClickableSurfaceDefaults.shape(shape = shape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface,
            focusedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
            pressedContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
            focusedContentColor = MaterialTheme.colorScheme.onSurface,
            pressedContentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = DefaultBorderSize,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        ),
        tonalElevation = 2.dp,
        modifier = modifier,
        onClick = { tfFocusRequester.requestFocus() }) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .focusRequester(tfFocusRequester)
                .onKeyEvent {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        when (it.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                focusManager.moveFocus(FocusDirection.Down)
                            }

                            KeyEvent.KEYCODE_DPAD_UP -> {
                                focusManager.moveFocus(FocusDirection.Up)
                            }

                            KeyEvent.KEYCODE_BACK -> {
                                focusManager.moveFocus(FocusDirection.Exit)
                            }
                        }
                    }
                    true
                },
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxWidth()
                ) {
                    if (leadingIcon != null) {
                        Box(
                            modifier = Modifier.padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            leadingIcon()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholderText,
                                color = textStyle.color.copy(alpha = 0.6f),
                                style = textStyle
                            )
                        }
                        innerTextField()
                    }

                    if (trailingIcon != null) {
                        Box(
                            modifier = Modifier.padding(start = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            trailingIcon()
                        }
                    }
                }
            })
    }
}
