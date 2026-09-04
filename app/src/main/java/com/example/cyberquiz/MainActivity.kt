package com.example.cyberquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyberquiz.ui.screens.CategoriesScreen
import com.example.cyberquiz.ui.screens.HomeScreenV2
import com.example.cyberquiz.ui.screens.ProfileScreen
import com.example.cyberquiz.ui.screens.QuizScreen
import com.example.cyberquiz.ui.screens.QuizType
import com.example.cyberquiz.ui.screens.QuizUnavailableScreen
import com.example.cyberquiz.ui.screens.SettingsScreen
import com.example.cyberquiz.ui.screens.StatisticsScreen
import com.example.cyberquiz.ui.theme.CyberQuizTheme
import com.example.cyberquiz.viewmodel.QuizViewModel

enum class AppScreen {
    HOME,
    QUIZ,
    STATS,
    CATEGORIES,
    PROFILE,
    SETTINGS,
    UNAVAILABLE_QUIZ
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
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var selectedQuizTypeName by rememberSaveable { mutableStateOf(QuizType.CYBERSECURITY.name) }
    val selectedQuizType = QuizType.valueOf(selectedQuizTypeName)

    when (screen) {
        AppScreen.HOME -> HomeScreenV2(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onQuiz = {
                if (selectedQuizType.available) {
                    vm.start()
                    screen = AppScreen.QUIZ
                } else {
                    screen = AppScreen.UNAVAILABLE_QUIZ
                }
            },
            onStats = {
                screen = AppScreen.STATS
            },
            onCategories = {
                if (selectedQuizType.available) {
                    screen = AppScreen.CATEGORIES
                } else {
                    screen = AppScreen.UNAVAILABLE_QUIZ
                }
            },
            onProfile = {
                screen = AppScreen.PROFILE
            },
            onSettings = {
                screen = AppScreen.SETTINGS
            }
        )

        AppScreen.QUIZ -> QuizScreen(
            vm = vm,
            onHome = {
                screen = AppScreen.HOME
            }
        )

        AppScreen.STATS -> StatisticsScreen(
            vm = vm,
            onBack = {
                screen = AppScreen.HOME
            }
        )

        AppScreen.CATEGORIES -> CategoriesScreen(
            vm = vm,
            onBack = {
                screen = AppScreen.HOME
            },
            onHome = {
                screen = AppScreen.QUIZ
            }
        )

        AppScreen.PROFILE -> ProfileScreen(
            selectedQuizType = selectedQuizType,
            onQuizTypeSelected = { selectedQuizTypeName = it.name },
            onBack = { screen = AppScreen.HOME }
        )

        AppScreen.SETTINGS -> SettingsScreen(
            onBack = { screen = AppScreen.HOME }
        )

        AppScreen.UNAVAILABLE_QUIZ -> QuizUnavailableScreen(
            quizType = selectedQuizType,
            onBack = { screen = AppScreen.HOME }
        )
    }
}
