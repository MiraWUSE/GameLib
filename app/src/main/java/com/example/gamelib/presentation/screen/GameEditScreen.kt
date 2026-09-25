package com.example.gamelib.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus

@Composable
fun GameEditScreen(
    onSaveClick: (Game) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("") }
    var developer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Добавление игры"
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

        Button(
            onClick = {
                val game = Game(
                    title = title,
                    description = description,
                    genre = genre,
                    platform = platform,
                    developer = developer,
                    status = GameStatus.WANT_TO_PLAY
                )

                onSaveClick(game)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}