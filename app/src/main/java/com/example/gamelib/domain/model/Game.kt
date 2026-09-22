package com.example.gamelib.domain.model

data class Game(
    val id: Int = 0,
    val title: String,
    val description: String,
    val genre: String,
    val platform: String,
    val developer: String,
    val status: GameStatus = GameStatus.WANT_TO_PLAY
)


enum class GameStatus {
    WANT_TO_PLAY,
    PLAYING,
    COMPLETED,
    DROPPED
}
