package com.example.gamelib.presentation.state

import com.example.gamelib.domain.model.Game

data class GameUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)