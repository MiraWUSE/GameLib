package com.example.gamelib.domain.usecase

import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GetGamesUseCase(
    private val repository: GameRepository
) {

    operator fun invoke(): Flow<List<Game>> {
        return repository.getAllGames()
    }
}