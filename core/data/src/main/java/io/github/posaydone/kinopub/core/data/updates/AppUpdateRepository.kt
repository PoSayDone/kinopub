package io.github.posaydone.kinopub.core.data.updates

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val APK_CONTENT_TYPE = "application/vnd.android.package-archive"
private const val META_UPDATE_REPOSITORY = "io.github.posaydone.kinopub.UPDATE_REPOSITORY"
private const val META_UPDATE_ASSET_KEYWORD = "io.github.posaydone.kinopub.UPDATE_ASSET_KEYWORD"

@Singleton
class AppUpdateRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val githubReleasesService: GithubReleasesService,
) {
    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private val cachedCurrentVersionName by lazy {
        packageInfo().versionName.orEmpty().ifBlank { "0.0.0" }
    }

    fun getCurrentVersionName(): String = cachedCurrentVersionName

    suspend fun fetchUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val config = updateConfig()
            ?: throw IllegalStateException("App updates are not configured for this build.")

        val currentVersionId = VersionId(cachedCurrentVersionName)
        val availableReleases = githubReleasesService.getReleases(
            owner = config.owner,
            repository = config.repository,
        ).mapNotNull { release ->
            release.toUpdateInfo(assetKeyword = config.assetKeyword)
        }.sortedBy { it.versionId }

        val compatibleReleases = if (currentVersionId.isStable) {
            availableReleases.filter { it.versionId.isStable }
        } else {
            availableReleases
        }

        compatibleReleases.maxByOrNull { it.versionId }?.takeIf { it.versionId > currentVersionId }
    }

    fun downloadUpdate(release: AppUpdateInfo): Flow<AppUpdateDownloadState> = flow {
        val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
        val request = DownloadManager.Request(release.apkUrl.toUri())
            .setTitle("$appName ${release.versionName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType(APK_CONTENT_TYPE)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                release.apkName,
            )

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        emit(AppUpdateDownloadState.InProgress(progress = null))

        while (currentCoroutineContext().isActive) {
            downloadManager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) {
                    emit(
                        AppUpdateDownloadState.Failed(
                            "The downloaded APK could not be found.",
                        ),
                    )
                    return@flow
                }

                val bytesDownloaded = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                )
                val bytesTotal = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                )
                val status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS),
                )

                val progress = if (bytesTotal > 0L) {
                    bytesDownloaded.toFloat() / bytesTotal.toFloat()
                } else {
                    null
                }
                emit(AppUpdateDownloadState.InProgress(progress))

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val apkUri = downloadManager.getUriForDownloadedFile(downloadId)?.toString()
                        if (apkUri.isNullOrBlank()) {
                            emit(
                                AppUpdateDownloadState.Failed(
                                    "The downloaded APK could not be found.",
                                ),
                            )
                        } else {
                            emit(AppUpdateDownloadState.ReadyToInstall(apkUri))
                        }
                        return@flow
                    }

                    DownloadManager.STATUS_FAILED -> {
                        emit(
                            AppUpdateDownloadState.Failed(
                                "Update download failed.",
                            ),
                        )
                        return@flow
                    }
                }
            }

            delay(250)
        }
    }.flowOn(Dispatchers.IO)

    private fun GithubReleaseDto.toUpdateInfo(assetKeyword: String): AppUpdateInfo? {
        if (prerelease && VersionId(tagName.removePrefix("v")).isStable) {
            return null
        }

        val apkAssets = assets.filter { asset ->
            asset.name.endsWith(".apk", ignoreCase = true) || asset.contentType == APK_CONTENT_TYPE
        }
        val selectedAsset = selectAsset(apkAssets, assetKeyword) ?: return null

        return AppUpdateInfo(
            id = id,
            versionName = tagName.removePrefix("v"),
            releaseUrl = htmlUrl,
            apkName = selectedAsset.name,
            apkUrl = selectedAsset.browserDownloadUrl,
            apkSizeBytes = selectedAsset.size,
            releaseNotes = body.orEmpty().trim(),
        )
    }

    private fun selectAsset(
        assets: List<GithubAssetDto>,
        assetKeyword: String,
    ): GithubAssetDto? {
        if (assets.isEmpty()) return null
        if (assets.size == 1) return assets.first()

        assets.firstOrNull { it.name.contains(assetKeyword, ignoreCase = true) }?.let {
            return it
        }

        val packageHint = context.packageName.substringAfterLast('.')
        assets.firstOrNull { it.name.contains(packageHint, ignoreCase = true) }?.let {
            return it
        }

        return assets.first()
    }

    private fun updateConfig(): UpdateConfig? {
        val metadata = applicationInfo().metaData ?: return null
        val repositoryValue = metadata.getString(META_UPDATE_REPOSITORY).orEmpty().trim()
        if (repositoryValue.isBlank() || !repositoryValue.contains('/')) {
            return null
        }

        val owner = repositoryValue.substringBefore('/').trim()
        val repository = repositoryValue.substringAfter('/').trim()
        if (owner.isBlank() || repository.isBlank()) {
            return null
        }

        val assetKeyword = metadata.getString(META_UPDATE_ASSET_KEYWORD)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: context.packageName.substringAfterLast('.')

        return UpdateConfig(
            owner = owner,
            repository = repository,
            assetKeyword = assetKeyword,
        )
    }

    private fun packageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }

    private fun applicationInfo(): ApplicationInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA,
            )
        }
    }
}

private data class UpdateConfig(
    val owner: String,
    val repository: String,
    val assetKeyword: String,
)
