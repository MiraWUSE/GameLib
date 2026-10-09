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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamelib.domain.model.Game
import com.example.gamelib.presentation.component.GameCover
import com.example.gamelib.presentation.util.toDisplayName
import com.example.gamelib.presentation.viewmodel.GameViewModel

@Composable
fun GameCatalogScreen(
    viewModel: GameViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedGenre by remember {
        mutableStateOf("Все")
    }

    var selectedPlatform by remember {
        mutableStateOf("Все")
    }

    var genreMenuExpanded by remember {
        mutableStateOf(false)
    }

    var platformMenuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (uiState.catalogGames.isEmpty()) {
            viewModel.loadCatalogGames()
        }
    }

    val genres = remember(uiState.catalogGames) {
        listOf("Все") +
                uiState.catalogGames
                    .map { it.genre }
                    .distinct()
                    .sorted()
    }

    val platforms = remember(uiState.catalogGames) {
        listOf("Все") +
                uiState.catalogGames
                    .map { it.platform }
                    .distinct()
                    .sorted()
    }

    val filteredGames = uiState.catalogGames.filter { game ->

        val titleMatches =
            searchQuery.isBlank() ||
                    game.title.contains(
                        searchQuery,
                        ignoreCase = true
                    )

        val genreMatches =
            selectedGenre == "Все" ||
                    game.genre == selectedGenre

        val platformMatches =
            selectedPlatform == "Все" ||
                    game.platform == selectedPlatform

        titleMatches &&
                genreMatches &&
                platformMatches
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

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    label = {
                        Text("Поиск по названию")
                    },
                    placeholder = {
                        Text("Введите название игры")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Button(
                            onClick = {
                                genreMenuExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Жанр: $selectedGenre")
                        }

                        DropdownMenu(
                            expanded = genreMenuExpanded,
                            onDismissRequest = {
                                genreMenuExpanded = false
                            }
                        ) {

                            genres.forEach { genre ->

                                DropdownMenuItem(
                                    text = {
                                        Text(genre)
                                    },
                                    onClick = {
                                        selectedGenre = genre
                                        genreMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Button(
                            onClick = {
                                platformMenuExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Платформа: $selectedPlatform")
                        }

                        DropdownMenu(
                            expanded = platformMenuExpanded,
                            onDismissRequest = {
                                platformMenuExpanded = false
                            }
                        ) {

                            platforms.forEach { platform ->

                                DropdownMenuItem(
                                    text = {
                                        Text(platform)
                                    },
                                    onClick = {
                                        selectedPlatform = platform
                                        platformMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Найдено игр: ${filteredGames.size}",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (filteredGames.isEmpty()) {

                    Text(
                        text = "Игры не найдены",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(filteredGames) { game ->

                            CatalogGameItem(
                                game = game,
                                onAddClick = {
                                    viewModel.addGame(game)
                                }
                            )
                        }
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

            GameCover(
                thumbnail = game.thumbnail,
                title = game.title
            )

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