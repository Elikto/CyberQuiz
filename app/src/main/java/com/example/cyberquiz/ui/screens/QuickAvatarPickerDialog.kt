package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PlayerAvatarPickerDialog(
    selected: PlayerAvatarStyle,
    onSelect: (PlayerAvatarStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF081225),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Choisis ton avatar",
                    color = Color(0xFFF5F7FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "5 avatars de départ · la collection complète est dans Mon profil.",
                    color = Color(0xFF9FAED3),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                starterPlayerAvatarStyles.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
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
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("FERMER", color = Color(0xFF8FA9D8), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun AvatarChoiceCard(
    style: PlayerAvatarStyle,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val accent = if (selected) Color(0xFF38E69A) else Color(0xFF19BFFF)
    Column(
        modifier = modifier
            .aspectRatio(.92f)
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = if (selected) .16f else .07f), Color(0xFF0A152A))
                ),
                RoundedCornerShape(18.dp)
            )
            .border(
                if (selected) 1.7.dp else 1.dp,
                accent.copy(alpha = if (selected) .95f else .36f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PlayerAvatarButton(
            style = style,
            onClick = onClick,
            size = 54.dp,
            syncWithStoredSelection = false,
            showEditBadge = false
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                style.displayName,
                color = Color(0xFFF5F7FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                if (selected) "SÉLECTIONNÉ" else style.subtitle,
                color = if (selected) accent else Color(0xFF9FAED3),
                fontSize = 7.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
