package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.KinopubRepository
import io.github.posaydone.filmix.core.data.SettingsManager
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.model.UserProfileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val repository: KinopubRepository,
    private val sessionManager: SessionManager,
    private val settingsManager: SettingsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileScreenUiState>(ProfileScreenUiState.Loading)
    val uiState: StateFlow<ProfileScreenUiState> = _uiState

    val videoQuality: StateFlow<String> = settingsManager.videoQuality
    val homeImmersiveBackgroundEnabled: StateFlow<Boolean> =
        settingsManager.homeImmersiveBackgroundEnabled
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

    fun updateHomeImmersiveDetailsEnabled(enabled: Boolean) {
        settingsManager.setHomeImmersiveDetailsEnabled(enabled)
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
