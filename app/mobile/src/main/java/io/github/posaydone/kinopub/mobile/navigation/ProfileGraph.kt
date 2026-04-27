package io.github.posaydone.kinopub.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.posaydone.kinopub.core.common.sharedViewModel.ProfileScreenViewModel
import io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.ProfileScreen
import io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.settings.ServerLocationScreen
import io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.settings.StreamTypeScreen
import io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.settings.VideoQualityScreen
import io.github.posaydone.kinopub.shared.graphData.ProfileGraphData

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
                    navigateToVideoQualityScreen = { backStack.add(ProfileGraphData.VideoQuality) },
                    navigateToVideoStreamTypeScreen = { backStack.add(ProfileGraphData.StreamType) },
                    navigateToVideoServerLocationScreen = { backStack.add(ProfileGraphData.ServerLocation) },
                    viewModel = viewModel
                )
            }
            entry<ProfileGraphData.VideoQuality> {
                VideoQualityScreen(
                    navigateBack = { backStack.removeAt(backStack.lastIndex) },
                    viewModel = viewModel
                )
            }
            entry<ProfileGraphData.StreamType> {
                StreamTypeScreen(
                    navigateBack = { backStack.removeAt(backStack.lastIndex) },
                    viewModel = viewModel
                )
            }
            entry<ProfileGraphData.ServerLocation> {
                ServerLocationScreen(
                    navigateBack = { backStack.removeAt(backStack.lastIndex) },
                    viewModel = viewModel
                )
            }
        })

}
