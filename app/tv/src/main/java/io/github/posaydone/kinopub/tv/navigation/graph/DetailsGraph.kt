package io.github.posaydone.kinopub.tv.navigation.graph

import androidx.annotation.OptIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesNavKey
import io.github.posaydone.kinopub.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenNavKey
import io.github.posaydone.kinopub.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowDetailsNavKey
import io.github.posaydone.kinopub.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.kinopub.shared.graphData.MainGraphData
import io.github.posaydone.kinopub.tv.ui.screen.episodesScreen.EpisodesScreen
import io.github.posaydone.kinopub.tv.ui.screen.playerScreen.PlayerScreen
import io.github.posaydone.kinopub.tv.ui.screen.showDetailsScreen.ShowDetailsScreen

@OptIn(UnstableApi::class)
@Composable
fun DetailsGraph(showId: Int) {
    val backStack = rememberNavBackStack(MainGraphData.ShowDetails(showId))

    NavDisplay(
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { it / 5 }
            ) + fadeIn(tween(300)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                targetOffsetX = { -it / 5 }
            ) + fadeOut(tween(200))
        },
        popTransitionSpec = {
            slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { -it / 5 }
            ) + fadeIn(tween(300)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                targetOffsetX = { it / 5 }
            ) + fadeOut(tween(200))
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialOffsetX = { -it / 5 }
            ) + fadeIn(tween(300)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                targetOffsetX = { it / 5 }
            ) + fadeOut(tween(200))
        },
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
