package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.mobile.ui.common.SingleSelectionCard

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
        ) {
            var tabIndex by rememberSaveable {
                mutableIntStateOf(
                    selectedSeason?.season?.minus(1) ?: 0
                )
            }
            Text(
                text = showTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            SecondaryScrollableTabRow(
                selectedTabIndex = tabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                seasons.forEachIndexed { index, season ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(0.dp)
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(season.season.toString())
                        }
                    }
                }
            }

            val selectedSeasonEpisodes = seasons[tabIndex].episodes

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(selectedSeasonEpisodes) { episode ->
                    SingleSelectionCard(
                        selectionOption = episode, selectedEpisode
                    ) {
                        viewModel.setSeason(seasons[tabIndex])
                        viewModel.setEpisode(episode)
                        onDismiss()
                    }
                }
            }
        }
    }
}
