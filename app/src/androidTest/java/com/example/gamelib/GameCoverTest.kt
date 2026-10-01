package com.example.gamelib

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamelib.presentation.component.GameCover
import com.example.gamelib.presentation.theme.GameLibTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class GameCoverTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun imageIsDecodedAndDisplayed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File.createTempFile("cover-test", ".png", context.cacheDir)
        try {
            val bitmap = Bitmap.createBitmap(16, 9, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.BLUE)
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            compose.setContent { GameLibTheme { GameCover(file.toURI().toString(), "Test") } }
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText("Загрузка обложки…").fetchSemanticsNodes().isEmpty()
            }
            compose.onNodeWithContentDescription("Обложка игры Test").assertIsDisplayed()
            org.junit.Assert.assertTrue(
                compose.onAllNodesWithText("Не удалось загрузить обложку").fetchSemanticsNodes().isEmpty()
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun missingThumbnailShowsPlaceholder() {
        compose.setContent { GameLibTheme { GameCover(null, "Test") } }
        compose.onNodeWithText("Нет обложки").assertIsDisplayed()
    }

    @Test
    fun failedImageShowsReadableError() {
        compose.setContent { GameLibTheme { GameCover("file:///missing-cover.png", "Test") } }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Не удалось загрузить обложку").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Не удалось загрузить обложку").assertIsDisplayed()
    }
}
