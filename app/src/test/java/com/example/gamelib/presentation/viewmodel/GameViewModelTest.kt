
package com.example.gamelib.presentation.viewmodel

import com.example.gamelib.MainDispatcherRule
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.repository.GameRepository
import com.example.gamelib.domain.repository.ImageStorageRepository
import com.example.gamelib.domain.usecase.AddGameUseCase
import com.example.gamelib.domain.usecase.DeleteGameUseCase
import com.example.gamelib.domain.usecase.GetCatalogGamesUseCase
import com.example.gamelib.domain.usecase.GetGamesUseCase
import com.example.gamelib.domain.usecase.UpdateGameUseCase
import com.example.gamelib.domain.usecase.UploadGameImageUseCase
import com.example.gamelib.fake.FakeGameRepository
import com.example.gamelib.presentation.state.GameUiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createGame(
        id: Int = 0,
        title: String = "Test Game"
    ): Game {
        return Game(
            id = id,
            title = title,
            description = "Описание",
            genre = "RPG",
            platform = "PC",
            developer = "Test Studio"
        )
    }

    private fun createViewModel(
        repository: GameRepository
    ): GameViewModel {

        val imageRepository = object : ImageStorageRepository {

            override suspend fun uploadImage(
                bytes: ByteArray,
                fileName: String,
                contentType: String
            ): String {
                return "https://example.com/$fileName"
            }
        }

        return GameViewModel(
            getGamesUseCase = GetGamesUseCase(repository),
            getCatalogGamesUseCase = GetCatalogGamesUseCase(repository),
            addGameUseCase = AddGameUseCase(repository),
            updateGameUseCase = UpdateGameUseCase(repository),
            deleteGameUseCase = DeleteGameUseCase(repository),
            uploadGameImageUseCase = UploadGameImageUseCase(
                imageRepository
            )
        )
    }

    @Test
    fun `игры загружаются из репозитория`() = runTest {

        val repository = FakeGameRepository()

        repository.setGames(
            listOf(
                createGame(id = 1, title = "Minecraft"),
                createGame(id = 2, title = "Terraria")
            )
        )

        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.games.size)

        assertEquals(
            "Minecraft",
            viewModel.uiState.value.games[0].title
        )

        assertEquals(
            "Terraria",
            viewModel.uiState.value.games[1].title
        )
    }

    @Test
    fun `игра добавляется в библиотеку`() = runTest {

        val repository = FakeGameRepository()
        val viewModel = createViewModel(repository)

        val events = mutableListOf<GameUiEvent>()

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.events.collect { event ->
                events.add(event)
            }
        }

        viewModel.addGame(
            createGame(title = "Cyberpunk 2077")
        )

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.games.size)

        assertEquals(
            "Cyberpunk 2077",
            viewModel.uiState.value.games.first().title
        )

        assertEquals(
            listOf(GameUiEvent.DataSaved),
            events
        )
    }

    @Test
    fun `игра редактируется`() = runTest {

        val repository = FakeGameRepository()

        repository.setGames(
            listOf(
                createGame(id = 1, title = "Старое название")
            )
        )

        val viewModel = createViewModel(repository)

        val events = mutableListOf<GameUiEvent>()

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.events.collect { event ->
                events.add(event)
            }
        }

        viewModel.updateGame(
            createGame(id = 1, title = "Новое название")
        )

        advanceUntilIdle()

        assertEquals(
            "Новое название",
            viewModel.uiState.value.games.single().title
        )

        assertEquals(
            listOf(GameUiEvent.DataSaved),
            events
        )
    }

    @Test
    fun `игра удаляется из библиотеки`() = runTest {

        val repository = FakeGameRepository()

        val game = createGame(
            id = 1,
            title = "Игра для удаления"
        )

        repository.setGames(listOf(game))

        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.games.size)

        viewModel.deleteGame(game)

        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.games.size)
    }

    @Test
    fun `каталог игр загружается`() = runTest {

        val repository = FakeGameRepository()

        repository.setCatalogGames(
            listOf(
                createGame(title = "Dota 2"),
                createGame(title = "Counter-Strike 2")
            )
        )

        val viewModel = createViewModel(repository)

        viewModel.loadCatalogGames()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(2, state.catalogGames.size)

        assertEquals(
            "Dota 2",
            state.catalogGames.first().title
        )

        assertFalse(state.isCatalogLoading)

        assertNull(state.catalogErrorMessage)
    }

    @Test
    fun `ошибка загрузки каталога обрабатывается`() = runTest {

        mockkStatic(Log::class)

        try {
            every {
                Log.e(any(), any(), any())
            } returns 0

            val repository = FakeGameRepository()

            repository.shouldFailCatalog = true

            val viewModel = createViewModel(repository)

            viewModel.loadCatalogGames()

            advanceUntilIdle()

            val state = viewModel.uiState.value

            assertFalse(state.isCatalogLoading)

            assertNotNull(state.catalogErrorMessage)

            assertTrue(state.catalogGames.isEmpty())

        } finally {
            unmockkStatic(Log::class)
        }
    }
    @Test
    fun `изображение загружается и возвращает ссылку`() = runTest {

        val repository = FakeGameRepository()
        val viewModel = createViewModel(repository)

        var uploadedUrl: String? = null

        viewModel.uploadGameImage(
            bytes = byteArrayOf(1, 2, 3),
            fileName = "cover.jpg",
            contentType = "image/jpeg",
            onSuccess = { url ->
                uploadedUrl = url
            }
        )

        advanceUntilIdle()

        assertEquals(
            "https://example.com/cover.jpg",
            uploadedUrl
        )

        assertFalse(viewModel.uiState.value.isImageUploading)

        assertNull(
            viewModel.uiState.value.imageUploadErrorMessage
        )
    }
}
