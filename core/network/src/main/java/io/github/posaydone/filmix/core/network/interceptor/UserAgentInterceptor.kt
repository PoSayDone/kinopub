package io.github.posaydone.filmix.core.network.interceptor

import android.os.Build
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.network.provider.DeviceIdProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UserAgentInterceptor @Inject constructor(
    private val deviceIdProvider: DeviceIdProvider,
    private val sessionManager: SessionManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val device = Build.DEVICE.lowercase().replace(" ", "_")
        val osVersion = Build.VERSION.SDK_INT
        val deviceId = deviceIdProvider.getDeviceId().uppercase()
        val username = sessionManager.fetchUsername() ?: "unknown"

        val userAgent = "kinopub/next device/$device os/Android$osVersion id/$deviceId username/$username"

        val request = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .build()

        return chain.proceed(request)
    }
}
