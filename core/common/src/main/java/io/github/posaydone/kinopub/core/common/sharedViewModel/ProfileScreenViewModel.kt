package io.github.posaydone.kinopub.core.common.sharedViewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.kinopub.core.data.ShowRepository
import io.github.posaydone.kinopub.core.data.SettingsManager
import io.github.posaydone.kinopub.core.data.updates.AppUpdateDownloadState
import io.github.posaydone.kinopub.core.data.updates.AppUpdateRepository
import io.github.posaydone.kinopub.core.model.SessionManager
import io.github.posaydone.kinopub.core.model.UserProfileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
sealed interface ProfileScreenUiState {
    data object Loading : ProfileScreenUiState
    data class Error(val message: String, val onRetry: () -> Unit) : ProfileScreenUiState
    data class Success(
        val currentStreamType: String,
        val streamTypes: Map<String, String>,
        val currentServerLocation: String,
        val serverLocations: Map<String, String>,
        val userProfile: UserProfileInfo,
    ) : ProfileScreenUiState
}

@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val sessionManager: SessionManager,
    private val settingsManager: SettingsManager,
    private val appUpdateRepository: AppUpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileScreenUiState>(ProfileScreenUiState.Loading)
    val uiState: StateFlow<ProfileScreenUiState> = _uiState
    private val _appUpdateState = MutableStateFlow(
        AppUpdateUiState(
            currentVersionName = appUpdateRepository.getCurrentVersionName(),
        ),
    )
    val appUpdateState: StateFlow<AppUpdateUiState> = _appUpdateState.asStateFlow()

    val videoQuality: StateFlow<String> = settingsManager.videoQuality
    val homeImmersiveBackgroundEnabled: StateFlow<Boolean> =
        settingsManager.homeImmersiveBackgroundEnabled
    val homeImmersiveGradientEnabled: StateFlow<Boolean> =
        settingsManager.homeImmersiveGradientEnabled
    val homeImmersiveDetailsEnabled: StateFlow<Boolean> =
        settingsManager.homeImmersiveDetailsEnabled

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = ProfileScreenUiState.Loading

        viewModelScope.launch {
            try {
                val streamTypeResponse = repository.getStreamType()
                val serverLocationResponse = repository.getServerLocation()
                val userProfile = repository.getUserProfile()

                _uiState.value = ProfileScreenUiState.Success(
                    currentStreamType = streamTypeResponse.streamType,
                    streamTypes = streamTypeResponse.labels,
                    currentServerLocation = serverLocationResponse.serverLocation,
                    serverLocations = serverLocationResponse.labels,
                    userProfile = userProfile,
                )
            } catch (e: Exception) {
                _uiState.value = ProfileScreenUiState.Error(
                    message = e.message ?: "Unknown error", onRetry = { loadSettings() })
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                // Even if the backend logout fails, we still want to clear the local session
            }
            sessionManager.logout()
        }
    }

    fun updateStreamType(newStreamType: String) {
        viewModelScope.launch {
            try {
                val success = repository.updateStreamType(newStreamType)
                if (success) {
                    val currentState = _uiState.value
                    if (currentState is ProfileScreenUiState.Success) {
                        _uiState.value = currentState.copy(currentStreamType = newStreamType)
                    }
                }
            } catch (e: Exception) {
                // Handle error - maybe revert the UI or show error message
            }
        }
    }

    fun updateServerLocation(newServerLocation: String) {
        viewModelScope.launch {
            try {
                val success = repository.updateServerLocation(newServerLocation)
                if (success) {
                    val currentState = _uiState.value
                    if (currentState is ProfileScreenUiState.Success) {
                        _uiState.value =
                            currentState.copy(currentServerLocation = newServerLocation)
                    }
                }
            } catch (e: Exception) {
                // Handle error - maybe revert the UI or show error message
            }
        }
    }

    fun updateDefaultVideoQuality(quality: String) {
        settingsManager.setVideoQuality(quality)
    }

    fun updateHomeImmersiveBackgroundEnabled(enabled: Boolean) {
        settingsManager.setHomeImmersiveBackgroundEnabled(enabled)
    }

    fun updateHomeImmersiveGradientEnabled(enabled: Boolean) {
        settingsManager.setHomeImmersiveGradientEnabled(enabled)
    }

    fun updateHomeImmersiveDetailsEnabled(enabled: Boolean) {
        settingsManager.setHomeImmersiveDetailsEnabled(enabled)
    }

    fun checkForAppUpdates() {
        _appUpdateState.value = _appUpdateState.value.copy(
            stage = AppUpdateStage.CHECKING,
            errorMessage = null,
            downloadProgress = null,
            installApkUri = null,
            pendingInstallApkUri = null,
        )

        viewModelScope.launch {
            runCatching {
                appUpdateRepository.fetchUpdate()
            }.onSuccess { update ->
                _appUpdateState.value = if (update == null) {
                    _appUpdateState.value.copy(
                        stage = AppUpdateStage.NO_UPDATE,
                        release = null,
                    )
                } else {
                    _appUpdateState.value.copy(
                        stage = AppUpdateStage.AVAILABLE,
                        release = update,
                    )
                }
            }.onFailure { throwable ->
                _appUpdateState.value = _appUpdateState.value.copy(
                    stage = AppUpdateStage.ERROR,
                    errorMessage = throwable.message ?: "Unknown error",
                )
            }
        }
    }

    fun downloadUpdate() {
        val release = _appUpdateState.value.release ?: return
        _appUpdateState.value = _appUpdateState.value.copy(
            stage = AppUpdateStage.DOWNLOADING,
            errorMessage = null,
            downloadProgress = null,
            installApkUri = null,
            pendingInstallApkUri = null,
        )

        viewModelScope.launch {
            appUpdateRepository.downloadUpdate(release).collect { downloadState ->
                when (downloadState) {
                    is AppUpdateDownloadState.InProgress -> {
                        _appUpdateState.value = _appUpdateState.value.copy(
                            stage = AppUpdateStage.DOWNLOADING,
                            downloadProgress = downloadState.progress,
                        )
                    }

                    is AppUpdateDownloadState.ReadyToInstall -> {
                        _appUpdateState.value = _appUpdateState.value.copy(
                            stage = AppUpdateStage.READY_TO_INSTALL,
                            downloadProgress = 1f,
                            installApkUri = downloadState.apkUri,
                            pendingInstallApkUri = downloadState.apkUri,
                        )
                    }

                    is AppUpdateDownloadState.Failed -> {
                        _appUpdateState.value = _appUpdateState.value.copy(
                            stage = AppUpdateStage.ERROR,
                            errorMessage = downloadState.message,
                            downloadProgress = null,
                            installApkUri = null,
                            pendingInstallApkUri = null,
                        )
                    }
                }
            }
        }
    }

    fun onInstallRequestHandled() {
        _appUpdateState.value = _appUpdateState.value.copy(
            pendingInstallApkUri = null,
        )
    }

    companion object {
        val videoQualities = mapOf(
            "auto" to "Auto",
            "high" to "High",
            "medium" to "Medium",
            "low" to "Low"
        )
    }
}
