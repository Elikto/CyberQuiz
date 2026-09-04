package com.example.cyberquiz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cyberquiz.ui.theme.*

@Composable fun NeonCard(modifier: Modifier=Modifier, content:@Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().background(Brush.linearGradient(listOf(CyberSurface2, CyberSurface)), RoundedCornerShape(22.dp)).border(1.dp, CyberPurple.copy(alpha=.16f), RoundedCornerShape(22.dp)).padding(20.dp), content=content)
}

@Composable fun PrimaryButton(text:String, onClick:()->Unit, modifier:Modifier=Modifier) {
    Button(onClick=onClick, modifier=modifier.fillMaxWidth().height(54.dp), shape=RoundedCornerShape(16.dp), colors=ButtonDefaults.buttonColors(containerColor=CyberPurple)) { Text(text) }
}

@Composable fun AnswerButton(text:String, selected:Boolean, correct:Boolean?, onClick:()->Unit) {
    val border = when { correct == true -> CyberGreen; correct == false -> CyberRed; selected -> CyberPurple; else -> CyberSurface2 }
    val bg = when { correct == true -> CyberGreen.copy(.12f); correct == false -> CyberRed.copy(.12f); selected -> CyberPurple.copy(.12f); else -> CyberSurface }
    Row(Modifier.fillMaxWidth().background(bg,RoundedCornerShape(16.dp)).border(1.dp,border,RoundedCornerShape(16.dp)).clickable(enabled=correct==null,onClick=onClick).padding(17.dp), verticalAlignment=Alignment.CenterVertically) { Text(text, color=CyberText) }
}

@Composable fun SectionTitle(title:String, subtitle:String?=null) { Column { Text(title, style=MaterialTheme.typography.headlineMedium); subtitle?.let{ Text(it, color=CyberMuted) } } }
