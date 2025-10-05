package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text

@Composable
fun <T> SingleSelectionCard(modifier: Modifier = Modifier, selectionOption: T, selectedOption: T?, onOptionClicked: (T) -> Unit) {
    ListItem(
        headlineContent = { Text(text = selectionOption.toString()) },
        scale = ListItemDefaults.scale(focusedScale = 1.02f),
        selected = false,
        onClick = { onOptionClicked(selectionOption) },
        trailingContent = {
            if (selectedOption == selectionOption) {
                Icon(Icons.Default.Check, contentDescription = "Check")
            }
        })
}