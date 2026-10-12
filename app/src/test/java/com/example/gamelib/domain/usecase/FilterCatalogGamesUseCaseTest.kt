
package com.example.gamelib.domain.usecase

import com.example.gamelib.domain.model.Game
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterCatalogGamesUseCaseTest {

    private val useCase = FilterCatalogGamesUseCase()

    private val games = listOf(
        createGame(1, "Dota 2", "MOBA", "PC"),
        createGame(2, "Counter-Strike 2", "Shooter", "PC"),
        createGame(3, "Apex Legends", "Shooter", "Browser"),
        createGame(4, "Genshin Impact", "RPG", "PC")
    )

    private fun createGame(
        id: Int,
        title: String,
        genre: String,
        platform: String
    ): Game {
        return Game(
            id = id,
            title = title,
            description = "Описание",
            genre = genre,
            platform = platform,
            developer = "Test Studio"
        )
    }

    private fun filter(
        query: String = "",
        genre: String = "Все",
        platform: String = "Все"
    ): List<Game> {
        return useCase(
            games = games,
            searchQuery = query,
            selectedGenre = genre,
            selectedPlatform = platform
        )
    }

    @Test
    fun `без фильтров возвращаются все игры`() {

        val result = filter()

        assertEquals(4, result.size)
    }

    @Test
    fun `поиск по названию находит игру`() {

        val result = filter(query = "Dota")

        assertEquals(1, result.size)
        assertEquals("Dota 2", result.first().title)
    }

    @Test
    fun `поиск не зависит от регистра`() {

        val result = filter(query = "dOtA")

        assertEquals("Dota 2", result.single().title)
    }

    @Test
    fun `фильтрация по жанру возвращает нужные игры`() {

        val result = filter(genre = "Shooter")

        assertEquals(
            listOf("Counter-Strike 2", "Apex Legends"),
            result.map { it.title }
        )
    }

    @Test
    fun `фильтрация по платформе работает`() {

        val result = filter(platform = "Browser")

        assertEquals("Apex Legends", result.single().title)
    }

    @Test
    fun `все фильтры работают одновременно`() {

        val result = filter(
            query = "counter",
            genre = "Shooter",
            platform = "PC"
        )

        assertEquals(
            "Counter-Strike 2",
            result.single().title
        )
    }

    @Test
    fun `при отсутствии совпадений возвращается пустой список`() {

        val result = filter(query = "Несуществующая игра")

        assertTrue(result.isEmpty())
    }
}
