package io.github.posaydone.kinopub.core.network.provider

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Provides a stable, unique identifier for the device installation.
     * This uses ANDROID_ID, which is constant for the lifetime of a device's factory state.
     * It will change if the device is factory reset.
     *
     * This is the recommended practice for most non-advertising device identification.
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
