package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cyberquiz.model.Category
import com.example.cyberquiz.ui.components.*
import com.example.cyberquiz.ui.theme.*
import com.example.cyberquiz.viewmodel.*

@Composable fun HomeScreen(vm:QuizViewModel,onQuiz:()->Unit,onStats:()->Unit,onCategories:()->Unit) {
    val p by vm.progress.collectAsState()
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(18.dp)); Text("◈ CYBERQUIZ", color=CyberPurple, style=MaterialTheme.typography.headlineLarge)
        Text("Entraîne ton esprit cyber.", color=CyberMuted)
        NeonCard { Text("NIVEAU ${p.level}", color=CyberBlue, fontWeight=FontWeight.Bold); Text("${p.xp} XP", style=MaterialTheme.typography.titleLarge); Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress={((p.xp%100)/100f)}, modifier=Modifier.fillMaxWidth(), color=CyberPurple, trackColor=CyberSurface) }
        PrimaryButton("Commencer", onQuiz)
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { OutlinedButton({onStats()},Modifier.weight(1f).height(52.dp)){Text("Statistiques")}; OutlinedButton({onCategories()},Modifier.weight(1f).height(52.dp)){Text("Catégories")} }
        NeonCard { Text("Défi du jour", color=CyberGreen, fontWeight=FontWeight.Bold); Text("Réponds à 5 questions et gagne jusqu'à 60 XP.", Modifier.padding(top=6.dp), color=CyberText) }
    }
}

@Composable fun QuizScreen(vm:QuizViewModel,onHome:()->Unit) {
    val state by vm.state.collectAsState(); val result by vm.result.collectAsState(); var selected by remember { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("QUIZ",color=CyberPurple,fontWeight=FontWeight.Bold); TextButton(onClick=onHome){Text("Quitter")}}
        when(state) {
            QuizUiState.Loading -> Box(Modifier.fillMaxSize(),Alignment.Center){CircularProgressIndicator()}
            QuizUiState.Finished -> { SectionTitle("Session terminée","Les questions vont être renouvelées."); PrimaryButton("Rejouer"){vm.start()} }
            is QuizUiState.Ready -> { val q=(state as QuizUiState.Ready).question; Text(q.category.uppercase(),color=CyberBlue,fontWeight=FontWeight.Bold); Text(q.question,style=MaterialTheme.typography.headlineSmall); val answers=listOf(q.answerA,q.answerB,q.answerC,q.answerD); answers.forEachIndexed { i,a -> AnswerButton(a,selected==i, if(result==null)null else if(i==q.correctIndex) true else if(selected==i) false else null){selected=i} }; if(result==null) PrimaryButton("Valider"){selected?.let(vm::answer)} else { NeonCard { Text(if(result!!.correct) "✓ Bonne réponse" else "✕ Mauvaise réponse", color=if(result!!.correct) CyberGreen else CyberRed, fontWeight=FontWeight.Bold); Text(result!!.explanation,Modifier.padding(top=8.dp),color=CyberMuted); Text("+${result!!.xp} XP",Modifier.padding(top=8.dp),color=CyberPurple,fontWeight=FontWeight.Bold) }; PrimaryButton("Question suivante"){selected=null;vm.nextQuestion()} } }
        }
    }
}

@Composable fun StatisticsScreen(vm:QuizViewModel,onBack:()->Unit) { val p by vm.progress.collectAsState(); val rate=if(p.answered==0)0 else p.correct*100/p.answered; Column(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("Statistiques",style=MaterialTheme.typography.headlineLarge); TextButton(onBack){Text("← Accueil")}; NeonCard{Text("TA RÉUSSITE",color=CyberMuted); Text("$rate%",style=MaterialTheme.typography.displaySmall,color=CyberPurple); LinearProgressIndicator(progress={rate/100f},Modifier.fillMaxWidth(),color=CyberPurple,trackColor=CyberSurface)}; Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){StatBox("Répondu",p.answered.toString(),Modifier.weight(1f));StatBox("Correct",p.correct.toString(),Modifier.weight(1f))}; Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){StatBox("Série",p.streak.toString(),Modifier.weight(1f));StatBox("Record",p.bestStreak.toString(),Modifier.weight(1f))} }
}
@Composable private fun StatBox(label:String,value:String,modifier:Modifier){NeonCard(modifier){Text(label,color=CyberMuted);Text(value,style=MaterialTheme.typography.headlineMedium,color=CyberText)}}

@Composable fun CategoriesScreen(vm:QuizViewModel,onBack:()->Unit,onHome:()->Unit){Column(Modifier.fillMaxSize().padding(22.dp)){Text("Catégories",style=MaterialTheme.typography.headlineLarge);TextButton(onBack){Text("← Accueil")};Spacer(Modifier.height(12.dp));LazyVerticalGrid(columns=GridCells.Fixed(2),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){items(Category.entries){c->NeonCard(Modifier.fillMaxWidth().height(105.dp)){Text(c.label,style=MaterialTheme.typography.titleMedium);Text("Entraînement",color=CyberMuted);Spacer(Modifier.weight(1f));Text("›",color=CyberPurple)}}};Spacer(Modifier.height(14.dp));PrimaryButton("Quiz adaptatif"){vm.start();onHome()} }}
