package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.KinopubRepository
import io.github.posaydone.filmix.core.data.MovieRepository
import io.github.posaydone.filmix.core.data.SettingsManager
import io.github.posaydone.filmix.core.model.FilmixCategory
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.ShowProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        val featuredShowProgress: ShowProgress,
        val lastSeenShows: ShowList,
        val popularMovies: ShowList,
        val newMovies: ShowList,
        val popularSeries: ShowList,
        val newSeries: ShowList,
        val newConcerts: ShowList,
        val new3d: ShowList,
        val newDocumentaryFilms: ShowList,
        val newDocumentarySeries: ShowList,
        val newTvShows: ShowList,
        val getShowImages: suspend (showId: Int) -> ShowImages,
    ) : HomeScreenUiState()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val kinopubRepository: KinopubRepository,
    private val movieRepository: MovieRepository,
    private val sessionManager: SessionManager,
    settingsManager: SettingsManager,
) : ViewModel() {
    private val retryChannel = Channel<Unit>()
    val showImmersiveBackground: StateFlow<Boolean> =
        settingsManager.homeImmersiveBackgroundEnabled
    val showImmersiveDetails: StateFlow<Boolean> =
        settingsManager.homeImmersiveDetailsEnabled

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = retryChannel.receiveAsFlow().flatMapLatest {
        combine(
            kinopubRepository.getHistoryList(20).mapToResult(),
            kinopubRepository.getPopularList(20, section = FilmixCategory.MOVIE).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.MOVIE).mapToResult(),
            kinopubRepository.getPopularList(20, section = FilmixCategory.SERIES).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.SERIES).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.CONCERT).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.FILM_3D).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.DOCUMENTARY_MOVIE).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.DOCUMENTARY_SERIES).mapToResult(),
            kinopubRepository.getFreshList(20, section = FilmixCategory.TV_SHOW).mapToResult(),
        ) { results ->
            @Suppress("UNCHECKED_CAST")
            val lastSeenResult = results[0] as Result<ShowList>
            val popularMoviesResult = results[1] as Result<ShowList>
            val newMoviesResult = results[2] as Result<ShowList>
            val popularSeriesResult = results[3] as Result<ShowList>
            val newSeriesResult = results[4] as Result<ShowList>
            val newConcertsResult = results[5] as Result<ShowList>
            val new3dResult = results[6] as Result<ShowList>
            val newDocumentaryFilmsResult = results[7] as Result<ShowList>
            val newDocumentarySeriesResult = results[8] as Result<ShowList>
            val newTvShowsResult = results[9] as Result<ShowList>

            val allResults = listOf(
                lastSeenResult,
                popularMoviesResult,
                newMoviesResult,
                popularSeriesResult,
                newSeriesResult,
                newConcertsResult,
                new3dResult,
                newDocumentaryFilmsResult,
                newDocumentarySeriesResult,
                newTvShowsResult,
            )

            val error = allResults.firstOrNull { it.isFailure }
            if (error != null) {
                HomeScreenUiState.Error(
                    sessionManager = sessionManager,
                    message = error.exceptionOrNull()?.message ?: "Unknown error",
                    onRetry = { retry() })
            } else {
                val lastSeenShows = lastSeenResult.getOrThrow()
                val popularMovies = popularMoviesResult.getOrThrow()
                val newMovies = newMoviesResult.getOrThrow()
                val popularSeries = popularSeriesResult.getOrThrow()
                val newSeries = newSeriesResult.getOrThrow()
                val newConcerts = newConcertsResult.getOrThrow()
                val new3d = new3dResult.getOrThrow()
                val newDocumentaryFilms = newDocumentaryFilmsResult.getOrThrow()
                val newDocumentarySeries = newDocumentarySeriesResult.getOrThrow()
                val newTvShows = newTvShowsResult.getOrThrow()

                val featuredShowId = listOf(
                    lastSeenShows,
                    popularMovies,
                    newMovies,
                    popularSeries,
                    newSeries,
                ).firstNotNullOfOrNull { it.firstOrNull()?.id }
                    ?: throw IllegalStateException("No content available for the home screen.")

                val fullShow = movieRepository.getFullMovieByFilmixId(featuredShowId)
                val featuredShowProgress = runCatching {
                    kinopubRepository.getShowProgress(featuredShowId)
                }.getOrDefault(emptyList())

                HomeScreenUiState.Done(
                    sessionManager = sessionManager,
                    featuredShow = fullShow,
                    featuredShowProgress = featuredShowProgress,
                    lastSeenShows = lastSeenShows,
                    popularMovies = popularMovies,
                    newMovies = newMovies,
                    popularSeries = popularSeries,
                    newSeries = newSeries,
                    newConcerts = newConcerts,
                    new3d = new3d,
                    newDocumentaryFilms = newDocumentaryFilms,
                    newDocumentarySeries = newDocumentarySeries,
                    newTvShows = newTvShows,
                    getShowImages = { kinopubRepository.getShowImages(it) })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState.Loading
    )

    init {
        retry()
    }

    fun retry() {
        viewModelScope.launch {
            retryChannel.send(Unit)
        }
    }
}

private fun <T> Flow<T>.mapToResult(): Flow<Result<T>> =
    this.map { Result.success(it) }.catch { emit(Result.failure(it)) }
