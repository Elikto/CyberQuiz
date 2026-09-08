package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.ui.theme.CyberBackground

internal const val MAX_PLAYER_LEVEL = 30

internal data class LevelRoadmapEntry(
    val level: Int,
    val role: String,
    val rewards: List<String>
)

private val playerLevelRoles = listOf(
    "Recrue cyber",
    "Initié réseau",
    "Opérateur novice",
    "Veilleur numérique",
    "Analyste SOC",
    "Gardien réseau",
    "Traqueur de signaux",
    "Blue Team Elite",
    "Défenseur système",
    "Hacker éthique",
    "Éclaireur Red Team",
    "Red Team Runner",
    "Auditeur offensif",
    "Briseur de failles",
    "Expert sécurité",
    "Crypto Sentinel",
    "Gardien des clés",
    "Ingénieur défense",
    "Commandant SOC",
    "Architecte cyber",
    "Stratège cyber",
    "Maître des incidents",
    "Chasseur de zero-day",
    "Commandant Red Team",
    "Expert cyber senior",
    "Architecte défensif",
    "Maître opérateur",
    "Légende du réseau",
    "Gardien suprême",
    "Cyber Légende"
)

internal fun playerRoleForLevel(level: Int): String =
    playerLevelRoles[(level.coerceIn(1, MAX_PLAYER_LEVEL) - 1)]

internal fun levelRewards(level: Int): List<String> {
    if (level == 1) {
        return listOf("Pack de départ · 5 avatars", "4 bannières", "3 contours")
    }

    val rewards = mutableListOf<String>()
    PlayerAvatarStyle.entries
        .filter { !it.mystery && it.unlockLevel == level }
        .forEach { rewards += "Avatar · ${it.displayName}" }
    PlayerBannerStyle.entries
        .filter { !it.mystery && it.unlockLevel == level }
        .forEach { rewards += "Bannière · ${it.displayName}" }
    PlayerFrameStyle.entries
        .filter {
            !it.mystery && !it.shopItem && it.coinCost == 0 && it.achievementId == null && it.unlockLevel == level
        }
        .forEach { rewards += "Contour · ${it.displayName}" }
    return rewards
}

internal fun levelRoadmap(): List<LevelRoadmapEntry> =
    (MAX_PLAYER_LEVEL downTo 1).map { level ->
        LevelRoadmapEntry(level, playerRoleForLevel(level), levelRewards(level))
    }

@Composable
internal fun CompactGameLevelBar(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val displayLevel = level.coerceIn(1, MAX_PLAYER_LEVEL)
    val safeProgress = if (level >= MAX_PLAYER_LEVEL) 1f else progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(4.dp)

    Column(
        modifier = modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "NIV. $displayLevel",
                color = Color(0xFF63EFFF),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .width(104.dp)
                    .height(8.dp)
                    .clip(shape)
                    .background(Color(0xFF08111F))
                    .border(1.dp, Color(0xFF45618D), shape)
                    .padding(1.dp)
            ) {
                if (safeProgress > 0f) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(safeProgress)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6D45FF), Color(0xFFD24DFF), Color(0xFF2DE9FF))
                                ),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = .12f))
                        )
                    }
                }
            }
        }
        Text(
            playerRoleForLevel(displayLevel),
            color = Color(0xFFDCE6FF),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun LevelProgressionScreen(
    currentLevel: Int,
    currentProgress: Float,
    onBack: () -> Unit
) {
    val displayLevel = currentLevel.coerceIn(1, MAX_PLAYER_LEVEL)
    val roadmap = levelRoadmap()
    val currentIndex = (MAX_PLAYER_LEVEL - displayLevel).coerceIn(0, roadmap.lastIndex)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentIndex - 2).coerceAtLeast(0)
    )

    LaunchedEffect(displayLevel) {
        listState.scrollToItem((currentIndex - 2).coerceAtLeast(0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071020), CyberBackground, Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF101A34), CircleShape)
                    .border(1.dp, Color(0xFF6F8FEA), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = Color.White, fontSize = 28.sp)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Progression de niveau",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Niveau max $MAX_PLAYER_LEVEL · ${playerRoleForLevel(displayLevel)}",
                    color = Color(0xFF93A9D3),
                    fontSize = 9.sp
                )
            }
        }

        Spacer(Modifier.height(9.dp))
        CompactGameLevelBar(
            level = displayLevel,
            progress = currentProgress,
            onClick = {},
            modifier = Modifier.width(155.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Les niveaux les plus élevés sont en haut. Tu arrives directement près de ton niveau actuel.",
            color = Color(0xFF9CAED1),
            fontSize = 9.sp,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roadmap, key = { it.level }) { entry ->
                LevelRoadmapCard(entry = entry, currentLevel = displayLevel)
            }
        }
    }
}

@Composable
private fun LevelRoadmapCard(
    entry: LevelRoadmapEntry,
    currentLevel: Int
) {
    val current = entry.level == currentLevel
    val unlocked = entry.level < currentLevel
    val future = entry.level > currentLevel
    val accent = when {
        current -> Color(0xFF2DE9FF)
        unlocked -> Color(0xFF45D89A)
        else -> Color(0xFF536A94)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Stretch
    ) {
        Column(
            modifier = Modifier.width(42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (current) 30.dp else 24.dp)
                    .background(
                        if (current) Brush.radialGradient(listOf(Color(0xFF6D45FF), Color(0xFF132545)))
                        else Brush.radialGradient(listOf(Color(0xFF101B31), Color(0xFF07101F))),
                        CircleShape
                    )
                    .border(if (current) 2.dp else 1.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entry.level.toString(),
                    color = if (current || unlocked) Color.White else Color(0xFF8495B7),
                    fontSize = if (current) 10.sp else 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Box(
                Modifier
                    .width(2.dp)
                    .height(66.dp)
                    .background(accent.copy(alpha = if (future) .18f else .45f))
            )
        }

        Spacer(Modifier.width(6.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (current) {
                        Brush.horizontalGradient(listOf(Color(0xFF15143D), Color(0xFF08182D)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF091326), Color(0xFF07101E)))
                    },
                    RoundedCornerShape(16.dp)
                )
                .border(
                    if (current) 1.4.dp else 1.dp,
                    accent.copy(alpha = if (current) .95f else .38f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.role,
                    modifier = Modifier.weight(1f),
                    color = if (future) Color(0xFFA8B4D0) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    when {
                        current -> "ACTUEL"
                        unlocked -> "DÉBLOQUÉ"
                        else -> "À VENIR"
                    },
                    color = accent,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
            }

            if (entry.rewards.isEmpty()) {
                Text("Progression de rang", color = Color(0xFF7183A8), fontSize = 8.sp)
            } else {
                entry.rewards.forEach { reward ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).background(accent.copy(alpha = .8f), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            reward,
                            color = if (future) Color(0xFF9CAED1) else Color(0xFFD8E4FF),
                            fontSize = 8.5.sp,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}
