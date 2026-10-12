
package com.example.gamelib.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus
import com.example.gamelib.presentation.screen.GameListContent
import com.example.gamelib.presentation.state.GameUiState
import com.example.gamelib.presentation.theme.GameLibTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameListScreenTest {

    @get:Rule
    val compose = createComposeRule()

    // Тестовая игра
    private val testGame = Game(
        id = 1,
        title = "Dota 2",
        description = "Командная игра",
        genre = "MOBA",
        platform = "PC",
        developer = "Valve",
        status = GameStatus.WANT_TO_PLAY,
        thumbnail = null
    )

    // Запуск тестируемой страницы
    private fun showScreen(
        games: List<Game> = emptyList(),
        onAddClick: () -> Unit = {},
        onCatalogClick: () -> Unit = {},
        onEditClick: (Game) -> Unit = {},
        onDeleteClick: (Game) -> Unit = {}
    ) {

        compose.setContent {

            GameLibTheme {

                GameListContent(
                    uiState = GameUiState(games = games),
                    onAddClick = onAddClick,
                    onCatalogClick = onCatalogClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }

    @Test
    fun emptyLibraryShowsMainButtons() {

        showScreen()

        // Основные кнопки должны присутствовать
        compose.onNodeWithText("Каталог игр")
            .assertIsDisplayed()

        compose.onNodeWithText("Добавить игру")
            .assertIsDisplayed()

        // Карточки игры быть не должно
        compose.onNodeWithText("Dota 2")
            .assertDoesNotExist()
        compose.onNodeWithText("Редактировать").assertDoesNotExist()
        compose.onNodeWithText("Удалить").assertDoesNotExist()
    }

    @Test
    fun gameCardShowsCorrectInformation() {

        showScreen(games = listOf(testGame))

        // Проверяем название
        compose.onNodeWithText("Dota 2")
            .performScrollTo()
            .assertIsDisplayed()

        // Проверяем жанр
        compose.onNodeWithText("Жанр: MOBA")
            .performScrollTo()
            .assertIsDisplayed()

        // Проверяем платформу
        compose.onNodeWithText("Платформа: PC")
            .performScrollTo()
            .assertIsDisplayed()

        // Проверяем разработчика
        compose.onNodeWithText("Разработчик: Valve")
            .performScrollTo()
            .assertIsDisplayed()

        // Проверяем статус
        compose.onNodeWithText("Статус: Хочу поиграть")
            .performScrollTo()
            .assertIsDisplayed()

        // Проверяем наличие кнопок карточки
        compose.onNodeWithText("Редактировать")
            .performScrollTo()
            .assertIsDisplayed()

        compose.onNodeWithText("Удалить")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun addButtonCallsCallback() {

        var clicked = false

        showScreen(
            onAddClick = {
                clicked = true
            }
        )

        compose.onNodeWithText("Добавить игру")
            .performClick()

        compose.runOnIdle {
            assertEquals(true, clicked)
        }
    }

    @Test
    fun catalogButtonCallsCallback() {

        var clicked = false

        showScreen(
            onCatalogClick = {
                clicked = true
            }
        )

        compose.onNodeWithText("Каталог игр")
            .performClick()

        compose.runOnIdle {
            assertEquals(true, clicked)
        }
    }

    @Test
    fun editButtonReturnsCorrectGame() {

        var selectedGame: Game? = null

        showScreen(
            games = listOf(testGame),
            onEditClick = { game ->
                selectedGame = game
            }
        )

        compose.onNodeWithText("Редактировать")
            .performScrollTo()
            .performClick()

        compose.runOnIdle {
            assertEquals(testGame, selectedGame)
        }
    }

    @Test
    fun deleteButtonReturnsCorrectGame() {

        var deletedGame: Game? = null

        showScreen(
            games = listOf(testGame),
            onDeleteClick = { game ->
                deletedGame = game
            }
        )

        compose.onNodeWithText("Удалить")
            .performScrollTo()
            .performClick()

        compose.runOnIdle {
            assertEquals(testGame, deletedGame)
        }
    }
}
