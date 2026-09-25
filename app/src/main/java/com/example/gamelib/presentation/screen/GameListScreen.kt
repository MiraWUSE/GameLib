package com.example.gamelib.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamelib.domain.model.Game
import com.example.gamelib.presentation.viewmodel.GameViewModel
import com.example.gamelib.presentation.util.toDisplayName

@Composable
fun GameListScreen(
    viewModel: GameViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Game) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить игру")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(uiState.games) { game ->

                GameItem(
                    game = game,
                    onEditClick = onEditClick,
                    onDeleteClick = {
                        viewModel.deleteGame(game)
                    }
                )
            }
        }
    }
}

@Composable
private fun GameItem(
    game: Game,
    onEditClick: (Game) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        onEditClick(game)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Редактировать")
                }

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Удалить")
                }
            }
        }
    }
}