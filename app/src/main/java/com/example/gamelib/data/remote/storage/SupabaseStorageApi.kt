package com.example.gamelib.data.remote.storage

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApi {

    @POST("storage/v1/object/game-covers/{fileName}")
    suspend fun uploadImage(
        @Path("fileName") fileName: String,

        @Header("apikey")
        apiKey: String,

        @Header("Content-Type")
        contentType: String,

        @Body
        file: RequestBody
    ): Response<ResponseBody>
}