package io.github.posaydone.filmix.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.math.pow

class RetryInterceptor @Inject constructor() : Interceptor {
    companion object {
        private const val NUMBER_OF_RETRIES = 3
        private const val INITIAL_DELAY_MILLIS = 300.0
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0

        while (!response.isSuccessful && tryCount < NUMBER_OF_RETRIES) {
            tryCount++

            val exponentialDelay = INITIAL_DELAY_MILLIS * 2.0.pow(tryCount - 1)

            try {
                Thread.sleep(exponentialDelay.toLong())
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Thread interrupted", e)
            }

            response.close()

            response = chain.proceed(request)
        }

        return response
    }
}
