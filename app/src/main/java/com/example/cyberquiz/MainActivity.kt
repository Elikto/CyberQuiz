package com.example.cyberquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import com.example.cyberquiz.ui.screens.QuizScreenV3
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
private fun CyberQuizApp(vm: QuizViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var previousScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var selectedQuizTypeName by rememberSaveable { mutableStateOf(QuizType.CYBERSECURITY.name) }
    val selectedQuizType = QuizType.valueOf(selectedQuizTypeName)

    fun navigateTo(destination: AppScreen) {
        if (destination != screen) {
            previousScreen = screen
            screen = destination
        }
    }

    fun goBack() {
        if (screen != AppScreen.HOME) {
            val destination = previousScreen
            screen = destination
            previousScreen = AppScreen.HOME
        }
    }

    BackHandler(enabled = screen != AppScreen.HOME) {
        goBack()
    }

    when (screen) {
        AppScreen.HOME -> HomeScreenV2(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onQuiz = {
                if (selectedQuizType.available) {
                    vm.start()
                    navigateTo(AppScreen.QUIZ)
                } else {
                    navigateTo(AppScreen.UNAVAILABLE_QUIZ)
                }
            },
            onStats = { navigateTo(AppScreen.STATS) },
            onCategories = {
                if (selectedQuizType.available) {
                    navigateTo(AppScreen.CATEGORIES)
                } else {
                    navigateTo(AppScreen.UNAVAILABLE_QUIZ)
                }
            },
            onProfile = { navigateTo(AppScreen.PROFILE) },
            onSettings = { navigateTo(AppScreen.SETTINGS) }
        )

        AppScreen.QUIZ -> QuizScreenV3(
            vm = vm,
            onBack = { goBack() }
        )

        AppScreen.STATS -> StatisticsScreen(
            vm = vm,
            onBack = { goBack() }
        )

        AppScreen.CATEGORIES -> CategoriesScreen(
            vm = vm,
            onBack = { goBack() },
            onHome = { navigateTo(AppScreen.QUIZ) }
        )

        AppScreen.PROFILE -> ProfileScreen(
            selectedQuizType = selectedQuizType,
            onQuizTypeSelected = { selectedQuizTypeName = it.name },
            onBack = { goBack() }
        )

        AppScreen.SETTINGS -> SettingsScreen(
            onBack = { goBack() }
        )

        AppScreen.UNAVAILABLE_QUIZ -> QuizUnavailableScreen(
            quizType = selectedQuizType,
            onBack = { goBack() }
        )
    }
}
