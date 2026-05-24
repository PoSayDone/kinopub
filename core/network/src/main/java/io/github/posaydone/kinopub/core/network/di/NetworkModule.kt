package io.github.posaydone.kinopub.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.posaydone.kinopub.core.network.BuildConfig
import io.github.posaydone.kinopub.core.network.Constants
import io.github.posaydone.kinopub.core.network.KinoPubAuthConfig
import io.github.posaydone.kinopub.core.network.interceptor.ApiUrlInterceptor
import io.github.posaydone.kinopub.core.network.interceptor.AuthInterceptor
import io.github.posaydone.kinopub.core.network.interceptor.RetryInterceptor
import io.github.posaydone.kinopub.core.network.interceptor.TokenAuthenticator
import io.github.posaydone.kinopub.core.network.interceptor.UserAgentInterceptor
import io.github.posaydone.kinopub.core.network.service.AuthService
import io.github.posaydone.kinopub.core.network.service.KinoPubApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    private const val PLAYBACK_OKHTTP_CLIENT = "playbackOkHttpClient"

    @Provides
    @Singleton
    fun provideKinoPubAuthConfig(): KinoPubAuthConfig {
        return KinoPubAuthConfig(
            clientId = BuildConfig.KINOPUB_CLIENT_ID,
            clientSecret = BuildConfig.KINOPUB_CLIENT_SECRET,
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        userAgentInterceptor: UserAgentInterceptor,
        apiUrlInterceptor: ApiUrlInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(apiUrlInterceptor)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @Named(PLAYBACK_OKHTTP_CLIENT)
    fun providePlaybackOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        userAgentInterceptor: UserAgentInterceptor,
        apiUrlInterceptor: ApiUrlInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(apiUrlInterceptor)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthService(
        userAgentInterceptor: UserAgentInterceptor,
        apiUrlInterceptor: ApiUrlInterceptor,
    ): AuthService {
        return Retrofit.Builder().baseUrl(Constants.KINOPUB_API_URL)
            .addConverterFactory(GsonConverterFactory.create()).client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor(apiUrlInterceptor)
                    .addInterceptor(userAgentInterceptor)
                    .build()
            ).build().create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideKinoPubApiService(
        okHttpClient: OkHttpClient,
    ): KinoPubApiService {
        return Retrofit.Builder().baseUrl(Constants.KINOPUB_API_URL)
            .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
            .create(KinoPubApiService::class.java)
    }
}
