package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.FilmixRepository
import io.github.posaydone.filmix.core.data.MovieRepository
import io.github.posaydone.filmix.core.data.TmdbRepository
import io.github.posaydone.filmix.core.model.FilmixCategory
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "HomeScreenViewModel"

@Immutable
sealed class HomeScreenUiState {
    data object Loading : HomeScreenUiState()
    data class Error(
        val sessionManager: SessionManager,
        val message: String,
        val onRetry: () -> Unit,
    ) : HomeScreenUiState()

    data class Done(
        val sessionManager: SessionManager,
        val featuredShow: FullShow,
        val lastSeenShows: ShowList,
        val viewingShows: ShowList,
        val popularMovies: ShowList,
        val popularSeries: ShowList,
        val popularCartoons: ShowList,
        val getShowImages: suspend (showId: Int) -> ShowImages,
    ) : HomeScreenUiState()
}

@Immutable
sealed interface ImmersiveContentUiState {
    data object Loading : ImmersiveContentUiState
    data class Content(
        val fullShow: FullShow,
    ) : ImmersiveContentUiState

    data object Error : ImmersiveContentUiState
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val filmixRepository: FilmixRepository,
    private val tmdbRepository: TmdbRepository,
    private val movieRepository: MovieRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val retryChannel = Channel<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = retryChannel.receiveAsFlow().flatMapLatest {
        combine(
            filmixRepository.getHistoryList(20).mapToResult(),
            filmixRepository.getViewingList(20).mapToResult(),
            filmixRepository.getPopularList(20, section = FilmixCategory.MOVIE).mapToResult(),
            filmixRepository.getPopularList(20, section = FilmixCategory.SERIES).mapToResult(),
            filmixRepository.getPopularList(20, section = FilmixCategory.CARTOON).mapToResult(),
        ) { lastSeenResult, viewingResult, popularMoviesResult, popularSeriesResult, popularCartoonsResult ->

            val results = listOf(
                lastSeenResult,
                viewingResult,
                popularMoviesResult,
                popularSeriesResult,
                popularCartoonsResult
            )

            val error = results.firstOrNull { it.isFailure }
            if (error != null) {
                HomeScreenUiState.Error(
                    sessionManager = sessionManager,
                    message = error.exceptionOrNull()?.message ?: "Unknown error",
                    onRetry = { retry() })
            } else {
                var show = lastSeenResult.getOrThrow().first()

                val fullShow = movieRepository.getFullMovieByFilmixId(show.id)

                HomeScreenUiState.Done(
                    sessionManager = sessionManager,
                    featuredShow = fullShow,
                    lastSeenShows = lastSeenResult.getOrThrow(),
                    viewingShows = viewingResult.getOrThrow(),
                    popularMovies = popularMoviesResult.getOrThrow(),
                    popularSeries = popularSeriesResult.getOrThrow(),
                    popularCartoons = popularCartoonsResult.getOrThrow(),
                    getShowImages = { filmixRepository.getShowImages(it) })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState.Loading
    )

    private val _immersiveContentState =
        MutableStateFlow<ImmersiveContentUiState>(ImmersiveContentUiState.Loading)
    val immersiveContentState: StateFlow<ImmersiveContentUiState> =
        _immersiveContentState.asStateFlow()

    private val kinopoiskCache = mutableMapOf<Int, ImmersiveContentUiState.Content>()
    private val cancelledRequests = mutableSetOf<Int>()  // Track cancelled requests
    private var fetchJob: Job? = null

    fun onImmersiveShowFocused(show: Show) {
        fetchJob?.cancel()

        // Check if previous request for this show was cancelled
        val wasCancelled = show.id in cancelledRequests
        
        // If it was cancelled, remove it from cache to force a new request
        if (wasCancelled) {
            cancelledRequests.remove(show.id)
            kinopoiskCache.remove(show.id)
        }

        if (kinopoiskCache.containsKey(show.id)) {
            _immersiveContentState.value = kinopoiskCache[show.id]!!
            return
        }

        fetchJob = viewModelScope.launch {
            _immersiveContentState.value = ImmersiveContentUiState.Loading
            try {
                // Use MovieRepository to get full show information
                val fullShow = withContext(Dispatchers.IO) {
                    movieRepository.getFullMovieByFilmixId(show.id)
                }
                
                val content = ImmersiveContentUiState.Content(
                    fullShow = fullShow
                )

                kinopoiskCache[show.id] = content
                _immersiveContentState.value = content

            } catch (e: CancellationException) {
                // Mark this request as cancelled so it will be retried
                cancelledRequests.add(show.id)
                // Don't update UI state when request is cancelled
            } catch (e: Exception) {
                // Only show error if this isn't a cancellation 
                _immersiveContentState.value = ImmersiveContentUiState.Error
            }
        }
    }

    init {
        retry()
    }

    fun retry() {
        viewModelScope.launch {
            retryChannel.send(Unit)
        }
    }
}

/**
 * Extension function to map a Flow to a Result.
 */
private fun <T> Flow<T>.mapToResult(): Flow<Result<T>> =
    this.map { Result.success(it) }.catch { emit(Result.failure(it)) }
