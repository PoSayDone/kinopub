package io.github.posaydone.kinopub.core.model

data class Show(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val poster: String,
    val backdropUrl: String? = null,
    val year: Int,
    val quality: String? = null,
    val status: ShowStatus? = null,
    val description: String? = null,
    val isSeries: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val countries: List<Country> = emptyList(),
    val ratingKp: Double? = null,
    val ratingImdb: Double? = null,
    val votesKp: Int? = null,
    val votesImdb: Int? = null,
    val votesPos: Int = 0,
    val votesNeg: Int = 0,
    val ageRating: Int = 0,
    val isFavorite: Boolean? = null,
    val isDeferred: Boolean? = null,
    val durationSeconds: Int? = null,
    val maxEpisode: MaxEpisode? = null,
    val cast: String? = null,
    val director: String? = null,
    val voice: String? = null,
    val langs: Int? = null,
    val hasAc3: Boolean? = null,
    val subtitlesCount: Int? = null,
)

data class ShowStatus(
    val comment: String? = null,
    val status: Int? = null,
    val status_text: String? = null,
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
