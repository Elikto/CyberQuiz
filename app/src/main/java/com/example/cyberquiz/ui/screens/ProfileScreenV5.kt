package com.example.cyberquiz.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PROFILE_V5_PREFERENCES = "cyberquiz_preferences"
private const val PROFILE_V5_NICKNAME_KEY = "player_nickname"
private const val DEFAULT_PROFILE_V5_NICKNAME = "Joueur Cyber"
private const val MAX_PROFILE_V5_NICKNAME_LENGTH = 24

private val ProfileV5Purple = Color(0xFFD652FF)
private val ProfileV5Blue = Color(0xFF19BFFF)
private val ProfileV5Cyan = Color(0xFF19F2E5)
private val ProfileV5Green = Color(0xFF38E69A)
private val ProfileV5Orange = Color(0xFFFFB84A)
private val ProfileV5Text = Color(0xFFF5F7FF)
private val ProfileV5Muted = Color(0xFF9FAED3)
private val ProfileV5Border = Color(0xFF244777)

@Composable
fun ProfileScreenV5(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember(context) {
        context.getSharedPreferences(PROFILE_V5_PREFERENCES, Context.MODE_PRIVATE)
    }
    val cosmetics = remember(context) { playerCosmeticsPreferences(context) }

    var nickname by rememberSaveable {
        mutableStateOf(
            preferences.getString(PROFILE_V5_NICKNAME_KEY, DEFAULT_PROFILE_V5_NICKNAME)
                ?.takeIf { it.isNotBlank() }
                ?: DEFAULT_PROFILE_V5_NICKNAME
        )
    }
    var selectedAvatarKey by rememberSaveable {
        mutableStateOf(
            cosmetics.getString(
                PLAYER_SELECTED_AVATAR_KEY,
                PlayerAvatarStyle.BEGINNER.storageKey
            ) ?: PlayerAvatarStyle.BEGINNER.storageKey
        )
    }
    var customAvatarPath by rememberSaveable {
        mutableStateOf(cosmetics.getString(PLAYER_CUSTOM_AVATAR_PATH_KEY, null))
    }
    var showProfileDetails by rememberSaveable { mutableStateOf(false) }
    var showAvatarChooser by rememberSaveable { mutableStateOf(false) }
    var bitmapToCrop by remember { mutableStateOf<Bitmap?>(null) }
    var cropSaving by remember { mutableStateOf(false) }
    var imageError by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedAvatar = playerAvatarFromStorage(selectedAvatarKey)

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageError = null
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) { loadAvatarBitmap(context, uri) }
                if (bitmap == null) {
                    imageError = "Cette image n'a pas pu être ouverte. Essaie avec une autre photo."
                } else {
                    bitmapToCrop = bitmap
                    showAvatarChooser = false
                }
            }
        }
    }

    fun importPersonalImage() {
        imageError = null
        showAvatarChooser = false
        imagePicker.launch("image/*")
    }

    bitmapToCrop?.let { sourceBitmap ->
        AvatarPhotoCropScreen(
            bitmap = sourceBitmap,
            saving = cropSaving,
            onCancel = {
                if (!cropSaving) bitmapToCrop = null
            },
            onSave = { transform ->
                if (!cropSaving) {
                    cropSaving = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching {
                                val cropped = cropAvatarBitmap(sourceBitmap, transform)
                                saveCustomPlayerAvatar(context, cropped)
                            }
                        }
                        cropSaving = false
                        result.onSuccess { path ->
                            customAvatarPath = path
                            selectedAvatarKey = PlayerAvatarStyle.CUSTOM.storageKey
                            bitmapToCrop = null
                            imageError = null
                        }.onFailure {
                            imageError = "L'avatar n'a pas pu être enregistré."
                        }
                    }
                }
            }
        )
        return
    }

    if (showProfileDetails) {
        BackHandler { showProfileDetails = false }
        ProfileDetailsV5(
            nickname = nickname,
            selectedQuizType = selectedQuizType,
            selectedAvatar = selectedAvatar,
            imageError = imageError,
            onChooseAvatar = { showAvatarChooser = true },
            onImportImage = { importPersonalImage() },
            onNicknameSaved = { newNickname ->
                nickname = newNickname
                preferences.edit().putString(PROFILE_V5_NICKNAME_KEY, newNickname).apply()
            },
            onBack = { showProfileDetails = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(profileV5Background())
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileV5Header(
                title = "Profil",
                subtitle = "TON UNIVERS",
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF15143D), Color(0xFF07172F))),
                        RoundedCornerShape(23.dp)
                    )
                    .border(1.2.dp, Color(0xFF416EC4), RoundedCornerShape(23.dp))
                    .padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileDetails = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerAvatarButton(
                        style = selectedAvatar,
                        onClick = { showAvatarChooser = true },
                        size = 72.dp
                    )
                    Spacer(Modifier.width(15.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Mon profil", color = ProfileV5Text, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text(nickname, color = ProfileV5Cyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            selectedAvatar.displayName,
                            color = ProfileV5Muted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                    Text("›", color = ProfileV5Cyan, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }

                ProfileAvatarActionButtons(
                    onChooseAvatar = { showAvatarChooser = true },
                    onImportImage = { importPersonalImage() }
                )
            }

            imageError?.let { message ->
                ProfileV5InfoMessage(message, ProfileV5Orange)
            }

            Text(
                "TYPE DE QUIZ",
                color = ProfileV5Blue,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                fontWeight = FontWeight.Bold
            )

            QuizType.entries.forEach { type ->
                ProfileUniverseCardV5(
                    type = type,
                    selected = type == selectedQuizType,
                    playable = type.isPlayableNow(),
                    onClick = { onQuizTypeSelected(type) }
                )
            }

            val playable = selectedQuizType.isPlayableNow()
            val statusColor = if (playable) ProfileV5Green else ProfileV5Orange
            ProfileV5InfoMessage(
                if (playable) {
                    "${selectedQuizType.label} est prêt à jouer."
                } else {
                    "${selectedQuizType.label} est sélectionné · questions bientôt disponibles."
                },
                statusColor
            )
        }
    }

    if (showAvatarChooser) {
        ProfileAvatarChooserDialogV5(
            selected = selectedAvatar,
            hasCustomAvatar = !customAvatarPath.isNullOrBlank(),
            onSelect = { style ->
                storePlayerAvatar(context, style)
                selectedAvatarKey = style.storageKey
                showAvatarChooser = false
                imageError = null
            },
            onImport = { importPersonalImage() },
            onDismiss = { showAvatarChooser = false }
        )
    }
}

@Composable
private fun ProfileDetailsV5(
    nickname: String,
    selectedQuizType: QuizType,
    selectedAvatar: PlayerAvatarStyle,
    imageError: String?,
    onChooseAvatar: () -> Unit,
    onImportImage: () -> Unit,
    onNicknameSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var editedNickname by rememberSaveable(nickname) { mutableStateOf(nickname) }
    var savedMessage by rememberSaveable { mutableStateOf(false) }
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    val cleanedNickname = editedNickname.trim()
    val canSave = cleanedNickname.isNotBlank() && cleanedNickname != nickname

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileV5Background())
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileV5Header(
            title = "Mon profil",
            subtitle = "PERSONNALISATION",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF11183A), Color(0xFF071329))),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, ProfileV5Border, RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatarButton(
                    style = selectedAvatar,
                    onClick = onChooseAvatar,
                    size = 76.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("AVATAR", color = ProfileV5Cyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                    Text(
                        selectedAvatar.displayName,
                        color = ProfileV5Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Personnalise ton avatar CyberQuiz.",
                        color = ProfileV5Muted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            ProfileAvatarActionButtons(
                onChooseAvatar = onChooseAvatar,
                onImportImage = onImportImage
            )
        }

        imageError?.let { ProfileV5InfoMessage(it, ProfileV5Orange) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF081329), RoundedCornerShape(20.dp))
                .border(1.dp, ProfileV5Border, RoundedCornerShape(20.dp))
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("PSEUDO", color = ProfileV5Blue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            OutlinedTextField(
                value = editedNickname,
                onValueChange = { value ->
                    if (value.length <= MAX_PROFILE_V5_NICKNAME_LENGTH) {
                        editedNickname = value
                        savedMessage = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(DEFAULT_PROFILE_V5_NICKNAME, color = ProfileV5Muted) },
                supportingText = {
                    Text(
                        "${editedNickname.length} / $MAX_PROFILE_V5_NICKNAME_LENGTH caractères",
                        color = ProfileV5Muted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                shape = RoundedCornerShape(15.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = ProfileV5Text,
                    unfocusedTextColor = ProfileV5Text,
                    focusedContainerColor = Color(0xFF071429),
                    unfocusedContainerColor = Color(0xFF071429),
                    focusedIndicatorColor = ProfileV5Cyan,
                    unfocusedIndicatorColor = ProfileV5Border,
                    cursorColor = ProfileV5Cyan
                )
            )

            Button(
                enabled = canSave,
                onClick = {
                    onNicknameSaved(cleanedNickname)
                    editedNickname = cleanedNickname
                    savedMessage = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileV5Purple,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF202A42),
                    disabledContentColor = ProfileV5Muted
                )
            ) {
                Text("ENREGISTRER LE PSEUDO", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }

            if (savedMessage) {
                Text("✓ Pseudo enregistré", color = ProfileV5Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF081329), RoundedCornerShape(18.dp))
                .border(1.dp, ProfileV5Border, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .background(ProfileV5Cyan.copy(alpha = .10f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(selectedQuizType.symbol, color = ProfileV5Cyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Univers actuel", color = ProfileV5Muted, fontSize = 9.sp)
                Text(selectedQuizType.label, color = ProfileV5Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            "DONNÉES",
            color = ProfileV5Blue,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF251016), RoundedCornerShape(19.dp))
                .border(1.2.dp, Color(0xFFFF557A).copy(alpha = .70f), RoundedCornerShape(19.dp))
                .clickable { showClearDataDialog = true }
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(Color(0xFFFF557A).copy(alpha = .12f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⌫", color = Color(0xFFFF7A96), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Effacer les données enregistrées",
                    color = Color(0xFFFFA0B3),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Progression, historique, révisions, profil, avatar et quiz en cours",
                    color = ProfileV5Muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            Text("›", color = Color(0xFFFF7A96), fontSize = 23.sp)
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = Color(0xFF0B1429),
            title = { Text("Effacer les données ?", color = ProfileV5Text, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Cette action supprimera toute la progression, l'historique, les révisions, le pseudo, l'avatar et les quiz en cours. L'application sera réinitialisée.",
                    color = ProfileV5Muted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        activityManager?.clearApplicationUserData()
                    }
                ) {
                    Text("EFFACER", color = Color(0xFFFF557A), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("ANNULER", color = ProfileV5Cyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileAvatarActionButtons(
    onChooseAvatar: () -> Unit,
    onImportImage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onChooseAvatar,
            modifier = Modifier.weight(1f).height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileV5Purple.copy(alpha = .92f),
                contentColor = Color.White
            )
        ) {
            Text("CHANGER D'AVATAR", fontSize = 9.sp, fontWeight = FontWeight.Black)
        }

        OutlinedButton(
            onClick = onImportImage,
            modifier = Modifier.weight(1f).height(46.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, ProfileV5Cyan.copy(alpha = .85f))
        ) {
            Text("IMAGE PERSO", color = ProfileV5Cyan, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ProfileAvatarChooserDialogV5(
    selected: PlayerAvatarStyle,
    hasCustomAvatar: Boolean,
    onSelect: (PlayerAvatarStyle) -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF081225),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Changer d'avatar", color = ProfileV5Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(
                    "Choisis un avatar CyberQuiz ou importe une image personnelle.",
                    color = ProfileV5Muted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                builtInPlayerAvatarStyles.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { style ->
                            AvatarChoiceCard(
                                style = style,
                                selected = style == selected,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelect(style) }
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                if (hasCustomAvatar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ProfileV5Cyan.copy(alpha = .08f), RoundedCornerShape(16.dp))
                            .border(
                                if (selected == PlayerAvatarStyle.CUSTOM) 1.5.dp else 1.dp,
                                ProfileV5Cyan.copy(alpha = .60f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(PlayerAvatarStyle.CUSTOM) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerAvatarButton(
                            style = PlayerAvatarStyle.CUSTOM,
                            onClick = { onSelect(PlayerAvatarStyle.CUSTOM) },
                            size = 48.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Mon image personnelle", color = ProfileV5Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (selected == PlayerAvatarStyle.CUSTOM) "SÉLECTIONNÉE" else "Utiliser la photo déjà recadrée",
                                color = if (selected == PlayerAvatarStyle.CUSTOM) ProfileV5Green else ProfileV5Muted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onImport,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileV5Cyan.copy(alpha = .16f),
                        contentColor = ProfileV5Cyan
                    )
                ) {
                    Text(
                        if (hasCustomAvatar) "IMPORTER UNE AUTRE IMAGE" else "IMPORTER UNE IMAGE PERSONNELLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("FERMER", color = ProfileV5Muted, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ProfileUniverseCardV5(
    type: QuizType,
    selected: Boolean,
    playable: Boolean,
    onClick: () -> Unit
) {
    val accent = when (type) {
        QuizType.CYBERSECURITY -> ProfileV5Blue
        QuizType.NUTRITION -> ProfileV5Green
        QuizType.TAROT -> ProfileV5Purple
        QuizType.LITHOTHERAPY -> ProfileV5Cyan
        QuizType.GENERAL_KNOWLEDGE -> Color(0xFF8B7CFF)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        if (selected) accent.copy(alpha = .16f) else Color(0xFF091429),
                        Color(0xFF071124)
                    )
                ),
                RoundedCornerShape(19.dp)
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) accent else ProfileV5Border,
                RoundedCornerShape(19.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(accent.copy(alpha = .13f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(type.symbol, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(type.label, color = ProfileV5Text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (playable) "PRÊT" else "BIENTÔT",
                    color = if (playable) ProfileV5Green else ProfileV5Orange,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(type.description, color = ProfileV5Muted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Text(
            if (selected) "✓" else "›",
            color = if (selected) ProfileV5Green else accent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileV5Header(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = ProfileV5Text, fontSize = 30.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = ProfileV5Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = ProfileV5Muted, fontSize = 8.sp, letterSpacing = 1.4.sp)
        }
    }
}

@Composable
private fun ProfileV5InfoMessage(text: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .08f), RoundedCornerShape(17.dp))
            .border(1.dp, accent.copy(alpha = .25f), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("•", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(9.dp))
        Text(
            text,
            color = Color(0xFFD4DCF5),
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun profileV5Background(): Brush =
    Brush.verticalGradient(
        listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
    )
