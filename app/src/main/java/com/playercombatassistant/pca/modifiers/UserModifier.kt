package com.playercombatassistant.pca.modifiers

import com.playercombatassistant.pca.effects.EffectColorId
import com.playercombatassistant.pca.effects.GenericEffect
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of modifier that can be applied.
 * Used to categorize user-defined modifiers for better organization and display.
 */
@Serializable
enum class ModifierType {
    ARMOR_CLASS,
    ATTACK_ROLLS,
    DAMAGE_ROLLS,
    ABILITY_SCORE,
    SKILL_CHECK,
    SAVING_THROW,
    SPEED,
    INITIATIVE,
    HIT_POINTS,
    REACTIONS,
    SPELL_ATTACKS,
    SPELL_DCS,
    OTHER,
}

/**
 * Data model for user-defined modifiers.
 *
 * This model allows users to create custom modifiers that can be applied as effects.
 * Modifiers can be numeric (with a value) or textual (with free text description).
 *
 * **Key Features:**
 * - Supports both numeric and textual modifiers
 * - Can be converted to GenericEffect for application
 * - Includes duration tracking
 * - Color-coded by modifier type
 *
 * **Usage:**
 * ```kotlin
 * val modifier = UserModifier(
 *     id = UUID.randomUUID().toString(),
 *     modifierType = ModifierType.ARMOR_CLASS,
 *     sign = -1, // negative modifier
 *     value = 2, // -2 AC
 *     durationRounds = 3,
 *     startRound = currentRound,
 * )
 * val effect = modifier.toGenericEffect()
 * ```
 */
@Serializable
data class UserModifier(
    /**
     * Unique identifier for this modifier.
     * Should be generated when the modifier is created.
     */
    val id: String,

    /**
     * The type of modifier (e.g., ARMOR_CLASS, ATTACK_ROLLS).
     * Used for categorization and default color assignment.
     */
    val modifierType: ModifierType,

    /**
     * Sign of the modifier: 1 for positive (+), -1 for negative (-).
     * Used in combination with [value] to determine the final modifier amount.
     */
    val sign: Int,

    /**
     * Numeric modifier value (optional).
     * If provided, the final modifier is sign * value (e.g., sign=-1, value=2 = -2).
     * If null, the modifier is textual only (uses [freeText]).
     */
    val value: Int? = null,

    /**
     * Free text description for the modifier (optional).
     * Used when the modifier cannot be expressed as a simple numeric value
     * (e.g., "Advantage", "Disadvantage", "Cannot use reactions").
     * If [value] is null, this should be provided.
     */
    val freeText: String? = null,

    /**
     * Number of rounds this modifier lasts.
     * Default is 1 round.
     * If set to null, the modifier is indefinite (converted to null in GenericEffect).
     */
    val durationRounds: Int? = 1,

    /**
     * The combat round when this modifier was applied.
     * Set when the modifier is applied to combat.
     */
    val startRound: Int,

    /**
     * Color ID for visual display.
     * Defaults to a color based on [modifierType], but can be overridden.
     */
    val colorId: EffectColorId = modifierType.defaultColorId(),
) {
    init {
        require(sign == 1 || sign == -1) { "Sign must be 1 (positive) or -1 (negative)" }
        require(value != null || freeText != null) {
            "Either value or freeText must be provided"
        }
        require(durationRounds == null || durationRounds > 0) {
            "Duration must be null (indefinite) or positive"
        }
    }

    /**
     * Get the display name for this modifier.
     * Combines type and value/freeText into a readable string.
     */
    fun getDisplayName(): String {
        val typeName = modifierType.getDisplayName()
        return when {
            value != null -> {
                val modifierValue = sign * value
                val signStr = if (modifierValue >= 0) "+" else ""
                "$typeName $signStr$modifierValue"
            }
            freeText != null -> "$typeName: $freeText"
            else -> typeName
        }
    }

    /**
     * Get the notes/description for this modifier.
     * Combines type information with any free text.
     */
    fun getNotes(): String {
        val typeName = modifierType.getDisplayName()
        return when {
            value != null && freeText != null -> {
                val modifierValue = sign * value
                val signStr = if (modifierValue >= 0) "+" else ""
                "$typeName $signStr$modifierValue. $freeText"
            }
            value != null -> {
                val modifierValue = sign * value
                val signStr = if (modifierValue >= 0) "+" else ""
                "$typeName $signStr$modifierValue"
            }
            freeText != null -> "$typeName: $freeText"
            else -> typeName
        }
    }

    /**
     * Convert this UserModifier to a GenericEffect for application.
     *
     * The GenericEffect will have:
     * - Name: Display name from getDisplayName()
     * - Notes: Description from getNotes()
     * - Color: The colorId from this modifier
     * - Duration: durationRounds (null if null or <= 0, indicating indefinite)
     * - Start round: startRound from this modifier
     */
    fun toGenericEffect(): GenericEffect {
        return GenericEffect(
            id = id,
            name = getDisplayName(),
            notes = getNotes(),
            colorId = colorId,
            startRound = startRound,
            durationRounds = durationRounds?.takeIf { it > 0 },
        )
    }
}

/**
 * Get the default color ID for a modifier type.
 * 
 * This mapping assigns semantic Material 3 colors to each modifier type for visual distinction.
 * The actual rendered colors (e.g., teal, red, orange) come from the Material theme color scheme
 * and adapt to light/dark mode and dynamic color themes.
 * 
 * Color Mapping:
 * - ARMOR_CLASS → PRIMARY_CONTAINER (defensive, typically teal/cyan tones)
 * - ATTACK_ROLLS → ERROR (aggressive, typically red tones)
 * - DAMAGE_ROLLS → ERROR_CONTAINER (damage dealing, typically orange/red tones)
 * - ABILITY_SCORE → TERTIARY (core attributes, typically purple tones)
 * - SKILL_CHECK → SECONDARY (skills, typically blue tones)
 * - SAVING_THROW → SECONDARY_CONTAINER (defensive saves, typically green/blue tones)
 * - SPEED → PRIMARY (movement, typically cyan/primary tones)
 * - INITIATIVE → TERTIARY_CONTAINER (turn order, typically yellow/amber tones)
 * - HIT_POINTS → ERROR (health, typically pink/red tones)
 * - REACTIONS → PRIMARY_CONTAINER (reactive abilities, typically grey/neutral tones)
 * - SPELL_ATTACKS → TERTIARY (magical attacks, typically indigo/purple tones)
 * - SPELL_DCS → SECONDARY_CONTAINER (spell difficulty, typically lavender/purple tones)
 * - OTHER → PRIMARY (default/fallback, uses theme primary color)
 * 
 * This mapping is used automatically when creating UserModifier instances without
 * a custom color override. Users can override these defaults via the color selector
 * in the ModifierBuilderSheet.
 */
fun ModifierType.defaultColorId(): EffectColorId {
    return when (this) {
        ModifierType.ARMOR_CLASS -> EffectColorId.PRIMARY_CONTAINER
        ModifierType.ATTACK_ROLLS -> EffectColorId.ERROR
        ModifierType.DAMAGE_ROLLS -> EffectColorId.ERROR_CONTAINER
        ModifierType.ABILITY_SCORE -> EffectColorId.TERTIARY
        ModifierType.SKILL_CHECK -> EffectColorId.SECONDARY
        ModifierType.SAVING_THROW -> EffectColorId.SECONDARY_CONTAINER
        ModifierType.SPEED -> EffectColorId.PRIMARY
        ModifierType.INITIATIVE -> EffectColorId.TERTIARY_CONTAINER
        ModifierType.HIT_POINTS -> EffectColorId.ERROR
        ModifierType.REACTIONS -> EffectColorId.PRIMARY_CONTAINER
        ModifierType.SPELL_ATTACKS -> EffectColorId.TERTIARY
        ModifierType.SPELL_DCS -> EffectColorId.SECONDARY_CONTAINER
        ModifierType.OTHER -> EffectColorId.PRIMARY
    }
}

/**
 * Get a human-readable display name for a modifier type.
 */
fun ModifierType.getDisplayName(): String {
    return when (this) {
        ModifierType.ARMOR_CLASS -> "AC"
        ModifierType.ATTACK_ROLLS -> "Attack"
        ModifierType.DAMAGE_ROLLS -> "Damage"
        ModifierType.ABILITY_SCORE -> "Ability Score"
        ModifierType.SKILL_CHECK -> "Skill Check"
        ModifierType.SAVING_THROW -> "Saving Throw"
        ModifierType.SPEED -> "Speed"
        ModifierType.INITIATIVE -> "Initiative"
        ModifierType.HIT_POINTS -> "Hit Points"
        ModifierType.REACTIONS -> "Reactions"
        ModifierType.SPELL_ATTACKS -> "Spell Attack"
        ModifierType.SPELL_DCS -> "Spell DC"
        ModifierType.OTHER -> "Other"
    }
}
