package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.model.claimableMissionIds
import com.example.cyberquiz.social.AccountEconomyManager
import com.example.cyberquiz.ui.theme.CyberBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QuestText = Color(0xFFF5F7FF)
private val QuestMuted = Color(0xFF96A7CD)
private val QuestBorder = Color(0xFF244777)
private val QuestCyan = Color(0xFF19F2E5)
private val QuestOrange = Color(0xFFFFB84A)
private val QuestGreen = Color(0xFF38E69A)
private val QuestPurple = Color(0xFFD652FF)
private val QuestRed = Color(0xFFFF667F)

@Composable
fun DailyQuestsScreen(
    metrics: EngagementMetrics,
    onCoinsChanged: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var snapshot by remember(metrics) { mutableStateOf(EngagementStore.sync(context, metrics)) }
    var rewardAnimation by remember { mutableIntStateOf(0) }
    var claimingMissionId by remember { mutableStateOf<String?>(null) }
    var claimError by remember { mutableStateOf<String?>(null) }
    val claimable = claimableMissionIds(snapshot.missions, snapshot.claimedMissionIds)

    LaunchedEffect(metrics) {
        runCatching { AccountEconomyManager.syncCurrentSession(context) }
            .onSuccess { if (it != null) snapshot = EngagementStore.snapshot(context, metrics) }
    }

    LaunchedEffect(rewardAnimation) {
        if (rewardAnimation > 0) {
            delay(1500)
            rewardAnimation = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(Color(0xFF101A34), CircleShape)
                        .border(1.dp, Color(0xFF718CE2), CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("‹", color = QuestText, fontSize = 29.sp)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("Quêtes du jour", color = QuestText, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text(
                        if (claimable.isEmpty()) "OBJECTIFS QUOTIDIENS" else "${claimable.size} RÉCOMPENSE${if (claimable.size > 1) "S" else ""} À RÉCOLTER",
                        color = if (claimable.isEmpty()) QuestMuted else QuestOrange,
                        fontSize = 8.sp,
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    Modifier
                        .background(Color(0xFF21163A), RoundedCornerShape(50.dp))
                        .border(1.dp, QuestOrange.copy(alpha = .65f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("◈ ${snapshot.coins}", color = Color(0xFFFFC86A), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            claimError?.let { message ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(QuestRed.copy(alpha = .10f), RoundedCornerShape(14.dp))
                        .border(1.dp, QuestRed.copy(alpha = .55f), RoundedCornerShape(14.dp))
                        .clickable { claimError = null }
                        .padding(11.dp)
                ) {
                    Text(message, color = QuestRed, fontSize = 10.sp)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(QuestOrange.copy(alpha = .08f), RoundedCornerShape(17.dp))
                    .border(1.dp, QuestOrange.copy(alpha = .35f), RoundedCornerShape(17.dp))
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 23.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Série quotidienne : ${snapshot.loginStreak}", color = QuestText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("La récompense de connexion est créditée automatiquement.", color = QuestMuted, fontSize = 9.sp)
                }
                Text("+${snapshot.loginRewardToday} ◈", color = QuestOrange, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }

            snapshot.missions.forEach { mission ->
                val claimed = mission.definition.id in snapshot.claimedMissionIds
                val canCollect = mission.completed && !claimed
                val progress = CosmeticProgress(
                    current = mission.current,
                    target = mission.definition.target,
                    label = "${mission.current} / ${mission.definition.target}"
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF081329), RoundedCornerShape(18.dp))
                        .border(
                            1.dp,
                            when {
                                canCollect -> QuestOrange.copy(alpha = .8f)
                                claimed -> QuestGreen.copy(alpha = .55f)
                                else -> QuestBorder
                            },
                            RoundedCornerShape(18.dp)
                        )
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(mission.definition.title, color = QuestText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(mission.definition.description, color = QuestMuted, fontSize = 9.5.sp, lineHeight = 13.sp)
                        }
                        Text(
                            "+${mission.definition.rewardCoins} ◈",
                            color = if (claimed) QuestGreen else QuestOrange,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    CosmeticProgressBar(progress)

                    if (canCollect) {
                        val collecting = claimingMissionId == mission.definition.id
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(QuestPurple.copy(alpha = .42f), QuestCyan.copy(alpha = .32f))),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, QuestCyan.copy(alpha = .65f), RoundedCornerShape(12.dp))
                                .clickable(enabled = claimingMissionId == null) {
                                    scope.launch {
                                        claimingMissionId = mission.definition.id
                                        claimError = null
                                        runCatching {
                                            AccountEconomyManager.claimMission(context, mission.definition.id)
                                        }.onSuccess { gained ->
                                            snapshot = EngagementStore.snapshot(context, metrics)
                                            if (gained > 0) rewardAnimation = gained
                                            onCoinsChanged()
                                        }.onFailure { throwable ->
                                            claimError = throwable.message ?: "Impossible de récolter la récompense. Réessaie avec une connexion Internet."
                                        }
                                        claimingMissionId = null
                                    }
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (collecting) "VALIDATION…" else "RÉCOLTER", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    } else {
                        Text(
                            if (claimed) "RÉCOMPENSE RÉCOLTÉE ✓" else "EN COURS",
                            color = if (claimed) QuestGreen else QuestMuted,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        AnimatedVisibility(
            visible = rewardAnimation > 0,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = .55f) + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut() + scaleOut(targetScale = .8f)
        ) {
            Column(
                Modifier
                    .background(Color(0xEE10152A), RoundedCornerShape(24.dp))
                    .border(1.5.dp, QuestOrange, RoundedCornerShape(24.dp))
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("◈", color = Color(0xFFFFD36F), fontSize = 34.sp, fontWeight = FontWeight.Black)
                Text("+$rewardAnimation CyberCoins", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("QUÊTE TERMINÉE", color = QuestCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            }
        }
    }
}
