package com.example.cyberquiz.ui.screens

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.ui.theme.CyberBlue
import com.example.cyberquiz.ui.theme.CyberMuted
import com.example.cyberquiz.ui.theme.CyberPurple
import com.example.cyberquiz.ui.theme.CyberText

enum class QuizType(
    val label: String,
    val description: String,
    val symbol: String,
    val available: Boolean
) {
    CYBERSECURITY("Cybersécurité", "Sécurité numérique, réseaux, phishing et bonnes pratiques", "⌁", true),
    NUTRITION("Nutrition", "Alimentation, nutriments et équilibre alimentaire", "●", false),
    TAROT("Tarot", "Arcanes, symbolique et apprentissage des cartes", "✦", false),
    LITHOTHERAPY("Lithothérapie", "Pierres, minéraux et connaissances associées", "◆", false),
    GENERAL_KNOWLEDGE("Culture générale", "Sciences, histoire, géographie et connaissances variées", "◎", false)
}

@Composable
fun ProfileScreen(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val showClearDataDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF040814), Color(0xFF071020), Color(0xFF040814))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        ScreenHeader(title = "Profil", onBack = onBack)
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF762EFF), Color(0xFF0877D8))),
                        CircleShape
                    )
                    .border(2.dp, Color(0xFF62CBFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("CQ", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("Mon profil", color = CyberText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                "Personnalise l'univers de tes quiz",
                color = CyberMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(28.dp))
        Text("TYPE DE QUIZ", color = CyberBlue, fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        QuizType.entries.forEach { type ->
            QuizTypeCard(
                type = type,
                selected = type == selectedQuizType,
                onClick = { onQuizTypeSelected(type) }
            )
            Spacer(Modifier.height(12.dp))
        }

        Text(
            text = if (selectedQuizType.available) {
                "${selectedQuizType.label} est actuellement disponible."
            } else {
                "${selectedQuizType.label} est sélectionné. Sa banque de questions sera ajoutée prochainement."
            },
            color = if (selectedQuizType.available) Color(0xFF32E5A1) else Color(0xFFFFCC66),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1429), RoundedCornerShape(16.dp))
                .border(1.dp, CyberBlue.copy(alpha = .35f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF251016), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFFFF557A).copy(alpha = .65f), RoundedCornerShape(18.dp))
                .clickable { showClearDataDialog.value = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFFF557A).copy(alpha = .12f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⌫", color = Color(0xFFFF7A96), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Effacer les données enregistrées",
                    color = Color(0xFFFFA0B3),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Progression, historique, révisions et quiz en cours",
                    color = CyberMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Text("›", color = Color(0xFFFF7A96), fontSize = 24.sp)
        }
    }

    if (showClearDataDialog.value) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog.value = false },
            containerColor = Color(0xFF0B1429),
            title = {
                Text(
                    "Effacer les données ?",
                    color = CyberText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Cette action supprimera toute la progression, l'historique, les révisions et les quiz en cours. L'application sera réinitialisée.",
                    color = CyberMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog.value = false
                        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        activityManager?.clearApplicationUserData()
                    }
                ) {
                    Text("EFFACER", color = Color(0xFFFF557A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog.value = false }) {
                    Text("ANNULER", color = CyberBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun QuizTypeCard(type: QuizType, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) CyberPurple else Color(0xFF23406F)
    val start = if (selected) Color(0xFF24134C) else Color(0xFF091429)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(start, Color(0xFF071124))),
                RoundedCornerShape(20.dp)
            )
            .border(if (selected) 1.8.dp else 1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (selected) CyberPurple.copy(alpha = .18f) else Color(0xFF111F38), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(type.symbol, color = if (selected) Color(0xFFE18BFF) else CyberBlue, fontSize = 22.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(type.label, color = CyberText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                if (!type.available) {
                    Spacer(Modifier.width(8.dp))
                    Text("BIENTÔT", color = Color(0xFFFFC85C), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(type.description, color = CyberMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Text(if (selected) "✓" else "›", color = if (selected) Color(0xFF38E7B1) else CyberMuted, fontSize = 24.sp)
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF040814), Color(0xFF071020), Color(0xFF040814))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        ScreenHeader(title = "Paramètres", onBack = onBack)
        Spacer(Modifier.height(24.dp))

        SettingsSectionTitle("APPLICATION")
        SettingsItem(
            symbol = "CQ",
            title = "Version de CyberQuiz",
            subtitle = "Version ${BuildConfig.VERSION_NAME}",
            trailing = "${BuildConfig.VERSION_CODE}"
        )
        SettingsItem(
            symbol = "⌘",
            title = "Projet GitHub",
            subtitle = "Elikto / CyberQuiz",
            trailing = "›",
            onClick = { uriHandler.openUri("https://github.com/Elikto/CyberQuiz") }
        )

        Spacer(Modifier.height(18.dp))
        SettingsSectionTitle("AIDE & SOUTIEN")
        SettingsItem(
            symbol = "?",
            title = "FAQ",
            subtitle = "Questions fréquentes et aide",
            trailing = "BIENTÔT"
        )
        SettingsItem(
            symbol = "✉",
            title = "Nous contacter",
            subtitle = "Signaler un problème ou envoyer une suggestion",
            trailing = "BIENTÔT"
        )
        SettingsItem(
            symbol = "♥",
            title = "Soutenir CyberQuiz",
            subtitle = "Un lien de don pourra être ajouté ici",
            trailing = "BIENTÔT"
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "CyberQuiz est développé comme une application évolutive : de nouveaux univers de quiz et de nouvelles fonctions seront ajoutés progressivement.",
            color = CyberMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("‹", color = CyberText, fontSize = 34.sp)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, color = CyberText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(title, color = CyberBlue, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingsItem(
    symbol: String,
    title: String,
    subtitle: String,
    trailing: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color(0xFF091429), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF203B69), RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(CyberPurple.copy(alpha = .13f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = Color(0xFFB98AFF), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = CyberText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = CyberMuted, fontSize = 12.sp)
        }
        Text(trailing, color = if (trailing == "BIENTÔT") Color(0xFFFFC85C) else CyberBlue, fontSize = if (trailing == "BIENTÔT") 9.sp else 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuizUnavailableScreen(quizType: QuizType, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF040814), Color(0xFF071020))))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(quizType.symbol, color = CyberPurple, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(quizType.label, color = CyberText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "Cet univers est déjà prévu dans l'application, mais sa banque de questions n'est pas encore intégrée.",
            color = CyberMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onBack) {
            Text("Retour à l'accueil", color = CyberBlue, fontWeight = FontWeight.Bold)
        }
    }
}
