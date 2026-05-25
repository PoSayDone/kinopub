package io.github.posaydone.kinopub.tv.ui.screen.authScreen

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.AuthScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.AuthScreenViewModel
import io.github.posaydone.kinopub.core.common.utils.createInstallPackageIntent
import io.github.posaydone.kinopub.core.common.utils.createUnknownSourcesSettingsIntent
import io.github.posaydone.kinopub.tv.ui.common.LargeButton
import io.github.posaydone.kinopub.tv.ui.common.LargeButtonStyle
import io.github.posaydone.kinopub.tv.ui.screen.profileScreen.ApiUrlDialog
import io.github.posaydone.kinopub.tv.ui.screen.profileScreen.AppUpdateDialog

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AuthScreen(
    navigateToHome: () -> Unit,
    viewModel: AuthScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val apiUrl by viewModel.apiUrl.collectAsStateWithLifecycle()
    val appUpdateState by viewModel.appUpdateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showApiUrlDialog by rememberSaveable { mutableStateOf(false) }
    var showAppUpdateDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthScreenUiState.Success) {
            navigateToHome()
            viewModel.onNavigationHandled()
        }
    }

    LaunchedEffect(appUpdateState.pendingInstallApkUri) {
        val apkUri = appUpdateState.pendingInstallApkUri ?: return@LaunchedEffect
        runCatching {
            context.startActivity(context.createInstallPackageIntent(apkUri))
        }.onFailure { throwable ->
            if (throwable is ActivityNotFoundException) {
                // Keep manual install available in the dialog if no installer is found.
            }
        }
        viewModel.onInstallRequestHandled()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .fillMaxHeight()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.device_sign_in_title),
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.device_sign_in_description),
            style = MaterialTheme.typography.bodyLarge,
        )

        if (uiState is AuthScreenUiState.AwaitingActivation) {
            val activation = uiState as AuthScreenUiState.AwaitingActivation

            Spacer(Modifier.height(32.dp))
            Text(
                text = activation.userCode,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = activation.verificationUri,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.device_sign_in_waiting),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        LargeButton(
            onClick = { viewModel.authorizeUser() },
            enabled = uiState != AuthScreenUiState.Loading,
            style = LargeButtonStyle.FILLED
        ) {
            Text(
                text = if (uiState is AuthScreenUiState.AwaitingActivation) {
                    stringResource(R.string.device_sign_in_refresh)
                } else {
                    stringResource(R.string.device_sign_in_start)
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.size(12.dp))
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.AutoMirrored.Rounded.ArrowRightAlt,
                contentDescription = null
            )
        }

        if (uiState is AuthScreenUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (uiState as AuthScreenUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { showApiUrlDialog = true }) {
                Text(stringResource(R.string.change_api_url))
            }
            OutlinedButton(
                onClick = {
                    showAppUpdateDialog = true
                    viewModel.checkForAppUpdates()
                }
            ) {
                Text(stringResource(R.string.check_for_updates))
            }
        }
    }

    ApiUrlDialog(
        showDialog = showApiUrlDialog,
        currentUrl = apiUrl,
        onDismiss = { showApiUrlDialog = false },
        onConfirm = { newUrl ->
            viewModel.updateApiUrl(newUrl)
            showApiUrlDialog = false
        },
        onReset = {
            viewModel.resetApiUrl()
            showApiUrlDialog = false
        },
    )

    AppUpdateDialog(
        showDialog = showAppUpdateDialog,
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
