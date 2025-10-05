package io.github.posaydone.filmix.core.data

import android.util.Log
import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.KinopoiskMovie
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.tmdb.TmdbImage
import io.github.posaydone.filmix.core.model.tmdb.TmdbImagesResponse
import io.github.posaydone.filmix.core.network.Constants
import io.github.posaydone.filmix.core.network.dataSource.FilmixRemoteDataSource
import io.github.posaydone.filmix.core.network.dataSource.KinopoiskRemoteDataSource
import io.github.posaydone.filmix.core.network.dataSource.TmdbRemoteDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val filmixRepository: FilmixRepository,
    private val kinopoiskRemoteDataSource: KinopoiskRemoteDataSource,
    private val tmdbRemoteDataSource: TmdbRemoteDataSource,
) {
    private val TAG = "movierepo"
    private val cacheMutex = Mutex()

    suspend fun getFullMovieByFilmixId(filmixId: Int): FullShow = cacheMutex.withLock {
        // Check cache first
        val filmixShow = filmixRepository.getShowDetails(filmixId)
        Log.d(TAG, "$filmixShow")

        var kinopoiskMovie: KinopoiskMovie? = null
        if (filmixShow.idKinopoisk != null) {
            try {
                kinopoiskMovie = kinopoiskRemoteDataSource.getById(filmixShow.idKinopoisk!!)
            } catch (e: Exception) {
                Log.e("MovieRepository", "Error fetching Kinopoisk data: ${e.message}")
            }
        }

        Log.d(TAG, "$kinopoiskMovie")

        var tmdbImages: TmdbImagesResponse? = null
        var tmdbId: Int? = null
        if (kinopoiskMovie?.externalId?.tmdb != null) {
            // TMDB ID is available, use it directly
            tmdbId = kinopoiskMovie.externalId!!.tmdb!!
        } else if (kinopoiskMovie?.externalId?.imdb != null) {
            // TMDB ID is not available, try to find it using IMDb ID
            try {
                val findResponse = tmdbRemoteDataSource.findByExternalId(
                    externalId = kinopoiskMovie.externalId!!.imdb!!, externalSource = "imdb_id"
                )

                // Try to find a movie first, then TV series
                if (findResponse.movieResults.isNotEmpty()) {
                    tmdbId = findResponse.movieResults[0].id
                } else if (findResponse.tvResults.isNotEmpty()) {
                    tmdbId = findResponse.tvResults[0].id
                }
            } catch (e: Exception) {
                Log.e("MovieRepository", "Error finding TMDB ID by IMDb ID: ${e.message}")
            }
        }

        // If still no TMDB ID, try searching by name and year
        if (tmdbId == null && kinopoiskMovie != null) {
            try {
                val query = kinopoiskMovie.alternativeName ?: kinopoiskMovie.name
                ?: filmixShow.originalTitle ?: filmixShow.title
                val year = kinopoiskMovie.year
                val searchResult = if (kinopoiskMovie.isSeries) {
                    tmdbRemoteDataSource.searchTv(
                        query = query, year = year
                    )
                } else {
                    tmdbRemoteDataSource.searchMovies(
                        query = query, year = year
                    )
                }

                // Use the first result if available
                if (searchResult.results.isNotEmpty()) {
                    tmdbId = searchResult.results[0].id
                }
            } catch (e: Exception) {
                Log.e("MovieRepository", "Error searching TMDB by name and year: ${e.message}")
            }
        }

        if (tmdbId != null) {
            try {
                if (kinopoiskMovie?.isSeries == true) {
                    tmdbImages = tmdbRemoteDataSource.getTvImages(tmdbId)
                } else {
                    tmdbImages = tmdbRemoteDataSource.getMovieImages(tmdbId)
                }
            } catch (e: Exception) {
                Log.e("MovieRepository", "Error fetching TMDB images: ${e.message}")
            }
        }

        var fullShow = FullShow.fromFilmixShow(
            Show(
                id = filmixShow.id,
                last_episode = filmixShow.lastEpisode?.episode?.toIntOrNull(),
                last_season = filmixShow.lastEpisode?.season,
                original_name = filmixShow.originalTitle,
                poster = filmixShow.poster,
                quality = filmixShow.quality ?: "N/A",
                status = filmixShow.status,
                title = filmixShow.title,
                votesNeg = filmixShow.votesNeg,
                votesPos = filmixShow.votesPos,
                year = filmixShow.year,
                url = filmixShow.url
            ), filmixShow
        )

        // Enhance with Kinopoisk and TMDB data
        fullShow = fullShow.copy(
            title = kinopoiskMovie?.name ?: filmixShow.title,
            originalTitle = kinopoiskMovie?.alternativeName ?: filmixShow.originalTitle,
            year = kinopoiskMovie?.year ?: filmixShow.year,
            posterUrl = getBestTmdbImage(
                tmdbImages?.posters, "w500"
            )?.let { "${Constants.TMDB_IMAGE_URL}w500$it" } ?: kinopoiskMovie?.poster?.url
            ?: filmixShow.poster,
            backdropUrl = getBestTmdbBackdrop(tmdbImages?.backdrops)?.let { "${Constants.TMDB_IMAGE_URL}w1280$it" }
                ?: kinopoiskMovie?.backdrop?.url ?: filmixShow.poster,
            logoUrl = kinopoiskMovie?.logo?.url
                ?: getBestTmdbLogo(tmdbImages?.logos)?.let { "${Constants.TMDB_IMAGE_URL}original$it" },
            description = kinopoiskMovie?.description,
            shortDescription = kinopoiskMovie?.shortDescription,
            ratingKp = kinopoiskMovie?.rating?.kp,
            ratingImdb = kinopoiskMovie?.rating?.imdb,
            votesKp = kinopoiskMovie?.votes?.kp,
            votesImdb = kinopoiskMovie?.votes?.imdb,
            isSeries = kinopoiskMovie?.isSeries ?: (filmixShow.lastEpisode != null),
            isShow = kinopoiskMovie?.isSeries ?: (filmixShow.lastEpisode != null),
            genres = kinopoiskMovie?.genres?.map { it.name } ?: emptyList(),
            countries = kinopoiskMovie?.countries?.map { it.name } ?: emptyList(),
            ageRating = kinopoiskMovie?.ageRating ?: 0,
            movieLength = kinopoiskMovie?.movieLength,
            seriesLength = kinopoiskMovie?.seriesLength,
            quality = kinopoiskMovie?.ratingMpaa ?: filmixShow.quality ?: "N/A",
            status = kinopoiskMovie?.status ?: filmixShow.status?.status_text,
            tmdbPosterPaths = tmdbImages?.posters?.mapNotNull { it.filePath } ?: emptyList(),
            tmdbBackdropPaths = tmdbImages?.backdrops?.mapNotNull { it.filePath } ?: emptyList(),
            tmdbLogoPaths = tmdbImages?.logos?.mapNotNull { it.filePath } ?: emptyList())

        // Cache the result before returning
        return fullShow
    }

    /**
     * Gets the best TMDB backdrop prioritizing ones with null language
     */
    private fun getBestTmdbBackdrop(backdrops: List<TmdbImage>?): String? {
        if (backdrops.isNullOrEmpty()) return null

        // First, try to find a backdrop with null language
        val nullLanguageBackdrop =
            backdrops.find { it.iso6391 == null || it.iso6391 == "null" || it.iso6391 == "xx" }
        if (nullLanguageBackdrop?.filePath != null) return nullLanguageBackdrop.filePath

        // If no null language backdrop found, return the first one
        return backdrops.firstOrNull()?.filePath
    }

    /**
     * Gets the best TMDB logo prioritizing ru language, then en language
     */
    private fun getBestTmdbLogo(logos: List<TmdbImage>?): String? {
        if (logos.isNullOrEmpty()) return null

        // First, try to find a logo with ru language
        val ruLogo = logos.find { it.iso6391 == "ru" }
        if (ruLogo?.filePath != null) return ruLogo.filePath

        // Then try to find a logo with en language
        val enLogo = logos.find { it.iso6391 == "en" }
        if (enLogo?.filePath != null) return enLogo.filePath

        // If no ru or en logo found, return the first one
        return logos.firstOrNull()?.filePath
    }

    /**
     * Gets the best TMDB image (for posters) prioritizing ones with null language
     */
    private fun getBestTmdbImage(images: List<TmdbImage>?, size: String = "w500"): String? {
        if (images.isNullOrEmpty()) return null

        // First, try to find an image with null language
        val nullLanguageImage = images.find { it.iso6391 == null || it.iso6391 == "null" }
        if (nullLanguageImage?.filePath != null) return nullLanguageImage.filePath

        // If no null language image found, return the first one
        return images.firstOrNull()?.filePath
    }
}