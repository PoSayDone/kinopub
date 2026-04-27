package io.github.posaydone.kinopub.tv.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.OutlinedButtonDefaults

enum class LargeButtonStyle {
    FILLED, OUTLINED
}

@Composable
private fun getDefaultColors(buttonStyle: LargeButtonStyle): ButtonColors {
    when (buttonStyle) {
        LargeButtonStyle.OUTLINED -> return OutlinedButtonDefaults.colors()
        LargeButtonStyle.FILLED -> return ButtonDefaults.colors()
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
    colors: ButtonColors = getDefaultColors(style),
    content: @Composable RowScope.() -> Unit,
) {
    val commonModifier = Modifier
        .height(56.dp)
        .then(modifier)

    val commonPadding = PaddingValues(
        horizontal = 24.dp, vertical = 18.dp
    )

    when (style) {
        LargeButtonStyle.FILLED -> {
            Button(
                onClick = onClick,
                modifier = commonModifier,
                enabled = enabled,
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
                contentPadding = commonPadding,
                content = content
            )
        }

    }
}