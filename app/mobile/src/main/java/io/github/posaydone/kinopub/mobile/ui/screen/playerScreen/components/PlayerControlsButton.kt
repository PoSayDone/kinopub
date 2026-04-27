package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PlayerControlsButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    disabled: Boolean = false,
    text: String? = null,
    onClick: () -> Unit = {},
) {
    if (text != null) {
        Button(
            modifier = modifier,
            onClick = onClick,
            enabled = !disabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(0.3f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(0.1f),
                disabledContentColor = Color.White.copy(0.8f),
            ),
            contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
        ) {
            Icon(
                modifier = modifier.size(16.dp),
                imageVector = icon,
                contentDescription = contentDescription
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
            )
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = !disabled,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White.copy(0.3f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(0.1f),
                disabledContentColor = Color.White.copy(0.8f),
            ),
        ) {
            Icon(
                modifier = modifier.size(16.dp),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White
            )
        }
    }
}
