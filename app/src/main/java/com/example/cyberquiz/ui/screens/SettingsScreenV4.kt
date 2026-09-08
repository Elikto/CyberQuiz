package com.example.cyberquiz.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.update.CyberQuizUpdateInfo
import com.example.cyberquiz.update.CyberQuizUpdateManager
import kotlinx.coroutines.launch

private val SettingsV4Purple = Color(0xFFD652FF)
private val SettingsV4Blue = Color(0xFF19BFFF)
private val SettingsV4Cyan = Color(0xFF19F2E5)
private val SettingsV4Green = Color(0xFF38E69A)
private val SettingsV4Orange = Color(0xFFFFB84A)
private val SettingsV4Text = Color(0xFFF5F7FF)
private val SettingsV4Muted = Color(0xFF9FAED3)
private val SettingsV4Border = Color(0xFF244777)

@Composable
fun SettingsScreenV4(
    onBack: () -> Unit,
    onVersionClick: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivityV4() }

    var showContactForm by rememberSaveable { mutableStateOf(false) }
    var showFaq by rememberSaveable { mutableStateOf(false) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var installingUpdate by remember { mutableStateOf(false) }
    var updateChecked by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<CyberQuizUpdateInfo?>(null) }
    var updateStatusText by remember { mutableStateOf<String?>(null) }

    if (showFaq) {
        FaqScreen(onBack = { showFaq = false })
        return
    }

    if (showContactForm) {
        ContactScreen(onBack = { showContactForm = false })
        return
    }

    suspend fun refreshUpdateStatus() {
        if (checkingUpdate || installingUpdate) return
        checkingUpdate = true
        updateStatusText = null
        availableUpdate = CyberQuizUpdateManager.checkForUpdate()
        updateChecked = true
        checkingUpdate = false
    }

    LaunchedEffect(Unit) {
        refreshUpdateStatus()
    }

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsV4Header(onBack)

        SettingsV4UpdateCard(
            checking = checkingUpdate,
            installing = installingUpdate,
            updateChecked = updateChecked,
            availableUpdate = availableUpdate,
            statusText = updateStatusText,
            onUpdateClick = {
                if (!checkingUpdate && !installingUpdate) {
                    val pendingUpdate = availableUpdate
                    if (pendingUpdate == null) {
                        scope.launch { refreshUpdateStatus() }
                    } else if (activity == null) {
                        updateStatusText = "Impossible d'ouvrir l'installation depuis cet écran."
                    } else {
                        scope.launch {
                            installingUpdate = true
                            updateStatusText = "Téléchargement de ${pendingUpdate.versionName}…"
                            val result = CyberQuizUpdateManager.downloadAndLaunchInstaller(activity, pendingUpdate)
                            installingUpdate = false
                            result.exceptionOrNull()?.let { error ->
                                updateStatusText = error.message ?: "La mise à jour n'a pas pu être installée."
                            }
                        }
                    }
                }
            },
            onVersionClick = onVersionClick
        )

        SettingsV4SectionLabel("APPLICATION")
        SettingsV4Item("⌘", "Projet GitHub", "Elikto / CyberQuiz", "›", SettingsV4Blue) {
            uriHandler.openUri("https://github.com/Elikto/CyberQuiz")
        }

        SettingsV4SectionLabel("AIDE & SOUTIEN")
        SettingsV4Item("?", "FAQ", "Questions fréquentes et aide", "›", SettingsV4Cyan) {
            showFaq = true
        }
        SettingsV4Item("✉", "Nous contacter", "Formulaire de contact", "›", SettingsV4Purple) {
            showContactForm = true
        }
        SettingsV4Item("♥", "Soutenir CyberQuiz", "paypal.me/EliktoCyber", "›", Color(0xFFFF678A)) {
            uriHandler.openUri("https://paypal.me/EliktoCyber")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF071225), RoundedCornerShape(18.dp))
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "UN MONDE PLUS SÛR COMMENCE PAR TOI",
                color = Color(0xFFD0D9F4),
                fontSize = 9.sp,
                letterSpacing = 1.8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "CyberQuiz évoluera avec de nouveaux univers, questions et fonctionnalités.",
                color = SettingsV4Muted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun SettingsV4UpdateCard(
    checking: Boolean,
    installing: Boolean,
    updateChecked: Boolean,
    availableUpdate: CyberQuizUpdateInfo?,
    statusText: String?,
    onUpdateClick: () -> Unit,
    onVersionClick: () -> Unit
) {
    val updateAvailable = availableUpdate != null
    val accent = if (updateAvailable) SettingsV4Green else SettingsV4Blue
    val inlineStatus = when {
        statusText != null -> statusText
        installing -> "Téléchargement en cours…"
        updateAvailable -> "Mise à jour ${availableUpdate?.versionName} disponible"
        checking -> "Vérification…"
        updateChecked -> "Application à jour"
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF17143D), Color(0xFF07172F))),
                RoundedCornerShape(22.dp)
            )
            .border(
                if (updateAvailable) 1.6.dp else 1.2.dp,
                if (updateAvailable) SettingsV4Green.copy(alpha = .85f) else Color(0xFF416EC2),
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(SettingsV4Purple.copy(alpha = .15f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("CQ", color = SettingsV4Purple, fontSize = 19.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = SettingsV4Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = SettingsV4Purple, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Text(
                "Version ${BuildConfig.VERSION_NAME}  ›",
                color = SettingsV4Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onVersionClick)
                    .padding(top = 5.dp, bottom = 2.dp)
            )
            if (inlineStatus.isNotBlank()) {
                Text(
                    inlineStatus,
                    color = when {
                        statusText != null -> SettingsV4Orange
                        updateAvailable -> SettingsV4Green
                        else -> SettingsV4Muted
                    },
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = if (updateAvailable) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .background(accent.copy(alpha = if (updateAvailable) .20f else .11f), CircleShape)
                .border(1.2.dp, accent.copy(alpha = .85f), CircleShape)
                .clickable(enabled = !checking && !installing, onClick = onUpdateClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (checking || installing) "…" else "↻",
                color = if (checking || installing) SettingsV4Muted else accent,
                fontSize = if (checking || installing) 20.sp else 25.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsV4Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF101A34), CircleShape)
                .border(1.2.dp, Color(0xFF718CE2), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", color = SettingsV4Text, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Paramètres", color = SettingsV4Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(
                "CYBERQUIZ",
                color = SettingsV4Muted,
                fontSize = 9.sp,
                letterSpacing = 1.7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsV4SectionLabel(text: String) {
    Text(
        text,
        color = SettingsV4Blue,
        fontSize = 10.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsV4Item(
    symbol: String,
    title: String,
    subtitle: String,
    trailing: String,
    accent: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(1.dp, SettingsV4Border, RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .background(accent.copy(alpha = .12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = SettingsV4Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SettingsV4Muted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Text(
            trailing,
            color = accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private tailrec fun Context.findActivityV4(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivityV4()
    else -> null
}
