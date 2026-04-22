package io.github.posaydone.filmix.core.model

import com.google.gson.annotations.SerializedName

data class StreamTypeResponse(
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("allowed_types") val allowedTypes: List<String>,
    val labels: Map<String, String> = emptyMap(),
)

data class StreamTypeRequest(
    @SerializedName("stream_type") val streamType: String,
)

data class ServerLocationResponse(
    val serverLocation: String,
    val labels: Map<String, String> = emptyMap(),
)
