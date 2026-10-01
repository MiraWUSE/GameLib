package com.example.gamelib.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun GameCover(thumbnail: String?, title: String) {
    val url = thumbnail?.takeIf { it.isNotBlank() }
    var loaded by remember(url) { mutableStateOf(false) }
    var failed by remember(url) { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = "Обложка игры $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onLoading = { loaded = false; failed = false },
                onSuccess = { loaded = true; failed = false },
                onError = { loaded = false; failed = true }
            )
        }
        if (!loaded) {
            Text(
                text = when {
                    url == null -> "Нет обложки"
                    failed -> "Не удалось загрузить обложку"
                    else -> "Загрузка обложки…"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
