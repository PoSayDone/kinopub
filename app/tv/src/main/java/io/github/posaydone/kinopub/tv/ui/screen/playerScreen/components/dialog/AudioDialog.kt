package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.dialog

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.kinopub.core.common.R
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowType
import io.github.posaydone.kinopub.core.model.Translation
import io.github.posaydone.kinopub.core.model.VideoWithQualities
import io.github.posaydone.kinopub.tv.ui.common.SideDialog
import io.github.posaydone.kinopub.tv.ui.common.SingleSelectionCard

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
    val initialFocusRequester = remember { FocusRequester() }
    val initialFocusIndex = translations.indexOf(selectedTranslation).takeIf { it >= 0 } ?: 0

    LaunchedEffect(Unit) {
        if (translations.isNotEmpty()) {
            initialFocusRequester.requestFocus()
        }
    }

    SideDialog(
        showDialog = isAudioDialogOpen,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.select_audio_track),
        description = null
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(translations) { index, item ->
                SingleSelectionCard(
                    modifier = if (index == initialFocusIndex) {
                        Modifier.focusRequester(initialFocusRequester)
                    } else {
                        Modifier
                    },
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
