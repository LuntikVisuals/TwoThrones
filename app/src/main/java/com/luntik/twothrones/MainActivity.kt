package com.luntik.twothrones

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luntik.twothrones.game.Country
import com.luntik.twothrones.game.GameState
import com.luntik.twothrones.game.Side

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

private enum class Screen { Lobby, Match }

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
                onPlay = { screen = Screen.Match }
            )
            Screen.Match -> MatchScreen(
                game = game,
                onBack = { screen = Screen.Lobby }
            )
        }
    }
}

@Composable
private fun LobbyScreen(onPlay: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Два Престола", color = T.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Свет и Тьма · свои государства в союзе", color = T.Dim, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CurrencyChip("Серебро", "1200")
                CurrencyChip("Золото", "40")
                CurrencyChip("Кристаллы", "5")
            }
            Spacer(Modifier.height(24.dp))
            MenuCard("Магазин", "Правители и армия · ротация 24ч")
            Spacer(Modifier.height(10.dp))
            MenuCard("Коллекция", "6 правителей · 10 типов войск")
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
            Spacer(Modifier.height(24.dp))
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
private fun MenuCard(title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(T.Card)
            .border(1.dp, T.Border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(title, color = T.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = T.Dim, fontSize = 13.sp)
    }
}

@Composable
private fun MatchScreen(game: GameState, onBack: () -> Unit) {
    var tab by remember { mutableStateOf(0) } // 0 map, 1 news, 2 country
    var selectedId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← Лобби", color = T.Accent, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onBack))
            Text("Год ${game.year}", color = T.Light, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("${game.playerMoney}💰", color = T.Green, fontSize = 14.sp)
        }

        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Страны", "Новости").forEachIndexed { i, label ->
                val sel = tab == i
                Text(
                    label,
                    color = if (sel) T.Accent else T.Mute,
                    fontSize = 14.sp,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) T.Card else Color.Transparent)
                        .clickable { tab = i; selectedId = null }
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
                    .clickable { game.nextYear() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        when {
            selectedId != null -> {
                val c = game.countries.find { it.id == selectedId }
                if (c != null) CountryPanel(game, c) { selectedId = null }
                else selectedId = null
            }
            tab == 1 -> NewsPanel(game)
            else -> CountriesPanel(game) { selectedId = it }
        }
    }
}

@Composable
private fun CountriesPanel(game: GameState, onOpen: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(game.countries, key = { it.id }) { c ->
            val atWar = game.isAtWar(c.id)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(T.Card)
                    .border(1.dp, T.Border, RoundedCornerShape(14.dp))
                    .clickable { if (!c.isPlayer) onOpen(c.id) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        c.name + if (c.isPlayer) " (ты)" else "",
                        color = T.Text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (c.side == Side.LIGHT) "Свет" else "Тьма",
                        color = if (c.side == Side.LIGHT) T.Light else T.Dark,
                        fontSize = 12.sp
                    )
                    if (!c.isPlayer) {
                        Text(
                            "Отношения: ${c.clampedRelations()}/100" +
                                if (!c.recognizedByPlayer) " · не признана" else "",
                            color = T.Dim,
                            fontSize = 12.sp
                        )
                    }
                    if (atWar) {
                        Text("Война · усталость ${c.warFatigue}", color = T.Dark, fontSize = 12.sp)
                    }
                }
            }
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
            if (c.recognizedByPlayer) "Признана державой" else "Не признана (потолок отношений 50)",
            color = T.Mute,
            fontSize = 13.sp
        )
        Text("Усталость от войны: ${c.warFatigue}", color = if (c.warFatigue > 50) T.Dark else T.Dim)
        Text("Стабильность: ${c.stability} · Счастье: ${c.happiness}", color = T.Dim, fontSize = 13.sp)

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
        ActionBtn("Попросить денег (отказ с торгом)") {
            game.news.add(
                0,
                com.luntik.twothrones.game.NewsItem(
                    game.year,
                    "${c.name} отклонила просьбу о деньгах.",
                    "Причина: просто так не дадим. За 300 денег — согласимся."
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
