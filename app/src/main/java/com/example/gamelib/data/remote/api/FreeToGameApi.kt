package com.example.gamelib.data.remote.api

import com.example.gamelib.data.remote.dto.GameDto
import retrofit2.http.GET

interface FreeToGameApi {

    @GET("games")
    suspend fun getGames(): List<GameDto>
}