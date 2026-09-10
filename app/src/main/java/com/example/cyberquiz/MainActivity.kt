package com.example.cyberquiz

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyberquiz.data.repository.QuestionBankQuizSessionFactory
import com.example.cyberquiz.engagement.DAILY_CHALLENGE_SIZE
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.model.LEARNING_PATH_STEP_QUESTIONS
import com.example.cyberquiz.model.QuizSessionConfig
import com.example.cyberquiz.model.QuizSessionMode
import com.example.cyberquiz.social.SharedQuizViewModel
import com.example.cyberquiz.ui.screens.CategoriesScreenV3
import com.example.cyberquiz.ui.screens.CyberMiniQuizCategoriesScreen
import com.example.cyberquiz.ui.screens.HomeScreenWithQuestionBank
import com.example.cyberquiz.ui.screens.LearningPathsScreen
import com.example.cyberquiz.ui.screens.ProfileScreenEntry
import com.example.cyberquiz.ui.screens.OnboardingScreen
import com.example.cyberquiz.ui.screens.QuestionBankScreen
import com.example.cyberquiz.ui.screens.QuizHistoryScreen
import com.example.cyberquiz.ui.screens.QuizSetupScreenUx
import com.example.cyberquiz.ui.screens.QuizType
import com.example.cyberquiz.ui.screens.QuizUnavailableScreen
import com.example.cyberquiz.ui.screens.ResumableQuizScreen
import com.example.cyberquiz.ui.screens.ReviewScreen
import com.example.cyberquiz.ui.screens.SettingsScreenV4
import com.example.cyberquiz.ui.screens.SharedFriendQuizScreen
import com.example.cyberquiz.ui.screens.SocialHubScreenV3
import com.example.cyberquiz.ui.screens.StatisticsScreenUx
import com.example.cyberquiz.ui.screens.StatisticsScreenV2
import com.example.cyberquiz.ui.screens.UniverseHomeScreen
import com.example.cyberquiz.ui.screens.UpdateHistoryScreen
import com.example.cyberquiz.ui.screens.isPlayableNow
import com.example.cyberquiz.ui.theme.CyberQuizTheme
import com.example.cyberquiz.ui.preferences.UxPreferences
import com.example.cyberquiz.ui.preferences.UxPreferencesStore
import com.example.cyberquiz.viewmodel.QuizViewModel

private const val CYBERQUIZ_PREFERENCES = "cyberquiz_preferences"
private const val KEY_UPDATE_NOTIFICATION_PERMISSION_REQUESTED =
    "update_notification_permission_requested"

enum class AppScreen {
    HOME,
    QUIZ_SETUP,
    QUIZ,
    STATS,
    CATEGORIES,
    REVIEW,
    HISTORY,
    QUESTION_BANK,
    LEARNING_PATHS,
    PROFILE,
    SOCIAL,
    SHARED_QUIZ,
    SETTINGS,
    UPDATE_HISTORY,
    UNAVAILABLE_QUIZ
}

class MainActivity : ComponentActivity() {
    private val updateNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val uxStore = remember(context) { UxPreferencesStore(context) }
            val existingInstall = remember(context) {
                context.getSharedPreferences(CYBERQUIZ_PREFERENCES, Context.MODE_PRIVATE).all.isNotEmpty()
            }
            var uxPreferences by remember { mutableStateOf(uxStore.load(existingInstall = existingInstall)) }
            val updateUxPreferences: (UxPreferences) -> Unit = { next ->
                uxStore.save(next)
                uxPreferences = next
            }
            CyberQuizTheme(preferences = uxPreferences) {
                CyberQuizApp(
                    uxPreferences = uxPreferences,
                    onUxPreferencesChange = updateUxPreferences
                )
            }
        }

        requestUpdateNotificationPermissionIfNeeded()
    }

    private fun requestUpdateNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val preferences = getSharedPreferences(CYBERQUIZ_PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getBoolean(KEY_UPDATE_NOTIFICATION_PERMISSION_REQUESTED, false)) return

        preferences.edit()
            .putBoolean(KEY_UPDATE_NOTIFICATION_PERMISSION_REQUESTED, true)
            .apply()
        updateNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun CyberQuizApp(
    uxPreferences: UxPreferences,
    onUxPreferencesChange: (UxPreferences) -> Unit,
    vm: QuizViewModel = viewModel(),
    sharedQuizVm: SharedQuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(CYBERQUIZ_PREFERENCES, Context.MODE_PRIVATE)
    }
    if (!uxPreferences.onboardingCompleted) {
        OnboardingScreen(
            preferences = uxPreferences,
            onPreferencesChange = onUxPreferencesChange,
            onComplete = {
                onUxPreferencesChange(uxPreferences.copy(onboardingCompleted = true))
            }
        )
        return
    }

    val storedQuizTypeName = remember {
        preferences.getString("selected_quiz_type", QuizType.CYBERSECURITY.name)
            ?: QuizType.CYBERSECURITY.name
    }

    val screenStateHolder = rememberSaveableStateHolder()
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var previousScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var selectedQuizTypeName by rememberSaveable { mutableStateOf(storedQuizTypeName) }
    var highlightedReviewConcept by rememberSaveable { mutableStateOf<String?>(null) }
    var configuredQuizUi by rememberSaveable { mutableStateOf(false) }
    var quizQuestionTotal by rememberSaveable { mutableStateOf<Int?>(null) }

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
        quizQuestionTotal = null
        previousScreen = AppScreen.HOME
        screen = AppScreen.QUIZ_SETUP
    }

    fun startConfigured(config: QuizSessionConfig) {
        if (vm.activeSessions.value.size >= QuizViewModel.MAX_ACTIVE_SESSIONS) return
        configuredQuizUi = true
        quizQuestionTotal = config.questionCount.takeIf { it > 0 }
        vm.startConfiguredQuiz(config)
        navigateTo(AppScreen.QUIZ)
    }

    BackHandler(
        enabled = screen != AppScreen.HOME &&
            screen != AppScreen.QUIZ &&
            screen != AppScreen.SOCIAL &&
            screen != AppScreen.SHARED_QUIZ
    ) {
        goBack()
    }

    screenStateHolder.SaveableStateProvider(screen.name) {
        when (screen) {
            AppScreen.HOME -> {
                if (selectedQuizType == QuizType.CYBERSECURITY) {
                    HomeScreenWithQuestionBank(
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
                        onQuestionBank = { navigateTo(AppScreen.QUESTION_BANK) },
                        onLearningPaths = { navigateTo(AppScreen.LEARNING_PATHS) },
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
                                quizQuestionTotal = null
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

            AppScreen.QUIZ_SETUP -> QuizSetupScreenUx(
                vm = vm,
                onBack = { goBack() },
                onStart = { config -> startConfigured(config) },
                onDailyChallenge = {
                    if (vm.startDailyChallenge()) {
                        configuredQuizUi = true
                        quizQuestionTotal = DAILY_CHALLENGE_SIZE
                        navigateTo(AppScreen.QUIZ)
                    }
                },
                onResume = { sessionId ->
                    configuredQuizUi = true
                    quizQuestionTotal = vm.activeSessions.value
                        .firstOrNull { it.id == sessionId }
                        ?.config
                        ?.questionCount
                        ?.takeIf { it > 0 }
                    vm.resumeConfiguredQuiz(sessionId)
                    navigateTo(AppScreen.QUIZ)
                },
                onAbandon = { sessionId ->
                    vm.abandonConfiguredQuiz(sessionId)
                }
            )

            AppScreen.QUIZ -> ResumableQuizScreen(
                vm = vm,
                configuredSession = configuredQuizUi,
                questionTotal = quizQuestionTotal,
                onBack = { goBack() },
                onOtherQuiz = { openQuizSetupFromFinish() },
                onHome = { goHome() }
            )

            AppScreen.STATS -> {
                if (selectedQuizType == QuizType.CYBERSECURITY) {
                    StatisticsScreenUx(
                        vm = vm,
                        onBack = { goBack() },
                        onReviewConcept = { concept ->
                            highlightedReviewConcept = concept
                            navigateTo(AppScreen.REVIEW)
                        },
                        onThemeQuiz = { category, questionCount ->
                            startConfigured(
                                QuizSessionConfig(
                                    mode = QuizSessionMode.RANDOM,
                                    categories = setOf(category),
                                    questionCount = questionCount
                                )
                            )
                        }
                    )
                } else {
                    StatisticsScreenV2(
                        vm = vm,
                        onBack = { goBack() }
                    )
                }
            }

            AppScreen.CATEGORIES -> {
                if (selectedQuizType == QuizType.CYBERSECURITY) {
                    CyberMiniQuizCategoriesScreen(
                        vm = vm,
                        onBack = { goBack() },
                        onCategoryQuiz = { category, questionCount ->
                            startConfigured(
                                QuizSessionConfig(
                                    mode = QuizSessionMode.RANDOM,
                                    categories = setOf(category),
                                    questionCount = questionCount
                                )
                            )
                        },
                        onAdaptiveQuiz = {
                            configuredQuizUi = false
                            quizQuestionTotal = null
                            vm.start(quizType = selectedQuizType.name)
                            navigateTo(AppScreen.QUIZ)
                        }
                    )
                } else {
                    CategoriesScreenV3(
                        vm = vm,
                        selectedQuizType = selectedQuizType,
                        onBack = { goBack() },
                        onQuiz = {
                            configuredQuizUi = false
                            quizQuestionTotal = null
                            navigateTo(AppScreen.QUIZ)
                        }
                    )
                }
            }

            AppScreen.REVIEW -> ReviewScreen(
                vm = vm,
                highlightedConcept = highlightedReviewConcept,
                onBack = { goBack() },
                onPractice = { item ->
                    configuredQuizUi = false
                    quizQuestionTotal = 1
                    vm.startReviewQuestion(item.quizType, item.questionId)
                    navigateTo(AppScreen.QUIZ)
                },
                onCategoryQuiz = { category, questionCount ->
                    if (questionCount > 0) {
                        startConfigured(
                            QuizSessionConfig(
                                mode = QuizSessionMode.DIFFICULTIES,
                                categories = setOf(category),
                                questionCount = questionCount
                            )
                        )
                    }
                },
                onReviewToday = { questionCount ->
                    if (questionCount > 0) {
                        startConfigured(
                            QuizSessionConfig(
                                mode = QuizSessionMode.DIFFICULTIES,
                                categories = Category.entries.map { it.label }.toSet(),
                                questionCount = questionCount
                            )
                        )
                    }
                }
            )

            AppScreen.HISTORY -> QuizHistoryScreen(
                vm = vm,
                onBack = { goBack() },
                onReplay = { historyId ->
                    val total = vm.quizHistory.value
                        .firstOrNull { it.id == historyId }
                        ?.config
                        ?.questionCount
                        ?.takeIf { it > 0 }
                    if (vm.restartHistoryQuiz(historyId)) {
                        configuredQuizUi = true
                        quizQuestionTotal = total
                        navigateTo(AppScreen.QUIZ)
                    }
                }
            )

            AppScreen.QUESTION_BANK -> QuestionBankScreen(
                vm = vm,
                onStartFavoriteQuiz = { questions ->
                    val selected = QuestionBankQuizSessionFactory.selectQuestions(questions)
                    val sessionId = QuestionBankQuizSessionFactory.create(context, selected)
                    if (sessionId != null) {
                        configuredQuizUi = true
                        quizQuestionTotal = selected.size
                        vm.resumeConfiguredQuiz(sessionId)
                        navigateTo(AppScreen.QUIZ)
                    }
                },
                onBack = { goBack() }
            )

            AppScreen.LEARNING_PATHS -> LearningPathsScreen(
                vm = vm,
                onLaunchCategory = { category ->
                    startConfigured(
                        QuizSessionConfig(
                            mode = QuizSessionMode.RANDOM,
                            categories = setOf(category),
                            questionCount = LEARNING_PATH_STEP_QUESTIONS
                        )
                    )
                },
                onBack = { goBack() }
            )

            AppScreen.PROFILE -> ProfileScreenEntry(
                selectedQuizType = selectedQuizType,
                onQuizTypeSelected = { type ->
                    selectedQuizTypeName = type.name
                    preferences.edit()
                        .putString("selected_quiz_type", type.name)
                        .apply()
                },
                onFriends = { navigateTo(AppScreen.SOCIAL) },
                onBack = { goBack() }
            )

            AppScreen.SOCIAL -> SocialHubScreenV3(
                playerLevel = vm.progress.value.level.coerceAtLeast(1),
                sharedQuizViewModel = sharedQuizVm,
                onSharedQuizStart = { navigateTo(AppScreen.SHARED_QUIZ) },
                onBack = { goBack() }
            )

            AppScreen.SHARED_QUIZ -> SharedFriendQuizScreen(
                viewModel = sharedQuizVm,
                onBackToFriends = {
                    previousScreen = AppScreen.PROFILE
                    screen = AppScreen.SOCIAL
                }
            )

            AppScreen.SETTINGS -> SettingsScreenV4(
                preferences = uxPreferences,
                onPreferencesChange = onUxPreferencesChange,
                onBack = { goBack() },
                onVersionClick = { navigateTo(AppScreen.UPDATE_HISTORY) }
            )

            AppScreen.UPDATE_HISTORY -> UpdateHistoryScreen(
                onBack = { goBack() }
            )

            AppScreen.UNAVAILABLE_QUIZ -> QuizUnavailableScreen(
                quizType = selectedQuizType,
                onBack = { goBack() }
            )
        }
    }
}
