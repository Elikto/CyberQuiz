package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.ui.components.*
import com.example.cyberquiz.ui.theme.*
import com.example.cyberquiz.viewmodel.*

@Composable
fun HomeScreen(
    vm: QuizViewModel,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit
) {
    val p by vm.progress.collectAsState()
    val xpInLevel = p.xp % 100
    val xpProgress = xpInLevel / 100f
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF050714),
                        CyberBackground,
                        Color(0xFF071020)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚙", color = CyberMuted, fontSize = 28.sp)
            Text("◎", color = CyberBlue, fontSize = 30.sp)
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(112.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            CyberPurple.copy(alpha = 0.82f),
                            CyberBlue.copy(alpha = 0.86f),
                            Color(0xFF00D7FF).copy(alpha = 0.72f)
                        )
                    ),
                    RoundedCornerShape(30.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF80E7FF),
                    shape = RoundedCornerShape(30.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CQ",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "CyberQuiz",
            color = CyberText,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "APPRENDS  ·  JOUE  ·  SÉCURISE",
            color = CyberMuted,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            CyberPurple.copy(alpha = 0.13f),
                            CyberBlue.copy(alpha = 0.10f),
                            CyberSurface.copy(alpha = 0.92f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .border(1.dp, CyberBlue.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(CyberPurple.copy(alpha = 0.8f), CyberBlue.copy(alpha = 0.8f))
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .border(1.dp, Color(0xFF8DEBFF), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NIV.", color = CyberText, fontSize = 11.sp)
                        Text(
                            p.level.toString(),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Hacker Éthique",
                        color = CyberText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$xpInLevel / 100 XP",
                        color = Color(0xFF22DFFB),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = CyberPurple,
                        trackColor = CyberSurface2
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "« La cybersécurité d’aujourd’hui\nconstruit un meilleur demain »",
            color = CyberMuted,
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(22.dp))

        HomeMenuButton(
            title = "Commencer",
            subtitle = "Lancer un nouveau quiz",
            symbol = "▶",
            accent = CyberPurple,
            onClick = onQuiz
        )

        Spacer(Modifier.height(12.dp))

        HomeMenuButton(
            title = "Statistiques",
            subtitle = "Suis ta progression",
            symbol = "▥",
            accent = CyberBlue,
            onClick = onStats
        )

        Spacer(Modifier.height(12.dp))

        HomeMenuButton(
            title = "Catégories",
            subtitle = "Choisis ton thème",
            symbol = "▦",
            accent = Color(0xFF16D9DD),
            onClick = onCategories
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeStatCard("🔥", p.streak.toString(), "Série", Modifier.weight(1f))
            HomeStatCard("✦", p.xp.toString(), "XP", Modifier.weight(1f))
            HomeStatCard("▥", p.level.toString(), "Niveau", Modifier.weight(1f))
            HomeStatCard("✓", "$accuracy%", "Réussite", Modifier.weight(1f))
        }

        Spacer(Modifier.height(26.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            CyberBlue.copy(alpha = 0.23f),
                            CyberPurple.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("◎", color = CyberBlue, fontSize = 72.sp)
                Text(
                    "UN MONDE PLUS SÛR\nCOMMENCE PAR TOI",
                    color = CyberMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun HomeMenuButton(
    title: String,
    subtitle: String,
    symbol: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.28f),
                        accent.copy(alpha = 0.10f),
                        CyberSurface.copy(alpha = 0.92f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .border(1.5.dp, accent.copy(alpha = 0.95f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            symbol,
            color = accent,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = CyberText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(subtitle, color = CyberMuted, fontSize = 13.sp)
        }
        Text("›", color = CyberText, fontSize = 34.sp)
    }
}

@Composable
private fun HomeStatCard(
    symbol: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(100.dp)
            .background(CyberSurface.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
            .border(1.dp, CyberBlue.copy(alpha = 0.38f), RoundedCornerShape(18.dp))
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = CyberPurple, fontSize = 18.sp)
        Text(value, color = CyberText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = CyberMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun QuizScreen(vm: QuizViewModel, onHome: () -> Unit) {
    val state by vm.state.collectAsState()
    val result by vm.result.collectAsState()
    var selected by remember { mutableStateOf<Int?>(null) }

    Column(
        Modifier.fillMaxSize().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("QUIZ", color = CyberPurple, fontWeight = FontWeight.Bold)
            TextButton(onClick = onHome) { Text("Quitter") }
        }

        when (state) {
            QuizUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            QuizUiState.Finished -> {
                SectionTitle("Session terminée", "Les questions vont être renouvelées.")
                PrimaryButton("Rejouer", onClick = { vm.start() })
            }

            is QuizUiState.Ready -> {
                val q = (state as QuizUiState.Ready).question
                Text(q.category.uppercase(), color = CyberBlue, fontWeight = FontWeight.Bold)
                Text(q.question, style = MaterialTheme.typography.headlineSmall)
                val answers = listOf(q.answerA, q.answerB, q.answerC, q.answerD)
                answers.forEachIndexed { i, a ->
                    AnswerButton(
                        a,
                        selected == i,
                        if (result == null) null else if (i == q.correctIndex) true else if (selected == i) false else null
                    ) { selected = i }
                }

                if (result == null) {
                    PrimaryButton("Valider", onClick = { selected?.let(vm::answer) })
                } else {
                    NeonCard {
                        Text(
                            if (result!!.correct) "✓ Bonne réponse" else "✕ Mauvaise réponse",
                            color = if (result!!.correct) CyberGreen else CyberRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            result!!.explanation,
                            Modifier.padding(top = 8.dp),
                            color = CyberMuted
                        )
                        Text(
                            "+${result!!.xp} XP",
                            Modifier.padding(top = 8.dp),
                            color = CyberPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    PrimaryButton(
                        "Question suivante",
                        onClick = {
                            selected = null
                            vm.nextQuestion()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsScreen(vm: QuizViewModel, onBack: () -> Unit) {
    val p by vm.progress.collectAsState()
    val rate = if (p.answered == 0) 0 else p.correct * 100 / p.answered

    Column(
        Modifier.fillMaxSize().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Statistiques", style = MaterialTheme.typography.headlineLarge)
        TextButton(onClick = onBack) { Text("← Accueil") }
        NeonCard {
            Text("TA RÉUSSITE", color = CyberMuted)
            Text("$rate%", style = MaterialTheme.typography.displaySmall, color = CyberPurple)
            LinearProgressIndicator(
                progress = { rate / 100f },
                Modifier.fillMaxWidth(),
                color = CyberPurple,
                trackColor = CyberSurface
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox("Répondu", p.answered.toString(), Modifier.weight(1f))
            StatBox("Correct", p.correct.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox("Série", p.streak.toString(), Modifier.weight(1f))
            StatBox("Record", p.bestStreak.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier) {
    NeonCard(modifier) {
        Text(label, color = CyberMuted)
        Text(value, style = MaterialTheme.typography.headlineMedium, color = CyberText)
    }
}

@Composable
fun CategoriesScreen(vm: QuizViewModel, onBack: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        Text("Catégories", style = MaterialTheme.typography.headlineLarge)
        TextButton(onClick = onBack) { Text("← Accueil") }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Category.entries) { c ->
                NeonCard(Modifier.fillMaxWidth().height(105.dp)) {
                    Text(c.label, style = MaterialTheme.typography.titleMedium)
                    Text("Entraînement", color = CyberMuted)
                    Spacer(Modifier.weight(1f))
                    Text("›", color = CyberPurple)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton("Quiz adaptatif", onClick = {
            vm.start()
            onHome()
        })
    }
}
