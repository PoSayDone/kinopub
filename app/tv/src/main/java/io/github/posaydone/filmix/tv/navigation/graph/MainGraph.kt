package io.github.posaydone.filmix.tv.navigation.graph

import android.annotation.SuppressLint
import androidx.annotation.OptIn
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
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.PlayerScreenViewModel
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsNavKey
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowDetailsScreenViewModel
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.shared.graphData.NavBarGraphData
import io.github.posaydone.filmix.shared.graphData.navBarScreenItems
import io.github.posaydone.filmix.shared.util.TopLevelBackStack
import io.github.posaydone.filmix.tv.ui.screen.exploreScreen.ExploreScreen
import io.github.posaydone.filmix.tv.ui.screen.favoritesScreen.FavoritesScreen
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.HomeScreen
import io.github.posaydone.filmix.tv.ui.screen.playerScreen.VideoPlayerScreen
import io.github.posaydone.filmix.tv.ui.screen.showDetailsScreen.ShowDetailsScreen
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
        drawerState = drawerState, drawerContent = {
            if (destination) {
                LazyColumn(
                    userScrollEnabled = false,
                    modifier = Modifier
                        .focusRequester(firstItem)
                        .padding(12.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp, alignment = Alignment.CenterVertically
                    ),
                ) {
                    itemsIndexed(navBarScreenItems) { index, item ->
                        val text = item.title
                        val icon = getIcon(item.icon)
                        val isSelected = item == topLevelBackStack.topLevelKey

                        NavigationDrawerItem(
                            selected = isSelected,
                            onClick = {
                                topLevelBackStack.addTopLevel(item)
                                drawerState.setValue(DrawerValue.Closed)
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
            backStack = topLevelBackStack.backStack,
            onBack = { topLevelBackStack.removeLast() },
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
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
                        showId = key.showId, navigateToMoviePlayer = {
                            topLevelBackStack.add(MainGraphData.Player(key.showId))
                        }, viewModel = viewModel
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
                                factory.create(PlayerScreenNavKey(showId = key.showId))
                            })
                    VideoPlayerScreen(key.showId, viewModel = viewModel)
                }
            })
    }
}
