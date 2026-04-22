package io.github.posaydone.filmix.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.posaydone.filmix.core.network.BuildConfig
import io.github.posaydone.filmix.core.network.Constants
import io.github.posaydone.filmix.core.network.KinoPubAuthConfig
import io.github.posaydone.filmix.core.network.interceptor.AuthInterceptor
import io.github.posaydone.filmix.core.network.interceptor.RetryInterceptor
import io.github.posaydone.filmix.core.network.interceptor.TokenAuthenticator
import io.github.posaydone.filmix.core.network.service.AuthService
import io.github.posaydone.filmix.core.network.service.KinoPubApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun provideKinoPubAuthConfig(): KinoPubAuthConfig {
        return KinoPubAuthConfig(
            clientId = BuildConfig.KINOPUB_CLIENT_ID,
            clientSecret = BuildConfig.KINOPUB_CLIENT_SECRET,
        )
    }

    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(authInterceptor).addInterceptor(retryInterceptor)
            .authenticator(tokenAuthenticator).build()

    }

    @Provides
    @Singleton
    fun provideAuthService(
    ): AuthService {
        return Retrofit.Builder().baseUrl(Constants.KINOPUB_API_URL)
            .addConverterFactory(GsonConverterFactory.create()).client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
