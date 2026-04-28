package io.github.posaydone.kinopub.core.data.updates

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubReleasesService {
    @GET("repos/{owner}/{repository}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repository") repository: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
    ): List<GithubReleaseDto>
}

data class GithubReleaseDto(
    val id: Long,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("tag_name") val tagName: String,
    val body: String?,
    val prerelease: Boolean,
    val assets: List<GithubAssetDto>,
)

data class GithubAssetDto(
    val name: String,
    val size: Long,
    @SerializedName("content_type") val contentType: String?,
    @SerializedName("browser_download_url") val browserDownloadUrl: String,
)
