package io.github.posaydone.kinopub.core.network.interceptor

import io.github.posaydone.kinopub.core.network.ApiUrlManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intercepts every OkHttp request and rewrites the scheme/host/port to the
 * URL stored in [ApiUrlManager]. This allows changing the API base URL at
 * runtime without recreating the Retrofit singleton.
 */
@Singleton
class ApiUrlInterceptor @Inject constructor(
    private val apiUrlManager: ApiUrlManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val customBaseUrl = apiUrlManager.getApiUrl().toHttpUrlOrNull()
            ?: return chain.proceed(chain.request())

        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .scheme(customBaseUrl.scheme)
            .host(customBaseUrl.host)
            .port(customBaseUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
