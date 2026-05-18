package com.standbyus.app.di

import android.content.Context
import androidx.room.Room
import com.standbyus.app.data.local.AppDatabase
import com.standbyus.app.data.local.CheckinDao
import com.standbyus.app.data.local.StatusDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "standbyus_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideStatusDao(database: AppDatabase): StatusDao = database.statusDao()

    @Provides
    fun provideCheckinDao(database: AppDatabase): CheckinDao = database.checkinDao()
}
