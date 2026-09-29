package com.luntik.twothrones.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

class GameState {
    var year by mutableIntStateOf(100)
    var playerMoney by mutableIntStateOf(1500)

    // мета-валюты лобби
    var silver by mutableIntStateOf(1200)
    var gold by mutableIntStateOf(40)
    var crystals by mutableIntStateOf(5)

    val countries = mutableStateListOf(
        Country("player", "Твоя держава", Side.LIGHT, isPlayer = true, money = 1500, relationsToPlayer = 100, recognizedByPlayer = true),
        Country("ally1", "Северный союз", Side.LIGHT, relationsToPlayer = 72, recognizedByPlayer = true),
        Country("ally2", "Восточный край", Side.LIGHT, relationsToPlayer = 65, recognizedByPlayer = true),
        Country("ally3", "Приморье", Side.LIGHT, relationsToPlayer = 58, recognizedByPlayer = false),
        Country("enemy1", "Тёмная империя", Side.DARK, relationsToPlayer = 22, recognizedByPlayer = true),
        Country("enemy2", "Орда", Side.DARK, relationsToPlayer = 15, recognizedByPlayer = false),
        Country("enemy3", "Чёрный престол", Side.DARK, relationsToPlayer = 8, recognizedByPlayer = true)
    )

    /** Карта 6 рядов × 5 колонок */
    val mapRows = 6
    val mapCols = 5
    val regions = mutableStateListOf<Region>()

    val wars = mutableStateListOf<War>()
    val news = mutableStateListOf<NewsItem>()

    val shopItems = listOf(
        ShopItem("r1", "Карл II Великий", "Тьма · стартовый правитель", 0, "Правители"),
        ShopItem("r2", "Наполеон", "Тьма · экономика и союз", 200, "Правители"),
        ShopItem("r3", "Кутузов", "Свет · жирная армия", 200, "Правители"),
        ShopItem("u1", "Отряд мечников", "5 мечников в коллекцию", 50, "Армия"),
        ShopItem("u2", "Отряд лучников", "5 лучников в коллекцию", 50, "Армия"),
        ShopItem("u3", "Элитный отряд", "Дорогой сильный юнит", 120, "Армия")
    )

    init {
        buildMap()
        news.add(0, NewsItem(100, "Начало эпохи. Год 100. Державы занимают регионы."))
    }

    private fun buildMap() {
        regions.clear()
        // Распределение: верх — свет (player + allies), низ — тьма
        val layout = listOf(
            listOf("player", "player", "ally1", "ally1", "ally2"),
            listOf("player", "player", "ally1", "ally2", "ally2"),
            listOf("ally3", "player", "ally2", "ally3", "ally3"),
            listOf("enemy2", "enemy1", "enemy1", "enemy3", "enemy3"),
            listOf("enemy2", "enemy1", "enemy1", "enemy3", "enemy2"),
            listOf("enemy2", "enemy1", "enemy3", "enemy3", "enemy1")
        )
        val names = listOf(
            "Северные земли", "Столичный край", "Заречье", "Холмы", "Поморье",
            "Долина", "Крепость", "Лес", "Степь", "Порт",
            "Граница", "Перевал", "Озеро", "Поля", "Тракт",
            "Тёмные холмы", "Цитадель", "Пепел", "Ущелье", "Форт",
            "Чёрный лес", "Бастион", "Пустошь", "Скалы", "Застава",
            "Юг", "Бездна", "Красные поля", "Камни", "Окраина"
        )
        var i = 0
        for (r in 0 until mapRows) {
            for (c in 0 until mapCols) {
                regions.add(
                    Region(
                        id = "reg_${r}_$c",
                        name = names[i % names.size],
                        row = r,
                        col = c,
                        ownerId = layout[r][c],
                        income = 15 + (i % 4) * 5
                    )
                )
                i++
            }
        }
    }

    fun player(): Country = countries.first { it.isPlayer }

    fun country(id: String): Country? = countries.find { it.id == id }

    fun regionsOf(ownerId: String): List<Region> = regions.filter { it.ownerId == ownerId }

    fun isAtWar(countryId: String): Boolean =
        wars.any { it.endYear == null && (it.attackerId == countryId || it.defenderId == countryId) }

    fun declareWar(attackerId: String, defenderId: String) {
        if (wars.any {
                it.endYear == null &&
                    ((it.attackerId == attackerId && it.defenderId == defenderId) ||
                        (it.attackerId == defenderId && it.defenderId == attackerId))
            }
        ) return

        val a = country(attackerId)?.name ?: attackerId
        val d = country(defenderId)?.name ?: defenderId
        val warName = generateWarName(a, d)
        wars.add(
            0,
            War(
                id = "w_${year}_${attackerId}_$defenderId",
                name = warName,
                attackerId = attackerId,
                defenderId = defenderId,
                startYear = year
            )
        )
        news.add(0, NewsItem(year, "$a объявила войну $d.", "Название войны: $warName"))
        country(defenderId)?.let {
            if (!it.isPlayer) it.relationsToPlayer = (it.relationsToPlayer - 25).coerceAtLeast(0)
        }
    }

    fun makePeace(warId: String) {
        val war = wars.find { it.id == warId && it.endYear == null } ?: return
        war.endYear = year
        war.peaceName = generatePeaceName()
        val a = country(war.attackerId)?.name ?: war.attackerId
        val d = country(war.defenderId)?.name ?: war.defenderId
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
        val c = country(countryId) ?: return
        if (c.recognizedByPlayer) return
        c.recognizedByPlayer = true
        news.add(0, NewsItem(year, "Ты признал державу ${c.name}."))
    }

    /** Захват соседнего вражеского региона, если есть война */
    fun tryCapture(regionId: String): String {
        val reg = regions.find { it.id == regionId } ?: return "Регион не найден"
        if (reg.ownerId == "player") return "Это уже твой регион"
        val owner = country(reg.ownerId) ?: return "Нет владельца"
        if (!isAtWar("player") || !isAtWar(reg.ownerId)) {
            // война именно с владельцем
            val warWith = wars.any {
                it.endYear == null &&
                    ((it.attackerId == "player" && it.defenderId == reg.ownerId) ||
                        (it.defenderId == "player" && it.attackerId == reg.ownerId))
            }
            if (!warWith) return "Нужна война с ${owner.name}"
        }
        val cost = 100
        if (playerMoney < cost) return "Нужно $cost денег"
        // соседство с твоим регионом
        val mine = regions.filter { it.ownerId == "player" }
        val adjacent = mine.any {
            kotlin.math.abs(it.row - reg.row) + kotlin.math.abs(it.col - reg.col) == 1
        }
        if (!adjacent) return "Регион не на границе"

        playerMoney -= cost
        player().money = playerMoney
        val oldOwner = reg.ownerId
        reg.ownerId = "player"
        news.add(
            0,
            NewsItem(year, "Ты захватил регион «${reg.name}» у ${owner.name}.", "Стоимость кампании: $cost")
        )
        country(oldOwner)?.relationsToPlayer =
            ((country(oldOwner)?.relationsToPlayer ?: 0) - 10).coerceAtLeast(0)
        return "Захвачено: ${reg.name}"
    }

    fun buyShopItem(item: ShopItem): String {
        if (item.priceSilver <= 0) return "Уже доступно"
        if (silver < item.priceSilver) return "Не хватает серебра"
        silver -= item.priceSilver
        return "Куплено: ${item.title}"
    }

    fun nextYear() {
        year += 1
        countries.forEach { c -> WarFatigue.tick(c, isAtWar(c.id)) }
        val income = regionsOf("player").sumOf { it.income }
        playerMoney += income
        player().money = playerMoney
        countries.filter { it.warFatigue >= 50 && isAtWar(it.id) }.forEach { c ->
            news.add(
                0,
                NewsItem(year, "Усталость от войны в ${c.name}: ${c.warFatigue}.", "Армия слабее, содержание дороже.")
            )
        }
    }

    private fun generateWarName(a: String, d: String): String {
        val options = listOf(
            "Война $a и $d", "Великая война", "Северная война",
            "Война за престолы", "Пограничная война"
        )
        return options.random()
    }

    private fun generatePeaceName(): String {
        val cities = listOf("Стамбульский", "Венский", "Парижский", "Римский", "Северный")
        return "${cities.random()} договор"
    }
}
