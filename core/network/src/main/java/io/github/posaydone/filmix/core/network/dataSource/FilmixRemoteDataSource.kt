package io.github.posaydone.filmix.core.network.dataSource

import android.os.Build
import io.github.posaydone.filmix.core.model.Country
import io.github.posaydone.filmix.core.model.FilmixCategory
import io.github.posaydone.filmix.core.model.File
import io.github.posaydone.filmix.core.model.Genre
import io.github.posaydone.filmix.core.model.LastEpisode
import io.github.posaydone.filmix.core.model.MaxEpisode
import io.github.posaydone.filmix.core.model.PageWithShows
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.core.model.Series
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.ShowImage
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowProgressItem
import io.github.posaydone.filmix.core.model.ShowResourceResponse
import io.github.posaydone.filmix.core.model.ShowStatus
import io.github.posaydone.filmix.core.model.ShowTrailers
import io.github.posaydone.filmix.core.model.ServerLocationResponse
import io.github.posaydone.filmix.core.model.StreamTypeResponse
import io.github.posaydone.filmix.core.model.Translation
import io.github.posaydone.filmix.core.model.UserProfileInfo
import io.github.posaydone.filmix.core.model.VideoWithQualities
import io.github.posaydone.filmix.core.model.Episode as AppEpisode
import io.github.posaydone.filmix.core.model.kinopub.KinoPubContentType
import io.github.posaydone.filmix.core.model.kinopub.KinoPubHistoryEntry
import io.github.posaydone.filmix.core.model.kinopub.KinoPubItem
import io.github.posaydone.filmix.core.model.kinopub.KinoPubBookmarkItemsResponse
import io.github.posaydone.filmix.core.model.kinopub.KinoPubMedia
import io.github.posaydone.filmix.core.model.kinopub.KinoPubMediaFile
import io.github.posaydone.filmix.core.model.kinopub.KinoPubPagination
import io.github.posaydone.filmix.core.model.kinopub.KinoPubServerLocation
import io.github.posaydone.filmix.core.model.kinopub.KinoPubSettingValue
import io.github.posaydone.filmix.core.model.kinopub.KinoPubStreamingType
import io.github.posaydone.filmix.core.model.kinopub.KinoPubWatchingListItem
import io.github.posaydone.filmix.core.model.kinopub.KinoPubWatchingEpisode
import io.github.posaydone.filmix.core.network.service.KinoPubApiService
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class FilmixRemoteDataSource @Inject constructor(
    private val kinoPubApiService: KinoPubApiService,
) {
    private var favoritesFolderId: Int? = null
    private var contentTypesCache: List<KinoPubContentType>? = null
    private var streamingTypesCache: List<KinoPubStreamingType>? = null
    private var serverLocationsCache: List<KinoPubServerLocation>? = null

    suspend fun fetchPage(
        limit: Int = 48,
        page: Int? = null,
        category: String = "s0",
        genre: String? = null,
    ): PageWithShows<Show> {
        val response = kinoPubApiService.listItems(
            type = resolveLegacyCategory(category),
            genre = genre,
            page = page,
            perPage = limit,
            sort = "updated-",
        )
        return response.toPage(page, limit) { it.toShow() }
    }

    suspend fun fetchViewingPage(limit: Int = 48, page: Int = 1): PageWithShows<Show> {
        val movieItems = kinoPubApiService.listWatchingMovies().items.map { it.toShow() }
        val serialItems = kinoPubApiService.listWatchingSerials().items.map { it.toShow() }
        val combined = (movieItems + serialItems).distinctBy(Show::id)
        return combined.toPage(page = page, limit = limit)
    }

    suspend fun fetchPopularPage(
        limit: Int = 48,
        page: Int? = null,
        section: FilmixCategory = FilmixCategory.MOVIE,
    ): PageWithShows<Show> {
        val response = kinoPubApiService.getPopularItems(
            type = resolveCategoryType(section),
            page = page,
            perPage = limit,
        )
        return response.toPage(page, limit) { it.toShow() }
    }

    suspend fun fetchFreshPage(
        limit: Int = 48,
        page: Int? = null,
        section: FilmixCategory = FilmixCategory.MOVIE,
    ): PageWithShows<Show> {
        val response = kinoPubApiService.getFreshItems(
            type = resolveCategoryType(section),
            page = page,
            perPage = limit,
        )
        return response.toPage(page, limit) { it.toShow() }
    }

    suspend fun fetchHistoryPageFull(
        limit: Int = 10,
        page: Int? = null,
    ): PageWithShows<ShowDetails> {
        return fetchUniqueHistoryPage(limit = limit, page = page) { it.toHistoryShowDetails() }
    }

    suspend fun fetchHistoryPage(limit: Int = 10, page: Int = 1): PageWithShows<Show> {
        return fetchUniqueHistoryPage(limit = limit, page = page) { it.item?.toShow() }
    }

    suspend fun fetchShowsListWithQuery(query: String, limit: Int = 48): List<Show> {
        return kinoPubApiService.searchItems(query = query, perPage = limit).items.map { it.toShow() }
    }

    suspend fun fetchShowDetails(movieId: Int): ShowDetails {
        val item = kinoPubApiService.getItemDetails(movieId).item
        val existingFavoritesFolderId = findFavoritesFolderId()
        return item.toShowDetails(existingFavoritesFolderId)
    }

    suspend fun fetchShowImages(movieId: Int): ShowImages {
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

    suspend fun fetchShowTrailers(movieId: Int): ShowTrailers {
        return kinoPubApiService.getTrailer(movieId).trailer.orEmpty().mapIndexedNotNull { index, trailer ->
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
    }

    suspend fun fetchShowProgress(movieId: Int): ShowProgress {
        val item = kinoPubApiService.getWatchingInfo(movieId).item
            ?: return emptyList()
        val movieProgress = item.videos.orEmpty()
            .sortedByDescending { it.updated ?: 0L }
            .mapNotNull { it.toMovieProgress() }
        if (movieProgress.isNotEmpty()) {
            return movieProgress
        }

        return item.seasons.orEmpty().flatMap { season ->
            season.episodes.mapNotNull { episode -> episode.toSeriesProgress(season.number) }
        }.sortedByDescending(ShowProgressItem::time)
    }

    suspend fun addShowProgress(movieId: Int, showProgressItem: ShowProgressItem) {
        val episodeNumber = showProgressItem.episode.coerceAtLeast(1)
        val seasonNumber = showProgressItem.season.takeIf { it > 0 }
        kinoPubApiService.markWatchingTime(
            id = movieId,
            video = episodeNumber,
            time = showProgressItem.time,
            season = seasonNumber,
        )
    }

    suspend fun fetchShowResource(movieId: Int): ShowResourceResponse {
        val item = kinoPubApiService.getItemDetails(movieId).item
        val preferredStreamType = preferredStreamTypeCode()

        return if (item.isSeries()) {
            val seasons = item.toAppSeasons(preferredStreamType)
            ShowResourceResponse.SeriesResourceResponse(Series(seasons))
        } else {
            val movies = item.toMovieResources(preferredStreamType)
            ShowResourceResponse.MovieResourceResponse(movies)
        }
    }

    suspend fun addToFavorites(showId: Int): Boolean {
        return runCatching {
            val folderId = ensureFavoritesFolderId()
            kinoPubApiService.addBookmarkItem(folderId, showId).status == 200
        }.getOrDefault(false)
    }

    suspend fun fetchFavoritesPage(
        limit: Int = 48,
        page: Int? = null,
    ): PageWithShows<Show> {
        val folderId = ensureFavoritesFolderId()
        val response = kinoPubApiService.listBookmarkItems(folderId, page = page, perPage = limit)
        return response.toPage(page, limit) { it.toShow() }
    }

    suspend fun fetchUserProfile(): UserProfileInfo {
        val response = kinoPubApiService.getUser().user
        return UserProfileInfo(
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
    }

    suspend fun fetchStreamType(): StreamTypeResponse {
        val streamingTypes = getStreamingTypes()
        val currentSettings = getCurrentDeviceSettings()
        val labels = streamingTypes.associate { it.code to it.name }
        val currentType = resolveStreamingType(
            settingValue = currentSettings["streamingType"],
            streamingTypes = streamingTypes,
        )?.code
            ?: defaultStreamTypeCode(streamingTypes)
        return StreamTypeResponse(
            streamType = currentType,
            allowedTypes = labels.keys.toList(),
            labels = labels,
        )
    }

    suspend fun fetchServerLocation(): ServerLocationResponse {
        val locations = getServerLocations()
        val currentSettings = getCurrentDeviceSettings()
        val labels = locations.associate { it.location to it.name }
        val currentLocation = resolveServerLocation(
            settingValue = currentSettings["serverLocation"],
            locations = locations,
        )?.location
            ?: locations.firstOrNull()?.location
            ?: ""
        return ServerLocationResponse(
            serverLocation = currentLocation,
            labels = labels,
        )
    }

    suspend fun updateStreamType(streamType: String): Boolean {
        val selectedType = getStreamingTypes().firstOrNull { it.code == streamType } ?: return false
        return updateCurrentDeviceSettings(streamingType = selectedType.id)
    }

    suspend fun updateServerLocation(serverLocation: String): Boolean {
        val selectedLocation = getServerLocations().firstOrNull { it.location == serverLocation }
            ?: return false
        return updateCurrentDeviceSettings(serverLocation = selectedLocation.id)
    }

    suspend fun removeFromFavorites(showId: Int): Boolean {
        return runCatching {
            val folderId = ensureFavoritesFolderId()
            kinoPubApiService.removeBookmarkItem(folderId, showId).status == 200
        }.getOrDefault(false)
    }

    private suspend fun <T> fetchUniqueHistoryPage(
        limit: Int,
        page: Int?,
        transform: (KinoPubHistoryEntry) -> T?,
    ): PageWithShows<T> {
        val safeLimit = limit.coerceAtLeast(1)
        val requestedPage = (page ?: 1).coerceAtLeast(1)
        val targetItemCount = requestedPage * safeLimit
        val rawPerPage = maxOf(50, safeLimit)

        val uniqueItems = mutableListOf<T>()
        val seenItemIds = LinkedHashSet<Int>()
        var rawPage = 1
        var hasMoreRawPages = true

        while (hasMoreRawPages && uniqueItems.size < targetItemCount) {
            val response = kinoPubApiService.getHistory(page = rawPage, perPage = rawPerPage)
            response.history.forEach { entry ->
                val itemId = entry.item?.id ?: return@forEach
                if (!seenItemIds.add(itemId)) {
                    return@forEach
                }
                transform(entry)?.let(uniqueItems::add)
            }

            if (response.history.isEmpty()) {
                hasMoreRawPages = false
                break
            }

            val currentRawPage = response.pagination?.current ?: rawPage
            val effectivePerPage = response.pagination?.perpage ?: rawPerPage
            hasMoreRawPages = response.pagination.hasNextPage(
                pageNumber = currentRawPage,
                limit = effectivePerPage,
                itemCount = response.history.size,
            )
            rawPage = currentRawPage + 1
        }

        val startIndex = ((requestedPage - 1) * safeLimit).coerceAtMost(uniqueItems.size)
        val endIndex = (startIndex + safeLimit).coerceAtMost(uniqueItems.size)
        return PageWithShows(
            has_next_page = uniqueItems.size > endIndex || hasMoreRawPages,
            items = uniqueItems.subList(startIndex, endIndex).toList(),
            page = requestedPage,
            status = "200",
        )
    }

    private suspend fun resolveLegacyCategory(category: String): String? {
        return when (category) {
            FilmixCategory.MOVIE.toString() -> resolveCategoryType(FilmixCategory.MOVIE)
            FilmixCategory.SERIES.toString() -> resolveCategoryType(FilmixCategory.SERIES)
            FilmixCategory.CARTOON.toString() -> resolveCategoryType(FilmixCategory.CARTOON)
            FilmixCategory.CARTOON_SERIES.toString() -> resolveCategoryType(FilmixCategory.CARTOON_SERIES)
            else -> null
        }
    }

    private suspend fun resolveCategoryType(section: FilmixCategory): String? {
        return when (section) {
            FilmixCategory.MOVIE -> resolveType(
                candidates = listOf("movie", "documovie", "concert"),
                fuzzyHints = listOf("movie", "film", "фильм"),
                fallback = "movie",
            )

            FilmixCategory.SERIES -> resolveType(
                candidates = listOf("serial", "docuserial"),
                fuzzyHints = listOf("serial", "series", "сериал"),
                fallback = "serial",
            )

            FilmixCategory.CARTOON,
            FilmixCategory.CARTOON_SERIES,
            -> resolveType(
                candidates = listOf("cartoon", "anime", "tvshow"),
                fuzzyHints = listOf("cartoon", "anime", "animation", "мульт", "аниме"),
                fallback = "movie",
            )

            FilmixCategory.CONCERT -> resolveType(
                candidates = listOf("concert"),
                fuzzyHints = listOf("concert", "концерт"),
                fallback = "concert",
            )

            FilmixCategory.FILM_3D -> resolveType(
                candidates = listOf("3d"),
                fuzzyHints = listOf("3d"),
                fallback = "3d",
            )

            FilmixCategory.DOCUMENTARY_MOVIE -> resolveType(
                candidates = listOf("documovie"),
                fuzzyHints = listOf("documovie", "documentary", "документальный фильм"),
                fallback = "documovie",
            )

            FilmixCategory.DOCUMENTARY_SERIES -> resolveType(
                candidates = listOf("docuserial"),
                fuzzyHints = listOf("docuserial", "documentary series", "документальный сериал"),
                fallback = "docuserial",
            )

            FilmixCategory.TV_SHOW -> resolveType(
                candidates = listOf("tvshow"),
                fuzzyHints = listOf("tvshow", "tv show", "шоу"),
                fallback = "tvshow",
            )
        }
    }

    private suspend fun resolveType(
        candidates: List<String>,
        fuzzyHints: List<String>,
        fallback: String,
    ): String {
        val types = getContentTypes()
        candidates.firstNotNullOfOrNull { candidate ->
            types.firstOrNull { it.id.equals(candidate, ignoreCase = true) }?.id
        }?.let { return it }

        types.firstOrNull { type ->
            val haystack = "${type.id} ${type.title}".lowercase(Locale.ROOT)
            fuzzyHints.any { haystack.contains(it.lowercase(Locale.ROOT)) }
        }?.id?.let { return it }

        return fallback
    }

    private suspend fun getContentTypes(): List<KinoPubContentType> {
        return contentTypesCache ?: kinoPubApiService.getContentTypes().items.also {
            contentTypesCache = it
        }
    }

    private suspend fun getStreamingTypes(): List<KinoPubStreamingType> {
        return streamingTypesCache ?: kinoPubApiService.listStreamingTypes().items.also {
            streamingTypesCache = it
        }
    }

    private suspend fun getServerLocations(): List<KinoPubServerLocation> {
        return serverLocationsCache ?: kinoPubApiService.listServerLocations().items.also {
            serverLocationsCache = it
        }
    }

    private suspend fun ensureFavoritesFolderId(): Int {
        findFavoritesFolderId()?.let { return it }
        val createdFolder = kinoPubApiService.createBookmarkFolder(FAVORITES_FOLDER_TITLE).folder
        favoritesFolderId = createdFolder.id
        return createdFolder.id
    }

    private suspend fun findFavoritesFolderId(): Int? {
        favoritesFolderId?.let { return it }
        val folderId = kinoPubApiService.listBookmarkFolders().items
            .firstOrNull { it.title.equals(FAVORITES_FOLDER_TITLE, ignoreCase = true) }
            ?.id
        favoritesFolderId = folderId
        return folderId
    }

    private suspend fun getCurrentDeviceSettings(): Map<String, KinoPubSettingValue> {
        val deviceId = getCurrentDeviceId()
        return kinoPubApiService.getDeviceSettings(deviceId).settings
    }

    private suspend fun getCurrentDeviceId(): Int {
        return runCatching {
            kinoPubApiService.getCurrentDevice().device.id
        }.getOrElse {
            kinoPubApiService.notifyDevice(
                title = "Filmix",
                hardware = Build.MODEL ?: "Android",
                software = "Android ${Build.VERSION.RELEASE ?: "unknown"}",
            )
            kinoPubApiService.getCurrentDevice().device.id
        }
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
            ?: resolveStreamingType(
                settingValue = settings["streamingType"],
                streamingTypes = streamingTypes,
            )?.id
            ?: streamingTypes.firstOrNull()?.id
            ?: return false
        val effectiveServerLocation = serverLocation
            ?: resolveServerLocation(
                settingValue = settings["serverLocation"],
                locations = serverLocations,
            )?.id
            ?: serverLocations.firstOrNull()?.id
            ?: return false
        val response = kinoPubApiService.updateDeviceSettings(
            id = deviceId,
            supportSsl = settings["supportSsl"].asFlag(),
            supportHevc = settings["supportHevc"].asFlag(),
            supportHdr = settings["supportHdr"].asFlag(),
            support4k = settings["support4k"].asFlag(),
            mixedPlaylist = settings["mixedPlaylist"].asFlag(),
            streamingType = effectiveStreamingType,
            serverLocation = effectiveServerLocation,
        )
        return response.status == 200
    }

    private suspend fun preferredStreamTypeCode(): String {
        val streamingTypes = getStreamingTypes()
        return runCatching {
            resolveStreamingType(
                settingValue = getCurrentDeviceSettings()["streamingType"],
                streamingTypes = streamingTypes,
            )?.code
        }.getOrNull() ?: defaultStreamTypeCode(streamingTypes)
    }

    private fun resolveStreamingType(
        settingValue: KinoPubSettingValue?,
        streamingTypes: List<KinoPubStreamingType>,
    ): KinoPubStreamingType? {
        return matchStreamingType(settingValue?.value, streamingTypes)
            ?: settingValue?.label?.let { label ->
                streamingTypes.firstOrNull {
                    it.code.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true)
                }
            }
    }

    private fun matchStreamingType(
        rawValue: Any?,
        streamingTypes: List<KinoPubStreamingType>,
    ): KinoPubStreamingType? {
        return when (rawValue) {
            is Number -> streamingTypes.firstOrNull { it.id == rawValue.toInt() }
            is String -> streamingTypes.firstOrNull {
                it.code.equals(rawValue, ignoreCase = true)
                    || it.name.equals(rawValue, ignoreCase = true)
                    || it.id.toString() == rawValue
            }
            is Map<*, *> -> {
                val directMatch = listOf(
                    rawValue["id"],
                    rawValue["value"],
                    rawValue["code"],
                    rawValue["label"],
                    rawValue["name"],
                ).firstNotNullOfOrNull { candidate ->
                    matchStreamingType(candidate, streamingTypes)
                }
                directMatch ?: rawValue["selected"]
                    ?.takeIf(::isSelectedOption)
                    ?.let { matchStreamingType(rawValue["id"] ?: rawValue["value"], streamingTypes) }
            }
            is List<*> -> {
                rawValue.firstNotNullOfOrNull { option ->
                    val optionMap = option as? Map<*, *> ?: return@firstNotNullOfOrNull null
                    if (isSelectedOption(optionMap["selected"])) {
                        matchStreamingType(
                            optionMap["id"] ?: optionMap["value"] ?: optionMap["code"] ?: optionMap["label"],
                            streamingTypes,
                        )
                    } else {
                        null
                    }
                }
            }
            else -> null
        }
    }

    private fun resolveServerLocation(
        settingValue: KinoPubSettingValue?,
        locations: List<KinoPubServerLocation>,
    ): KinoPubServerLocation? {
        return matchServerLocation(settingValue?.value, locations)
            ?: settingValue?.label?.let { label ->
                locations.firstOrNull {
                    it.location.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true)
                }
            }
    }

    private fun matchServerLocation(
        rawValue: Any?,
        locations: List<KinoPubServerLocation>,
    ): KinoPubServerLocation? {
        return when (rawValue) {
            is Number -> locations.firstOrNull { it.id == rawValue.toInt() }
            is String -> locations.firstOrNull {
                it.location.equals(rawValue, ignoreCase = true)
                    || it.name.equals(rawValue, ignoreCase = true)
                    || it.id.toString() == rawValue
            }
            is Map<*, *> -> {
                val directMatch = listOf(
                    rawValue["id"],
                    rawValue["value"],
                    rawValue["location"],
                    rawValue["label"],
                    rawValue["name"],
                ).firstNotNullOfOrNull { candidate ->
                    matchServerLocation(candidate, locations)
                }
                directMatch ?: rawValue["selected"]
                    ?.takeIf(::isSelectedOption)
                    ?.let { matchServerLocation(rawValue["id"] ?: rawValue["value"], locations) }
            }
            is List<*> -> {
                rawValue.firstNotNullOfOrNull { option ->
                    val optionMap = option as? Map<*, *> ?: return@firstNotNullOfOrNull null
                    if (isSelectedOption(optionMap["selected"])) {
                        matchServerLocation(
                            optionMap["id"] ?: optionMap["value"] ?: optionMap["location"] ?: optionMap["label"],
                            locations,
                        )
                    } else {
                        null
                    }
                }
            }
            else -> null
        }
    }

    private fun defaultStreamTypeCode(streamingTypes: List<KinoPubStreamingType>): String {
        return streamingTypes.firstOrNull { it.code.equals("http", ignoreCase = true) }?.code
            ?: streamingTypes.firstOrNull()?.code
            ?: "hls4"
    }

    private fun isSelectedOption(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> value == "1" || value.equals("true", ignoreCase = true)
            else -> false
        }
    }

    private fun KinoPubItem.toShow(): Show {
        return Show(
            id = id,
            last_episode = maxEpisodeNumber(),
            last_season = maxSeasonNumber(),
            original_name = title,
            poster = bestPosterUrl(),
            quality = qualityLabel(),
            status = toShowStatus(),
            title = title,
            votesNeg = negativeVotes(),
            votesPos = positiveVotes(),
            year = year ?: 0,
            url = "",
        )
    }

    private fun KinoPubItem.toShowDetails(existingFavoritesFolderId: Int?): ShowDetails {
        val isSeries = isSeries()
        val maxSeasonNumber = maxSeasonNumber()
        val maxEpisodeNumber = maxEpisodeNumber()
        return ShowDetails(
            id = id,
            category = subtype ?: type,
            title = title,
            originalTitle = title,
            year = year ?: 0,
            updated = updated_at?.toString(),
            actors = null,
            directors = null,
            lastEpisode = if (isSeries && maxSeasonNumber != null && maxEpisodeNumber != null) {
                LastEpisode(
                    season = maxSeasonNumber,
                    episode = maxEpisodeNumber.toString(),
                )
            } else {
                null
            },
            maxEpisode = if (isSeries && maxSeasonNumber != null && maxEpisodeNumber != null) {
                MaxEpisode(
                    season = maxSeasonNumber,
                    episode = maxEpisodeNumber,
                )
            } else {
                null
            },
            countries = ArrayList(countries.map { Country(it.id, it.title) }),
            genres = ArrayList(genres.map { Genre(it.title.slug(), it.id, it.title) }),
            poster = bestPosterUrl(),
            rip = null,
            quality = qualityLabel(),
            votesPos = positiveVotes(),
            votesNeg = negativeVotes(),
            ratingImdb = imdb_rating ?: 0.0,
            ratingKinopoisk = kinopoisk_rating ?: 0.0,
            url = "",
            duration = durationMinutes(),
            votesIMDB = imdb_votes,
            votesKinopoisk = kinopoisk_votes,
            idKinopoisk = kinopoisk,
            mpaa = null,
            slogan = null,
            shortStory = plot.orEmpty(),
            status = toShowStatus(),
            isFavorite = bookmarks?.any { it.id == existingFavoritesFolderId } == true,
            isDeferred = in_watchlist,
            isHdr = false,
        )
    }

    private fun KinoPubHistoryEntry.toHistoryShowDetails(): ShowDetails? {
        val historyItem = item ?: return null
        val details = historyItem.toShowDetails(existingFavoritesFolderId = null)
        return details.copy(
            duration = media?.duration ?: details.duration,
        )
    }

    private fun KinoPubWatchingListItem.toShow(): Show {
        return Show(
            id = id,
            last_episode = null,
            last_season = null,
            original_name = title,
            poster = posters?.big ?: posters?.medium ?: posters?.small ?: posters?.wide.orEmpty(),
            quality = "N/A",
            status = null,
            title = title,
            votesNeg = 0,
            votesPos = 0,
            year = 0,
            url = "",
        )
    }

    private fun io.github.posaydone.filmix.core.model.kinopub.KinoPubWatchingSerialItem.toShow(): Show {
        return Show(
            id = id,
            last_episode = new,
            last_season = null,
            original_name = title,
            poster = posters?.big ?: posters?.medium ?: posters?.small ?: posters?.wide.orEmpty(),
            quality = "N/A",
            status = null,
            title = title,
            votesNeg = 0,
            votesPos = 0,
            year = 0,
            url = "",
        )
    }

    private suspend fun KinoPubItem.toMovieResources(preferredStreamType: String): List<VideoWithQualities> {
        val movieMedia = videos.orEmpty().ifEmpty {
            seasons.orEmpty().flatMap { it.episodes }
        }
        return movieMedia.flatMapIndexed { index, media ->
            val files = media.toAppFiles(preferredStreamType)
            if (files.isEmpty()) return@flatMapIndexed emptyList()

            val audioTracks = media.audios.orEmpty()
            if (audioTracks.isEmpty()) {
                listOf(
                    VideoWithQualities(
                        season = (media.snumber ?: 0).toString(),
                        episode = (media.number ?: index + 1).toString(),
                        adSkip = 0,
                        title = media.title.orEmpty(),
                        released = "",
                        files = files,
                        voiceover = media.primaryAudioLabel(defaultLabel = voice ?: "Default"),
                        updated = (updated_at ?: 0L).toInt(),
                        uk = false,
                        type = type,
                        audioIndex = 1,
                    )
                )
            } else {
                val seen = mutableSetOf<String>()
                audioTracks.mapIndexedNotNull { i, audio ->
                    val label = audio.author?.title ?: audio.type?.title ?: audio.lang
                        ?: voice ?: "Default"
                    if (!seen.add(label)) return@mapIndexedNotNull null
                    VideoWithQualities(
                        season = (media.snumber ?: 0).toString(),
                        episode = (media.number ?: index + 1).toString(),
                        adSkip = 0,
                        title = media.title.orEmpty(),
                        released = "",
                        files = files,
                        voiceover = label,
                        updated = (updated_at ?: 0L).toInt(),
                        uk = false,
                        type = type,
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
                .map { (seasonNumber, episodes) ->
                    io.github.posaydone.filmix.core.model.kinopub.KinoPubSeason(
                        number = seasonNumber,
                        episodes = episodes,
                    )
                }

            else -> emptyList()
        }

        return sourceSeasons.mapNotNull { season ->
            val episodes = season.episodes.mapNotNull { media ->
                val translations = media.toTranslations(preferredStreamType, voice)
                if (translations.isEmpty()) {
                    null
                } else {
                    AppEpisode(
                        episode = media.number ?: 1,
                        ad_skip = 0,
                        title = media.title.orEmpty(),
                        released = "",
                        translations = translations.toMutableList(),
                    )
                }
            }.toMutableList()

            if (episodes.isEmpty()) {
                null
            } else {
                Season(
                    season = season.number ?: 1,
                    episodes = episodes,
                )
            }
        }.sortedBy(Season::season)
    }

    private suspend fun KinoPubMedia.toTranslations(
        preferredStreamType: String,
        voiceFallback: String?,
    ): List<Translation> {
        val files = toAppFiles(preferredStreamType)
        if (files.isEmpty()) {
            return emptyList()
        }

        val audioTracks = audios.orEmpty()
        if (audioTracks.isEmpty()) {
            return listOf(
                Translation(
                    translation = voiceFallback ?: "Default",
                    files = files,
                    audioIndex = 1,
                )
            )
        }

        val seen = mutableSetOf<String>()
        return audioTracks.mapIndexedNotNull { i, audio ->
            val label = audio.author?.title ?: audio.type?.title ?: audio.lang ?: voiceFallback ?: "Default"
            if (seen.add(label)) {
                Translation(
                    translation = label,
                    files = files,
                    audioIndex = audio.index ?: (i + 1),
                )
            } else null
        }
    }

    private suspend fun KinoPubMedia.toAppFiles(preferredStreamType: String): List<File> {
        return files.orEmpty().mapNotNull { mediaFile ->
            val url = mediaFile.resolveUrl(preferredStreamType) ?: return@mapNotNull null
            File(
                url = url,
                quality = mediaFile.qualityValue(),
                proPlus = false,
            )
        }.sortedByDescending(File::quality)
    }

    private suspend fun KinoPubMediaFile.resolveUrl(preferredStreamType: String): String? {
        val urls = urls ?: url
        urls?.urlFor(preferredStreamType)?.let { return it }
        urls?.hls4?.let { return it }
        urls?.hls2?.let { return it }
        urls?.hls?.let { return it }
        urls?.http?.let { return it }
        val relativeFile = file ?: return null
        return runCatching {
            kinoPubApiService.getMediaVideoLink(relativeFile, preferredStreamType).url
        }.getOrNull()
    }

    private fun KinoPubMedia.primaryAudioLabel(defaultLabel: String): String {
        return audios.orEmpty().firstOrNull()?.let {
            it.author?.title ?: it.type?.title ?: it.lang
        } ?: defaultLabel
    }

    private fun KinoPubWatchingEpisode.toMovieProgress(): ShowProgressItem? {
        val progressTime = time?.toLong() ?: return null
        if (progressTime <= 0) {
            return null
        }
        return ShowProgressItem(
            season = 0,
            episode = number ?: 1,
            voiceover = "",
            time = progressTime,
            quality = 1080,
        )
    }

    private fun KinoPubWatchingEpisode.toSeriesProgress(seasonNumber: Int?): ShowProgressItem? {
        val progressTime = time?.toLong() ?: return null
        if (progressTime <= 0) {
            return null
        }
        return ShowProgressItem(
            season = seasonNumber ?: 1,
            episode = number ?: 1,
            voiceover = "",
            time = progressTime,
            quality = 1080,
        )
    }

    private fun KinoPubItem.isSeries(): Boolean {
        return type.contains("serial", ignoreCase = true)
            || type.contains("show", ignoreCase = true)
            || !seasons.isNullOrEmpty()
    }

    private fun KinoPubItem.maxSeasonNumber(): Int? {
        return seasons.orEmpty().maxOfOrNull { it.number ?: 0 }?.takeIf { it > 0 }
            ?: videos.orEmpty().maxOfOrNull { it.snumber ?: 0 }?.takeIf { it > 0 }
    }

    private fun KinoPubItem.maxEpisodeNumber(): Int? {
        return seasons.orEmpty().flatMap { it.episodes }.maxOfOrNull { it.number ?: 0 }?.takeIf { it > 0 }
            ?: videos.orEmpty().maxOfOrNull { it.number ?: 0 }?.takeIf { it > 0 }
    }

    private fun KinoPubItem.bestPosterUrl(): String {
        return posters?.big ?: posters?.medium ?: posters?.small ?: posters?.wide.orEmpty()
    }

    private fun KinoPubItem.durationMinutes(): Int? {
        return duration?.average?.roundToInt() ?: duration?.total
    }

    private fun KinoPubItem.qualityLabel(): String {
        return quality?.toString() ?: "N/A"
    }

    private fun KinoPubItem.toShowStatus(): ShowStatus? {
        val statusText = when {
            finished == true -> "Finished"
            isSeries() -> "Ongoing"
            else -> null
        }
        return statusText?.let {
            ShowStatus(status = if (finished == true) 1 else 0, status_text = it)
        }
    }

    private fun KinoPubItem.positiveVotes(): Int {
        val totalVotes = rating_votes ?: return 0
        val ratingPercent = (rating_percentage ?: 0).coerceIn(0, 100)
        return (totalVotes * (ratingPercent / 100.0)).roundToInt()
    }

    private fun KinoPubItem.negativeVotes(): Int {
        val totalVotes = rating_votes ?: return 0
        return (totalVotes - positiveVotes()).coerceAtLeast(0)
    }

    private fun String.slug(): String {
        return lowercase(Locale.ROOT)
            .replace(" ", "_")
            .replace("-", "_")
    }

    private fun KinoPubSettingValue?.asInt(): Int {
        val rawValue = this?.value ?: return 0
        return when (rawValue) {
            is Number -> rawValue.toInt()
            is Boolean -> if (rawValue) 1 else 0
            is String -> rawValue.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun KinoPubSettingValue?.asFlag(): Int {
        return when (val rawValue = this?.value) {
            is Boolean -> if (rawValue) 1 else 0
            is Number -> if (rawValue.toInt() != 0) 1 else 0
            is String -> if (rawValue == "true" || rawValue == "1") 1 else 0
            else -> 0
        }
    }

    private fun io.github.posaydone.filmix.core.model.kinopub.KinoPubVideoUrls.urlFor(code: String): String? {
        return when (code.lowercase(Locale.ROOT)) {
            "http", "mp4" -> http
            "hls" -> hls
            "hls2" -> hls2
            "hls4" -> hls4
            else -> null
        }
    }

    private fun KinoPubMediaFile.qualityValue(): Int {
        quality?.filter(Char::isDigit)?.toIntOrNull()?.let { return it }
        h?.let { return it }
        quality_id?.let { return it }
        return 0
    }

    private fun <T> io.github.posaydone.filmix.core.model.kinopub.KinoPubItemsResponse.toPage(
        requestedPage: Int?,
        limit: Int,
        transform: (KinoPubItem) -> T,
    ): PageWithShows<T> {
        val mappedItems = items.map(transform)
        return pagination.toPage(mappedItems, requestedPage, limit)
    }

    private fun <T> KinoPubBookmarkItemsResponse.toPage(
        requestedPage: Int?,
        limit: Int,
        transform: (KinoPubItem) -> T,
    ): PageWithShows<T> {
        val mappedItems = items.map(transform)
        return pagination.toPage(mappedItems, requestedPage, limit)
    }

    private fun <T> KinoPubPagination?.toPage(
        items: List<T>,
        requestedPage: Int?,
        limit: Int,
    ): PageWithShows<T> {
        val pageNumber = this?.current ?: requestedPage ?: 1
        return PageWithShows(
            has_next_page = hasNextPage(pageNumber = pageNumber, limit = limit, itemCount = items.size),
            items = items,
            page = pageNumber,
            status = "200",
        )
    }

    private fun KinoPubPagination?.hasNextPage(
        pageNumber: Int,
        limit: Int,
        itemCount: Int,
    ): Boolean {
        val totalItems = this?.total_items ?: this?.total_count ?: this?.total
        val totalPages = this?.total
        val perPage = this?.perpage
        return when {
            totalItems != null -> (pageNumber * limit) < totalItems
            totalPages != null && perPage != null -> pageNumber < totalPages
            else -> itemCount >= limit
        }
    }

    private fun <T> List<T>.toPage(page: Int, limit: Int): PageWithShows<T> {
        val safePage = page.coerceAtLeast(1)
        val start = (safePage - 1) * limit
        val end = (start + limit).coerceAtMost(size)
        val slice = if (start in 0 until size) subList(start, end) else emptyList()
        return PageWithShows(
            has_next_page = end < size,
            items = slice,
            page = safePage,
            status = "200",
        )
    }

    private companion object {
        const val FAVORITES_FOLDER_TITLE = "Filmix Favorites"
    }
}
