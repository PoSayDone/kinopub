package io.github.posaydone.kinopub.core.data

import android.util.Log
import io.github.posaydone.kinopub.core.model.IntroDbSegments
import io.github.posaydone.kinopub.core.model.SegmentRange
import io.github.posaydone.kinopub.core.model.introdb.IntroDbSegmentDto
import io.github.posaydone.kinopub.core.model.introdb.IntroDbSegmentsResponse
import io.github.posaydone.kinopub.core.network.service.IntroDbApiService
import javax.inject.Inject

class IntroDbRepository @Inject constructor(
    private val introDbApiService: IntroDbApiService,
) {
    // Most episodes have no submitted segments yet, so a failed/empty lookup is the
    // common case, not an error — never let it surface to the player.
    suspend fun getSegments(imdbId: String, season: Int, episode: Int): IntroDbSegments? =
        runCatching {
            introDbApiService.getSegments(imdbId, season, episode).toDomain()
        }.onFailure {
            Log.d(TAG, "getSegments: no segments for imdbId=$imdbId season=$season episode=$episode: ${it.message}")
        }.getOrNull()

    private fun IntroDbSegmentsResponse.toDomain(): IntroDbSegments? {
        val intro = intro?.toRange()
        val recap = recap?.toRange()
        val outro = outro?.toRange()
        if (intro == null && recap == null && outro == null) return null
        return IntroDbSegments(intro = intro, recap = recap, outro = outro)
    }

    private fun IntroDbSegmentDto.toRange(): SegmentRange? {
        val start = start_ms ?: return null
        val end = end_ms ?: return null
        if (end <= start) return null
        return SegmentRange(startMs = start, endMs = end)
    }

    companion object {
        private const val TAG = "IntroDbRepository"
    }
}
