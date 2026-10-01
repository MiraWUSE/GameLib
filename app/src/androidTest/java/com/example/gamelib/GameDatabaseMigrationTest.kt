package com.example.gamelib

import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamelib.data.local.database.GameDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDatabaseMigrationTest {
    @Test
    fun versionTwoGamesSurviveAddingThumbnail() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-test-${System.nanoTime()}"
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        try {
            SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
                old.execSQL("""CREATE TABLE games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, remoteId INTEGER,
                    title TEXT NOT NULL, description TEXT NOT NULL, genre TEXT NOT NULL,
                    platform TEXT NOT NULL, developer TEXT NOT NULL, status TEXT NOT NULL)
                """)
                old.execSQL("INSERT INTO games VALUES (1, 452, 'Existing game', 'Description', 'RPG', 'PC', 'Studio', 'PLAYING')")
                old.version = 2
            }
            val database = Room.databaseBuilder(context, GameDatabase::class.java, name)
                .addMigrations(GameDatabase.MIGRATION_2_3).build()
            try {
                val game = database.gameDao().getAllGames().first().single()
                assertEquals("Existing game", game.title)
                assertEquals(452, game.remoteId)
                assertEquals("PLAYING", game.status)
                assertNull(game.thumbnail)
                database.gameDao().updateGame(game.copy(thumbnail = "https://example.com/cover.jpg"))
                assertEquals("https://example.com/cover.jpg", database.gameDao().getAllGames().first().single().thumbnail)
            } finally {
                database.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
