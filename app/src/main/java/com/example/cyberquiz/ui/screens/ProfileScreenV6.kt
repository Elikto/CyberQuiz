package com.example.cyberquiz.ui.screens

import android.app.ActivityManager
import android.content.Context
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.cyberquiz.ui.theme.CyberBackground

private const val PROFILE_V6_PREFERENCES = "cyberquiz_preferences"
private const val PROFILE_V6_NICKNAME_KEY = "player_nickname"
private const val DEFAULT_PROFILE_V6_NICKNAME = "Joueur Cyber"
private const val MAX_PROFILE_V6_NICKNAME_LENGTH = 24

private val ProfileV6Blue = Color(0xFF19BFFF)
private val ProfileV6Cyan = Color(0xFF19F2E5)
private val ProfileV6Purple = Color(0xFFD652FF)
private val ProfileV6Text = Color(0xFFF5F7FF)
private val ProfileV6Muted = Color(0xFF9FAED3)
private val ProfileV6Border = Color(0xFF244777)

@Composable
internal fun ProfileScreenV6(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onCosmetics: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PROFILE_V6_PREFERENCES, Context.MODE_PRIVATE)
    }
    var nickname by rememberSaveable {
        mutableStateOf(
            preferences.getString(PROFILE_V6_NICKNAME_KEY, DEFAULT_PROFILE_V6_NICKNAME)
                ?.takeIf { it.isNotBlank() }
                ?: DEFAULT_PROFILE_V6_NICKNAME
        )
    }
    var details by rememberSaveable { mutableStateOf(false) }

    if (details) {
        BackHandler { details = false }
        ProfileDetailsV6(
            nickname = nickname,
            selectedQuizType = selectedQuizType,
            onNicknameSaved = { value ->
                nickname = value
                preferences.edit().putString(PROFILE_V6_NICKNAME_KEY, value).apply()
            },
            onBack = { details = false }
        )
        return
    }

    val avatar = storedPlayerAvatar(context)
    val banner = storedPlayerBanner(context)
    val frame = storedPlayerFrame(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileV6Background())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileHeaderV6("Profil", "TON UNIVERS", onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF15143D), Color(0xFF07172F))),
                    RoundedCornerShape(23.dp)
                )
                .border(1.2.dp, Color(0xFF416EC4), RoundedCornerShape(23.dp))
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { details = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberAvatarView(
                    style = avatar,
                    banner = banner,
                    frame = frame,
                    onClick = onCosmetics,
                    size = 74.dp,
                    showEditBadge = true
                )
                Spacer(Modifier.width(15.dp))
                Column(Modifier.weight(1f)) {
                    Text("Mon profil", color = ProfileV6Text, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(nickname, color = ProfileV6Cyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${avatar.displayName} · ${banner.displayName}",
                        color = ProfileV6Muted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    Text(frame.displayName, color = Color(0xFFFFC86A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text("›", color = ProfileV6Cyan, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onCosmetics,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileV6Purple,
                    contentColor = Color.White
                )
            ) {
                Text("AVATARS · BANNIÈRES · CONTOURS", fontSize = 9.5.sp, fontWeight = FontWeight.Black)
            }
        }

        Text(
            "TYPE DE QUIZ",
            color = ProfileV6Blue,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
        )

        QuizType.entries.forEach { type ->
            ProfileUniverseCardV6(
                type = type,
                selected = type == selectedQuizType,
                playable = type.isPlayableNow(),
                onClick = { onQuizTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun ProfileDetailsV6(
    nickname: String,
    selectedQuizType: QuizType,
    onNicknameSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var editedNickname by rememberSaveable(nickname) { mutableStateOf(nickname) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    val cleaned = editedNickname.trim()
    val canSave = cleaned.isNotBlank() && cleaned != nickname

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileV6Background())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileHeaderV6("Mon profil", "PERSONNALISATION", onBack)

        // Intentionally no second avatar/cosmetics card here: the main profile card is the single avatar preview.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF081329), RoundedCornerShape(20.dp))
                .border(1.dp, ProfileV6Border, RoundedCornerShape(20.dp))
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("PSEUDO", color = ProfileV6Blue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            OutlinedTextField(
                value = editedNickname,
                onValueChange = { value ->
                    if (value.length <= MAX_PROFILE_V6_NICKNAME_LENGTH) {
                        editedNickname = value
                        saved = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    Text(
                        "${editedNickname.length} / $MAX_PROFILE_V6_NICKNAME_LENGTH caractères",
                        color = ProfileV6Muted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = ProfileV6Text,
                    unfocusedTextColor = ProfileV6Text,
                    focusedContainerColor = Color(0xFF071429),
                    unfocusedContainerColor = Color(0xFF071429),
                    focusedIndicatorColor = ProfileV6Cyan,
                    unfocusedIndicatorColor = ProfileV6Border,
                    cursorColor = ProfileV6Cyan
                )
            )
            Button(
                enabled = canSave,
                onClick = {
                    onNicknameSaved(cleaned)
                    editedNickname = cleaned
                    saved = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileV6Purple,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF202A42),
                    disabledContentColor = ProfileV6Muted
                )
            ) {
                Text("ENREGISTRER LE PSEUDO", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            if (saved) Text("✓ Pseudo enregistré", color = Color(0xFF38E69A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF081329), RoundedCornerShape(18.dp))
                .border(1.dp, ProfileV6Border, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .background(ProfileV6Cyan.copy(alpha = .10f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(selectedQuizType.symbol, color = ProfileV6Cyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Univers actuel", color = ProfileV6Muted, fontSize = 9.sp)
                Text(selectedQuizType.label, color = ProfileV6Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text("DONNÉES", color = ProfileV6Blue, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF251016), RoundedCornerShape(19.dp))
                .border(1.2.dp, Color(0xFFFF557A).copy(alpha = .70f), RoundedCornerShape(19.dp))
                .clickable { showClearDataDialog = true }
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⌫", color = Color(0xFFFF7A96), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Effacer les données enregistrées", color = Color(0xFFFFA0B3), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Progression, historique, profil, récompenses et cosmétiques",
                    color = ProfileV6Muted,
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = Color(0xFF0B1429),
            title = { Text("Effacer les données ?", color = ProfileV6Text, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Cette action réinitialisera la progression, l'historique, le pseudo, les récompenses, les cosmétiques et les quiz en cours.",
                    color = ProfileV6Muted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        manager?.clearApplicationUserData()
                    }
                ) {
                    Text("EFFACER", color = Color(0xFFFF557A), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("ANNULER", color = ProfileV6Cyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileUniverseCardV6(
    type: QuizType,
    selected: Boolean,
    playable: Boolean,
    onClick: () -> Unit
) {
    val accent = when (type) {
        QuizType.CYBERSECURITY -> ProfileV6Blue
        QuizType.NUTRITION -> Color(0xFF38E69A)
        QuizType.TAROT -> ProfileV6Purple
        QuizType.LITHOTHERAPY -> ProfileV6Cyan
        QuizType.GENERAL_KNOWLEDGE -> Color(0xFF8B7CFF)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(if (selected) accent.copy(alpha = .16f) else Color(0xFF091429), Color(0xFF071124))
                ),
                RoundedCornerShape(19.dp)
            )
            .border(if (selected) 1.5.dp else 1.dp, if (selected) accent else ProfileV6Border, RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(accent.copy(alpha = .12f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(type.symbol, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(type.label, color = ProfileV6Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(if (playable) "Disponible" else "Bientôt disponible", color = ProfileV6Muted, fontSize = 10.sp)
        }
        if (selected) Text("✓", color = accent, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ProfileHeaderV6(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = ProfileV6Text, fontSize = 31.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = ProfileV6Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = ProfileV6Muted, fontSize = 8.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun profileV6Background(): Brush = Brush.verticalGradient(
    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
)
