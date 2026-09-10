package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.cyberquiz.social.SharedQuizViewModel

/**
 * CyberSquad 2.0 keeps the proven account/friend lobby intact and adds a dedicated
 * squad dashboard for multiplayer creation, results history and friend ranking.
 */
@Composable
internal fun SocialHubScreenV3(
    playerLevel: Int,
    sharedQuizViewModel: SharedQuizViewModel,
    onSharedQuizStart: () -> Unit,
    onBack: () -> Unit
) {
    var showSquadDashboard by rememberSaveable { mutableStateOf(false) }

    if (showSquadDashboard) {
        SquadDashboardScreen(
            sharedQuizViewModel = sharedQuizViewModel,
            onRoomCreated = { showSquadDashboard = false },
            onBack = { showSquadDashboard = false }
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        SocialHubScreenV2(
            playerLevel = playerLevel,
            sharedQuizViewModel = sharedQuizViewModel,
            onSharedQuizStart = onSharedQuizStart,
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 14.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4A176E), Color(0xFF123D70))
                    ),
                    RoundedCornerShape(50.dp)
                )
                .border(1.2.dp, Color(0xFF20E7F2).copy(alpha = .80f), RoundedCornerShape(50.dp))
                .clickable { showSquadDashboard = true }
                .padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 13.sp)
            Text(
                "  CYBERSQUAD 2.0",
                color = Color(0xFFF5F7FF),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .8.sp
            )
        }
    }
}
