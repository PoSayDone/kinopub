package io.github.posaydone.kinopub.core.network.service

import io.github.posaydone.kinopub.core.model.introdb.IntroDbSegmentsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IntroDbApiService {
    @GET("segments")
    suspend fun getSegments(
        @Query("imdb_id") imdbId: String,
        @Query("season") season: Int,
        @Query("episode") episode: Int,
    ): IntroDbSegmentsResponse
}
