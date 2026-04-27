package io.github.posaydone.kinopub.core.network.service

import io.github.posaydone.kinopub.core.model.kinopub.KinoPubBookmarkActionResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubBookmarkFoldersResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubBookmarkItemsResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubCountriesResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubCreateFolderResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubDeviceResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubDeviceSettingsResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubGenresResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubHistoryResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubItemDetailResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubItemsResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubMediaVideoLinkResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubServerLocationsResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubStatusResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubStreamingTypesResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubTrailerResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubUserResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubWatchingInfoResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubWatchingMoviesResponse
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubWatchingSerialsResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KinoPubApiService {
    @GET("v1/genres")
    suspend fun getGenres(
        @Query("type") type: String? = null,
    ): KinoPubGenresResponse

    @GET("v1/countries")
    suspend fun getCountries(): KinoPubCountriesResponse

    @GET("v1/items")
    suspend fun listItems(
        @Query("type") type: String? = null,
        @Query("genre") genre: String? = null,
        @Query("country") country: String? = null,
        @Query("page") page: Int? = null,
        @Query("perpage") perPage: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("conditions[]") conditions: String? = null,
    ): KinoPubItemsResponse

    @GET("v1/items/fresh")
    suspend fun getFreshItems(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("perpage") perPage: Int? = null,
    ): KinoPubItemsResponse

    @GET("v1/items/popular")
    suspend fun getPopularItems(
        @Query("type") type: String? = null,
        @Query("genre") genre: String? = null,
        @Query("page") page: Int? = null,
        @Query("perpage") perPage: Int? = null,
    ): KinoPubItemsResponse

    @GET("v1/items/search")
    suspend fun searchItems(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("perpage") perPage: Int? = null,
    ): KinoPubItemsResponse

    @GET("v1/items/{id}")
    suspend fun getItemDetails(
        @Path("id") id: Int,
        @Query("nolinks") noLinks: Int? = null,
    ): KinoPubItemDetailResponse

    @GET("v1/items/trailer")
    suspend fun getTrailer(
        @Query("id") id: Int,
    ): KinoPubTrailerResponse

    @GET("v1/items/media-video-link")
    suspend fun getMediaVideoLink(
        @Query("file") file: String,
        @Query("type") type: String,
    ): KinoPubMediaVideoLinkResponse

    @GET("v1/watching/togglewatchlist")
    suspend fun toggleWatchlist(
        @Query("id") id: Int,
    ): KinoPubStatusResponse

    @GET("v1/watching")
    suspend fun getWatchingInfo(
        @Query("id") id: Int,
    ): KinoPubWatchingInfoResponse

    @GET("v1/watching/movies")
    suspend fun listWatchingMovies(
        @Query("subscribed") subscribed: Int? = null,
    ): KinoPubWatchingMoviesResponse

    @GET("v1/watching/serials")
    suspend fun listWatchingSerials(
        @Query("subscribed") subscribed: Int? = null,
    ): KinoPubWatchingSerialsResponse

    @GET("v1/watching/marktime")
    suspend fun markWatchingTime(
        @Query("id") id: Int,
        @Query("video") video: Int,
        @Query("time") time: Long,
        @Query("season") season: Int? = null,
    ): KinoPubStatusResponse

    @GET("v1/history")
    suspend fun getHistory(
        @Query("page") page: Int? = null,
        @Query("perpage") perPage: Int? = null,
    ): KinoPubHistoryResponse

    @GET("v1/bookmarks")
    suspend fun listBookmarkFolders(): KinoPubBookmarkFoldersResponse

    @GET("v1/bookmarks/{id}")
    suspend fun listBookmarkItems(
        @Path("id") id: Int,
        @Query("page") page: Int? = null,
        @Query("perpage") perPage: Int? = null,
    ): KinoPubBookmarkItemsResponse

    @POST("v1/bookmarks/create")
    @FormUrlEncoded
    suspend fun createBookmarkFolder(
        @Field("title") title: String,
    ): KinoPubCreateFolderResponse

    @POST("v1/bookmarks/add")
    @FormUrlEncoded
    suspend fun addBookmarkItem(
        @Field("folder") folderId: Int,
        @Field("item") itemId: Int,
    ): KinoPubBookmarkActionResponse

    @POST("v1/bookmarks/remove-item")
    @FormUrlEncoded
    suspend fun removeBookmarkItem(
        @Field("folder") folderId: Int,
        @Field("item") itemId: Int,
    ): KinoPubBookmarkActionResponse

    @GET("v1/user")
    suspend fun getUser(): KinoPubUserResponse

    @GET("v1/device/info")
    suspend fun getCurrentDevice(): KinoPubDeviceResponse

    @GET("v1/device/{id}/settings")
    suspend fun getDeviceSettings(
        @Path("id") id: Int,
    ): KinoPubDeviceSettingsResponse

    @POST("v1/device/{id}/settings")
    @FormUrlEncoded
    suspend fun updateDeviceSettings(
        @Path("id") id: Int,
        @Field("supportSsl") supportSsl: Int,
        @Field("supportHevc") supportHevc: Int,
        @Field("supportHdr") supportHdr: Int,
        @Field("support4k") support4k: Int,
        @Field("mixedPlaylist") mixedPlaylist: Int,
        @Field("streamingType") streamingType: Int,
        @Field("serverLocation") serverLocation: Int,
    ): KinoPubStatusResponse

    @GET("v1/references/streaming-type")
    suspend fun listStreamingTypes(): KinoPubStreamingTypesResponse

    @GET("v1/references/server-location")
    suspend fun listServerLocations(): KinoPubServerLocationsResponse

    @POST("v1/device/notify")
    @FormUrlEncoded
    suspend fun notifyDevice(
        @Field("title") title: String,
        @Field("hardware") hardware: String,
        @Field("software") software: String,
    ): Response<KinoPubStatusResponse>

    @POST("v1/device/unlink")
    suspend fun unlink(): Response<KinoPubStatusResponse>
}
