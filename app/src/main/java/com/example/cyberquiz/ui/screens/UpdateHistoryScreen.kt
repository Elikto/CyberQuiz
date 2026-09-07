package com.example.cyberquiz.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.ui.theme.CyberBackground

private val HistoryPurple = Color(0xFFD652FF)
private val HistoryBlue = Color(0xFF19BFFF)
private val HistoryCyan = Color(0xFF19F2E5)
private val HistoryGreen = Color(0xFF38E69A)
private val HistoryText = Color(0xFFF5F7FF)
private val HistoryMuted = Color(0xFF9FAED3)
private val HistoryBorder = Color(0xFF244777)

private data class UpdateHistoryEntry(
    val version: String,
    val date: String,
    val changes: List<String>
)

private val previousUpdates = listOf(
    UpdateHistoryEntry(
        version = "1.0.33",
        date = "7 septembre 2026",
        changes = listOf(
            "Suppression complète du pop-up de mise à jour au lancement de l'application.",
            "Les nouvelles versions sont désormais signalées uniquement par une petite pastille sur l'icône Paramètres.",
            "La vérification des mises à jour reste silencieuse et aucune installation ne démarre automatiquement."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.32",
        date = "7 septembre 2026",
        changes = listOf(
            "La carte Nous contacter des Paramètres affiche maintenant l'adresse elikto@proton.me.",
            "Un appui sur cette carte ouvre directement l'application de messagerie pour écrire à CyberQuiz."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.31",
        date = "7 septembre 2026",
        changes = listOf(
            "CyberQuiz utilise désormais le package Android com.elikto.cyberquiz, enregistré dans Android Developer Console.",
            "La nouvelle identité Android correspond à une installation propre et repart donc à zéro.",
            "La chaîne de publication vérifie désormais le nom de package de l'APK avant sa publication."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.30",
        date = "7 septembre 2026",
        changes = listOf(
            "La consultation des mises à jour ouvre désormais une page complète au lieu d'une petite fenêtre.",
            "Tous les changements sont présentés en français, avec la date de chaque mise à jour.",
            "Les anciennes versions sont regroupées sous la dernière version et restent repliées par défaut.",
            "Le contrôle de mise à jour dans Paramètres est devenu un simple bouton circulaire et discret."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.29",
        date = "7 septembre 2026",
        changes = listOf(
            "Les mises à jour sont devenues non intrusives : aucun grand pop-up n'apparaît au lancement de l'application.",
            "La présence d'une nouvelle version est signalée discrètement dans l'onglet Paramètres.",
            "Le téléchargement et l'installation ne démarrent qu'après une action volontaire de l'utilisateur."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.28",
        date = "7 septembre 2026",
        changes = listOf(
            "Ajout du contrôle de recherche de mise à jour directement dans la carte CyberQuiz des Paramètres.",
            "La ligne de version est devenue interactive pour consulter les nouveautés de l'application.",
            "Le numéro technique de build affiché auparavant dans la carte a été supprimé."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.27",
        date = "7 septembre 2026",
        changes = listOf(
            "Fiabilisation de la chaîne de publication des APK Android.",
            "La signature de l'application est isolée du processus de compilation et vérifiée avant publication.",
            "Amélioration de la sécurité et de la continuité des futures mises à jour."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.24",
        date = "7 septembre 2026",
        changes = listOf(
            "Renforcement de la vérification de la signature Android des APK distribués.",
            "Ajout d'analyses de sécurité automatiques sur le code Android, Python et JavaScript.",
            "Durcissement de plusieurs contrôles liés au téléchargement et à l'installation des mises à jour."
        )
    ),
    UpdateHistoryEntry(
        version = "1.0.21",
        date = "7 septembre 2026",
        changes = listOf(
            "Amélioration du système de mise à jour intégré à l'application.",
            "Contrôle du fichier téléchargé avec vérification SHA-256, package Android, version et certificat de signature.",
            "Renforcement général des protections de l'application et de sa configuration Android."
        )
    )
)

@Composable
fun UpdateHistoryScreen(onBack: () -> Unit) {
    val currentChanges = listOf(
        "La carte Soutenir CyberQuiz ouvre désormais directement la page PayPal.Me officielle du projet.",
        "Le lien paypal.me/EliktoCyber est visible dans les Paramètres pour permettre un soutien volontaire."
    )

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
        UpdateHistoryHeader(onBack)

        Text(
            "DERNIÈRE MISE À JOUR",
            color = HistoryBlue,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
        )

        CurrentVersionCard(
            version = BuildConfig.VERSION_NAME,
            date = "8 septembre 2026",
            changes = currentChanges
        )

        Text(
            "VERSIONS PRÉCÉDENTES",
            color = HistoryBlue,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            fontWeight = FontWeight.Bold
        )

        previousUpdates.forEach { update ->
            CollapsibleVersionCard(update)
        }

        Spacer(Modifier.size(8.dp))
    }
}

@Composable
private fun UpdateHistoryHeader(onBack: () -> Unit) {
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
            Text("‹", color = HistoryText, fontSize = 34.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Mises à jour", color = HistoryText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(
                "HISTORIQUE CYBERQUIZ",
                color = HistoryMuted,
                fontSize = 9.sp,
                letterSpacing = 1.7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CurrentVersionCard(
    version: String,
    date: String,
    changes: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF17143D), Color(0xFF07172F))),
                RoundedCornerShape(22.dp)
            )
            .border(1.4.dp, HistoryCyan.copy(alpha = .75f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Version $version",
                    color = HistoryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Text(date, color = HistoryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "ACTUELLE",
                color = HistoryGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        }

        changes.forEach { change ->
            ChangeRow(change)
        }
    }
}

@Composable
private fun CollapsibleVersionCard(update: UpdateHistoryEntry) {
    var expanded by rememberSaveable(update.version) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF081329), RoundedCornerShape(18.dp))
            .border(1.dp, HistoryBorder, RoundedCornerShape(18.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Version ${update.version}",
                    color = HistoryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(update.date, color = HistoryMuted, fontSize = 11.sp)
            }
            Text(
                if (expanded) "⌃" else "⌄",
                color = HistoryCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (expanded) {
            update.changes.forEach { change ->
                ChangeRow(change)
            }
        }
    }
}

@Composable
private fun ChangeRow(change: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•", color = HistoryCyan, fontWeight = FontWeight.Bold)
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
