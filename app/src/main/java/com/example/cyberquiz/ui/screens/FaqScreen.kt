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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.ui.theme.CyberBackground

private val FaqPurple = Color(0xFFD652FF)
private val FaqBlue = Color(0xFF19BFFF)
private val FaqCyan = Color(0xFF19F2E5)
private val FaqGreen = Color(0xFF38E69A)
private val FaqOrange = Color(0xFFFFB84A)
private val FaqText = Color(0xFFF5F7FF)
private val FaqMuted = Color(0xFF9FAED3)
private val FaqBorder = Color(0xFF244777)

private data class FaqEntry(
    val question: String,
    val answer: String
)

private data class FaqSection(
    val title: String,
    val symbol: String,
    val accent: Color,
    val entries: List<FaqEntry>
)

private val faqSections = listOf(
    FaqSection(
        title = "Quiz & fonctionnement",
        symbol = "?",
        accent = FaqPurple,
        entries = listOf(
            FaqEntry(
                "Comment lancer un quiz ?",
                "Depuis l'accueil, ouvre Commencer puis appuie sur « Commencer un quiz ». Choisis ensuite le mode, les catégories et le nombre de questions avant de lancer la session."
            ),
            FaqEntry(
                "Puis-je reprendre un quiz quitté ?",
                "Oui. Les quiz configurés en cours peuvent être repris depuis l'écran Commencer. Lorsque tu quittes une session active, CyberQuiz te demande confirmation afin d'éviter de perdre ta progression."
            ),
            FaqEntry(
                "Que signifie le mode Infini ?",
                "Le mode Infini continue à proposer des questions tant que tu souhaites jouer. Tu peux quitter la session lorsque tu veux et consulter ensuite les résultats enregistrés."
            )
        )
    ),
    FaqSection(
        title = "XP, niveaux & statistiques",
        symbol = "★",
        accent = FaqBlue,
        entries = listOf(
            FaqEntry(
                "Comment sont calculés les XP ?",
                "La progression globale augmente au fil de tes réponses. Chaque catégorie possède aussi sa propre progression : une réponse rapporte de l'XP et une bonne réponse apporte un bonus supplémentaire."
            ),
            FaqEntry(
                "Pourquoi ai-je un niveau différent selon les catégories ?",
                "Le niveau de catégorie mesure uniquement ton activité et tes bonnes réponses dans ce thème. Tu peux donc être plus avancé en Réseaux qu'en Cryptographie, par exemple."
            ),
            FaqEntry(
                "À quoi sert la progression par thème et notion ?",
                "Elle t'aide à voir les domaines solides et ceux à renforcer. Les pourcentages sont calculés à partir de tes réponses enregistrées, tandis que les notions à revoir mettent en avant les erreurs récentes."
            )
        )
    ),
    FaqSection(
        title = "Profil & avatar",
        symbol = "◉",
        accent = FaqCyan,
        entries = listOf(
            FaqEntry(
                "Comment changer mon avatar ?",
                "Tu peux toucher l'avatar depuis l'accueil ou depuis Mon profil. Cinq avatars CyberQuiz sont disponibles et le choix est conservé sur ton téléphone."
            ),
            FaqEntry(
                "Puis-je utiliser ma propre image ?",
                "Oui. Dans Mon profil, touche ton avatar puis choisis « Importer une image personnelle ». CyberQuiz ouvre ensuite un éditeur pour zoomer, déplacer et recadrer la photo avant de l'utiliser."
            ),
            FaqEntry(
                "Ma photo de profil est-elle envoyée sur Internet ?",
                "Non. L'image personnalisée est recadrée puis enregistrée localement dans l'espace privé de CyberQuiz sur ton téléphone. Elle n'est pas envoyée au serveur de contact."
            )
        )
    ),
    FaqSection(
        title = "Mises à jour",
        symbol = "↻",
        accent = FaqGreen,
        entries = listOf(
            FaqEntry(
                "Comment savoir qu'une mise à jour est disponible ?",
                "CyberQuiz vérifie les nouvelles versions au lancement et périodiquement en arrière-plan. Si Android autorise les notifications, une alerte apparaît lorsqu'une nouvelle APK signée est prête."
            ),
            FaqEntry(
                "Pourquoi une nouveauté fusionnée sur GitHub n'apparaît-elle pas immédiatement ?",
                "Après la fusion du code, GitHub doit encore compiler, tester, signer puis publier l'APK et son fichier de mise à jour. La notification n'apparaît qu'une fois cette version installable réellement publiée."
            )
        )
    ),
    FaqSection(
        title = "Données & assistance",
        symbol = "i",
        accent = FaqOrange,
        entries = listOf(
            FaqEntry(
                "Où sont enregistrées mes données ?",
                "La progression, l'historique, les révisions, le pseudo et les préférences sont conservés localement par l'application. Certaines fonctions comme le formulaire de contact utilisent le serveur CyberQuiz uniquement lorsque tu les déclenches."
            ),
            FaqEntry(
                "Que fait « Effacer les données » ?",
                "Cette action réinitialise l'application et supprime la progression, l'historique, les révisions, les quiz en cours, le pseudo et l'avatar personnalisé. Une confirmation est demandée avant l'effacement."
            ),
            FaqEntry(
                "Comment signaler un bug ou proposer une amélioration ?",
                "Ouvre Paramètres puis Nous contacter. Pour une demande importante, tu peux cocher Urgent afin qu'elle passe en priorité et déclenche aussi l'alerte Telegram prévue côté CyberQuiz."
            )
        )
    )
)

@Composable
fun FaqScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

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
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF101A34), CircleShape)
                    .border(1.1.dp, Color(0xFF718CE2), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = FaqText, fontSize = 31.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text("FAQ", color = FaqText, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text(
                    "QUESTIONS FRÉQUENTES & AIDE",
                    color = FaqMuted,
                    fontSize = 8.sp,
                    letterSpacing = 1.4.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(FaqPurple.copy(alpha = .14f), Color(0xFF07162A))),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, FaqPurple.copy(alpha = .45f), RoundedCornerShape(20.dp))
                .padding(15.dp)
        ) {
            Text("Besoin d'un coup de main ?", color = FaqText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Retrouve ici les réponses sur les quiz, les XP, le profil, les mises à jour et tes données. Appuie sur une question pour afficher la réponse.",
                color = FaqMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }

        faqSections.forEach { section ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(31.dp)
                        .background(section.accent.copy(alpha = .12f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(section.symbol, color = section.accent, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.size(9.dp))
                Text(
                    section.title.uppercase(),
                    color = section.accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            section.entries.forEach { entry ->
                val key = "${section.title}|${entry.question}"
                val isExpanded = expanded[key] == true
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF081329), RoundedCornerShape(17.dp))
                        .border(
                            1.dp,
                            if (isExpanded) section.accent.copy(alpha = .55f) else FaqBorder,
                            RoundedCornerShape(17.dp)
                        )
                        .clickable { expanded[key] = !isExpanded }
                        .padding(horizontal = 14.dp, vertical = 13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.question,
                            color = FaqText,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            if (isExpanded) "⌃" else "⌄",
                            color = section.accent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (isExpanded) {
                        Spacer(Modifier.height(9.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(section.accent.copy(alpha = .20f))
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            entry.answer,
                            color = Color(0xFFC9D4EE),
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
