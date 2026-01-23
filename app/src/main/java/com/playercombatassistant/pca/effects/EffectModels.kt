package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

@Serializable
enum class EffectType {
    CONDITION,
    TIMER,
}

@Serializable
enum class GameSystem {
    PF1,
    PF2,
    DND5E,
    SAVAGE_WORLDS,
    DCC,
    GENERIC,
}

@Serializable
data class Modifier(
    val target: String,
    val value: String, // e.g. "-2", "ADV", "DIS"
    val source: String,
)

@Serializable
data class Effect(
    val id: String,
    val name: String,
    val system: GameSystem,
    val description: String,
    val remainingRounds: Int?,
    val type: EffectType,
    val modifiers: List<Modifier>,
)

