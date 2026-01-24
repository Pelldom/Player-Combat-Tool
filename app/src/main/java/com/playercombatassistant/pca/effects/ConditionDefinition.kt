package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Definition for a system-specific condition that can be applied to a character.
 *
 * This is a reference data model - it defines what conditions are available
 * for each game system, but does not track active instances of conditions.
 *
 * **Intended Use:**
 * - Reference data loaded from JSON assets
 * - UI selection lists for adding conditions
 * - Display-only information about condition properties
 *
 * **Key Characteristics:**
 * - Immutable data class
 * - Serializable for JSON loading
 * - No behavior or logic
 * - No tracking of active instances
 */
@Serializable
data class ConditionDefinition(
    /**
     * Unique identifier for this condition within its system.
     * Should be stable and URL-safe (e.g., "blinded", "stunned", "shaken").
     */
    val id: String,

    /**
     * Display name of the condition.
     * Should match official terminology where possible.
     */
    val name: String,

    /**
     * The game system this condition belongs to.
     */
    val system: GameSystem,

    /**
     * Brief description of the condition (1-2 sentences).
     * Used in lists and tooltips.
     */
    val shortDescription: String,

    /**
     * Detailed description of the condition (optional).
     * Can include full rules text, stacking rules, etc.
     * If null, shortDescription is used for detailed views.
     */
    val detailedDescription: String? = null,

    /**
     * Default duration in rounds (optional).
     * - If null: condition is indefinite by default (user must specify duration)
     * - If set: suggested default duration when adding this condition
     * 
     * Note: This is a suggestion only - users can override.
     */
    val defaultDuration: Int? = null,

    /**
     * Default color ID to use when displaying this condition.
     * Uses EffectColorId enum for consistency.
     */
    val defaultColorId: EffectColorId,

    /**
     * Tags for categorizing and filtering conditions.
     * Examples: ["debuff", "movement", "senses", "combat"]
     * Empty list if no tags.
     */
    val tags: List<String> = emptyList(),

    /**
     * List of modifiers that this condition applies.
     * Each modifier specifies a target and value.
     * Empty list if the condition has no modifiers.
     */
    val modifiers: List<ModifierEntry> = emptyList(),
)

/**
 * A modifier entry within a condition definition.
 * Specifies what target is modified and what the value is.
 */
@Serializable
data class ModifierEntry(
    /**
     * The target of the modifier (e.g., "Armor Class", "Attack Rolls", "Saving Throws").
     */
    val target: String,

    /**
     * The value of the modifier (e.g., "-2", "-4", "Cannot take actions", "50% concealment").
     * Can be numeric, text, or special values.
     */
    val value: String,
)
