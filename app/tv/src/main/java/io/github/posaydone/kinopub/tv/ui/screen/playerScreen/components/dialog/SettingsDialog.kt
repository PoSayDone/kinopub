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
    cropOptions: List<String>,
    selectedCrop: String?,
    isSettingsSheetOpen: Boolean,
    onDismiss: () -> Unit,
    onQualitySelected: (File) -> Unit,
    onCropSelected: (String) -> Unit,
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    var mainPageFocusTarget by remember { mutableStateOf(SettingsMainPageFocusTarget.QUALITY) }
    var selectedTempQuality by remember { mutableStateOf(selectedQuality) }
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
                        selectedCrop = selectedTempCrop,
                        selectedQuality = selectedTempQuality
                    )

                }

                SettingsPage.QUALITY -> {
                    QualitySettingsPage(
                        qualities = qualities,
                        selectedQuality = selectedTempQuality,
                        onQualitySelected = { quality ->
                            selectedTempQuality = quality
                            onQualitySelected(quality)
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
            }
        }
    }
}

@Composable
private fun MainSettingsPage(
    initialFocusTarget: SettingsMainPageFocusTarget,
    onQualityClick: () -> Unit,
    onCropClick: () -> Unit,
    selectedQuality: File?,
    selectedCrop: String?,
    modifier: Modifier = Modifier,
) {
    val qualityItemFocusRequester = remember { FocusRequester() }
    val cropItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        when (initialFocusTarget) {
            SettingsMainPageFocusTarget.QUALITY -> qualityItemFocusRequester.requestFocus()
            SettingsMainPageFocusTarget.CROP -> cropItemFocusRequester.requestFocus()
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
                        if (selectedQuality != null) Text("${selectedQuality.quality}p")
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
    }
}

@Composable
private fun QualitySettingsPage(
    qualities: List<File>,
    selectedQuality: File?,
    onQualitySelected: (File) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialFocusRequester = remember { FocusRequester() }
    val initialFocusIndex = qualities.indexOf(selectedQuality).takeIf { it >= 0 } ?: 0

    LaunchedEffect(Unit) {
        if (qualities.isNotEmpty()) {
            initialFocusRequester.requestFocus()
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(qualities) { index, quality ->
            ListItem(
                modifier = Modifier.let {
                    if (index == initialFocusIndex) {
                        it.focusRequester(initialFocusRequester)
                    } else {
                        it
                    }
                },
                headlineContent = { Text("${quality.quality}p") },
                trailingContent = {
                    if (quality == selectedQuality) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
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

enum class SettingsPage {
    MAIN, QUALITY, CROP
}

private enum class SettingsMainPageFocusTarget {
    QUALITY, CROP
}
