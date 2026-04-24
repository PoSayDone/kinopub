package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.ShowRepository
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowTrailers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ShowDetailsScreenUiState {
    data object Loading : ShowDetailsScreenUiState()
    data class Error(val message: String, val onRetry: () -> Unit) : ShowDetailsScreenUiState()
    data class Done(
        val sessionManager: SessionManager,
        val showDetails: ShowDetails,
        val showImages: ShowImages,
        val showTrailers: ShowTrailers,
        val showProgress: ShowProgress,
        val toggleFavorites: () -> Unit,
    ) : ShowDetailsScreenUiState()
}

data class ShowDetailsNavKey(val showId: Int)

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
                try {
                    val showId = navKey.showId
                    val showDetails = showRepository.getShowDetails(showId)
                    val images = showRepository.getShowImages(showId)
                    val trailers = showRepository.getShowTrailers(showId)
                    val progress = showRepository.getShowProgress(showId)

                    val backdropUrl = images.frames.firstOrNull()?.url
                        ?: images.posters.firstOrNull()?.url
                        ?: showDetails.backdropUrl
                        ?: showDetails.poster
                    val enriched = showDetails.copy(backdropUrl = backdropUrl)

                    emit(
                        ShowDetailsScreenUiState.Done(
                            showDetails = enriched,
                            showImages = images,
                            showTrailers = trailers,
                            showProgress = progress,
                            sessionManager = sessionManager,
                            toggleFavorites = { toggleFavorites() },
                        )
                    )
                } catch (error: Exception) {
                    emit(ShowDetailsScreenUiState.Error(message = "Unknown error", onRetry = ::reload))
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
