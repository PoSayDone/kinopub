package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
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
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.model.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    qualities: List<File>,
    selectedQuality: File?,
    isSettingsSheetOpen: Boolean,
    onDismiss: () -> Unit,
    onQualitySelected: (File) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    var selectedTempQuality by remember { mutableStateOf(selectedQuality) }
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
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (currentPage) {
                    SettingsPage.MAIN -> {
                        MainSettingsPage(
                            selectedQuality = selectedQuality,
                            onQualityClick = { currentPage = SettingsPage.QUALITY })
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

                }
            }
        }
    }
}

@Composable
private fun MainSettingsPage(
    onQualityClick: () -> Unit,
    selectedQuality: File?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
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
                        if (selectedQuality != null) Text("${selectedQuality.quality}p")
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ChevronRight, contentDescription = null
                        )
                    }
                },
                modifier = Modifier.clickable { onQualityClick() })
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
    ) {
        items(qualities) { quality ->
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text("${quality.quality}p") },
                trailingContent = {
                    if (quality == selectedQuality) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                    }
                },
                modifier = Modifier.clickable { onQualitySelected(quality) })
        }
    }
}

enum class SettingsPage {
    MAIN, QUALITY
}
