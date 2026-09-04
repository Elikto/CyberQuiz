package com.example.cyberquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyberquiz.ui.screens.*
import com.example.cyberquiz.ui.theme.CyberQuizTheme
import com.example.cyberquiz.viewmodel.QuizViewModel

enum class AppScreen {
    HOME,
    QUIZ,
    STATS,
    CATEGORIES
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CyberQuizTheme {
                CyberQuizApp()
            }
        }
    }
}

@Composable
private fun CyberQuizApp(
    vm: QuizViewModel = viewModel()
) {

    var screen by rememberSaveable {
        mutableStateOf(AppScreen.HOME)
    }

    when (screen) {

        AppScreen.HOME -> HomeScreen(
            vm,
            onQuiz = {
                vm.start()
                screen = AppScreen.QUIZ
            },
            onStats = {
                screen = AppScreen.STATS
            },
            onCategories = {
                screen = AppScreen.CATEGORIES
            }
        )

        AppScreen.QUIZ -> QuizScreen(
            vm,
            onHome = {
                screen = AppScreen.HOME
            }
        )

        AppScreen.STATS -> StatisticsScreen(
            vm,
            onBack = {
                screen = AppScreen.HOME
            }
        )

        AppScreen.CATEGORIES -> CategoriesScreen(
            vm,
            onBack = {
                screen = AppScreen.HOME
            },
            onHome = {
                screen = AppScreen.QUIZ
            }
        )
    }
}