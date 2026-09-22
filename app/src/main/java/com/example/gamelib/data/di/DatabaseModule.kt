package com.example.gamelib.data.di

import android.content.Context
import androidx.room3.Room
import com.example.gamelib.data.local.dao.GameDao
import com.example.gamelib.data.local.database.GameDatabase
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
    fun provideGameDatabase(
        @ApplicationContext context: Context
    ): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            "game_database"
        ).build()
    }

    @Provides
    fun provideGameDao(
        database: GameDatabase
    ): GameDao {
        return database.gameDao()
    }
}