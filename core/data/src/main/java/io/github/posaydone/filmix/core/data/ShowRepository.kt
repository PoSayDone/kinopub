package io.github.posaydone.filmix.core.data

import android.os.Build
import io.github.posaydone.filmix.core.data.di.ApplicationScope
import io.github.posaydone.filmix.core.model.Country
import io.github.posaydone.filmix.core.model.Episode as AppEpisode
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.core.model.Genre
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.MaxEpisode
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.Series
import io.github.posaydone.filmix.core.model.ServerLocationResponse
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowImage
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowProgressItem
import io.github.posaydone.filmix.core.model.ShowResourceResponse
import io.github.posaydone.filmix.core.model.ShowStatus
import io.github.posaydone.filmix.core.model.ShowTrailers
import io.github.posaydone.filmix.core.model.StreamTypeResponse
import io.github.posaydone.filmix.core.model.Translation
import io.github.posaydone.filmix.core.model.UserProfileInfo
import io.github.posaydone.filmix.core.model.VideoWithQualities
import io.github.posaydone.filmix.core.model.kinopub.KinoPubCountry
import io.github.posaydone.filmix.core.model.kinopub.KinoPubGenre
import io.github.posaydone.filmix.core.model.kinopub.KinoPubHistoryEntry
import io.github.posaydone.filmix.core.model.kinopub.KinoPubItem
import io.github.posaydone.filmix.core.model.kinopub.KinoPubMedia
import io.github.posaydone.filmix.core.model.kinopub.KinoPubMediaFile
import io.github.posaydone.filmix.core.model.kinopub.KinoPubPeriod
import io.github.posaydone.filmix.core.model.kinopub.KinoPubSeason
import io.github.posaydone.filmix.core.model.kinopub.KinoPubServerLocation
import io.github.posaydone.filmix.core.model.kinopub.KinoPubSettingValue
import io.github.posaydone.filmix.core.model.kinopub.KinoPubStreamingType
import io.github.posaydone.filmix.core.model.kinopub.KinoPubVideoUrls
import io.github.posaydone.filmix.core.model.kinopub.KinoPubWatchingEpisode
import io.github.posaydone.filmix.core.model.kinopub.KinoPubWatchingListItem
import io.github.posaydone.filmix.core.model.kinopub.KinoPubWatchingSerialItem
import io.github.posaydone.filmix.core.model.sortedForResume
import io.github.posaydone.filmix.core.network.service.KinoPubApiService
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ShowRepository @Inject constructor(
    private val kinoPubApiService: KinoPubApiService,
    private val sessionManager: SessionManager,
    @ApplicationScope private val externalScope: CoroutineScope,
) {
    private var favoritesFolderId: Int? = null
    private var streamingTypesCache: List<KinoPubStreamingType>? = null
    private var serverLocationsCache: List<KinoPubServerLocation>? = null

    fun getCatalogList(
        contentType: String?,
        sort: String,
        period: String? = null,
        limit: Int = 20,
        page: Int? = null,
    ): Flow<ShowList> = flow {
        emit(kinoPubApiService.listItems(
            type = contentType,
            page = page,
            perPage = limit,
            sort = sort,
            conditions = periodToCondition(period),
        ).items.map { it.toShow() })
    }

    suspend fun getCatalogPage(
        contentType: String?,
        sort: String,
        period: String? = null,
        genreIds: Set<Int> = emptySet(),
        countryIds: Set<Int> = emptySet(),
        limit: Int = 48,
        page: Int? = null,
    ): List<Show> = kinoPubApiService.listItems(
        type = contentType,
        genre = genreIds.takeIf { it.isNotEmpty() }?.joinToString(","),
        country = countryIds.takeIf { it.isNotEmpty() }?.joinToString(","),
        page = page,
        perPage = limit,
        sort = sort,
        conditions = periodToCondition(period),
    ).items.map { it.toShow() }

    suspend fun getGenres(genreType: String?): List<KinoPubGenre> =
        kinoPubApiService.getGenres(genreType)

    suspend fun getCountries(): List<KinoPubCountry> =
        kinoPubApiService.getCountries()

    suspend fun getWatchingMovies(): List<Show> =
        kinoPubApiService.listWatchingMovies().items.map { it.toShow() }

    suspend fun getWatchingSerials(): List<Show> =
        kinoPubApiService.listWatchingSerials(subscribed = 1).items.map { it.toShow() }

    suspend fun getHistoryList(
        limit: Int = 10,
        page: Int? = null,
    ): List<HistoryShow> = kinoPubApiService.getHistory(
        page = page ?: 1,
        perPage = limit,
    ).history.mapNotNull { it.toHistoryShow() }

    suspend fun getShowsListWithQuery(query: String, limit: Int = 48): List<Show> =
        kinoPubApiService.searchItems(query = query, perPage = limit).items.map { it.toShow() }

    suspend fun getShowDetails(movieId: Int): Show =
        kinoPubApiService.getItemDetails(movieId).item.toShow(withDetails = true)

    suspend fun getShowImages(movieId: Int): ShowImages {
        val item = kinoPubApiService.getItemDetails(movieId, noLinks = 1).item
        val posters = buildList {
            item.posters?.small?.let { add(ShowImage(250, "Small poster", it)) }
            item.posters?.medium?.let { add(ShowImage(375, "Medium poster", it)) }
            item.posters?.big?.let { add(ShowImage(750, "Large poster", it)) }
        }
        val frames = buildList {
            item.posters?.wide?.let { add(ShowImage(1080, "Backdrop", it)) }
            item.videos.orEmpty()
                .mapNotNull { it.thumbnail }
                .distinct()
                .forEach { add(ShowImage(270, "Frame", it)) }
        }
        return ShowImages(frames = frames, posters = posters)
    }

    suspend fun getShowTrailers(movieId: Int): ShowTrailers =
        kinoPubApiService.getTrailer(movieId).trailer.orEmpty().mapIndexedNotNull { index, trailer ->
            val url = trailer.url ?: return@mapIndexedNotNull null
            VideoWithQualities(
                season = "0",
                episode = (index + 1).toString(),
                adSkip = 0,
                title = "Trailer ${index + 1}",
                released = "",
                files = listOf(File(url = url, quality = 720, proPlus = false)),
                voiceover = "Trailer",
                updated = 0,
                uk = false,
                type = "trailer",
            )
        }

    suspend fun getShowProgress(movieId: Int): ShowProgress {
        val item = kinoPubApiService.getWatchingInfo(movieId).item ?: return emptyList()
        val movieProgress = item.videos.orEmpty().mapNotNull { it.toMovieProgress() }.sortedForResume()
        if (movieProgress.isNotEmpty()) return movieProgress
        return item.seasons.orEmpty().flatMap { season ->
            season.episodes.mapNotNull { it.toSeriesProgress(season.number) }
        }.sortedForResume()
    }

    fun addShowProgress(movieId: Int, showProgressItem: ShowProgressItem) {
        externalScope.launch {
            runCatching {
                kinoPubApiService.markWatchingTime(
                    id = movieId,
                    video = showProgressItem.episode.coerceAtLeast(1),
                    time = showProgressItem.time,
                    season = showProgressItem.season.takeIf { it > 0 },
                )
            }
        }
    }

    suspend fun getShowResource(movieId: Int): ShowResourceResponse {
        val item = kinoPubApiService.getItemDetails(movieId).item
        val preferredStreamType = preferredStreamTypeCode()
        return if (item.isSeries()) {
            ShowResourceResponse.SeriesResourceResponse(Series(item.toAppSeasons(preferredStreamType)))
        } else {
            ShowResourceResponse.MovieResourceResponse(item.toMovieResources(preferredStreamType))
        }
    }

    suspend fun getFavoritesPage(
        limit: Int = 48,
        page: Int? = null,
    ): List<Show> {
        val folderId = ensureFavoritesFolderId()
        return kinoPubApiService.listBookmarkItems(folderId, page = page, perPage = limit).items.map { it.toShow() }
    }

    suspend fun getUserProfile(): UserProfileInfo {
        val response = kinoPubApiService.getUser().user
        val profile = UserProfileInfo(
            userId = 0,
            email = "",
            isPro = response.subscription?.active == true,
            isProPlus = false,
            login = response.username,
            avatar = response.profile?.avatar,
            proDate = response.subscription?.end_time?.toString(),
            ga = null,
            server = null,
            displayName = response.profile?.name,
            registerDate = response.reg_date?.toString(),
            proDaysLeft = response.subscription?.days?.roundToInt(),
        )
        sessionManager.saveUsername(profile.login)
        return profile
    }

    suspend fun getStreamType(): StreamTypeResponse {
        val streamingTypes = getStreamingTypes()
        val currentSettings = getCurrentDeviceSettings()
        val labels = streamingTypes.associate { it.code to it.name }
        val currentType = resolveStreamingType(currentSettings["streamingType"], streamingTypes)?.code
            ?: defaultStreamTypeCode(streamingTypes)
        return StreamTypeResponse(streamType = currentType, allowedTypes = labels.keys.toList(), labels = labels)
    }

    suspend fun getServerLocation(): ServerLocationResponse {
        val locations = getServerLocations()
        val currentSettings = getCurrentDeviceSettings()
        val labels = locations.associate { it.location to it.name }
        val currentLocation = resolveServerLocation(currentSettings["serverLocation"], locations)?.location
            ?: locations.firstOrNull()?.location ?: ""
        return ServerLocationResponse(serverLocation = currentLocation, labels = labels)
    }

    suspend fun updateStreamType(streamType: String): Boolean {
        val selectedType = getStreamingTypes().firstOrNull { it.code == streamType } ?: return false
        return updateCurrentDeviceSettings(streamingType = selectedType.id)
    }

    suspend fun updateServerLocation(serverLocation: String): Boolean {
        val selectedLocation = getServerLocations().firstOrNull { it.location == serverLocation } ?: return false
        return updateCurrentDeviceSettings(serverLocation = selectedLocation.id)
    }

    suspend fun notifyDevice() {
        kinoPubApiService.notifyDevice(
            title = "Android KinoPub",
            hardware = Build.DEVICE.lowercase().replace(" ", "_"),
            software = "Android${Build.VERSION.SDK_INT}",
        )
    }

    suspend fun logout() {
        kinoPubApiService.unlink()
    }

    suspend fun toggleFavorite(showId: Int, isFavorite: Boolean): Boolean =
        runCatching {
            val folderId = ensureFavoritesFolderId()
            if (isFavorite) {
                kinoPubApiService.addBookmarkItem(folderId, showId).status == 200
            } else {
                kinoPubApiService.removeBookmarkItem(folderId, showId).status == 200
            }
        }.getOrDefault(false)

    private fun periodToCondition(period: String?): String? {
        val durationSeconds = when (period) {
            KinoPubPeriod.WEEK         -> 7L   * 24 * 3600
            KinoPubPeriod.MONTH        -> 30L  * 24 * 3600
            KinoPubPeriod.THREE_MONTHS -> 90L  * 24 * 3600
            KinoPubPeriod.SIX_MONTHS   -> 180L * 24 * 3600
            KinoPubPeriod.YEAR         -> 365L * 24 * 3600
            else -> return null
        }
        return "created>${System.currentTimeMillis() / 1000 - durationSeconds}"
    }

    private suspend fun getStreamingTypes(): List<KinoPubStreamingType> =
        streamingTypesCache ?: kinoPubApiService.listStreamingTypes().items.also { streamingTypesCache = it }

    private suspend fun getServerLocations(): List<KinoPubServerLocation> =
        serverLocationsCache ?: kinoPubApiService.listServerLocations().items.also { serverLocationsCache = it }

    private suspend fun ensureFavoritesFolderId(): Int {
        favoritesFolderId?.let { return it }
        val existing = kinoPubApiService.listBookmarkFolders().items
            .firstOrNull { it.title.equals(FAVORITES_FOLDER_TITLE, ignoreCase = true) }?.id
        if (existing != null) {
            favoritesFolderId = existing
            return existing
        }
        val created = kinoPubApiService.createBookmarkFolder(FAVORITES_FOLDER_TITLE).folder.id
        favoritesFolderId = created
        return created
    }

    private suspend fun getCurrentDeviceSettings(): Map<String, KinoPubSettingValue> =
        kinoPubApiService.getDeviceSettings(getCurrentDeviceId()).settings

    private suspend fun getCurrentDeviceId(): Int =
        runCatching {
            kinoPubApiService.getCurrentDevice().device.id
        }.getOrElse {
            kinoPubApiService.notifyDevice(
                title = "Android KinoPub",
                hardware = Build.DEVICE.lowercase().replace(" ", "_"),
                software = "Android${Build.VERSION.SDK_INT}",
            )
            kinoPubApiService.getCurrentDevice().device.id
        }

    private suspend fun updateCurrentDeviceSettings(
        streamingType: Int? = null,
        serverLocation: Int? = null,
    ): Boolean {
        val deviceId = getCurrentDeviceId()
        val settings = kinoPubApiService.getDeviceSettings(deviceId).settings
        val streamingTypes = getStreamingTypes()
        val serverLocations = getServerLocations()
        val effectiveStreamingType = streamingType
            ?: resolveStreamingType(settings["streamingType"], streamingTypes)?.id
            ?: streamingTypes.firstOrNull()?.id
            ?: return false
        val effectiveServerLocation = serverLocation
            ?: resolveServerLocation(settings["serverLocation"], serverLocations)?.id
            ?: serverLocations.firstOrNull()?.id
            ?: return false
        return kinoPubApiService.updateDeviceSettings(
            id = deviceId,
            supportSsl = settings["supportSsl"].asFlag(),
            supportHevc = settings["supportHevc"].asFlag(),
            supportHdr = settings["supportHdr"].asFlag(),
            support4k = settings["support4k"].asFlag(),
            mixedPlaylist = settings["mixedPlaylist"].asFlag(),
            streamingType = effectiveStreamingType,
            serverLocation = effectiveServerLocation,
        ).status == 200
    }

    private suspend fun preferredStreamTypeCode(): String {
        val streamingTypes = getStreamingTypes()
        return runCatching {
            resolveStreamingType(getCurrentDeviceSettings()["streamingType"], streamingTypes)?.code
        }.getOrNull() ?: defaultStreamTypeCode(streamingTypes)
    }

    private fun resolveStreamingType(
        settingValue: KinoPubSettingValue?,
        streamingTypes: List<KinoPubStreamingType>,
    ): KinoPubStreamingType? = matchStreamingType(settingValue?.value, streamingTypes)
        ?: settingValue?.label?.let { label ->
            streamingTypes.firstOrNull {
                it.code.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true)
            }
        }

    private fun matchStreamingType(rawValue: Any?, streamingTypes: List<KinoPubStreamingType>): KinoPubStreamingType? =
        when (rawValue) {
            is Number -> streamingTypes.firstOrNull { it.id == rawValue.toInt() }
            is String -> streamingTypes.firstOrNull {
                it.code.equals(rawValue, ignoreCase = true)
                    || it.name.equals(rawValue, ignoreCase = true)
                    || it.id.toString() == rawValue
            }
            is Map<*, *> -> listOf(rawValue["id"], rawValue["value"], rawValue["code"], rawValue["label"], rawValue["name"])
                .firstNotNullOfOrNull { matchStreamingType(it, streamingTypes) }
                ?: rawValue["selected"]?.takeIf(::isSelectedOption)
                    ?.let { matchStreamingType(rawValue["id"] ?: rawValue["value"], streamingTypes) }
            is List<*> -> rawValue.firstNotNullOfOrNull { option ->
                val map = option as? Map<*, *> ?: return@firstNotNullOfOrNull null
                if (isSelectedOption(map["selected"])) {
                    matchStreamingType(map["id"] ?: map["value"] ?: map["code"] ?: map["label"], streamingTypes)
                } else null
            }
            else -> null
        }

    private fun resolveServerLocation(
        settingValue: KinoPubSettingValue?,
        locations: List<KinoPubServerLocation>,
    ): KinoPubServerLocation? = matchServerLocation(settingValue?.value, locations)
        ?: settingValue?.label?.let { label ->
            locations.firstOrNull {
                it.location.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true)
            }
        }

    private fun matchServerLocation(rawValue: Any?, locations: List<KinoPubServerLocation>): KinoPubServerLocation? =
        when (rawValue) {
            is Number -> locations.firstOrNull { it.id == rawValue.toInt() }
            is String -> locations.firstOrNull {
                it.location.equals(rawValue, ignoreCase = true)
                    || it.name.equals(rawValue, ignoreCase = true)
                    || it.id.toString() == rawValue
            }
            is Map<*, *> -> listOf(rawValue["id"], rawValue["value"], rawValue["location"], rawValue["label"], rawValue["name"])
                .firstNotNullOfOrNull { matchServerLocation(it, locations) }
                ?: rawValue["selected"]?.takeIf(::isSelectedOption)
                    ?.let { matchServerLocation(rawValue["id"] ?: rawValue["value"], locations) }
            is List<*> -> rawValue.firstNotNullOfOrNull { option ->
                val map = option as? Map<*, *> ?: return@firstNotNullOfOrNull null
                if (isSelectedOption(map["selected"])) {
                    matchServerLocation(map["id"] ?: map["value"] ?: map["location"] ?: map["label"], locations)
                } else null
            }
            else -> null
        }

    private fun defaultStreamTypeCode(streamingTypes: List<KinoPubStreamingType>): String =
        streamingTypes.firstOrNull { it.code.equals("http", ignoreCase = true) }?.code
            ?: streamingTypes.firstOrNull()?.code
            ?: "hls4"

    private fun isSelectedOption(value: Any?): Boolean = when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value == "1" || value.equals("true", ignoreCase = true)
        else -> false
    }

    private fun KinoPubItem.toShow(withDetails: Boolean = false): Show {
        val isSeries = isSeries()
        val maxSeasonNumber = maxSeasonNumber()
        val maxEpisodeNumber = maxEpisodeNumber()
        return Show(
            id = id,
            title = title,
            originalTitle = title,
            poster = bestPosterUrl(),
            backdropUrl = posters?.wide ?: bestPosterUrl(),
            year = year ?: 0,
            quality = qualityLabel(),
            status = toShowStatus(),
            description = plot?.takeIf { it.isNotBlank() },
            isSeries = isSeries,
            genres = genres.map { Genre(it.id, it.title.slug(), it.title) },
            countries = countries.map { Country(it.id, it.title) },
            ratingKp = kinopoisk_rating?.takeIf { it > 0.0 },
            ratingImdb = imdb_rating?.takeIf { it > 0.0 },
            votesKp = kinopoisk_votes,
            votesImdb = imdb_votes,
            votesPos = positiveVotes(),
            votesNeg = negativeVotes(),
            duration = if (!isSeries) durationMinutes() else null,
            maxEpisode = if (isSeries && maxSeasonNumber != null && maxEpisodeNumber != null) {
                MaxEpisode(season = maxSeasonNumber, episode = maxEpisodeNumber)
            } else null,
            isFavorite = if (withDetails) in_watchlist == true else null,
            isDeferred = if (withDetails) in_watchlist else null,
            cast = if (withDetails) cast?.takeIf { it.isNotBlank() } else null,
            director = if (withDetails) director?.takeIf { it.isNotBlank() } else null,
        )
    }

    private fun KinoPubHistoryEntry.toHistoryShow(): HistoryShow? {
        val historyItem = item ?: return null
        val isSeries = historyItem.isSeries()
        return HistoryShow(
            id = historyItem.id,
            title = historyItem.title,
            poster = historyItem.bestPosterUrl(),
            isSeries = isSeries,
            description = historyItem.plot.orEmpty(),
            thumbnail = media?.thumbnail,
            watchedSeconds = media?.time ?: time,
            durationSeconds = media?.duration ?: historyItem.durationMinutes()?.let { it * 60 },
            seasonNumber = media?.snumber?.takeIf { isSeries && it > 0 },
            episodeNumber = media?.number?.takeIf { isSeries },
            episodeTitle = media?.title?.takeIf { it.isNotBlank() && isSeries },
            watchedAtSeconds = last_seen ?: first_seen,
            backdropUrl = historyItem.posters?.wide ?: historyItem.bestPosterUrl(),
            genres = historyItem.genres.map { it.title },
            countries = historyItem.countries.map { it.title },
            ratingKp = historyItem.kinopoisk_rating?.takeIf { it > 0.0 },
            ratingImdb = historyItem.imdb_rating?.takeIf { it > 0.0 },
            votesKp = historyItem.kinopoisk_votes,
            year = historyItem.year ?: 0,
            movieLength = if (!isSeries) historyItem.durationMinutes() else null,
        )
    }

    private fun KinoPubWatchingListItem.toShow(): Show {
        val posterUrl = posters?.big ?: posters?.medium ?: posters?.small ?: posters?.wide.orEmpty()
        return Show(
            id = id,
            title = title,
            originalTitle = title,
            poster = posterUrl,
            backdropUrl = posters?.wide ?: posterUrl,
            year = 0,
        )
    }

    private fun KinoPubWatchingSerialItem.toShow(): Show {
        val posterUrl = posters?.big ?: posters?.medium ?: posters?.small ?: posters?.wide.orEmpty()
        return Show(
            id = id,
            title = title,
            originalTitle = title,
            poster = posterUrl,
            backdropUrl = posters?.wide ?: posterUrl,
            year = 0,
            isSeries = true,
            maxEpisode = new?.let { MaxEpisode(episode = it) },
        )
    }

    private suspend fun KinoPubItem.toMovieResources(preferredStreamType: String): List<VideoWithQualities> {
        val movieMedia = videos.orEmpty().ifEmpty { seasons.orEmpty().flatMap { it.episodes } }
        return movieMedia.flatMapIndexed { index, media ->
            val files = media.toAppFiles(preferredStreamType)
            if (files.isEmpty()) return@flatMapIndexed emptyList()
            val audioTracks = media.audios.orEmpty()
            if (audioTracks.isEmpty()) {
                listOf(VideoWithQualities(
                    season = (media.snumber ?: 0).toString(),
                    episode = (media.number ?: index + 1).toString(),
                    adSkip = 0, title = media.title.orEmpty(), released = "",
                    files = files,
                    voiceover = media.primaryAudioLabel(defaultLabel = voice ?: "Default"),
                    updated = (updated_at ?: 0L).toInt(), uk = false, type = type, audioIndex = 1,
                ))
            } else {
                val seen = mutableSetOf<String>()
                audioTracks.mapIndexedNotNull { i, audio ->
                    val label = audio.author?.title ?: audio.type?.title ?: audio.lang ?: voice ?: "Default"
                    if (!seen.add(label)) return@mapIndexedNotNull null
                    VideoWithQualities(
                        season = (media.snumber ?: 0).toString(),
                        episode = (media.number ?: index + 1).toString(),
                        adSkip = 0, title = media.title.orEmpty(), released = "",
                        files = files, voiceover = label,
                        updated = (updated_at ?: 0L).toInt(), uk = false, type = type,
                        audioIndex = audio.index ?: (i + 1),
                    )
                }
            }
        }
    }

    private suspend fun KinoPubItem.toAppSeasons(preferredStreamType: String): List<Season> {
        val sourceSeasons = when {
            !seasons.isNullOrEmpty() -> seasons.orEmpty()
            !videos.isNullOrEmpty() -> videos.orEmpty()
                .groupBy { it.snumber ?: 1 }
                .map { (seasonNumber, episodes) -> KinoPubSeason(number = seasonNumber, episodes = episodes) }
            else -> emptyList()
        }
        return sourceSeasons.mapNotNull { season ->
            val episodes = season.episodes.mapNotNull { media ->
                val translations = media.toTranslations(preferredStreamType, voice)
                if (translations.isEmpty()) null
                else AppEpisode(
                    episode = media.number ?: 1, ad_skip = 0,
                    title = media.title.orEmpty(), released = "",
                    translations = translations.toMutableList(), thumbnail = media.thumbnail,
                )
            }.toMutableList()
            if (episodes.isEmpty()) null else Season(season = season.number ?: 1, episodes = episodes)
        }.sortedBy(Season::season)
    }

    private suspend fun KinoPubMedia.toTranslations(
        preferredStreamType: String,
        voiceFallback: String?,
    ): List<Translation> {
        val files = toAppFiles(preferredStreamType)
        if (files.isEmpty()) return emptyList()
        val audioTracks = audios.orEmpty()
        if (audioTracks.isEmpty()) {
            return listOf(Translation(translation = voiceFallback ?: "Default", files = files, audioIndex = 1))
        }
        val seen = mutableSetOf<String>()
        return audioTracks.mapIndexedNotNull { i, audio ->
            val label = audio.author?.title ?: audio.type?.title ?: audio.lang ?: voiceFallback ?: "Default"
            if (seen.add(label)) Translation(translation = label, files = files, audioIndex = audio.index ?: (i + 1))
            else null
        }
    }

    private suspend fun KinoPubMedia.toAppFiles(preferredStreamType: String): List<File> =
        files.orEmpty().mapNotNull { mediaFile ->
            val url = mediaFile.resolveUrl(preferredStreamType) ?: return@mapNotNull null
            File(url = url, quality = mediaFile.qualityValue(), proPlus = false)
        }.sortedByDescending(File::quality)

    private suspend fun KinoPubMediaFile.resolveUrl(preferredStreamType: String): String? {
        val urls = urls ?: url
        urls?.urlFor(preferredStreamType)?.let { return it }
        urls?.hls4?.let { return it }
        urls?.hls2?.let { return it }
        urls?.hls?.let { return it }
        urls?.http?.let { return it }
        val relativeFile = file ?: return null
        return runCatching { kinoPubApiService.getMediaVideoLink(relativeFile, preferredStreamType).url }.getOrNull()
    }

    private fun KinoPubMedia.primaryAudioLabel(defaultLabel: String): String =
        audios.orEmpty().firstOrNull()?.let { it.author?.title ?: it.type?.title ?: it.lang } ?: defaultLabel

    private fun KinoPubWatchingEpisode.toMovieProgress(): ShowProgressItem? {
        val progressTime = time?.toLong()?.takeIf { it > 0 } ?: return null
        return ShowProgressItem(season = 0, episode = number ?: 1, voiceover = "", time = progressTime, quality = 1080, updatedAt = updated)
    }

    private fun KinoPubWatchingEpisode.toSeriesProgress(seasonNumber: Int?): ShowProgressItem? {
        val progressTime = time?.toLong()?.takeIf { it > 0 } ?: return null
        return ShowProgressItem(season = seasonNumber ?: 1, episode = number ?: 1, voiceover = "", time = progressTime, quality = 1080, updatedAt = updated)
    }

    private fun KinoPubItem.isSeries(): Boolean =
        type.contains("serial", ignoreCase = true)
            || type.contains("show", ignoreCase = true)
            || !seasons.isNullOrEmpty()

    private fun KinoPubItem.maxSeasonNumber(): Int? =
        seasons.orEmpty().maxOfOrNull { it.number ?: 0 }?.takeIf { it > 0 }
            ?: videos.orEmpty().maxOfOrNull { it.snumber ?: 0 }?.takeIf { it > 0 }

    private fun KinoPubItem.maxEpisodeNumber(): Int? =
        seasons.orEmpty().flatMap { it.episodes }.maxOfOrNull { it.number ?: 0 }?.takeIf { it > 0 }
            ?: videos.orEmpty().maxOfOrNull { it.number ?: 0 }?.takeIf { it > 0 }

    private fun KinoPubItem.bestPosterUrl(): String =
        posters?.big ?: posters?.medium ?: posters?.small ?: posters?.wide.orEmpty()

    private fun KinoPubItem.durationMinutes(): Int? =
        duration?.average?.roundToInt() ?: duration?.total

    private fun KinoPubItem.qualityLabel(): String = quality?.toString() ?: "N/A"

    private fun KinoPubItem.toShowStatus(): ShowStatus? {
        val statusText = when {
            finished == true -> "Finished"
            isSeries() -> "Ongoing"
            else -> null
        }
        return statusText?.let { ShowStatus(status = if (finished == true) 1 else 0, status_text = it) }
    }

    private fun KinoPubItem.positiveVotes(): Int {
        val totalVotes = rating_votes ?: return 0
        return (totalVotes * ((rating_percentage ?: 0).coerceIn(0, 100) / 100.0)).roundToInt()
    }

    private fun KinoPubItem.negativeVotes(): Int {
        val totalVotes = rating_votes ?: return 0
        return (totalVotes - positiveVotes()).coerceAtLeast(0)
    }

    private fun String.slug(): String =
        lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")

    private fun KinoPubSettingValue?.asFlag(): Int = when (val v = this?.value) {
        is Boolean -> if (v) 1 else 0
        is Number -> if (v.toInt() != 0) 1 else 0
        is String -> if (v == "true" || v == "1") 1 else 0
        else -> 0
    }

    private fun KinoPubVideoUrls.urlFor(code: String): String? = when (code.lowercase(Locale.ROOT)) {
        "http", "mp4" -> http
        "hls" -> hls
        "hls2" -> hls2
        "hls4" -> hls4
        else -> null
    }

    private fun KinoPubMediaFile.qualityValue(): Int {
        quality?.filter(Char::isDigit)?.toIntOrNull()?.let { return it }
        h?.let { return it }
        quality_id?.let { return it }
        return 0
    }

    private companion object {
        const val FAVORITES_FOLDER_TITLE = "Filmix Favorites"
    }
}
