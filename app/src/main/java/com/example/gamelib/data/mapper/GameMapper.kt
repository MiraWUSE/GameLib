package com.example.gamelib.data.mapper

import com.example.gamelib.data.local.entity.GameEntity
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus

fun GameEntity.toDomain(): Game {
    return Game(
        id = id,
        title = title,
        description = description,
        genre = genre,
        platform = platform,
        developer = developer,
        status = GameStatus.valueOf(status)
    )
}

fun Game.toEntity(): GameEntity {
    return GameEntity(
        id = id,
        title = title,
        description = description,
        genre = genre,
        platform = platform,
        developer = developer,
        status = status.name
    )
}