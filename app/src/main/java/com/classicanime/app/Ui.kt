package com.classicanime.app
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

val Bg = Color(0xFF0B0B10); val Gold = Color(0xFFD4A857)
val Ink = Color(0xFFF2F0EA); val Muted = Color(0xFFB8B6C0)

fun Modifier.glass(r: Dp = 24.dp) = this.clip(RoundedCornerShape(r))
    .background(Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))))
    .border(1.dp, Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x0FFFFFFF))), RoundedCornerShape(r))

fun String?.plain() = (this ?: "").replace(Regex("<[^>]*>"), "").trim()

@Composable fun App(vm: HomeVm = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    var sel by remember { mutableStateOf<Media?>(null) }
    Box(Modifier.fillMaxSize().background(Bg).drawBehind {
        drawCircle(Brush.radialGradient(listOf(Color(0x66B0405E), Color.Transparent), Offset(size.width * 0.9f, size.height * 0.1f), 700f), 700f, Offset(size.width * 0.9f, size.height * 0.1f))
        drawCircle(Brush.radialGradient(listOf(Color(0x553A4FB0), Color.Transparent), Offset(0f, size.height * 0.7f), 800f), 800f, Offset(0f, size.height * 0.7f))
    }) {
        if (tab == 0) Home(vm) { sel = it } else Library(vm) { sel = it }
        NavBar(tab, Modifier.align(Alignment.BottomCenter)) { tab = it }
        sel?.let { Detail(it, vm) { sel = null } }
    }
}

@Composable fun Home(vm: HomeVm, open: (Media) -> Unit) {
    val hero = vm.rows[vm.decades[1].first]?.firstOrNull()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(420.dp)) {
                AsyncImage(hero?.bannerImage ?: hero?.coverImage?.large, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Bg), startY = 300f)))
                if (hero != null) Column(Modifier.align(Alignment.BottomStart).padding(20.dp).fillMaxWidth().glass().padding(18.dp)) {
                    Text("CLASSIC OF THE DAY", color = Gold, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(hero.title?.romaji ?: "", color = Ink, fontSize = 28.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
                    Text("${hero.startDate?.year ?: ""} · ${hero.format ?: ""}", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    GoldButton("View") { open(hero) }
                }
            }
        }
        vm.error?.let { item { Text(it, color = Muted, modifier = Modifier.padding(20.dp)) } }
        vm.decades.forEach { (name, _, _) ->
            item {
                Text(name, color = Ink, fontSize = 20.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(vm.rows[name].orEmpty()) { Poster(it) { open(it) } }
                }
            }
        }
    }
}

@Composable fun Library(vm: HomeVm, open: (Media) -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("My Watchlist", color = Ink, fontSize = 28.sp, fontFamily = FontFamily.Serif)
        if (vm.saved.isEmpty()) Text("Save classics from their detail page.", color = Muted, modifier = Modifier.padding(top = 12.dp))
        LazyRow(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { items(vm.saved) { Poster(it) { open(it) } } }
    }
}

@Composable fun Poster(m: Media, onClick: () -> Unit) {
    Column(Modifier.width(120.dp).clickable(onClick = onClick)) {
        AsyncImage(m.coverImage?.large, m.title?.romaji, Modifier.size(120.dp, 172.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
        Text(m.title?.romaji ?: "", color = Ink, fontSize = 13.sp, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
        Text("${m.startDate?.year ?: ""}", color = Muted, fontSize = 12.sp)
    }
}

@Composable fun GoldButton(label: String, onClick: () -> Unit) {
    Box(Modifier.height(48.dp).clip(RoundedCornerShape(24.dp)).background(Gold).clickable(onClick = onClick).padding(horizontal = 28.dp), Alignment.Center) {
        Text(label, color = Bg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable fun NavBar(tab: Int, modifier: Modifier, onTab: (Int) -> Unit) {
    Row(modifier.navigationBarsPadding().padding(20.dp).fillMaxWidth().height(64.dp).glass(32.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
        listOf("Home", "Library").forEachIndexed { i, l ->
            Text(l, color = if (tab == i) Gold else Muted, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onTab(i) }.padding(16.dp))
        }
    }
}

@Composable fun Detail(m: Media, vm: HomeVm, close: () -> Unit) {
    BackHandler(onBack = close)
    val uri = LocalUriHandler.current
    Box(Modifier.fillMaxSize().background(Bg)) {
        AsyncImage(m.coverImage?.large, null, Modifier.fillMaxWidth().height(380.dp).blur(40.dp), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxWidth().height(380.dp).background(Brush.verticalGradient(listOf(Color(0x440B0B10), Bg))))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(20.dp).navigationBarsPadding()) {
            Text("‹ Back", color = Ink, modifier = Modifier.glass(22.dp).clickable(onClick = close).padding(16.dp, 10.dp))
            Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.Bottom) {
                AsyncImage(m.coverImage?.large, null, Modifier.size(130.dp, 188.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(m.title?.romaji ?: "", color = Ink, fontSize = 26.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
                    Text("${m.startDate?.year ?: ""} · ${m.format ?: ""}", color = Muted, fontSize = 13.sp)
                }
            }
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                m.genres.orEmpty().take(3).forEach { Text(it, color = Ink, fontSize = 12.sp, modifier = Modifier.glass(14.dp).padding(12.dp, 6.dp)) }
            }
            Text(m.description.plain(), color = Muted, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 16.dp).fillMaxWidth().glass(20.dp).padding(16.dp))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GoldButton(if (vm.isSaved(m)) "Saved ✓" else "+ Watchlist") { vm.toggle(m) }
            }
            Text("Where to watch", color = Ink, fontSize = 20.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
            val links = m.externalLinks.orEmpty().filter { it.type == "STREAMING" && it.url != null }
            if (links.isEmpty()) Text("No streaming link listed yet.", color = Muted)
            links.forEach { l -> Text("${l.site} ›", color = Gold, modifier = Modifier.padding(bottom = 10.dp).fillMaxWidth().glass(18.dp).clickable { uri.openUri(l.url!!) }.padding(16.dp)) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
