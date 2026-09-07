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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.ui.theme.CyberBackground
import com.example.cyberquiz.update.CyberQuizUpdateInfo
import com.example.cyberquiz.update.CyberQuizUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val SettingsV3Purple = Color(0xFFD652FF)
private val SettingsV3Blue = Color(0xFF19BFFF)
private val SettingsV3Cyan = Color(0xFF19F2E5)
private val SettingsV3Green = Color(0xFF38E69A)
private val SettingsV3Orange = Color(0xFFFFB84A)
private val SettingsV3Text = Color(0xFFF5F7FF)
private val SettingsV3Muted = Color(0xFF9FAED3)
private val SettingsV3Border = Color(0xFF244777)

private const val RECENT_CHANGES_URL =
    "https://api.github.com/repos/Elikto/CyberQuiz/commits?path=app/src/main&per_page=8"
private const val MAX_CHANGES_RESPONSE_CHARS = 64_000

@Composable
fun SettingsScreenV3(onBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() }

    var checkingUpdate by remember { mutableStateOf(false) }
    var installingUpdate by remember { mutableStateOf(false) }
    var updateChecked by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<CyberQuizUpdateInfo?>(null) }
    var updateStatusText by remember { mutableStateOf<String?>(null) }
    var showChangesDialog by remember { mutableStateOf(false) }
    var loadingChanges by remember { mutableStateOf(false) }
    var recentChanges by remember { mutableStateOf<List<String>>(emptyList()) }
    var changesLoadError by remember { mutableStateOf<String?>(null) }

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
        SettingsV3Header(onBack = onBack)

        SettingsUpdateCard(
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
                        updateStatusText = "Impossible d'ouvrir l'installateur Android depuis cet écran."
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
            onVersionClick = {
                showChangesDialog = true
                if (!loadingChanges && recentChanges.isEmpty()) {
                    scope.launch {
                        loadingChanges = true
                        changesLoadError = null
                        loadRecentAndroidChanges()
                            .onSuccess { recentChanges = it }
                            .onFailure {
                                changesLoadError = "Impossible de charger les derniers changements pour le moment."
                            }
                        loadingChanges = false
                    }
                }
            }
        )

        SettingsV3SectionLabel("APPLICATION")
        SettingsV3Item("⌘", "Projet GitHub", "Elikto / CyberQuiz", "›", SettingsV3Blue) {
            uriHandler.openUri("https://github.com/Elikto/CyberQuiz")
        }

        SettingsV3SectionLabel("AIDE & SOUTIEN")
        SettingsV3Item("?", "FAQ", "Questions fréquentes et aide", "BIENTÔT", SettingsV3Cyan)
        SettingsV3Item("✉", "Nous contacter", "Signaler un problème ou proposer une idée", "BIENTÔT", SettingsV3Purple)
        SettingsV3Item("♥", "Soutenir CyberQuiz", "Un lien de don sera ajouté plus tard", "BIENTÔT", Color(0xFFFF678A))

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
                color = SettingsV3Muted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(6.dp))
    }

    if (showChangesDialog) {
        AlertDialog(
            onDismissRequest = { showChangesDialog = false },
            containerColor = Color(0xFF081329),
            title = {
                Column {
                    Text("Derniers changements", color = SettingsV3Text, fontWeight = FontWeight.Bold)
                    Text(
                        "CyberQuiz ${BuildConfig.VERSION_NAME}",
                        color = SettingsV3Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when {
                        loadingChanges -> Text(
                            "Chargement des dernières modifications…",
                            color = SettingsV3Muted
                        )

                        changesLoadError != null -> Text(
                            changesLoadError.orEmpty(),
                            color = SettingsV3Orange
                        )

                        recentChanges.isEmpty() -> Text(
                            "Aucun changement récent à afficher.",
                            color = SettingsV3Muted
                        )

                        else -> recentChanges.forEach { change ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", color = SettingsV3Cyan, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    change,
                                    color = Color(0xFFD8E1F7),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangesDialog = false }) {
                    Text("FERMER", color = SettingsV3Cyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsUpdateCard(
    checking: Boolean,
    installing: Boolean,
    updateChecked: Boolean,
    availableUpdate: CyberQuizUpdateInfo?,
    statusText: String?,
    onUpdateClick: () -> Unit,
    onVersionClick: () -> Unit
) {
    val updateAvailable = availableUpdate != null
    val accent = if (updateAvailable) SettingsV3Green else SettingsV3Blue
    val inlineStatus = when {
        statusText != null -> statusText
        installing -> "Téléchargement en cours…"
        updateAvailable -> "Version ${availableUpdate?.versionName} disponible"
        checking -> "Vérification discrète des mises à jour…"
        updateChecked -> "Aucune mise à jour en attente"
        else -> "Recherche de mise à jour disponible"
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
                if (updateAvailable) SettingsV3Green.copy(alpha = .85f) else Color(0xFF416EC2),
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(SettingsV3Purple.copy(alpha = .15f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("CQ", color = SettingsV3Purple, fontSize = 19.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Cyber", color = SettingsV3Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Quiz", color = SettingsV3Purple, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Text(
                "Version ${BuildConfig.VERSION_NAME}  ›",
                color = SettingsV3Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onVersionClick)
                    .padding(top = 5.dp, bottom = 2.dp)
            )
            Text(
                inlineStatus,
                color = when {
                    statusText != null -> SettingsV3Orange
                    updateAvailable -> SettingsV3Green
                    updateChecked -> SettingsV3Muted
                    else -> SettingsV3Muted
                },
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                fontWeight = if (updateAvailable) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(118.dp)
                .background(accent.copy(alpha = if (updateAvailable) .18f else .10f), RoundedCornerShape(13.dp))
                .border(1.dp, accent.copy(alpha = .78f), RoundedCornerShape(13.dp))
                .clickable(enabled = !checking && !installing, onClick = onUpdateClick)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when {
                    installing -> "TÉLÉCHARGEMENT…"
                    checking -> "VÉRIFICATION…"
                    updateAvailable -> "● MISE À JOUR\nDISPONIBLE"
                    updateChecked -> "À JOUR\nRECHERCHER"
                    else -> "RECHERCHER\nUNE MISE À JOUR"
                },
                color = when {
                    installing || checking -> SettingsV3Muted
                    updateAvailable -> SettingsV3Green
                    else -> SettingsV3Cyan
                },
                fontSize = if (updateAvailable) 8.3.sp else 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsV3Header(onBack: () -> Unit) {
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
            Text("‹", color = SettingsV3Text, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Paramètres", color = SettingsV3Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(
                "CYBERQUIZ",
                color = SettingsV3Muted,
                fontSize = 9.sp,
                letterSpacing = 1.7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsV3SectionLabel(text: String) {
    Text(
        text,
        color = SettingsV3Blue,
        fontSize = 10.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsV3Item(
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
            .border(1.dp, SettingsV3Border, RoundedCornerShape(18.dp))
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
            Text(title, color = SettingsV3Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SettingsV3Muted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Text(
            trailing,
            color = if (trailing == "BIENTÔT") SettingsV3Orange else accent,
            fontSize = if (trailing == "BIENTÔT") 8.sp else 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private suspend fun loadRecentAndroidChanges(): Result<List<String>> = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(RECENT_CHANGES_URL).openConnection() as HttpsURLConnection).apply {
            connectTimeout = 6_000
            readTimeout = 6_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "CyberQuiz-Android")
        }

        try {
            if (connection.responseCode !in 200..299) {
                error("GitHub a répondu avec le code ${connection.responseCode}.")
            }

            val body = connection.inputStream.bufferedReader().use { reader ->
                val output = StringBuilder()
                val buffer = CharArray(4_096)
                while (true) {
                    val read = reader.read(buffer)
                    if (read < 0) break
                    if (output.length + read > MAX_CHANGES_RESPONSE_CHARS) {
                        error("Réponse GitHub trop volumineuse.")
                    }
                    output.append(buffer, 0, read)
                }
                output.toString()
            }

            val commits = JSONArray(body)
            buildList {
                for (index in 0 until commits.length()) {
                    val message = commits
                        .optJSONObject(index)
                        ?.optJSONObject("commit")
                        ?.optString("message")
                        ?.lineSequence()
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()

                    if (
                        message.isNotBlank() &&
                        !message.startsWith("Merge ", ignoreCase = true) &&
                        message !in this
                    ) {
                        add(message.take(140))
                    }
                    if (size >= 6) break
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
