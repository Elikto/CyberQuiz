package com.example.cyberquiz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.cyberquiz.contact.CyberQuizContactQueue
import com.example.cyberquiz.ui.theme.CyberBackground

private const val CONTACT_EMAIL = "elikto@proton.me"
private const val MAX_CONTACT_MESSAGE_CHARS = 3000

private val ContactPurple = Color(0xFFD652FF)
private val ContactBlue = Color(0xFF19BFFF)
private val ContactCyan = Color(0xFF19F2E5)
private val ContactGreen = Color(0xFF38E69A)
private val ContactOrange = Color(0xFFFFB84A)
private val ContactText = Color(0xFFF5F7FF)
private val ContactMuted = Color(0xFF9FAED3)
private val ContactBorder = Color(0xFF244777)

private data class ContactReason(
    val code: String,
    val label: String,
    val subject: String
)

private val contactReasons = listOf(
    ContactReason("improvement", "Proposer une amélioration", "[CyberQuiz] Proposition d'amélioration"),
    ContactReason("bug", "Signaler un bug", "[CyberQuiz] Signalement de bug"),
    ContactReason("update", "Problème de mise à jour", "[CyberQuiz] Problème de mise à jour"),
    ContactReason("content", "Question sur un quiz / contenu", "[CyberQuiz] Question sur un quiz ou contenu"),
    ContactReason("support", "Aide / support", "[CyberQuiz] Demande d'aide"),
    ContactReason("other", "Autre", "[CyberQuiz] Contact")
)

@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedReasonIndex by rememberSaveable { mutableStateOf(0) }
    var reasonMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }
    var urgent by rememberSaveable { mutableStateOf(false) }
    var sendError by rememberSaveable { mutableStateOf<String?>(null) }
    var showSentConfirmation by rememberSaveable { mutableStateOf(false) }
    val selectedReason = contactReasons[selectedReasonIndex]
    val canSend = message.isNotBlank()

    if (showSentConfirmation) {
        BackHandler { showSentConfirmation = false }
        ContactSentConfirmation(
            onBack = onBack,
            onAnotherMessage = { showSentConfirmation = false }
        )
        return
    }

    BackHandler(onBack = onBack)

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
        ContactHeader(onBack)

        ContactSectionLabel("OBJET")

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF081329), RoundedCornerShape(18.dp))
                    .border(1.dp, ContactBorder, RoundedCornerShape(18.dp))
                    .clickable { reasonMenuExpanded = true }
                    .padding(horizontal = 15.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .background(ContactPurple.copy(alpha = .13f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✉", color = ContactPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Motif", color = ContactMuted, fontSize = 10.sp)
                    Text(
                        selectedReason.label,
                        color = ContactText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("⌄", color = ContactCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            DropdownMenu(
                expanded = reasonMenuExpanded,
                onDismissRequest = { reasonMenuExpanded = false },
                modifier = Modifier
                    .background(Color(0xFF0A1630))
                    .fillMaxWidth(.92f)
            ) {
                contactReasons.forEachIndexed { index, reason ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                reason.label,
                                color = if (index == selectedReasonIndex) ContactCyan else ContactText,
                                fontWeight = if (index == selectedReasonIndex) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedReasonIndex = index
                            reasonMenuExpanded = false
                            sendError = null
                        }
                    )
                }
            }
        }

        Text(
            "Objet : ${selectedReason.subject}",
            color = ContactMuted,
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        ContactSectionLabel("MESSAGE")

        OutlinedTextField(
            value = message,
            onValueChange = { newValue ->
                if (newValue.length <= MAX_CONTACT_MESSAGE_CHARS) {
                    message = newValue
                    sendError = null
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Décris ta demande avec le plus de détails possible…",
                    color = ContactMuted
                )
            },
            minLines = 8,
            maxLines = 12,
            shape = RoundedCornerShape(18.dp),
            supportingText = {
                Text(
                    "${message.length} / $MAX_CONTACT_MESSAGE_CHARS caractères",
                    color = ContactMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = ContactText,
                unfocusedTextColor = ContactText,
                focusedContainerColor = Color(0xFF081329),
                unfocusedContainerColor = Color(0xFF081329),
                focusedIndicatorColor = ContactCyan,
                unfocusedIndicatorColor = ContactBorder,
                cursorColor = ContactCyan
            )
        )

        ContactSectionLabel("PRIORITÉ")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (urgent) ContactOrange.copy(alpha = .10f) else Color(0xFF081329),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    if (urgent) ContactOrange.copy(alpha = .55f) else ContactBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable {
                    urgent = !urgent
                    sendError = null
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = urgent,
                onCheckedChange = {
                    urgent = it
                    sendError = null
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = ContactOrange,
                    uncheckedColor = ContactMuted,
                    checkmarkColor = Color(0xFF111111)
                )
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Urgent",
                    color = if (urgent) ContactOrange else ContactText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Si vous cochez cette case, votre demande passera en priorité.",
                    color = ContactMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )
            }
        }

        sendError?.let { error ->
            ContactStatusMessage(
                text = error,
                accent = ContactOrange
            )
        }

        Button(
            enabled = canSend,
            onClick = {
                sendError = null
                val pendingMessage = message.trim()
                val pendingReason = selectedReason.code

                CyberQuizContactQueue.enqueue(
                    context = context,
                    reason = pendingReason,
                    message = pendingMessage,
                    urgent = urgent
                ).fold(
                    onSuccess = {
                        message = ""
                        urgent = false
                        showSentConfirmation = true
                    },
                    onFailure = { error ->
                        sendError = error.message ?: "Le message n'a pas pu être préparé pour l'envoi."
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ContactPurple,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF202A42),
                disabledContentColor = ContactMuted
            )
        ) {
            Text(
                "ENVOYER",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.3.sp
            )
        }

        Text(
            "Destinataire : $CONTACT_EMAIL",
            color = ContactMuted,
            fontSize = 10.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun ContactSentConfirmation(
    onBack: () -> Unit,
    onAnotherMessage: () -> Unit
) {
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF020610), Color(0xFF071022), CyberBackground, Color(0xFF030712))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = animateIn,
            enter = fadeIn() + scaleIn(initialScale = .88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF0C1B32), Color(0xFF071329))),
                        RoundedCornerShape(26.dp)
                    )
                    .border(1.4.dp, ContactGreen.copy(alpha = .65f), RoundedCornerShape(26.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(ContactGreen.copy(alpha = .13f), CircleShape)
                        .border(1.5.dp, ContactGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = ContactGreen, fontSize = 38.sp, fontWeight = FontWeight.Black)
                }

                Text(
                    "Message envoyé",
                    color = ContactText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Merci pour votre message. Votre demande a bien été prise en charge.",
                    color = ContactMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF14233D),
                            contentColor = ContactText
                        )
                    ) {
                        Text("RETOUR", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = onAnotherMessage,
                        modifier = Modifier.weight(1.4f).height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ContactPurple,
                            contentColor = Color.White
                        )
                    ) {
                        Text("ÉCRIRE UN AUTRE MESSAGE", fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactStatusMessage(text: String, accent: Color) {
    Text(
        text,
        color = accent,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .08f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(12.dp))
            .padding(11.dp)
    )
}

@Composable
private fun ContactHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = ContactText, fontSize = 30.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Nous contacter", color = ContactText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(
                "FORMULAIRE CYBERQUIZ",
                color = ContactMuted,
                fontSize = 9.sp,
                letterSpacing = 1.7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ContactSectionLabel(text: String) {
    Text(
        text,
        color = ContactBlue,
        fontSize = 10.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Bold
    )
}
