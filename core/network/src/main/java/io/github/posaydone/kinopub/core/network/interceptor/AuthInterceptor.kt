package io.github.posaydone.kinopub.core.network.interceptor

import android.util.Log
import io.github.posaydone.kinopub.core.model.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private const val TAG = "AuthInterceptor"

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sessionManager.fetchAccessToken()

        Log.d(TAG, "intercept: ${accessToken}")

        val requestBuilder = chain.request().newBuilder()
        accessToken?.let { requestBuilder.header("Authorization", "Bearer $it") }

        return chain.proceed(requestBuilder.build())
    }   
}
