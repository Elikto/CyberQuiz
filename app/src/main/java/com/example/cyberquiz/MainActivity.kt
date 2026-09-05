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
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.ui.screens.CategoriesScreenV3
import com.example.cyberquiz.ui.screens.HomeScreenV2
import com.example.cyberquiz.ui.screens.ProfileScreenV3
import com.example.cyberquiz.ui.screens.QuizHistoryScreen
import com.example.cyberquiz.ui.screens.QuizScreenV8
import com.example.cyberquiz.ui.screens.QuizSetupScreen
import com.example.cyberquiz.ui.screens.QuizType
import com.example.cyberquiz.ui.screens.QuizUnavailableScreen
import com.example.cyberquiz.ui.screens.ReviewScreen
import com.example.cyberquiz.ui.screens.SettingsScreenV2
import com.example.cyberquiz.ui.screens.StatisticsScreenV2
import com.example.cyberquiz.ui.screens.StatisticsScreenV3
import com.example.cyberquiz.ui.screens.UniverseHomeScreen
import com.example.cyberquiz.ui.screens.isPlayableNow
import com.example.cyberquiz.ui.theme.CyberQuizTheme
import com.example.cyberquiz.viewmodel.QuizViewModel

enum class AppScreen {
    HOME,
    QUIZ_SETUP,
    QUIZ,
    STATS,
    CATEGORIES,
    REVIEW,
    HISTORY,
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
    var highlightedReviewConcept by rememberSaveable { mutableStateOf<String?>(null) }
    var configuredQuizUi by rememberSaveable { mutableStateOf(false) }

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

    fun goHome() {
        screen = AppScreen.HOME
        previousScreen = AppScreen.HOME
    }

    fun openQuizSetupFromFinish() {
        configuredQuizUi = false
        previousScreen = AppScreen.HOME
        screen = AppScreen.QUIZ_SETUP
    }

    BackHandler(enabled = screen != AppScreen.HOME) {
        goBack()
    }

    when (screen) {
        AppScreen.HOME -> {
            if (selectedQuizType == QuizType.CYBERSECURITY) {
                HomeScreenV2(
                    vm = vm,
                    selectedQuizType = selectedQuizType,
                    onQuiz = { navigateTo(AppScreen.QUIZ_SETUP) },
                    onStats = { navigateTo(AppScreen.STATS) },
                    onCategories = { navigateTo(AppScreen.CATEGORIES) },
                    onReview = {
                        highlightedReviewConcept = null
                        navigateTo(AppScreen.REVIEW)
                    },
                    onHistory = { navigateTo(AppScreen.HISTORY) },
                    onProfile = { navigateTo(AppScreen.PROFILE) },
                    onSettings = { navigateTo(AppScreen.SETTINGS) }
                )
            } else {
                UniverseHomeScreen(
                    vm = vm,
                    selectedQuizType = selectedQuizType,
                    onQuiz = {
                        if (!selectedQuizType.isPlayableNow()) {
                            navigateTo(AppScreen.UNAVAILABLE_QUIZ)
                        } else {
                            configuredQuizUi = false
                            vm.start(quizType = selectedQuizType.name)
                            navigateTo(AppScreen.QUIZ)
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
                    onReview = {},
                    onProfile = { navigateTo(AppScreen.PROFILE) },
                    onSettings = { navigateTo(AppScreen.SETTINGS) }
                )
            }
        }

        AppScreen.QUIZ_SETUP -> QuizSetupScreen(
            vm = vm,
            onBack = { goBack() },
            onStart = { config ->
                configuredQuizUi = true
                vm.startConfiguredQuiz(config)
                navigateTo(AppScreen.QUIZ)
            },
            onResume = { sessionId ->
                configuredQuizUi = true
                vm.resumeConfiguredQuiz(sessionId)
                navigateTo(AppScreen.QUIZ)
            },
            onAbandon = { sessionId ->
                vm.abandonConfiguredQuiz(sessionId)
            }
        )

        AppScreen.QUIZ -> QuizScreenV8(
            vm = vm,
            configuredSession = configuredQuizUi,
            onBack = { goBack() },
            onOtherQuiz = { openQuizSetupFromFinish() },
            onHome = { goHome() }
        )

        AppScreen.STATS -> {
            if (selectedQuizType == QuizType.CYBERSECURITY) {
                StatisticsScreenV3(
                    vm = vm,
                    onBack = { goBack() },
                    onReviewConcept = { concept ->
                        highlightedReviewConcept = concept
                        navigateTo(AppScreen.REVIEW)
                    },
                    onThemeQuiz = { category, questionCount ->
                        if (vm.activeSessions.value.size < QuizViewModel.MAX_ACTIVE_SESSIONS) {
                            configuredQuizUi = true
                            vm.startConfiguredQuiz(
                                QuizSessionConfig(
                                    mode = QuizSessionMode.RANDOM,
                                    categories = setOf(category),
                                    questionCount = questionCount
                                )
                            )
                            navigateTo(AppScreen.QUIZ)
                        }
                    }
                )
            } else {
                StatisticsScreenV2(
                    vm = vm,
                    onBack = { goBack() }
                )
            }
        }

        AppScreen.CATEGORIES -> CategoriesScreenV3(
            vm = vm,
            selectedQuizType = selectedQuizType,
            onBack = { goBack() },
            onQuiz = {
                configuredQuizUi = false
                navigateTo(AppScreen.QUIZ)
            }
        )

        AppScreen.REVIEW -> ReviewScreen(
            vm = vm,
            highlightedConcept = highlightedReviewConcept,
            onBack = { goBack() },
            onPractice = { item ->
                configuredQuizUi = false
                vm.startReviewQuestion(item.quizType, item.questionId)
                navigateTo(AppScreen.QUIZ)
            }
        )

        AppScreen.HISTORY -> QuizHistoryScreen(
            vm = vm,
            onBack = { goBack() },
            onReplay = { historyId ->
                if (vm.restartHistoryQuiz(historyId)) {
                    configuredQuizUi = true
                    navigateTo(AppScreen.QUIZ)
                }
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
