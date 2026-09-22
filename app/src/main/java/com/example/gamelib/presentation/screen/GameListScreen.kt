package com.example.gamelib.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun GameListScreen(
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(uiState.games) { game ->
            GameItem(game = game)
        }
    }
}

@Composable
private fun GameItem(
    game: Game
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = game.genre,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = game.platform,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = game.status.name,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}