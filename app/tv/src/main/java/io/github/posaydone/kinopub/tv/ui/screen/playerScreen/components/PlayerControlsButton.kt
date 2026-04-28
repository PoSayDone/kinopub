package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

@Composable
fun PlayerControlsButton(
    modifier: Modifier = Modifier,
    onShowControls: () -> Unit,
    isPlaying: Boolean,
    icon: ImageVector,
    contentDescription: String? = null,
    disabled: Boolean = false,
    text: String? = null,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused && isPlaying) {
        if (isFocused && isPlaying) {
            onShowControls()
        }
    }

    Surface(
        modifier = modifier
            .then(if (text != null) Modifier else Modifier.size(40.dp)),
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (disabled) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.1f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        interactionSource = interactionSource,
        enabled = !disabled
    ) {
        if (text != null) {
            // Icon with text
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    modifier = Modifier.size(24.dp),
                    contentDescription = contentDescription,
                    tint = LocalContentColor.current
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current
                )
            }
        } else {
            // Icon only
            Icon(
                icon,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentDescription = contentDescription,
                tint = LocalContentColor.current
            )
        }
    }
}
