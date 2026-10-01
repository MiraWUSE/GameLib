package com.example.gamelib.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamelib.presentation.component.GameCover
import com.example.gamelib.domain.model.Game
import com.example.gamelib.presentation.util.toDisplayName
import com.example.gamelib.presentation.viewmodel.GameViewModel

@Composable
fun GameCatalogScreen(
    viewModel: GameViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.catalogGames.isEmpty()) {
            viewModel.loadCatalogGames()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Мои игры")
        }

        when {

            uiState.isCatalogLoading -> {

                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }

            uiState.catalogErrorMessage != null -> {

                Text(
                    text = uiState.catalogErrorMessage ?: "",
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = {
                        viewModel.loadCatalogGames()
                    }
                ) {
                    Text("Повторить")
                }
            }

            else -> {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(uiState.catalogGames) { game ->

                        CatalogGameItem(
                            game = game,
                            onAddClick = { viewModel.addGame(game) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogGameItem(
    game: Game,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            GameCover(thumbnail = game.thumbnail, title = game.title)

            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = game.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Жанр: ${game.genre}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Платформа: ${game.platform}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Разработчик: ${game.developer}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Статус: ${game.status.toDisplayName()}",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить в библиотеку")
            }
        }
    }
}
