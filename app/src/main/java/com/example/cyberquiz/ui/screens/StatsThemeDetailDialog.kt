package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cyberquiz.data.database.CategoryProgressEntity
import com.example.cyberquiz.data.database.ConceptProgressEntity

private val ThemeDetailPurple = Color(0xFFD652FF)
private val ThemeDetailBlue = Color(0xFF19BFFF)
private val ThemeDetailCyan = Color(0xFF19F2E5)
private val ThemeDetailGreen = Color(0xFF38E69A)
private val ThemeDetailOrange = Color(0xFFFFB84A)
private val ThemeDetailRed = Color(0xFFFF657F)
private val ThemeDetailText = Color(0xFFF5F7FF)
private val ThemeDetailMuted = Color(0xFF9FAED3)

@Composable
internal fun StatsThemeDetailDialog(
    item: CategoryProgressEntity,
    concepts: List<ConceptProgressEntity>,
    onQuiz: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val accuracy = if (item.answered == 0) 0 else item.correct * 100 / item.answered
    val profile = remember(item.answered, accuracy) { themeLearningProfile(item.answered, accuracy) }
    val recommendedCourse = remember(item.category, concepts) {
        recommendedThemeCourse(item.category, concepts)
    }
    var courseTerm by remember(item.category) { mutableStateOf<String?>(null) }
    var showQuizOptions by remember(item.category) { mutableStateOf(false) }
    var selectedQuestionCount by remember(item.category) { mutableStateOf(10) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 740.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF11163A), Color(0xFF071327), Color(0xFF040914))
                    ),
                    RoundedCornerShape(28.dp)
                )
                .border(1.4.dp, profile.accent.copy(alpha = .75f), RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .background(profile.accent.copy(alpha = .13f), CircleShape)
                        .border(1.dp, profile.accent.copy(alpha = .45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("%", color = profile.accent, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.category, color = ThemeDetailText, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text("COMPRENDRE TON NIVEAU", color = ThemeDetailMuted, fontSize = 8.sp, letterSpacing = 1.4.sp)
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .background(Color(0xFF121C36), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = ThemeDetailMuted, fontSize = 23.sp)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                ThemeMetric("RÉUSSITE", "$accuracy%", profile.accent, Modifier.weight(1f))
                ThemeMetric("JUSTES", "${item.correct}/${item.answered}", ThemeDetailGreen, Modifier.weight(1f))
                ThemeMetric("DONNÉES", dataConfidenceLabel(item.answered), ThemeDetailBlue, Modifier.weight(1f))
            }

            ThemeSection("TON NIVEAU ACTUEL", profile.accent)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(profile.accent.copy(alpha = .07f), RoundedCornerShape(17.dp))
                    .border(1.dp, profile.accent.copy(alpha = .28f), RoundedCornerShape(17.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(profile.title, color = profile.accent, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(profile.meaning, color = Color(0xFFD5DDF3), fontSize = 12.sp, lineHeight = 18.sp)
            }

            ThemeSection("COMMENT LIRE CE POURCENTAGE", ThemeDetailCyan)
            Text(
                confidenceExplanation(item.answered),
                color = Color(0xFFC9D4EE),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeDetailBlue.copy(alpha = .055f), RoundedCornerShape(16.dp))
                    .padding(13.dp)
            )

            ThemeSection("PROCHAINE ÉTAPE CONSEILLÉE", ThemeDetailPurple)
            Text(
                profile.nextStep,
                color = Color(0xFFD5DDF3),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            ThemeActionButton(
                title = "LANCER UN QUIZ CIBLÉ",
                subtitle = if (showQuizOptions) {
                    "Choisis le nombre de questions juste en dessous"
                } else {
                    "Choisir le nombre de questions · ${item.category}"
                },
                accent = ThemeDetailPurple,
                onClick = { showQuizOptions = true }
            )

            if (showQuizOptions) {
                ThemeSection("NOMBRE DE QUESTIONS", ThemeDetailCyan)
                ThemeQuestionCountSelector(
                    selected = selectedQuestionCount,
                    onSelected = { selectedQuestionCount = it }
                )
                ThemeLaunchButton(
                    selectedQuestionCount = selectedQuestionCount,
                    onClick = { onQuiz(selectedQuestionCount) }
                )
            }

            ThemeActionButton(
                title = "OUVRIR LE COURS CONSEILLÉ",
                subtitle = recommendedCourse,
                accent = ThemeDetailCyan,
                onClick = { courseTerm = recommendedCourse }
            )

            Text(
                if (profile.preferCourse) {
                    "Conseil : commence par le cours, puis relance un quiz ciblé pour vérifier ce que tu as retenu."
                } else {
                    "Conseil : commence par le quiz ciblé. Si une notion bloque encore, le cours conseillé te permettra de la reprendre depuis la base."
                },
                color = ThemeDetailMuted,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    courseTerm?.let { term ->
        CyberCourseDialog(
            term = term,
            category = item.category,
            onDismiss = { courseTerm = null },
            dismissLabel = "Retour au menu du thème"
        )
    }
}

@Composable
private fun ThemeQuestionCountSelector(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val options = listOf(10, 20, 50, 100, 200, 0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEach { rowOptions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { count ->
                    val active = count == selected
                    val label = if (count == 0) "INFINI" else count.toString()
                    Box(
                        Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(
                                if (active) ThemeDetailPurple.copy(alpha = .17f) else Color(0xFF08152B),
                                RoundedCornerShape(13.dp)
                            )
                            .border(
                                if (active) 1.4.dp else 1.dp,
                                if (active) ThemeDetailPurple else Color(0xFF2A4774),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { onSelected(count) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (active) ThemeDetailText else ThemeDetailMuted,
                            fontSize = if (count == 0) 9.sp else 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeLaunchButton(
    selectedQuestionCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(ThemeDetailPurple.copy(alpha = .95f), Color(0xFF234A91))
                ),
                RoundedCornerShape(16.dp)
            )
            .border(1.4.dp, ThemeDetailPurple, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (selectedQuestionCount == 0) "LANCER · INFINI" else "LANCER · $selectedQuestionCount QUESTIONS",
            color = ThemeDetailText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = .7.sp
        )
    }
}

private data class ThemeLearningProfile(
    val title: String,
    val meaning: String,
    val nextStep: String,
    val accent: Color,
    val preferCourse: Boolean
)

private fun themeLearningProfile(answered: Int, accuracy: Int): ThemeLearningProfile = when {
    answered < 3 -> ThemeLearningProfile(
        title = "Premières données",
        meaning = "Tu as encore trop peu de réponses dans ce thème pour considérer le pourcentage comme un niveau stable. Une ou deux questions peuvent faire varier fortement le résultat.",
        nextStep = "Fais quelques questions supplémentaires sur ce thème. L'objectif est d'obtenir assez de données avant d'interpréter précisément ton niveau.",
        accent = ThemeDetailBlue,
        preferCourse = false
    )
    accuracy >= 85 -> ThemeLearningProfile(
        title = "Très bon niveau",
        meaning = "Tu réponds correctement à la grande majorité des questions de ce thème. Tes repères sont solides, même si quelques notions peuvent encore mériter une vérification.",
        nextStep = "Lance un quiz ciblé pour confirmer cette maîtrise sur de nouvelles questions. Le cours est surtout utile pour approfondir une notion précise.",
        accent = ThemeDetailGreen,
        preferCourse = false
    )
    accuracy >= 70 -> ThemeLearningProfile(
        title = "En bonne progression",
        meaning = "Tu comprends déjà une grande partie du thème, mais certaines confusions reviennent encore. Le socle est présent sans être totalement stabilisé.",
        nextStep = "Un quiz ciblé est idéal pour identifier les dernières zones fragiles. Utilise ensuite le cours conseillé si une notion reste difficile.",
        accent = ThemeDetailCyan,
        preferCourse = false
    )
    accuracy >= 50 -> ThemeLearningProfile(
        title = "À consolider",
        meaning = "Tu reconnais plusieurs notions du thème, mais le résultat montre encore des hésitations importantes. Les connaissances sont partielles et doivent être reliées entre elles.",
        nextStep = "Reprends le cours conseillé, puis fais un quiz ciblé pour vérifier immédiatement si les distinctions sont plus claires.",
        accent = ThemeDetailOrange,
        preferCourse = true
    )
    else -> ThemeLearningProfile(
        title = "À retravailler",
        meaning = "Les bases de ce thème ne sont pas encore assez stables. Ce score ne signifie pas que tu ne peux pas progresser : il indique surtout où concentrer l'apprentissage maintenant.",
        nextStep = "Commence par le cours conseillé pour reconstruire les bases, puis utilise un quiz ciblé comme exercice de validation.",
        accent = ThemeDetailRed,
        preferCourse = true
    )
}

private fun dataConfidenceLabel(answered: Int): String = when {
    answered < 3 -> "FAIBLE"
    answered < 8 -> "MOYENNE"
    else -> "BONNE"
}

private fun confidenceExplanation(answered: Int): String = when {
    answered < 3 -> "Seulement $answered réponse${if (answered > 1) "s" else ""} enregistrée${if (answered > 1) "s" else ""}. Le pourcentage est une première indication, pas encore une mesure fiable de ton niveau."
    answered < 8 -> "$answered réponses sont déjà utiles pour voir une tendance, mais quelques nouvelles questions peuvent encore faire bouger sensiblement le pourcentage."
    else -> "$answered réponses donnent une tendance plus représentative. Le pourcentage reste un indicateur pédagogique : regarde aussi les notions que tu rates ou que tu maîtrises après révision."
}

private fun recommendedThemeCourse(
    category: String,
    concepts: List<ConceptProgressEntity>
): String {
    val learnedCandidate = concepts
        .asSequence()
        .filter { it.category.equals(category, ignoreCase = true) }
        .filter { hasCyberExpertCourse(it.concept) }
        .sortedWith(
            compareBy<ConceptProgressEntity> { it.reviewMastered }
                .thenBy { it.lastResultCorrect }
                .thenBy { if (it.attempts == 0) 0 else it.correct * 100 / it.attempts }
                .thenByDescending { it.attempts }
        )
        .map { it.concept }
        .firstOrNull()

    return learnedCandidate ?: when (category.lowercase()) {
        "réseaux" -> "DNS"
        "linux" -> "PWD"
        "windows" -> "BitLocker"
        "cryptographie" -> "AES"
        "sécurité web" -> "XSS"
        "malware" -> "Ransomware"
        "ingénierie sociale" -> "Phishing"
        "osint" -> "OSINT"
        "forensics" -> "SHA-256"
        "pentest" -> "Nmap"
        "active directory" -> "Kerberos"
        "cloud security" -> "MFA"
        "mobile security" -> "MDM"
        "sécurité système" -> "Pare-feu"
        else -> "MFA"
    }
}

@Composable
private fun ThemeMetric(label: String, value: String, accent: Color, modifier: Modifier) {
    Column(
        modifier
            .background(Color(0xFF07152C), RoundedCornerShape(15.dp))
            .border(1.dp, accent.copy(alpha = .30f), RoundedCornerShape(15.dp))
            .padding(horizontal = 5.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = ThemeDetailMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun ThemeSection(text: String, accent: Color) {
    Text(text, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
}

@Composable
private fun ThemeActionButton(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(accent.copy(alpha = .12f), Color(0xFF071226))),
                RoundedCornerShape(17.dp)
            )
            .border(1.2.dp, accent.copy(alpha = .70f), RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).background(accent.copy(alpha = .12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("›", color = accent, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = ThemeDetailText, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = ThemeDetailMuted, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}