package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components.dialog

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.tv.ui.common.ScrollableTabRow
import io.github.posaydone.filmix.tv.ui.common.SideDialog
import io.github.posaydone.filmix.tv.ui.common.SingleSelectionCard
import kotlinx.coroutines.delay

@UnstableApi
@Composable
fun EpisodeDialog(
    viewModel: PlayerScreenViewModel,
    seasons: List<Season>,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    isEpisodeDialogOpen: Boolean,
    onDismiss: () -> Unit,
) {
    val seasonsList = seasons.map { season ->
        stringResource(R.string.season, season.season)
    }

    var selectedTab by remember(selectedSeason, seasons) {
        mutableIntStateOf(
            if (selectedSeason != null && seasons.isNotEmpty()) {
                val index = seasons.indexOfFirst { it.season == selectedSeason.season }
                if (index >= 0) index else 0
            } else {
                0
            })
    }

    val selectedEpisodeFocusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()

    SideDialog(
        showDialog = isEpisodeDialogOpen,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.select_episode),
        description = null
    ) {
        val selectedSeasonEpisodes = if (selectedTab >= 0 && selectedTab < seasons.size) {
            seasons[selectedTab].episodes
        } else {
            emptyList()
        }

        Column(modifier = Modifier.focusable(false)) {
            ScrollableTabRow(
                items = seasonsList,
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            )
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                itemsIndexed(selectedSeasonEpisodes) { index, episode ->
                    val itemModifier = if (episode == selectedEpisode) {
                        Modifier.focusRequester(selectedEpisodeFocusRequester)
                    } else {
                        Modifier
                    }

                    SingleSelectionCard(
                        modifier = itemModifier,
                        selectionOption = episode,
                        selectedOption = selectedEpisode,
                        onOptionClicked = {
                            viewModel.setSeason(seasons[selectedTab])
                            viewModel.setEpisode(episode)
                            onDismiss()
                        })
                }
            }
        }
    }

    // Scroll to and focus the selected episode when the dialog becomes visible
    LaunchedEffect(isEpisodeDialogOpen, selectedEpisode, selectedTab) {
        if (isEpisodeDialogOpen && selectedEpisode != null) {
            val selectedEpisodeIndex = seasons[selectedTab].episodes.indexOf(selectedEpisode)

            if (selectedEpisodeIndex >= 0) {
                delay(100)

                lazyListState.animateScrollToItem(selectedEpisodeIndex)

                delay(100)
                selectedEpisodeFocusRequester.requestFocus()
            }
        }
    }
}