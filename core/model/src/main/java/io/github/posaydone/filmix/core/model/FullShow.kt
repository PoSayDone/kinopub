package io.github.posaydone.filmix.core.model

/**
 * A UI-facing content model assembled from the app's current backend payloads.
 */
data class FullShow(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val year: Int,
    val posterUrl: String,
    val backdropUrl: String,
    val logoUrl: String?,
    val description: String?,
    val shortDescription: String?,
    val ratingKp: Double?,
    val ratingImdb: Double?,
    val votesKp: Int?,
    val votesImdb: Int?,
    val isSeries: Boolean,
    val isShow: Boolean,
    val genres: List<String>,
    val countries: List<String>,
    val ageRating: Int,
    val movieLength: Int?,
    val seriesLength: Int?,
    val quality: String,
    val status: String?,
    val votesPos: Int,
    val votesNeg: Int,
    val tmdbPosterPaths: List<String>,
    val tmdbBackdropPaths: List<String>,
    val tmdbLogoPaths: List<String>,
)
