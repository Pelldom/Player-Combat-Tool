package com.playercombatassistant.pca.effects

/**
 * Display-only aggregation of numeric modifiers.
 *
 * Rules:
 * - Numeric modifiers only (e.g. "-2", "+1", "3"). Non-numeric values like "ADV" are ignored.
 * - No stacking rules, no bonus types, no enforcement.
 * - Preserves a per-source breakdown for display.
 * - Extracts modifiers from GenericEffect notes when in format "target: value".
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

    /**
     * Enhanced aggregation that includes both Effect and GenericEffect objects.
     * Also counts active conditions.
     */
    data class EnhancedSummary(
        val activeConditionsCount: Int,
        val modifierAggregations: List<TargetAggregation>,
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

    /**
     * Enhanced aggregation that includes both Effect and GenericEffect objects.
     * Extracts modifiers from GenericEffect notes and counts active conditions.
     */
    fun aggregateEnhanced(
        effects: List<Effect>,
        genericEffects: List<GenericEffect>,
    ): EnhancedSummary {
        val numericMods = buildList<Triple<String, String, Int>> {
            // Extract from Effect objects
            for (effect in effects) {
                for (mod in effect.modifiers) {
                    val value = mod.value.toIntOrNullStrict() ?: continue
                    add(Triple(mod.target, mod.source, value))
                }
            }

            // Extract from GenericEffect notes (format: "target: value; target: value")
            for (genericEffect in genericEffects) {
                val notes = genericEffect.notes ?: continue
                val source = genericEffect.name

                // Parse notes in format "target: value; target: value"
                val entries = notes.split(";").map { it.trim() }
                for (entry in entries) {
                    val colonIndex = entry.indexOf(':')
                    if (colonIndex < 0) continue

                    val target = entry.substring(0, colonIndex).trim()
                    val valueStr = entry.substring(colonIndex + 1).trim()
                    val value = valueStr.toIntOrNullStrict() ?: continue

                    add(Triple(target, source, value))
                }
            }
        }

        // Count active conditions
        val conditionsCount = effects.count { it.type == EffectType.CONDITION } +
            genericEffects.size // Generic effects are treated as conditions for counting

        // Group by target
        val byTarget = numericMods.groupBy { it.first }
        val aggregations = byTarget
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

        return EnhancedSummary(
            activeConditionsCount = conditionsCount,
            modifierAggregations = aggregations,
        )
    }

    private fun String.toIntOrNullStrict(): Int? {
        // Only accept pure integers with optional +/-
        // Examples allowed: "-2", "+1", "0", "12"
        // Examples ignored: "ADV", "DIS", "+2 circumstance", "1d4", "- 2"
        if (!matches(Regex("^[+-]?\\d+$"))) return null
        return toIntOrNull()
    }
}

