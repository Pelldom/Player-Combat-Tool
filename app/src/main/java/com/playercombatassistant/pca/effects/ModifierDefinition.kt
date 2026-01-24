package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Definition for a modifier that can be applied by a condition.
 *
 * This is a reference data model - it defines what modifiers are available
 * for conditions, but does not track active instances of modifiers.
 *
 * **Intended Use:**
 * - Reference data loaded from JSON assets
 * - Display-only information about modifier properties
 * - Standalone modifier definitions that can be referenced
 *
 * **Key Characteristics:**
 * - Immutable data class
 * - Serializable for JSON loading
 * - No behavior or logic
 * - Display-only (no enforcement)
 */
@Serializable
data class ModifierDefinition(
    /**
     * Unique identifier for this modifier within its system.
     * Should be stable and URL-safe (e.g., "ac_penalty", "attack_bonus").
     */
    val id: String,

    /**
     * Display name of the modifier.
     * Should match official terminology where possible.
     */
    val name: String,

    /**
     * The game system this modifier belongs to.
     */
    val system: GameSystem,

    /**
     * List of effects that this modifier applies.
     * Each effect specifies a target and value/description.
     * Empty list if the modifier has no effects.
     */
    val effects: List<EffectEntry> = emptyList(),
)

/**
 * An effect entry within a modifier definition.
 * Specifies what target is affected and what the value/description is.
 */
@Serializable
data class EffectEntry(
    /**
     * The target of the effect (e.g., "Armor Class", "Attack Rolls", "Saving Throws").
     */
    val target: String,

    /**
     * The value or description of the effect (e.g., "-2", "-4", "Cannot take actions", "50% concealment").
     * Can be numeric, text, or special values.
     * 
     * Note: This field may be called "value" or "description" depending on the JSON structure.
     */
    val value: String? = null,
    
    /**
     * Alternative field name for the effect value/description.
     * Used when JSON uses "description" instead of "value".
     */
    val description: String? = null,
) {
    /**
     * Gets the value or description, preferring value over description.
     */
    val valueOrDescription: String
        get() = value ?: description ?: ""
}
