
package com.example.gamelib.domain.usecase

import com.example.gamelib.domain.model.Game
import javax.inject.Inject

class FilterCatalogGamesUseCase @Inject constructor() {

    operator fun invoke(
        games: List<Game>,
        searchQuery: String,
        selectedGenre: String,
        selectedPlatform: String
    ): List<Game> {

        val query = searchQuery.trim()

        return games.filter { game ->

            val matchesTitle =
                query.isEmpty() ||
                        game.title.contains(
                            query,
                            ignoreCase = true
                        )

            val matchesGenre =
                selectedGenre == "Все" ||
                        game.genre == selectedGenre

            val matchesPlatform =
                selectedPlatform == "Все" ||
                        game.platform == selectedPlatform

            matchesTitle &&
                    matchesGenre &&
                    matchesPlatform
        }
    }
}
