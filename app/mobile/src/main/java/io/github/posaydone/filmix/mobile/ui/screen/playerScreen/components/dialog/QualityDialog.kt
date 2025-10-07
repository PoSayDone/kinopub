@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.mobile.ui.common.SingleSelectionCard

@OptIn(UnstableApi::class)
@Composable
fun QualityDialog(
    qualities: List<File>,
    selectedQuality: File?,
    viewModel: PlayerScreenViewModel,
    isQualitySheetOpen: Boolean,
    onDismiss: () -> Unit,
) {

    if (isQualitySheetOpen) ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(qualities) { item ->
                SingleSelectionCard(
                    selectionOption = item,
                    selectedQuality,
                ) {
                    viewModel.setQuality(item)
                }
            }
        }
    }
}