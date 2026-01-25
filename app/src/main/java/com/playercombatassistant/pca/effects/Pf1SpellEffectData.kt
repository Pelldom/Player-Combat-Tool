package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Data models for deserializing PF1 spell effects JSON file.
 * These models match the exact JSON structure and are used only for loading.
 */

/**
 * A single spell effect entry in the PF1 spell effects JSON file.
 */
@Serializable
data class Pf1SpellEffectEntry(
    val id: String,
    val name: String,
    val system: String,
    val spellLevel: Int,
    val description: String,
    val modifiers: List<SpellModifierEntry>,
    val durationRounds: Int?,
    val colorId: String? = null,
)

/**
 * A modifier entry within a spell effect definition.
 * Specifies modifier type, target, and value.
 */
@Serializable
data class SpellModifierEntry(
    val modifierType: String? = null,
    val modifierTarget: String,
    val modifierValue: Int,
)
