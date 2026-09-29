package com.example.gamelib.domain.usecase

import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.repository.GameRepository
import javax.inject.Inject

class GetCatalogGamesUseCase @Inject constructor(
    private val repository: GameRepository
) {

    suspend operator fun invoke(): List<Game> {
        return repository.getCatalogGames()
    }
}