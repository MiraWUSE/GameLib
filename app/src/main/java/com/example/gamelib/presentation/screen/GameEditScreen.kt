package com.example.gamelib.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus
import com.example.gamelib.presentation.util.toDisplayName

@Composable
fun GameEditScreen(
    game: Game? = null,
    onSaveClick: (Game) -> Unit
) {
    var title by remember(game) {
        mutableStateOf(game?.title ?: "")
    }

    var description by remember(game) {
        mutableStateOf(game?.description ?: "")
    }

    var genre by remember(game) {
        mutableStateOf(game?.genre ?: "")
    }

    var platform by remember(game) {
        mutableStateOf(game?.platform ?: "")
    }

    var developer by remember(game) {
        mutableStateOf(game?.developer ?: "")
    }

    var status by remember(game) {
        mutableStateOf(
            game?.status ?: GameStatus.WANT_TO_PLAY
        )
    }

    var statusMenuExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = if (game == null) {
                "Добавление игры"
            } else {
                "Редактирование игры"
            }
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text("Название")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = {
                Text("Описание")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = {
                Text("Жанр")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = platform,
            onValueChange = { platform = it },
            label = {
                Text("Платформа")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = developer,
            onValueChange = { developer = it },
            label = {
                Text("Разработчик")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                onClick = {
                    statusMenuExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Статус: ${status.toDisplayName()}"
                )
            }

            DropdownMenu(
                expanded = statusMenuExpanded,
                onDismissRequest = {
                    statusMenuExpanded = false
                }
            ) {

                GameStatus.entries.forEach { gameStatus ->

                    DropdownMenuItem(
                        text = {
                            Text(gameStatus.toDisplayName())
                        },
                        onClick = {
                            status = gameStatus
                            statusMenuExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {

                val savedGame = Game(
                    id = game?.id ?: 0,
                    title = title,
                    description = description,
                    genre = genre,
                    platform = platform,
                    developer = developer,
                    status = status
                )

                onSaveClick(savedGame)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}