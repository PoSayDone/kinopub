package io.github.posaydone.kinopub.core.model

data class PageWithShows<T>(
    val has_next_page: Boolean,
    val items: List<T>,
    val page: Int,
    val status: String,
)


