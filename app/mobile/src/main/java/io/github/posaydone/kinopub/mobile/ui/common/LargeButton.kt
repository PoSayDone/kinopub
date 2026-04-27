package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

enum class LargeButtonStyle {
    FILLED, OUTLINED, TEXT
}


@Composable
private fun getDefaultColors(buttonStyle: LargeButtonStyle): ButtonColors {
    when (buttonStyle) {
        LargeButtonStyle.OUTLINED -> return ButtonDefaults.outlinedButtonColors()
        LargeButtonStyle.TEXT -> return ButtonDefaults.textButtonColors()
        LargeButtonStyle.FILLED -> return ButtonDefaults.buttonColors()
    }
}

/**
 * A custom composable that provides a larger, more prominent version of a button,
 * supporting different visual styles like Filled, Outlined, and Text.
 *
 * @param onClick The callback to be invoked when this button is clicked.
 * @param modifier The modifier to be applied to the button. A default height is pre-applied.
 * @param style The visual style of the button. See [LargeButtonStyle].
 * @param enabled Controls the enabled state of the button.
 * @param shape Defines the button's shape. Defaults to a larger, rounded shape.
 * @param colors The colors to be used for the button in different states.
 * @param border The border to be used for the button, typically for the OUTLINED style.
 * @param content The content to be displayed inside the button.
 */
@Composable
fun LargeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LargeButtonStyle = LargeButtonStyle.FILLED,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = getDefaultColors(style),
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val commonModifier = Modifier
        .height(56.dp)
        .then(modifier)

    val commonPadding = PaddingValues(
        horizontal = 24.dp, vertical = 16.dp
    )

    when (style) {
        LargeButtonStyle.FILLED -> {
            Button(
                onClick = onClick,
                modifier = commonModifier,
                enabled = enabled,
                shape = shape,
                colors = colors,
                contentPadding = commonPadding,
                content = content
            )
        }

        LargeButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = commonModifier,
                enabled = enabled,
                colors = colors,
                shape = shape,
                contentPadding = commonPadding,
                content = content
            )
        }

        LargeButtonStyle.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = commonModifier,
                enabled = enabled,
                shape = shape,
                border = border,
                contentPadding = commonPadding,
                content = content
            )
        }
    }
}
