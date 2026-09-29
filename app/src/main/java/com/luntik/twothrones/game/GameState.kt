package com.luntik.twothrones.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameState {
    var year by mutableIntStateOf(100)
    var playerMoney by mutableIntStateOf(1500)

    val countries = mutableStateListOf(
        Country("player", "Твоя держава", Side.LIGHT, isPlayer = true, money = 1500, relationsToPlayer = 100, recognizedByPlayer = true),
        Country("ally1", "Северный союз", Side.LIGHT, relationsToPlayer = 72, recognizedByPlayer = true),
        Country("ally2", "Восточный край", Side.LIGHT, relationsToPlayer = 65, recognizedByPlayer = true),
        Country("ally3", "Приморье", Side.LIGHT, relationsToPlayer = 58, recognizedByPlayer = false),
        Country("enemy1", "Тёмная империя", Side.DARK, relationsToPlayer = 22, recognizedByPlayer = true),
        Country("enemy2", "Орда", Side.DARK, relationsToPlayer = 15, recognizedByPlayer = false),
        Country("enemy3", "Чёрный престол", Side.DARK, relationsToPlayer = 8, recognizedByPlayer = true)
    )

    val wars = mutableStateListOf<War>()
    val news = mutableStateListOf<NewsItem>()

    init {
        news.add(0, NewsItem(100, "Начало эпохи. Год 100. Державы занимают позиции."))
    }

    fun player(): Country = countries.first { it.isPlayer }

    fun isAtWar(countryId: String): Boolean =
        wars.any { it.endYear == null && (it.attackerId == countryId || it.defenderId == countryId) }

    fun declareWar(attackerId: String, defenderId: String) {
        if (wars.any { it.endYear == null &&
                ((it.attackerId == attackerId && it.defenderId == defenderId) ||
                    (it.attackerId == defenderId && it.defenderId == attackerId)) }) return

        val a = countries.find { it.id == attackerId }?.name ?: attackerId
        val d = countries.find { it.id == defenderId }?.name ?: defenderId
        val warName = generateWarName(a, d)
        val war = War(
            id = "w_${year}_${attackerId}_$defenderId",
            name = warName,
            attackerId = attackerId,
            defenderId = defenderId,
            startYear = year
        )
        wars.add(0, war)
        news.add(0, NewsItem(year, "$a объявила войну $d.", "Название войны: $warName"))

        countries.find { it.id == defenderId }?.let {
            if (!it.isPlayer) it.relationsToPlayer = (it.relationsToPlayer - 25).coerceAtLeast(0)
        }
    }

    fun makePeace(warId: String) {
        val war = wars.find { it.id == warId && it.endYear == null } ?: return
        war.endYear = year
        war.peaceName = generatePeaceName()
        val a = countries.find { it.id == war.attackerId }?.name ?: war.attackerId
        val d = countries.find { it.id == war.defenderId }?.name ?: war.defenderId
        val years = war.duration(year)
        news.add(
            0,
            NewsItem(
                year,
                "$a заключила мир с $d. Война продлилась $years лет.",
                "Название войны: ${war.name}. Название мира: ${war.peaceName}."
            )
        )
    }

    fun recognize(countryId: String) {
        val c = countries.find { it.id == countryId } ?: return
        if (c.recognizedByPlayer) return
        c.recognizedByPlayer = true
        news.add(0, NewsItem(year, "Ты признал державу ${c.name}."))
    }

    fun nextYear() {
        year += 1
        countries.forEach { c ->
            WarFatigue.tick(c, isAtWar(c.id))
        }
        // лёгкий доход
        playerMoney += 80
        player().money = playerMoney

        val tired = countries.filter { it.warFatigue >= 50 && isAtWar(it.id) }
        tired.forEach { c ->
            if (c.warFatigue >= 50) {
                news.add(
                    0,
                    NewsItem(
                        year,
                        "Усталость от войны в ${c.name}: ${c.warFatigue}.",
                        "Армия слабее, содержание дороже."
                    )
                )
            }
        }
    }

    private fun generateWarName(a: String, d: String): String {
        val options = listOf(
            "Война $a и $d",
            "Великая война",
            "Северная война",
            "Война за престолы",
            "Пограничная война"
        )
        return options.random()
    }

    private fun generatePeaceName(): String {
        val cities = listOf("Стамбульский", "Венский", "Парижский", "Римский", "Северный")
        return "${cities.random()} договор"
    }
}
