package io.github.posaydone.kinopub.core.model.kinopub

data class KinoPubStatusResponse(
    val status: Int,
)

data class KinoPubPagination(
    val total: Int? = null,
    val current: Int? = null,
    val perpage: Int? = null,
    val total_items: Int? = null,
    val total_count: Int? = null,
)

data class KinoPubGenre(
    val id: Int,
    val title: String,
    val type: String? = null,
)

data class KinoPubCountry(
    val id: Int,
    val title: String,
)

data class KinoPubGenresResponse(
    val status: Int,
    val items: List<KinoPubGenre> = emptyList(),
)

data class KinoPubCountriesResponse(
    val status: Int,
    val items: List<KinoPubCountry> = emptyList(),
)

data class KinoPubPosters(
    val small: String? = null,
    val medium: String? = null,
    val big: String? = null,
    val wide: String? = null,
)

data class KinoPubDuration(
    val average: Double? = null,
    val total: Int? = null,
)

data class KinoPubTrailerSummary(
    val id: Int? = null,
    val file: String? = null,
    val url: String? = null,
)

data class KinoPubBookmarkFolder(
    val id: Int,
    val user_id: Int? = null,
    val title: String,
    val views: Int? = null,
    val count: Int? = null,
    val created: Long? = null,
    val updated: Long? = null,
    val created_at: Long? = null,
    val updated_at: Long? = null,
)

data class KinoPubWatchingState(
    val status: Int? = null,
    val time: Int? = null,
)

data class KinoPubSeasonWatching(
    val status: Int? = null,
    val episodes: List<KinoPubEpisodeWatching>? = null,
)

data class KinoPubEpisodeWatching(
    val id: Int,
    val number: Int? = null,
    val status: Int? = null,
    val time: Int? = null,
)

data class KinoPubVoiceoverType(
    val id: Int,
    val title: String,
    val short_title: String? = null,
)

data class KinoPubVoiceoverAuthor(
    val id: Int,
    val title: String,
    val short_title: String? = null,
)

data class KinoPubAudioTrack(
    val id: Int,
    val index: Int? = null,
    val codec: String? = null,
    val channels: Int? = null,
    val lang: String? = null,
    val type: KinoPubVoiceoverType? = null,
    val author: KinoPubVoiceoverAuthor? = null,
)

data class KinoPubSubtitle(
    val lang: String,
    val shift: Int? = null,
    val embed: Boolean? = null,
    val forced: Boolean? = null,
    val file: String? = null,
    val url: String? = null,
)

data class KinoPubVideoUrls(
    val http: String? = null,
    val hls: String? = null,
    val hls2: String? = null,
    val hls4: String? = null,
)

data class KinoPubMediaFile(
    val codec: String? = null,
    val w: Int? = null,
    val h: Int? = null,
    val quality: String? = null,
    val quality_id: Int? = null,
    val file: String? = null,
    val url: KinoPubVideoUrls? = null,
    val urls: KinoPubVideoUrls? = null,
)

data class KinoPubMedia(
    val id: Int,
    val number: Int? = null,
    val snumber: Int? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
    val watched: Int? = null,
    val time: Int? = null,
    val status: Int? = null,
    val watching: KinoPubWatchingState? = null,
    val tracks: Int? = null,
    val ac3: Int? = null,
    val audios: List<KinoPubAudioTrack>? = null,
    val subtitles: List<KinoPubSubtitle>? = null,
    val files: List<KinoPubMediaFile>? = null,
)

data class KinoPubSeason(
    val id: Int? = null,
    val number: Int? = null,
    val title: String? = null,
    val watching: KinoPubSeasonWatching? = null,
    val episodes: List<KinoPubMedia> = emptyList(),
)

data class KinoPubItem(
    val id: Int,
    val title: String,
    val type: String,
    val subtype: String? = null,
    val year: Int? = null,
    val cast: String? = null,
    val director: String? = null,
    val voice: String? = null,
    val duration: KinoPubDuration? = null,
    val langs: Int? = null,
    val ac3: Int? = null,
    val subtitles: Int? = null,
    val quality: Int? = null,
    val genres: List<KinoPubGenre> = emptyList(),
    val countries: List<KinoPubCountry> = emptyList(),
    val plot: String? = null,
    val imdb: Int? = null,
    val imdb_rating: Double? = null,
    val imdb_votes: Int? = null,
    val kinopoisk: Int? = null,
    val kinopoisk_rating: Double? = null,
    val kinopoisk_votes: Int? = null,
    val rating: Int? = null,
    val rating_votes: Int? = null,
    val rating_percentage: Int? = null,
    val views: Int? = null,
    val comments: Int? = null,
    val finished: Boolean? = null,
    val advert: Boolean? = null,
    val poor_quality: Boolean? = null,
    val in_watchlist: Boolean? = null,
    val subscribed: Boolean? = null,
    val created_at: Long? = null,
    val updated_at: Long? = null,
    val posters: KinoPubPosters? = null,
    val trailer: KinoPubTrailerSummary? = null,
    val watched: Int? = null,
    val watching: KinoPubWatchingState? = null,
    val bookmarks: List<KinoPubBookmarkFolder>? = null,
    val seasons: List<KinoPubSeason>? = null,
    val videos: List<KinoPubMedia>? = null,
)

data class KinoPubItemsResponse(
    val status: Int,
    val items: List<KinoPubItem>,
    val pagination: KinoPubPagination? = null,
)

data class KinoPubItemDetailResponse(
    val status: Int,
    val item: KinoPubItem,
)

data class KinoPubTrailer(
    val id: Int? = null,
    val url: String? = null,
)

data class KinoPubTrailerResponse(
    val status: Int,
    val trailer: List<KinoPubTrailer>? = null,
)

data class KinoPubWatchingEpisode(
    val id: Int,
    val number: Int? = null,
    val title: String? = null,
    val duration: Int? = null,
    val status: Int? = null,
    val time: Int? = null,
    val updated: Long? = null,
)

data class KinoPubWatchingSeason(
    val number: Int? = null,
    val status: Int? = null,
    val episodes: List<KinoPubWatchingEpisode> = emptyList(),
)

data class KinoPubWatchingItem(
    val id: Int,
    val title: String,
    val type: String,
    val subtype: String? = null,
    val status: Int? = null,
    val posters: KinoPubPosters? = null,
    val videos: List<KinoPubWatchingEpisode>? = null,
    val seasons: List<KinoPubWatchingSeason>? = null,
)

data class KinoPubWatchingInfoResponse(
    val status: Int,
    val item: KinoPubWatchingItem? = null,
)

data class KinoPubWatchingListItem(
    val id: Int,
    val title: String,
    val type: String,
    val subtype: String? = null,
    val posters: KinoPubPosters? = null,
)

data class KinoPubWatchingMoviesResponse(
    val status: Int,
    val items: List<KinoPubWatchingListItem>,
)

data class KinoPubWatchingSerialItem(
    val id: Int,
    val title: String,
    val type: String,
    val subtype: String? = null,
    val posters: KinoPubPosters? = null,
    val new: Int? = null,
    val total: Int? = null,
    val watched: Int? = null,
)

data class KinoPubWatchingSerialsResponse(
    val status: Int,
    val items: List<KinoPubWatchingSerialItem>,
)

data class KinoPubHistoryEntry(
    val counter: Int? = null,
    val first_seen: Long? = null,
    val last_seen: Long? = null,
    val time: Int? = null,
    val deleted: Boolean? = null,
    val item: KinoPubItem? = null,
    val media: KinoPubMedia? = null,
)

data class KinoPubHistoryResponse(
    val status: Int,
    val history: List<KinoPubHistoryEntry>,
    val pagination: KinoPubPagination? = null,
)

data class KinoPubBookmarkFoldersResponse(
    val status: Int,
    val items: List<KinoPubBookmarkFolder>,
)

data class KinoPubBookmarkItemsResponse(
    val status: Int,
    val items: List<KinoPubItem>,
    val pagination: KinoPubPagination? = null,
)

data class KinoPubItemFoldersResponse(
    val status: Int,
    val folders: List<KinoPubBookmarkFolder>,
)

data class KinoPubCreateFolderResponse(
    val status: Int,
    val folder: KinoPubBookmarkFolder,
)

data class KinoPubBookmarkActionResponse(
    val status: Int,
    val exists: Boolean? = null,
)

data class KinoPubSubscription(
    val active: Boolean? = null,
    val end_time: Long? = null,
    val days: Double? = null,
)

data class KinoPubUserProfile(
    val name: String? = null,
    val avatar: String? = null,
)

data class KinoPubUserSettings(
    val show_erotic: Boolean? = null,
    val show_uncertain: Boolean? = null,
)

data class KinoPubUser(
    val username: String,
    val reg_date: Long? = null,
    val subscription: KinoPubSubscription? = null,
    val settings: KinoPubUserSettings? = null,
    val profile: KinoPubUserProfile? = null,
)

data class KinoPubUserResponse(
    val status: Int,
    val user: KinoPubUser,
)

data class KinoPubSettingValue(
    val label: String? = null,
    val value: Any? = null,
    val type: String? = null,
)

data class KinoPubDevice(
    val id: Int,
    val title: String? = null,
    val hardware: String? = null,
    val software: String? = null,
    val created: Long? = null,
    val updated: Long? = null,
    val last_seen: Long? = null,
    val is_browser: Boolean? = null,
    val settings: Map<String, KinoPubSettingValue>? = null,
)

data class KinoPubDeviceResponse(
    val status: Int,
    val device: KinoPubDevice,
)

data class KinoPubDeviceSettingsResponse(
    val status: Int,
    val settings: Map<String, KinoPubSettingValue>,
)

data class KinoPubStreamingType(
    val id: Int,
    val code: String,
    val version: Int? = null,
    val name: String,
    val description: String? = null,
)

data class KinoPubStreamingTypesResponse(
    val status: Int,
    val items: List<KinoPubStreamingType>,
)

data class KinoPubServerLocation(
    val id: Int,
    val location: String,
    val name: String,
)

data class KinoPubServerLocationsResponse(
    val status: Int,
    val items: List<KinoPubServerLocation>,
)

data class KinoPubMediaVideoLinkResponse(
    val url: String,
)
