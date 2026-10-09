package com.example.gamelib.domain.usecase

import com.example.gamelib.domain.repository.ImageStorageRepository
import javax.inject.Inject

class UploadGameImageUseCase @Inject constructor(
    private val imageStorageRepository: ImageStorageRepository
) {

    suspend operator fun invoke(
        bytes: ByteArray,
        fileName: String,
        contentType: String
    ): String {

        return imageStorageRepository.uploadImage(
            bytes = bytes,
            fileName = fileName,
            contentType = contentType
        )
    }
}