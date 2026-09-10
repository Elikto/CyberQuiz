package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.cyberquiz.engagement.DailyChallengeStore
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.engagement.ProfileTitleStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.model.achievementDefinitions
import com.example.cyberquiz.model.collectionCompletionPercent
import com.example.cyberquiz.model.effectiveProfileTitle
import com.example.cyberquiz.model.profileTitleDefinitions
import com.example.cyberquiz.model.unlockedProfileTitles

private val CollectionBg = Color(0xFF030712)
private val CollectionCard = Color(0xFF08142A)
private val CollectionText = Color(0xFFF5F7FF)
private val CollectionMuted = Color(0xFF9FAED3)
private val CollectionCyan = Color(0xFF19F2E5)
private val CollectionBlue = Color(0xFF19BFFF)
private val CollectionPurple = Color(0xFFD652FF)
private val CollectionGreen = Color(0xFF38E69A)
private val CollectionGold = Color(0xFFFFC86A)
private val CollectionOrange = Color(0xFFFF9F43)

private val dailyBadgeIds = listOf("daily_2", "daily_3", "daily_7", "daily_30")

private data class SecretCollectible(
    val kind: String,
    val name: String,
    val condition: String,
    val unlocked: Boolean
)

@Composable
internal fun CollectionScreen(
    playerLevel: Int,
    metrics: EngagementMetrics,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val snapshot = EngagementStore.sync(context, metrics)
    val unlockedAchievements = snapshot.unlockedAchievementIds
    val unlockedAchievementCount = achievementDefinitions.count { it.id in unlockedAchievements }
    val unlockedTitles = unlockedProfileTitles(unlockedAchievements)
    val daily = DailyChallengeStore.snapshot(context)
    var selectedTitleId by rememberSaveable {
        mutableStateOf(ProfileTitleStore.selectedTitleId(context))
    }
    val activeTitle = effectiveProfileTitle(selectedTitleId, unlockedAchievements)

    val secretCollectibles = buildList {
        mysteryPlayerAvatarStyles.forEach { style ->
            add(
                SecretCollectible(
                    kind = "AVATAR",
                    name = if (isAvatarUnlocked(style, playerLevel, unlockedAchievements)) {
                        style.displayName
                    } else {
                        lockedAvatarTitle(style)
                    },
                    condition = lockedAvatarCondition(style),
                    unlocked = isAvatarUnlocked(style, playerLevel, unlockedAchievements)
                )
            )
        }
        mysteryPlayerBannerStyles.forEach { style ->
            add(
                SecretCollectible(
                    kind = "BANNIÈRE",
                    name = if (isBannerUnlocked(style, playerLevel, unlockedAchievements)) {
                        style.displayName
                    } else {
                        lockedBannerTitle(style)
                    },
                    condition = lockedBannerCondition(style),
                    unlocked = isBannerUnlocked(style, playerLevel, unlockedAchievements)
                )
            )
        }
        PlayerFrameStyle.entries
            .filter { it.achievementId != null }
            .forEach { style ->
                val unlocked = isFrameUnlocked(
                    style = style,
                    playerLevel = playerLevel,
                    purchasedFrameKeys = snapshot.purchasedFrameKeys,
                    unlockedAchievementIds = unlockedAchievements
                )
                val achievement = achievementDefinitions.firstOrNull { it.id == style.achievementId }
                add(
                    SecretCollectible(
                        kind = "CONTOUR",
                        name = if (unlocked) style.displayName else "???",
                        condition = achievement?.description ?: "Succès à découvrir",
                        unlocked = unlocked
                    )
                )
            }
    }

    val unlockedSecretCount = secretCollectibles.count { it.unlocked }
    val completionPercent = collectionCompletionPercent(
        unlockedAchievements = unlockedAchievementCount,
        totalAchievements = achievementDefinitions.size,
        unlockedTitles = unlockedTitles.size,
        totalTitles = profileTitleDefinitions.size,
        unlockedSecretCosmetics = unlockedSecretCount,
        totalSecretCosmetics = secretCollectibles.size
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CollectionBg, Color(0xFF07142B), Color(0xFF160B2A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF111C34), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = CollectionCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "COLLECTION",
                    color = CollectionCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.7.sp
                )
                Text("Tes trophées cyber", color = CollectionText, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier
                    .background(CollectionPurple.copy(alpha = .12f), RoundedCornerShape(50.dp))
                    .border(1.dp, CollectionPurple.copy(alpha = .60f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text("$completionPercent %", color = CollectionPurple, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }

        activeTitle?.let { title ->
            Text(
                "Titre actif · ${title.label}",
                color = CollectionGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CollectionGold.copy(alpha = .07f), RoundedCornerShape(14.dp))
                    .border(1.dp, CollectionGold.copy(alpha = .30f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CollectionMetric(
                value = "$unlockedAchievementCount/${achievementDefinitions.size}",
                label = "Succès",
                accent = CollectionCyan,
                modifier = Modifier.weight(1f)
            )
            CollectionMetric(
                value = "${unlockedTitles.size}/${profileTitleDefinitions.size}",
                label = "Titres",
                accent = CollectionGold,
                modifier = Modifier.weight(1f)
            )
            CollectionMetric(
                value = "$unlockedSecretCount/${secretCollectibles.size}",
                label = "Secrets",
                accent = CollectionPurple,
                modifier = Modifier.weight(1f)
            )
        }

        CollectionSection("BADGES DE DÉFI QUOTIDIEN", "Les séries 2, 3, 7 et 30 jours restent visibles ici.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            dailyBadgeIds.forEach { badgeId ->
                val unlocked = badgeId in daily.badges
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            (if (unlocked) CollectionGreen else CollectionCard).copy(alpha = if (unlocked) .10f else 1f),
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            (if (unlocked) CollectionGreen else Color(0xFF29466F)).copy(alpha = .65f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(vertical = 11.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (unlocked) "◆" else "◇", color = if (unlocked) CollectionGreen else CollectionMuted, fontSize = 14.sp)
                    Text(
                        DailyChallengeStore.badgeLabel(badgeId),
                        color = if (unlocked) CollectionText else CollectionMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        CollectionSection("SUCCÈS", "Chaque réussite devient un badge permanent dans ta collection.")
        achievementDefinitions.forEach { achievement ->
            val unlocked = achievement.id in unlockedAchievements
            val rewardLabels = rewardLabelsForAchievement(achievement.id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        (if (unlocked) CollectionGreen else CollectionCard).copy(alpha = if (unlocked) .065f else 1f),
                        RoundedCornerShape(17.dp)
                    )
                    .border(
                        1.dp,
                        (if (unlocked) CollectionGreen else Color(0xFF29466F)).copy(alpha = .45f),
                        RoundedCornerShape(17.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            (if (unlocked) CollectionGreen else CollectionMuted).copy(alpha = .12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (unlocked) "★" else "☆", color = if (unlocked) CollectionGold else CollectionMuted, fontSize = 17.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        achievement.title,
                        color = if (unlocked) CollectionText else CollectionMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(achievement.description, color = CollectionMuted, fontSize = 9.5.sp, lineHeight = 13.sp)
                    if (rewardLabels.isNotEmpty()) {
                        Text(
                            rewardLabels.joinToString(" · "),
                            color = if (unlocked) CollectionPurple else CollectionMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 11.sp
                        )
                    }
                }
                Text(
                    if (unlocked) "OBTENU" else "VERROUILLÉ",
                    color = if (unlocked) CollectionGreen else CollectionOrange,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        CollectionSection("TITRES DE PROFIL", "Les titres sont des récompenses de succès, sans dépense de CyberCoins.")
        profileTitleDefinitions.forEach { title ->
            val unlocked = title.achievementId in unlockedAchievements
            val selected = activeTitle?.id == title.id
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        (if (selected) CollectionGold else CollectionCard).copy(alpha = if (selected) .09f else 1f),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        (if (selected) CollectionGold else if (unlocked) CollectionPurple else Color(0xFF29466F)).copy(alpha = .55f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = unlocked) {
                        if (ProfileTitleStore.selectTitle(context, title.id, unlockedAchievements)) {
                            selectedTitleId = title.id
                        }
                    }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (unlocked) title.label else "Titre verrouillé",
                        color = if (unlocked) CollectionText else CollectionMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        when {
                            selected -> "ACTIF"
                            unlocked -> "ÉQUIPER"
                            else -> "🔒"
                        },
                        color = when {
                            selected -> CollectionGold
                            unlocked -> CollectionPurple
                            else -> CollectionMuted
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(title.description, color = CollectionMuted, fontSize = 9.5.sp, lineHeight = 13.sp)
            }
        }

        CollectionSection("OBJETS SECRETS", "Avatars, bannières et contours gagnés automatiquement avec tes succès.")
        secretCollectibles.forEach { collectible ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CollectionCard, RoundedCornerShape(15.dp))
                    .border(
                        1.dp,
                        (if (collectible.unlocked) CollectionCyan else Color(0xFF29466F)).copy(alpha = .40f),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (collectible.unlocked) "◆" else "◇",
                    color = if (collectible.unlocked) CollectionCyan else CollectionMuted,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        collectible.name,
                        color = if (collectible.unlocked) CollectionText else CollectionMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (collectible.unlocked) "Débloqué dans ta collection" else collectible.condition,
                        color = CollectionMuted,
                        fontSize = 8.5.sp,
                        lineHeight = 12.sp
                    )
                }
                Text(
                    collectible.kind,
                    color = if (collectible.unlocked) CollectionGreen else CollectionMuted,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        val purchasedCount = snapshot.purchasedAvatarKeys.size +
            snapshot.purchasedBannerKeys.size + snapshot.purchasedFrameKeys.size
        if (purchasedCount > 0) {
            Text(
                "$purchasedCount cosmétique${if (purchasedCount > 1) "s" else ""} acheté${if (purchasedCount > 1) "s" else ""} en boutique s'ajoutent à ta collection.",
                color = CollectionBlue,
                fontSize = 9.5.sp,
                lineHeight = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CollectionBlue.copy(alpha = .06f), RoundedCornerShape(14.dp))
                    .border(1.dp, CollectionBlue.copy(alpha = .25f), RoundedCornerShape(14.dp))
                    .padding(11.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CollectionMetric(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CollectionCard, RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = CollectionMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CollectionSection(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            title,
            color = CollectionCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.3.sp
        )
        Text(subtitle, color = CollectionMuted, fontSize = 9.5.sp, lineHeight = 13.sp)
    }
}

private fun rewardLabelsForAchievement(achievementId: String): List<String> = buildList {
    profileTitleDefinitions
        .filter { it.achievementId == achievementId }
        .forEach { add("Titre · ${it.label}") }

    mysteryPlayerAvatarStyles
        .filter { secretAvatarRule(it)?.achievementId == achievementId }
        .forEach { add("Avatar · ${it.displayName}") }

    mysteryPlayerBannerStyles
        .filter { secretBannerRule(it)?.achievementId == achievementId }
        .forEach { add("Bannière · ${it.displayName}") }

    PlayerFrameStyle.entries
        .filter { it.achievementId == achievementId }
        .forEach { add("Contour · ${it.displayName}") }
}