package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.tv.ui.common.SideDialog

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
    var selectedTempQuality by remember { mutableStateOf(selectedQuality) }
    var selectedTempCrop by remember { mutableStateOf(selectedCrop) }

    SideDialog(
        modifier = modifier,
        showDialog = isSettingsSheetOpen,
        onDismissRequest = {
            onDismiss()
            currentPage = SettingsPage.MAIN
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            when (currentPage) {
                SettingsPage.MAIN -> {
                    MainSettingsPage(
                        onQualityClick = { currentPage = SettingsPage.QUALITY },
                        onCropClick = { currentPage = SettingsPage.CROP },
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
    onQualityClick: () -> Unit,
    onCropClick: () -> Unit,
    selectedQuality: File?,
    selectedCrop: String?,
    modifier: Modifier = Modifier,
) {
    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(lazyColumn)
            .focusRestorer(firstItem),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            ListItem(
                modifier = Modifier.focusRequester(firstItem),
                onClick = {
                    onQualityClick()
                    lazyColumn.saveFocusedChild()
                },
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
    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(lazyColumn)
            .focusRestorer(firstItem),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        itemsIndexed(qualities) { index, quality ->
            ListItem(
                modifier = Modifier.let {
                    if (index == 0) {
                        it.focusRequester(firstItem)
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
    val (lazyColumn, firstItem) = remember { FocusRequester.createRefs() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(lazyColumn)
            .focusRestorer(firstItem),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        itemsIndexed(cropOptions) { index, option ->
            ListItem(
                modifier = Modifier.let {
                    if (index == 0) {
                        it.focusRequester(firstItem)
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