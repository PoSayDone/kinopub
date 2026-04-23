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
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowResourceResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EpisodesNavKey(val showId: Int)

sealed class EpisodesScreenUiState {
    data object Loading : EpisodesScreenUiState()
    data class Error(val onRetry: () -> Unit) : EpisodesScreenUiState()
    data class Done(
        val fullShow: FullShow,
        val seasons: List<Season>,
        val showProgress: ShowProgress,
    ) : EpisodesScreenUiState()
}

@HiltViewModel(assistedFactory = EpisodesScreenViewModel.Factory::class)
class EpisodesScreenViewModel @AssistedInject constructor(
    @Assisted val navKey: EpisodesNavKey,
    private val kinopubRepository: KinopubRepository,
    private val movieRepository: MovieRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: EpisodesNavKey): EpisodesScreenViewModel
    }

    private val retryChannel = Channel<Unit>(Channel.CONFLATED)

    val uiState = retryChannel.receiveAsFlow()
        .flatMapLatest {
            flow {
                try {
                    val showId = navKey.showId
                    val fullShow = movieRepository.getFullMovieByFilmixId(showId)
                    val resource = kinopubRepository.getShowResource(showId)
                    val progress = kinopubRepository.getShowProgress(showId)

                    val seasons = when (resource) {
                        is ShowResourceResponse.SeriesResourceResponse -> resource.series.seasons
                        is ShowResourceResponse.MovieResourceResponse -> emptyList()
                    }

                    emit(EpisodesScreenUiState.Done(fullShow, seasons, progress))
                } catch (e: Exception) {
                    emit(EpisodesScreenUiState.Error(onRetry = ::reload))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EpisodesScreenUiState.Loading,
        )

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch { retryChannel.send(Unit) }
    }
}
