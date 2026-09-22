package com.example.gamelib.data.local.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.gamelib.data.local.dao.GameDao
import com.example.gamelib.data.local.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 1
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
}