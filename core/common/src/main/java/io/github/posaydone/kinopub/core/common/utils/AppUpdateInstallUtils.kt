package io.github.posaydone.kinopub.core.common.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri

fun Context.canInstallPackageUpdates(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()
}

fun Context.createInstallPackageIntent(apkUri: String): Intent {
    @Suppress("DEPRECATION")
    return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        setDataAndType(
            apkUri.toUri(),
            "application/vnd.android.package-archive",
        )
        putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
    }
}

fun Context.createUnknownSourcesSettingsIntent(): Intent {
    return Intent(
        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        "package:$packageName".toUri(),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
