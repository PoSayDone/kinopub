package io.github.posaydone.filmix.core.data

import io.github.posaydone.filmix.core.data.di.ApplicationScope
import io.github.posaydone.filmix.core.model.FilmixCategory
import io.github.posaydone.filmix.core.model.PageWithShows
import io.github.posaydone.filmix.core.model.ServerLocationResponse
import io.github.posaydone.filmix.core.model.Show
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

class KinopubRepository @Inject constructor(
    private val kinopubRemoteDataSource: KinopubRemoteDataSource,
    private val sessionManager: SessionManager,
    @ApplicationScope private val externalScope: CoroutineScope
) {
    suspend fun getPage(
        limit: Int = 48,
        page: Int? = null,
        category: String = "s0",
        genre: String? = null,
    ): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchPage(
            category = category, page = page, limit = limit, genre = genre
        )
    }

    fun getList(
        limit: Int = 48,
        page: Int? = null,
        category: String = "s0",
        genre: String? = null,
    ): Flow<ShowList> = flow {
        val list = getPage(
            category = category, page = page, limit = limit, genre = genre
        ).items
        emit(list)
    }

    suspend fun getViewingPage(limit: Int = 48, page: Int = 1): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchViewingPage(
            page = page, limit = limit
        )
    }

    fun getViewingList(limit: Int = 48, page: Int = 1): Flow<ShowList> = flow {
        val list = getViewingPage(
            page = page, limit = limit
        ).items
        emit(list)
    }

    suspend fun getPopularPage(
        limit: Int = 48,
        page: Int? = null,
        section: FilmixCategory = FilmixCategory.MOVIE,
    ): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchPopularPage(
            limit = limit, page = page, section = section
        )
    }

    fun getPopularList(
        limit: Int = 48,
        page: Int? = null,
        section: FilmixCategory = FilmixCategory.MOVIE,
    ): Flow<ShowList> = flow {
        val list = getPopularPage(
            limit = limit, page = page, section = section
        ).items
        emit(list)
    }

    suspend fun getFreshPage(
        limit: Int = 48,
        page: Int? = null,
        section: FilmixCategory = FilmixCategory.MOVIE,
    ): PageWithShows<Show> {
        return kinopubRemoteDataSource.fetchFreshPage(
            limit = limit, page = page, section = section
        )
    }

    fun getFreshList(
        limit: Int = 48,
        page: Int? = null,
        section: FilmixCategory = FilmixCategory.MOVIE,
    ): Flow<ShowList> = flow {
        val list = getFreshPage(
            limit = limit, page = page, section = section
        ).items
        emit(list)
    }

    suspend fun getHistoryPageFull(
        limit: Int = 10,
        page: Int? = null,
    ): PageWithShows<ShowDetails> {
        return kinopubRemoteDataSource.fetchHistoryPageFull(
            limit = limit,
            page = page,
        )
    }

    fun getHistoryListFull(
        limit: Int = 10,
        page: Int? = null,
    ): Flow<List<ShowDetails>> = flow {
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
