package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.File
import io.github.posaydone.kinopub.tv.ui.common.SideDialog

@OptIn(UnstableApi::class)
@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    qualities: List<File>,
    selectedQuality: File?,
    isAutoQuality: Boolean = false,
    isHlsStream: Boolean = false,
    cropOptions: List<String>,
    selectedCrop: String?,
    speedOptions: List<Float> = PlaybackSpeeds,
    selectedSpeed: Float,
    isSettingsSheetOpen: Boolean,
    onDismiss: () -> Unit,
    onQualitySelected: (File) -> Unit,
    onAutoQualitySelected: () -> Unit = {},
    onCropSelected: (String) -> Unit,
    onSpeedSelected: (Float) -> Unit,
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    var mainPageFocusTarget by remember { mutableStateOf(SettingsMainPageFocusTarget.QUALITY) }
    var selectedTempQuality by remember { mutableStateOf(selectedQuality) }
    var selectedTempIsAuto by remember { mutableStateOf(isAutoQuality) }
    var selectedTempCrop by remember { mutableStateOf(selectedCrop) }

    SideDialog(
        modifier = modifier,
        showDialog = isSettingsSheetOpen,
        onDismissRequest = {
            onDismiss()
            currentPage = SettingsPage.MAIN
            mainPageFocusTarget = SettingsMainPageFocusTarget.QUALITY
        },
        onBack = if (currentPage != SettingsPage.MAIN) {
            { currentPage = SettingsPage.MAIN }
        } else null,
        title = when (currentPage) {
            SettingsPage.MAIN -> stringResource(R.string.settings)
            SettingsPage.QUALITY -> stringResource(R.string.quality)
            SettingsPage.CROP -> stringResource(R.string.crop)
            SettingsPage.SPEED -> stringResource(R.string.playback_speed)
        },
        description = null
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (currentPage) {
                SettingsPage.MAIN -> {
                    MainSettingsPage(
                        initialFocusTarget = mainPageFocusTarget,
                        onQualityClick = {
                            mainPageFocusTarget = SettingsMainPageFocusTarget.QUALITY
                            currentPage = SettingsPage.QUALITY
                        },
                        onCropClick = {
                            mainPageFocusTarget = SettingsMainPageFocusTarget.CROP
                            currentPage = SettingsPage.CROP
                        },
                        onSpeedClick = {
                            mainPageFocusTarget = SettingsMainPageFocusTarget.SPEED
                            currentPage = SettingsPage.SPEED
                        },
                        selectedCrop = selectedTempCrop,
                        selectedQuality = selectedTempQuality,
                        isAutoQuality = selectedTempIsAuto,
                        selectedSpeed = selectedSpeed,
                    )

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

                SettingsPage.CROP -> {
                    CropSettingsPage(
                        cropOptions = cropOptions,
                        selectedCrop = selectedTempCrop,
                        onCropSelected = { crop ->
                            selectedTempCrop = crop
                            onCropSelected(crop)
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

val PlaybackSpeeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

fun formatSpeedLabel(speed: Float): String =
    if (speed == speed.toLong().toFloat()) "${speed.toLong()}x" else "${speed}x"

@Composable
private fun MainSettingsPage(
    initialFocusTarget: SettingsMainPageFocusTarget,
    onQualityClick: () -> Unit,
    onCropClick: () -> Unit,
    onSpeedClick: () -> Unit,
    selectedQuality: File?,
    isAutoQuality: Boolean = false,
    selectedCrop: String?,
    selectedSpeed: Float,
    modifier: Modifier = Modifier,
) {
    val qualityItemFocusRequester = remember { FocusRequester() }
    val cropItemFocusRequester = remember { FocusRequester() }
    val speedItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        when (initialFocusTarget) {
            SettingsMainPageFocusTarget.QUALITY -> qualityItemFocusRequester.requestFocus()
            SettingsMainPageFocusTarget.CROP -> cropItemFocusRequester.requestFocus()
            SettingsMainPageFocusTarget.SPEED -> speedItemFocusRequester.requestFocus()
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            ListItem(
                modifier = Modifier.focusRequester(qualityItemFocusRequester),
                onClick = onQualityClick,
                selected = false,
                headlineContent = { Text(stringResource(R.string.quality)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.quality)
                    )
                },
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
                })
        }

        item {
            ListItem(
                modifier = Modifier.focusRequester(cropItemFocusRequester),
                onClick = { onCropClick() },
                selected = false,
                headlineContent = { Text(stringResource(R.string.crop)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.AspectRatio, contentDescription = stringResource(R.string.crop)
                    )
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCrop != null) Text(selectedCrop)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ChevronRight, contentDescription = null
                        )
                    }
                })
        }

        item {
            ListItem(
                modifier = Modifier.focusRequester(speedItemFocusRequester),
                onClick = onSpeedClick,
                selected = false,
                headlineContent = { Text(stringResource(R.string.playback_speed)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Speed, contentDescription = stringResource(R.string.playback_speed)
                    )
                },
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
                })
        }
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
    val initialFocusRequester = remember { FocusRequester() }
    val autoFocusRequester = remember { FocusRequester() }
    val initialFocusIndex = if (isHlsStream) {
        qualities.indexOf(selectedQuality).takeIf { !isAutoQuality && it >= 0 } ?: -1
    } else {
        qualities.indexOf(selectedQuality).takeIf { it >= 0 } ?: 0
    }

    LaunchedEffect(Unit) {
        if (isHlsStream && isAutoQuality) {
            autoFocusRequester.requestFocus()
        } else if (qualities.isNotEmpty()) {
            initialFocusRequester.requestFocus()
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (isHlsStream) {
            item {
                ListItem(
                    modifier = Modifier.focusRequester(autoFocusRequester),
                    headlineContent = { Text(stringResource(R.string.quality_auto)) },
                    trailingContent = {
                        if (isAutoQuality) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                        }
                    },
                    onClick = onAutoQualitySelected,
                    selected = isAutoQuality,
                    scale = ListItemDefaults.scale(focusedScale = 1.02f)
                )
            }
        }
        itemsIndexed(qualities) { index, quality ->
            ListItem(
                modifier = Modifier.let {
                    if (index == initialFocusIndex) it.focusRequester(initialFocusRequester) else it
                },
                headlineContent = { Text("${quality.quality}p") },
                trailingContent = {
                    if (!isAutoQuality && quality == selectedQuality) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                onClick = { onQualitySelected(quality) },
                selected = !isAutoQuality && selectedQuality == quality,
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
    val initialFocusRequester = remember { FocusRequester() }
    val initialFocusIndex = cropOptions.indexOf(selectedCrop).takeIf { it >= 0 } ?: 0

    LaunchedEffect(Unit) {
        if (cropOptions.isNotEmpty()) {
            initialFocusRequester.requestFocus()
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(cropOptions) { index, option ->
            ListItem(
                modifier = Modifier.let {
                    if (index == initialFocusIndex) {
                        it.focusRequester(initialFocusRequester)
                    } else {
                        it
                    }
                },
                headlineContent = { Text(option) },
                trailingContent = {
                    if (option == selectedCrop) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                onClick = { onCropSelected(option) },
                scale = ListItemDefaults.scale(focusedScale = 1.02f),
                selected = selectedCrop == option
            )
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
    val initialFocusRequester = remember { FocusRequester() }
    val initialFocusIndex = speedOptions.indexOf(selectedSpeed).takeIf { it >= 0 } ?: 0

    LaunchedEffect(Unit) {
        if (speedOptions.isNotEmpty()) {
            initialFocusRequester.requestFocus()
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(speedOptions) { index, speed ->
            ListItem(
                modifier = Modifier.let {
                    if (index == initialFocusIndex) {
                        it.focusRequester(initialFocusRequester)
                    } else {
                        it
                    }
                },
                headlineContent = { Text(formatSpeedLabel(speed)) },
                trailingContent = {
                    if (speed == selectedSpeed) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                onClick = { onSpeedSelected(speed) },
                scale = ListItemDefaults.scale(focusedScale = 1.02f),
                selected = selectedSpeed == speed
            )
        }
    }
}

enum class SettingsPage {
    MAIN, QUALITY, CROP, SPEED
}

private enum class SettingsMainPageFocusTarget {
    QUALITY, CROP, SPEED
}
