package com.playercombatassistant.pca.effects

import android.content.Context
import com.playercombatassistant.pca.modifiers.ModifierRepository
import com.playercombatassistant.pca.modifiers.StackingRule

/**
 * Display-only aggregation of numeric modifiers.
 *
 * Rules:
 * - Numeric modifiers only (e.g. "-2", "+1", "3"). Non-numeric values like "ADV" are ignored.
 * - No stacking rules, no bonus types, no enforcement.
 * - Preserves a per-source breakdown for display.
 */
object ModifierAggregation {

    data class SourceBreakdown(
        val source: String,
        val values: List<Int>,
        val sum: Int,
    )

    data class TargetAggregation(
        val target: String,
        val total: Int,
        val sources: List<SourceBreakdown>,
    )

    fun aggregateNumeric(effects: List<Effect>): List<TargetAggregation> {
        val numericMods = buildList {
            for (effect in effects) {
                for (mod in effect.modifiers) {
                    val value = mod.value.toIntOrNullStrict() ?: continue
                    add(Triple(mod.target, mod.source, value))
                }
            }
        }

        val byTarget = numericMods.groupBy { it.first }
        return byTarget
            .map { (target, triples) ->
                val bySource = triples.groupBy { it.second }
                val sources = bySource
                    .map { (source, entries) ->
                        val values = entries.map { it.third }
                        SourceBreakdown(source = source, values = values, sum = values.sum())
                    }
                    .sortedBy { it.source }

                TargetAggregation(
                    target = target,
                    total = triples.sumOf { it.third },
                    sources = sources,
                )
            }
            .sortedBy { it.target }
    }

    private fun String.toIntOrNullStrict(): Int? {
        // Only accept pure integers with optional +/-
        // Examples allowed: "-2", "+1", "0", "12"
        // Examples ignored: "ADV", "DIS", "+2 circumstance", "1d4", "- 2"
        if (!matches(Regex("^[+-]?\\d+$"))) return null
        return toIntOrNull()
    }

    /**
     * Data class for a modifier entry from any source.
     */
    data class ModifierEntry(
        val target: String,
        val value: Int,
        val type: String? = null, // null or missing = "untyped"
    )

    /**
     * Aggregates modifiers from all active effects with system-specific stacking rules.
     *
     * @param activeEffects System-specific effects (from JSON)
     * @param activeGenericEffects User-created generic effects
     * @param gameSystem Current game system for stacking rules
     * @param modifierRepository Repository for loading stacking rules
     * @return Map of target name to total modifier value
     */
    fun aggregateWithStacking(
        activeEffects: List<Effect>,
        activeGenericEffects: List<GenericEffect>,
        gameSystem: GameSystem,
        modifierRepository: ModifierRepository,
    ): Map<String, Int> {
        // Collect all modifier entries from all sources
        val allEntries = mutableListOf<ModifierEntry>()

        // From system-specific Effects
        for (effect in activeEffects) {
            for (mod in effect.modifiers) {
                val value = mod.value.toIntOrNullStrict() ?: continue
                // Effects never have modifierType (always untyped)
                // Normalize target name to match display names
                val normalizedTarget = normalizeTargetName(mod.target)
                allEntries.add(ModifierEntry(target = normalizedTarget, value = value, type = null))
            }
        }

        // From GenericEffects
        for (genericEffect in activeGenericEffects) {
            if (genericEffect.modifierTarget != null && genericEffect.modifierValue != null) {
                val targetName = mapModifierTargetToDisplayName(genericEffect.modifierTarget)
                // null or missing modifierType = "untyped"
                val type = genericEffect.modifierType ?: "untyped"
                allEntries.add(
                    ModifierEntry(
                        target = targetName,
                        value = genericEffect.modifierValue,
                        type = type,
                    ),
                )
            }
        }

        // Get stacking rule for current system
        val stackingRule = modifierRepository.getStackingRuleBySystem(gameSystem)
        val stackableTypes = stackingRule?.stackableTypes?.toSet() ?: emptySet()

        // Group by target
        val byTarget = allEntries.groupBy { it.target }

        // Apply stacking rules per target
        return byTarget.mapValues { (_, entries) ->
            when (gameSystem) {
                GameSystem.PF1, GameSystem.PF2 -> {
                    // Group by type, apply stacking rules
                    // Normalize null/missing types to "untyped"
                    val byType = entries.groupBy { it.type ?: "untyped" }
                    var total = 0
                    for ((type, typeEntries) in byType) {
                        // Check if this type is stackable (includes "untyped" if in list)
                        if (type in stackableTypes) {
                            // Sum all values in this type group
                            total += typeEntries.sumOf { it.value }
                        } else {
                            // Take only the highest value
                            total += typeEntries.maxOfOrNull { it.value } ?: 0
                        }
                    }
                    total
                }
                GameSystem.DND5E, GameSystem.SAVAGE_WORLDS, GameSystem.DCC -> {
                    // Sum all regardless of type
                    entries.sumOf { it.value }
                }
                GameSystem.GENERIC -> {
                    // Default to sum all (like 5e)
                    entries.sumOf { it.value }
                }
            }
        }
    }

    /**
     * Maps ModifierTarget enum to display name string.
     */
    private fun mapModifierTargetToDisplayName(target: ModifierTarget): String {
        return when (target) {
            ModifierTarget.ARMOR_CLASS -> "Armor Class"
            ModifierTarget.ATTACK_ROLLS -> "Attack Rolls"
            ModifierTarget.SAVING_THROWS -> "Saving Throws"
            ModifierTarget.SKILL_CHECKS -> "Skill Checks"
            ModifierTarget.INITIATIVE -> "Initiative"
            ModifierTarget.ABILITY_SCORES -> "Ability Scores"
            ModifierTarget.DAMAGE_ROLLS -> "Damage Rolls"
            ModifierTarget.HIT_POINTS -> "Hit Points"
            ModifierTarget.SPELL_ATTACKS -> "Spell Attacks"
            ModifierTarget.SPELL_DCS -> "Spell DCs"
        }
    }

    /**
     * Normalizes target name strings from JSON/Effects to match display names.
     * Handles common abbreviations and variations.
     */
    private fun normalizeTargetName(target: String): String {
        // Normalize common abbreviations and variations to display names
        return when (target.uppercase().trim()) {
            "AC", "ARMOR CLASS", "ARMOUR CLASS" -> "Armor Class"
            "ATTACK", "ATTACK ROLL", "ATTACK ROLLS", "ATK" -> "Attack Rolls"
            "SAVE", "SAVING THROW", "SAVING THROWS", "SAVES" -> "Saving Throws"
            "SKILL", "SKILL CHECK", "SKILL CHECKS" -> "Skill Checks"
            "INIT", "INITIATIVE" -> "Initiative"
            "ABILITY", "ABILITY SCORE", "ABILITY SCORES", "ABILITIES" -> "Ability Scores"
            "DAMAGE", "DAMAGE ROLL", "DAMAGE ROLLS", "DMG" -> "Damage Rolls"
            "HP", "HIT POINT", "HIT POINTS", "HEALTH" -> "Hit Points"
            "SPELL ATTACK", "SPELL ATTACKS" -> "Spell Attacks"
            "SPELL DC", "SPELL DCS", "SPELL SAVE DC" -> "Spell DCs"
            else -> target // Return as-is if no match (might be a custom target)
        }
    }
}

