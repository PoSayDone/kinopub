package io.github.posaydone.kinopub.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.posaydone.kinopub.core.data.SessionManagerImpl
import io.github.posaydone.kinopub.core.data.updates.GithubReleasesService
import io.github.posaydone.kinopub.core.model.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@JvmSuppressWildcards
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    private const val GITHUB_API_URL = "https://api.github.com/"

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
    ): SessionManager = SessionManagerImpl(context)

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideGithubReleasesService(): GithubReleasesService {
        return Retrofit.Builder()
            .baseUrl(GITHUB_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubReleasesService::class.java)
    }
}
