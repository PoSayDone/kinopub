package io.github.posaydone.kinopub.core.model

data class HashResponse(
    val token: String,
    val code: String,
    val expire: Long
)
