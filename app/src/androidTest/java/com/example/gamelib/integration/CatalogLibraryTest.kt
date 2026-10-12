
package com.example.gamelib.integration

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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamelib.data.local.database.GameDatabase
import com.example.gamelib.data.remote.api.FreeToGameApi
import com.example.gamelib.data.remote.dto.GameDto
import com.example.gamelib.data.repository.GameRepositoryImpl
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus
import com.example.gamelib.domain.repository.ImageStorageRepository
import com.example.gamelib.domain.usecase.AddGameUseCase
import com.example.gamelib.domain.usecase.DeleteGameUseCase
import com.example.gamelib.domain.usecase.GetCatalogGamesUseCase
import com.example.gamelib.domain.usecase.GetGamesUseCase
import com.example.gamelib.domain.usecase.UpdateGameUseCase
import com.example.gamelib.domain.usecase.UploadGameImageUseCase
import com.example.gamelib.presentation.screen.GameCatalogScreen
import com.example.gamelib.presentation.screen.GameEditScreen
import com.example.gamelib.presentation.screen.GameListScreen
import com.example.gamelib.presentation.theme.GameLibTheme
import com.example.gamelib.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogLibraryTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun catalogGameCanBeSavedEditedAndReadAfterDatabaseReopen() {

        val context =
            InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("com.example.gamelib.verification", context.packageName)

        val databaseName = "catalog-test-${System.nanoTime()}"

        var database = GameDatabase.open(context, databaseName)

        val store = ViewModelStore()

        try {

            // Подменяем FreeToGame API тестовыми данными
            val api = object : FreeToGameApi {

                override suspend fun getGames(): List<GameDto> {
                    return listOf(
                        GameDto(
                            id = 452,
                            title = "Catalog test game",
                            shortDescription = "Short description",
                            genre = "RPG",
                            platform = "PC",
                            developer = "Studio",
                            thumbnail = "https://example.invalid/cover.jpg"
                        )
                    )
                }
            }

            // Репозиторий использует настоящую тестовую Room
            val repository = GameRepositoryImpl(
                gameDao = database.gameDao(),
                freeToGameApi = api
            )

            // Подменяем Supabase тестовым репозиторием
            val imageRepository = object : ImageStorageRepository {

                override suspend fun uploadImage(
                    bytes: ByteArray,
                    fileName: String,
                    contentType: String
                ): String {
                    return "https://example.com/$fileName"
                }
            }

            lateinit var viewModel: GameViewModel

            // Создаём ViewModel со всеми шестью UseCase
            compose.runOnUiThread {

                val factory = object : ViewModelProvider.Factory {

                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>
                    ): T {

                        return GameViewModel(
                            getGamesUseCase = GetGamesUseCase(repository),
                            getCatalogGamesUseCase =
                                GetCatalogGamesUseCase(repository),
                            addGameUseCase = AddGameUseCase(repository),
                            updateGameUseCase = UpdateGameUseCase(repository),
                            deleteGameUseCase = DeleteGameUseCase(repository),
                            uploadGameImageUseCase =
                                UploadGameImageUseCase(imageRepository)
                        ) as T
                    }
                }

                viewModel = ViewModelProvider(
                    store,
                    factory
                )[GameViewModel::class.java]
            }

            // Переключение между экранами
            var screen by mutableStateOf("catalog")

            var selectedGame by mutableStateOf<Game?>(null)

            compose.setContent {

                GameLibTheme {

                    when (screen) {

                        "catalog" -> {

                            GameCatalogScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    screen = "library"
                                }
                            )
                        }

                        "edit" -> {

                            GameEditScreen(
                                viewModel = viewModel,
                                game = selectedGame,
                                onSaveClick = { game ->
                                    viewModel.updateGame(game)
                                    screen = "library"
                                },
                                onCancelClick = {
                                    screen = "library"
                                }
                            )
                        }

                        else -> {

                            GameListScreen(
                                viewModel = viewModel,
                                onAddClick = {},
                                onEditClick = { game ->
                                    selectedGame = game
                                    screen = "edit"
                                },
                                onCatalogClick = {
                                    screen = "catalog"
                                }
                            )
                        }
                    }
                }
            }

            // 1. Ожидаем загрузку каталога
            compose.waitUntil(10_000) {
                viewModel.uiState.value.catalogGames.isNotEmpty()
            }

            // 2. Добавляем игру из каталога
            compose.onNodeWithText("Добавить в библиотеку")
                .performScrollTo()
                .performClick()

            compose.waitUntil(10_000) {
                viewModel.uiState.value.games.size == 1
            }

            // 3. Проверяем сохранённую игру
            val saved = viewModel.uiState.value.games.single()

            assertTrue(saved.id > 0)

            assertEquals(
                452,
                saved.remoteId
            )

            assertEquals(
                "https://example.invalid/cover.jpg",
                saved.thumbnail
            )

            assertEquals(
                GameStatus.WANT_TO_PLAY,
                saved.status
            )

            // 4. Переходим в раздел "Мои игры"
            compose.onNodeWithText("Мои игры")
                .performClick()

            compose.onNodeWithText("Catalog test game")
                .performScrollTo()
                .assertIsDisplayed()

            // 5. Открываем редактирование
            compose.onNodeWithText("Редактировать")
                .performScrollTo()
                .performClick()

            // 6. Меняем статус игры
            compose.onNodeWithText("Статус: Хочу поиграть")
                .performScrollTo()
                .performClick()

            compose.onNodeWithText("Играю")
                .performClick()

            // 7. Сохраняем изменения
            compose.onNodeWithText("Сохранить")
                .performScrollTo()
                .performClick()

            compose.waitUntil(10_000) {
                viewModel.uiState.value.games.single().status ==
                    GameStatus.PLAYING
            }

            // 8. Закрываем ViewModel и базу
            compose.runOnUiThread {
                store.clear()
            }

            database.close()

            // 9. Повторно открываем базу
            database = GameDatabase.open(
                context,
                databaseName
            )

            // 10. Проверяем, сохранились ли изменения
            val persisted = runBlocking {

                GameRepositoryImpl(
                    gameDao = database.gameDao(),
                    freeToGameApi = api
                ).getAllGames().first().single()
            }

            assertEquals(
                saved.copy(status = GameStatus.PLAYING),
                persisted
            )

        } finally {

            // Очищаем ресурсы после теста
            compose.runOnUiThread {
                store.clear()
            }

            database.close()

            context.deleteDatabase(databaseName)
        }
    }
}
