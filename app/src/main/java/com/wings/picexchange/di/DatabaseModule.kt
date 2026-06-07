package com.wings.picexchange.di

import android.content.Context
import androidx.room.Room
import com.wings.picexchange.data.local.PicExchangeDatabase
import com.wings.picexchange.data.local.SeedCallback
import com.wings.picexchange.data.local.dao.CardDao
import com.wings.picexchange.data.local.dao.CategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PicExchangeDatabase =
        Room.databaseBuilder(context, PicExchangeDatabase::class.java, PicExchangeDatabase.NAME)
            .addCallback(SeedCallback())
            .build()

    @Provides
    fun provideCategoryDao(db: PicExchangeDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideCardDao(db: PicExchangeDatabase): CardDao = db.cardDao()
}
