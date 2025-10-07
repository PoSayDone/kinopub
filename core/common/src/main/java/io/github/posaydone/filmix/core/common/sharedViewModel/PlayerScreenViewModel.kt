package io.github.posaydone.filmix.core.common.sharedViewModel

import android.app.Application
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.AspectRatioFrameLayout

import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.common.services.PlaybackService
import io.github.posaydone.filmix.core.data.FilmixRepository
import io.github.posaydone.filmix.core.data.MovieRepository
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.Series
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowProgressItem
import io.github.posaydone.filmix.core.model.ShowResourceResponse
import io.github.posaydone.filmix.core.model.Translation
import io.github.posaydone.filmix.core.model.VideoWithQualities
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@androidx.media3.common.util.UnstableApi
data class PlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    val orientation: Int = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE,
    val controlsVisible: Boolean = true,
    val isSpeedUpActive: Boolean = false,
)

@Immutable
sealed class VideoPlayerScreenUiState {
    object Loading : VideoPlayerScreenUiState()
    object Error : VideoPlayerScreenUiState()
    data class Done(val showDetails: FullShow) : VideoPlayerScreenUiState()
}

data class PlayerScreenNavKey(val showId: Int)

@androidx.media3.common.util.UnstableApi
@HiltViewModel(assistedFactory = PlayerScreenViewModel.Factory::class)
class PlayerScreenViewModel @AssistedInject constructor(
    @Assisted val navKey: PlayerScreenNavKey,
    val sessionManager: SessionManager,
    private val repository: FilmixRepository,
    private val movieRepository: MovieRepository,
    context: Application,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: PlayerScreenNavKey): PlayerScreenViewModel
    }

    private val TAG: String = "PlayerViewModel"
    private val showId = navKey.showId

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _selectedSeason = MutableStateFlow<Season?>(null)
    val selectedSeason: StateFlow<Season?> = _selectedSeason.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<Episode?>(null)
    val selectedEpisode: StateFlow<Episode?> = _selectedEpisode.asStateFlow()

    private val _selectedTranslation = MutableStateFlow<Translation?>(null)
    val selectedTranslation: StateFlow<Translation?> = _selectedTranslation.asStateFlow()

    private val _selectedQuality = MutableStateFlow<File?>(null)
    val selectedQuality: StateFlow<File?> = _selectedQuality.asStateFlow()

    private val _seasons = MutableStateFlow<List<Season>?>(null)
    val seasons: StateFlow<List<Season>?> = _seasons.asStateFlow()

    private val _moviePieces = MutableStateFlow<List<VideoWithQualities>?>(null)
    val moviePieces: StateFlow<List<VideoWithQualities>?> = _moviePieces.asStateFlow()

    private val _selectedMovieTranslation = MutableStateFlow<VideoWithQualities?>(null)
    val selectedMovieTranslation: StateFlow<VideoWithQualities?> =
        _selectedMovieTranslation.asStateFlow()

    private val _details = MutableStateFlow<FullShow?>(null)
    val details = _details.asStateFlow()

    private val _contentType = MutableStateFlow<ShowType?>(null)
    val contentType: StateFlow<ShowType?> = _contentType.asStateFlow()

    private lateinit var savedProgress: ShowProgress

    // StateFlow for the final video URL
    private val _videoUrl = MutableStateFlow<String?>(null)
    val videoUrl = _videoUrl.asStateFlow()

    val hasNextEpisode: StateFlow<Boolean> = selectedEpisode.map { episode ->
        val currentSeason = selectedSeason.value
        currentSeason?.episodes?.indexOf(episode)?.let { index ->
            index < (currentSeason.episodes.size - 1)
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val hasPrevEpisode: StateFlow<Boolean> = selectedEpisode.map { episode ->
        val currentSeason = selectedSeason.value
        currentSeason?.episodes?.indexOf(episode)?.let { index ->
            index > 0
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)


    val audioAttributes =
        AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

    private lateinit var series: Series 
    private lateinit var movie: List<VideoWithQualities>

    private var positionJob: Job? = null

    private val sessionToken =
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
    private val _playerController = MutableStateFlow<MediaController?>(null)
    private var mediaControllerFuture: ListenableFuture<MediaController>? =
        MediaController.Builder(context, sessionToken).buildAsync()
    var playerController: StateFlow<MediaController?> = _playerController.asStateFlow()


    private var controlsHideJob: Job? = null

    fun showControls(seconds: Int = 8) {
        // Cancel any existing hide job
        controlsHideJob?.cancel()

        _playerState.update { it.copy(controlsVisible = true) }

        // Create a new job to hide controls after the specified time
        if (seconds > 0 && seconds != Int.MAX_VALUE) { // Don't auto-hide if using MAX_VALUE
            controlsHideJob = viewModelScope.launch {
                delay(seconds.toLong() * 1000)
                _playerState.update { it.copy(controlsVisible = false) }
            }
        }
    }

    fun hideControls() {
        controlsHideJob?.cancel()
        _playerState.update { it.copy(controlsVisible = false) }
    }


    fun toggleControls() {
        if (_playerState.value.controlsVisible) {
            hideControls()
        } else {
            showControls()
        }
    }

    init {
        mediaControllerFuture?.let {
            it.addListener({
                val controller = it.get()

                controller.setAudioAttributes(audioAttributes, true)

                controller.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        if (shouldRetry(error)) retryPlay()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update { it.copy(isPlaying = isPlaying) }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        val isLoading =
                            state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                        _playerState.update { it.copy(isLoading = isLoading) }
                    }

                })

                _playerState.update { it.copy(isLoading = true) }
                initialize()
                controller.playWhenReady = true
                _playerController.value = controller
                startTrackingPlayback()
            }, ContextCompat.getMainExecutor(context))
        }

    }

    private fun startTrackingPlayback() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                playerController.value?.let { player ->
                    _playerState.update {
                        it.copy(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0)
                        )
                    }
                }
                delay(500) // Update every 500ms
            }
        }
    }

    fun shouldRetry(error: PlaybackException): Boolean {
        return error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED || error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
    }

    fun retryPlay(retryCount: Int = 3) {
        if (retryCount > 0) {
            _playerController.value.let {
                Handler(Looper.getMainLooper()).postDelayed({
                    playVideo(it!!.currentPosition / 1000) // retry from current time
                }, 2000)
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            savedProgress = repository.getShowProgress(showId)
            when (val response = repository.getShowResource(showId)) {
                is ShowResourceResponse.MovieResourceResponse -> {
                    movie = response.movies
                    _details.value = movieRepository.getFullMovieByFilmixId(showId)
                    _moviePieces.value = movie
                    _contentType.value = ShowType.MOVIE
                    restoreMovieProgress()
                }

                is ShowResourceResponse.SeriesResourceResponse -> {
                    val seriesTransformed = response.series
                    series = seriesTransformed
                    _details.value = movieRepository.getFullMovieByFilmixId(showId)
                    _contentType.value = ShowType.SERIES
                    _seasons.value = seriesTransformed.seasons
                    restoreSeriesProgress()
                }
            }
        }
    }

    private fun restoreMovieProgress() {
        viewModelScope.launch {
            if (savedProgress.isNotEmpty()) {
                restoreMovieSavedProgress(savedProgress.first())
            } else {
                setDefaultMovieProgress()
            }
        }
        movie.get(0).let {
            _selectedMovieTranslation.value = it
            selectedMovieTranslation.value?.files?.get(0).let {
                _selectedQuality.value = it
            }
        }
    }

    private fun restoreMovieSavedProgress(savedMovie: ShowProgressItem) {
        val translation = moviePieces.value?.find { it.voiceover == savedMovie.voiceover }
        _selectedMovieTranslation.value = translation

        val file = translation?.files?.find {
            it.quality == savedMovie.quality || it.quality == 1080
        } ?: translation?.files?.getOrNull(0)
        _selectedQuality.value = file
        file?.url?.let {
            _videoUrl.value = it
        }
        playVideo(savedMovie.time)
    }

    private fun setDefaultMovieProgress() {
        val defaultTranslation = moviePieces.value?.getOrNull(0)
        _selectedMovieTranslation.value = defaultTranslation

        val defaultFile = defaultTranslation?.files?.getOrNull(0)
        _selectedQuality.value = defaultFile

        defaultFile?.url?.let {
            _videoUrl.value = it
        }
        playVideo()
    }

    private fun restoreSeriesProgress() {
        Log.d(TAG, "restoreSeriesProgress: restoring series")
        viewModelScope.launch {
            if (savedProgress.isNotEmpty()) {
                restoreSeriesSavedProgress(savedProgress.first())
            } else {
                setDefaultSeriesProgress()
            }
        }
    }

    private fun restoreSeriesSavedProgress(savedSeries: ShowProgressItem) {
        Log.d(TAG, "restoreSeriesProgress: restoring series saved")
        val season = seasons.value?.find { it.season == savedSeries.season }
        _selectedSeason.value = season

        val episode = season?.episodes?.find { it.episode == savedSeries.episode }
        _selectedEpisode.value = episode

        val translation = episode?.translations?.find {
            it.translation.equals(savedSeries.voiceover, ignoreCase = true)
        }
        _selectedTranslation.value = translation

        val file = translation?.files?.find {
            it.quality == savedSeries.quality || it.quality == 1080
        } ?: translation?.files?.getOrNull(0)
        _selectedQuality.value = file

        file?.url?.let {
            _videoUrl.value = it
        }
        playVideo(savedSeries.time)
    }

    private fun setDefaultSeriesProgress() {
        val defaultSeason = seasons.value?.getOrNull(0)
        _selectedSeason.value = defaultSeason

        val defaultEpisode = defaultSeason?.episodes?.getOrNull(0)
        _selectedEpisode.value = defaultEpisode

        val defaultTranslation = defaultEpisode?.translations?.getOrNull(0)
        _selectedTranslation.value = defaultTranslation

        val defaultFile = defaultTranslation?.files?.getOrNull(0)
        _selectedQuality.value = defaultFile

        defaultFile?.url?.let {
            _videoUrl.value = it
        }

        playVideo()
    }

    // Function to set the selected season
    fun setSeason(season: Season) {
        _selectedSeason.value = season
    }

    // Function to set the selected episode
    fun setEpisode(episode: Episode) {
        val oldTranslation = selectedTranslation.value?.translation
        val oldQuality = selectedQuality.value?.quality

        _selectedEpisode.value = episode

        val oldTranslationInNewEpisode =
            selectedEpisode.value?.translations?.find { it.translation == oldTranslation }
        if (oldTranslationInNewEpisode != null) {
            _selectedTranslation.value = oldTranslationInNewEpisode
            val oldQualityInNewEpisode =
                selectedTranslation.value?.files?.find { it.quality == oldQuality }
            if (oldQualityInNewEpisode != null) {
                _selectedQuality.value = oldQualityInNewEpisode
            } else {
                _selectedQuality.value = selectedTranslation.value?.files?.get(0)
            }

        } else {
            _selectedTranslation.value = selectedEpisode.value?.translations?.get(0)
            _selectedQuality.value = selectedTranslation.value?.files?.get(0)
        }
        _videoUrl.value = selectedQuality.value?.url
        saveProgress()
        playVideo()
    }

    // Function to set the selected translation
    fun setTranslation(translation: Translation) {
        playerController.value?.let { player ->
            val currentTime = player.currentPosition / 1000
            val oldQuality = selectedQuality.value?.quality
            _selectedTranslation.value = translation

            val oldQualityInNewTranslation = translation.files.find { it.quality == oldQuality }
            _selectedQuality.value = oldQualityInNewTranslation ?: translation.files.firstOrNull()

            _videoUrl.value = _selectedQuality.value?.url
            playVideo(currentTime)
            saveProgress()
        }
    }

    fun setMovieTranslation(movieTranslation: VideoWithQualities) {
        playerController.value?.let { player ->
            val currentTime = player.currentPosition / 1000
            val oldQuality = selectedQuality.value?.quality
            _selectedMovieTranslation.value = movieTranslation

            val oldQualityInNewTranslation =
                movieTranslation.files.find { it.quality == oldQuality }
            _selectedQuality.value =
                oldQualityInNewTranslation ?: movieTranslation.files.firstOrNull()

            _videoUrl.value = _selectedQuality.value?.url
            playVideo(currentTime)
            saveProgress()
        }
    }

    // Function to set the selected quality
    fun setQuality(qualityFile: File) {
        playerController.value?.let { player ->
            val currentTime = player.currentPosition / 1000
            _selectedQuality.value = qualityFile
            _videoUrl.value = selectedQuality.value?.url
            playVideo(currentTime)
            saveProgress()
        }
    }


    fun playVideo(time: Long = 0) {
        val url = selectedQuality.value?.url ?: return
        Log.d("video", url)
        val mediaItem = MediaItem.fromUri(Uri.parse(url))

        val controller = playerController.value ?: return

        controller.setMediaItem(mediaItem)
        controller.prepare()

        if (time > 0) {
            controller.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        controller.seekTo(time * 1000L)
                        controller.removeListener(this)
                    }
                }
            })
        }
    }

    fun saveProgress() {
        playerController.value?.let { player ->
            val season = selectedSeason.value
            val episode = selectedEpisode.value
            val translation = _selectedTranslation.value
            val movieTranslation = _selectedMovieTranslation.value
            val qualityFile = _selectedQuality.value
            val time = player.currentPosition / 1000

            if (contentType.value == ShowType.MOVIE) {
                if (movieTranslation != null && qualityFile != null) viewModelScope.launch {
                    val savedSeriesProgress = ShowProgressItem(
                        0,
                        0,
                        movieTranslation.voiceover,
                        time,
                        qualityFile.quality,
                    )
                    repository.addShowProgress(showId, savedSeriesProgress)
                }
            } else {
                if (season != null && episode != null && translation != null && qualityFile != null) viewModelScope.launch {
                    val savedSeriesProgress = ShowProgressItem(
                        season.season,
                        episode.episode,
                        translation.translation,
                        time,
                        qualityFile.quality,
                    )
                    repository.addShowProgress(showId, savedSeriesProgress)
                }
            }
        }
    }

    fun onPlayPauseClick() {
        playerController.value?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }

    fun pause() {
        playerController.value?.let { player ->
            player.pause()
        }
    }

    fun play() {
        playerController.value?.let { player ->
            player.play()
        }
    }

    fun goToPrevEpisode() {
        val currentSeason = selectedSeason.value
        val currentEpisode = selectedEpisode.value
        if (currentSeason != null && currentEpisode != null) {
            val currentIndex = currentSeason.episodes.indexOf(currentEpisode)
            if (currentIndex > 0) {
                setEpisode(currentSeason.episodes[currentIndex - 1])
            } else {
                // Handle moving to the previous season if necessary
                val prevSeasonIndex = _seasons.value?.indexOf(currentSeason)?.minus(1) ?: return
                if (prevSeasonIndex >= 0) {
                    val prevSeason = _seasons.value?.get(prevSeasonIndex)
                    if (!prevSeason?.episodes.isNullOrEmpty()) {
                        setSeason(prevSeason!!)
                        setEpisode(prevSeason.episodes.last())
                    }
                }
            }
        }
    }

    fun goToNextEpisode() {
        val currentSeason = selectedSeason.value
        val currentEpisode = selectedEpisode.value
        if (currentSeason != null && currentEpisode != null) {
            val currentIndex = currentSeason.episodes.indexOf(currentEpisode)
            if (currentIndex < currentSeason.episodes.size - 1) {
                setEpisode(currentSeason.episodes[currentIndex + 1])
            } else {
                // Handle moving to the next season if necessary
                val nextSeasonIndex = _seasons.value?.indexOf(currentSeason)?.plus(1) ?: return
                if (nextSeasonIndex < (_seasons.value?.size ?: 0)) {
                    val nextSeason = _seasons.value?.get(nextSeasonIndex)
                    if (!nextSeason?.episodes.isNullOrEmpty()) {
                        setSeason(nextSeason!!)
                        setEpisode(nextSeason.episodes.first())
                    }
                }
            }
        }
    }

    fun setResizeMode(resizeMode: Int) {
        _playerState.update {
            it.copy(resizeMode = resizeMode)
        }
    }

    fun seekForward() {
        playerController.value?.let { it.seekForward() }
    }

    fun seekBack() {
        playerController.value?.let { it.seekBack() }
    }

    fun seekTo(to: Long) {
        playerController.value?.let { it.seekTo(to) }
    }

    fun enableSpeedUp() {
        playerController.value?.let { player ->
            player.setPlaybackSpeed(2f)
            _playerState.update { it.copy(isSpeedUpActive = true) }
        }
    }

    fun disableSpeedUp() {
        playerController.value?.let { player ->
            player.setPlaybackSpeed(1f)
            _playerState.update { it.copy(isSpeedUpActive = false) }
        }
    }


    override fun onCleared() {
        saveProgress()
        mediaControllerFuture?.let {
            _playerController.value?.let { player ->
                player.stop()
                player.clearMediaItems()
                player.release()
            }
            MediaController.releaseFuture(it)
            _playerController.value = null
        }
        mediaControllerFuture = null
        positionJob?.cancel()
        positionJob = null
        _playerState.update {
            it.copy(
                isPlaying = false, isLoading = false, currentPosition = 0L, duration = 0L
            )
        }
        super.onCleared()
    }
}

enum class ShowType {
    MOVIE, SERIES
}
