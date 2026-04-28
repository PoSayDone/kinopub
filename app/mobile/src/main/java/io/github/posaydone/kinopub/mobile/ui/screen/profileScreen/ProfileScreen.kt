package io.github.posaydone.kinopub.mobile.ui.screen.profileScreen

import android.content.ActivityNotFoundException
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.AppUpdateUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ProfileScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.ProfileScreenViewModel
import io.github.posaydone.kinopub.core.common.utils.createInstallPackageIntent
import io.github.posaydone.kinopub.core.common.utils.createUnknownSourcesSettingsIntent
import io.github.posaydone.kinopub.core.model.UserProfileInfo
import io.github.posaydone.kinopub.mobile.ui.common.Error
import io.github.posaydone.kinopub.mobile.ui.common.LargeButton
import io.github.posaydone.kinopub.mobile.ui.common.LargeButtonStyle
import io.github.posaydone.kinopub.mobile.ui.common.Loading
import io.github.posaydone.kinopub.mobile.ui.common.settings.SettingItemLink
import io.github.posaydone.kinopub.mobile.ui.common.settings.SettingsGroup

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
    val appUpdateState by viewModel.appUpdateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAppUpdateDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(appUpdateState.pendingInstallApkUri) {
        val apkUri = appUpdateState.pendingInstallApkUri ?: return@LaunchedEffect
        runCatching {
            context.startActivity(context.createInstallPackageIntent(apkUri))
        }.onFailure { throwable ->
            if (throwable is ActivityNotFoundException) {
                // Leave the install button available in the dialog if no installer is found.
            }
        }
        viewModel.onInstallRequestHandled()
    }

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
                    streamTypes = state.streamTypes,
                    currentServerLocation = state.currentServerLocation,
                    serverLocations = state.serverLocations,
                    appUpdateState = appUpdateState,
                    onAppUpdateClick = {
                        showAppUpdateDialog = true
                        viewModel.checkForAppUpdates()
                    },
                )
            }
        }

        if (showAppUpdateDialog) {
            AppUpdateDialog(
                state = appUpdateState,
                onDismiss = { showAppUpdateDialog = false },
                onDownload = { viewModel.downloadUpdate() },
                onInstall = {
                    appUpdateState.installApkUri?.let { apkUri ->
                        runCatching {
                            context.startActivity(context.createInstallPackageIntent(apkUri))
                        }
                    }
                },
                onAllowInstalls = {
                    runCatching {
                        context.startActivity(context.createUnknownSourcesSettingsIntent())
                    }
                },
            )
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
    streamTypes: Map<String, String>,
    currentServerLocation: String,
    serverLocations: Map<String, String>,
    appUpdateState: AppUpdateUiState,
    onAppUpdateClick: () -> Unit,
) {
    val videoQualities = ProfileScreenViewModel.videoQualities

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
            if (userProfile.email.isNotBlank()) {
                SettingItemLink(
                    title = stringResource(R.string.email),
                    currentValue = userProfile.email,
                    onClick = {}
                )
            }
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
        SettingsGroup(
            title = stringResource(R.string.app_updates)
        ) {
            SettingItemLink(
                title = stringResource(R.string.app_version_label),
                currentValue = appUpdateState.currentVersionName,
                description = stringResource(R.string.check_for_updates),
                onClick = onAppUpdateClick,
            )
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
