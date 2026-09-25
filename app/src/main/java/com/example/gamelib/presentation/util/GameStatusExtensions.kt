package com.example.gamelib.presentation.util

import com.example.gamelib.domain.model.GameStatus

fun GameStatus.toDisplayName(): String {
    return when (this) {
        GameStatus.WANT_TO_PLAY -> "Хочу поиграть"
        GameStatus.PLAYING -> "Играю"
        GameStatus.COMPLETED -> "Пройдено"
        GameStatus.DROPPED -> "Заброшено"

        }

}