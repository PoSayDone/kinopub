package io.github.posaydone.filmix.tv.ui.screen.profileScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import io.github.posaydone.filmix.core.common.sharedViewModel.ProfileScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.ProfileScreenViewModel
import io.github.posaydone.filmix.core.model.UserProfileInfo
import io.github.posaydone.filmix.tv.ui.common.Error
import io.github.posaydone.filmix.tv.ui.common.LargeButton
import io.github.posaydone.filmix.tv.ui.common.LargeButtonStyle
import io.github.posaydone.filmix.tv.ui.common.Loading
import io.github.posaydone.filmix.tv.ui.common.SideDialog
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ProfileScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is ProfileScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize(), onRetry = state.onRetry)
        }

        is ProfileScreenUiState.Success -> {
            ProfileScreenContent(
                userProfile = state.userProfile,
                onLogout = { viewModel.logout() },
                currentVideoQuality = videoQuality,
                currentStreamType = state.currentStreamType,
                currentServerLocation = state.currentServerLocation,
                modifier = Modifier.fillMaxSize(),
                onVideoQualityChange = { viewModel.updateDefaultVideoQuality(it) },
                onStreamTypeChange = { viewModel.updateStreamType(it) },
                onServerLocationChange = { viewModel.updateServerLocation(it) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    userProfile: UserProfileInfo,
    onLogout: () -> Unit,
    currentVideoQuality: String,
    currentStreamType: String,
    currentServerLocation: String,
    onVideoQualityChange: (quality: String) -> Unit,
    onStreamTypeChange: (newStreamType: String) -> Unit,
    onServerLocationChange: (newServerLocation: String) -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val videoQualities = ProfileScreenViewModel.videoQualities
    val streamTypes = ProfileScreenViewModel.streamTypes
    val serverLocations = ProfileScreenViewModel.serverLocations

    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showStreamTypeDialog by remember { mutableStateOf(false) }
    var showServerLocationDialog by remember { mutableStateOf(false) }

    val verticalBivs = remember { CustomBringIntoViewSpec(0.5f, 0.5f) }

    val lazyListState = rememberLazyListState();
    val childPadding = rememberChildPadding();

    val proStatus = if (userProfile.isProPlus) {
        "Pro+ (Days left: ${userProfile.proDaysLeft})"
    } else if (userProfile.isPro) {
        "Pro (Days left: ${userProfile.proDaysLeft})"
    } else {
        "Free Account"
    }

    CompositionLocalProvider(LocalBringIntoViewSpec provides verticalBivs) {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = PaddingValues(
                start = childPadding.start,
                end = childPadding.end,
                top = childPadding.top,
                bottom = childPadding.bottom
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            item {
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
            }

            item {
                SettingsGroup(modifier = Modifier, title = "Account") {
                    SettingItem(
                        title = "Username", currentValue = userProfile.login, onClick = {})
                    SettingItem(
                        title = "Email", currentValue = userProfile.email, onClick = {})
                    SettingItem(
                        title = "Subscription", currentValue = proStatus, onClick = {})
                }
            }

            item {
                SettingsGroup(title = "Player") {
                    SettingItem(
                        title = "Video Quality",
                        currentValue = videoQualities[currentVideoQuality] ?: currentVideoQuality,
                        onClick = { showVideoQualityDialog = true })

                    SettingItem(
                        title = "Stream Type",
                        currentValue = streamTypes[currentStreamType] ?: currentStreamType,
                        onClick = { showStreamTypeDialog = true })
                    SettingItem(
                        title = "Server Location",
                        currentValue = serverLocations[currentServerLocation]
                            ?: currentServerLocation,
                        onClick = { showServerLocationDialog = true })
                }
            }


            item {
                LargeButton(
                    onClick = onLogout, style = LargeButtonStyle.OUTLINED
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = "Logout icon"
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "Logout", style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        SettingDialog(
            title = "Video quality",
            description = "Choose the default video quality, which will be used in the player",
            currentValue = currentVideoQuality,
            values = videoQualities,
            onValueSelected = { quality ->
                onVideoQualityChange(quality)
                showVideoQualityDialog = false
            },
            opened = showVideoQualityDialog,
            onDismiss = { showVideoQualityDialog = false })

        // Stream Type Dialog
        SettingDialog(
            title = "Stream type",
            description = "Type of video stream, pick auto if unsure",
            currentValue = currentStreamType,
            values = streamTypes,
            onValueSelected = { streamType ->
                onStreamTypeChange(streamType)
                showStreamTypeDialog = false
            },
            opened = showStreamTypeDialog,
            onDismiss = { showStreamTypeDialog = false })

        // Server Location Dialog
        SettingDialog(
            title = "Server location",
            description = "Pick the nearest location for a better speed",
            currentValue = currentServerLocation,
            values = serverLocations,
            onValueSelected = { serverLocation ->
                onServerLocationChange(serverLocation)
                showServerLocationDialog = false
            },
            opened = showServerLocationDialog,
            onDismiss = { showServerLocationDialog = false },
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    currentValue: String,
    onClick: () -> Unit,
) {
    with(LocalDensity.current) {
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()
        val circleCoordinates = Rect(0f, 0f, 0f, 10000f)

        Card(
            onClick = onClick,
            scale = CardDefaults.scale(focusedScale = 1.05f),
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView(circleCoordinates)
                        }
                    }
                }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String, content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.width(500.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)

        ) {
            content()
        }
    }
}

@Composable
fun SettingDialog(
    title: String,
    description: String?,
    opened: Boolean,
    values: Map<String, String>,
    currentValue: String,
    onValueSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    SideDialog(
        showDialog = opened, onDismissRequest = onDismiss, title = title, description = description
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(values.toList()) { (key, label) ->
                Card(
                    onClick = { onValueSelected(key) },
                    modifier = Modifier.fillMaxWidth(),
                    scale = CardDefaults.scale(focusedScale = 1.05f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label, style = MaterialTheme.typography.bodyLarge
                        )
                        if (key == currentValue) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
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
        contentDescription = "User Avatar",
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}