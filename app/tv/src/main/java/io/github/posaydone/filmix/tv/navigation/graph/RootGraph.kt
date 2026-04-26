package io.github.posaydone.filmix.tv.navigation.graph

import androidx.annotation.OptIn
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.tv.material3.MaterialTheme
import io.github.posaydone.filmix.core.model.AuthEvent
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.shared.graphData.MainGraphData
import io.github.posaydone.filmix.tv.ui.screen.authScreen.AuthScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(UnstableApi::class)
@Composable
fun RootGraph(
    sessionManager: SessionManager,
    authEventFlow: SharedFlow<@JvmSuppressWildcards AuthEvent>,
) {
    val startDestination =
        if (sessionManager.isLoggedIn()) MainGraphData.MainGraph else MainGraphData.Auth
    val backStack = rememberNavBackStack(startDestination)

    LaunchedEffect(key1 = true) {
        authEventFlow.collectLatest { event ->
            when (event) {
                is AuthEvent.ForceLogout -> {
                    backStack.clear()
                    backStack.add(MainGraphData.Auth)
                }
            }
        }
    }

    NavDisplay(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        backStack = backStack, onBack = { backStack.removeLastOrNull() }, entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<MainGraphData.Auth> {
                AuthScreen(navigateToHome = {
                    backStack.clear()
                    backStack.add(MainGraphData.MainGraph)
                })
            }
            entry<MainGraphData.MainGraph> {
                MainGraph()
            }
        })

}
