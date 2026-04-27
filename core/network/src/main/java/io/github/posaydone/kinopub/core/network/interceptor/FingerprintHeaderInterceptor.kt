package io.github.posaydone.kinopub.core.network.interceptor

import io.github.posaydone.kinopub.core.network.provider.DeviceIdProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class FingerprintHeaderInterceptor @Inject constructor(
    private val deviceIdProvider: DeviceIdProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get the stable device ID
        val deviceId = deviceIdProvider.getDeviceId()

        // Add the header to the request
        val requestWithHeader =
            originalRequest.newBuilder().header("X-Device-Fingerprint", deviceId).build()

        return chain.proceed(requestWithHeader)
    }
}