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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.cyberquiz.social.MAX_SQUAD_FRIENDS
import com.example.cyberquiz.social.SQUAD_QUESTION_COUNTS
import com.example.cyberquiz.social.SharedQuizViewModel
import com.example.cyberquiz.social.SocialApiClient
import com.example.cyberquiz.social.SocialTokenStore
import com.example.cyberquiz.social.SocialUser
import com.example.cyberquiz.social.SquadDashboard
import com.example.cyberquiz.social.SquadDashboardApiClient
import com.example.cyberquiz.social.toggleSquadFriend
import kotlinx.coroutines.launch

private val Squad2Bg = Color(0xFF030712)
private val Squad2Card = Color(0xFF08142A)
private val Squad2Text = Color(0xFFF5F7FF)
private val Squad2Muted = Color(0xFF9EACCB)
private val Squad2Cyan = Color(0xFF20E7F2)
private val Squad2Blue = Color(0xFF2B8CFF)
private val Squad2Purple = Color(0xFFB64EFF)
private val Squad2Green = Color(0xFF39E79B)
private val Squad2Gold = Color(0xFFFFC863)
private val Squad2Red = Color(0xFFFF5F7D)

@Composable
internal fun SquadDashboardScreen(
    sharedQuizViewModel: SharedQuizViewModel,
    onRoomCreated: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val token = remember(context) { SocialTokenStore.load(context) }
    var dashboard by remember { mutableStateOf<SquadDashboard?>(null) }
    var friends by remember { mutableStateOf<List<SocialUser>>(emptyList()) }
    var selectedFriendIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var questionCount by remember { mutableStateOf(10) }
    var loading by remember { mutableStateOf(true) }
    var creatingRoom by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun reload() {
        val activeToken = token ?: return
        loading = true
        error = null
        runCatching {
            val loadedDashboard = SquadDashboardApiClient.dashboard(activeToken)
            val loadedFriends = SocialApiClient.friends(activeToken)
            loadedDashboard to loadedFriends
        }.onSuccess { (loadedDashboard, loadedFriends) ->
            dashboard = loadedDashboard
            friends = loadedFriends
            selectedFriendIds = selectedFriendIds.intersect(loadedFriends.map { it.id }.toSet())
        }.onFailure { throwable ->
            error = throwable.message?.takeIf(String::isNotBlank)
                ?: "Impossible de charger CyberSquad."
        }
        loading = false
    }

    LaunchedEffect(token) {
        if (token == null) {
            loading = false
            error = "Connecte-toi d'abord à ton compte CyberQuiz pour utiliser CyberSquad."
        } else {
            reload()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Squad2Bg, Color(0xFF07152C), Color(0xFF160A2B))
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
                Text("‹", color = Squad2Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "CYBERSQUAD 2.0",
                    color = Squad2Cyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.7.sp
                )
                Text("Tableau d'escouade", color = Squad2Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
            if (loading) CircularProgressIndicator(color = Squad2Cyan, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }

        error?.let { message ->
            Squad2Notice(message, Squad2Red)
        }

        dashboard?.let { current ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Squad2Metric("${current.played}", "Parties", Squad2Blue, Modifier.weight(1f))
                Squad2Metric("${current.wins}", "Victoires", Squad2Gold, Modifier.weight(1f))
                Squad2Metric("${current.accuracy}%", "Réussite", Squad2Green, Modifier.weight(1f))
            }
        }

        Squad2Section("CRÉER UNE PARTIE D'ESCOUADE")
        Text(
            "Invite jusqu'à $MAX_SQUAD_FRIENDS amis. Tout le monde reçoit exactement les mêmes questions et le départ reste synchronisé.",
            color = Squad2Muted,
            fontSize = 11.sp,
            lineHeight = 17.sp
        )

        if (friends.isEmpty() && !loading) {
            Squad2Notice("Ajoute d'abord des amis depuis l'écran Compte & amis.", Squad2Blue)
        } else {
            friends.forEach { friend ->
                val selected = friend.id in selectedFriendIds
                val canSelect = selected || selectedFriendIds.size < MAX_SQUAD_FRIENDS
                SquadFriendSelectCard(
                    friend = friend,
                    selected = selected,
                    enabled = canSelect && !creatingRoom,
                    onClick = {
                        selectedFriendIds = toggleSquadFriend(selectedFriendIds, friend.id)
                    }
                )
            }
        }

        Text(
            "${selectedFriendIds.size}/$MAX_SQUAD_FRIENDS amis sélectionnés",
            color = if (selectedFriendIds.isEmpty()) Squad2Muted else Squad2Cyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Text("NOMBRE DE QUESTIONS", color = Squad2Muted, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SQUAD_QUESTION_COUNTS.forEach { count ->
                SquadCountChip(
                    count = count,
                    selected = questionCount == count,
                    modifier = Modifier.weight(1f),
                    onClick = { questionCount = count }
                )
            }
        }

        Button(
            onClick = {
                val activeToken = token ?: return@Button
                if (selectedFriendIds.isEmpty()) return@Button
                scope.launch {
                    creatingRoom = true
                    error = null
                    runCatching {
                        val questionIds = sharedQuizViewModel.prepareQuestionIds(questionCount)
                        require(questionIds.size == questionCount) {
                            "Pas assez de questions disponibles pour cette partie."
                        }
                        SocialApiClient.createRoom(
                            token = activeToken,
                            inviteeIds = selectedFriendIds.toList(),
                            questionIds = questionIds,
                            mode = "RANDOM",
                            categories = emptyList()
                        )
                    }.onSuccess {
                        onRoomCreated()
                    }.onFailure { throwable ->
                        error = throwable.message?.takeIf(String::isNotBlank)
                            ?: "Impossible de créer la partie."
                    }
                    creatingRoom = false
                }
            },
            enabled = token != null && selectedFriendIds.isNotEmpty() && !creatingRoom,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Squad2Purple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (creatingRoom) "CRÉATION…" else "INVITER L'ESCOUADE",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = .8.sp
            )
        }

        dashboard?.let { current ->
            Squad2Section("CLASSEMENT ENTRE AMIS")
            if (current.friendLeaderboard.isEmpty()) {
                Squad2Notice("Le classement apparaîtra après vos premières parties terminées.", Squad2Blue)
            } else {
                current.friendLeaderboard.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Squad2Card, RoundedCornerShape(16.dp))
                            .border(1.dp, Squad2Blue.copy(alpha = .30f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}", color = Squad2Gold, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(9.dp))
                        PlayerAvatarButton(
                            style = playerAvatarFromStorage(entry.user.avatarKey),
                            onClick = {},
                            size = 38.dp,
                            syncWithStoredSelection = false,
                            syncBannerWithStoredSelection = false,
                            showEditBadge = false
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.user.nickname, color = Squad2Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${entry.played} parties · ${entry.accuracy}%", color = Squad2Muted, fontSize = 9.sp)
                        }
                        Text("${entry.wins} V", color = Squad2Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Squad2Section("20 DERNIÈRES PARTIES")
            if (current.recentMatches.isEmpty()) {
                Squad2Notice("Aucune partie CyberSquad terminée pour l'instant.", Squad2Blue)
            } else {
                current.recentMatches.forEach { match ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Squad2Card, RoundedCornerShape(15.dp))
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val podium = match.rank == 1
                        Box(
                            Modifier
                                .size(35.dp)
                                .background(
                                    (if (podium) Squad2Gold else Squad2Blue).copy(alpha = .12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("#${match.rank}", color = if (podium) Squad2Gold else Squad2Blue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${match.correct}/${match.answered} bonnes réponses",
                                color = Squad2Text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${match.players} joueurs · ${match.questionCount} questions · ${shortDate(match.playedAt)}",
                                color = Squad2Muted,
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }

        if (!loading && token != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.launch { reload() } }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("ACTUALISER", color = Squad2Cyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
            }
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SquadFriendSelectCard(
    friend: SocialUser,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = if (selected) Squad2Purple else Squad2Blue
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = if (selected) .12f else .05f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = if (enabled) .60f else .20f), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatarButton(
            style = playerAvatarFromStorage(friend.avatarKey),
            onClick = {},
            size = 40.dp,
            syncWithStoredSelection = false,
            syncBannerWithStoredSelection = false,
            showEditBadge = false
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(friend.nickname, color = Squad2Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Niveau ${friend.level}", color = Squad2Muted, fontSize = 9.sp)
        }
        Box(
            Modifier
                .size(25.dp)
                .background(accent.copy(alpha = .12f), RoundedCornerShape(7.dp))
                .border(1.dp, accent.copy(alpha = .70f), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Text("✓", color = Squad2Cyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SquadCountChip(count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .background(
                (if (selected) Squad2Purple else Squad2Card).copy(alpha = if (selected) .16f else 1f),
                RoundedCornerShape(13.dp)
            )
            .border(1.dp, if (selected) Squad2Purple else Color(0xFF29466F), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("$count", color = if (selected) Squad2Text else Squad2Muted, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Squad2Metric(value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Squad2Card, RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(16.dp))
            .padding(vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(label, color = Squad2Muted, fontSize = 8.5.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Squad2Section(text: String) {
    Text(text, color = Squad2Cyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
}

@Composable
private fun Squad2Notice(message: String, accent: Color) {
    Text(
        message,
        color = accent,
        fontSize = 10.sp,
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .06f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .25f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    )
}

private fun shortDate(value: String?): String {
    if (value.isNullOrBlank()) return "date inconnue"
    return value.take(10)
}
