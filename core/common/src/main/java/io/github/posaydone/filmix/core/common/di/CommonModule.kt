package io.github.posaydone.filmix.core.common.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.posaydone.filmix.core.data.di.ApplicationScope
import io.github.posaydone.filmix.core.common.services.buildPlaybackPlayer
import io.github.posaydone.filmix.core.model.AuthEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CommonModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun providePlayer(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
    ): ExoPlayer {
        return buildPlaybackPlayer(context, okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAuthEventChannel(): MutableSharedFlow<AuthEvent> {
        return MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
    }

    @Provides
    @Singleton
    fun provideAuthEventFlow(
        mutableSharedFlow: MutableSharedFlow<AuthEvent>,
    ): SharedFlow<AuthEvent> {
        return mutableSharedFlow
    }
}
