package io.github.posaydone.filmix.tv.navigation.graph

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.EpisodesScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.tv.ui.screen.episodesScreen.EpisodesScreen
import io.github.posaydone.filmix.shared.graphData.NavBarGraphData
import io.github.posaydone.filmix.shared.graphData.navBarScreenItems
import io.github.posaydone.filmix.shared.util.TopLevelBackStack
import io.github.posaydone.filmix.tv.ui.screen.exploreScreen.ExploreScreen
import io.github.posaydone.filmix.tv.ui.screen.favoritesScreen.FavoritesScreen
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.HomeScreen
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.PlayerScreen
import io.github.posaydone.filmix.tv.ui.screen.showDetailsScreen.ShowDetailsScreen
import io.github.posaydone.filmix.tv.ui.screen.showsGridScreen.ShowsGridScreen
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.tv.material3.MaterialTheme
import io.github.posaydone.filmix.core.common.R

fun getIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Home" -> Icons.Default.Home
        "Explore" -> Icons.Default.Explore
        "Favorite" -> Icons.Default.Favorite
        "Profile" -> Icons.Default.Person
        else -> Icons.Default.Error
    }
}

fun getLocalizedTitle(context: android.content.Context, item: NavBarGraphData): String {
    return when (item) {
        NavBarGraphData.Home -> context.getString(R.string.home)
        NavBarGraphData.Explore -> context.getString(R.string.explore)
        NavBarGraphData.Favorite -> context.getString(R.string.favorite_nav)
        NavBarGraphData.ProfileGraph -> context.getString(R.string.profile)
        else -> item.title
    }
}

@OptIn(UnstableApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainGraph() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(NavBarGraphData.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val destination = navBarScreenItems.any { it == topLevelBackStack.backStack.last() }
    val (drawer, firstItem) = remember { FocusRequester.createRefs() }

    NavigationDrawer(
        modifier = Modifier
            .focusRequester(drawer)
            .focusRestorer(firstItem),
        drawerState = drawerState,
        drawerContent = {
            if (destination) {
                LazyColumn(
                    userScrollEnabled = false,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp, alignment = Alignment.CenterVertically
                    ),
                ) {
                    itemsIndexed(navBarScreenItems) { index, item ->
                        val context = LocalContext.current
                        val text = getLocalizedTitle(context, item)
                        val icon = getIcon(item.icon)
                        val isSelected = item == topLevelBackStack.topLevelKey

                        NavigationDrawerItem(
                            selected = isSelected,
                            onClick = {
                                topLevelBackStack.addTopLevel(item)
                                drawerState.setValue(DrawerValue.Closed)
                                firstItem.requestFocus()
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            },
                        ) {
                            Text(text)
                        }
                    }
                }
            }
        }) {
        NavDisplay(
            modifier = Modifier.focusRequester(firstItem),
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<NavBarGraphData.Home> {
                    HomeScreen(
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(
                                MainGraphData.ShowDetails(
                                    showId
                                )
                            )
                        })
                }
                entry<NavBarGraphData.Explore> {
                    ExploreScreen(
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(
                                MainGraphData.ShowDetails(
                                    showId
                                )
                            )
                        })
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
}
