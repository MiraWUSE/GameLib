package com.example.gamelib.domain.usecase

import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.repository.GameRepository

class UpdateGameUseCase(
    private val repository: GameRepository
) {

    suspend operator fun invoke(game: Game) {
        repository.updateGame(game)
    }
}