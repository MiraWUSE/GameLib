package com.example.gamelib.data.mapper

import com.example.gamelib.data.local.entity.GameEntity
import com.example.gamelib.data.remote.dto.GameDto
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus

fun GameEntity.toDomain(): Game {
    return Game(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        genre = genre,
        platform = platform,
        developer = developer,
        status = GameStatus.valueOf(status),
        thumbnail = thumbnail
    )
}

fun Game.toEntity(): GameEntity {
    return GameEntity(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        genre = genre,
        platform = platform,
        developer = developer,
        status = status.name,
        thumbnail = thumbnail
    )
}

fun GameDto.toDomain(): Game {
    return Game(
        id = 0,
        remoteId = id,
        title = title,
        description = shortDescription,
        genre = genre,
        platform = platform,
        developer = developer,
        status = GameStatus.WANT_TO_PLAY,
        thumbnail = thumbnail
    )
}
