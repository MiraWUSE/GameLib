package com.example.gamelib.presentation.state

import com.example.gamelib.domain.model.Game

data class GameUiState(
    val games: List<Game> = emptyList(),

    val catalogGames: List<Game> = emptyList(),

    val isCatalogLoading: Boolean = false,

    val catalogErrorMessage: String? = null,

    val isImageUploading: Boolean = false,

    val imageUploadErrorMessage: String? = null
)