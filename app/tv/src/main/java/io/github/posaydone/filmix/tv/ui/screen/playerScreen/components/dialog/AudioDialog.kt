package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.compose.ui.res.stringResource
import io.github.posaydone.filmix.core.common.R
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.Translation
import io.github.posaydone.filmix.core.model.VideoWithQualities
import io.github.posaydone.filmix.tv.ui.common.SideDialog
import io.github.posaydone.filmix.tv.ui.common.SingleSelectionCard

@ExperimentalTvMaterial3Api
@OptIn(UnstableApi::class)
@Composable
fun <T> AudioDialog(
    translations: List<T>,
    selectedTranslation: T?,
    viewModel: PlayerScreenViewModel,
    isAudioDialogOpen: Boolean,
    showType: ShowType?,
    onDismiss: () -> Unit,
) {

    SideDialog(
        showDialog = isAudioDialogOpen,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.select_audio_track),
        description = null
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            items(translations) { item ->
                SingleSelectionCard(
                    selectionOption = item,
                    selectedOption = selectedTranslation,
                    onOptionClicked = {
                        when (showType) {
                            ShowType.MOVIE -> viewModel.setMovieTranslation(item as VideoWithQualities)
                            ShowType.SERIES -> viewModel.setTranslation(item as Translation)
                            null -> {}
                        }
                        onDismiss()
                    })
            }
        }
    }

}