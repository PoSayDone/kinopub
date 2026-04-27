package io.github.posaydone.kinopub.core.common.sharedViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.kinopub.core.data.ShowRepository
import io.github.posaydone.kinopub.core.model.SessionManager
import io.github.posaydone.kinopub.core.model.ShowDetails
import io.github.posaydone.kinopub.core.model.ShowProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ShowDetailsVM"

sealed class ShowDetailsScreenUiState {
    data object Loading : ShowDetailsScreenUiState()
    data class Error(val message: String, val onRetry: () -> Unit) : ShowDetailsScreenUiState()
    data class Done(
        val sessionManager: SessionManager,
        val showDetails: ShowDetails,
        val showProgress: ShowProgress,
        val toggleFavorites: () -> Unit,
    ) : ShowDetailsScreenUiState()
}

data class ShowDetailsNavKey(val showId: Int)

private suspend inline fun <T> runLoggedRequest(
    requestName: String,
    showId: Int,
    crossinline block: suspend () -> T,
): T {
    Log.d(TAG, "Request started: $requestName, showId=$showId")

    return try {
        val result = block()
        Log.d(TAG, "Request completed: $requestName, showId=$showId")
        result
    } catch (error: Exception) {
        Log.e(TAG, "Request failed: $requestName, showId=$showId", error)
        throw error
    }
}

@HiltViewModel(assistedFactory = ShowDetailsScreenViewModel.Factory::class)
class ShowDetailsScreenViewModel @AssistedInject constructor(
    @Assisted val navKey: ShowDetailsNavKey,
    private val showRepository: ShowRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: ShowDetailsNavKey): ShowDetailsScreenViewModel
    }

    private val retryChannel = Channel<Unit>(Channel.CONFLATED)

    val uiState = retryChannel.receiveAsFlow()
        .flatMapLatest {
            flow {
                val showId = navKey.showId

                try {
                    Log.d(TAG, "Loading show details screen. showId=$showId")

                    val showDetails = runLoggedRequest("getShowDetails", showId) {
                        showRepository.getShowDetails(showId)
                    }

                    val progress = runLoggedRequest("getShowProgress", showId) {
                        showRepository.getShowProgress(showId)
                    }

                    emit(
                        ShowDetailsScreenUiState.Done(
                            showDetails = showDetails,
                            showProgress = progress,
                            sessionManager = sessionManager,
                            toggleFavorites = { toggleFavorites() }
                        )
                    )
                } catch (error: Exception) {
                    Log.e(TAG, "Failed to load show details screen. showId=$showId", error)

                    emit(
                        ShowDetailsScreenUiState.Error(
                            message = error.message ?: "Unknown error",
                            onRetry = ::reload
                        )
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShowDetailsScreenUiState.Loading,
        )

    init {
        reload()
    }

    fun toggleFavorites() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is ShowDetailsScreenUiState.Done) {
                val currentShowDetails = currentState.showDetails
                val newFavoriteState = !(currentShowDetails.isFavorite ?: false)
                val success = withContext(Dispatchers.IO) {
                    showRepository.toggleFavorite(
                        showId = currentShowDetails.id, isFavorite = newFavoriteState,
                    )
                }
                if (success) reload()
            }
        }
    }

    fun reload() {
        viewModelScope.launch { retryChannel.send(Unit) }
    }
}
