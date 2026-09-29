package com.luntik.twothrones

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luntik.twothrones.game.Country
import com.luntik.twothrones.game.GameState
import com.luntik.twothrones.game.NewsItem
import com.luntik.twothrones.game.Region
import com.luntik.twothrones.game.Side

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

private object T {
    val Bg = Color(0xFF0A0A10)
    val Card = Color.White.copy(alpha = 0.08f)
    val Border = Color.White.copy(alpha = 0.12f)
    val Text = Color(0xFFF2F2F7)
    val Dim = Color.White.copy(alpha = 0.55f)
    val Mute = Color.White.copy(alpha = 0.35f)
    val Accent = Color(0xFF8B9CFF)
    val Light = Color(0xFFFFD56A)
    val Dark = Color(0xFFFF6B7A)
    val Green = Color(0xFF5CFFB0)
    val Player = Color(0xFF5B8CFF)
    val Ally = Color(0xFF6BCB77)
    val Enemy = Color(0xFFE74C3C)
}

private enum class Screen { Lobby, Shop, Match }

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.Lobby) }
    val game = remember { GameState() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(T.Bg, Color(0xFF12121A), T.Bg)))
    ) {
        when (screen) {
            Screen.Lobby -> LobbyScreen(
                game = game,
                onPlay = { screen = Screen.Match },
                onShop = { screen = Screen.Shop }
            )
            Screen.Shop -> ShopScreen(
                game = game,
                onBack = { screen = Screen.Lobby }
            )
            Screen.Match -> MatchScreen(
                game = game,
                onBack = { screen = Screen.Lobby }
            )
        }
    }
}

@Composable
private fun LobbyScreen(game: GameState, onPlay: () -> Unit, onShop: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Два Престола", color = T.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Свет и Тьма · карта регионов", color = T.Dim, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyChip("Серебро", "${game.silver}")
                CurrencyChip("Золото", "${game.gold}")
                CurrencyChip("Кристаллы", "${game.crystals}")
            }
            Spacer(Modifier.height(24.dp))
            MenuCard("Магазин", "Правители и армия") { onShop() }
            Spacer(Modifier.height(10.dp))
            MenuCard("Коллекция", "6 правителей · 10 типов войск") { }
        }
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(T.Accent)
                    .clickable(onClick = onPlay)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("ИГРАТЬ", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CurrencyChip(label: String, value: String) {
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(T.Card)
            .border(1.dp, T.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, color = T.Mute, fontSize = 11.sp)
        Text(value, color = T.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MenuCard(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(T.Card)
            .border(1.dp, T.Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(title, color = T.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = T.Dim, fontSize = 13.sp)
    }
}

@Composable
private fun ShopScreen(game: GameState, onBack: () -> Unit) {
    var toast by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text("← Назад", color = T.Accent, modifier = Modifier.clickable(onClick = onBack))
        Spacer(Modifier.height(8.dp))
        Text("Магазин", color = T.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Серебро: ${game.silver}", color = T.Green, fontSize = 14.sp)
        toast?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = T.Accent, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val sections = game.shopItems.groupBy { it.section }
            sections.forEach { (section, items) ->
                item {
                    Text(section, color = T.Light, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp))
                }
                items(items, key = { it.id }) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(T.Card)
                            .border(1.dp, T.Border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.title, color = T.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(item.subtitle, color = T.Dim, fontSize = 12.sp)
                        }
                        Text(
                            if (item.priceSilver <= 0) "Есть" else "${item.priceSilver} Ag",
                            color = if (item.priceSilver <= 0) T.Green else T.Accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(T.Card)
                                .clickable {
                                    toast = game.buyShopItem(item)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchScreen(game: GameState, onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) } // 0 map, 1 news
    var selectedCountryId by remember { mutableStateOf<String?>(null) }
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // top bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← Выход", color = T.Accent, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onBack))
            Text("Год ${game.year}", color = T.Light, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("${game.playerMoney}💰", color = T.Green, fontSize = 14.sp)
        }

        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Карта", "Новости").forEachIndexed { i, label ->
                val sel = tab == i
                Text(
                    label,
                    color = if (sel) T.Accent else T.Mute,
                    fontSize = 14.sp,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) T.Card else Color.Transparent)
                        .clickable {
                            tab = i
                            selectedCountryId = null
                            selectedRegionId = null
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "+1 год",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(T.Accent)
                    .clickable { game.nextYear(); status = "Год ${game.year}. Доход с земель." }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        status?.let {
            Text(it, color = T.Dim, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        when {
            selectedCountryId != null -> {
                val c = game.country(selectedCountryId!!)
                if (c != null) CountryPanel(game, c) {
                    selectedCountryId = null
                    status = null
                } else selectedCountryId = null
            }
            selectedRegionId != null -> {
                val reg = game.regions.find { it.id == selectedRegionId }
                if (reg != null) RegionPanel(game, reg,
                    onClose = { selectedRegionId = null; status = null },
                    onOpenCountry = { selectedCountryId = it; selectedRegionId = null },
                    onCapture = {
                        status = game.tryCapture(reg.id)
                        selectedRegionId = null
                    }
                ) else selectedRegionId = null
            }
            tab == 1 -> NewsPanel(game)
            else -> MapPanel(game) { selectedRegionId = it }
        }
    }
}

@Composable
private fun MapPanel(game: GameState, onRegion: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            "Тап по клетке — регион. Свои / союз / враг.",
            color = T.Mute,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))
        // легенда
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendDot(T.Player, "Ты")
            LegendDot(T.Ally, "Союз")
            LegendDot(T.Enemy, "Враг")
        }
        Spacer(Modifier.height(10.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (r in 0 until game.mapRows) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (c in 0 until game.mapCols) {
                        val reg = game.regions.find { it.row == r && it.col == c }!!
                        val owner = game.country(reg.ownerId)
                        val color = when {
                            reg.ownerId == "player" -> T.Player
                            owner?.side == Side.LIGHT -> T.Ally
                            else -> T.Enemy
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.55f))
                                .border(1.dp, color.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                .clickable { onRegion(reg.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                reg.name.take(6),
                                color = T.Text,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Твои регионы: ${game.regionsOf("player").size} · доход/год ~${game.regionsOf("player").sumOf { it.income }}",
            color = T.Dim,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, color = T.Mute, fontSize = 11.sp)
    }
}

@Composable
private fun RegionPanel(
    game: GameState,
    reg: Region,
    onClose: () -> Unit,
    onOpenCountry: (String) -> Unit,
    onCapture: () -> Unit
) {
    val owner = game.country(reg.ownerId)
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("← К карте", color = T.Accent, modifier = Modifier.clickable(onClick = onClose))
        Spacer(Modifier.height(12.dp))
        Text(reg.name, color = T.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Доход: ${reg.income} / год", color = T.Green, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text("Владелец: ${owner?.name ?: "?"}", color = T.Dim, fontSize = 15.sp)
        if (owner != null && !owner.isPlayer) {
            Text(
                "Отношения: ${owner.clampedRelations()} · " +
                    if (owner.recognizedByPlayer) "признана" else "не признана",
                color = T.Mute,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        if (owner != null && !owner.isPlayer) {
            ActionBtn("Дипломатия: ${owner.name}") { onOpenCountry(owner.id) }
            Spacer(Modifier.height(8.dp))
            ActionBtn("Атаковать регион (100💰)") { onCapture() }
        } else {
            Text("Это твоя земля.", color = T.Green, fontSize = 14.sp)
        }
    }
}

@Composable
private fun NewsPanel(game: GameState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(game.news.size) { idx ->
            val n = game.news[idx]
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(T.Card)
                    .padding(12.dp)
            ) {
                Text("Год ${n.year}", color = T.Mute, fontSize = 11.sp)
                Text(n.text, color = T.Text, fontSize = 14.sp)
                n.spyDetail?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Доклад: $it", color = T.Accent, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CountryPanel(game: GameState, c: Country, onClose: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("← Назад", color = T.Accent, modifier = Modifier.clickable(onClick = onClose))
        Spacer(Modifier.height(12.dp))
        Text(c.name, color = T.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            if (c.side == Side.LIGHT) "Свет" else "Тьма",
            color = if (c.side == Side.LIGHT) T.Light else T.Dark
        )
        Spacer(Modifier.height(8.dp))
        Text("Отношения: ${c.clampedRelations()} / ${c.effectiveRelationsCap()}", color = T.Dim)
        Text(
            if (c.recognizedByPlayer) "Признана державой" else "Не признана (потолок 50)",
            color = T.Mute,
            fontSize = 13.sp
        )
        Text("Усталость от войны: ${c.warFatigue}", color = if (c.warFatigue > 50) T.Dark else T.Dim)
        Text("Регионов: ${game.regionsOf(c.id).size}", color = T.Dim, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Text("Религии:", color = T.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        c.religions.forEach {
            Text("${it.name}: ${it.percent}%", color = T.Dim, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        if (!c.recognizedByPlayer) {
            ActionBtn("Признать державой") { game.recognize(c.id) }
            Spacer(Modifier.height(8.dp))
        }
        val war = game.wars.find {
            it.endYear == null &&
                ((it.attackerId == "player" && it.defenderId == c.id) ||
                    (it.defenderId == "player" && it.attackerId == c.id))
        }
        if (war != null) {
            ActionBtn("Заключить мир") { game.makePeace(war.id) }
        } else {
            ActionBtn("Объявить войну") { game.declareWar("player", c.id) }
        }
        Spacer(Modifier.height(8.dp))
        ActionBtn("Попросить денег") {
            game.news.add(
                0,
                NewsItem(
                    game.year,
                    "${c.name} отклонила просьбу о деньгах.",
                    "За 300 денег — согласимся."
                )
            )
        }
    }
}

@Composable
private fun ActionBtn(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(T.Accent)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
