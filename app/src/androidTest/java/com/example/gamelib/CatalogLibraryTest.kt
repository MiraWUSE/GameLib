package com.example.gamelib

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamelib.data.local.database.GameDatabase
import com.example.gamelib.data.remote.api.FreeToGameApi
import com.example.gamelib.data.remote.dto.GameDto
import com.example.gamelib.data.repository.GameRepositoryImpl
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus
import com.example.gamelib.domain.usecase.*
import com.example.gamelib.presentation.screen.GameCatalogScreen
import com.example.gamelib.presentation.screen.GameEditScreen
import com.example.gamelib.presentation.screen.GameListScreen
import com.example.gamelib.presentation.theme.GameLibTheme
import com.example.gamelib.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogLibraryTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun catalogGameCanBeSavedEditedAndReadAfterDatabaseReopen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "catalog-test-${System.nanoTime()}"
        var database = Room.databaseBuilder(context, GameDatabase::class.java, databaseName).build()
        val store = ViewModelStore()
        try {
            // Only the network is replaced: UI, use cases, repository and Room are real.
            val api = object : FreeToGameApi {
                override suspend fun getGames() = listOf(
                    GameDto(452, "Catalog test game", "Short description", "RPG", "PC", "Studio",
                        thumbnail = "https://example.invalid/cover.jpg")
                )
            }
            val repository = GameRepositoryImpl(database.gameDao(), api)
            lateinit var viewModel: GameViewModel
            compose.runOnUiThread {
                viewModel = ViewModelProvider(store, object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(
                        GetGamesUseCase(repository), GetCatalogGamesUseCase(repository),
                        AddGameUseCase(repository), UpdateGameUseCase(repository), DeleteGameUseCase(repository)
                    ) as T
                })[GameViewModel::class.java]
            }
            var screen by mutableStateOf("catalog")
            var selectedGame by mutableStateOf<Game?>(null)
            compose.setContent {
                GameLibTheme {
                    when (screen) {
                        "catalog" -> GameCatalogScreen(viewModel) { screen = "library" }
                        "edit" -> GameEditScreen(selectedGame) {
                            viewModel.updateGame(it)
                            screen = "library"
                        }
                        else -> GameListScreen(
                            viewModel = viewModel,
                            onAddClick = {},
                            onEditClick = { selectedGame = it; screen = "edit" },
                            onCatalogClick = { screen = "catalog" }
                        )
                    }
                }
            }
            compose.waitUntil(10_000) { viewModel.uiState.value.catalogGames.isNotEmpty() }
            compose.onNodeWithText("Добавить в библиотеку").performScrollTo().performClick()
            compose.waitUntil(10_000) { viewModel.uiState.value.games.size == 1 }
            val saved = viewModel.uiState.value.games.single()
            assertTrue(saved.id > 0)
            assertEquals(452, saved.remoteId)
            assertEquals("https://example.invalid/cover.jpg", saved.thumbnail)
            assertEquals(GameStatus.WANT_TO_PLAY, saved.status)

            compose.onNodeWithText("Мои игры").performClick()
            compose.onNodeWithText("Catalog test game").assertIsDisplayed()
            compose.onNodeWithText("Редактировать").performScrollTo().performClick()
            compose.onNodeWithText("Статус: Хочу поиграть").performClick()
            compose.onNodeWithText("Играю").performClick()
            compose.onNodeWithText("Сохранить").performClick()
            compose.waitUntil(10_000) { viewModel.uiState.value.games.single().status == GameStatus.PLAYING }

            compose.runOnUiThread { store.clear() }
            database.close()
            database = Room.databaseBuilder(context, GameDatabase::class.java, databaseName).build()
            val persisted = runBlocking { GameRepositoryImpl(database.gameDao(), api).getAllGames().first().single() }
            assertEquals(saved.copy(status = GameStatus.PLAYING), persisted)
        } finally {
            compose.runOnUiThread { store.clear() }
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
