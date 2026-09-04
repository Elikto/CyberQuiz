package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val titleLevel = when {
        p.level >= 15 -> "Architecte Sécurité"
        p.level >= 10 -> "Hacker Éthique"
        p.level >= 5 -> "Analyste SOC"
        else -> "Débutant Cyber"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020612),
                        Color(0xFF071020),
                        Color(0xFF040814)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundTopIcon("⚙")
            RoundTopIcon("◎")
        }

        Spacer(Modifier.height(6.dp))

        NeonShieldLogo()

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Cyber",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Quiz",
                color = Color(0xFFB64DFF),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            text = "APPRENDS  ·  JOUE  ·  SÉCURISE",
            color = Color(0xFF9EADE3),
            fontSize = 11.sp,
            letterSpacing = 2.2.sp
        )

        Spacer(Modifier.height(20.dp))

        LevelProgressCard(
            level = p.level,
            title = titleLevel,
            xpInLevel = xpInLevel,
            progress = xpProgress
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "« La cybersécurité d’aujourd’hui\nconstruit un meilleur demain »",
            color = Color(0xFFB8C0DF),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 21.sp
        )

        Spacer(Modifier.height(20.dp))

        MockupMenuButton(
            title = "Commencer",
            subtitle = "Lancer un nouveau quiz",
            symbol = "▶",
            borderColor = Color(0xFFE35DFF),
            startColor = Color(0xFF49108E),
            endColor = Color(0xFF180C43),
            onClick = onQuiz
        )

        Spacer(Modifier.height(11.dp))

        MockupMenuButton(
            title = "Statistiques",
            subtitle = "Suis ta progression",
            symbol = "▥",
            borderColor = Color(0xFF1EB5FF),
            startColor = Color(0xFF073B8E),
            endColor = Color(0xFF061B43),
            onClick = onStats
        )

        Spacer(Modifier.height(11.dp))

        MockupMenuButton(
            title = "Catégories",
            subtitle = "Choisis ton thème",
            symbol = "▦",
            borderColor = Color(0xFF20E9E1),
            startColor = Color(0xFF07545C),
            endColor = Color(0xFF06272F),
            onClick = onCategories
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MockupStatCard("🔥", p.streak.toString(), "Série en cours", Modifier.weight(1f))
            MockupStatCard("★", p.xp.toString(), "Points totaux", Modifier.weight(1f))
            MockupStatCard("▥", p.level.toString(), "Niveau actuel", Modifier.weight(1f))
            MockupStatCard("🏆", "$accuracy%", "Réussite", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        CyberGlobeFooter()

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RoundTopIcon(symbol: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color(0xFF0B1327), CircleShape)
            .border(1.dp, Color(0xFF6E83CC).copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color(0xFFC6D3FF), fontSize = 20.sp)
    }
}

@Composable
private fun NeonShieldLogo() {
    Box(
        modifier = Modifier
            .size(width = 176.dp, height = 164.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF7C2CFF).copy(alpha = 0.24f),
                        Color(0xFF00C8FF).copy(alpha = 0.10f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(138.dp)) {
            val w = size.width
            val h = size.height
            val shield = Path().apply {
                moveTo(w * 0.50f, h * 0.05f)
                lineTo(w * 0.87f, h * 0.23f)
                lineTo(w * 0.82f, h * 0.66f)
                cubicTo(w * 0.78f, h * 0.84f, w * 0.62f, h * 0.94f, w * 0.50f, h * 0.99f)
                cubicTo(w * 0.38f, h * 0.94f, w * 0.22f, h * 0.84f, w * 0.18f, h * 0.66f)
                lineTo(w * 0.13f, h * 0.23f)
                close()
            }
            drawPath(
                path = shield,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFEB59FF), Color(0xFF28D5FF))
                ),
                style = Stroke(width = 6f)
            )

            val inner = Path().apply {
                moveTo(w * 0.50f, h * 0.14f)
                lineTo(w * 0.78f, h * 0.28f)
                lineTo(w * 0.74f, h * 0.62f)
                cubicTo(w * 0.70f, h * 0.76f, w * 0.58f, h * 0.85f, w * 0.50f, h * 0.89f)
                cubicTo(w * 0.42f, h * 0.85f, w * 0.30f, h * 0.76f, w * 0.26f, h * 0.62f)
                lineTo(w * 0.22f, h * 0.28f)
                close()
            }
            drawPath(inner, color = Color(0xFF287DFF), style = Stroke(width = 3f))

            drawArc(
                color = Color(0xFFE35DFF),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * 0.37f, h * 0.32f),
                size = Size(w * 0.26f, h * 0.28f),
                style = Stroke(width = 7f)
            )
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFA53DFF), Color(0xFF21C8FF))),
                topLeft = Offset(w * 0.34f, h * 0.47f),
                size = Size(w * 0.32f, h * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
            drawCircle(
                color = Color(0xFF06152B),
                radius = w * 0.035f,
                center = Offset(w * 0.50f, h * 0.59f)
            )
        }
    }
}

@Composable
private fun LevelProgressCard(
    level: Int,
    title: String,
    xpInLevel: Int,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF141440), Color(0xFF071B37))
                ),
                RoundedCornerShape(24.dp)
            )
            .border(1.4.dp, Color(0xFF368BFF), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF5928B9), Color(0xFF163C89))),
                        RoundedCornerShape(24.dp)
                    )
                    .border(1.4.dp, Color(0xFFC05CFF), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NIV.", color = Color(0xFFD8DBFF), fontSize = 11.sp)
                    Text(level.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "$xpInLevel / 100 XP",
                    color = Color(0xFF1CE2FF),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFFA434FF),
                    trackColor = Color(0xFF22345C)
                )
            }

            Spacer(Modifier.width(8.dp))
            Text("▁▃▅", color = Color(0xFF36A1FF), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MockupMenuButton(
    title: String,
    subtitle: String,
    symbol: String,
    borderColor: Color,
    startColor: Color,
    endColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(
                Brush.horizontalGradient(listOf(startColor, endColor)),
                RoundedCornerShape(22.dp)
            )
            .border(1.6.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.White.copy(alpha = 0.055f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = borderColor, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(15.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFCBD4F6), fontSize = 13.sp)
        }

        Text("›", color = Color.White, fontSize = 34.sp)
    }
}

@Composable
private fun MockupStatCard(
    symbol: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(106.dp)
            .background(Color(0xFF081229), RoundedCornerShape(17.dp))
            .border(1.dp, Color(0xFF214894), RoundedCornerShape(17.dp))
            .padding(horizontal = 5.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, fontSize = 18.sp)
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF9EA9CE), fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

@Composable
private fun CyberGlobeFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF006CFF).copy(alpha = 0.25f),
                            Color(0xFF5D24FF).copy(alpha = 0.09f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(Modifier.size(width = 260.dp, height = 105.dp)) {
                val stroke = Stroke(width = 2f)
                val c = Color(0xFF147CFF).copy(alpha = 0.75f)
                drawArc(c, 180f, 180f, false, style = stroke)
                drawArc(c, 195f, 150f, false, topLeft = Offset(size.width * .18f, 7f), size = Size(size.width * .64f, size.height * .86f), style = stroke)
                drawArc(c, 205f, 130f, false, topLeft = Offset(size.width * .34f, 10f), size = Size(size.width * .32f, size.height * .80f), style = stroke)
                repeat(4) { i ->
                    val y = size.height * (0.42f + i * 0.11f)
                    drawLine(c, Offset(size.width * .10f, y), Offset(size.width * .90f, y), strokeWidth = 1.2f)
                }
            }
        }

        Text(
            text = "UN MONDE PLUS SÛR\nCOMMENCE PAR TOI",
            color = Color(0xFFB9C6EE),
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            letterSpacing = 2.2.sp,
            lineHeight = 16.sp
        )
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
