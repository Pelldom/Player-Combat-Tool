package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Definition for a system-specific spell effect that can be applied to a character.
 *
 * This is a reference data model - it defines what spell effects are available
 * for each game system, but does not track active instances of spell effects.
 *
 * **Intended Use:**
 * - Reference data loaded from JSON assets
 * - UI selection lists for adding spell effects
 * - Display-only information about spell effect properties
 *
 * **Key Characteristics:**
 * - Immutable data class
 * - Serializable for JSON loading
 * - No behavior or logic
 * - No tracking of active instances
 */
@Serializable
data class SpellEffectDefinition(
    /**
     * Unique identifier for this spell effect within its system.
     * Should be stable and URL-safe (e.g., "bless", "mage_armor").
     */
    val id: String,

    /**
     * Display name of the spell effect.
     * Should match official spell name.
     */
    val name: String,

    /**
     * The game system this spell effect belongs to.
     */
    val system: GameSystem,

    /**
     * The spell level (1-9 for most spells).
     */
    val spellLevel: Int,

    /**
     * Description of the spell effect.
     */
    val description: String,

    /**
     * Default duration in rounds (optional).
     * - If null: spell effect is indefinite by default (user must specify duration)
     * - If set: suggested default duration when adding this spell effect
     * 
     * Note: This is a suggestion only - users can override.
     */
    val defaultDuration: Int? = null,

    /**
     * Default color ID to use when displaying this spell effect.
     * Uses EffectColorId enum for consistency.
     */
    val defaultColorId: EffectColorId,

    /**
     * List of modifiers that this spell effect applies.
     * Each modifier specifies a type, target, and value.
     * Empty list if the spell effect has no modifiers.
     */
    val modifiers: List<SpellModifierEntry> = emptyList(),
)
