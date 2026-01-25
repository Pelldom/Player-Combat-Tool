package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Definition for a system-specific feat or ability that can be applied to a character.
 *
 * This is a reference data model - it defines what feats/abilities are available
 * for each game system, but does not track active instances of feats/abilities.
 *
 * **Intended Use:**
 * - Reference data loaded from JSON assets
 * - UI selection lists for adding feats/abilities
 * - Display-only information about feat/ability properties
 *
 * **Key Characteristics:**
 * - Immutable data class
 * - Serializable for JSON loading
 * - No behavior or logic
 * - No tracking of active instances
 */
@Serializable
data class FeatAbilityDefinition(
    /**
     * Unique identifier for this feat/ability within its system.
     * Should be stable and URL-safe (e.g., "weapon_focus", "power_attack").
     */
    val id: String,

    /**
     * Display name of the feat/ability.
     * Should match official feat/ability name.
     */
    val name: String,

    /**
     * The game system this feat/ability belongs to.
     */
    val system: GameSystem,

    /**
     * Description of the feat/ability.
     */
    val description: String,

    /**
     * Default duration in rounds.
     * - If 0: feat/ability is passive (does not decrement, persists until End Combat)
     * - If > 0: feat/ability is timed (decrements normally)
     * - If null: feat/ability is indefinite (user must specify duration)
     * 
     * Note: This is a suggestion only - users can override.
     */
    val defaultDuration: Int? = null,

    /**
     * Default color ID to use when displaying this feat/ability.
     * Uses EffectColorId enum for consistency.
     */
    val defaultColorId: EffectColorId,

    /**
     * List of modifiers that this feat/ability applies.
     * Each modifier specifies a type, target, and value.
     * Empty list if the feat/ability has no modifiers.
     */
    val modifiers: List<FeatAbilityModifierEntry> = emptyList(),
)
