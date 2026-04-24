package io.github.posaydone.filmix.core.model

sealed class ShowResourceResponse {
    data class SeriesResourceResponse(val series: Series) : ShowResourceResponse()
    data class MovieResourceResponse(val movies: List<VideoWithQualities>) : ShowResourceResponse()
}

data class Series(
    val seasons: List<Season>,
)

data class Season(
    val season: Int,
    val episodes: MutableList<Episode>,
) {
    override fun toString(): String = season.toString()
}

data class Episode(
    val episode: Int,
    val ad_skip: Int,
    val title: String,
    val released: String,
    val translations: MutableList<Translation>,
    val thumbnail: String? = null,
) {
    override fun toString(): String = "Серия ${episode}"
}

data class Translation(
    val translation: String,
    val files: List<File>,
    val audioIndex: Int = 0,
) {
    override fun toString(): String = translation
}

data class VideoWithQualities(
    val season: String,
    val episode: String,
    val adSkip: Int,
    val title: String,
    val released: String,
    val files: List<File>,
    val voiceover: String,
    val updated: Int,
    val uk: Boolean,
    val type: String,
    val audioIndex: Int = 0,
) {
    override fun toString(): String = voiceover
}

data class File(
    val url: String,
    val quality: Int,
    val proPlus: Boolean,
) {
    override fun toString(): String = quality.toString()
}
