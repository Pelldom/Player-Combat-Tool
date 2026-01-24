package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Data models for deserializing PF1 conditions JSON file.
 * These models match the exact JSON structure and are used only for loading.
 */

/**
 * Root structure of the PF1 conditions JSON file.
 */
@Serializable
data class Pf1ConditionsFile(
    val conditions: List<Pf1ConditionEntry>,
)

/**
 * A single condition entry in the PF1 conditions JSON file.
 */
@Serializable
data class Pf1ConditionEntry(
    val id: String,
    val name: String,
    val system: String,
    val shortDescription: String,
    val modifiers: List<ModifierEntry>,
    val source: String? = null,
)
