package io.github.posaydone.filmix.core.data

import io.github.posaydone.filmix.core.data.di.ApplicationScope
import io.github.posaydone.filmix.core.model.PageWithShows
import io.github.posaydone.filmix.core.model.ServerLocationResponse
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.ShowImages
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.core.model.ShowProgress
import io.github.posaydone.filmix.core.model.ShowProgressItem
import io.github.posaydone.filmix.core.model.ShowResourceResponse
import io.github.posaydone.filmix.core.model.ShowTrailers
import io.github.posaydone.filmix.core.model.StreamTypeResponse
import io.github.posaydone.filmix.core.model.UserProfileInfo
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.network.dataSource.KinopubRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShowRepository @Inject constructor(
    private val kinopubRemoteDataSource: KinopubRemoteDataSource,
    private val sessionManager: SessionManager,
    @ApplicationScope private val externalScope: CoroutineScope
) {
    suspend fun getViewingPage(limit: Int = 48, page: Int = 1): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchViewingPage(
            page = page, limit = limit
        )
    }

    fun getCatalogList(
        contentType: String?,
        sort: String,
        period: String? = null,
        limit: Int = 20,
        page: Int? = null,
    ): Flow<ShowList> = flow {
        emit(kinopubRemoteDataSource.fetchCatalogPage(
            contentType = contentType, sort = sort, period = period,
            limit = limit, page = page,
        ).items)
    }

    suspend fun getCatalogPage(
        contentType: String?,
        sort: String,
        period: String? = null,
        genreIds: Set<Int> = emptySet(),
        countryIds: Set<Int> = emptySet(),
        limit: Int = 48,
        page: Int? = null,
    ): PageWithShows<Show> = kinopubRemoteDataSource.fetchCatalogPage(
        contentType = contentType, sort = sort, period = period,
        genreIds = genreIds, countryIds = countryIds,
        limit = limit, page = page,
    )

    suspend fun getGenres(genreType: String?): List<io.github.posaydone.filmix.core.model.kinopub.KinoPubGenre> =
        kinopubRemoteDataSource.fetchGenres(genreType)

    suspend fun getCountries(): List<io.github.posaydone.filmix.core.model.kinopub.KinoPubCountry> =
        kinopubRemoteDataSource.fetchCountries()

    suspend fun getWatchingMovies(): List<Show> = kinopubRemoteDataSource.fetchWatchingMovies()

    suspend fun getWatchingSerials(): List<Show> = kinopubRemoteDataSource.fetchWatchingSerials()

    fun getViewingList(limit: Int = 48, page: Int = 1): Flow<ShowList> = flow {
        val list = getViewingPage(
            page = page, limit = limit
        ).items
        emit(list)
    }

    suspend fun getHistoryPageFull(
        limit: Int = 10,
        page: Int? = null,
    ): PageWithShows<HistoryShow> {
        return kinopubRemoteDataSource.fetchHistoryPageFull(
            limit = limit,
            page = page,
        )
    }

    fun getHistoryListFull(
        limit: Int = 10,
        page: Int? = null,
    ): Flow<List<HistoryShow>> = flow {
        val list = getHistoryPageFull(
            limit = limit,
            page = page,
        ).items
        emit(list)
    }

    suspend fun getHistoryPage(limit: Int = 10, page: Int = 1): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchHistoryPage(
            limit = limit, page = page
        )
    }

    fun getHistoryList(limit: Int = 10, page: Int = 1): Flow<ShowList> = flow {
        val list = getHistoryPage(
            limit = limit, page = page
        ).items
        emit(list)
    }

    // Поиск фильмов по запросу
    suspend fun getShowsListWithQuery(query: String, limit: Int = 48): List<Show> {
        return kinopubRemoteDataSource.fetchShowsListWithQuery(query, limit)
    }

    // Получение деталей фильма, включая сезоны, серии и озвучки
    suspend fun getShowDetails(movieId: Int): ShowDetails {
        return kinopubRemoteDataSource.fetchShowDetails(movieId)
    }

    suspend fun getShowImages(movieId: Int): ShowImages {
        return kinopubRemoteDataSource.fetchShowImages(movieId)
    }

    suspend fun getShowTrailers(movieId: Int): ShowTrailers {
        return kinopubRemoteDataSource.fetchShowTrailers(movieId)
    }

    suspend fun getShowProgress(movieId: Int): ShowProgress {
        return kinopubRemoteDataSource.fetchShowProgress(movieId)
    }

    fun addShowProgress(movieId: Int, showProgressItem: ShowProgressItem) {
        externalScope.launch {
            try {
                kinopubRemoteDataSource.addShowProgress(movieId, showProgressItem)
            } catch (e: Exception) {
                // Log the exception or handle it as needed, but don't crash the app
                e.printStackTrace() // In production, you might want to use a proper logging framework
            }
        }
    }

    suspend fun getShowResource(movieId: Int): ShowResourceResponse {
        return kinopubRemoteDataSource.fetchShowResource(movieId)
    }

    suspend fun getFavoritesPage(
        limit: Int = 48,
        page: Int? = null,
    ): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchFavoritesPage(
            page = page, limit = limit
        )
    }

    suspend fun getFavoritesList(
        limit: Int = 48,
        page: Int? = null,
    ): Flow<List<Show>> = flow {
        val list = getFavoritesPage(
            page = page, limit = limit
        ).items
        emit(list)
    }

    suspend fun getUserProfile(): UserProfileInfo {
        val profile = kinopubRemoteDataSource.fetchUserProfile()
        sessionManager.saveUsername(profile.login)
        return profile
    }

    suspend fun getStreamType(): StreamTypeResponse {
        return kinopubRemoteDataSource.fetchStreamType()
    }

    suspend fun getServerLocation(): ServerLocationResponse {
        return kinopubRemoteDataSource.fetchServerLocation()
    }

    suspend fun updateStreamType(streamType: String): Boolean {
        return kinopubRemoteDataSource.updateStreamType(streamType)
    }

    suspend fun updateServerLocation(serverLocation: String): Boolean {
        return kinopubRemoteDataSource.updateServerLocation(serverLocation)
    }

    suspend fun notifyDevice() {
        kinopubRemoteDataSource.notifyDevice()
    }

    suspend fun logout() {
        kinopubRemoteDataSource.logout()
    }

    suspend fun toggleFavorite(showId: Int, isFavorite: Boolean): Boolean {
        return if (isFavorite) {
            kinopubRemoteDataSource.addToFavorites(showId)
        } else {
            kinopubRemoteDataSource.removeFromFavorites(showId)
        }
    }
}
