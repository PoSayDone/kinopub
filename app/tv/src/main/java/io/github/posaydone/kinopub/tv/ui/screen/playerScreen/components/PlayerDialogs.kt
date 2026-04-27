@file:kotlin.OptIn(ExperimentalTvMaterial3Api::class)

package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowType
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.dialog.AudioDialog
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.dialog.EpisodeDialog
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components.dialog.SettingsDialog

@OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class, ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerDialogs(
    viewModel: PlayerScreenViewModel,
    isEpisodeSheetOpen: Boolean,
    isAudioSheetOpen: Boolean,
    isQualitySheetOpen: Boolean,
    closeEpisodeSheet: () -> Unit,
    closeAudioSheet: () -> Unit,
    closeQualitySheet: () -> Unit,
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
                isEpisodeDialogOpen = isEpisodeSheetOpen,
                onDismiss = closeEpisodeSheet
            )
        }

        if (isHls4AudioTrackSelectionEnabled) selectedEpisode?.translations?.let { translations ->
            AudioDialog(
                translations,
                selectedTranslation,
                viewModel,
                isAudioSheetOpen,
                showType,
                onDismiss = closeAudioSheet
            )
        }

        // Use SettingsDialog instead of QualityDialog
        selectedTranslation?.files?.let { qualities ->
            SettingsDialog(
                qualities = qualities,
                selectedQuality = selectedQuality,
                cropOptions = listOf("Fit", "Fill", "Zoom"),
                selectedCrop = viewModel.selectedCrop.value,
                isSettingsSheetOpen = isQualitySheetOpen,
                onDismiss = closeQualitySheet,
                onQualitySelected = { quality -> viewModel.setQuality(quality) },
                onCropSelected = { crop -> viewModel.setCrop(crop) })
        }
    } else {
        if (isHls4AudioTrackSelectionEnabled) moviePieces?.let { translations ->
            AudioDialog(
                translations,
                selectedMovieTranslation,
                viewModel,
                isAudioSheetOpen,
                showType,
                onDismiss = closeAudioSheet
            )
        }

        selectedMovieTranslation?.files?.let { qualities ->
            SettingsDialog(
                qualities = qualities,
                selectedQuality = selectedQuality,
                cropOptions = listOf("Fit", "Fill", "Zoom"),
                selectedCrop = viewModel.selectedCrop.value,
                isSettingsSheetOpen = isQualitySheetOpen,
                onDismiss = closeQualitySheet,
                onQualitySelected = { quality -> viewModel.setQuality(quality) },
                onCropSelected = { crop -> viewModel.setCrop(crop) })
        }
    }
}
