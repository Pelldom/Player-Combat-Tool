package com.playercombatassistant.pca.effects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifierAggregationTest {

    @Test
    fun aggregateNumeric_ignoresNonNumericValues() {
        val effects = listOf(
            Effect(
                id = "e1",
                name = "Test",
                system = GameSystem.PF2,
                description = "",
                remainingRounds = 3,
                type = EffectType.CONDITION,
                modifiers = listOf(
                    Modifier(target = "AC", value = "-2", source = "Frightened"),
                    Modifier(target = "Attack", value = "ADV", source = "Some Advantage"),
                ),
            ),
        )

        val result = ModifierAggregation.aggregateNumeric(effects)
        assertEquals(1, result.size)
        assertEquals("AC", result[0].target)
        assertEquals(-2, result[0].total)
        assertEquals(1, result[0].sources.size)
        assertEquals("Frightened", result[0].sources[0].source)
        assertEquals(listOf(-2), result[0].sources[0].values)
    }

    @Test
    fun aggregateNumeric_groupsByTargetAndPreservesSourceBreakdown() {
        val effects = listOf(
            Effect(
                id = "e1",
                name = "A",
                system = GameSystem.PF1,
                description = "",
                remainingRounds = null,
                type = EffectType.CONDITION,
                modifiers = listOf(
                    Modifier(target = "AC", value = "-1", source = "A"),
                    Modifier(target = "AC", value = "-2", source = "A"),
                    Modifier(target = "Attack", value = "+1", source = "A"),
                ),
            ),
            Effect(
                id = "e2",
                name = "B",
                system = GameSystem.PF2,
                description = "",
                remainingRounds = 2,
                type = EffectType.TIMER,
                modifiers = listOf(
                    Modifier(target = "AC", value = "+1", source = "B"),
                ),
            ),
        )

        val result = ModifierAggregation.aggregateNumeric(effects)
        assertEquals(setOf("AC", "Attack"), result.map { it.target }.toSet())

        val ac = result.first { it.target == "AC" }
        assertEquals(-2, ac.total) // (-1) + (-2) + (+1)
        assertEquals(2, ac.sources.size)
        assertTrue(ac.sources.any { it.source == "A" && it.values == listOf(-1, -2) && it.sum == -3 })
        assertTrue(ac.sources.any { it.source == "B" && it.values == listOf(1) && it.sum == 1 })
    }
}

