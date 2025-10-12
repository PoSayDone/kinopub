package io.github.posaydone.filmix.mobile.ui.screen.profileScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.ProfileScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.ProfileScreenViewModel
import io.github.posaydone.filmix.core.model.UserProfileInfo
import io.github.posaydone.filmix.mobile.ui.common.Error
import io.github.posaydone.filmix.mobile.ui.common.LargeButton
import io.github.posaydone.filmix.mobile.ui.common.LargeButtonStyle
import io.github.posaydone.filmix.mobile.ui.common.Loading
import io.github.posaydone.filmix.mobile.ui.common.settings.SettingItemLink
import io.github.posaydone.filmix.mobile.ui.common.settings.SettingsGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateToVideoQualityScreen: () -> Unit,
    navigateToVideoStreamTypeScreen: () -> Unit,
    navigateToVideoServerLocationScreen: () -> Unit,
    viewModel: ProfileScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(
            NavigationBarDefaults.windowInsets
        )
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileScreenUiState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            is ProfileScreenUiState.Error -> {
                Error(modifier = Modifier.fillMaxSize(), onRetry = state.onRetry)
            }

            is ProfileScreenUiState.Success -> {
                ProfileScreenContent(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .padding(paddingValues),
                    userProfile = state.userProfile,
                    onLogout = { viewModel.logout() },
                    navigateToVideoQualityScreen = navigateToVideoQualityScreen,
                    navigateToServerLocationScreen = navigateToVideoServerLocationScreen,
                    navigateToVideoStreamTypeScreen = navigateToVideoStreamTypeScreen,
                    currentVideoQuality = videoQuality,
                    currentStreamType = state.currentStreamType,
                    currentServerLocation = state.currentServerLocation,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    userProfile: UserProfileInfo,
    onLogout: () -> Unit,
    navigateToVideoQualityScreen: () -> Unit,
    navigateToVideoStreamTypeScreen: () -> Unit,
    navigateToServerLocationScreen: () -> Unit,
    currentVideoQuality: String,
    currentStreamType: String,
    currentServerLocation: String,
) {
    val videoQualities = ProfileScreenViewModel.videoQualities
    val streamTypes = ProfileScreenViewModel.streamTypes
    val serverLocations = ProfileScreenViewModel.serverLocations

    val proStatus = if (userProfile.isProPlus) {
        "Pro+ (Days left: ${userProfile.proDaysLeft})"
    } else if (userProfile.isPro) {
        "Pro (Days left: ${userProfile.proDaysLeft})"
    } else {
        "Free Account"
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserAvatar(userProfile.avatar)

            Text(
                text = userProfile.displayName ?: userProfile.login,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium)
            )

        }
        SettingsGroup(
            title = stringResource(R.string.account)
        ) {
            SettingItemLink(
                title = stringResource(R.string.username), currentValue = userProfile.login, onClick = {})
            SettingItemLink(
                title = stringResource(R.string.email), currentValue = userProfile.email, onClick = {})
            SettingItemLink(
                title = stringResource(R.string.subscription), currentValue = proStatus, onClick = {})
        }
        SettingsGroup(
            title = stringResource(R.string.player)
        ) {

            SettingItemLink(
                title = stringResource(R.string.video_quality),
                currentValue = videoQualities[currentVideoQuality] ?: currentVideoQuality,
                onClick = {
                    navigateToVideoQualityScreen()
                })
            SettingItemLink(
                title = stringResource(R.string.stream_type),
                currentValue = streamTypes[currentStreamType] ?: currentStreamType,
                onClick = {
                    navigateToVideoStreamTypeScreen()
                })
            SettingItemLink(
                title = stringResource(R.string.server_location),
                currentValue = serverLocations[currentServerLocation] ?: currentServerLocation,
                onClick = {
                    navigateToServerLocationScreen()
                })
        }
        LargeButton(
            style = LargeButtonStyle.TEXT, onClick = onLogout, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                contentDescription = stringResource(R.string.logout_icon), imageVector = Icons.AutoMirrored.Rounded.Logout
            )
            Spacer(Modifier.size(12.dp))
            Text(text = stringResource(R.string.logout))
        }
    }
}


@Composable
fun UserAvatar(avatarUrl: String?) {
    val painter = rememberAsyncImagePainter(
        model = avatarUrl,
        placeholder = rememberVectorPainter(image = Icons.Default.Person),
        error = rememberVectorPainter(image = Icons.Default.Person),
    )

    Image(
        painter = painter,
        contentDescription = stringResource(R.string.user_avatar),
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    )
}