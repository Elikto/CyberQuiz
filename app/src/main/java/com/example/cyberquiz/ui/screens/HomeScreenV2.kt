package com.example.cyberquiz.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.cyberquiz.ui.theme.*
import com.example.cyberquiz.update.CyberQuizUpdateManager
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
    val context = LocalContext.current
    val avatarPreferences = remember(context) {
        context.getSharedPreferences("cyberquiz_player_cosmetics", Context.MODE_PRIVATE)
    }
    var selectedAvatarKey by rememberSaveable {
        mutableStateOf(
            avatarPreferences.getString(
                "selected_avatar",
                PlayerAvatarStyle.BEGINNER.storageKey
            ) ?: PlayerAvatarStyle.BEGINNER.storageKey
        )
    }
    var showAvatarPicker by rememberSaveable { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf(false) }
    val selectedAvatar = playerAvatarFromStorage(selectedAvatarKey)
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

    LaunchedEffect(Unit) {
        updateAvailable = CyberQuizUpdateManager.checkForUpdate() != null
    }

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020610), Color(0xFF060B19), CyberBackground, Color(0xFF030712))))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TopButton(TopIcon.SETTINGS, onSettings, showBadge = updateAvailable)
            TopButton(TopIcon.PROFILE, onProfile)
        }

        Spacer(Modifier.height(7.dp))
        PlayerProgressHeader(
            level = p.level,
            title = title,
            xp = xp,
            progress = progress,
            avatarStyle = selectedAvatar,
            onAvatarClick = { showAvatarPicker = true }
        )

        Spacer(Modifier.height(5.dp))
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

    if (showAvatarPicker) {
        PlayerAvatarPickerDialog(
            selected = selectedAvatar,
            onSelect = { style ->
                selectedAvatarKey = style.storageKey
                avatarPreferences.edit()
                    .putString("selected_avatar", style.storageKey)
                    .apply()
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }
}

private enum class TopIcon { SETTINGS, PROFILE }

@Composable
private fun TopButton(icon: TopIcon, onClick: () -> Unit, showBadge: Boolean = false) {
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

        if (showBadge && icon == TopIcon.SETTINGS) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(11.dp)
                    .background(Color(0xFFFF4F6D), CircleShape)
                    .border(1.5.dp, Color(0xFF081123), CircleShape)
            )
        }
    }
}

@Composable
private fun PlayerProgressHeader(
    level: Int,
    title: String,
    xp: Int,
    progress: Float,
    avatarStyle: PlayerAvatarStyle,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(226.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF132946), Color(0xFF0A1730))
                            ),
                            RoundedCornerShape(50.dp)
                        )
                        .border(1.dp, Color(0xFF25DFFF).copy(alpha = .62f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        "NIV. $level",
                        color = Color(0xFF2DE8FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = .7.sp
                    )
                }
                Spacer(Modifier.width(7.dp))
                Text(
                    title,
                    color = Color(0xFFF1F4FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF071323), Color(0xFF132946), Color(0xFF071323))
                        ),
                        RoundedCornerShape(50.dp)
                    )
                    .border(1.dp, Color(0xFF3F69A2).copy(alpha = .78f), RoundedCornerShape(50.dp))
                    .padding(2.dp)
            ) {
                if (progress > 0f) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF6A34FF),
                                        Color(0xFFD54EFF),
                                        Color(0xFF27DFFF),
                                        Color(0xFF72F7FF)
                                    )
                                ),
                                RoundedCornerShape(50.dp)
                            )
                    )
                }
                Canvas(Modifier.matchParentSize()) {
                    for (i in 1..9) {
                        val x = size.width * i / 10f
                        drawLine(
                            Color.White.copy(alpha = .12f),
                            Offset(x, 1f),
                            Offset(x, size.height - 1f),
                            1f
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PROGRESSION DU NIVEAU",
                    color = Color(0xFF7891BE),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = .8.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$xp / 100 XP",
                    color = Color(0xFFCBD7F5),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(Modifier.width(12.dp))
        PlayerAvatarButton(
            style = avatarStyle,
            onClick = onAvatarClick,
            size = 64.dp
        )
    }
}

@Composable
private fun HeroLogo() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(202.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val cyan = Color(0xFF23DFFF)
            val purple = Color(0xFFAA47FF)
            val blue = Color(0xFF347DFF)
            val grid = Color(0xFF2C6EA2).copy(alpha = .12f)

            drawCircle(
                Brush.radialGradient(
                    listOf(purple.copy(alpha = .25f), blue.copy(alpha = .10f), Color.Transparent),
                    Offset(cx, cy),
                    150f
                ),
                150f,
                Offset(cx, cy)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(cyan.copy(alpha = .14f), Color.Transparent),
                    Offset(cx, cy),
                    205f
                ),
                205f,
                Offset(cx, cy)
            )

            for (i in 1..9) {
                val x = w * i / 10f
                drawLine(grid, Offset(x, h * .08f), Offset(x, h * .92f), 1f)
            }
            for (i in 1..6) {
                val y = h * i / 7f
                drawLine(grid, Offset(w * .04f, y), Offset(w * .96f, y), 1f)
            }

            val circuitYs = listOf(.17f, .29f, .41f, .59f, .71f, .83f)
            circuitYs.forEachIndexed { index, yFraction ->
                val y = h * yFraction
                val accent = if (index % 2 == 0) cyan else purple
                val elbowX = w * (.22f + (index % 3) * .035f)
                val innerX = cx - (88f + (index % 2) * 12f)
                val deltaY = if (index < 3) 12f else -12f

                val left = Path().apply {
                    moveTo(w * .035f, y)
                    lineTo(elbowX, y)
                    lineTo(elbowX + 18f, y + deltaY)
                    lineTo(innerX, y + deltaY)
                }
                val right = Path().apply {
                    moveTo(w * .965f, y)
                    lineTo(w - elbowX, y)
                    lineTo(w - elbowX - 18f, y + deltaY)
                    lineTo(cx + (cx - innerX), y + deltaY)
                }
                drawPath(left, accent.copy(alpha = .72f), style = Stroke(2f, cap = StrokeCap.Round))
                drawPath(right, accent.copy(alpha = .72f), style = Stroke(2f, cap = StrokeCap.Round))
                drawCircle(accent, 3.3f, Offset(w * .035f, y))
                drawCircle(accent, 3.3f, Offset(w * .965f, y))
                drawCircle(accent.copy(alpha = .55f), 2.7f, Offset(elbowX, y))
                drawCircle(accent.copy(alpha = .55f), 2.7f, Offset(w - elbowX, y))
            }

            listOf(.18f, .30f, .70f, .82f).forEachIndexed { index, xFraction ->
                val x = w * xFraction
                val accent = if (index % 2 == 0) cyan else purple
                val topY = h * .08f
                val endY = h * .25f
                drawLine(accent.copy(alpha = .45f), Offset(x, topY), Offset(x, endY), 1.7f)
                drawCircle(accent.copy(alpha = .85f), 3.1f, Offset(x, topY))
            }

            val shieldWidth = 148f
            val shieldHeight = 142f
            val top = cy - shieldHeight * .52f
            val outer = Path().apply {
                moveTo(cx, top)
                lineTo(cx + shieldWidth * .48f, top + shieldHeight * .22f)
                lineTo(cx + shieldWidth * .43f, top + shieldHeight * .63f)
                quadraticBezierTo(cx + shieldWidth * .31f, top + shieldHeight * .86f, cx, top + shieldHeight)
                quadraticBezierTo(cx - shieldWidth * .31f, top + shieldHeight * .86f, cx - shieldWidth * .43f, top + shieldHeight * .63f)
                lineTo(cx - shieldWidth * .48f, top + shieldHeight * .22f)
                close()
            }
            val inner = Path().apply {
                moveTo(cx, top + 12f)
                lineTo(cx + shieldWidth * .35f, top + shieldHeight * .28f)
                lineTo(cx + shieldWidth * .31f, top + shieldHeight * .59f)
                quadraticBezierTo(cx + shieldWidth * .22f, top + shieldHeight * .76f, cx, top + shieldHeight * .87f)
                quadraticBezierTo(cx - shieldWidth * .22f, top + shieldHeight * .76f, cx - shieldWidth * .31f, top + shieldHeight * .59f)
                lineTo(cx - shieldWidth * .35f, top + shieldHeight * .28f)
                close()
            }

            drawPath(
                outer,
                Brush.linearGradient(listOf(Color(0xFFF16EFF), Color(0xFF55E7FF), blue)),
                style = Stroke(7f, cap = StrokeCap.Round)
            )
            drawPath(inner, Color(0xFF54B6FF).copy(alpha = .92f), style = Stroke(2.8f))

            drawCircle(
                brush = Brush.radialGradient(listOf(purple.copy(alpha = .18f), Color.Transparent)),
                radius = 58f,
                center = Offset(cx, cy + 5f)
            )

            val shackleTop = cy - 37f
            drawArc(
                color = Color(0xFFEE83FF),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 23f, shackleTop),
                size = Size(46f, 50f),
                style = Stroke(7f, cap = StrokeCap.Round)
            )
            val bodyTop = cy - 10f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFFC065FF), Color(0xFF3D94FF))),
                topLeft = Offset(cx - 29f, bodyTop),
                size = Size(58f, 52f),
                cornerRadius = CornerRadius(13f, 13f)
            )
            drawCircle(Color(0xFF08162F), 8f, Offset(cx, bodyTop + 24f))
            drawLine(
                Color(0xFF08162F),
                Offset(cx, bodyTop + 31f),
                Offset(cx, bodyTop + 42f),
                5f,
                StrokeCap.Round
            )
        }
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
