package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    qualities: List<File>,
    selectedQuality: File?,
    isAutoQuality: Boolean = false,
    isHlsStream: Boolean = false,
    speedOptions: List<Float> = PlaybackSpeeds,
    selectedSpeed: Float,
    isSettingsSheetOpen: Boolean,
    onDismiss: () -> Unit,
    onQualitySelected: (File) -> Unit,
    onAutoQualitySelected: () -> Unit = {},
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    var selectedTempQuality by remember { mutableStateOf(selectedQuality) }
    var selectedTempIsAuto by remember { mutableStateOf(isAutoQuality) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isSettingsSheetOpen) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = {
                onDismiss()
                currentPage = SettingsPage.MAIN
            },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (currentPage) {
                    SettingsPage.MAIN -> {
                        MainSettingsPage(
                            selectedQuality = selectedQuality,
                            isAutoQuality = selectedTempIsAuto,
                            selectedSpeed = selectedSpeed,
                            onQualityClick = { currentPage = SettingsPage.QUALITY },
                            onSpeedClick = { currentPage = SettingsPage.SPEED })
                    }

                    SettingsPage.QUALITY -> {
                        QualitySettingsPage(
                            qualities = qualities,
                            selectedQuality = selectedTempQuality,
                            isAutoQuality = selectedTempIsAuto,
                            isHlsStream = isHlsStream,
                            onQualitySelected = { quality ->
                                selectedTempIsAuto = false
                                selectedTempQuality = quality
                                onQualitySelected(quality)
                            },
                            onAutoQualitySelected = {
                                selectedTempIsAuto = true
                                onAutoQualitySelected()
                            })
                    }

                    SettingsPage.SPEED -> {
                        SpeedSettingsPage(
                            speedOptions = speedOptions,
                            selectedSpeed = selectedSpeed,
                            onSpeedSelected = onSpeedSelected,
                        )
                    }
                }
            }
        }
    }
}

val PlaybackSpeeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

fun formatSpeedLabel(speed: Float): String =
    if (speed == speed.toLong().toFloat()) "${speed.toLong()}x" else "${speed}x"

@Composable
private fun MainSettingsPage(
    onQualityClick: () -> Unit,
    onSpeedClick: () -> Unit,
    selectedQuality: File?,
    isAutoQuality: Boolean = false,
    selectedSpeed: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.quality)
                )
            },
            headlineContent = { Text(stringResource(R.string.quality)) },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAutoQuality) {
                        Text(stringResource(R.string.quality_auto))
                    } else if (selectedQuality != null) {
                        Text("${selectedQuality.quality}p")
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ChevronRight, contentDescription = null
                    )
                }
            },
            modifier = Modifier.clickable { onQualityClick() })

        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = stringResource(R.string.playback_speed)
                )
            },
            headlineContent = { Text(stringResource(R.string.playback_speed)) },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatSpeedLabel(selectedSpeed))
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ChevronRight, contentDescription = null
                    )
                }
            },
            modifier = Modifier.clickable { onSpeedClick() })
    }
}

@Composable
private fun QualitySettingsPage(
    qualities: List<File>,
    selectedQuality: File?,
    isAutoQuality: Boolean = false,
    isHlsStream: Boolean = false,
    onQualitySelected: (File) -> Unit,
    onAutoQualitySelected: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (isHlsStream) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(stringResource(R.string.quality_auto)) },
                trailingContent = {
                    if (isAutoQuality) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                modifier = Modifier.clickable { onAutoQualitySelected() })
        }
        qualities.forEach { quality ->
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text("${quality.quality}p") },
                trailingContent = {
                    if (!isAutoQuality && quality == selectedQuality) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                modifier = Modifier.clickable { onQualitySelected(quality) })
        }
    }
}

@Composable
private fun SpeedSettingsPage(
    speedOptions: List<Float>,
    selectedSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        speedOptions.forEach { speed ->
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(formatSpeedLabel(speed)) },
                trailingContent = {
                    if (speed == selectedSpeed) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                modifier = Modifier.clickable { onSpeedSelected(speed) })
        }
    }
}

enum class SettingsPage {
    MAIN, QUALITY, SPEED
}
