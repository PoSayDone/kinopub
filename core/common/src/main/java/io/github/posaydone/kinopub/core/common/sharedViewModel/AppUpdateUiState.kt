package io.github.posaydone.kinopub.core.common.sharedViewModel

import io.github.posaydone.kinopub.core.data.updates.AppUpdateInfo

enum class AppUpdateStage {
    IDLE,
    CHECKING,
    NO_UPDATE,
    AVAILABLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    ERROR,
}

data class AppUpdateUiState(
    val currentVersionName: String,
    val stage: AppUpdateStage = AppUpdateStage.IDLE,
    val release: AppUpdateInfo? = null,
    val downloadProgress: Float? = null,
    val errorMessage: String? = null,
    val installApkUri: String? = null,
    val pendingInstallApkUri: String? = null,
)
