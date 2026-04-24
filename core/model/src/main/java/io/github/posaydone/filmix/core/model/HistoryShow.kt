package io.github.posaydone.filmix.core.model

data class HistoryShow(
    val id: Int,
    val title: String,
    val poster: String,
    val isSeries: Boolean,
    val description: String,
    val thumbnail: String?,
    val watchedSeconds: Int?,
    val durationSeconds: Int?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val episodeTitle: String?,
)
