package io.github.posaydone.kinopub.core.model

typealias ShowProgress = List<ShowProgressItem>

data class ShowProgressItem(
    val season: Int,
    val episode: Int,
    val voiceover: String,
    val time: Long,
    val quality: Int,
    val updatedAt: Long? = null,
)

private val showProgressComparator =
    compareByDescending<ShowProgressItem> { it.updatedAt ?: Long.MIN_VALUE }
        .thenByDescending { it.time }
        .thenByDescending { it.season }
        .thenByDescending { it.episode }

fun ShowProgress.sortedForResume(): ShowProgress = sortedWith(showProgressComparator)

fun ShowProgress.latestProgressItem(): ShowProgressItem? = sortedForResume().firstOrNull()

fun ShowProgress.latestSeriesProgress(): ShowProgressItem? =
    filter { it.season > 0 && it.episode > 0 }
        .sortedWith(showProgressComparator)
        .firstOrNull()

fun ShowProgress.findEpisodeProgress(
    season: Int,
    episode: Int,
): ShowProgressItem? = filter { it.season == season && it.episode == episode }
    .sortedWith(showProgressComparator)
    .firstOrNull()
