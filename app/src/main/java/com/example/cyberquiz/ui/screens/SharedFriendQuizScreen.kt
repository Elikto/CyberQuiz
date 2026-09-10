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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.cyberquiz.social.SharedQuizState
import com.example.cyberquiz.social.SharedQuizViewModel

private val SharedBg = Color(0xFF040817)
private val SharedCard = Color(0xFF09142A)
private val SharedCyan = Color(0xFF22E7F2)
private val SharedBlue = Color(0xFF248CFF)
private val SharedPurple = Color(0xFFB84DFF)
private val SharedText = Color(0xFFF5F7FF)
private val SharedMuted = Color(0xFF9DAAC8)
private val SharedGreen = Color(0xFF3AE69A)
private val SharedRed = Color(0xFFFF5E7D)

@Composable
internal fun SharedFriendQuizScreen(
    viewModel: SharedQuizViewModel,
    onBackToFriends: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var confirmQuit by remember { mutableStateOf(false) }

    fun leave() {
        if (state is SharedQuizState.Finished || state is SharedQuizState.Idle) {
            viewModel.reset()
            onBackToFriends()
        } else {
            viewModel.leaveRemoteRoom(onBackToFriends)
        }
    }

    BackHandler {
        if (state is SharedQuizState.Playing) confirmQuit = true else leave()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SharedBg, Color(0xFF071126), Color(0xFF120A26))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF111C34), RoundedCornerShape(12.dp))
                    .clickable {
                        if (state is SharedQuizState.Playing) confirmQuit = true else leave()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = SharedCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("QUIZ ENTRE AMIS", color = SharedCyan, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Black)
                Text("Même quiz · même départ", color = SharedText, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }

        when (val current = state) {
            SharedQuizState.Idle,
            SharedQuizState.Loading -> SharedLoadingCard()

            is SharedQuizState.Error -> {
                SharedMessageCard(current.message, SharedRed)
                Button(onClick = ::leave, modifier = Modifier.fillMaxWidth()) {
                    Text("RETOUR AUX AMIS", fontWeight = FontWeight.Black)
                }
            }

            is SharedQuizState.Playing -> {
                SharedPlayersStrip(current.room.members.map { it.user.nickname to it.answered })
                Text(
                    "QUESTION ${current.index + 1} / ${current.total}",
                    color = SharedBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.3.sp
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SharedCard, RoundedCornerShape(22.dp))
                        .border(1.dp, Color(0xFF26558A), RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        current.question.question,
                        color = SharedText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 25.sp
                    )
                    val answers = listOf(
                        current.question.answerA,
                        current.question.answerB,
                        current.question.answerC,
                        current.question.answerD
                    )
                    answers.forEachIndexed { index, answer ->
                        SharedAnswerCard(
                            text = answer,
                            selected = current.selectedIndex == index,
                            correct = when {
                                current.selectedIndex == null -> null
                                index == current.question.correctIndex -> true
                                current.selectedIndex == index -> false
                                else -> null
                            },
                            enabled = current.selectedIndex == null,
                            onClick = { viewModel.answer(index) }
                        )
                    }
                }

                if (current.selectedIndex != null) {
                    SharedMessageCard(
                        message = if (current.correct == true) {
                            "Bonne réponse · score ${current.score}/${current.index + 1}"
                        } else {
                            "Réponse incorrecte · score ${current.score}/${current.index + 1}"
                        },
                        accent = if (current.correct == true) SharedGreen else SharedRed
                    )
                    if (current.question.explanation.isNotBlank()) {
                        Text(
                            current.question.explanation,
                            color = SharedMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Button(
                        onClick = viewModel::next,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SharedPurple),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            if (current.index + 1 >= current.total) "VOIR LES RÉSULTATS" else "QUESTION SUIVANTE",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            is SharedQuizState.Finished -> {
                Text("PARTIE TERMINÉE", color = SharedGreen, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SharedCard, RoundedCornerShape(24.dp))
                        .border(1.2.dp, SharedPurple.copy(alpha = .75f), RoundedCornerShape(24.dp))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("${current.score} / ${current.total}", color = SharedCyan, fontSize = 38.sp, fontWeight = FontWeight.Black)
                    Text("Ton score", color = SharedMuted, fontSize = 12.sp)
                }
                current.room?.members?.sortedByDescending { it.correct }?.forEachIndexed { index, member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A1730), RoundedCornerShape(16.dp))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}", color = SharedPurple, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(8.dp))
                        PlayerAvatarButton(
                            style = playerAvatarFromStorage(member.user.avatarKey),
                            onClick = {},
                            size = 42.dp,
                            syncWithStoredSelection = false,
                            syncBannerWithStoredSelection = false,
                            showEditBadge = false
                        )
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(member.user.nickname, color = SharedText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(if (member.finished) "Terminé" else "Encore en jeu", color = SharedMuted, fontSize = 10.sp)
                        }
                        Text(
                            "${member.correct}/${member.answered}",
                            color = SharedCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
                Button(
                    onClick = ::leave,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SharedPurple),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text("RETOUR AUX AMIS", fontWeight = FontWeight.Black)
                }
            }
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            containerColor = Color(0xFF0B1429),
            title = { Text("Quitter la partie ?", color = SharedText, fontWeight = FontWeight.Black) },
            text = { Text("Ton départ sera signalé au salon et la partie continuera pour tes amis.", color = SharedMuted) },
            confirmButton = {
                TextButton(onClick = { confirmQuit = false; leave() }) {
                    Text("QUITTER", color = SharedRed, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmQuit = false }) {
                    Text("RESTER", color = SharedCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SharedLoadingCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SharedCard, RoundedCornerShape(22.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("◌", color = SharedCyan, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Préparation du quiz…", color = SharedText, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SharedMessageCard(message: String, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .10f), RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = .70f), RoundedCornerShape(16.dp))
            .padding(13.dp)
    ) {
        Text(message, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SharedPlayersStrip(players: List<Pair<String, Int>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        players.take(4).forEach { (nickname, answered) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF0A1730), RoundedCornerShape(13.dp))
                    .padding(horizontal = 8.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(nickname.take(10), color = SharedText, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("$answered ✓", color = SharedCyan, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SharedAnswerCard(
    text: String,
    selected: Boolean,
    correct: Boolean?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = when (correct) {
        true -> SharedGreen
        false -> SharedRed
        null -> if (selected) SharedPurple else Color(0xFF31527A)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = if (selected || correct != null) .13f else .05f), RoundedCornerShape(15.dp))
            .border(1.dp, accent.copy(alpha = .80f), RoundedCornerShape(15.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(text, color = SharedText, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
