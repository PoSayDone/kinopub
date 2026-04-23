package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.KinopubRepository
import io.github.posaydone.filmix.core.data.MovieRepository
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.SessionManager
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
        val fullShow: FullShow,
        val showDetails: io.github.posaydone.filmix.core.model.ShowDetails, // Still need this for isFavorite status
        val showImages: ShowImages,
        val showTrailers: ShowTrailers,
        val showProgress: ShowProgress,
        val toggleFavorites: () -> Unit,
    ) : ShowDetailsScreenUiState()
}

private var TAG = "SWAG"

data class ShowDetailsNavKey(val showId: Int)

@HiltViewModel(assistedFactory = ShowDetailsScreenViewModel.Factory::class)
class ShowDetailsScreenViewModel @AssistedInject constructor(
    @Assisted val navKey: ShowDetailsNavKey,
    private val kinopubRepository: KinopubRepository,
    private val movieRepository: MovieRepository,
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
                    val showDetails = kinopubRepository.getShowDetails(showId) // Need this for toggleFavorites
                    val fullShow = movieRepository.getFullMovieByFilmixId(showId)
                    val images = kinopubRepository.getShowImages(showId)
                    val trailers = kinopubRepository.getShowTrailers(showId)
                    val history = kinopubRepository.getShowProgress(showId)

                    emit(
                        ShowDetailsScreenUiState.Done(
                            fullShow = fullShow,
                            showDetails = showDetails,
                            showImages = images,
                            showTrailers = trailers,
                            showProgress = history,
                            sessionManager = sessionManager,
                            toggleFavorites = { toggleFavorites() }
                        ))
                } catch (error: Exception) {
                    emit(
                        ShowDetailsScreenUiState.Error(
                            message = "Unknown error",
                            onRetry = ::reload
                        )
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShowDetailsScreenUiState.Loading
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
                    kinopubRepository.toggleFavorite(
                        showId = currentShowDetails.id, isFavorite = newFavoriteState
                    )
                }

                if (success) {
                    reload()
                }
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            retryChannel.send(Unit)
        }
    }

}
