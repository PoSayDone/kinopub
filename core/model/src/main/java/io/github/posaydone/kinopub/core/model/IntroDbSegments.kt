package io.github.posaydone.kinopub.core.model

data class SegmentRange(
    val startMs: Long,
    val endMs: Long,
)

data class IntroDbSegments(
    val intro: SegmentRange? = null,
    val recap: SegmentRange? = null,
    val outro: SegmentRange? = null,
)

enum class SegmentType { INTRO, RECAP, OUTRO }

fun IntroDbSegments.activeSegmentAt(positionMs: Long): SegmentType? = when {
    intro != null && positionMs in intro.startMs..intro.endMs -> SegmentType.INTRO
    recap != null && positionMs in recap.startMs..recap.endMs -> SegmentType.RECAP
    outro != null && positionMs in outro.startMs..outro.endMs -> SegmentType.OUTRO
    else -> null
}

fun IntroDbSegments.rangeFor(type: SegmentType): SegmentRange? = when (type) {
    SegmentType.INTRO -> intro
    SegmentType.RECAP -> recap
    SegmentType.OUTRO -> outro
}
