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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.viewmodel.QuizViewModel

private val SecondaryPurple = Color(0xFFD652FF)
private val SecondaryBlue = Color(0xFF19BFFF)
private val SecondaryCyan = Color(0xFF19F2E5)
private val SecondaryGreen = Color(0xFF38E69A)
private val SecondaryOrange = Color(0xFFFFB84A)
private val SecondaryText = Color(0xFFF5F7FF)
private val SecondaryMuted = Color(0xFF9FAED3)
private val SecondaryPanel = Color(0xFF081226)
private val SecondaryBorder = Color(0xFF244777)

private fun secondaryBackground() = Brush.verticalGradient(
    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
)

@Composable
fun StatisticsScreenV2(vm: QuizViewModel, onBack: () -> Unit) {
    val p by vm.progress.collectAsState()
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered
    val averageMs = if (p.answered == 0) 0L else p.totalResponseMs / p.answered

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryBackground())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PolishedHeader(
            title = "Statistiques",
            subtitle = "TA PROGRESSION",
            onBack = onBack
        )

        StatisticsHero(
            accuracy = accuracy,
            level = p.level,
            xp = p.xp
        )

        Text(
            "VUE D'ENSEMBLE",
            color = SecondaryBlue,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryStatCard("✓", p.correct.toString(), "Bonnes réponses", SecondaryGreen, Modifier.weight(1f))
            SecondaryStatCard("?", p.answered.toString(), "Questions répondues", SecondaryBlue, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryStatCard("🔥", p.streak.toString(), "Série actuelle", SecondaryOrange, Modifier.weight(1f))
            SecondaryStatCard("★", p.bestStreak.toString(), "Meilleure série", SecondaryPurple, Modifier.weight(1f))
        }

        ProgressSummaryCard(
            accuracy = accuracy,
            correct = p.correct,
            answered = p.answered
        )

        if (averageMs > 0L) {
            InfoStrip(
                symbol = "⏱",
                title = "Temps moyen",
                value = "${averageMs / 1000.0} s"
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatisticsHero(accuracy: Int, level: Int, xp: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF12133A), Color(0xFF07172F), Color(0xFF071024))),
                RoundedCornerShape(24.dp)
            )
            .border(1.2.dp, Color(0xFF3B6FD1), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(108.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 10f
                drawArc(
                    color = Color(0xFF20375D),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                if (accuracy > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(SecondaryPurple, SecondaryBlue, SecondaryCyan)),
                        startAngle = -90f,
                        sweepAngle = 360f * accuracy / 100f,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = Size(size.width - stroke * 2, size.height - stroke * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$accuracy%", color = SecondaryText, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("RÉUSSITE", color = SecondaryMuted, fontSize = 8.sp, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text("NIVEAU $level", color = SecondaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
            Text("Progression globale", color = SecondaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("$xp XP accumulés", color = SecondaryMuted, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            UnifiedSecondaryBar((xp % 100) / 100f)
            Spacer(Modifier.height(5.dp))
            Text("${xp % 100} / 100 XP vers le prochain niveau", color = Color(0xFFB9C5E8), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SecondaryStatCard(symbol: String, value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(112.dp)
            .background(Color(0xFF08152B), RoundedCornerShape(19.dp))
            .border(1.dp, SecondaryBorder, RoundedCornerShape(19.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(value, color = SecondaryText, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text(label, color = SecondaryMuted, fontSize = 11.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun ProgressSummaryCard(accuracy: Int, correct: Int, answered: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(20.dp))
            .border(1.dp, SecondaryBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Précision", color = SecondaryText, fontWeight = FontWeight.Bold)
            Text("$accuracy%", color = SecondaryCyan, fontWeight = FontWeight.Black)
        }
        UnifiedSecondaryBar(accuracy / 100f)
        Text("$correct bonnes réponses sur $answered questions", color = SecondaryMuted, fontSize = 12.sp)
    }
}

@Composable
fun CategoriesScreenV2(vm: QuizViewModel, onBack: () -> Unit, onQuiz: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryBackground())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        PolishedHeader(
            title = "Catégories",
            subtitle = "CHOISIS TON DOMAINE",
            onBack = onBack
        )
        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF17133D), Color(0xFF07162D))),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, SecondaryPurple.copy(alpha = .65f), RoundedCornerShape(20.dp))
                .padding(15.dp)
        ) {
            Text("ENTRAÎNEMENT CIBLÉ", color = SecondaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(4.dp))
            Text("Choisis une spécialité", color = SecondaryText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Le quiz utilisera uniquement les questions de ce domaine.", color = SecondaryMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }

        Spacer(Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            items(Category.entries) { category ->
                CategoryCardV2(category) {
                    vm.start(category)
                    onQuiz()
                }
            }
        }

        AdaptiveQuizButton {
            vm.start()
            onQuiz()
        }
    }
}

@Composable
private fun CategoryCardV2(category: Category, onClick: () -> Unit) {
    val accent = categoryAccent(category)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = .12f), SecondaryPanel)),
                RoundedCornerShape(19.dp)
            )
            .border(1.dp, accent.copy(alpha = .55f), RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(categorySymbol(category), color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Text("›", color = accent, fontSize = 24.sp)
        }
        Text(category.label, color = SecondaryText, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
    }
}

private fun categoryAccent(category: Category): Color = when (category.ordinal % 4) {
    0 -> SecondaryPurple
    1 -> SecondaryBlue
    2 -> SecondaryCyan
    else -> Color(0xFF8B7CFF)
}

private fun categorySymbol(category: Category): String = when (category) {
    Category.RESEAUX -> "⌁"
    Category.LINUX -> ">_"
    Category.WINDOWS -> "▦"
    Category.CRYPTO -> "◇"
    Category.WEB -> "◎"
    Category.MALWARE -> "!"
    Category.SOCIAL -> "◌"
    Category.OSINT -> "⌖"
    Category.FORENSICS -> "⌕"
    Category.PENTEST -> "⚡"
    Category.AD -> "AD"
    Category.CLOUD -> "☁"
    Category.MOBILE -> "▯"
    Category.SYSTEM -> "⚙"
}

@Composable
private fun AdaptiveQuizButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF6B1AA1), Color(0xFF163A7C))),
                RoundedCornerShape(18.dp)
            )
            .border(1.4.dp, SecondaryPurple, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✦", color = SecondaryCyan, fontSize = 18.sp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("Quiz adaptatif", color = SecondaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Mélange toutes les catégories", color = Color(0xFFC3CCEA), fontSize = 11.sp)
        }
        Text("›", color = SecondaryText, fontSize = 27.sp)
    }
}

@Composable
fun ProfileScreenV2(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryBackground())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PolishedHeader(
            title = "Profil",
            subtitle = "TON UNIVERS",
            onBack = onBack
        )

        ProfileHero(selectedQuizType)

        Text("TYPE DE QUIZ", color = SecondaryBlue, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)

        QuizType.entries.forEach { type ->
            QuizUniverseCardV2(
                type = type,
                selected = type == selectedQuizType,
                onClick = { onQuizTypeSelected(type) }
            )
        }

        val availableColor = if (selectedQuizType.available) SecondaryGreen else SecondaryOrange
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(availableColor.copy(alpha = .08f), RoundedCornerShape(17.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (selectedQuizType.available) "✓" else "◷", color = availableColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Text(
                if (selectedQuizType.available) {
                    "${selectedQuizType.label} est prêt à jouer."
                } else {
                    "${selectedQuizType.label} est sélectionné · questions bientôt disponibles."
                },
                color = Color(0xFFD4DCF5),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ProfileHero(selectedQuizType: QuizType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF15143D), Color(0xFF07172F))),
                RoundedCornerShape(23.dp)
            )
            .border(1.2.dp, Color(0xFF416EC2), RoundedCornerShape(23.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val p = Path().apply {
                    moveTo(size.width * .5f, 3f)
                    lineTo(size.width * .88f, size.height * .23f)
                    lineTo(size.width * .82f, size.height * .68f)
                    quadraticBezierTo(size.width * .70f, size.height * .88f, size.width * .5f, size.height * .97f)
                    quadraticBezierTo(size.width * .30f, size.height * .88f, size.width * .18f, size.height * .68f)
                    lineTo(size.width * .12f, size.height * .23f)
                    close()
                }
                drawPath(p, brush = Brush.linearGradient(listOf(SecondaryPurple, SecondaryBlue)), style = Stroke(5f))
            }
            Text("CQ", color = SecondaryText, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text("Mon profil", color = SecondaryText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Univers actif", color = SecondaryMuted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(selectedQuizType.label, color = SecondaryCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuizUniverseCardV2(type: QuizType, selected: Boolean, onClick: () -> Unit) {
    val accent = if (selected) SecondaryPurple else SecondaryBlue
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(if (selected) Color(0xFF24143F) else Color(0xFF09152B), Color(0xFF071024))
                ),
                RoundedCornerShape(18.dp)
            )
            .border(if (selected) 1.5.dp else 1.dp, if (selected) SecondaryPurple else SecondaryBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(43.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(type.symbol, color = accent, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(type.label, color = SecondaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (!type.available) {
                    Spacer(Modifier.width(7.dp))
                    Text("BIENTÔT", color = SecondaryOrange, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(type.description, color = SecondaryMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Text(if (selected) "✓" else "›", color = if (selected) SecondaryGreen else SecondaryMuted, fontSize = 21.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsScreenV2(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryBackground())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PolishedHeader(
            title = "Paramètres",
            subtitle = "CYBERQUIZ",
            onBack = onBack
        )

        SettingsAppCard()

        SectionLabel("APPLICATION")
        PolishedSettingsItem("⌘", "Projet GitHub", "Elikto / CyberQuiz", "›", SecondaryBlue) {
            uriHandler.openUri("https://github.com/Elikto/CyberQuiz")
        }

        SectionLabel("AIDE & SOUTIEN")
        PolishedSettingsItem("?", "FAQ", "Questions fréquentes et aide", "BIENTÔT", SecondaryCyan)
        PolishedSettingsItem("✉", "Nous contacter", "Signaler un problème ou proposer une idée", "BIENTÔT", SecondaryPurple)
        PolishedSettingsItem("♥", "Soutenir CyberQuiz", "Un lien de don sera ajouté plus tard", "BIENTÔT", Color(0xFFFF678A))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF071225), RoundedCornerShape(18.dp))
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("UN MONDE PLUS SÛR COMMENCE PAR TOI", color = Color(0xFFD0D9F4), fontSize = 9.sp, letterSpacing = 1.8.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(5.dp))
            Text("CyberQuiz évoluera avec de nouveaux univers, questions et fonctionnalités.", color = SecondaryMuted, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun SettingsAppCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF17143D), Color(0xFF07172F))),
                RoundedCornerShape(22.dp)
            )
            .border(1.2.dp, Color(0xFF416EC2), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(56.dp).background(SecondaryPurple.copy(alpha = .15f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("CQ", color = SecondaryPurple, fontSize = 19.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = SecondaryText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = SecondaryPurple, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Text("Version ${BuildConfig.VERSION_NAME}", color = SecondaryMuted, fontSize = 11.sp)
        }
        Box(
            Modifier.background(Color(0xFF0A1730), RoundedCornerShape(50.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text("#${BuildConfig.VERSION_CODE}", color = SecondaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PolishedSettingsItem(
    symbol: String,
    title: String,
    subtitle: String,
    trailing: String,
    accent: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(1.dp, SecondaryBorder, RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = SecondaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SecondaryMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Text(
            trailing,
            color = if (trailing == "BIENTÔT") SecondaryOrange else accent,
            fontSize = if (trailing == "BIENTÔT") 8.sp else 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PolishedHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = SecondaryText, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = SecondaryText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = SecondaryMuted, fontSize = 9.sp, letterSpacing = 1.7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = SecondaryBlue, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun UnifiedSecondaryBar(progress: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(9.dp)
            .background(Color(0xFF20365E), RoundedCornerShape(50.dp))
    ) {
        if (progress > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(
                        Brush.horizontalGradient(listOf(SecondaryPurple, SecondaryBlue, SecondaryCyan)),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
private fun InfoStrip(symbol: String, title: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF071225), RoundedCornerShape(16.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, color = SecondaryBlue, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(title, color = SecondaryMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = SecondaryText, fontWeight = FontWeight.Bold)
    }
}
