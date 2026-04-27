package io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.ProfileScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ProfileScreenViewModel
import io.github.posaydone.kinopub.mobile.ui.common.Error
import io.github.posaydone.kinopub.mobile.ui.common.Loading
import io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.components.SettingScreenContent
import io.github.posaydone.kinopub.mobile.ui.screen.profileScreen.components.SettingScreenTopAppBar

@Composable
fun ServerLocationScreen(
    navigateBack: () -> Unit,
    viewModel: ProfileScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SettingScreenTopAppBar(title = stringResource(R.string.server_location), navigateBack)
        }) { paddingValues ->
        when (val state = uiState) {
            is ProfileScreenUiState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            is ProfileScreenUiState.Error -> {
                Error(modifier = Modifier.fillMaxSize(), onRetry = state.onRetry)
            }

            is ProfileScreenUiState.Success -> {
                SettingScreenContent(
                    paddingValues = paddingValues,
                    values = state.serverLocations,
                    currentValue = state.currentServerLocation,
                    updateValue = { value -> viewModel.updateServerLocation(value) })
            }
        }
    }
}
