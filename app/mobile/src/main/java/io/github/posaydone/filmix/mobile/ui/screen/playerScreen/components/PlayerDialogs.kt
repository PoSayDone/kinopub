package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog.AudioDialog
import io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog.EpisodeDialog
import io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog.SettingsDialog

@OptIn(UnstableApi::class)
@Composable
fun PlayerDialogs(
    showDetails: FullShow,
    viewModel: PlayerScreenViewModel,

    isEpisodeDialogOpen: Boolean,
    isAudioDialogOpen: Boolean,
    isSettingsDialogOpen: Boolean,

    closeEpisodeDialog: () -> Unit,
    closeAudioDialog: () -> Unit,
    closeSettingsDialog: () -> Unit,
) {
    val seasons by viewModel.seasons.collectAsState()
    val moviePieces by viewModel.moviePieces.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val selectedTranslation by viewModel.selectedTranslation.collectAsState()
    val selectedMovieTranslation by viewModel.selectedMovieTranslation.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val showType by viewModel.contentType.collectAsState()
    val isHls4AudioTrackSelectionEnabled by viewModel.isHls4AudioTrackSelectionEnabled.collectAsState()

    if (showType == ShowType.SERIES) {
        if (seasons != null && selectedSeason != null) {
            EpisodeDialog(
                viewModel = viewModel,
                seasons = seasons!!,
                selectedSeason = selectedSeason,
                selectedEpisode = selectedEpisode,
                isEpisodeDialogOpen = isEpisodeDialogOpen,
                onDismiss = closeEpisodeDialog,
                showTitle = showDetails.title,
            )
        }

        if (isHls4AudioTrackSelectionEnabled) selectedEpisode?.translations?.let { translations ->
            AudioDialog(
                translations,
                selectedTranslation,
                viewModel,
                isAudioDialogOpen,
                showType,
                onDismiss = closeAudioDialog
            )
        }

        selectedTranslation?.files?.let { qualities ->
            SettingsDialog(
                qualities = qualities,
                selectedQuality = selectedQuality,
                isSettingsSheetOpen = isSettingsDialogOpen,
                onDismiss = closeSettingsDialog,
                onQualitySelected = { quality -> viewModel.setQuality(quality) })
        }
    } else {
        if (isHls4AudioTrackSelectionEnabled) moviePieces?.let { translations ->
            AudioDialog(
                translations,
                selectedMovieTranslation,
                viewModel,
                isAudioDialogOpen,
                showType,
                onDismiss = closeAudioDialog
            )
        }
       
        selectedMovieTranslation?.files?.let { qualities ->
            SettingsDialog(
                qualities = qualities,
                selectedQuality = selectedQuality,
                isSettingsSheetOpen = isSettingsDialogOpen,
                onDismiss = closeSettingsDialog,
                onQualitySelected = { quality -> viewModel.setQuality(quality) })
        }
    }
}
