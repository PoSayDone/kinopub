package io.github.posaydone.filmix.tv.navigation.graph

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.tv.ui.screen.episodesScreen.EpisodesScreen
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.PlayerScreen
import io.github.posaydone.filmix.tv.ui.screen.showDetailsScreen.ShowDetailsScreen

@OptIn(UnstableApi::class)
@Composable
fun DetailsGraph(showId: Int) {
    val backStack = rememberNavBackStack(MainGraphData.ShowDetails(showId))

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<MainGraphData.ShowDetails> { key ->
                val viewModel =
                    hiltViewModel<ShowDetailsScreenViewModel, ShowDetailsScreenViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(ShowDetailsNavKey(showId = key.showId))
                        })
                ShowDetailsScreen(
                    showId = key.showId,
                    navigateToMoviePlayer = { id, startSeason, startEpisode ->
                        backStack.add(
                            MainGraphData.Player(
                                id,
                                startSeason = startSeason,
                                startEpisode = startEpisode,
                            )
                        )
                    },
                    navigateToEpisodes = { id ->
                        backStack.add(MainGraphData.Episodes(id))
                    },
                    viewModel = viewModel,
                )
            }
            entry<MainGraphData.Episodes> { key ->
                val viewModel =
                    hiltViewModel<EpisodesScreenViewModel, EpisodesScreenViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(EpisodesNavKey(showId = key.showId))
                        })
                EpisodesScreen(
                    showId = key.showId,
                    navigateToPlayer = { id, season, episode ->
                        backStack.add(
                            MainGraphData.Player(
                                id,
                                startSeason = season,
                                startEpisode = episode
                            )
                        )
                    },
                    viewModel = viewModel,
                )
            }
            entry<MainGraphData.Player> { key ->
                val viewModel =
                    hiltViewModel<PlayerScreenViewModel, PlayerScreenViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(
                                PlayerScreenNavKey(
                                    showId = key.showId,
                                    startSeason = key.startSeason,
                                    startEpisode = key.startEpisode,
                                )
                            )
                        })
                PlayerScreen(viewModel = viewModel)
            }
        })
}
