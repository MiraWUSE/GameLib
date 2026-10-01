package com.example.gamelib

import com.example.gamelib.data.mapper.toDomain
import com.example.gamelib.data.mapper.toEntity
import com.example.gamelib.data.remote.dto.GameDto
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class GameThumbnailTest {
    @Test
    fun apiThumbnailSurvivesMappingToLocalStorage() {
        val dto = Gson().fromJson("""{
            "id":452,"title":"Game","short_description":"Description",
            "genre":"RPG","platform":"PC","developer":"Studio",
            "thumbnail":"https://www.freetogame.com/g/452/thumbnail.jpg"
        }""", GameDto::class.java)
        val restored = dto.toDomain().toEntity().toDomain()
        assertEquals(dto.thumbnail, restored.thumbnail)
        assertEquals(452, restored.remoteId)
    }

    @Test
    fun missingApiThumbnailIsAllowed() {
        val dto = Gson().fromJson("""{
            "id":1,"title":"Game","short_description":"Description",
            "genre":"RPG","platform":"PC","developer":"Studio"
        }""", GameDto::class.java)
        assertNull(dto.toDomain().toEntity().thumbnail)
    }
}
