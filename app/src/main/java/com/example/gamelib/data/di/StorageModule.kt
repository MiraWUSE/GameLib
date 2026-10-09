package com.example.gamelib.data.di

import com.example.gamelib.BuildConfig
import com.example.gamelib.data.remote.storage.SupabaseStorageApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideSupabaseStorageApi(): SupabaseStorageApi {

        return Retrofit.Builder()
            .baseUrl("${BuildConfig.SUPABASE_URL}/")
            .build()
            .create(SupabaseStorageApi::class.java)
    }
}