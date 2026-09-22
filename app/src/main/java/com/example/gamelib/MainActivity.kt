package com.example.gamelib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.gamelib.presentation.screen.GameListScreen
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

                GameListScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}