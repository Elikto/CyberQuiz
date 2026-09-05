package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.cyberquiz.R
import com.example.cyberquiz.ui.theme.*
import com.example.cyberquiz.viewmodel.QuizViewModel
import kotlin.math.*

@Composable
fun HomeScreenV2(
    vm: QuizViewModel,
    selectedQuizType: QuizType,
    onQuiz: () -> Unit,
    onStats: () -> Unit,
    onCategories: () -> Unit,
    onReview: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit
) {
    val p by vm.progress.collectAsState()
    val reviewItems by vm.reviewItems.collectAsState()
    val history by vm.quizHistory.collectAsState()
    val activeReviewCount = reviewItems.count { !it.mastered }
    val xp = p.xp % 100
    val progress = (xp / 100f).coerceIn(0f, 1f)
    val accuracy = if (p.answered == 0) 0 else p.correct * 100 / p.answered
    val title = when {
        p.level >= 15 -> "Architecte Sécurité"
        p.level >= 10 -> "Hacker Éthique"
        p.level >= 5 -> "Analyste SOC"
        else -> "Débutant Cyber"
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF020611))
    ) {
        HomeCircuitBackground()

        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TopButton(TopIcon.SETTINGS, onSettings)
                TopButton(TopIcon.PROFILE, onProfile)
            }

            Spacer(Modifier.height(4.dp))
            HeroLogo()

            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = Color.White, fontSize = 41.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = Color(0xFFE04FFF), fontSize = 41.sp, fontWeight = FontWeight.Black)
            }
            Text("APPRENDS  ·  JOUE  ·  SÉCURISE", color = Color(0xFFAEB9EA), fontSize = 11.sp, letterSpacing = 2.1.sp)

            Spacer(Modifier.height(8.dp))
            Box(Modifier.background(Color(0xFF0D1730), RoundedCornerShape(50.dp)).border(1.dp, CyberBlue.copy(.55f), RoundedCornerShape(50.dp)).padding(horizontal = 13.dp, vertical = 6.dp)) {
                Text("QUIZ : ${selectedQuizType.label.uppercase()}", color = Color(0xFFBFD7FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }

            Spacer(Modifier.height(16.dp))
            LevelCard(p.level, title, xp, progress)

            Spacer(Modifier.height(14.dp))
            Text("« La cybersécurité d'aujourd'hui\nconstruit un meilleur demain »", color = Color(0xFFC9D0F3), textAlign = TextAlign.Center, fontSize = 15.sp, lineHeight = 22.sp)

            Spacer(Modifier.height(18.dp))
            MenuCard("Commencer", "Lancer un nouveau quiz", Color(0xFFD652FF), Color(0xFF381160), ActionIcon.PLAY, onQuiz)
            Spacer(Modifier.height(10.dp))
            MenuCard("Statistiques", "Suis ta progression", Color(0xFF18BFFF), Color(0xFF072B5B), ActionIcon.BARS, onStats)
            Spacer(Modifier.height(10.dp))
            MenuCard("Catégories", "Choisis ton thème", Color(0xFF19F2E5), Color(0xFF06393B), ActionIcon.GRID, onCategories)
            Spacer(Modifier.height(10.dp))
            MenuCard(
                "À revoir",
                if (activeReviewCount == 0) "Aucune notion en attente" else "$activeReviewCount notion${if (activeReviewCount > 1) "s" else ""} à retravailler",
                Color(0xFFFFB84A),
                Color(0xFF4A2710),
                ActionIcon.REVIEW,
                onReview
            )
            Spacer(Modifier.height(10.dp))
            MenuCard(
                "Historique",
                if (history.isEmpty()) "Aucun quiz terminé" else "${history.size} quiz terminé${if (history.size > 1) "s" else ""} enregistré${if (history.size > 1) "s" else ""}",
                Color(0xFF8B7CFF),
                Color(0xFF211B50),
                ActionIcon.HISTORY,
                onHistory
            )

            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("🔥", p.streak.toString(), "Série", Modifier.weight(1f), Color(0xFFFFA61A))
                StatCard("★", p.xp.toString(), "XP", Modifier.weight(1f), Color(0xFFD64CFF))
                StatCard("▥", p.level.toString(), "Niveau", Modifier.weight(1f), Color(0xFF1AC3FF))
                StatCard("🏆", "$accuracy%", "Réussite", Modifier.weight(1f), Color(0xFFFFCC33))
            }

            Spacer(Modifier.height(10.dp))
            DigitalPlanet()
        }
    }
}

@Composable
private fun HomeCircuitBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF020611),
                    Color(0xFF040B18),
                    Color(0xFF050A16),
                    Color(0xFF020610)
                )
            )
        )

        val cyan = Color(0xFF18CFFF).copy(alpha = .12f)
        val purple = Color(0xFF9B5CFF).copy(alpha = .10f)
        val dim = Color(0xFF4074B8).copy(alpha = .07f)
        val w = size.width
        val h = size.height

        var x = 24f
        while (x < w) {
            drawLine(dim, Offset(x, 0f), Offset(x, h), 1f)
            x += 58f
        }
        var y = 20f
        while (y < h) {
            drawLine(dim, Offset(0f, y), Offset(w, y), 1f)
            y += 58f
        }

        val leftTraces = listOf(
            Triple(.08f, .10f, .28f),
            Triple(.03f, .22f, .42f),
            Triple(.10f, .36f, .24f),
            Triple(.02f, .50f, .34f),
            Triple(.09f, .66f, .26f),
            Triple(.04f, .82f, .39f)
        )
        leftTraces.forEachIndexed { index, (startX, yFraction, reach) ->
            val c = if (index % 2 == 0) cyan else purple
            val sy = h * yFraction
            val sx = w * startX
            val ex = w * reach
            val p = Path().apply {
                moveTo(sx, sy)
                lineTo(ex - 34f, sy)
                lineTo(ex, sy + if (index % 2 == 0) 28f else -28f)
                lineTo(ex + 48f, sy + if (index % 2 == 0) 28f else -28f)
            }
            drawPath(p, c, style = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(c.copy(alpha = .7f), 4f, Offset(sx, sy))
            drawCircle(c.copy(alpha = .55f), 3f, Offset(ex + 48f, sy + if (index % 2 == 0) 28f else -28f))
        }

        val rightTraces = listOf(.15f, .29f, .44f, .58f, .73f, .90f)
        rightTraces.forEachIndexed { index, yFraction ->
            val c = if (index % 2 == 0) purple else cyan
            val sy = h * yFraction
            val sx = w * .94f
            val ex = w * (if (index % 3 == 0) .66f else .72f)
            val p = Path().apply {
                moveTo(sx, sy)
                lineTo(ex + 38f, sy)
                lineTo(ex, sy + if (index % 2 == 0) -24f else 24f)
                lineTo(ex - 42f, sy + if (index % 2 == 0) -24f else 24f)
            }
            drawPath(p, c, style = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(c.copy(alpha = .7f), 4f, Offset(sx, sy))
        }
    }
}

private enum class TopIcon { SETTINGS, PROFILE }

@Composable
private fun TopButton(icon: TopIcon, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF172A57), Color(0xFF081123))), CircleShape)
            .border(1.3.dp, Color(0xFF7898F2), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(25.dp)) {
            when (icon) {
                TopIcon.SETTINGS -> {
                    val c = Offset(size.width / 2, size.height / 2)
                    repeat(8) { i ->
                        val a = Math.toRadians(i * 45.0)
                        val x = cos(a).toFloat(); val y = sin(a).toFloat()
                        drawLine(Color(0xFFE0E7FF), Offset(c.x + x * size.minDimension * .39f, c.y + y * size.minDimension * .39f), Offset(c.x + x * size.minDimension * .48f, c.y + y * size.minDimension * .48f), 3f, StrokeCap.Round)
                    }
                    drawCircle(Color(0xFFE0E7FF), size.minDimension * .30f, c, style = Stroke(3f))
                    drawCircle(Color(0xFF081123), size.minDimension * .11f, c)
                }
                TopIcon.PROFILE -> {
                    drawCircle(Color(0xFFE0E7FF), size.minDimension * .17f, Offset(size.width / 2, size.height * .31f), style = Stroke(3f))
                    drawArc(Color(0xFFE0E7FF), 198f, 144f, false, Offset(size.width * .17f, size.height * .46f), Size(size.width * .66f, size.height * .48f), style = Stroke(3f, cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Composable
private fun HeroLogo() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(214.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val cyan = Color(0xFF19D9FF).copy(alpha = .42f)
            val purple = Color(0xFF9B5CFF).copy(alpha = .38f)

            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF1A7DFF).copy(alpha = .18f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 180f
                ),
                radius = 180f,
                center = Offset(cx, cy)
            )

            listOf(.20f, .33f, .47f, .62f, .76f).forEachIndexed { i, fraction ->
                val yy = h * fraction
                val c = if (i % 2 == 0) cyan else purple
                val left = Path().apply {
                    moveTo(8f, yy)
                    lineTo(cx - 118f, yy)
                    lineTo(cx - 92f, yy + if (i % 2 == 0) 18f else -18f)
                }
                val right = Path().apply {
                    moveTo(w - 8f, yy)
                    lineTo(cx + 118f, yy)
                    lineTo(cx + 92f, yy + if (i % 2 == 0) 18f else -18f)
                }
                drawPath(left, c, style = Stroke(2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(right, c, style = Stroke(2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawCircle(c, 4f, Offset(8f, yy))
                drawCircle(c, 4f, Offset(w - 8f, yy))
            }
        }

        Box(
            Modifier
                .size(184.dp)
                .background(Color(0xFF020611), RoundedCornerShape(38.dp))
                .border(
                    1.4.dp,
                    Brush.linearGradient(listOf(Color(0xFF9B5CFF), Color(0xFF19D9FF))),
                    RoundedCornerShape(38.dp)
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.cyberquiz_app_icon),
                contentDescription = "Logo CyberQuiz",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun LevelCard(level: Int, title: String, xp: Int, progress: Float) {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFF0B1430), Color(0xFF07152E), Color(0xFF061024))), RoundedCornerShape(22.dp))
            .border(1.35.dp, Color(0xFF2AAEFF), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelBadge(level); Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$xp / 100 XP", color = Color(0xFF21E3FF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp)); MiniBars()
        }
        Spacer(Modifier.height(11.dp)); UnifiedBar(progress)
    }
}

@Composable
private fun UnifiedBar(progress: Float) {
    Box(Modifier.fillMaxWidth().height(10.dp).background(Color(0xFF20365E), RoundedCornerShape(50.dp))) {
        if (progress > 0f) Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(Brush.horizontalGradient(listOf(Color(0xFF7B34FF), Color(0xFFD54EFF))), RoundedCornerShape(50.dp)))
    }
}

@Composable
private fun LevelBadge(level: Int) {
    Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val p = Path().apply { moveTo(w*.5f,h*.06f); lineTo(w*.84f,h*.26f); lineTo(w*.84f,h*.74f); lineTo(w*.5f,h*.94f); lineTo(w*.16f,h*.74f); lineTo(w*.16f,h*.26f); close() }
            drawPath(p, Brush.linearGradient(listOf(Color(0xFFE163FF), Color(0xFF00CFFF))), style = Stroke(5f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("NIV.", color = Color(0xFFE8ECFF), fontSize = 9.sp); Text(level.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun MiniBars() {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(34.dp)) {
        Box(Modifier.width(6.dp).height(13.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
        Box(Modifier.width(6.dp).height(20.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
        Box(Modifier.width(6.dp).height(28.dp).background(Color(0xFF52C8FF), RoundedCornerShape(3.dp)))
    }
}

private enum class ActionIcon { PLAY, BARS, GRID, REVIEW, HISTORY }

@Composable
private fun MenuCard(title: String, subtitle: String, accent: Color, dark: Color, icon: ActionIcon, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(78.dp)
            .background(Brush.horizontalGradient(listOf(dark, accent.copy(.17f), Color(0xFF081327))), RoundedCornerShape(20.dp))
            .border(1.6.dp, accent, RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(62.dp).fillMaxHeight(), contentAlignment = Alignment.Center) { ActionCanvas(icon, accent) }
        Box(Modifier.fillMaxHeight(.66f).width(1.dp).background(accent.copy(.28f)))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Color(0xFFCBD4F8), fontSize = 12.sp) }
        Text("›", color = Color.White, fontSize = 31.sp)
    }
}

@Composable
private fun ActionCanvas(icon: ActionIcon, accent: Color) {
    Canvas(Modifier.size(31.dp)) {
        when (icon) {
            ActionIcon.PLAY -> drawPath(Path().apply { moveTo(size.width*.25f,size.height*.15f); lineTo(size.width*.8f,size.height*.5f); lineTo(size.width*.25f,size.height*.85f); close() }, Color.White)
            ActionIcon.BARS -> { val bw=size.width*.18f; drawRoundRect(accent,Offset(size.width*.08f,size.height*.52f),Size(bw,size.height*.28f)); drawRoundRect(accent,Offset(size.width*.4f,size.height*.35f),Size(bw,size.height*.45f)); drawRoundRect(accent,Offset(size.width*.72f,size.height*.16f),Size(bw,size.height*.64f)) }
            ActionIcon.GRID -> { val c=size.width*.28f; val g=size.width*.12f; listOf(0f,c+g).forEach { x -> listOf(0f,c+g).forEach { y -> drawRoundRect(accent,Offset(x,y),Size(c,c)) } } }
            ActionIcon.REVIEW -> {
                drawCircle(accent, size.minDimension * .36f, Offset(size.width * .5f, size.height * .5f), style = Stroke(2.6f))
                drawLine(accent, Offset(size.width * .5f, size.height * .27f), Offset(size.width * .5f, size.height * .56f), 3.2f, StrokeCap.Round)
                drawCircle(accent, 2.4f, Offset(size.width * .5f, size.height * .68f))
            }
            ActionIcon.HISTORY -> {
                val center = Offset(size.width * .5f, size.height * .5f)
                drawCircle(accent, size.minDimension * .36f, center, style = Stroke(2.6f))
                drawLine(accent, center, Offset(center.x, size.height * .28f), 2.8f, StrokeCap.Round)
                drawLine(accent, center, Offset(size.width * .68f, size.height * .58f), 2.8f, StrokeCap.Round)
                drawCircle(accent, 2.6f, center)
            }
        }
    }
}

@Composable
private fun StatCard(symbol: String, value: String, label: String, modifier: Modifier, accent: Color) {
    Column(
        modifier.height(92.dp).background(Color(0xFF071123).copy(.96f), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFF214B85), RoundedCornerShape(16.dp)).padding(3.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text(symbol, color = accent, fontSize = 18.sp); Spacer(Modifier.height(2.dp)); Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(label, color = Color(0xFFC9D0F3), fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DigitalPlanet() {
    Box(Modifier.fillMaxWidth().height(132.dp), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width; val h=size.height; val center=Offset(w/2,h*1.11f); val rx=w*.54f; val ry=h*.88f
            val main=Color(0xFF21BFFF).copy(.86f); val dim=Color(0xFF315D9B).copy(.68f); val purple=Color(0xFF8F45FF).copy(.55f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF0078FF).copy(.24f),Color.Transparent),Offset(w/2,h*.93f),w*.48f),w*.48f,Offset(w/2,h*.93f))
            drawArc(main,188f,164f,false,Offset(center.x-rx,center.y-ry),Size(rx*2,ry*2),style=Stroke(3.2f,cap=StrokeCap.Round))
            for(i in 1..4){ val ix=rx*(.06f*i); val iy=ry*(.09f*i); drawArc(if(i==2)purple else dim,192f,156f,false,Offset(center.x-rx+ix,center.y-ry+iy),Size((rx-ix)*2,(ry-iy)*2),style=Stroke(if(i==2)1.8f else 1.3f)) }
            for(i in -5..5){ val sx=center.x+i*(w*.075f); val ex=center.x+i*(w*.025f); drawLine(if(i%3==0)purple else dim,Offset(sx,h*.44f+abs(i)*2.8f),Offset(ex,h),1.25f) }
            listOf(Offset(w*.22f,h*.69f),Offset(w*.34f,h*.54f),Offset(w*.5f,h*.48f),Offset(w*.66f,h*.55f),Offset(w*.79f,h*.70f)).forEachIndexed { i,n -> val c=if(i%2==0)main else purple; drawCircle(c,3.8f,n); drawCircle(c.copy(.2f),9f,n) }
        }
        Text("UN MONDE PLUS SÛR\nCOMMENCE PAR TOI", color = Color(0xFFD6DCF8), textAlign = TextAlign.Center, fontSize = 10.sp, letterSpacing = 2.6.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 8.dp))
    }
}