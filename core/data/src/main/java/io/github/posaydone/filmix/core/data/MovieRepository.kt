package io.github.posaydone.filmix.core.data

import io.github.posaydone.filmix.core.model.FullShow
import io.github.posaydone.filmix.core.model.ShowDetails
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val filmixRepository: FilmixRepository,
) {
    private val cacheMutex = Mutex()
    private val fullShowCache = mutableMapOf<Int, FullShow>()

    suspend fun getFullMovieByFilmixId(filmixId: Int): FullShow = cacheMutex.withLock {
        fullShowCache[filmixId]?.let { return it }

        val showDetails = filmixRepository.getShowDetails(filmixId)
        val showImages = runCatching { filmixRepository.getShowImages(filmixId) }.getOrNull()
        val fullShow = showDetails.toFullShow(
            backdropUrl = showImages?.frames?.firstOrNull()?.url
                ?: showImages?.posters?.firstOrNull()?.url
                ?: showDetails.poster,
        )

        fullShowCache[filmixId] = fullShow
        fullShow
    }

    private fun ShowDetails.toFullShow(backdropUrl: String): FullShow {
        val isSeriesContent = lastEpisode != null || maxEpisode?.episode != null

        return FullShow(
            id = id,
            title = title,
            originalTitle = originalTitle,
            year = year,
            posterUrl = poster,
            backdropUrl = backdropUrl,
            logoUrl = null,
            description = shortStory.takeIf { it.isNotBlank() },
            shortDescription = shortStory.takeIf { it.isNotBlank() },
            ratingKp = ratingKinopoisk.takeIf { it > 0.0 },
            ratingImdb = ratingImdb.takeIf { it > 0.0 },
            votesKp = votesKinopoisk,
            votesImdb = votesIMDB,
            isSeries = isSeriesContent,
            isShow = isSeriesContent,
            genres = genres.map { it.name },
            countries = countries.map { it.name },
            ageRating = 0,
            movieLength = duration.takeIf { !isSeriesContent },
            seriesLength = maxEpisode?.episode.takeIf { isSeriesContent },
            quality = quality ?: "N/A",
            status = status?.status_text,
            votesPos = votesPos,
            votesNeg = votesNeg,
            tmdbPosterPaths = emptyList(),
            tmdbBackdropPaths = emptyList(),
            tmdbLogoPaths = emptyList(),
        )
    }
}
