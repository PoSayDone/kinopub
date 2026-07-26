package io.github.posaydone.kinopub.core.model.introdb

data class IntroDbSegmentsResponse(
    val imdb_id: String? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val intro: IntroDbSegmentDto? = null,
    val recap: IntroDbSegmentDto? = null,
    val outro: IntroDbSegmentDto? = null,
)

data class IntroDbSegmentDto(
    val start_ms: Long? = null,
    val end_ms: Long? = null,
    val start_sec: Double? = null,
    val end_sec: Double? = null,
    val confidence: Double? = null,
    val submission_count: Int? = null,
)
