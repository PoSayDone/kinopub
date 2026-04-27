package io.github.posaydone.kinopub.core.common.sharedViewModel

import android.app.Application
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.AspectRatioFrameLayout
import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.kinopub.core.common.services.PlaybackService
import io.github.posaydone.kinopub.core.data.ShowRepository
import io.github.posaydone.kinopub.core.data.SettingsManager
import io.github.posaydone.kinopub.core.model.Episode
import io.github.posaydone.kinopub.core.model.File
import io.github.posaydone.kinopub.core.model.ShowDetails
import io.github.posaydone.kinopub.core.model.Season
import io.github.posaydone.kinopub.core.model.Series
import io.github.posaydone.kinopub.core.model.SessionManager
import io.github.posaydone.kinopub.core.model.findEpisodeProgress
import io.github.posaydone.kinopub.core.model.latestProgressItem
import io.github.posaydone.kinopub.core.model.latestSeriesProgress
import io.github.posaydone.kinopub.core.model.ShowProgress
import io.github.posaydone.kinopub.core.model.ShowProgressItem
import io.github.posaydone.kinopub.core.model.ShowResourceResponse
import io.github.posaydone.kinopub.core.model.Translation
import io.github.posaydone.kinopub.core.model.VideoWithQualities
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
    data class Done(val showDetails: ShowDetails) : VideoPlayerScreenUiState()
}

data class PlayerScreenNavKey(
    val showId: Int,
    val startSeason: Int = -1,
    val startEpisode: Int = -1,
)

@androidx.media3.common.util.UnstableApi
@HiltViewModel(assistedFactory = PlayerScreenViewModel.Factory::class)
class PlayerScreenViewModel @AssistedInject constructor(
    @Assisted val navKey: PlayerScreenNavKey,
    val sessionManager: SessionManager,
    private val repository: ShowRepository,
    private val settingsManager: SettingsManager,
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

    private val _details = MutableStateFlow<ShowDetails?>(null)
    val details = _details.asStateFlow()

    private val _contentType = MutableStateFlow<ShowType?>(null)
    val contentType: StateFlow<ShowType?> = _contentType.asStateFlow()

    private val _currentStreamType = MutableStateFlow<String?>(null)
    val currentStreamType: StateFlow<String?> = _currentStreamType.asStateFlow()

    val isHls4AudioTrackSelectionEnabled: StateFlow<Boolean> = currentStreamType.map {
        it.equals(HLS4_STREAM_TYPE, ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _selectedCrop = MutableStateFlow<String?>("Fit") // Default crop mode
    val selectedCrop: StateFlow<String?> = _selectedCrop.asStateFlow()

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
    private var minuteProgressSaveJob: Job? = null
    private var isPreparingMediaItem = false
    private var pendingResumePositionMs: Long? = null

    fun showControls(seconds: Int = SHOW_CONTROLS_TIME) {
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
                        if (!isPlaying) {
                            saveProgress()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        val isLoading =
                            state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                        _playerState.update { it.copy(isLoading = isLoading) }
                        maybeClearPendingPlaybackState(controller)
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        Log.d(TAG, "onTracksChanged: groups=${tracks.groups.size}")
                        // HLS4 exposes all audio renditions inside one master playlist.
                        // Use TrackSelectionParameters preferences instead of manual overrides.
                        applyAudioSelection(tracks)
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
        minuteProgressSaveJob?.cancel()

        positionJob = viewModelScope.launch {
            while (isActive) {
                playerController.value?.let { player ->
                    maybeClearPendingPlaybackState(player)
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

        // Save progress every minute
        minuteProgressSaveJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000) // Wait for 1 minute (60,000 ms)
                if (playerController.value?.isPlaying == true) { // Only save if currently playing
                    saveProgress()
                }
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
            Log.d(
                TAG,
                "initialize: showId=$showId navStartSeason=${navKey.startSeason} navStartEpisode=${navKey.startEpisode}"
            )
            runCatching { repository.getStreamType() }
                .onSuccess { streamType ->
                    _currentStreamType.value = streamType.streamType
                    Log.d(
                        TAG,
                        "initialize: streamType=${streamType.streamType} allowedTypes=${streamType.allowedTypes}"
                    )
                }
                .onFailure { error ->
                    _currentStreamType.value = null
                    Log.w(
                        TAG,
                        "initialize: failed to fetch stream type, audio track switching disabled",
                        error
                    )
                }
            savedProgress = repository.getShowProgress(showId)
            Log.d(TAG, "initialize: savedProgress=${savedProgress.toDebugString()}")
            when (val response = repository.getShowResource(showId)) {
                is ShowResourceResponse.MovieResourceResponse -> {
                    movie = response.movies
                    _details.value = repository.getShowDetails(showId)
                    _moviePieces.value = movie
                    _contentType.value = ShowType.MOVIE
                    Log.d(
                        TAG,
                        "initialize: contentType=MOVIE translations=${movie.size} firstVoices=${movie.take(3).joinToString { it.voiceover }}"
                    )
                    restoreMovieProgress()
                }

                is ShowResourceResponse.SeriesResourceResponse -> {
                    val seriesTransformed = response.series
                    series = seriesTransformed
                    _details.value = repository.getShowDetails(showId)
                    _contentType.value = ShowType.SERIES
                    _seasons.value = seriesTransformed.seasons
                    Log.d(
                        TAG,
                        "initialize: contentType=SERIES seasons=${seriesTransformed.seasons.size} seasonSummary=${seriesTransformed.seasons.joinToString { "S${it.season}:${it.episodes.size}ep" }}"
                    )
                    restoreSeriesProgress()
                }
            }
        }
    }

    private fun restoreMovieProgress() {
        viewModelScope.launch {
            Log.d(TAG, "restoreMovieProgress: savedProgressCount=${savedProgress.size}")
            val latestProgress = savedProgress.latestProgressItem()
            if (latestProgress != null) {
                restoreMovieSavedProgress(latestProgress)
            } else {
                Log.d(TAG, "restoreMovieProgress: no saved progress, using defaults")
                setDefaultMovieProgress()
            }
        }
    }

    private fun restoreMovieSavedProgress(savedMovie: ShowProgressItem) {
        val savedVoiceover = savedMovie.voiceover.ifBlank { settingsManager.getSavedVoiceTrack(showId) }
        val translation = moviePieces.value?.find { it.voiceover == savedVoiceover }
            ?: moviePieces.value?.getOrNull(0)
        _selectedMovieTranslation.value = translation

        val file = translation?.files?.find {
            it.quality == savedMovie.quality || it.quality == 1080
        } ?: translation?.files?.getOrNull(0)
        _selectedQuality.value = file
        file?.url?.let { _videoUrl.value = it }
        Log.d(
            TAG,
            "restoreMovieSavedProgress: saved=${savedMovie.toDebugString()} savedVoiceover=$savedVoiceover selectedVoice=${translation?.voiceover} selectedAudioIndex=${translation?.audioIndex} selectedQuality=${file?.quality} selectedUrl=${file?.url}"
        )
        logSelectionSnapshot("restoreMovieSavedProgress")
        playVideo(savedMovie.time)
    }

    private fun setDefaultMovieProgress() {
        val savedVoiceover = settingsManager.getSavedVoiceTrack(showId)
        val defaultTranslation = if (savedVoiceover != null) {
            moviePieces.value?.find { it.voiceover == savedVoiceover }
                ?: moviePieces.value?.getOrNull(0)
        } else {
            moviePieces.value?.getOrNull(0)
        }
        _selectedMovieTranslation.value = defaultTranslation

        val defaultFile = defaultTranslation?.files?.getOrNull(0)
        _selectedQuality.value = defaultFile
        defaultFile?.url?.let { _videoUrl.value = it }
        Log.d(
            TAG,
            "setDefaultMovieProgress: savedVoiceover=$savedVoiceover selectedVoice=${defaultTranslation?.voiceover} selectedAudioIndex=${defaultTranslation?.audioIndex} selectedQuality=${defaultFile?.quality} selectedUrl=${defaultFile?.url}"
        )
        logSelectionSnapshot("setDefaultMovieProgress")
        playVideo()
    }

    private fun restoreSeriesProgress() {
        Log.d(
            TAG,
            "restoreSeriesProgress: navStartSeason=${navKey.startSeason} navStartEpisode=${navKey.startEpisode} savedProgress=${savedProgress.toDebugString()}"
        )
        viewModelScope.launch {
            val requestedSeason = navKey.startSeason
            val requestedEpisode = navKey.startEpisode
            if (requestedSeason != -1 && requestedEpisode != -1) {
                Log.d(TAG, "restoreSeriesProgress: using nav key override")
                setSpecificSeriesProgress(requestedSeason, requestedEpisode)
            } else {
                val latestProgress = savedProgress.latestSeriesProgress()
                if (latestProgress != null) {
                    Log.d(TAG, "restoreSeriesProgress: using latest saved series progress")
                    restoreSeriesSavedProgress(latestProgress)
                } else {
                    Log.d(TAG, "restoreSeriesProgress: no nav override or saved progress, using defaults")
                    setDefaultSeriesProgress()
                }
            }
        }
    }

    private fun setSpecificSeriesProgress(seasonNumber: Int, episodeNumber: Int) {
        val savedEpisodeProgress = savedProgress.findEpisodeProgress(
            season = seasonNumber,
            episode = episodeNumber,
        )
        val season = seasons.value?.find { it.season == seasonNumber }
            ?: seasons.value?.getOrNull(0)
        _selectedSeason.value = season

        val episode = season?.episodes?.find { it.episode == episodeNumber }
            ?: season?.episodes?.getOrNull(0)
        _selectedEpisode.value = episode

        val savedVoiceover = savedEpisodeProgress?.voiceover
            ?.takeIf(String::isNotBlank)
            ?: settingsManager.getSavedVoiceTrack(showId)
        val translation = if (savedVoiceover != null) {
            episode?.translations?.find { it.translation.equals(savedVoiceover, ignoreCase = true) }
                ?: episode?.translations?.getOrNull(0)
        } else {
            episode?.translations?.getOrNull(0)
        }
        _selectedTranslation.value = translation

        val file = translation?.files?.find {
            it.quality == savedEpisodeProgress?.quality || it.quality == 1080
        } ?: translation?.files?.getOrNull(0)
        _selectedQuality.value = file
        file?.url?.let { _videoUrl.value = it }
        Log.d(
            TAG,
            "setSpecificSeriesProgress: requestedSeason=$seasonNumber requestedEpisode=$episodeNumber matchedSaved=${savedEpisodeProgress?.toDebugString()} resolvedSeason=${season?.season} resolvedEpisode=${episode?.episode} savedVoiceover=$savedVoiceover selectedTranslation=${translation?.translation} selectedAudioIndex=${translation?.audioIndex} selectedQuality=${file?.quality} selectedUrl=${file?.url}"
        )
        logSelectionSnapshot("setSpecificSeriesProgress")
        playVideo(savedEpisodeProgress?.time ?: 0)
    }

    private fun restoreSeriesSavedProgress(savedSeries: ShowProgressItem) {
        Log.d(TAG, "restoreSeriesSavedProgress: saved=${savedSeries.toDebugString()}")
        val season = seasons.value?.find { it.season == savedSeries.season }
        _selectedSeason.value = season

        val episode = season?.episodes?.find { it.episode == savedSeries.episode }
        _selectedEpisode.value = episode

        val savedVoiceover = savedSeries.voiceover.ifBlank { settingsManager.getSavedVoiceTrack(showId) }
        val translation = episode?.translations?.find {
            it.translation.equals(savedVoiceover, ignoreCase = true)
        } ?: episode?.translations?.getOrNull(0)
        _selectedTranslation.value = translation

        val file = translation?.files?.find {
            it.quality == savedSeries.quality || it.quality == 1080
        } ?: translation?.files?.getOrNull(0)
        _selectedQuality.value = file

        file?.url?.let {
            _videoUrl.value = it
        }
        Log.d(
            TAG,
            "restoreSeriesSavedProgress: resolvedSeason=${season?.season} resolvedEpisode=${episode?.episode} savedVoiceover=$savedVoiceover selectedTranslation=${translation?.translation} selectedAudioIndex=${translation?.audioIndex} selectedQuality=${file?.quality} selectedUrl=${file?.url}"
        )
        logSelectionSnapshot("restoreSeriesSavedProgress")
        playVideo(savedSeries.time)
    }

    private fun setDefaultSeriesProgress() {
        val defaultSeason = seasons.value?.getOrNull(0)
        _selectedSeason.value = defaultSeason

        val defaultEpisode = defaultSeason?.episodes?.getOrNull(0)
        _selectedEpisode.value = defaultEpisode

        val savedVoiceover = settingsManager.getSavedVoiceTrack(showId)
        val defaultTranslation = if (savedVoiceover != null) {
            defaultEpisode?.translations?.find { it.translation.equals(savedVoiceover, ignoreCase = true) }
                ?: defaultEpisode?.translations?.getOrNull(0)
        } else {
            defaultEpisode?.translations?.getOrNull(0)
        }
        _selectedTranslation.value = defaultTranslation

        val defaultFile = defaultTranslation?.files?.getOrNull(0)
        _selectedQuality.value = defaultFile

        defaultFile?.url?.let {
            _videoUrl.value = it
        }

        Log.d(
            TAG,
            "setDefaultSeriesProgress: savedVoiceover=$savedVoiceover defaultSeason=${defaultSeason?.season} defaultEpisode=${defaultEpisode?.episode} selectedTranslation=${defaultTranslation?.translation} selectedAudioIndex=${defaultTranslation?.audioIndex} selectedQuality=${defaultFile?.quality} selectedUrl=${defaultFile?.url}"
        )
        logSelectionSnapshot("setDefaultSeriesProgress")
        playVideo()
    }

    // Function to set the selected season
    fun setSeason(season: Season) {
        Log.d(
            TAG,
            "setSeason: previousSeason=${selectedSeason.value?.season} newSeason=${season.season} episodeCount=${season.episodes.size}"
        )
        _selectedSeason.value = season
    }

    // Function to set the selected episode
    fun setEpisode(episode: Episode) {
        val oldTranslation = selectedTranslation.value?.translation
        val oldQuality = selectedQuality.value?.quality
        val previousEpisode = selectedEpisode.value?.episode

        Log.d(
            TAG,
            "setEpisode: saving current episode before switch previousEpisode=$previousEpisode targetEpisode=${episode.episode}"
        )
        saveProgress()

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
        Log.d(
            TAG,
            "setEpisode: previousEpisode=$previousEpisode newEpisode=${episode.episode} oldTranslation=$oldTranslation oldQuality=$oldQuality resolvedTranslation=${selectedTranslation.value?.translation} resolvedAudioIndex=${selectedTranslation.value?.audioIndex} resolvedQuality=${selectedQuality.value?.quality} resolvedUrl=${selectedQuality.value?.url}"
        )
        logSelectionSnapshot("setEpisode")
        playVideo()
    }

    private data class SelectedAudioOption(
        val label: String,
        val audioIndex: Int,
    )

    private fun currentSelectedAudioOption(): SelectedAudioOption? {
        return if (contentType.value == ShowType.MOVIE) {
            _selectedMovieTranslation.value?.let {
                SelectedAudioOption(it.voiceover, it.audioIndex)
            }
        } else {
            _selectedTranslation.value?.let {
                SelectedAudioOption(it.translation, it.audioIndex)
            }
        }
    }

    private fun canChangeAudioTrack(): Boolean {
        return isHls4AudioTrackSelectionEnabled.value
    }

    private fun isHlsStream(): Boolean {
        return currentStreamType.value?.contains("hls", ignoreCase = true) == true
    }

    private fun applyVideoQualitySelection(qualityHeight: Int) {
        val controller = playerController.value ?: return
        val maxHeight = if (qualityHeight > 0) qualityHeight else Int.MAX_VALUE
        val params = controller.trackSelectionParameters
            .buildUpon()
            .setMaxVideoSize(Int.MAX_VALUE, maxHeight)
            .build()
        controller.setTrackSelectionParameters(params)
        Log.d(TAG, "applyVideoQualitySelection: qualityHeight=$qualityHeight maxHeight=$maxHeight")
    }

    private fun applyAudioSelection(tracks: Tracks): Boolean {
        if (!canChangeAudioTrack()) {
            Log.d(
                TAG,
                "applyAudio: skipped because streamType=${currentStreamType.value} is not $HLS4_STREAM_TYPE"
            )
            return false
        }
        val controller = playerController.value ?: run {
            Log.w(TAG, "applyAudio: controller is null")
            return false
        }
        if (!controller.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)) {
            Log.w(TAG, "applyAudio: track selection command is unavailable")
            return false
        }
        val selectedAudio = currentSelectedAudioOption() ?: run {
            Log.w(TAG, "applyAudio: no selected audio option")
            return false
        }
        Log.d(
            TAG,
            "applyAudio: contentType=${contentType.value} label=${selectedAudio.label} audioIndex=${selectedAudio.audioIndex}"
        )

        val audioGroups = tracks.groups.filter {
            it.type == C.TRACK_TYPE_AUDIO && it.isSupported(true)
        }

        if (audioGroups.isEmpty()) {
            Log.w(TAG, "applyAudio: no supported audio groups in controller tracks")
            return false
        }

        logAudioGroups(audioGroups)

        val preferredAudioLabels = resolvePreferredAudioLabels(audioGroups, selectedAudio)
        val preferredAudioLanguages = resolvePreferredAudioLanguages(audioGroups, selectedAudio)
        if (preferredAudioLabels.isEmpty() && preferredAudioLanguages.isEmpty()) {
            Log.w(
                TAG,
                "applyAudio: unable to resolve audio preferences for label=${selectedAudio.label} audioIndex=${selectedAudio.audioIndex}"
            )
            return false
        }

        val currentParameters = controller.trackSelectionParameters
        if (currentParameters.preferredAudioLabels == preferredAudioLabels &&
            currentParameters.preferredAudioLanguages == preferredAudioLanguages
        ) {
            Log.d(
                TAG,
                "applyAudio: preferences already applied labels=$preferredAudioLabels languages=$preferredAudioLanguages"
            )
            return true
        }

        val paramsBuilder = currentParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            .setPreferredAudioLabels(*preferredAudioLabels.toTypedArray())
            .setPreferredAudioLanguages(*preferredAudioLanguages.toTypedArray())

        controller.setTrackSelectionParameters(paramsBuilder.build())
        Log.d(
            TAG,
            "applyAudio: applied preferredLabels=$preferredAudioLabels preferredLanguages=$preferredAudioLanguages"
        )
        return true
    }

    private fun resolvePreferredAudioLabels(
        audioGroups: List<Tracks.Group>,
        selectedAudio: SelectedAudioOption,
    ): List<String> {
        val strongMatches = linkedSetOf<String>()
        val weakMatches = linkedSetOf<String>()
        val indexPrefixes = selectedAudio.audioIndex.toHlsAudioIndexPrefixes()

        audioGroups.forEach { group ->
            for (trackIndex in 0 until group.length) {
                if (!group.isTrackSupported(trackIndex, true)) continue
                val format = group.getTrackFormat(trackIndex)
                val trackLabel = format.label?.trim().orEmpty()
                if (trackLabel.isBlank()) continue

                val hasIndexMatch = indexPrefixes.any { prefix ->
                    trackLabel.startsWith(prefix)
                }
                val hasLabelMatch = matchesSelectedAudio(
                    trackLabel = format.label,
                    trackLanguage = format.language,
                    selectedLabel = selectedAudio.label,
                )

                when {
                    hasIndexMatch && hasLabelMatch -> strongMatches += trackLabel
                    hasIndexMatch || hasLabelMatch -> weakMatches += trackLabel
                }
            }
        }

        return when {
            strongMatches.isNotEmpty() -> strongMatches.toList()
            weakMatches.isNotEmpty() -> weakMatches.toList()
            else -> emptyList()
        }
    }

    private fun resolvePreferredAudioLanguages(
        audioGroups: List<Tracks.Group>,
        selectedAudio: SelectedAudioOption,
    ): List<String> {
        val preferredLabels = resolvePreferredAudioLabels(audioGroups, selectedAudio).toSet()
        if (preferredLabels.isEmpty()) {
            return emptyList()
        }

        val languages = linkedSetOf<String>()
        audioGroups.forEach { group ->
            for (trackIndex in 0 until group.length) {
                if (!group.isTrackSupported(trackIndex, true)) continue
                val format = group.getTrackFormat(trackIndex)
                val trackLabel = format.label?.trim().orEmpty()
                val trackLanguage = format.language?.trim().orEmpty()
                if (trackLabel in preferredLabels && trackLanguage.isNotBlank()) {
                    languages += trackLanguage
                }
            }
        }
        return languages.toList()
    }

    private fun matchesSelectedAudio(
        trackLabel: String?,
        trackLanguage: String?,
        selectedLabel: String,
    ): Boolean {
        val normalizedSelectedLabel = selectedLabel.normalizeAudioLabel()
        if (normalizedSelectedLabel.isEmpty()) return false

        return listOfNotNull(trackLabel, trackLanguage)
            .map { it.normalizeAudioLabel() }
            .any { candidate ->
                candidate == normalizedSelectedLabel ||
                    candidate.contains(normalizedSelectedLabel) ||
                    normalizedSelectedLabel.contains(candidate)
            }
    }

    private fun String.normalizeAudioLabel(): String {
        return lowercase()
            .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
            .trim()
    }

    private fun Int.toHlsAudioIndexPrefixes(): List<String> {
        if (this <= 0) return emptyList()
        val rawIndex = toString()
        val paddedIndex = rawIndex.padStart(2, '0')
        return linkedSetOf("$rawIndex.", "$paddedIndex.").toList()
    }

    private fun logAudioGroups(audioGroups: List<Tracks.Group>) {
        audioGroups.forEachIndexed { groupIndex, group ->
            for (trackIndex in 0 until group.length) {
                val format = group.getTrackFormat(trackIndex)
                Log.d(
                    TAG,
                    "audioGroup[$groupIndex] track[$trackIndex] label=${format.label} language=${format.language} selected=${group.isTrackSelected(trackIndex)} supported=${group.isTrackSupported(trackIndex, true)}"
                )
            }
        }
    }

    fun setTranslation(translation: Translation) {
        if (!canChangeAudioTrack()) {
            Log.d(
                TAG,
                "setTranslation: ignored because streamType=${currentStreamType.value} is not $HLS4_STREAM_TYPE"
            )
            return
        }
        Log.d(TAG, "setTranslation: ${translation.translation} audioIndex=${translation.audioIndex}")
        playerController.value?.let { player ->
            val oldQuality = selectedQuality.value?.quality
            _selectedTranslation.value = translation

            val oldQualityInNewTranslation = translation.files.find { it.quality == oldQuality }
            _selectedQuality.value = oldQualityInNewTranslation ?: translation.files.firstOrNull()
            _videoUrl.value = _selectedQuality.value?.url

            settingsManager.saveVoiceTrack(showId, translation.translation)

            val applied = applyAudioSelection(player.currentTracks)
            Log.d(TAG, "setTranslation: applyAudioSelection returned $applied")
            if (!applied) {
                Log.d(TAG, "setTranslation: audio preferences will be retried onTracksChanged")
            }
            saveProgress()
        }
    }

    fun setMovieTranslation(movieTranslation: VideoWithQualities) {
        if (!canChangeAudioTrack()) {
            Log.d(
                TAG,
                "setMovieTranslation: ignored because streamType=${currentStreamType.value} is not $HLS4_STREAM_TYPE"
            )
            return
        }
        Log.d(TAG, "setMovieTranslation: ${movieTranslation.voiceover} audioIndex=${movieTranslation.audioIndex}")
        playerController.value?.let { player ->
            val oldQuality = selectedQuality.value?.quality
            _selectedMovieTranslation.value = movieTranslation

            val oldQualityInNewTranslation = movieTranslation.files.find { it.quality == oldQuality }
            _selectedQuality.value = oldQualityInNewTranslation ?: movieTranslation.files.firstOrNull()
            _videoUrl.value = _selectedQuality.value?.url

            settingsManager.saveVoiceTrack(showId, movieTranslation.voiceover)

            val applied = applyAudioSelection(player.currentTracks)
            Log.d(TAG, "setMovieTranslation: applyAudioSelection returned $applied")
            if (!applied) {
                Log.d(TAG, "setMovieTranslation: audio preferences will be retried onTracksChanged")
            }
            saveProgress()
        }
    }

    // Function to set the selected quality
    fun setQuality(qualityFile: File) {
        playerController.value?.let { player ->
            _selectedQuality.value = qualityFile
            _videoUrl.value = qualityFile.url
            Log.d(
                TAG,
                "setQuality: newQuality=${qualityFile.quality} url=${qualityFile.url} isHls=${isHlsStream()}"
            )
            logSelectionSnapshot("setQuality")
            if (isHlsStream()) {
                applyVideoQualitySelection(qualityFile.quality)
            } else {
                val currentTime = player.currentPosition / 1000
                playVideo(currentTime)
            }
            saveProgress()
        }
    }


    fun playVideo(time: Long = 0) {
        val quality = selectedQuality.value
        val url = quality?.url
        if (url == null) {
            Log.w(TAG, "playVideo: skipped because selectedQuality/url is null time=$time")
            logSelectionSnapshot("playVideoSkippedNoUrl")
            return
        }
        val controller = playerController.value
        if (controller == null) {
            Log.w(TAG, "playVideo: skipped because controller is null time=$time url=$url")
            logSelectionSnapshot("playVideoSkippedNoController")
            return
        }
        isPreparingMediaItem = true
        pendingResumePositionMs = time.takeIf { it > 0 }?.times(1000L)

        val showTitle = _details.value?.title ?: ""
        val displayTitle = when (_contentType.value) {
            ShowType.SERIES -> {
                val season = _selectedSeason.value?.season
                val episode = _selectedEpisode.value?.episode
                if (season != null && episode != null) {
                    "$showTitle — С$season Э$episode"
                } else {
                    showTitle
                }
            }
            else -> showTitle
        }

        Log.d(
            TAG,
            "playVideo: time=$time url=$url contentType=${_contentType.value} displayTitle=$displayTitle controllerState=${controller.playbackState} controllerPosition=${controller.currentPosition}"
        )
        logSelectionSnapshot("playVideo")

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setDisplayTitle(displayTitle)
                    .setTitle(displayTitle)
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()

        if (isHlsStream()) {
            applyVideoQualitySelection(_selectedQuality.value?.quality ?: 0)
        }

        if (time > 0) {
            controller.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        Log.d(
                            TAG,
                            "playVideo: STATE_READY seekTo=${time * 1000L} currentPositionBeforeSeek=${controller.currentPosition}"
                        )
                        controller.seekTo(time * 1000L)
                        controller.removeListener(this)
                    }
                }
            })
        }
    }

    fun saveProgress() {
        try {
            playerController.value?.let { player ->
                if (shouldSkipProgressSave(player)) {
                    return@let
                }
                val season = selectedSeason.value
                val episode = selectedEpisode.value
                val translation = _selectedTranslation.value
                val movieTranslation = _selectedMovieTranslation.value
                val qualityFile = _selectedQuality.value
                val time = player.currentPosition / 1000

                if (contentType.value == ShowType.MOVIE) {
                    if (movieTranslation != null && qualityFile != null) {
                        settingsManager.saveVoiceTrack(showId, movieTranslation.voiceover)
                        Log.d(
                            TAG,
                            "saveProgress: movie time=$time voiceover=${movieTranslation.voiceover} audioIndex=${movieTranslation.audioIndex} quality=${qualityFile.quality} url=${qualityFile.url}"
                        )
                        viewModelScope.launch {
                            repository.addShowProgress(showId, ShowProgressItem(
                                0, 0, movieTranslation.voiceover, time, qualityFile.quality,
                            ))
                        }
                    }
                } else {
                    if (season != null && episode != null && translation != null && qualityFile != null) {
                        settingsManager.saveVoiceTrack(showId, translation.translation)
                        Log.d(
                            TAG,
                            "saveProgress: series season=${season.season} episode=${episode.episode} time=$time translation=${translation.translation} audioIndex=${translation.audioIndex} quality=${qualityFile.quality} url=${qualityFile.url}"
                        )
                        viewModelScope.launch {
                            repository.addShowProgress(showId, ShowProgressItem(
                                season = season.season,
                                episode = episode.episode,
                                voiceover = translation.translation,
                                time = time,
                                quality = qualityFile.quality,
                            ))
                        }
                    } else {
                        Log.d(
                            TAG,
                            "saveProgress: skipped for series because season=${season?.season} episode=${episode?.episode} translation=${translation?.translation} quality=${qualityFile?.quality}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerScreenViewModel", "Error saving progress: ${e.message}", e)
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
            saveProgress()
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

    fun setCrop(crop: String) {
        _selectedCrop.value = crop
        val resizeMode = when (crop) {
            "Fit" -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            "Fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            "Zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        setResizeMode(resizeMode)
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
        minuteProgressSaveJob?.cancel()
        minuteProgressSaveJob = null
        _playerState.update {
            it.copy(
                isPlaying = false, isLoading = false, currentPosition = 0L, duration = 0L
            )
        }
        super.onCleared()
    }

    private fun logSelectionSnapshot(source: String) {
        Log.d(
            TAG,
            "$source: streamType=${currentStreamType.value} selection season=${selectedSeason.value?.season} episode=${selectedEpisode.value?.episode} translation=${selectedTranslation.value?.translation} movieTranslation=${selectedMovieTranslation.value?.voiceover} quality=${selectedQuality.value?.quality} url=${selectedQuality.value?.url} savedVideoUrl=${videoUrl.value}"
        )
    }

    private fun maybeClearPendingPlaybackState(player: Player) {
        val pendingPosition = pendingResumePositionMs
        if (pendingPosition != null) {
            val clearThreshold = (pendingPosition - 1_000L).coerceAtLeast(0L)
            if (player.playbackState == Player.STATE_READY && player.currentPosition >= clearThreshold) {
                Log.d(
                    TAG,
                    "maybeClearPendingPlaybackState: resume seek applied pending=$pendingPosition current=${player.currentPosition}"
                )
                pendingResumePositionMs = null
                isPreparingMediaItem = false
            }
            return
        }

        if (isPreparingMediaItem && player.playbackState == Player.STATE_READY) {
            Log.d(TAG, "maybeClearPendingPlaybackState: media ready without pending seek")
            isPreparingMediaItem = false
        }
    }

    private fun shouldSkipProgressSave(player: Player): Boolean {
        maybeClearPendingPlaybackState(player)
        if (isPreparingMediaItem) {
            Log.d(
                TAG,
                "saveProgress: skipped because media item is preparing currentPosition=${player.currentPosition} playbackState=${player.playbackState} pendingResumePositionMs=$pendingResumePositionMs"
            )
            return true
        }
        return false
    }

    companion object {
        var SHOW_CONTROLS_TIME = 4
        private const val HLS4_STREAM_TYPE = "hls4"
    }
}

private fun ShowProgress.toDebugString(): String {
    return if (isEmpty()) {
        "[]"
    } else {
        joinToString(prefix = "[", postfix = "]") { it.toDebugString() }
    }
}

private fun ShowProgressItem.toDebugString(): String {
    return "S$season/E$episode t=$time q=$quality voice=$voiceover updatedAt=$updatedAt"
}

enum class ShowType {
    MOVIE, SERIES
}
