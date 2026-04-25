package io.github.posaydone.filmix.tv.navigation.graph

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridQueryType
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowsGridScreenViewModel
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.shared.graphData.NavBarGraphData
import io.github.posaydone.filmix.shared.util.TopLevelBackStack
import io.github.posaydone.filmix.tv.ui.screen.exploreScreen.ExploreScreen
import io.github.posaydone.filmix.tv.ui.screen.favoritesScreen.FavoritesScreen
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.HomeScreen
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.PlayerScreen
import io.github.posaydone.filmix.tv.ui.screen.showsGridScreen.ShowsGridScreen

fun getIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Home" -> Icons.Default.Home
        "Explore" -> Icons.Default.Explore
        "Favorite" -> Icons.Default.Favorite
        "Profile" -> Icons.Default.Person
        else -> Icons.Default.Error
    }
}

fun getLocalizedTitle(context: Context, item: NavBarGraphData): String {
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
    val (drawer, firstItem) = remember { FocusRequester.createRefs() }

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
                NavBarGraph(topLevelBackStack, drawer, firstItem) {
                    HomeScreen(
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(MainGraphData.ShowDetails(showId))
                        },
                        navigateToPlayer = { showId, startSeason, startEpisode ->
                            topLevelBackStack.add(
                                MainGraphData.Player(showId, startSeason, startEpisode)
                            )
                        },
                        navigateToShowsGrid = { grid ->
                            topLevelBackStack.add(grid)
                        },
                    )
                }
            }
            entry<NavBarGraphData.Explore> {
                NavBarGraph(topLevelBackStack, drawer, firstItem) {
                    ExploreScreen(
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(MainGraphData.ShowDetails(showId))
                        }
                    )
                }
            }
            entry<NavBarGraphData.Favorite> {
                NavBarGraph(topLevelBackStack, drawer, firstItem) {
                    FavoritesScreen(
                        navigateToShowDetails = { showId ->
                            topLevelBackStack.add(MainGraphData.ShowDetails(showId))
                        },
                        navigateToPlayer = { showId, startSeason, startEpisode ->
                            topLevelBackStack.add(
                                MainGraphData.Player(showId, startSeason, startEpisode)
                            )
                        },
                        navigateToShowsGrid = { queryType ->
                            val title = when (queryType) {
                                "WATCHING" -> "Я смотрю"
                                "HISTORY" -> "История просмотра"
                                else -> ""
                            }
                            topLevelBackStack.add(MainGraphData.ShowsGrid(queryType, title))
                        },
                    )
                }
            }
            entry<NavBarGraphData.ProfileGraph> {
                NavBarGraph(topLevelBackStack, drawer, firstItem) {
                    ProfileGraph()
                }
            }
            entry<MainGraphData.ShowDetails> { key ->
                DetailsGraph(showId = key.showId)
            }
            entry<MainGraphData.ShowsGrid> { key ->
                val viewModel =
                    hiltViewModel<ShowsGridScreenViewModel, ShowsGridScreenViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(
                                ShowsGridNavKey(
                                    queryType = runCatching { ShowsGridQueryType.valueOf(key.queryType) }
                                        .getOrDefault(ShowsGridQueryType.HISTORY),
                                    title = key.title,
                                    contentType = key.contentType,
                                    sort = key.sort,
                                    period = key.period,
                                )
                            )
                        }
                    )
                ShowsGridScreen(
                    navigateToShowDetails = { showId ->
                        topLevelBackStack.add(MainGraphData.ShowDetails(showId))
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
        }
    )
}
