package com.example.gamelib.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.usecase.AddGameUseCase
import com.example.gamelib.domain.usecase.DeleteGameUseCase
import com.example.gamelib.domain.usecase.GetCatalogGamesUseCase
import com.example.gamelib.domain.usecase.GetGamesUseCase
import com.example.gamelib.domain.usecase.UpdateGameUseCase
import com.example.gamelib.presentation.state.GameUiEvent
import com.example.gamelib.presentation.state.GameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import com.example.gamelib.domain.usecase.UploadGameImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getGamesUseCase: GetGamesUseCase,
    private val getCatalogGamesUseCase: GetCatalogGamesUseCase,
    private val addGameUseCase: AddGameUseCase,
    private val updateGameUseCase: UpdateGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val uploadGameImageUseCase: UploadGameImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())

    val uiState: StateFlow<GameUiState> =
        _uiState.asStateFlow()

    private val _events = Channel<GameUiEvent>()

    val events = _events.receiveAsFlow()

    init {
        observeGames()
    }

    private fun observeGames() {
        viewModelScope.launch {

            getGamesUseCase().collect { games ->

                _uiState.value = _uiState.value.copy(
                    games = games
                )
            }
        }
    }

    fun loadCatalogGames() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isCatalogLoading = true,
                catalogErrorMessage = null
            )

            try {
                val games = getCatalogGamesUseCase()

                _uiState.value = _uiState.value.copy(
                    catalogGames = games,
                    isCatalogLoading = false
                )

            } catch (exception: Exception) {

                Log.e(
                    "GameCatalog",
                    "Ошибка загрузки каталога",
                    exception
                )

                _uiState.value = _uiState.value.copy(
                    isCatalogLoading = false,
                    catalogErrorMessage =
                        "${exception.javaClass.simpleName}: ${exception.message}"
                )
            }
        }
    }

    fun uploadGameImage(
        bytes: ByteArray,
        fileName: String,
        contentType: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isImageUploading = true,
                imageUploadErrorMessage = null
            )

            try {

                val imageUrl = uploadGameImageUseCase(
                    bytes = bytes,
                    fileName = fileName,
                    contentType = contentType
                )

                _uiState.value = _uiState.value.copy(
                    isImageUploading = false
                )

                onSuccess(imageUrl)

            } catch (exception: Exception) {

                Log.e(
                    "ImageUpload",
                    "Ошибка загрузки изображения",
                    exception
                )

                _uiState.value = _uiState.value.copy(
                    isImageUploading = false,
                    imageUploadErrorMessage =
                        "Не удалось загрузить изображение"
                )
            }
        }
    }

    fun addGame(game: Game) {
        viewModelScope.launch {

            addGameUseCase(game)

            _events.send(
                GameUiEvent.DataSaved
            )
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch {

            updateGameUseCase(game)

            _events.send(
                GameUiEvent.DataSaved
            )
        }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            deleteGameUseCase(game)
        }
    }
}