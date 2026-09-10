package com.example.cyberquiz.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.cyberquiz.social.ProgressSyncManager
import com.example.cyberquiz.social.SharedQuizViewModel
import com.example.cyberquiz.social.SocialApiClient
import com.example.cyberquiz.social.SocialApiException
import com.example.cyberquiz.social.SocialFriendRequest
import com.example.cyberquiz.social.SocialLifecycleApiClient
import com.example.cyberquiz.social.SocialQuizInvite
import com.example.cyberquiz.social.SocialQuizRoom
import com.example.cyberquiz.social.SocialSentFriendRequest
import com.example.cyberquiz.social.SocialSession
import com.example.cyberquiz.social.SocialTokenStore
import com.example.cyberquiz.social.SocialUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SquadBg = Color(0xFF040817)
private val SquadCard = Color(0xFF08142A)
private val SquadAlt = Color(0xFF0D1832)
private val SquadCyan = Color(0xFF20E7F2)
private val SquadBlue = Color(0xFF2B8CFF)
private val SquadPurple = Color(0xFFB64EFF)
private val SquadGreen = Color(0xFF39E79B)
private val SquadRed = Color(0xFFFF5F7D)
private val SquadGold = Color(0xFFFFC863)
private val SquadText = Color(0xFFF5F7FF)
private val SquadMuted = Color(0xFF9EACCB)
private val SquadBorder = Color(0xFF284A78)

private const val PROFILE_PREFS = "cyberquiz_preferences"
private const val NICKNAME_KEY = "player_nickname"

@Composable
internal fun SocialHubScreenV2(
    playerLevel: Int,
    sharedQuizViewModel: SharedQuizViewModel,
    onSharedQuizStart: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localNickname = remember(context) {
        context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
            .getString(NICKNAME_KEY, "Joueur Cyber")
            ?.takeIf(String::isNotBlank)
            ?: "Joueur Cyber"
    }
    val localAvatarKey = storedPlayerAvatar(context).storageKey

    var token by rememberSaveable { mutableStateOf(SocialTokenStore.load(context)) }
    var me by remember { mutableStateOf<SocialUser?>(null) }
    var friends by remember { mutableStateOf<List<SocialUser>>(emptyList()) }
    var incoming by remember { mutableStateOf<List<SocialFriendRequest>>(emptyList()) }
    var sent by remember { mutableStateOf<List<SocialSentFriendRequest>>(emptyList()) }
    var invites by remember { mutableStateOf<List<SocialQuizInvite>>(emptyList()) }
    var room by remember { mutableStateOf<SocialQuizRoom?>(null) }
    var googleClientId by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var launchedRoomId by rememberSaveable { mutableStateOf<String?>(null) }
    var removeFriend by remember { mutableStateOf<SocialUser?>(null) }
    var deleteAccount by remember { mutableStateOf(false) }
    var changePassword by remember { mutableStateOf(false) }
    var rotateCode by remember { mutableStateOf(false) }

    fun clearSession(message: String? = null) {
        SocialTokenStore.clear(context)
        token = null
        me = null
        friends = emptyList()
        incoming = emptyList()
        sent = emptyList()
        invites = emptyList()
        room = null
        error = message
    }

    fun handleFailure(throwable: Throwable) {
        if (throwable is SocialApiException && throwable.statusCode == 401) {
            clearSession("Ta session a expiré. Reconnecte-toi.")
        } else {
            error = throwable.message?.takeIf(String::isNotBlank)
                ?: "Une erreur réseau est survenue."
        }
    }

    suspend fun refresh(activeToken: String, syncProfile: Boolean = true) {
        val remote = SocialApiClient.me(activeToken)
        me = if (
            syncProfile && (
                remote.nickname != localNickname ||
                    remote.avatarKey != localAvatarKey ||
                    remote.level != playerLevel
                )
        ) {
            SocialApiClient.updateProfile(
                activeToken,
                localNickname,
                localAvatarKey,
                playerLevel.coerceAtLeast(1)
            )
        } else {
            remote
        }
        friends = SocialApiClient.friends(activeToken)
        incoming = SocialApiClient.friendRequests(activeToken)
        sent = SocialLifecycleApiClient.sentFriendRequests(activeToken)
        invites = SocialApiClient.roomInvites(activeToken)
        room = SocialLifecycleApiClient.activeRoom(activeToken)
    }

    fun adopt(session: SocialSession) {
        SocialTokenStore.save(context, session.token)
        token = session.token
        me = session.user
        error = null
        info = "Connexion réussie · synchronisation du compte lancée"
        ProgressSyncManager.request(context, delayMs = 100L)
    }

    LaunchedEffect(Unit) {
        googleClientId = runCatching { SocialApiClient.config().googleClientId }.getOrNull()
    }

    LaunchedEffect(token) {
        val activeToken = token ?: return@LaunchedEffect
        busy = true
        runCatching { refresh(activeToken) }.onFailure(::handleFailure)
        busy = false
    }

    LaunchedEffect(room?.id, token) {
        val activeToken = token ?: return@LaunchedEffect
        val roomId = room?.id ?: return@LaunchedEffect
        while (room?.id == roomId) {
            delay(1_000L)
            runCatching { SocialApiClient.room(activeToken, roomId) }
                .onSuccess { latest ->
                    room = if (latest.status == "finished") null else latest
                }
                .onFailure { throwable ->
                    if (throwable is SocialApiException && throwable.statusCode == 404) room = null
                }
        }
    }

    LaunchedEffect(room?.status, room?.startsAt, room?.serverNow) {
        val current = room ?: return@LaunchedEffect
        val activeToken = token ?: return@LaunchedEffect
        if (current.id == launchedRoomId) return@LaunchedEffect
        if (current.status !in setOf("countdown", "active")) return@LaunchedEffect
        val wait = synchronizedStartDelayMs(current)
        launchedRoomId = current.id
        if (wait > 0L) delay(wait)
        val latest = runCatching { SocialApiClient.room(activeToken, current.id) }.getOrDefault(current)
        sharedQuizViewModel.begin(latest, activeToken)
        onSharedQuizStart()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SquadBg, Color(0xFF07132C), Color(0xFF150A2A))))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SquadHeader(onBack)
        error?.let { SquadNotice(it, SquadRed) { error = null } }
        info?.let { SquadNotice(it, SquadGreen) { info = null } }

        if (token == null) {
            SquadAuthPanel(
                busy = busy,
                googleEnabled = !googleClientId.isNullOrBlank(),
                onLogin = { email, password ->
                    scope.launch {
                        busy = true
                        runCatching { SocialApiClient.login(email, password) }
                            .onSuccess(::adopt)
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onRegister = { email, password ->
                    scope.launch {
                        busy = true
                        runCatching {
                            SocialApiClient.register(
                                email,
                                password,
                                localNickname,
                                localAvatarKey,
                                playerLevel.coerceAtLeast(1)
                            )
                        }.onSuccess(::adopt).onFailure(::handleFailure)
                        busy = false
                    }
                },
                onGoogle = {
                    scope.launch {
                        busy = true
                        val clientId = googleClientId
                        if (clientId.isNullOrBlank()) {
                            error = "La connexion Google n'est pas encore disponible."
                        } else {
                            runCatching {
                                val idToken = GoogleAccountSignIn.getIdToken(context, clientId)
                                SocialApiClient.googleLogin(
                                    idToken,
                                    localNickname,
                                    localAvatarKey,
                                    playerLevel.coerceAtLeast(1)
                                )
                            }.onSuccess(::adopt).onFailure { throwable ->
                                if (throwable is SocialApiException) handleFailure(throwable)
                                else error = "Connexion Google annulée ou indisponible."
                            }
                        }
                        busy = false
                    }
                }
            )
            return@Column
        }

        val activeToken = token ?: return@Column
        val currentUser = me
        if (currentUser == null) {
            SquadEmpty("Connexion au compte…")
            return@Column
        }

        SquadAccountCard(
            user = currentUser,
            busy = busy,
            onRotateCode = { rotateCode = true },
            onChangePassword = { changePassword = true },
            onDeleteAccount = { deleteAccount = true }
        )

        room?.let { currentRoom ->
            SquadRoomCard(
                room = currentRoom,
                me = currentUser,
                busy = busy,
                onReady = { ready ->
                    scope.launch {
                        busy = true
                        runCatching { SocialApiClient.setReady(activeToken, currentRoom.id, ready) }
                            .onSuccess { room = it }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onStart = {
                    scope.launch {
                        busy = true
                        runCatching { SocialApiClient.startRoom(activeToken, currentRoom.id) }
                            .onSuccess { room = it }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                },
                onLeaveOrCancel = {
                    scope.launch {
                        busy = true
                        val action = if (currentRoom.hostUserId == currentUser.id) {
                            runCatching { SocialLifecycleApiClient.cancelRoom(activeToken, currentRoom.id) }
                        } else {
                            runCatching {
                                SocialLifecycleApiClient.leaveRoom(activeToken, currentRoom.id)
                                currentRoom
                            }
                        }
                        action.onSuccess {
                            room = null
                            info = if (currentRoom.hostUserId == currentUser.id) "Salon annulé" else "Salon quitté"
                        }.onFailure(::handleFailure)
                        busy = false
                    }
                }
            )
        }

        SquadSection("AJOUTER UN AMI")
        var friendCode by rememberSaveable { mutableStateOf("") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SquadField(
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
                                info = "Demande d'ami envoyée"
                                runCatching { refresh(activeToken, false) }
                            }.onFailure(::handleFailure)
                        busy = false
                    }
                },
                enabled = friendCode.length >= 6 && !busy,
                colors = ButtonDefaults.buttonColors(containerColor = SquadBlue),
                shape = RoundedCornerShape(13.dp)
            ) { Text("AJOUTER", fontSize = 9.sp, fontWeight = FontWeight.Black) }
        }

        if (incoming.isNotEmpty()) {
            SquadSection("DEMANDES REÇUES · ${incoming.size}")
            incoming.forEach { request ->
                SquadPlayerCard(
                    user = request.from,
                    primary = "ACCEPTER",
                    secondary = "REFUSER",
                    enabled = !busy,
                    onPrimary = {
                        scope.launch {
                            busy = true
                            runCatching { SocialApiClient.acceptFriendRequest(activeToken, request.id) }
                                .onSuccess {
                                    info = "${request.from.nickname} est maintenant ton ami"
                                    runCatching { refresh(activeToken, false) }
                                }.onFailure(::handleFailure)
                            busy = false
                        }
                    },
                    onSecondary = {
                        scope.launch {
                            busy = true
                            runCatching { SocialLifecycleApiClient.rejectFriendRequest(activeToken, request.id) }
                                .onSuccess {
                                    incoming = incoming.filterNot { it.id == request.id }
                                    info = "Demande refusée"
                                }.onFailure(::handleFailure)
                            busy = false
                        }
                    }
                )
            }
        }

        if (sent.isNotEmpty()) {
            SquadSection("DEMANDES ENVOYÉES · ${sent.size}")
            sent.forEach { request ->
                SquadPlayerCard(
                    user = request.to,
                    subtitle = "En attente de réponse",
                    primary = "ANNULER",
                    enabled = !busy,
                    onPrimary = {
                        scope.launch {
                            busy = true
                            runCatching { SocialLifecycleApiClient.cancelSentFriendRequest(activeToken, request.id) }
                                .onSuccess { sent = sent.filterNot { it.id == request.id } }
                                .onFailure(::handleFailure)
                            busy = false
                        }
                    }
                )
            }
        }

        if (invites.isNotEmpty()) {
            SquadSection("INVITATIONS DE QUIZ · ${invites.size}")
            invites.forEach { invite ->
                SquadPlayerCard(
                    user = invite.from,
                    subtitle = "t'invite à jouer",
                    primary = "REJOINDRE",
                    secondary = "REFUSER",
                    enabled = !busy && room == null,
                    onPrimary = {
                        scope.launch {
                            busy = true
                            runCatching { SocialApiClient.acceptRoomInvite(activeToken, invite.id) }
                                .onSuccess { joined ->
                                    room = joined
                                    invites = invites.filterNot { it.id == invite.id }
                                    info = "Salon rejoint · indique quand tu es prêt"
                                }.onFailure(::handleFailure)
                            busy = false
                        }
                    },
                    onSecondary = {
                        scope.launch {
                            busy = true
                            runCatching { SocialLifecycleApiClient.declineRoomInvite(activeToken, invite.id) }
                                .onSuccess { invites = invites.filterNot { it.id == invite.id } }
                                .onFailure(::handleFailure)
                            busy = false
                        }
                    }
                )
            }
        }

        SquadSection("MES AMIS · ${friends.size}")
        if (friends.isEmpty()) {
            SquadEmpty("Ajoute un ami avec son code CyberQuiz pour jouer avec lui.")
        } else {
            friends.forEach { friend ->
                SquadPlayerCard(
                    user = friend,
                    primary = "JOUER",
                    secondary = "RETIRER",
                    enabled = !busy && room == null,
                    onPrimary = {
                        scope.launch {
                            busy = true
                            runCatching {
                                val questionIds = sharedQuizViewModel.prepareQuestionIds(10)
                                if (questionIds.isEmpty()) error("Aucune question disponible")
                                SocialApiClient.createRoom(
                                    activeToken,
                                    listOf(friend.id),
                                    questionIds,
                                    "RANDOM",
                                    emptyList()
                                )
                            }.onSuccess { created ->
                                room = created
                                info = "Invitation envoyée à ${friend.nickname}"
                            }.onFailure(::handleFailure)
                            busy = false
                        }
                    },
                    onSecondary = { removeFriend = friend }
                )
            }
        }

        TextButton(
            onClick = {
                scope.launch { GoogleAccountSignIn.clearState(context) }
                clearSession()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SE DÉCONNECTER", color = SquadRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }

    removeFriend?.let { friend ->
        AlertDialog(
            onDismissRequest = { removeFriend = null },
            containerColor = SquadCard,
            title = { Text("Retirer ${friend.nickname} ?", color = SquadText, fontWeight = FontWeight.Black) },
            text = { Text("Vous ne pourrez plus vous inviter à un quiz tant que vous ne redevenez pas amis.", color = SquadMuted) },
            confirmButton = {
                TextButton(onClick = {
                    removeFriend = null
                    val activeToken = token ?: return@TextButton
                    scope.launch {
                        busy = true
                        runCatching { SocialLifecycleApiClient.removeFriend(activeToken, friend.id) }
                            .onSuccess { friends = friends.filterNot { it.id == friend.id } }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                }) { Text("RETIRER", color = SquadRed, fontWeight = FontWeight.Black) }
            },
            dismissButton = { TextButton(onClick = { removeFriend = null }) { Text("ANNULER", color = SquadCyan) } }
        )
    }

    if (rotateCode) {
        AlertDialog(
            onDismissRequest = { rotateCode = false },
            containerColor = SquadCard,
            title = { Text("Changer ton code ami ?", color = SquadText, fontWeight = FontWeight.Black) },
            text = { Text("L'ancien code cessera immédiatement de fonctionner.", color = SquadMuted) },
            confirmButton = {
                TextButton(onClick = {
                    rotateCode = false
                    val activeToken = token ?: return@TextButton
                    scope.launch {
                        busy = true
                        runCatching { SocialLifecycleApiClient.rotateFriendCode(activeToken) }
                            .onSuccess { me = it; info = "Nouveau code ami généré" }
                            .onFailure(::handleFailure)
                        busy = false
                    }
                }) { Text("CHANGER", color = SquadGold, fontWeight = FontWeight.Black) }
            },
            dismissButton = { TextButton(onClick = { rotateCode = false }) { Text("ANNULER", color = SquadCyan) } }
        )
    }

    if (changePassword) {
        PasswordDialog(
            onDismiss = { changePassword = false },
            onSubmit = { current, next ->
                val activeToken = token ?: return@PasswordDialog
                scope.launch {
                    busy = true
                    runCatching { SocialLifecycleApiClient.changePassword(activeToken, current, next) }
                        .onSuccess { changePassword = false; info = "Mot de passe modifié" }
                        .onFailure(::handleFailure)
                    busy = false
                }
            }
        )
    }

    if (deleteAccount) {
        DeleteAccountDialog(
            onDismiss = { deleteAccount = false },
            onDelete = { password ->
                val activeToken = token ?: return@DeleteAccountDialog
                scope.launch {
                    busy = true
                    runCatching { SocialLifecycleApiClient.deleteAccount(activeToken, password) }
                        .onSuccess {
                            deleteAccount = false
                            scope.launch { GoogleAccountSignIn.clearState(context) }
                            clearSession("Le compte CyberQuiz a été supprimé.")
                        }.onFailure(::handleFailure)
                    busy = false
                }
            }
        )
    }
}

@Composable
private fun SquadHeader(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(38.dp).background(Color(0xFF111C34), RoundedCornerShape(12.dp)).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) { Text("‹", color = SquadCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("COMPTE & AMIS", color = SquadCyan, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Black)
            Text("CyberSquad", color = SquadText, fontSize = 23.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SquadAuthPanel(
    busy: Boolean,
    googleEnabled: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogle: () -> Unit
) {
    var register by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().background(SquadCard, RoundedCornerShape(24.dp)).border(1.dp, SquadBorder, RoundedCornerShape(24.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (register) "Créer ton compte" else "Connexion", color = SquadText, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Text("Ton compte sauvegarde ta progression et te permet de jouer avec tes amis.", color = SquadMuted, fontSize = 11.sp, lineHeight = 16.sp)
        SquadField(email, { email = it.take(254) }, "E-mail", keyboardType = KeyboardType.Email)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(128) },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        if (register) Text("10 caractères minimum.", color = SquadMuted, fontSize = 9.sp)
        Button(
            onClick = { if (register) onRegister(email.trim(), password) else onLogin(email.trim(), password) },
            enabled = !busy && email.contains('@') && password.length >= if (register) 10 else 1,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SquadPurple),
            shape = RoundedCornerShape(15.dp)
        ) { Text(if (register) "CRÉER MON COMPTE" else "SE CONNECTER", fontWeight = FontWeight.Black, fontSize = 11.sp) }
        Button(
            onClick = onGoogle,
            enabled = !busy && googleEnabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
            shape = RoundedCornerShape(15.dp)
        ) { Text("G  CONTINUER AVEC GOOGLE", fontWeight = FontWeight.Black, fontSize = 10.sp) }
        TextButton(onClick = { register = !register }, modifier = Modifier.fillMaxWidth()) {
            Text(if (register) "J'AI DÉJÀ UN COMPTE" else "CRÉER UN COMPTE PAR E-MAIL", color = SquadCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SquadAccountCard(
    user: SocialUser,
    busy: Boolean,
    onRotateCode: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF11143C), Color(0xFF071A32))), RoundedCornerShape(22.dp)).border(1.dp, SquadBlue.copy(alpha = .7f), RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatarButton(style = playerAvatarFromStorage(user.avatarKey), onClick = {}, size = 60.dp, syncWithStoredSelection = false, syncBannerWithStoredSelection = false, showEditBadge = false)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.nickname, color = SquadText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("Niveau ${user.level}", color = SquadCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                user.email?.let { Text(it, color = SquadMuted, fontSize = 9.sp) }
            }
            Text("●", color = SquadGreen)
        }
        user.friendCode?.let { code ->
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF071020), RoundedCornerShape(14.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("TON CODE AMI", color = SquadMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(code, color = SquadGold, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
                TextButton(onClick = onRotateCode, enabled = !busy) { Text("CHANGER", color = SquadCyan, fontSize = 8.sp, fontWeight = FontWeight.Black) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onChangePassword, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text("MOT DE PASSE", color = SquadCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDeleteAccount, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text("SUPPRIMER LE COMPTE", color = SquadRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SquadRoomCard(
    room: SocialQuizRoom,
    me: SocialUser,
    busy: Boolean,
    onReady: (Boolean) -> Unit,
    onStart: () -> Unit,
    onLeaveOrCancel: () -> Unit
) {
    val host = room.hostUserId == me.id
    val mine = room.currentMember(me.id)
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF11102E), RoundedCornerShape(22.dp)).border(1.dp, SquadPurple, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SALON ACTIF · ${room.questionIds.size} QUESTIONS", color = SquadPurple, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Text(
            when (room.status) {
                "countdown" -> "Départ synchronisé imminent"
                "active" -> "Partie en cours"
                else -> "En attente des joueurs"
            },
            color = SquadText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        room.members.forEach { member ->
            Row(modifier = Modifier.fillMaxWidth().background(SquadAlt, RoundedCornerShape(13.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(member.user.nickname, color = SquadText, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(if (member.ready) "PRÊT ✓" else "PAS PRÊT", color = if (member.ready) SquadGreen else SquadMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
        if (room.status == "lobby") {
            if (host) {
                Button(onClick = onStart, enabled = room.allReady() && !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SquadPurple)) {
                    Text("LANCER LE QUIZ", fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            } else {
                Button(onClick = { onReady(!(mine?.ready ?: false)) }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SquadBlue)) {
                    Text(if (mine?.ready == true) "JE NE SUIS PLUS PRÊT" else "JE SUIS PRÊT", fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
        }
        TextButton(onClick = onLeaveOrCancel, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (host) "ANNULER LE SALON" else "QUITTER LE SALON", color = SquadRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SquadPlayerCard(
    user: SocialUser,
    subtitle: String? = null,
    primary: String,
    secondary: String? = null,
    enabled: Boolean,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth().background(SquadCard, RoundedCornerShape(17.dp)).border(1.dp, SquadBorder, RoundedCornerShape(17.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatarButton(style = playerAvatarFromStorage(user.avatarKey), onClick = {}, size = 44.dp, syncWithStoredSelection = false, syncBannerWithStoredSelection = false, showEditBadge = false)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(user.nickname, color = SquadText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(subtitle ?: "Niveau ${user.level}", color = SquadMuted, fontSize = 9.5.sp)
            }
            Button(onClick = onPrimary, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = SquadBlue), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)) {
                Text(primary, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
        if (secondary != null && onSecondary != null) {
            TextButton(onClick = onSecondary, enabled = enabled, modifier = Modifier.align(Alignment.End)) {
                Text(secondary, color = SquadRed, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SquadField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = modifier, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType))
}

@Composable
private fun SquadSection(text: String) {
    Text(text, color = SquadCyan, fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Black)
}

@Composable
private fun SquadEmpty(message: String) {
    Text(message, color = SquadMuted, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.fillMaxWidth().background(SquadCard, RoundedCornerShape(16.dp)).padding(14.dp))
}

@Composable
private fun SquadNotice(message: String, accent: Color, onDismiss: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(accent.copy(alpha = .1f), RoundedCornerShape(14.dp)).border(1.dp, accent.copy(alpha = .6f), RoundedCornerShape(14.dp)).clickable(onClick = onDismiss).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(message, color = accent, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text("×", color = accent, fontSize = 18.sp)
    }
}

@Composable
private fun PasswordDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var current by rememberSaveable { mutableStateOf("") }
    var next by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SquadCard,
        title = { Text("Changer le mot de passe", color = SquadText, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(current, { current = it.take(128) }, label = { Text("Mot de passe actuel") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(next, { next = it.take(128) }, label = { Text("Nouveau mot de passe") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                Text("Le nouveau mot de passe doit contenir au moins 10 caractères.", color = SquadMuted, fontSize = 9.sp)
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(current, next) }, enabled = current.isNotBlank() && next.length >= 10) { Text("MODIFIER", color = SquadGreen, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANNULER", color = SquadCyan) } }
    )
}

@Composable
private fun DeleteAccountDialog(onDismiss: () -> Unit, onDelete: (String?) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    var typed by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SquadCard,
        title = { Text("Supprimer définitivement le compte ?", color = SquadRed, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Les données du compte, les amis et la sauvegarde cloud seront supprimés. Les données locales du téléphone ne sont pas effacées automatiquement.", color = SquadMuted, fontSize = 11.sp, lineHeight = 16.sp)
                OutlinedTextField(password, { password = it.take(128) }, label = { Text("Mot de passe actuel (si compte e-mail)") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(typed, { typed = it.take(9).uppercase() }, label = { Text("Tape SUPPRIMER") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onDelete(password.takeIf(String::isNotBlank)) }, enabled = typed == "SUPPRIMER") { Text("SUPPRIMER", color = SquadRed, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANNULER", color = SquadCyan) } }
    )
}
