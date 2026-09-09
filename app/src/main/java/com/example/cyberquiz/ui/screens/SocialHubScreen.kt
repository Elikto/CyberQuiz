package com.example.cyberquiz.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.social.GoogleAccountSignIn
import com.example.cyberquiz.social.SharedQuizViewModel
import com.example.cyberquiz.social.SocialApiClient
import com.example.cyberquiz.social.SocialApiException
import com.example.cyberquiz.social.SocialFriendRequest
import com.example.cyberquiz.social.SocialQuizInvite
import com.example.cyberquiz.social.SocialQuizRoom
import com.example.cyberquiz.social.SocialSession
import com.example.cyberquiz.social.SocialTokenStore
import com.example.cyberquiz.social.SocialUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime

private val SocialBg = Color(0xFF040817)
private val SocialCard = Color(0xFF08142A)
private val SocialCardAlt = Color(0xFF0D1832)
private val SocialCyan = Color(0xFF20E7F2)
private val SocialBlue = Color(0xFF2B8CFF)
private val SocialPurple = Color(0xFFB64EFF)
private val SocialGreen = Color(0xFF39E79B)
private val SocialRed = Color(0xFFFF5F7D)
private val SocialGold = Color(0xFFFFC863)
private val SocialText = Color(0xFFF5F7FF)
private val SocialMuted = Color(0xFF9EACCB)
private val SocialBorder = Color(0xFF284A78)

private const val SOCIAL_PROFILE_PREFS = "cyberquiz_preferences"
private const val SOCIAL_NICKNAME_KEY = "player_nickname"

@Composable
internal fun SocialHubScreen(
    playerLevel: Int,
    sharedQuizViewModel: SharedQuizViewModel,
    onSharedQuizStart: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localNickname = remember(context) {
        context.getSharedPreferences(SOCIAL_PROFILE_PREFS, Context.MODE_PRIVATE)
            .getString(SOCIAL_NICKNAME_KEY, "Joueur Cyber")
            ?.takeIf { it.isNotBlank() }
            ?: "Joueur Cyber"
    }
    val localAvatarKey = storedPlayerAvatar(context).storageKey

    var token by rememberSaveable { mutableStateOf(SocialTokenStore.load(context)) }
    var me by remember { mutableStateOf<SocialUser?>(null) }
    var friends by remember { mutableStateOf<List<SocialUser>>(emptyList()) }
    var requests by remember { mutableStateOf<List<SocialFriendRequest>>(emptyList()) }
    var quizInvites by remember { mutableStateOf<List<SocialQuizInvite>>(emptyList()) }
    var currentRoom by remember { mutableStateOf<SocialQuizRoom?>(null) }
    var googleClientId by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var launchedRoomId by rememberSaveable { mutableStateOf<String?>(null) }

    suspend fun refreshDashboard(activeToken: String, syncLocalProfile: Boolean = true) {
        val remoteMe = SocialApiClient.me(activeToken)
        val syncedMe = if (syncLocalProfile && (
                remoteMe.nickname != localNickname ||
                    remoteMe.avatarKey != localAvatarKey ||
                    remoteMe.level != playerLevel
                )
        ) {
            SocialApiClient.updateProfile(
                token = activeToken,
                nickname = localNickname,
                avatarKey = localAvatarKey,
                level = playerLevel.coerceAtLeast(1)
            )
        } else {
            remoteMe
        }
        me = syncedMe
        friends = SocialApiClient.friends(activeToken)
        requests = SocialApiClient.friendRequests(activeToken)
        quizInvites = SocialApiClient.roomInvites(activeToken)
    }

    fun handleFailure(throwable: Throwable) {
        if (throwable is SocialApiException && throwable.statusCode == 401) {
            SocialTokenStore.clear(context)
            token = null
            me = null
            friends = emptyList()
            requests = emptyList()
            quizInvites = emptyList()
            currentRoom = null
            errorMessage = "Ta session a expiré. Reconnecte-toi."
        } else {
            errorMessage = throwable.message?.takeIf { it.isNotBlank() }
                ?: "Une erreur réseau est survenue."
        }
    }

    fun adoptSession(session: SocialSession) {
        SocialTokenStore.save(context, session.token)
        token = session.token
        me = session.user
        errorMessage = null
        infoMessage = "Connexion réussie"
    }

    LaunchedEffect(Unit) {
        googleClientId = runCatching { SocialApiClient.config().googleClientId }.getOrNull()
    }

    LaunchedEffect(token) {
        val activeToken = token ?: return@LaunchedEffect
        busy = true
        runCatching { refreshDashboard(activeToken) }
            .onFailure(::handleFailure)
        busy = false
    }

    LaunchedEffect(currentRoom?.id, token) {
        val activeToken = token ?: return@LaunchedEffect
        val roomId = currentRoom?.id ?: return@LaunchedEffect
        while (currentRoom?.id == roomId) {
            delay(1_000)
            runCatching { SocialApiClient.room(activeToken, roomId) }
                .onSuccess { currentRoom = it }
                .onFailure { throwable ->
                    if (throwable is SocialApiException && throwable.statusCode == 404) {
                        currentRoom = null
                    }
                }
        }
    }

    LaunchedEffect(currentRoom?.status, currentRoom?.startsAt, currentRoom?.serverNow) {
        val room = currentRoom ?: return@LaunchedEffect
        val activeToken = token ?: return@LaunchedEffect
        if (room.id == launchedRoomId) return@LaunchedEffect
        if (room.status != "countdown" && room.status != "active") return@LaunchedEffect
        val waitMs = synchronizedStartDelayMs(room)
        launchedRoomId = room.id
        if (waitMs > 0) delay(waitMs)
        val latest = runCatching { SocialApiClient.room(activeToken, room.id) }.getOrDefault(room)
        sharedQuizViewModel.begin(latest, activeToken)
        onSharedQuizStart()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SocialBg, Color(0xFF07132C), Color(0xFF150A2A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SocialHeader(onBack)

        errorMessage?.let {
            SocialNotice(it, SocialRed) { errorMessage = null }
        }
        infoMessage?.let {
            SocialNotice(it, SocialGreen) { infoMessage = null }
        }

        if (token == null) {
            SocialAuthPanel(
                busy = busy,
                googleEnabled = !googleClientId.isNullOrBlank(),
                onLogin = { email, password ->
                    scope.launch {
                        busy = true
                        errorMessage = null
                        runCatching { SocialApiClient.login(email, password) }
                            .onSuccess(::adoptSession)
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onRegister = { email, password ->
                    scope.launch {
                        busy = true
                        errorMessage = null
                        runCatching {
                            SocialApiClient.register(
                                email = email,
                                password = password,
                                nickname = localNickname,
                                avatarKey = localAvatarKey,
                                level = playerLevel.coerceAtLeast(1)
                            )
                        }.onSuccess(::adoptSession)
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onGoogle = {
                    scope.launch {
                        busy = true
                        errorMessage = null
                        val clientId = googleClientId
                        if (clientId.isNullOrBlank()) {
                            errorMessage = "La connexion Google doit encore être activée sur le serveur."
                        } else {
                            runCatching {
                                val idToken = GoogleAccountSignIn.getIdToken(context, clientId)
                                SocialApiClient.googleLogin(
                                    idToken = idToken,
                                    nickname = localNickname,
                                    avatarKey = localAvatarKey,
                                    level = playerLevel.coerceAtLeast(1)
                                )
                            }.onSuccess(::adoptSession)
                                .onFailure { throwable ->
                                    if (throwable is SocialApiException) handleFailure(throwable)
                                    else errorMessage = "Connexion Google annulée ou indisponible."
                                }
                        }
                        busy = false
                    }
                }
            )
            if (googleClientId.isNullOrBlank()) {
                Text(
                    "La connexion e-mail est prête. Le bouton Google s'activera automatiquement dès que l'identifiant OAuth sera configuré sur le serveur.",
                    color = SocialMuted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
            return@Column
        }

        val activeToken = token ?: return@Column
        val currentUser = me
        if (currentUser == null) {
            SocialLoadingCard()
            return@Column
        }

        SocialAccountCard(currentUser)

        currentRoom?.let { room ->
            SocialLobbyCard(
                room = room,
                me = currentUser,
                busy = busy,
                onReady = { ready ->
                    scope.launch {
                        busy = true
                        runCatching { SocialApiClient.setReady(activeToken, room.id, ready) }
                            .onSuccess { currentRoom = it }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onStart = {
                    scope.launch {
                        busy = true
                        runCatching { SocialApiClient.startRoom(activeToken, room.id) }
                            .onSuccess { currentRoom = it }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onClose = { currentRoom = null }
            )
        }

        SocialSectionTitle("AJOUTER UN AMI")
        var friendCode by rememberSaveable { mutableStateOf("") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialField(
                value = friendCode,
                onValueChange = { friendCode = it.uppercase().take(16) },
                label = "Code ami",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        runCatching { SocialApiClient.addFriend(activeToken, friendCode) }
                            .onSuccess {
                                friendCode = ""
                                infoMessage = "Demande d'ami envoyée"
                                runCatching { refreshDashboard(activeToken, syncLocalProfile = false) }
                            }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                enabled = friendCode.length >= 6 && !busy,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SocialBlue)
            ) {
                Text("AJOUTER", fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }

        if (requests.isNotEmpty()) {
            SocialSectionTitle("DEMANDES D'AMIS · ${requests.size}")
            requests.forEach { request ->
                SocialPlayerCard(
                    user = request.from,
                    action = "ACCEPTER",
                    actionEnabled = !busy,
                    onAction = {
                        scope.launch {
                            busy = true
                            runCatching { SocialApiClient.acceptFriendRequest(activeToken, request.id) }
                                .onSuccess {
                                    infoMessage = "${request.from.nickname} est maintenant ton ami"
                                    runCatching { refreshDashboard(activeToken, syncLocalProfile = false) }
                                }
                                .onFailure(::handleFailure)
                            busy = false
                        }
                    }
                )
            }
        }

        if (quizInvites.isNotEmpty()) {
            SocialSectionTitle("INVITATIONS DE QUIZ · ${quizInvites.size}")
            quizInvites.forEach { invite ->
                SocialPlayerCard(
                    user = invite.from,
                    subtitle = "t'invite à jouer",
                    action = "REJOINDRE",
                    actionEnabled = !busy && currentRoom == null,
                    onAction = {
                        scope.launch {
                            busy = true
                            runCatching { SocialApiClient.acceptRoomInvite(activeToken, invite.id) }
                                .onSuccess { room ->
                                    currentRoom = room
                                    quizInvites = quizInvites.filterNot { it.id == invite.id }
                                    infoMessage = "Salon rejoint · indique quand tu es prêt"
                                }
                                .onFailure(::handleFailure)
                            busy = false
                        }
                    }
                )
            }
        }

        SocialSectionTitle("MES AMIS · ${friends.size}")
        if (friends.isEmpty()) {
            SocialEmptyCard("Ajoute un ami avec son code CyberQuiz pour voir son niveau, son avatar et jouer avec lui.")
        } else {
            friends.forEach { friend ->
                SocialPlayerCard(
                    user = friend,
                    action = "JOUER",
                    actionEnabled = !busy && currentRoom == null,
                    onAction = {
                        scope.launch {
                            busy = true
                            errorMessage = null
                            runCatching {
                                val questionIds = sharedQuizViewModel.prepareQuestionIds(10)
                                if (questionIds.isEmpty()) error("Aucune question disponible")
                                SocialApiClient.createRoom(
                                    token = activeToken,
                                    inviteeIds = listOf(friend.id),
                                    questionIds = questionIds,
                                    mode = "RANDOM",
                                    categories = emptyList()
                                )
                            }.onSuccess { room ->
                                currentRoom = room
                                infoMessage = "Invitation de quiz envoyée à ${friend.nickname}"
                            }.onFailure(::handleFailure)
                            busy = false
                        }
                    }
                )
            }
        }

        TextButton(
            onClick = {
                scope.launch { GoogleAccountSignIn.clearState(context) }
                SocialTokenStore.clear(context)
                token = null
                me = null
                friends = emptyList()
                requests = emptyList()
                quizInvites = emptyList()
                currentRoom = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SE DÉCONNECTER", color = SocialRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}

internal fun synchronizedStartDelayMs(room: SocialQuizRoom): Long {
    val startsAt = room.startsAt ?: return 0L
    val serverNow = room.serverNow ?: return 0L
    return runCatching {
        val start = OffsetDateTime.parse(startsAt).toInstant()
        val now = OffsetDateTime.parse(serverNow).toInstant()
        Duration.between(now, start).toMillis().coerceAtLeast(0L)
    }.getOrDefault(0L)
}

@Composable
private fun SocialHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFF111C34), RoundedCornerShape(12.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = SocialCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("COMPTE & AMIS", color = SocialCyan, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Black)
            Text("CyberSquad", color = SocialText, fontSize = 23.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SocialAuthPanel(
    busy: Boolean,
    googleEnabled: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogle: () -> Unit
) {
    var registerMode by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SocialCard, RoundedCornerShape(24.dp))
            .border(1.2.dp, SocialBorder, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (registerMode) "Créer ton compte" else "Connexion",
            color = SocialText,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            if (registerMode) "Retrouve ton profil et joue avec tes amis." else "Accède à tes amis et à tes parties synchronisées.",
            color = SocialMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
        SocialField(
            value = email,
            onValueChange = { email = it.take(254) },
            label = "E-mail",
            keyboardType = KeyboardType.Email
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(128) },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(15.dp),
            colors = socialTextFieldColors()
        )
        if (registerMode) {
            Text("10 caractères minimum.", color = SocialMuted, fontSize = 9.sp)
        }
        Button(
            onClick = {
                if (registerMode) onRegister(email.trim(), password) else onLogin(email.trim(), password)
            },
            enabled = !busy && email.contains("@") && password.length >= if (registerMode) 10 else 1,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SocialPurple)
        ) {
            Text(
                if (registerMode) "CRÉER MON COMPTE" else "SE CONNECTER",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
        Button(
            onClick = onGoogle,
            enabled = !busy && googleEnabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1F1F1F),
                disabledContainerColor = Color(0xFF222B3C),
                disabledContentColor = SocialMuted
            )
        ) {
            Text("G  CONTINUER AVEC GOOGLE", fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
        TextButton(
            onClick = { registerMode = !registerMode },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (registerMode) "J'AI DÉJÀ UN COMPTE" else "CRÉER UN COMPTE PAR E-MAIL",
                color = SocialCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SocialAccountCard(user: SocialUser) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF11143C), Color(0xFF071A32))),
                RoundedCornerShape(22.dp)
            )
            .border(1.2.dp, SocialBlue.copy(alpha = .72f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatarButton(
                style = playerAvatarFromStorage(user.avatarKey),
                onClick = {},
                size = 64.dp,
                syncWithStoredSelection = false,
                syncBannerWithStoredSelection = false,
                showEditBadge = false
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(user.nickname, color = SocialText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("Niveau ${user.level}", color = SocialCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                user.email?.let { Text(it, color = SocialMuted, fontSize = 9.sp) }
            }
            Text("●", color = SocialGreen, fontSize = 14.sp)
        }
        user.friendCode?.let { code ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF071020), RoundedCornerShape(14.dp))
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("TON CODE AMI", color = SocialMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(code, color = SocialGold, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
                Text("À PARTAGER", color = SocialCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SocialLobbyCard(
    room: SocialQuizRoom,
    me: SocialUser,
    busy: Boolean,
    onReady: (Boolean) -> Unit,
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    val isHost = room.hostUserId == me.id
    val mine = room.currentMember(me.id)
    val countdown = if (room.status == "countdown") (synchronizedStartDelayMs(room) / 1000L + 1L).coerceAtLeast(1L) else null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11102E), RoundedCornerShape(22.dp))
            .border(1.2.dp, SocialPurple.copy(alpha = .75f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SALON DE QUIZ", color = SocialPurple, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                Text(
                    when (room.status) {
                        "countdown" -> "Départ dans ${countdown ?: 1}s"
                        "active" -> "La partie démarre…"
                        else -> "En attente des joueurs"
                    },
                    color = SocialText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("${room.questionIds.size} Q", color = SocialCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        room.members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SocialCardAlt, RoundedCornerShape(13.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatarButton(
                    style = playerAvatarFromStorage(member.user.avatarKey),
                    onClick = {},
                    size = 38.dp,
                    syncWithStoredSelection = false,
                    syncBannerWithStoredSelection = false,
                    showEditBadge = false
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(member.user.nickname, color = SocialText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Niveau ${member.user.level}", color = SocialMuted, fontSize = 9.sp)
                }
                Text(
                    if (member.ready) "PRÊT ✓" else "PAS PRÊT",
                    color = if (member.ready) SocialGreen else SocialMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        if (room.status == "lobby") {
            if (isHost) {
                Button(
                    onClick = onStart,
                    enabled = room.allReady() && !busy,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SocialPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("LANCER LE QUIZ EN MÊME TEMPS", fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                }
                if (room.members.size < 2) {
                    Text("L'ami invité doit d'abord rejoindre le salon.", color = SocialMuted, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Button(
                    onClick = { onReady(!(mine?.ready ?: false)) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (mine?.ready == true) SocialGreen else SocialBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (mine?.ready == true) "JE NE SUIS PLUS PRÊT" else "JE SUIS PRÊT", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("FERMER CE SALON", color = SocialMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun SocialPlayerCard(
    user: SocialUser,
    subtitle: String? = null,
    action: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SocialCard, RoundedCornerShape(17.dp))
            .border(1.dp, SocialBorder, RoundedCornerShape(17.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatarButton(
            style = playerAvatarFromStorage(user.avatarKey),
            onClick = {},
            size = 48.dp,
            syncWithStoredSelection = false,
            syncBannerWithStoredSelection = false,
            showEditBadge = false
        )
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(user.nickname, color = SocialText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle ?: "Niveau ${user.level}", color = SocialMuted, fontSize = 9.5.sp)
        }
        Button(
            onClick = onAction,
            enabled = actionEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SocialBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 11.dp, vertical = 8.dp)
        ) {
            Text(action, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SocialField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(15.dp),
        colors = socialTextFieldColors()
    )
}

@Composable
private fun socialTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = SocialText,
    unfocusedTextColor = SocialText,
    focusedContainerColor = Color(0xFF071326),
    unfocusedContainerColor = Color(0xFF071326),
    focusedIndicatorColor = SocialCyan,
    unfocusedIndicatorColor = SocialBorder,
    cursorColor = SocialCyan,
    focusedLabelColor = SocialCyan,
    unfocusedLabelColor = SocialMuted
)

@Composable
private fun SocialSectionTitle(text: String) {
    Text(text, color = SocialBlue, fontSize = 9.5.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
}

@Composable
private fun SocialNotice(message: String, accent: Color, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .10f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = .65f), RoundedCornerShape(14.dp))
            .clickable(onClick = onDismiss)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = accent, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("×", color = accent, fontSize = 18.sp)
    }
}

@Composable
private fun SocialLoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SocialCard, RoundedCornerShape(18.dp))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Connexion à CyberSquad…", color = SocialMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SocialEmptyCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SocialCard, RoundedCornerShape(17.dp))
            .border(1.dp, SocialBorder.copy(alpha = .7f), RoundedCornerShape(17.dp))
            .padding(15.dp)
    ) {
        Text(message, color = SocialMuted, fontSize = 10.5.sp, lineHeight = 16.sp)
    }
}
