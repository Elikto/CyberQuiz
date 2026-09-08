package com.example.cyberquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberquiz.engagement.EngagementStore
import com.example.cyberquiz.model.EngagementMetrics
import com.example.cyberquiz.ui.theme.CyberBackground

private enum class CosmeticsTab { AVATARS, BANNERS, FRAMES }
private val CText = Color(0xFFF5F7FF)
private val CMuted = Color(0xFF9FAED3)
private val CBorder = Color(0xFF244777)
private val CCyan = Color(0xFF19F2E5)
private val CPurple = Color(0xFFD652FF)
private val CGreen = Color(0xFF38E69A)
private val COrange = Color(0xFFFFB84A)

@Composable
fun CosmeticsScreen(
    playerLevel: Int,
    metrics: EngagementMetrics? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(CosmeticsTab.AVATARS) }
    var avatar by remember { mutableStateOf(storedPlayerAvatar(context)) }
    var banner by remember { mutableStateOf(storedPlayerBanner(context)) }
    var frame by remember { mutableStateOf(storedPlayerFrame(context)) }
    var shopAvatar by remember { mutableStateOf(storedShopAvatar(context)) }
    var shopBanner by remember { mutableStateOf(storedShopBanner(context)) }
    var coins by remember { mutableIntStateOf(EngagementStore.currentCoins(context)) }
    var purchasedFrames by remember { mutableStateOf(EngagementStore.purchasedFrameKeys(context)) }
    val achievements = remember(metrics) {
        metrics?.let { EngagementStore.sync(context, it).unlockedAchievementIds }
            ?: EngagementStore.unlockedAchievementIds(context)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020610),Color(0xFF071022),CyberBackground,Color(0xFF030712))))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal=17.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ) {
        CosmeticsHeader(onBack, coins)
        CosmeticsPreview(avatar,banner,frame,shopAvatar,shopBanner,playerLevel)
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF071329),RoundedCornerShape(17.dp)).border(1.dp,CBorder,RoundedCornerShape(17.dp)).padding(4.dp),
            horizontalArrangement=Arrangement.spacedBy(4.dp)
        ) {
            CosmeticsTabButton("AVATARS",selectedTab==CosmeticsTab.AVATARS,Modifier.weight(1f)){selectedTab=CosmeticsTab.AVATARS}
            CosmeticsTabButton("BANNIÈRES",selectedTab==CosmeticsTab.BANNERS,Modifier.weight(1f)){selectedTab=CosmeticsTab.BANNERS}
            CosmeticsTabButton("CONTOURS",selectedTab==CosmeticsTab.FRAMES,Modifier.weight(1f)){selectedTab=CosmeticsTab.FRAMES}
        }

        when(selectedTab) {
            CosmeticsTab.AVATARS -> {
                SectionTitle("DISPONIBLES","5 avatars disponibles dès le départ")
                AvatarGrid(starterPlayerAvatarStyles,playerLevel,avatar,banner,frame,achievements,metrics,shopAvatar!=null) { style ->
                    clearShopAvatar(context); shopAvatar=null; storePlayerAvatar(context,style); avatar=style
                }
                SectionTitle("PAR NIVEAU","Chaque objectif affiche désormais ta progression")
                AvatarGrid(levelPlayerAvatarStyles,playerLevel,avatar,banner,frame,achievements,metrics,shopAvatar!=null) { style ->
                    clearShopAvatar(context); shopAvatar=null; storePlayerAvatar(context,style); avatar=style
                }
                SectionTitle("DÉFIS","Les objectifs secrets affichent aussi une barre de progression")
                AvatarGrid(mysteryPlayerAvatarStyles,playerLevel,avatar,banner,frame,achievements,metrics,shopAvatar!=null) { style ->
                    clearShopAvatar(context); shopAvatar=null; storePlayerAvatar(context,style); avatar=style
                }
            }
            CosmeticsTab.BANNERS -> {
                SectionTitle("DISPONIBLES","Arrière-plans utilisables immédiatement")
                BannerGrid(starterPlayerBannerStyles,playerLevel,banner,avatar,frame,shopBanner!=null) { style ->
                    clearShopBanner(context); shopBanner=null; storePlayerBanner(context,style); banner=style
                }
                SectionTitle("PAR NIVEAU","Suis ta progression jusqu'au prochain arrière-plan")
                BannerGrid(levelPlayerBannerStyles,playerLevel,banner,avatar,frame,shopBanner!=null) { style ->
                    clearShopBanner(context); shopBanner=null; storePlayerBanner(context,style); banner=style
                }
                SectionTitle("SECRÈTES","Ces bannières seront liées à de futurs défis")
                BannerGrid(mysteryPlayerBannerStyles,playerLevel,banner,avatar,frame,shopBanner!=null) { style ->
                    clearShopBanner(context); shopBanner=null; storePlayerBanner(context,style); banner=style
                }
            }
            CosmeticsTab.FRAMES -> {
                SectionTitle("CONTOURS","Le Faisceau orbital est offert et animé")
                PlayerFrameStyle.entries.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                        row.forEach { style ->
                            val unlocked=isFrameUnlocked(style,playerLevel,purchasedFrames,achievements)
                            val canBuy=canPurchaseFrame(style,playerLevel,purchasedFrames,coins)
                            val progress=frameProgress(style,playerLevel,metrics)
                            FrameCard(style,style==frame,avatar,banner,unlocked,canBuy,progress,Modifier.weight(1f)) {
                                when {
                                    unlocked -> { storePlayerFrame(context,style); frame=style }
                                    canBuy && EngagementStore.purchaseFrame(context,style.storageKey,style.coinCost) -> {
                                        purchasedFrames=EngagementStore.purchasedFrameKeys(context)
                                        coins=EngagementStore.currentCoins(context)
                                        storePlayerFrame(context,style); frame=style
                                    }
                                }
                            }
                        }
                        if(row.size==1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(7.dp))
    }
}

@Composable
private fun CosmeticsHeader(onBack:()->Unit,coins:Int) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(Color(0xFF101A34),CircleShape).border(1.dp,Color(0xFF718CE2),CircleShape).clickable(onClick=onBack),contentAlignment=Alignment.Center){Text("‹",color=CText,fontSize=29.sp)}
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)){Text("Cosmétiques",color=CText,fontSize=23.sp,fontWeight=FontWeight.Black);Text("COLLECTION & PROGRESSION",color=CMuted,fontSize=8.sp,letterSpacing=1.1.sp)}
        Box(Modifier.background(Color(0xFF21163A),RoundedCornerShape(50.dp)).border(1.dp,COrange.copy(alpha=.55f),RoundedCornerShape(50.dp)).padding(horizontal=9.dp,vertical=6.dp)){Text("◈ $coins",color=Color(0xFFFFC86A),fontSize=9.sp,fontWeight=FontWeight.Black)}
    }
}

@Composable
private fun CosmeticsPreview(
    avatar:PlayerAvatarStyle,banner:PlayerBannerStyle,frame:PlayerFrameStyle,
    shopAvatar:ShopAvatarStyle?,shopBanner:ShopBannerStyle?,level:Int
) {
    Row(
        Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF15143D),Color(0xFF07172F))),RoundedCornerShape(21.dp)).border(1.dp,Color(0xFF416EC4),RoundedCornerShape(21.dp)).padding(14.dp),
        verticalAlignment=Alignment.CenterVertically
    ) {
        CyberAvatarView(avatar,banner,frame,{},78.dp,shopAvatarStyle=shopAvatar,shopBannerStyle=shopBanner,syncShopSelection=false)
        Spacer(Modifier.width(13.dp))
        Column {
            Text(shopAvatar?.displayName ?: avatar.displayName,color=CText,fontSize=17.sp,fontWeight=FontWeight.Black)
            Text(shopBanner?.displayName ?: banner.displayName,color=CCyan,fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text(frame.displayName,color=COrange,fontSize=10.sp,fontWeight=FontWeight.Bold)
            Text("Niveau $level",color=CMuted,fontSize=9.sp)
        }
    }
}

@Composable
private fun CosmeticsTabButton(label:String,selected:Boolean,modifier:Modifier,onClick:()->Unit) {
    Box(modifier.background(if(selected)CPurple.copy(alpha=.22f) else Color.Transparent,RoundedCornerShape(12.dp)).border(if(selected)1.dp else 0.dp,if(selected)CPurple else Color.Transparent,RoundedCornerShape(12.dp)).clickable(onClick=onClick).padding(vertical=9.dp),contentAlignment=Alignment.Center){Text(label,color=if(selected)Color.White else CMuted,fontSize=8.sp,fontWeight=FontWeight.Black)}
}

@Composable
private fun SectionTitle(title:String,subtitle:String) {
    Column(verticalArrangement=Arrangement.spacedBy(2.dp)){Text(title,color=Color(0xFF19BFFF),fontSize=9.sp,fontWeight=FontWeight.Black,letterSpacing=1.2.sp);Text(subtitle,color=CMuted,fontSize=9.sp)}
}

@Composable
private fun AvatarGrid(
    styles:List<PlayerAvatarStyle>,level:Int,selected:PlayerAvatarStyle,banner:PlayerBannerStyle,frame:PlayerFrameStyle,
    achievements:Set<String>,metrics:EngagementMetrics?,shopAvatarActive:Boolean,onSelect:(PlayerAvatarStyle)->Unit
) {
    styles.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            row.forEach { style ->
                val unlocked=isAvatarUnlocked(style,level,achievements)
                val isSelected=selected==style && !shopAvatarActive
                val progress=when {
                    unlocked -> null
                    style.mystery && metrics!=null -> achievementCosmeticProgress(secretAvatarRule(style)?.achievementId,metrics)
                    !style.mystery -> levelCosmeticProgress(level,style.unlockLevel)
                    else -> null
                }
                AvatarCard(style,isSelected,banner,frame,unlocked,progress,Modifier.weight(1f)){onSelect(style)}
            }
            if(row.size==1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
    }
}

@Composable
private fun AvatarCard(
    style:PlayerAvatarStyle,selected:Boolean,banner:PlayerBannerStyle,frame:PlayerFrameStyle,unlocked:Boolean,
    progress:CosmeticProgress?,modifier:Modifier,onClick:()->Unit
) {
    val title=if(!unlocked&&style.mystery) lockedAvatarTitle(style) else style.displayName
    val status=when{selected->"ÉQUIPÉ";unlocked->"ÉQUIPER";style.mystery->lockedAvatarCondition(style);else->"Débloqué au niveau ${style.unlockLevel}"}
    Column(
        modifier.background(Color(0xFF081329),RoundedCornerShape(17.dp)).border(if(selected)1.6.dp else 1.dp,if(selected)CGreen else CBorder,RoundedCornerShape(17.dp)).clickable(enabled=unlocked,onClick=onClick).padding(10.dp),
        horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment=Alignment.Center){
            Box(if(!unlocked&&style.mystery)Modifier.blur(6.dp) else Modifier){CyberAvatarView(style,banner,frame,{if(unlocked)onClick()},62.dp,showEditBadge=false,syncShopSelection=false)}
            if(!unlocked) Text("🔒",fontSize=13.sp,modifier=Modifier.align(Alignment.BottomEnd))
        }
        Text(title,color=CText,fontSize=10.5.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=2)
        Text(status,color=if(unlocked)CCyan else COrange,fontSize=7.5.sp,lineHeight=10.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=3)
        if(progress!=null) CosmeticProgressBar(progress)
    }
}

@Composable
private fun BannerGrid(
    styles:List<PlayerBannerStyle>,level:Int,selected:PlayerBannerStyle,avatar:PlayerAvatarStyle,frame:PlayerFrameStyle,
    shopBannerActive:Boolean,onSelect:(PlayerBannerStyle)->Unit
) {
    styles.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            row.forEach { style ->
                val unlocked=isBannerUnlocked(style,level)
                val isSelected=selected==style&&!shopBannerActive
                val progress=if(!unlocked&&!style.mystery) levelCosmeticProgress(level,style.unlockLevel) else null
                BannerCard(style,isSelected,avatar,frame,unlocked,progress,Modifier.weight(1f)){onSelect(style)}
            }
            if(row.size==1)Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
    }
}

@Composable
private fun BannerCard(
    style:PlayerBannerStyle,selected:Boolean,avatar:PlayerAvatarStyle,frame:PlayerFrameStyle,unlocked:Boolean,
    progress:CosmeticProgress?,modifier:Modifier,onClick:()->Unit
) {
    Column(modifier.background(Color(0xFF081329),RoundedCornerShape(17.dp)).border(if(selected)1.6.dp else 1.dp,if(selected)CGreen else CBorder,RoundedCornerShape(17.dp)).clickable(enabled=unlocked,onClick=onClick).padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Box(if(!unlocked&&style.mystery)Modifier.blur(6.dp) else Modifier){CyberAvatarView(avatar,style,frame,{if(unlocked)onClick()},60.dp,showEditBadge=false,syncShopSelection=false)}
        Text(if(!unlocked&&style.mystery)"???" else style.displayName,color=CText,fontSize=10.5.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
        Text(when{selected->"ÉQUIPÉE";unlocked->"ÉQUIPER";style.mystery->"Défi à venir";else->"Niveau ${style.unlockLevel}"},color=if(unlocked)CCyan else COrange,fontSize=7.5.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
        if(progress!=null)CosmeticProgressBar(progress)
    }
}

private fun frameProgress(style:PlayerFrameStyle,level:Int,metrics:EngagementMetrics?):CosmeticProgress? = when {
    style.coinCost>0 -> if(level<style.unlockLevel) levelCosmeticProgress(level,style.unlockLevel) else null
    style.achievementId!=null && metrics!=null -> achievementCosmeticProgress(style.achievementId,metrics)
    style.unlockLevel>1 -> levelCosmeticProgress(level,style.unlockLevel)
    else -> null
}

@Composable
private fun FrameCard(
    style:PlayerFrameStyle,selected:Boolean,avatar:PlayerAvatarStyle,banner:PlayerBannerStyle,
    unlocked:Boolean,canBuy:Boolean,progress:CosmeticProgress?,modifier:Modifier,onClick:()->Unit
) {
    val status=when{selected->"ÉQUIPÉ";unlocked->"ÉQUIPER";canBuy->"ACHETER · ◈ ${style.coinCost}";style.shopItem->"BOUTIQUE · 10 ◈";style.coinCost>0->"◈ ${style.coinCost}";style.achievementId!=null->style.subtitle;else->"Niveau ${style.unlockLevel}"}
    Column(modifier.background(Color(0xFF081329),RoundedCornerShape(17.dp)).border(if(selected)1.6.dp else 1.dp,if(selected)CGreen else CBorder,RoundedCornerShape(17.dp)).clickable(enabled=unlocked||canBuy,onClick=onClick).padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        CyberAvatarView(avatar,banner,style,{if(unlocked||canBuy)onClick()},61.dp,showEditBadge=false)
        Text(if(style.mystery&&!unlocked)"???" else style.displayName,color=CText,fontSize=10.5.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=2)
        Text(status,color=when{selected->CGreen;unlocked->CCyan;canBuy->COrange;else->CMuted},fontSize=7.5.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,maxLines=2)
        if(!unlocked&&progress!=null)CosmeticProgressBar(progress)
    }
}