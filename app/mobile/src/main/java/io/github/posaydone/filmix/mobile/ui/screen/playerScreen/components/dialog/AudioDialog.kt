@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.Translation
import io.github.posaydone.filmix.core.model.VideoWithQualities
import io.github.posaydone.filmix.mobile.ui.common.SingleSelectionCard

@OptIn(UnstableApi::class)
@Composable
fun <T> AudioDialog(
    translations: List<T>,
    selectedTranslation: T?,
    viewModel: PlayerScreenViewModel,
    isAudioSheetOpen: Boolean,
    showType: ShowType?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isAudioSheetOpen) ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(translations) { item ->
                SingleSelectionCard(
                    selectionOption = item,
                    selectedTranslation,
                ) {
                    when (showType) {
                        ShowType.MOVIE -> viewModel.setMovieTranslation(item as VideoWithQualities)
                        ShowType.SERIES -> viewModel.setTranslation(item as Translation)
                        null -> {}
                    }
                }
            }
        }
    }
}