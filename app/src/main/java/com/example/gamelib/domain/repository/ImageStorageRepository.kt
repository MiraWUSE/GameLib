package com.example.gamelib.domain.repository

interface ImageStorageRepository {

    suspend fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String
}