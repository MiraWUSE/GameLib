package com.example.gamelib.fake

import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGameRepository : GameRepository {

    private val games = MutableStateFlow<List<Game>>(emptyList())

    private var catalogGames = emptyList<Game>()

    var shouldFailCatalog = false

    private var nextId = 1

    fun setGames(newGames: List<Game>) {
        games.value = newGames
        nextId = (newGames.maxOfOrNull { it.id } ?: 0) + 1
    }

    fun setCatalogGames(newGames: List<Game>) {
        catalogGames = newGames
    }

    override fun getAllGames(): Flow<List<Game>> {
        return games
    }

    override suspend fun getCatalogGames(): List<Game> {
        if (shouldFailCatalog) {
            throw Exception("Ошибка загрузки каталога")
        }

        return catalogGames
    }

    override suspend fun addGame(game: Game) {
        val savedGame = game.copy(id = nextId++)
        games.value = games.value + savedGame
    }

    override suspend fun updateGame(game: Game) {
        games.value = games.value.map {
            if (it.id == game.id) game else it
        }
    }

    override suspend fun deleteGame(game: Game) {
        games.value = games.value.filter {
            it.id != game.id
        }
    }
}