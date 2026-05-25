package io.github.posaydone.kinopub.core.common.sharedViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.kinopub.core.data.AuthRepository
import io.github.posaydone.kinopub.core.data.ShowRepository
import io.github.posaydone.kinopub.core.data.updates.AppUpdateDownloadState
import io.github.posaydone.kinopub.core.data.updates.AppUpdateRepository
import io.github.posaydone.kinopub.core.model.DeviceAuthorizationStatus
import io.github.posaydone.kinopub.core.model.SessionManager
import io.github.posaydone.kinopub.core.network.ApiUrlManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


sealed class AuthScreenUiState {
    data object Idle : AuthScreenUiState()
    data object Loading : AuthScreenUiState()
    data class AwaitingActivation(
        val userCode: String,
        val verificationUri: String,
    ) : AuthScreenUiState()

    data object Success : AuthScreenUiState()
    data class Error(val message: String) : AuthScreenUiState()
}

@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val showRepository: ShowRepository,
    private val apiUrlManager: ApiUrlManager,
    private val appUpdateRepository: AppUpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthScreenUiState>(AuthScreenUiState.Idle)
    val uiState: StateFlow<AuthScreenUiState> = _uiState
    private var pollingJob: Job? = null

    val apiUrl: StateFlow<String> = apiUrlManager.apiUrl

    private val _appUpdateState = MutableStateFlow(
        AppUpdateUiState(currentVersionName = appUpdateRepository.getCurrentVersionName()),
    )
    val appUpdateState: StateFlow<AppUpdateUiState> = _appUpdateState.asStateFlow()

    fun authorizeUser() {
        pollingJob?.cancel()
        viewModelScope.launch {
            _uiState.value = AuthScreenUiState.Loading
            try {
                val deviceCode = withContext(Dispatchers.IO) {
                    authRepository.requestDeviceCode()
                }

                _uiState.value = AuthScreenUiState.AwaitingActivation(
                    userCode = deviceCode.user_code,
                    verificationUri = deviceCode.verification_uri,
                )
                startPolling(deviceCode.code, deviceCode.interval, deviceCode.expires_in)
            } catch (e: Exception) {
                _uiState.value =
                    AuthScreenUiState.Error(e.message.toString())
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.value = AuthScreenUiState.Idle
    }

    fun updateApiUrl(url: String) {
        apiUrlManager.setApiUrl(url)
    }

    fun resetApiUrl() {
        apiUrlManager.resetToDefault()
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
                    _appUpdateState.value.copy(stage = AppUpdateStage.NO_UPDATE, release = null)
                } else {
                    _appUpdateState.value.copy(stage = AppUpdateStage.AVAILABLE, release = update)
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
        _appUpdateState.value = _appUpdateState.value.copy(pendingInstallApkUri = null)
    }

    private fun startPolling(code: String, pollIntervalSeconds: Long, expiresInSeconds: Long) {
        pollingJob = viewModelScope.launch {
            val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000)
            var currentPollInterval = pollIntervalSeconds.coerceAtLeast(1)

            while (isActive && System.currentTimeMillis() < expiresAt) {
                delay(currentPollInterval * 1000)

                when (val result = withContext(Dispatchers.IO) { authRepository.pollDeviceCode(code) }) {
                    DeviceAuthorizationStatus.Pending -> Unit

                    is DeviceAuthorizationStatus.SlowDown -> {
                        currentPollInterval = (currentPollInterval + result.retryAfterSeconds).coerceAtLeast(1)
                    }

                    is DeviceAuthorizationStatus.Authorized -> {
                        sessionManager.saveAccessToken(
                            result.response.access_token,
                            System.currentTimeMillis() + (result.response.expires_in * 1000)
                        )
                        sessionManager.saveRefreshToken(result.response.refresh_token)
                        runCatching {
                            withContext(Dispatchers.IO) {
                                showRepository.notifyDevice()
                            }
                        }
                        _uiState.value = AuthScreenUiState.Success
                        return@launch
                    }

                    DeviceAuthorizationStatus.Expired -> {
                        _uiState.value = AuthScreenUiState.Error("Activation code expired. Request a new one.")
                        return@launch
                    }

                    is DeviceAuthorizationStatus.Failed -> {
                        _uiState.value = AuthScreenUiState.Error(result.message)
                        return@launch
                    }
                }
            }

            if (isActive) {
                _uiState.value = AuthScreenUiState.Error("Timed out waiting for device activation.")
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
