package io.github.posaydone.filmix.core.model

data class ShowDetails(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val year: Int,
    val poster: String,
    val backdropUrl: String?,
    val description: String,
    val isSeries: Boolean,
    val quality: String?,
    val status: ShowStatus?,
    val isFavorite: Boolean?,
    val isDeferred: Boolean?,
    val isHdr: Boolean?,
    val ratingKp: Double,
    val ratingImdb: Double,
    val votesKp: Int?,
    val votesImdb: Int?,
    val votesPos: Int,
    val votesNeg: Int,
    val duration: Int?,
    val ageRating: Int = 0,
    val genres: List<Genre>,
    val countries: List<Country>,
    val lastEpisode: LastEpisode?,
    val maxEpisode: MaxEpisode?,
    val idKinopoisk: Int?,
    val category: String?,
)

data class Genre(
    val id: Int,
    val slug: String,
    val name: String,
)

data class Country(
    val id: Int,
    val name: String,
)

data class MaxEpisode(
    val season: Int? = null,
    val episode: Int? = null,
)

data class LastEpisode(
    val season: Int? = null,
    val episode: String? = null,
    val translation: String? = null,
    val date: String? = null,
)
