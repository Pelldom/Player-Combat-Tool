package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Enumeration of modifier targets that can be affected by effects.
 * Used to specify what aspect of a character is being modified.
 */
@Serializable
enum class ModifierTarget {
    ATTACK_ROLLS,
    SAVING_THROWS,
    ARMOR_CLASS,
    SKILL_CHECKS,
    INITIATIVE,
    ABILITY_SCORES,
    DAMAGE_ROLLS,
    HIT_POINTS,
    SPELL_ATTACKS,
    SPELL_DCS,
}

/**
 * Extension function to get a human-readable label for a ModifierTarget.
 */
fun ModifierTarget.getDisplayLabel(): String {
    return when (this) {
        ModifierTarget.ATTACK_ROLLS -> "Attack Rolls"
        ModifierTarget.SAVING_THROWS -> "Saving Throws"
        ModifierTarget.ARMOR_CLASS -> "Armor Class"
        ModifierTarget.SKILL_CHECKS -> "Skill Checks"
        ModifierTarget.INITIATIVE -> "Initiative"
        ModifierTarget.ABILITY_SCORES -> "Ability Scores"
        ModifierTarget.DAMAGE_ROLLS -> "Damage Rolls"
        ModifierTarget.HIT_POINTS -> "Hit Points (Bonus/Temporary)"
        ModifierTarget.SPELL_ATTACKS -> "Spell Attacks"
        ModifierTarget.SPELL_DCS -> "Spell DCs"
    }
}
