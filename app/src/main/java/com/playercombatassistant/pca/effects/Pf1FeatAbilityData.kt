package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Data models for deserializing PF1 feats and abilities JSON file.
 * These models match the exact JSON structure and are used only for loading.
 */

/**
 * A single feat/ability entry in the PF1 feats and abilities JSON file.
 */
@Serializable
data class Pf1FeatAbilityEntry(
    val id: String,
    val name: String,
    val system: String,
    val description: String,
    val modifiers: List<FeatAbilityModifierEntry>,
    val durationRounds: Int?,
    val colorId: String? = null,
)

/**
 * A modifier entry within a feat/ability definition.
 * Specifies modifier type, target, and value.
 */
@Serializable
data class FeatAbilityModifierEntry(
    val modifierType: String? = null,
    val modifierTarget: String,
    val modifierValue: Int,
)
