package io.github.posaydone.kinopub.core.model

data class ShowImages(
    val frames: List<ShowImage?>,
    val posters: List<ShowImage?>,
)

data class ShowImage(
    val size: Int,
    val title: String,
    val url: String
)
