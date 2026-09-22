package com.example.gamelib.domain.repository

import com.example.gamelib.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    fun getAllGames(): Flow<List<Game>>

    suspend fun addGame(game: Game)

    suspend fun updateGame(game: Game)

    suspend fun deleteGame(game: Game)
}