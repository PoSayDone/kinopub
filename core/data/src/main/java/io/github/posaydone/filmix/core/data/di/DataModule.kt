package io.github.posaydone.filmix.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.posaydone.filmix.core.data.AuthRepository
import io.github.posaydone.filmix.core.data.FilmixRepository
import io.github.posaydone.filmix.core.data.SessionManagerImpl
import io.github.posaydone.filmix.core.model.SessionManager
import io.github.posaydone.filmix.core.network.dataSource.AuthRemoteDataSource
import io.github.posaydone.filmix.core.network.dataSource.FilmixRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@JvmSuppressWildcards
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideFilmixRepository(
        dataSource: FilmixRemoteDataSource,
        sessionManager: SessionManager,
        @ApplicationScope externalScope: CoroutineScope
    ): FilmixRepository {
        return FilmixRepository(dataSource, sessionManager, externalScope)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        dataSource: AuthRemoteDataSource,
    ): AuthRepository {
        return AuthRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
    ): SessionManager {
        return SessionManagerImpl(context)
    }


    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
