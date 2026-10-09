package com.example.gamelib.presentation.screen

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gamelib.domain.model.Game
import com.example.gamelib.domain.model.GameStatus
import com.example.gamelib.presentation.component.GameCover
import com.example.gamelib.presentation.util.toDisplayName
import com.example.gamelib.presentation.viewmodel.GameViewModel
import java.util.UUID

@Composable
fun GameEditScreen(
    viewModel: GameViewModel,
    game: Game? = null,
    onSaveClick: (Game) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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

    var showErrors by remember(game) {
        mutableStateOf(false)
    }

    var selectedImageUri by remember(game) {
        mutableStateOf<Uri?>(null)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    val titleError = showErrors && title.isBlank()
    val descriptionError = showErrors && description.isBlank()
    val genreError = showErrors && genre.isBlank()
    val platformError = showErrors && platform.isBlank()
    val developerError = showErrors && developer.isBlank()

    fun createGame(thumbnailUrl: String?): Game {
        return Game(
            id = game?.id ?: 0,
            remoteId = game?.remoteId,
            thumbnail = thumbnailUrl,
            title = title.trim(),
            description = description.trim(),
            genre = genre.trim(),
            platform = platform.trim(),
            developer = developer.trim(),
            status = status
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        GameCover(
            thumbnail = selectedImageUri?.toString() ?: game?.thumbnail,
            title = title.ifBlank { "Игра" }
        )

        Button(
            onClick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isImageUploading
        ) {
            Text("Выбрать изображение")
        }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            label = {
                Text("Название")
            },
            isError = titleError,
            supportingText = {
                if (titleError) {
                    Text("Обязательное поле")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = {
                Text("Описание")
            },
            isError = descriptionError,
            supportingText = {
                if (descriptionError) {
                    Text("Обязательное поле")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = genre,
            onValueChange = {
                genre = it
            },
            label = {
                Text("Жанр")
            },
            isError = genreError,
            supportingText = {
                if (genreError) {
                    Text("Обязательное поле")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = platform,
            onValueChange = {
                platform = it
            },
            label = {
                Text("Платформа")
            },
            isError = platformError,
            supportingText = {
                if (platformError) {
                    Text("Обязательное поле")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = developer,
            onValueChange = {
                developer = it
            },
            label = {
                Text("Разработчик")
            },
            isError = developerError,
            supportingText = {
                if (developerError) {
                    Text("Обязательное поле")
                }
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

        if (uiState.isImageUploading) {
            CircularProgressIndicator()

            Text("Загрузка изображения...")
        }

        if (uiState.imageUploadErrorMessage != null) {
            Text(
                text = uiState.imageUploadErrorMessage ?: ""
            )
        }

        Button(
            onClick = {

                showErrors = true

                val isValid =
                    title.isNotBlank() &&
                            description.isNotBlank() &&
                            genre.isNotBlank() &&
                            platform.isNotBlank() &&
                            developer.isNotBlank()

                if (isValid) {

                    val imageUri = selectedImageUri

                    if (imageUri == null) {

                        // Новую картинку не выбирали.
                        // Оставляем старый URL.
                        onSaveClick(
                            createGame(game?.thumbnail)
                        )

                    } else {

                        val contentResolver = context.contentResolver

                        val bytes = contentResolver
                            .openInputStream(imageUri)
                            ?.use { inputStream ->
                                inputStream.readBytes()
                            }

                        if (bytes != null) {

                            val contentType =
                                contentResolver.getType(imageUri)
                                    ?: "image/jpeg"

                            val extension =
                                MimeTypeMap
                                    .getSingleton()
                                    .getExtensionFromMimeType(contentType)
                                    ?: "jpg"

                            val fileName =
                                "${UUID.randomUUID()}.$extension"

                            viewModel.uploadGameImage(
                                bytes = bytes,
                                fileName = fileName,
                                contentType = contentType,
                                onSuccess = { imageUrl ->

                                    onSaveClick(
                                        createGame(imageUrl)
                                    )
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isImageUploading
        ) {

            Text(
                if (uiState.isImageUploading) {
                    "Загрузка..."
                } else {
                    "Сохранить"
                }
            )
        }
    }
}