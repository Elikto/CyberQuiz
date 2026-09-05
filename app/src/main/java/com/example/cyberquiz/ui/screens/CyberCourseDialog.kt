package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.net.URLEncoder

private val CoursePurple = Color(0xFFD652FF)
private val CourseBlue = Color(0xFF19BFFF)
private val CourseCyan = Color(0xFF19F2E5)
private val CourseGreen = Color(0xFF38E69A)
private val CourseOrange = Color(0xFFFFB84A)
private val CourseText = Color(0xFFF5F7FF)
private val CourseMuted = Color(0xFF9FAED3)

@Composable
internal fun CyberCourseDialog(
    term: String,
    category: String,
    onDismiss: () -> Unit,
    dismissLabel: String = "Retour à l'analyse"
) {
    var activeTerm by remember(term, category) { mutableStateOf(term) }
    var activeCategory by remember(term, category) { mutableStateOf(category) }
    val course = remember(activeTerm, activeCategory) { buildCyberCourse(activeTerm, activeCategory) }
    val extra = remember(activeTerm, activeCategory) { buildCyberCourseExtra(activeTerm, activeCategory) }
    val nextStep = remember(activeTerm, activeCategory) { nextCyberCourseStep(activeTerm, activeCategory) }
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    LaunchedEffect(activeTerm, activeCategory) {
        scrollState.scrollTo(0)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF11163A), Color(0xFF071327), Color(0xFF040914))
                    ),
                    RoundedCornerShape(28.dp)
                )
                .border(1.5.dp, CoursePurple.copy(alpha = .75f), RoundedCornerShape(28.dp))
                .verticalScroll(scrollState)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CourseHeader(course.title, course.subtitle, onDismiss)

            BeginnerMemoryCard(course.memorySentence)

            CourseSectionTitle(course.simpleSchemaTitle)
            CourseFlowSchema(course.simpleSchema, strong = true)

            CourseSectionTitle("CE QUE TU VAS COMPRENDRE")
            CourseBulletCard(course.objectives, CourseCyan)

            CourseSectionTitle("1 · C'EST QUOI ?")
            CourseTextCard(
                title = "Définition simple",
                body = course.deepDive.definition,
                accent = CourseCyan
            )

            CourseSectionTitle("2 · À QUOI ÇA SERT ?")
            CourseTextCard(
                title = "Son rôle",
                body = course.deepDive.action,
                accent = CourseBlue
            )
            CourseBulletCard(course.deepDive.actions, CourseBlue)

            CourseSectionTitle("3 · COMMENT ÇA MARCHE ?")
            CourseTextCard(
                title = "D'abord l'idée générale",
                body = course.deepDive.details,
                accent = CoursePurple
            )

            CourseSectionTitle("SCHÉMA · CE QUI SE PASSE")
            CourseFlowSchema(course.deepDive.schema)

            CourseSectionTitle("4 · ÉTAPE PAR ÉTAPE")
            TextBlock(
                "Ici, on reprend la même notion plus lentement. Chaque ligne correspond à une étape. Tu peux les lire une par une sans avoir besoin de connaître les mots techniques à l'avance."
            )
            CourseNumberedCard(extra.stepByStep, CoursePurple)

            CourseSectionTitle(extra.secondSchemaTitle)
            CourseFlowSchema(extra.secondSchema, strong = true)

            CourseSectionTitle("5 · EXEMPLE DANS LA VRAIE VIE")
            CourseTextCard(
                title = "Situation concrète",
                body = course.deepDive.example,
                accent = CourseGreen
            )

            CourseSectionTitle("UNE IMAGE POUR LE RETENIR")
            CourseTextCard(
                title = "Analogie du quotidien",
                body = course.deepDive.analogy,
                accent = CourseOrange
            )

            CourseSectionTitle("DESSIN MENTAL")
            ConceptMapCourse(activeTerm)

            CourseSectionTitle("6 · VOIR ÇA SUR UN VRAI ORDINATEUR")
            TerminalCourseCard(
                title = course.deepDive.demoTitle,
                command = course.deepDive.demo,
                expectedTitle = course.expectedOutputTitle,
                expected = course.expectedOutput
            )

            CourseSectionTitle("CE QU'ON CONFOND SOUVENT")
            CourseBulletCard(extra.commonMistakes, CourseOrange)

            CourseSectionTitle("POURQUOI C'EST IMPORTANT EN CYBERSÉCURITÉ ?")
            CourseTextCard(
                title = "Le lien avec la sécurité",
                body = extra.securityWhy,
                accent = CourseGreen
            )

            CourseSectionTitle("PETIT LEXIQUE")
            TextBlock(
                "Tu n'as pas besoin d'apprendre ces mots par cœur tout de suite. Le lexique sert simplement à éviter qu'un nouveau terme bloque ta compréhension du cours."
            )
            CourseBulletCard(extra.glossary, CourseCyan)

            CourseSectionTitle("EST-CE QUE TU AS COMPRIS ?")
            CourseBulletCard(course.checkpoints, CourseGreen)

            CourseSectionTitle("POUR ALLER UN PEU PLUS LOIN · OPTIONNEL")
            OptionalAdvancedCard(course.advancedNote)

            CourseSectionTitle("VIDÉOS EN FRANÇAIS")
            if (course.videos.isEmpty()) {
                TextBlock(
                    "Je n'ai pas encore associé une vidéo précise à cette notion. Le bouton ci-dessous lance une recherche YouTube en français avec des mots simples."
                )
            } else {
                course.videos.forEach { video ->
                    CourseVideoCard(
                        title = video.title,
                        channel = video.channel,
                        onClick = { uriHandler.openUri(video.url) }
                    )
                }
            }

            val encoded = remember(course.videoSearchQuery) {
                URLEncoder.encode(course.videoSearchQuery, "UTF-8")
            }
            SearchVideoButton {
                uriHandler.openUri("https://www.youtube.com/results?search_query=$encoded")
            }

            CourseSectionTitle("CONTINUER TON PARCOURS")
            if (nextStep != null) {
                CourseNextButton(nextStep) {
                    activeTerm = nextStep.term
                    activeCategory = nextStep.category
                }
            } else {
                TextBlock(
                    "Tu es arrivé au bout du parcours de cours Cyber actuellement disponible. Tu peux maintenant consolider l'ensemble avec des quiz ciblés."
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF59208E), Color(0xFF173E7A))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, CoursePurple, RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    dismissLabel,
                    color = CourseText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CourseNextButton(step: CyberCourseStep, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(CourseGreen.copy(alpha = .13f), CourseBlue.copy(alpha = .11f), Color(0xFF071226))
                ),
                RoundedCornerShape(19.dp)
            )
            .border(1.3.dp, CourseGreen.copy(alpha = .65f), RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(CourseGreen.copy(alpha = .14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("→", color = CourseGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            androidx.compose.material3.Text(
                "COURS SUIVANT",
                color = CourseGreen,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            androidx.compose.material3.Text(
                step.term,
                color = CourseText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            androidx.compose.material3.Text(
                step.category,
                color = CourseMuted,
                fontSize = 10.sp
            )
        }
        androidx.compose.material3.Text("›", color = CourseCyan, fontSize = 25.sp)
    }
}

@Composable
private fun CourseHeader(title: String, subtitle: String, onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(46.dp)
                .background(CoursePurple.copy(alpha = .16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("C", color = CoursePurple, fontSize = 19.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            androidx.compose.material3.Text(title, color = CourseText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            androidx.compose.material3.Text(subtitle, color = CourseMuted, fontSize = 10.sp, lineHeight = 15.sp)
        }
        Box(
            Modifier
                .size(36.dp)
                .background(Color(0xFF121C36), CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("×", color = CourseMuted, fontSize = 23.sp)
        }
    }
}

@Composable
private fun BeginnerMemoryCard(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(CourseGreen.copy(alpha = .14f), CourseBlue.copy(alpha = .10f))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.2.dp, CourseGreen.copy(alpha = .55f), RoundedCornerShape(20.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        androidx.compose.material3.Text(
            "À RETENIR EN 20 SECONDES",
            color = CourseGreen,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.3.sp
        )
        androidx.compose.material3.Text(
            text,
            color = CourseText,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CourseSectionTitle(text: String) {
    androidx.compose.material3.Text(
        text,
        color = CourseCyan,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp
    )
}

@Composable
private fun CourseTextCard(title: String, body: String, accent: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .34f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        androidx.compose.material3.Text(title, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
        androidx.compose.material3.Text(body, color = Color(0xFFD7E0F5), fontSize = 12.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun CourseBulletCard(items: List<String>, accent: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .28f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(top = 5.dp)
                        .size(7.dp)
                        .background(accent, CircleShape)
                )
                Spacer(Modifier.width(9.dp))
                androidx.compose.material3.Text(
                    item,
                    color = Color(0xFFD5DEF4),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CourseNumberedCard(items: List<String>, accent: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .30f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .size(27.dp)
                        .background(accent.copy(alpha = .13f), CircleShape)
                        .border(1.dp, accent.copy(alpha = .45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        (index + 1).toString(),
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.width(10.dp))
                androidx.compose.material3.Text(
                    item,
                    color = Color(0xFFD5DEF4),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OptionalAdvancedCard(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CourseOrange.copy(alpha = .055f), RoundedCornerShape(18.dp))
            .border(1.dp, CourseOrange.copy(alpha = .24f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        androidx.compose.material3.Text(
            "Tu peux ignorer cette partie au début",
            color = CourseOrange,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
        androidx.compose.material3.Text(
            text,
            color = Color(0xFFD7E0F5),
            fontSize = 11.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TerminalCourseCard(
    title: String,
    command: String,
    expectedTitle: String,
    expected: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.material3.Text(title, color = CourseGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        TerminalBlock(command)
        androidx.compose.material3.Text(expectedTitle, color = CourseBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        TerminalBlock(expected)
    }
}

@Composable
private fun TerminalBlock(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF02060B), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF235E5A), RoundedCornerShape(16.dp))
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(Color(0xFFFF557A), CircleShape))
            Spacer(Modifier.width(5.dp))
            Box(Modifier.size(8.dp).background(Color(0xFFFFB84A), CircleShape))
            Spacer(Modifier.width(5.dp))
            Box(Modifier.size(8.dp).background(CourseGreen, CircleShape))
        }
        Spacer(Modifier.height(9.dp))
        androidx.compose.material3.Text(
            text,
            color = Color(0xFFC9FFE5),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun ConceptMapCourse(term: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF061225), RoundedCornerShape(18.dp))
            .border(1.dp, CoursePurple.copy(alpha = .30f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SmallConceptBox("J'AI UN BESOIN", CourseBlue)
        androidx.compose.material3.Text("↓", color = CourseCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Box(
            Modifier
                .fillMaxWidth()
                .background(CoursePurple.copy(alpha = .15f), RoundedCornerShape(14.dp))
                .border(1.2.dp, CoursePurple, RoundedCornerShape(14.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(term, color = CourseText, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
        androidx.compose.material3.Text("↓", color = CourseCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallConceptBox("IL FAIT SON TRAVAIL", CourseGreen, Modifier.weight(1f))
            SmallConceptBox("J'OBTIENS UN RÉSULTAT", CourseOrange, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SmallConceptBox(text: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(accent.copy(alpha = .09f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(text, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CourseFlowSchema(steps: List<String>, strong: Boolean = false) {
    val border = if (strong) CourseGreen else CourseBlue
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF061225), RoundedCornerShape(18.dp))
            .border(1.dp, border.copy(alpha = if (strong) .50f else .30f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        steps.forEachIndexed { index, step ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            if (strong) {
                                listOf(CourseGreen.copy(alpha = .09f), CourseBlue.copy(alpha = .09f))
                            } else {
                                listOf(CoursePurple.copy(alpha = .10f), CourseBlue.copy(alpha = .10f))
                            }
                        ),
                        RoundedCornerShape(13.dp)
                    )
                    .border(1.dp, border.copy(alpha = .28f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    step,
                    color = CourseText,
                    fontSize = if (strong) 13.sp else 12.sp,
                    fontWeight = if (strong) FontWeight.Bold else FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
            if (index < steps.lastIndex) {
                androidx.compose.material3.Text("↓", color = CourseCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun CourseVideoCard(title: String, channel: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF171229), RoundedCornerShape(17.dp))
            .border(1.dp, CoursePurple.copy(alpha = .35f), RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .background(Color(0xFFFF334D).copy(alpha = .16f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("▶", color = Color(0xFFFF557A), fontSize = 17.sp)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            androidx.compose.material3.Text(title, color = CourseText, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 17.sp)
            androidx.compose.material3.Text("FR · $channel", color = CourseMuted, fontSize = 10.sp)
        }
        androidx.compose.material3.Text("›", color = CourseCyan, fontSize = 22.sp)
    }
}

@Composable
private fun SearchVideoButton(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(CourseBlue.copy(alpha = .08f), RoundedCornerShape(15.dp))
            .border(1.dp, CourseBlue.copy(alpha = .40f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            "Rechercher d'autres vidéos en français",
            color = CourseCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TextBlock(text: String) {
    androidx.compose.material3.Text(
        text,
        color = Color(0xFFD1DBF3),
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF08152B), RoundedCornerShape(16.dp))
            .padding(13.dp)
    )
}
