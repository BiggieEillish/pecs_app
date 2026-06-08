package com.wings.picexchange.di

import com.wings.picexchange.tts.Speaker
import com.wings.picexchange.tts.TtsManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TtsModule {
    @Binds
    @Singleton
    abstract fun bindSpeaker(impl: TtsManager): Speaker
}
