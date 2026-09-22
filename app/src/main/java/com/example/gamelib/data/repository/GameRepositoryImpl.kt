package com.example.gamelib.data.repository

import com.example.gamelib.data.local.dao.GameDao
import com.example.gamelib.data.mapper.toDomain
import com.example.gamelib.data.mapper.toEntity
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao
): GameRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames()
            .map { games -> games.map { it.toDomain() }
            }
    }

    override suspend fun addGame(game: Game) {
        gameDao.insertGame(game.toEntity())
    }

    override suspend fun updateGame(game: Game) {
        gameDao.updateGame(game.toEntity())
    }

    override suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game.toEntity())
    }
}
