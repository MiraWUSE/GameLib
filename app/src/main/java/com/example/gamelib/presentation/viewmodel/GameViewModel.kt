package com.example.gamelib.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.usecase.AddGameUseCase
import com.example.gamelib.domain.usecase.DeleteGameUseCase
import com.example.gamelib.domain.usecase.GetGamesUseCase
import com.example.gamelib.domain.usecase.UpdateGameUseCase
import com.example.gamelib.presentation.state.GameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getGamesUseCase: GetGamesUseCase,
    private val addGameUseCase: AddGameUseCase,
    private val updateGameUseCase: UpdateGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

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

    fun addGame(game: Game) {
        viewModelScope.launch {
            addGameUseCase(game)
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch {
            updateGameUseCase(game)
        }
    }

    fun deleteGame(game: Game) {
        viewModelScope.launch {
            deleteGameUseCase(game)
        }
    }
}