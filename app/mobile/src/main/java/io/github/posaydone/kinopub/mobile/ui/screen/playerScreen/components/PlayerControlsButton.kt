package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
    val commonModifier = Modifier
        .height(48.dp)
        .then(modifier)

    val commonPadding = PaddingValues(
        horizontal = 20.dp, vertical = 0.dp
    )

    if (text != null) {
        Button(
            modifier = commonModifier,
            onClick = onClick,
            enabled = !disabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(0.1f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(0.1f),
                disabledContentColor = Color.White.copy(0.8f),
            ),
            contentPadding = commonPadding
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
            modifier = Modifier
                .size(48.dp)
                .then(modifier),
            enabled = !disabled,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White.copy(0.1f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(0.05f),
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
