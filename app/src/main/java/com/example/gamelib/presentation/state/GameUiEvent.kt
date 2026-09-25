package com.example.gamelib.presentation.state

sealed interface GameUiEvent {

    data object DataSaved : GameUiEvent
}