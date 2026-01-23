package com.playercombatassistant.pca.effects

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
}

