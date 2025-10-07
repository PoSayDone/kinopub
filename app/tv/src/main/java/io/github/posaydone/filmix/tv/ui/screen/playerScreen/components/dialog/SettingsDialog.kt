package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.tv.ui.common.SideDialog

@OptIn(UnstableApi::class)
@Composable
fun SettingsDialog(
    qualities: List<File>,
    selectedQuality: File?,
    cropOptions: List<String>,
    selectedCrop: String?,
    isSettingsSheetOpen: Boolean,
    onDismiss: () -> Unit,
    onQualitySelected: (File) -> Unit,
    onCropSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    var selectedTempQuality by remember { mutableStateOf(selectedQuality) }
    var selectedTempCrop by remember { mutableStateOf(selectedCrop) }

    SideDialog(
        showDialog = isSettingsSheetOpen,
        onDismissRequest = onDismiss,
        title = when (currentPage) {
            SettingsPage.MAIN -> "Settings"
            SettingsPage.QUALITY -> "Quality"
            SettingsPage.CROP -> "Crop"
        },
        description = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            if (currentPage != SettingsPage.MAIN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        onClick = { currentPage = SettingsPage.MAIN },
                        colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            when (currentPage) {
                SettingsPage.MAIN -> {
                    MainSettingsPage(
                        onQualityClick = { currentPage = SettingsPage.QUALITY },
                        onCropClick = { currentPage = SettingsPage.CROP }
                    )
                }

                SettingsPage.QUALITY -> {
                    QualitySettingsPage(
                        qualities = qualities,
                        selectedQuality = selectedTempQuality,
                        onQualitySelected = { quality ->
                            selectedTempQuality = quality
                            onQualitySelected(quality)
                        }
                    )
                }

                SettingsPage.CROP -> {
                    CropSettingsPage(
                        cropOptions = cropOptions,
                        selectedCrop = selectedTempCrop,
                        onCropSelected = { crop ->
                            selectedTempCrop = crop
                            onCropSelected(crop)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainSettingsPage(
    onQualityClick: () -> Unit,
    onCropClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            ListItem(
                headlineContent = { Text("Quality") },
                supportingContent = { Text("Adjust video quality") },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null) },
                onClick = onQualityClick,
                selected = false,
                scale = ListItemDefaults.scale(focusedScale = 1.02f)
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Crop") },
                supportingContent = { Text("Adjust video cropping") },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null) },
                onClick = onCropClick,
                selected = false,
                scale = ListItemDefaults.scale(focusedScale = 1.02f)
            )
        }
    }
}

@Composable
private fun QualitySettingsPage(
    qualities: List<File>,
    selectedQuality: File?,
    onQualitySelected: (File) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(qualities) { quality ->
            ListItem(
                headlineContent = { Text("${quality.quality}p") },
                trailingContent = {
                    if (quality == selectedQuality) {
                        Icon(Icons.Default.Check, contentDescription = "Selected")
                    }
                },
                onClick = { onQualitySelected(quality) },
                selected = selectedQuality == quality,
                scale = ListItemDefaults.scale(focusedScale = 1.02f)
            )
        }
    }
}

@Composable
private fun CropSettingsPage(
    cropOptions: List<String>,
    selectedCrop: String?,
    onCropSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        items(cropOptions) { option ->
            ListItem(
                headlineContent = { Text(option) },
                trailingContent = {
                    if (option == selectedCrop) {
                        Icon(Icons.Default.Check, contentDescription = "Selected")
                    }
                },
                onClick = { onCropSelected(option) },
                scale = ListItemDefaults.scale(focusedScale = 1.02f),
                selected = selectedCrop == option
            )
        }
    }
}

enum class SettingsPage {
    MAIN, QUALITY, CROP
}