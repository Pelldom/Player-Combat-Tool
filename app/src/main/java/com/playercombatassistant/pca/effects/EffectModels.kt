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
    /**
     * The combat round when this effect was added/started.
     * Set when the effect is created.
     */
    val startRound: Int,
    /**
     * The combat round when this effect ends (inclusive).
     * 
     * Rules:
     * - If [remainingRounds] is not null (finite duration):
     *   endRound = startRound + initialDuration
     * - If [remainingRounds] is null (indefinite effect):
     *   endRound = null
     */
    val endRound: Int?,
    /**
     * Color ID for displaying this effect in the round tracker.
     * Uses EffectColorId enum for consistency.
     * Defaults to PRIMARY for backward compatibility with old data.
     */
    val colorId: EffectColorId = EffectColorId.PRIMARY,
)

