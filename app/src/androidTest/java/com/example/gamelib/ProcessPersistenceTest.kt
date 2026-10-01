package com.example.gamelib

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamelib.data.local.database.GameDatabase
import com.example.gamelib.data.mapper.toEntity
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Run with -e persistencePhase seed, force-stop the app, then run with verify.
 * Uses the isolated verification application and the real production database configuration.
 */
@RunWith(AndroidJUnit4::class)
class ProcessPersistenceTest {
    @Test
    fun gamesSurviveProcessRestart() = runBlocking {
        val phase = InstrumentationRegistry.getArguments().getString("persistencePhase")
        assumeTrue("Requires separate seed/verify instrumentation processes", phase != null)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.gamelib.verification", context.packageName)
        val manual = Game(id = 910001, title = "Persistence: manual", description = "Test",
            genre = "RPG", platform = "PC", developer = "Studio", status = GameStatus.PLAYING)
        val catalog = manual.copy(id = 910002, remoteId = 452, title = "Persistence: catalog",
            thumbnail = "https://www.freetogame.com/g/452/thumbnail.jpg")
        val database = GameDatabase.open(context)
        try {
            val dao = database.gameDao()
            if (phase == "seed") {
                // Replace only these explicitly reserved test records, allowing reruns.
                dao.deleteGame(manual.toEntity())
                dao.deleteGame(catalog.toEntity())
                dao.insertGame(manual.toEntity())
                dao.insertGame(catalog.toEntity())
            } else {
                assertEquals("verify", phase)
            }
            val saved = dao.getAllGames().first().associateBy { it.id }
            assertEquals(manual.toEntity(), saved[manual.id])
            assertEquals(catalog.toEntity(), saved[catalog.id])
        } finally {
            database.close()
        }
    }
}
