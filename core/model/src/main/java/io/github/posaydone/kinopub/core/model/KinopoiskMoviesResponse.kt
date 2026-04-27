package io.github.posaydone.kinopub.core.model

import com.google.gson.annotations.SerializedName

/**
 * The response object for a single movie/series getById query.
 */
data class KinopoiskMovie(
    @SerializedName("id") val id: Int,

    @SerializedName("externalId") val externalId: ExternalId?,

    @SerializedName("name") val name: String?,

    @SerializedName("alternativeName") val alternativeName: String?,

    @SerializedName("enName") val enName: String?,

    @SerializedName("names") val names: List<NameObject> = emptyList(),

    @SerializedName("type") val type: String,

    @SerializedName("typeNumber") val typeNumber: Int,

    @SerializedName("year") val year: Int?,

    @SerializedName("description") val description: String?,

    @SerializedName("shortDescription") val shortDescription: String?,

    @SerializedName("slogan") val slogan: String?,

    @SerializedName("status") val status: String?,

    @SerializedName("rating") val rating: Rating?,

    @SerializedName("votes") val votes: Votes?,

    @SerializedName("movieLength") val movieLength: Int?,

    @SerializedName("ratingMpaa") val ratingMpaa: String?,

    @SerializedName("ageRating") val ageRating: Int?,

    @SerializedName("logo") val logo: ImageObject?,

    @SerializedName("poster") val poster: ImageObject?,

    @SerializedName("backdrop") val backdrop: ImageObject?,

    @SerializedName("genres") val genres: List<KinopoiskGenre> = emptyList(),

    @SerializedName("countries") val countries: List<KinopoiskCountry> = emptyList(),

    @SerializedName("persons") val persons: List<KinopoiskPerson> = emptyList(),

    @SerializedName("seasonsInfo") val seasonsInfo: List<SeasonInfo> = emptyList(),

    @SerializedName("isSeries") val isSeries: Boolean,

    @SerializedName("seriesLength") val seriesLength: Int?,

    @SerializedName("totalSeriesLength") val totalSeriesLength: Int?,

    @SerializedName("releaseYears") val releaseYears: List<ReleaseYear> = emptyList(),

    @SerializedName("similarMovies") val similarMovies: List<SimilarMovie> = emptyList(),

    @SerializedName("sequelsAndPrequels") val sequelsAndPrequels: List<SequelOrPrequel> = emptyList(),

    @SerializedName("ticketsOnSale") val ticketsOnSale: Boolean?,

    @SerializedName("audience") val audience: List<Audience>?,

    @SerializedName("networks") val networks: Networks?,

    @SerializedName("facts") val facts: List<Fact>?,

    @SerializedName("imagesInfo") val imagesInfo: ImagesInfo?,
)

/**
 * Represents a movie or series from search results.
 */
data class KinopoiskMovieSearchResult(
    @SerializedName("id") val id: Int?,

    @SerializedName("name") val name: String?,

    @SerializedName("alternativeName") val alternativeName: String?,

    @SerializedName("enName") val enName: String?,

    @SerializedName("type") val type: String,

    @SerializedName("year") val year: Int,

    @SerializedName("description") val description: String?,

    @SerializedName("shortDescription") val shortDescription: String?,

    @SerializedName("movieLength") val movieLength: Int?,

    @SerializedName("isSeries") val isSeries: Boolean,

    @SerializedName("seriesLength") val seriesLength: Int?,

    @SerializedName("totalSeriesLength") val totalSeriesLength: Int?,

    @SerializedName("ageRating") val ageRating: Int,

    @SerializedName("ratingMpaa") val ratingMpaa: String?,

    @SerializedName("top250") val top250: Int?,

    @SerializedName("status") val status: String?,

    @SerializedName("names") val names: List<NameObject> = emptyList(),

    @SerializedName("externalId") val externalId: ExternalId?,

    @SerializedName("logo") val logo: ImageObject?,

    @SerializedName("poster") val poster: ImageObject?,

    @SerializedName("backdrop") val backdrop: ImageObject?,

    @SerializedName("rating") val rating: Rating,

    @SerializedName("votes") val votes: Votes,

    @SerializedName("genres") val genres: List<KinopoiskGenre> = emptyList(),

    @SerializedName("countries") val countries: List<KinopoiskCountry> = emptyList(),

    @SerializedName("releaseYears") val releaseYears: List<ReleaseYear> = emptyList(),
)

/**
 * The top-level response object for a movie search query.
 */
data class KinopoiskMoviesResponse(
    @SerializedName("docs") val docs: List<KinopoiskMovieSearchResult> = emptyList(),

    @SerializedName("total") val total: Int?,

    @SerializedName("limit") val limit: Int?,

    @SerializedName("page") val page: Int?,

    @SerializedName("pages") val pages: Int?,
)

/**
 * Represents various names and translations for a movie.
 */
data class NameObject(
    @SerializedName("name") val name: String?,

    @SerializedName("language") val language: String?,

    @SerializedName("type") val type: String?,
)

/**
 * Contains IDs from external movie databases.
 */
data class ExternalId(
    @SerializedName("imdb") val imdb: String?,

    @SerializedName("tmdb") val tmdb: Int?,

    @SerializedName("kpHD") val kpHD: String?,
)

/**

 * Represents an image with multiple resolutions. Used for logos, posters, and backdrops.
 */
data class ImageObject(
    @SerializedName("url") val url: String?,

    @SerializedName("previewUrl") val previewUrl: String?,
)

/**
 * Contains rating values from different sources.
 */
data class Rating(
    @SerializedName("kp") val kp: Double?,

    @SerializedName("imdb") val imdb: Double?,

    @SerializedName("filmCritics") val filmCritics: Double?,

    @SerializedName("russianFilmCritics") val russianFilmCritics: Double?,

    @SerializedName("await") val await: Double?,
)

/**
 * Contains vote counts from different sources.
 */
data class Votes(
    @SerializedName("kp") val kp: Int?,

    @SerializedName("imdb") val imdb: Int?,

    @SerializedName("filmCritics") val filmCritics: Int?,

    @SerializedName("russianFilmCritics") val russianFilmCritics: Int?,

    @SerializedName("await") val await: Int?,
)

/**
 * Represents a single genre.
 */
data class KinopoiskGenre(
    @SerializedName("name") val name: String,
)

/**
 * Represents a single country of origin.
 */
data class KinopoiskCountry(
    @SerializedName("name") val name: String,
)

/**
 * Represents the start and end years of a series' release.
 */
data class ReleaseYear(
    @SerializedName("start") val start: Int?,

    @SerializedName("end") val end: Int?,
)

/**
 * Represents a person involved in the movie/series.
 */
data class KinopoiskPerson(
    @SerializedName("id") val id: Int?,
    @SerializedName("photo") val photo: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("enName") val enName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("profession") val profession: String?,
    @SerializedName("enProfession") val enProfession: String?,
)

/**
 * Information about seasons for series.
 */
data class SeasonInfo(
    @SerializedName("number") val number: Int?,
    @SerializedName("episodesCount") val episodesCount: Int?,
)

/**
 * Information about similar movies.
 */
data class SimilarMovie(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("enName") val enName: String?,
    @SerializedName("alternativeName") val alternativeName: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("poster") val poster: ImageObject?,
    @SerializedName("rating") val rating: Rating?,
    @SerializedName("year") val year: Int?,
)

/**
 * Information about sequels and prequels.
 */
data class SequelOrPrequel(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("enName") val enName: String?,
    @SerializedName("alternativeName") val alternativeName: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("poster") val poster: ImageObject?,
    @SerializedName("rating") val rating: Rating?,
    @SerializedName("year") val year: Int?,
)

/**
 * Information about audience.
 */
data class Audience(
    @SerializedName("count") val count: Int?,
    @SerializedName("country") val country: String?,
)

/**
 * Information about networks.
 */
data class Networks(
    @SerializedName("items") val items: List<NetworkItem> = emptyList(),
)

/**
 * Network item information.
 */
data class NetworkItem(
    @SerializedName("name") val name: String?,
    @SerializedName("logo") val logo: ImageObject?,
)

/**
 * Information about facts.
 */
data class Fact(
    @SerializedName("value") val value: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("spoiler") val spoiler: Boolean?,
)

/**
 * Information about images.
 */
data class ImagesInfo(
    @SerializedName("postersCount") val postersCount: Int?,
    @SerializedName("backdropsCount") val backdropsCount: Int?,
    @SerializedName("framesCount") val framesCount: Int?,
)