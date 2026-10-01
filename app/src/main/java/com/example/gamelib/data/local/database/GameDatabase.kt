package com.example.gamelib.data.local.database

import android.content.Context
import androidx.room3.Room
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
        fun open(context: Context, name: String = "game_database"): GameDatabase =
            Room.databaseBuilder(context, GameDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                // Some early builds already added remoteId without changing version 1.
                val hasRemoteId = connection.prepare("PRAGMA table_info(games)").use { columns ->
                    var found = false
                    while (columns.step()) {
                        if (columns.getText(1) == "remoteId") found = true
                    }
                    found
                }
                if (!hasRemoteId) connection.execSQL("ALTER TABLE games ADD COLUMN remoteId INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE games ADD COLUMN thumbnail TEXT")
            }
        }
    }
}
