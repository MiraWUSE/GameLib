package com.example.gamelib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.gamelib.presentation.screen.GameEditScreen
import com.example.gamelib.presentation.screen.GameListScreen
import com.example.gamelib.presentation.state.GameUiEvent
import com.example.gamelib.presentation.theme.GameLibTheme
import com.example.gamelib.presentation.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            GameLibTheme {

                val viewModel: GameViewModel = hiltViewModel()

                var showEditScreen by remember {
                    mutableStateOf(false)
                }

                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                LaunchedEffect(Unit) {

                    viewModel.events.collect { event ->

                        when (event) {

                            GameUiEvent.DataSaved -> {
                                snackbarHostState.showSnackbar(
                                    message = "Данные сохранены"
                                )
                            }
                        }
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    }
                ) { paddingValues ->

                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {

                        if (showEditScreen) {

                            GameEditScreen(
                                onSaveClick = { game ->

                                    viewModel.addGame(game)

                                    showEditScreen = false
                                }
                            )

                        } else {

                            GameListScreen(
                                viewModel = viewModel,
                                onAddClick = {
                                    showEditScreen = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}