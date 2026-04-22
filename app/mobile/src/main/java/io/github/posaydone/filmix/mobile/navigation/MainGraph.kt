package io.github.posaydone.filmix.mobile.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.SearchResultsNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.SearchResultsViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.mobile.ui.screen.episodesScreen.EpisodesScreen
import io.github.posaydone.filmix.mobile.ui.screen.exploreScreen.ExploreScreen
import io.github.posaydone.filmix.mobile.ui.screen.favoritesScreen.FavoritesScreen
import io.github.posaydone.filmix.mobile.ui.screen.homeScreen.HomeScreen
import io.github.posaydone.filmix.mobile.ui.screen.playerScreen.PlayerScreen
import io.github.posaydone.filmix.mobile.ui.screen.searchResults.SearchResultsScreen
import io.github.posaydone.filmix.mobile.ui.screen.showDetailsScreen.ShowDetailsScreen
import io.github.posaydone.filmix.mobile.ui.screen.showsGridScreen.ShowsGridScreen
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.shared.graphData.NavBarGraphData
import io.github.posaydone.filmix.shared.graphData.navBarScreenItems
import io.github.posaydone.filmix.shared.util.TopLevelBackStack

fun getIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Home" -> Icons.Default.Home
        "Explore" -> Icons.Default.Explore
        "Favorite" -> Icons.Default.Favorite
        "Profile" -> Icons.Default.Person
        else -> Icons.Default.Error
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGraph() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(NavBarGraphData.Home) }
    val lastEntry = topLevelBackStack.backStack.lastOrNull()
    val isPlayerScreen = lastEntry is MainGraphData.Player

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavDisplay(
            modifier = Modifier.weight(1f),
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<NavBarGraphData.Home> {
                    HomeScreen(navigateToMoviePlayer = { showId, startSeason, startEpisode ->
                        topLevelBackStack.add(
                            MainGraphData.Player(
                                showId,
                                startSeason = startSeason,
                                startEpisode = startEpisode,
                            )
                        )
                    }, navigateToShowDetails = { showId ->
                        topLevelBackStack.add(
                            MainGraphData.ShowDetails(
                                showId
                            )
                        )
                    })
                }
                entry<NavBarGraphData.Explore> {
                    ExploreScreen({ query -> topLevelBackStack.add(MainGraphData.SearchResults(query)) })
                }
                entry<NavBarGraphData.Favorite> {
                    FavoritesScreen(navigateToShowDetails = { showId ->
                        topLevelBackStack.add(
                            MainGraphData.ShowDetails(
                                showId
                            )
                        )
                    }, navigateToShowsGrid = { queryType ->
                        topLevelBackStack.add(
                            MainGraphData.ShowsGrid(
                                queryType
                            )
                        )
                    })
                }
                entry<NavBarGraphData.ProfileGraph> {
                    ProfileGraph()
                }
                entry<MainGraphData.ShowDetails> { key ->
                    val viewModel =
                        hiltViewModel<ShowDetailsScreenViewModel, ShowDetailsScreenViewModel.Factory>(
                            creationCallback = { factory ->
                                factory.create(ShowDetailsNavKey(showId = key.showId))
                            })
                    ShowDetailsScreen(
                        showId = key.showId,
                        navigateBack = { topLevelBackStack.removeLast() },
                        navigateToMoviePlayer = { showId, startSeason, startEpisode ->
                            topLevelBackStack.add(
                                MainGraphData.Player(
                                    showId,
                                    startSeason = startSeason,
                                    startEpisode = startEpisode,
                                )
                            )
                        },
                        navigateToEpisodes = { showId ->
                            topLevelBackStack.add(MainGraphData.Episodes(showId))
                        },
                        viewModel = viewModel
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
                        navigateBack = { topLevelBackStack.removeLast() },
                        navigateToPlayer = { showId, season, episode ->
                            topLevelBackStack.add(
                                MainGraphData.Player(showId, startSeason = season, startEpisode = episode)
                            )
                        },
                        viewModel = viewModel,
                    )
                }
                entry<MainGraphData.ShowsGrid> {
                    ShowsGridScreen(
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(
                                MainGraphData.ShowDetails(
                                    showId
                                )
                            )
                        },

                        )
                }
                entry<MainGraphData.SearchResults> { key ->
                    val viewModel =
                        hiltViewModel<SearchResultsViewModel, SearchResultsViewModel.Factory>(
                            creationCallback = { factory ->
                                factory.create(SearchResultsNavKey(query = key.query))
                            })
                    SearchResultsScreen(
                        navigateBack = { topLevelBackStack.removeLast() },
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(
                                MainGraphData.ShowDetails(
                                    showId
                                )
                            )
                        },
                        viewModel = viewModel
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
        AnimatedVisibility(visible = !isPlayerScreen) {
            NavigationBar {
                navBarScreenItems.forEach { destination ->
                    val isSelected = destination == topLevelBackStack.topLevelKey
                    NavigationBarItem(selected = isSelected, icon = {
                        Icon(
                            imageVector = getIcon(destination.icon),
                            contentDescription = "$destination icon"
                        )
                    }, onClick = {
                        topLevelBackStack.addTopLevel(destination)
                    })
                }
            }
        }
    }
}
