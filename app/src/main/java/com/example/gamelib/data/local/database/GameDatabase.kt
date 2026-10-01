package com.example.gamelib.data.local.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.gamelib.data.local.dao.GameDao
import com.example.gamelib.data.local.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 3
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE games ADD COLUMN thumbnail TEXT")
            }
        }
    }
}
