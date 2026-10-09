package com.example.gamelib.data.repository

import com.example.gamelib.BuildConfig
import com.example.gamelib.data.remote.storage.SupabaseStorageApi
import com.example.gamelib.domain.repository.ImageStorageRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ImageStorageRepositoryImpl @Inject constructor(
    private val supabaseStorageApi: SupabaseStorageApi
) : ImageStorageRepository {

    override suspend fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String {

        val requestBody = bytes.toRequestBody(
            contentType.toMediaType()
        )

        val response = supabaseStorageApi.uploadImage(
            fileName = fileName,
            apiKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
            contentType = contentType,
            file = requestBody
        )

        if (!response.isSuccessful) {
            throw Exception(
                "Ошибка загрузки изображения: ${response.code()}"
            )
        }

        return "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/game-covers/$fileName"
    }
}