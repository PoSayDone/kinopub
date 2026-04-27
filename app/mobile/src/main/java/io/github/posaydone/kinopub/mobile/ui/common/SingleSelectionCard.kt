package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> SingleSelectionCard(selectionOption: T, selectedOption: T?, onOptionClicked: (T) -> Unit) {
    Surface(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(32.dp))
            .clickable(true, onClick = { onOptionClicked(selectionOption) }),
        color = if (selectionOption == selectedOption) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Companion.Transparent
        },
    ) {
        Row(modifier = Modifier.Companion.padding(16.dp)) {
            Text(
                text = selectionOption.toString(), style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}