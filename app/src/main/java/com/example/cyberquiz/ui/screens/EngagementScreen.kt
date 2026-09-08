package com.example.cyberquiz.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementSnapshot
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.model.achievementDefinitions
import com.example.cyberquiz.ui.theme.CyberBackground

private val RewardPurple = Color(0xFFD652FF)
private val RewardBlue = Color(0xFF19BFFF)
private val RewardCyan = Color(0xFF19F2E5)
private val RewardGreen = Color(0xFF38E69A)
private val RewardOrange = Color(0xFFFFB84A)
private val RewardText = Color(0xFFF5F7FF)
private val RewardMuted = Color(0xFF9FAED3)
private val RewardBorder = Color(0xFF244777)

@Composable
fun EngagementScreen(
    metrics: EngagementMetrics,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var snapshot by remember(metrics) { mutableStateOf(EngagementStore.sync(context, metrics)) }
    var selectedFrame by remember {
        mutableStateOf(storedPlayerFrame(context))
    }
    val avatar = remember { storedPlayerAvatar(context) }
    val banner = remember { storedPlayerBanner(context) }

    fun refresh() {
        snapshot = EngagementStore.sync(context, metrics)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RewardHeader(onBack)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RewardCounterCard("◈", snapshot.coins.toString(), "CyberCoins", RewardOrange, Modifier.weight(1f))
            RewardCounterCard("🔥", snapshot.loginStreak.toString(), "Jours de suite", RewardPurple, Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF15143D), Color(0xFF07172F))),
                    RoundedCornerShape(22.dp)
                )
                .border(1.2.dp, Color(0xFF416EC4), RoundedCornerShape(22.dp))
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatarWithFrame(
                style = avatar,
                onClick = {},
                size = 76.dp,
                syncWithStoredSelection = false,
                bannerStyle = banner,
                syncBannerWithStoredSelection = false,
                frameStyle = selectedFrame,
                syncFrameWithStoredSelection = false,
                showEditBadge = false
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("TON ÉQUIPEMENT", color = RewardCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                Text(selectedFrame.displayName, color = RewardText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Niveau ${metrics.level} · ${snapshot.unlockedAchievementIds.size}/${achievementDefinitions.size} succès",
                    color = RewardMuted,
                    fontSize = 10.sp
                )
            }
        }

        DailyLoginCard(snapshot)

        SectionTitle("MISSIONS DU JOUR", "3 objectifs renouvelés chaque jour")
        snapshot.missions.forEach { mission ->
            MissionCard(
                title = mission.definition.title,
                description = mission.definition.description,
                current = mission.current,
                target = mission.definition.target,
                reward = mission.definition.rewardCoins,
                claimed = mission.definition.id in snapshot.claimedMissionIds
            )
        }

        SectionTitle("CADRES D'AVATAR", "Débloque, achète et équipe de nouveaux cadres")
        PlayerFrameStyle.entries.chunked(2).forEach { rowFrames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rowFrames.forEach { frame ->
                    FrameCard(
                        style = frame,
                        playerLevel = metrics.level,
                        snapshot = snapshot,
                        selected = frame == selectedFrame,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val unlocked = isFrameUnlocked(
                                frame,
                                metrics.level,
                                snapshot.purchasedFrameKeys,
                                snapshot.unlockedAchievementIds
                            )
                            when {
                                unlocked -> {
                                    storePlayerFrame(context, frame)
                                    selectedFrame = frame
                                }
                                canPurchaseFrame(
                                    frame,
                                    metrics.level,
                                    snapshot.purchasedFrameKeys,
                                    snapshot.coins
                                ) -> {
                                    if (EngagementStore.purchaseFrame(context, frame.storageKey, frame.coinCost)) {
                                        storePlayerFrame(context, frame)
                                        selectedFrame = frame
                                        refresh()
                                    }
                                }
                            }
                        }
                    )
                }
                if (rowFrames.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        SectionTitle("SUCCÈS", "Les succès donnent des CyberCoins et ouvrent certains cadres")
        achievementDefinitions.forEach { achievement ->
            val unlocked = achievement.id in snapshot.unlockedAchievementIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF081329), RoundedCornerShape(17.dp))
                    .border(
                        1.dp,
                        if (unlocked) RewardGreen.copy(alpha = .65f) else RewardBorder,
                        RoundedCornerShape(17.dp)
                    )
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            (if (unlocked) RewardGreen else RewardMuted).copy(alpha = .10f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (unlocked) "✓" else "🔒", color = if (unlocked) RewardGreen else RewardMuted, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(achievement.title, color = RewardText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(achievement.description, color = RewardMuted, fontSize = 10.sp, lineHeight = 14.sp)
                }
                Text(
                    "+${achievement.rewardCoins} ◈",
                    color = if (unlocked) RewardOrange else RewardMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RewardHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = RewardText, fontSize = 31.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Récompenses", color = RewardText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("MISSIONS · SUCCÈS · COLLECTION", color = RewardMuted, fontSize = 8.sp, letterSpacing = 1.4.sp)
        }
    }
}

@Composable
private fun RewardCounterCard(icon: String, value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(accent.copy(alpha = .08f), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = .40f), RoundedCornerShape(18.dp))
            .padding(13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(value, color = RewardText, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Text(label, color = RewardMuted, fontSize = 9.sp)
    }
}

@Composable
private fun DailyLoginCard(snapshot: EngagementSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RewardOrange.copy(alpha = .08f), RoundedCornerShape(18.dp))
            .border(1.dp, RewardOrange.copy(alpha = .35f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎁", fontSize = 25.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Récompense de connexion", color = RewardText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                "Série de ${snapshot.loginStreak} jour${if (snapshot.loginStreak > 1) "s" else ""} · récompense du jour déjà ajoutée",
                color = RewardMuted,
                fontSize = 10.sp
            )
        }
        Text("+${snapshot.loginRewardToday} ◈", color = RewardOrange, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MissionCard(
    title: String,
    description: String,
    current: Int,
    target: Int,
    reward: Int,
    claimed: Boolean
) {
    val progress = (current.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(1.dp, if (claimed) RewardGreen.copy(alpha = .55f) else RewardBorder, RoundedCornerShape(18.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = RewardText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(description, color = RewardMuted, fontSize = 10.sp)
            }
            Text(
                if (claimed) "✓ +$reward ◈" else "+$reward ◈",
                color = if (claimed) RewardGreen else RewardOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF111E35), RoundedCornerShape(50.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(
                        Brush.horizontalGradient(listOf(RewardPurple, RewardCyan)),
                        RoundedCornerShape(50.dp)
                    )
            )
        }
        Text("$current / $target", color = RewardMuted, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
    }
}

@Composable
private fun FrameCard(
    style: PlayerFrameStyle,
    playerLevel: Int,
    snapshot: EngagementSnapshot,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val unlocked = isFrameUnlocked(
        style,
        playerLevel,
        snapshot.purchasedFrameKeys,
        snapshot.unlockedAchievementIds
    )
    val canBuy = canPurchaseFrame(style, playerLevel, snapshot.purchasedFrameKeys, snapshot.coins)
    val hidden = style.mystery && !unlocked
    val title = if (hidden) "???" else style.displayName
    val status = when {
        selected -> "ÉQUIPÉ"
        unlocked -> "ÉQUIPER"
        hidden -> "SUCCÈS SECRET"
        playerLevel < style.unlockLevel -> "NIVEAU ${style.unlockLevel}"
        style.coinCost > 0 && canBuy -> "ACHETER · ${style.coinCost} ◈"
        style.coinCost > 0 -> "${style.coinCost} ◈"
        else -> "VERROUILLÉ"
    }

    Column(
        modifier = modifier
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) RewardGreen else RewardBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = unlocked || canBuy, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = if (hidden) Modifier.blur(6.dp) else Modifier,
            contentAlignment = Alignment.Center
        ) {
            PlayerFrameDecoration(style, Modifier.size(70.dp))
            Box(
                Modifier
                    .size(58.dp)
                    .background(Color(0xFF071020), RoundedCornerShape(17.dp))
            )
        }
        Text(title, color = RewardText, fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
        Text(
            status,
            color = when {
                selected -> RewardGreen
                unlocked || canBuy -> RewardCyan
                else -> RewardOrange
            },
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = RewardBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Text(subtitle, color = RewardMuted, fontSize = 10.sp)
    }
}
