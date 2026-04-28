package io.github.posaydone.kinopub.mobile.ui.screen.profileScreen

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.AppUpdateStage
import io.github.posaydone.kinopub.core.common.sharedViewModel.AppUpdateUiState
import io.github.posaydone.kinopub.core.common.utils.canInstallPackageUpdates
import kotlin.math.roundToInt

@Composable
internal fun AppUpdateDialog(
    state: AppUpdateUiState,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onAllowInstalls: () -> Unit,
) {
    val context = LocalContext.current
    val canInstallPackages = context.canInstallPackageUpdates()
    val primaryAction = when (state.stage) {
        AppUpdateStage.AVAILABLE -> if (canInstallPackages) onDownload else onAllowInstalls
        AppUpdateStage.READY_TO_INSTALL -> onInstall
        AppUpdateStage.ERROR -> {
            if (state.release != null) {
                if (canInstallPackages) onDownload else onAllowInstalls
            } else {
                null
            }
        }

        else -> null
    }
    val primaryLabel = when (state.stage) {
        AppUpdateStage.AVAILABLE -> {
            if (canInstallPackages) {
                stringResource(R.string.update_download)
            } else {
                stringResource(R.string.update_allow_installs)
            }
        }

        AppUpdateStage.READY_TO_INSTALL -> stringResource(R.string.update_install)
        AppUpdateStage.ERROR -> if (state.release != null) {
            stringResource(R.string.retry)
        } else {
            null
        }

        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (state.stage) {
                    AppUpdateStage.AVAILABLE,
                    AppUpdateStage.DOWNLOADING,
                    AppUpdateStage.READY_TO_INSTALL,
                    AppUpdateStage.ERROR -> stringResource(R.string.update_available_title)

                    else -> stringResource(R.string.check_for_updates)
                },
            )
        },
        text = {
            val release = state.release

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.update_current_version, state.currentVersionName))

                if (release != null) {
                    Text(stringResource(R.string.update_latest_version, release.versionName))
                    Text(
                        stringResource(
                            R.string.update_file_size,
                            Formatter.formatShortFileSize(context, release.apkSizeBytes),
                        ),
                    )
                }

                when (state.stage) {
                    AppUpdateStage.CHECKING -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.checking_for_updates))
                        }
                    }

                    AppUpdateStage.NO_UPDATE -> {
                        Text(stringResource(R.string.no_update_available))
                    }

                    AppUpdateStage.AVAILABLE -> {
                        if (!canInstallPackages) {
                            Text(
                                text = stringResource(R.string.update_unknown_sources_required),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    AppUpdateStage.DOWNLOADING -> {
                        val progress = state.downloadProgress
                        Text(stringResource(R.string.update_downloading))
                        if (progress == null) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text("${(progress * 100f).roundToInt()}%")
                        }
                    }

                    AppUpdateStage.READY_TO_INSTALL -> {
                        Text(stringResource(R.string.update_download_ready))
                    }

                    AppUpdateStage.ERROR -> {
                        Text(
                            text = state.errorMessage ?: stringResource(R.string.update_download_failed),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    AppUpdateStage.IDLE -> {
                        Text(stringResource(R.string.check_for_updates))
                    }
                }

                if (!release?.releaseNotes.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.update_release_notes),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(release.releaseNotes)
                }
            }
        },
        confirmButton = {
            if (primaryAction != null && primaryLabel != null) {
                TextButton(onClick = primaryAction) {
                    Text(primaryLabel)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}
