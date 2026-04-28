package io.github.posaydone.kinopub.core.data.updates

import java.util.Locale

data class AppUpdateInfo(
    val id: Long,
    val versionName: String,
    val releaseUrl: String,
    val apkName: String,
    val apkUrl: String,
    val apkSizeBytes: Long,
    val releaseNotes: String,
) {
    val versionId: VersionId = VersionId(versionName)
}

sealed interface AppUpdateDownloadState {
    data class InProgress(val progress: Float?) : AppUpdateDownloadState
    data class ReadyToInstall(val apkUri: String) : AppUpdateDownloadState
    data class Failed(val message: String) : AppUpdateDownloadState
}

data class VersionId(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val variantType: String,
    val variantNumber: Int,
) : Comparable<VersionId> {

    override fun compareTo(other: VersionId): Int {
        var diff = major.compareTo(other.major)
        if (diff != 0) return diff

        diff = minor.compareTo(other.minor)
        if (diff != 0) return diff

        diff = patch.compareTo(other.patch)
        if (diff != 0) return diff

        diff = variantWeight(variantType).compareTo(variantWeight(other.variantType))
        if (diff != 0) return diff

        return variantNumber.compareTo(other.variantNumber)
    }

    private fun variantWeight(variantType: String): Int = when (variantType.lowercase(Locale.ROOT)) {
        "n", "nightly" -> 0
        "a", "alpha" -> 1
        "b", "beta" -> 2
        "rc" -> 3
        "" -> 4
        else -> 0
    }
}

val VersionId.isStable: Boolean
    get() = variantType.isBlank()

fun VersionId(versionName: String): VersionId {
    val normalized = versionName.removePrefix("v").trim()
    if (normalized.startsWith("n", ignoreCase = true)) {
        return VersionId(
            major = 0,
            minor = 0,
            patch = normalized.filter(Char::isDigit).toIntOrNull() ?: 0,
            variantType = "n",
            variantNumber = 0,
        )
    }

    val mainVersion = normalized.substringBeforeLast('-')
    val variant = normalized.substringAfterLast('-', missingDelimiterValue = "")
    val parts = mainVersion.split('.')

    return VersionId(
        major = parts.getOrNull(0)?.toIntOrNull() ?: 0,
        minor = parts.getOrNull(1)?.toIntOrNull() ?: 0,
        patch = parts.getOrNull(2)?.toIntOrNull() ?: 0,
        variantType = variant.takeWhile(Char::isLetter),
        variantNumber = variant.dropWhile(Char::isLetter).filter(Char::isDigit).toIntOrNull() ?: 0,
    )
}
