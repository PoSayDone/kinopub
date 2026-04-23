package io.github.posaydone.filmix.core.model

data class Show(
    val id: Int,
    val last_episode: Int?,
    val last_season: Int?,
    val original_name: String,
    val poster: String,
    val backdropUrl: String = "",
    val quality: String,
    val status: ShowStatus?,
    val title: String,
    val votesNeg: Int,
    val votesPos: Int,
    val year: Int,
    val url: String,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val ratingKp: Double? = null,
    val ratingImdb: Double? = null,
    val votesKp: Int? = null,
    val votesImdb: Int? = null,
    val movieLength: Int? = null,
    val seriesLength: Int? = null,
    val ageRating: Int = 0,
)

data class ShowStatus(
    val comment: String? = null,
    val status: Int? = null,
    val status_text: String? = null,
)
