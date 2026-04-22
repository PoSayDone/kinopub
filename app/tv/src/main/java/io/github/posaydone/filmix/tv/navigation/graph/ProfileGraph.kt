package io.github.posaydone.filmix.tv.navigation.graph

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.posaydone.filmix.core.common.sharedViewModel.ProfileScreenViewModel
import io.github.posaydone.filmix.shared.graphData.ProfileGraphData
import io.github.posaydone.filmix.tv.ui.screen.profileScreen.ProfileScreen

@Composable
fun ProfileGraph(
    viewModel: ProfileScreenViewModel = hiltViewModel(),
) {
    val backStack = rememberNavBackStack(ProfileGraphData.Profile)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<ProfileGraphData.Profile> {
                ProfileScreen(
                    viewModel = viewModel
                )
            }
        })
}
