package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.mobile.ui.common.EpisodeList
import io.github.posaydone.kinopub.mobile.ui.common.episodeListInitialIndex

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun EpisodeDialog(
    viewModel: PlayerScreenViewModel,
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    isEpisodeDialogOpen: Boolean,
    onDismiss: () -> Unit,
    showTitle: String,
) {
    if (isEpisodeDialogOpen) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            sheetMaxWidth = LocalConfiguration.current.screenWidthDp.dp,
        ) {
            Text(
                text = showTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = remember(seasons, selectedSeason, selectedEpisode) {
                    episodeListInitialIndex(seasons, selectedSeason, selectedEpisode)
                }
            )

            EpisodeList(
                seasons = seasons,
                selectedSeason = selectedSeason,
                selectedEpisode = selectedEpisode,
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                onEpisodeClick = { season, episode ->
                    viewModel.setSeason(season)
                    viewModel.setEpisode(episode)
                    onDismiss()
                },
            )
        }
    }
}
