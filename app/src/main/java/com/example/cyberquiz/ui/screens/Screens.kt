package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.ui.components.AnswerButton
import com.example.cyberquiz.ui.components.NeonCard
import com.example.cyberquiz.ui.components.PrimaryButton
import com.example.cyberquiz.ui.components.SectionTitle
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.ui.theme.CyberBlue
import com.example.cyberquiz.ui.theme.CyberGreen
import com.example.cyberquiz.ui.theme.CyberMuted
import com.example.cyberquiz.ui.theme.CyberPurple
import com.example.cyberquiz.ui.theme.CyberRed
import com.example.cyberquiz.ui.theme.CyberSurface
import com.example.cyberquiz.ui.theme.CyberText
import com.example.cyberquiz.viewmodel.QuizUiState
import com.example.cyberquiz.viewmodel.QuizViewModel

@Composable
fun HomeScreen(
    vm: QuizViewModel,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit
) {
    val p by vm.progress.collectAsState()
    val xpTarget = (p.level * 100).coerceAtLeast(100)
    val xpProgress = (p.xp.toFloat() / xpTarget).coerceIn(0f, 1f)
    val accuracy = if (p.answered == 0) 0 else (p.correct * 100 / p.answered)
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
                        Color(0xFF040814),
                        Color(0xFF060B1A),
                        CyberBackground,
                        Color(0xFF040814)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopIconButton("⚙")
            TopIconButton("◯")
        }

        Spacer(Modifier.height(8.dp))
        HeroShieldSection()
        Spacer(Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Cyber",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Quiz",
                color = Color(0xFFE14EFF),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(
            text = "APPRENDS  ·  JOUE  ·  SÉCURISE",
            color = Color(0xFFAFB8E8),
            fontSize = 12.sp,
            letterSpacing = 2.2.sp
        )

        Spacer(Modifier.height(22.dp))

        LevelProgressCard(
            level = p.level,
            levelTitle = titleLevel,
            currentXp = p.xp,
            targetXp = xpTarget,
            progress = xpProgress
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "« La cybersécurité d'aujourd'hui\nconstruit un meilleur demain »",
            color = Color(0xFFC9D0F3),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(22.dp))

        MenuActionCard(
            title = "Commencer",
            subtitle = "Lancer un nouveau quiz",
            accent = Color(0xFFD652FF),
            accentDark = Color(0xFF381160),
            iconKind = HomeIconKind.Play,
            onClick = onQuiz
        )

        Spacer(Modifier.height(12.dp))

        MenuActionCard(
            title = "Statistiques",
            subtitle = "Suis ta progression",
            accent = Color(0xFF18BFFF),
            accentDark = Color(0xFF072B5B),
            iconKind = HomeIconKind.Bars,
            onClick = onStats
        )

        Spacer(Modifier.height(12.dp))

        MenuActionCard(
            title = "Catégories",
            subtitle = "Choisis ton thème",
            accent = Color(0xFF19F2E5),
            accentDark = Color(0xFF06393B),
            iconKind = HomeIconKind.Grid,
            onClick = onCategories
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMiniCard("🔥", p.streak.toString(), "Série en cours", Modifier.weight(1f), Color(0xFFFFA61A))
            StatMiniCard("★", p.xp.toString(), "Points totaux", Modifier.weight(1f), Color(0xFFD64CFF))
            StatMiniCard("▥", p.level.toString(), "Niveau actuel", Modifier.weight(1f), Color(0xFF1AC3FF))
            StatMiniCard("🏆", "$accuracy%", "Réussite", Modifier.weight(1f), Color(0xFFFFCC33))
        }

        Spacer(Modifier.height(20.dp))
        GlobeFooter()
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun TopIconButton(symbol: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color(0xFF0B132A).copy(alpha = 0.9f), CircleShape)
            .border(1.dp, Color(0xFF8FA6FF).copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color(0xFFDCE3FF), fontSize = 24.sp)
    }
}

@Composable
private fun HeroShieldSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2.2f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B2DFF).copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = 140f
                ),
                radius = 140f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00C8FF).copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = 180f
                ),
                radius = 180f,
                center = Offset(centerX, centerY)
            )

            val circuitYs = listOf(h * 0.38f, h * 0.50f, h * 0.63f)
            circuitYs.forEachIndexed { index, y ->
                val offset = index * 18f
                drawLine(
                    color = if (index % 2 == 0) Color(0xFF11C7FF) else Color(0xFFB546FF),
                    start = Offset(centerX - 95f, y),
                    end = Offset(40f + offset, y),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = if (index % 2 == 0) Color(0xFF11C7FF) else Color(0xFFB546FF),
                    start = Offset(centerX + 95f, y),
                    end = Offset(w - 40f - offset, y),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = if (index % 2 == 0) Color(0xFF11C7FF) else Color(0xFFB546FF),
                    radius = 5f,
                    center = Offset(40f + offset, y)
                )
                drawCircle(
                    color = if (index % 2 == 0) Color(0xFF11C7FF) else Color(0xFFB546FF),
                    radius = 5f,
                    center = Offset(w - 40f - offset, y)
                )
            }

            val outer = Path().apply {
                moveTo(centerX, 14f)
                lineTo(centerX + 78f, 52f)
                lineTo(centerX + 70f, 118f)
                quadraticBezierTo(centerX + 58f, 150f, centerX, 175f)
                quadraticBezierTo(centerX - 58f, 150f, centerX - 70f, 118f)
                lineTo(centerX - 78f, 52f)
                close()
            }
            val inner = Path().apply {
                moveTo(centerX, 28f)
                lineTo(centerX + 58f, 58f)
                lineTo(centerX + 51f, 108f)
                quadraticBezierTo(centerX + 42f, 132f, centerX, 149f)
                quadraticBezierTo(centerX - 42f, 132f, centerX - 51f, 108f)
                lineTo(centerX - 58f, 58f)
                close()
            }

            drawPath(
                path = outer,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE866FF), Color(0xFF37CFFF), Color(0xFF00B5FF))
                ),
                style = Stroke(width = 7f)
            )
            drawPath(
                path = inner,
                color = Color(0xFF56C8FF),
                style = Stroke(width = 3.5f)
            )

            drawArc(
                color = Color(0xFFD980FF),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - 24f, 56f),
                size = Size(48f, 52f),
                style = Stroke(width = 8f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFFB05CFF), Color(0xFF508DFF))),
                topLeft = Offset(centerX - 30f, 80f),
                size = Size(60f, 58f)
            )
            drawCircle(Color(0xFF0D1732), radius = 10f, center = Offset(centerX, 108f))
            drawLine(
                color = Color(0xFF0D1732),
                start = Offset(centerX, 116f),
                end = Offset(centerX, 128f),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun LevelProgressCard(
    level: Int,
    levelTitle: String,
    currentXp: Int,
    targetXp: Int,
    progress: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0B1430),
                        Color(0xFF07152E),
                        Color(0xFF061024)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .border(1.5.dp, Color(0xFF2AAEFF), RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HexLevelBadge(level = level)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(levelTitle, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(
                "$currentXp / $targetXp XP",
                color = Color(0xFF21E3FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = CyberPurple,
                trackColor = Color(0xFF223A67)
            )
        }
        Spacer(Modifier.width(12.dp))
        BarsMiniIcon()
    }
}

@Composable
private fun HexLevelBadge(level: Int) {
    Box(
        modifier = Modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.84f, h * 0.26f)
                lineTo(w * 0.84f, h * 0.74f)
                lineTo(w * 0.50f, h * 0.94f)
                lineTo(w * 0.16f, h * 0.74f)
                lineTo(w * 0.16f, h * 0.26f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE163FF), Color(0xFF00CFFF))
                ),
                style = Stroke(width = 6f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NIV.", color = Color(0xFFE8ECFF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(level.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BarsMiniIcon() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(42.dp)
    ) {
        Box(Modifier.width(8.dp).height(16.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
        Box(Modifier.width(8.dp).height(24.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
        Box(Modifier.width(8.dp).height(34.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
    }
}

private enum class HomeIconKind { Play, Bars, Grid }

@Composable
private fun MenuActionCard(
    title: String,
    subtitle: String,
    accent: Color,
    accentDark: Color,
    iconKind: HomeIconKind,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accentDark,
                        accent.copy(alpha = 0.18f),
                        Color(0xFF081327)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .border(2.dp, accent, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(76.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            HomeActionIcon(iconKind = iconKind, accent = accent)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(accent.copy(alpha = 0.22f))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFCBD4F8), fontSize = 13.sp)
        }
        Text("›", color = Color.White, fontSize = 36.sp)
    }
}

@Composable
private fun HomeActionIcon(iconKind: HomeIconKind, accent: Color) {
    Canvas(modifier = Modifier.size(34.dp)) {
        when (iconKind) {
            HomeIconKind.Play -> {
                val path = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.15f)
                    lineTo(size.width * 0.80f, size.height * 0.50f)
                    lineTo(size.width * 0.25f, size.height * 0.85f)
                    close()
                }
                drawPath(path, color = Color.White)
            }
            HomeIconKind.Bars -> {
                val barW = size.width * 0.18f
                drawRoundRect(accent, topLeft = Offset(size.width * 0.08f, size.height * 0.52f), size = Size(barW, size.height * 0.28f))
                drawRoundRect(accent, topLeft = Offset(size.width * 0.40f, size.height * 0.35f), size = Size(barW, size.height * 0.45f))
                drawRoundRect(accent, topLeft = Offset(size.width * 0.72f, size.height * 0.16f), size = Size(barW, size.height * 0.64f))
            }
            HomeIconKind.Grid -> {
                val cell = size.width * 0.28f
                val gap = size.width * 0.12f
                val xs = listOf(0f, cell + gap)
                val ys = listOf(0f, cell + gap)
                xs.forEach { x ->
                    ys.forEach { y ->
                        drawRoundRect(accent, topLeft = Offset(x, y), size = Size(cell, cell))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    symbol: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color
) {
    Column(
        modifier = modifier
            .height(108.dp)
            .background(Color(0xFF071123).copy(alpha = 0.96f), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF214B85), RoundedCornerShape(18.dp))
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color(0xFFC9D0F3), fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
    }
}

@Composable
private fun GlobeFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val arcTop = h * 0.35f
            val arcSize = Size(w * 1.1f, h * 1.25f)
            val arcLeft = -w * 0.05f

            for (i in 0..4) {
                drawArc(
                    color = if (i == 0) Color(0xFF2AC6FF) else Color(0xFF315D9B),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(arcLeft, arcTop + i * 10f),
                    size = Size(arcSize.width, arcSize.height - i * 22f),
                    style = Stroke(width = if (i == 0) 3f else 1.5f)
                )
            }

            val centerX = w / 2f
            for (i in -3..3) {
                drawLine(
                    color = Color(0xFF2E5A93),
                    start = Offset(centerX + i * 28f, h * 0.62f),
                    end = Offset(centerX + i * 18f, h * 0.98f),
                    strokeWidth = 1.4f
                )
            }
        }

        Text(
            text = "UN MONDE PLUS SÛR\nCOMMENCE PAR TOI",
            color = Color(0xFFD6DCF8),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            letterSpacing = 3.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
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
            QuizUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                modifier = Modifier.fillMaxWidth(),
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
