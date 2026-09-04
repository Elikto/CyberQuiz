package com.example.cyberquiz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyberquiz.ui.screens.CategoriesScreenV3
import com.example.cyberquiz.ui.screens.ProfileScreenV3
import com.example.cyberquiz.ui.screens.QuizScreenV8
import com.example.cyberquiz.ui.screens.QuizType
import com.example.cyberquiz.ui.screens.QuizUnavailableScreen
import com.example.cyberquiz.ui.screens.ReviewScreen
import com.example.cyberquiz.ui.screens.SettingsScreenV2
import com.example.cyberquiz.ui.screens.StatisticsScreenV2
import com.example.cyberquiz.ui.screens.UniverseHomeScreen
import com.example.cyberquiz.ui.screens.isPlayableNow
import com.example.cyberquiz.ui.theme.CyberQuizTheme
import com.example.cyberquiz.viewmodel.QuizViewModel

enum class AppScreen {
    HOME,
    QUIZ,
    STATS,
    CATEGORIES,
    REVIEW,
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
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("cyberquiz_preferences", Context.MODE_PRIVATE)
    }
    val storedQuizTypeName = remember {
        preferences.getString("selected_quiz_type", QuizType.CYBERSECURITY.name)
            ?: QuizType.CYBERSECURITY.name
    }

    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var previousScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var selectedQuizTypeName by rememberSaveable { mutableStateOf(storedQuizTypeName) }

    val selectedQuizType = QuizType.entries.firstOrNull { it.name == selectedQuizTypeName }
        ?: QuizType.CYBERSECURITY

    LaunchedEffect(selectedQuizType.name) {
        vm.selectQuizType(selectedQuizType.name)
    }

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
        AppScreen.HOME -> UniverseHomeScreen(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onQuiz = {
                if (selectedQuizType.isPlayableNow()) {
                    vm.start(quizType = selectedQuizType.name)
                    navigateTo(AppScreen.QUIZ)
                } else {
                    navigateTo(AppScreen.UNAVAILABLE_QUIZ)
                }
            },
            onStats = { navigateTo(AppScreen.STATS) },
            onCategories = {
                if (selectedQuizType.isPlayableNow()) {
                    navigateTo(AppScreen.CATEGORIES)
                } else {
                    navigateTo(AppScreen.UNAVAILABLE_QUIZ)
                }
            },
            onReview = {
                if (selectedQuizType == QuizType.CYBERSECURITY) {
                    navigateTo(AppScreen.REVIEW)
                }
            },
            onProfile = { navigateTo(AppScreen.PROFILE) },
            onSettings = { navigateTo(AppScreen.SETTINGS) }
        )

        AppScreen.QUIZ -> QuizScreenV8(
            vm = vm,
            onBack = { goBack() }
        )

        AppScreen.STATS -> StatisticsScreenV2(
            vm = vm,
            onBack = { goBack() }
        )

        AppScreen.CATEGORIES -> CategoriesScreenV3(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onBack = { goBack() },
            onQuiz = { navigateTo(AppScreen.QUIZ) }
        )

        AppScreen.REVIEW -> ReviewScreen(
            vm = vm,
            onBack = { goBack() },
            onPractice = { item ->
                vm.startReviewQuestion(item.quizType, item.questionId)
                navigateTo(AppScreen.QUIZ)
            }
        )

        AppScreen.PROFILE -> ProfileScreenV3(
            selectedQuizType = selectedQuizType,
            onQuizTypeSelected = { type ->
                selectedQuizTypeName = type.name
                preferences.edit()
                    .putString("selected_quiz_type", type.name)
                    .apply()
            },
            onBack = { goBack() }
        )

        AppScreen.SETTINGS -> SettingsScreenV2(
            onBack = { goBack() }
        )

        AppScreen.UNAVAILABLE_QUIZ -> QuizUnavailableScreen(
            quizType = selectedQuizType,
            onBack = { goBack() }
        )
    }
}
