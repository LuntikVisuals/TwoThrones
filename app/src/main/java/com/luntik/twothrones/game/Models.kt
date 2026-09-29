package com.luntik.twothrones.game

enum class Side { LIGHT, DARK }

data class ReligionShare(
    val name: String,
    val percent: Int
)

data class Region(
    val id: String,
    val name: String,
    val row: Int,
    val col: Int,
    var ownerId: String,
    val income: Int = 20
)

data class Country(
    val id: String,
    val name: String,
    val side: Side,
    val isPlayer: Boolean = false,
    var money: Int = 1000,
    var relationsToPlayer: Int = 50,
    var recognizedByPlayer: Boolean = false,
    var stability: Int = 70,
    var happiness: Int = 60,
    var warFatigue: Int = 0,
    val religions: List<ReligionShare> = listOf(
        ReligionShare("Христианство", 50),
        ReligionShare("Вальхалла", 30),
        ReligionShare("Ислам", 20)
    )
) {
    fun effectiveRelationsCap(): Int = if (recognizedByPlayer) 100 else 50
    fun clampedRelations(): Int = relationsToPlayer.coerceIn(0, effectiveRelationsCap())
}

data class War(
    val id: String,
    val name: String,
    val attackerId: String,
    val defenderId: String,
    val startYear: Int,
    var endYear: Int? = null,
    var peaceName: String? = null
) {
    fun duration(currentYear: Int): Int = (endYear ?: currentYear) - startYear
}

data class NewsItem(
    val year: Int,
    val text: String,
    val spyDetail: String? = null
)

data class ShopItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceSilver: Int,
    val section: String
)

object WarFatigue {
    fun tick(country: Country, atWar: Boolean) {
        if (atWar) {
            country.warFatigue = (country.warFatigue + 4).coerceAtMost(100)
            if (country.warFatigue > 25) country.happiness = (country.happiness - 1).coerceAtLeast(0)
            if (country.warFatigue > 50) country.stability = (country.stability - 1).coerceAtLeast(0)
        } else {
            country.warFatigue = (country.warFatigue - 6).coerceAtLeast(0)
        }
    }

    fun armyPenalty(fatigue: Int): Float = when {
        fatigue >= 76 -> 0.70f
        fatigue >= 51 -> 0.85f
        fatigue >= 26 -> 0.95f
        else -> 1f
    }

    fun upkeepMultiplier(fatigue: Int): Float = when {
        fatigue >= 51 -> 1.25f
        fatigue >= 26 -> 1.10f
        else -> 1f
    }
}
