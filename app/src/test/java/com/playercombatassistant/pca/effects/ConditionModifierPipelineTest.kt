package com.playercombatassistant.pca.effects

import org.junit.Test
import org.junit.Assert.*

/**
 * Verification tests for the condition and modifier pipeline.
 * 
 * Tests:
 * 1. JSON structure validation
 * 2. Data model correctness
 * 3. Aggregation logic
 * 4. Edge cases
 */
class ConditionModifierPipelineTest {

    @Test
    fun testModifierAggregationWithGenericEffects() {
        // Test that GenericEffect notes are parsed correctly
        val genericEffect = GenericEffect(
            id = "test1",
            name = "Rage",
            notes = "Strength: +4; Armor Class: -2",
            colorId = EffectColorId.PRIMARY,
            startRound = 1,
            durationRounds = 10,
        )

        val summary = ModifierAggregation.aggregateEnhanced(
            effects = emptyList(),
            genericEffects = listOf(genericEffect),
        )

        assertEquals(1, summary.activeConditionsCount)
        assertEquals(2, summary.modifierAggregations.size)

        val strengthAgg = summary.modifierAggregations.find { it.target == "Strength" }
        assertNotNull(strengthAgg)
        assertEquals(4, strengthAgg!!.total)
        assertEquals(1, strengthAgg.sources.size)
        assertEquals("Rage", strengthAgg.sources[0].source)

        val acAgg = summary.modifierAggregations.find { it.target == "Armor Class" }
        assertNotNull(acAgg)
        assertEquals(-2, acAgg!!.total)
    }

    @Test
    fun testModifierAggregationWithMultipleSources() {
        // Test grouping by target with multiple sources
        val effect1 = GenericEffect(
            id = "test1",
            name = "Shaken",
            notes = "Armor Class: -2",
            colorId = EffectColorId.PRIMARY,
            startRound = 1,
            durationRounds = 5,
        )

        val effect2 = GenericEffect(
            id = "test2",
            name = "Rage",
            notes = "Armor Class: -2",
            colorId = EffectColorId.SECONDARY,
            startRound = 1,
            durationRounds = 10,
        )

        val summary = ModifierAggregation.aggregateEnhanced(
            effects = emptyList(),
            genericEffects = listOf(effect1, effect2),
        )

        assertEquals(2, summary.activeConditionsCount)
        assertEquals(1, summary.modifierAggregations.size)

        val acAgg = summary.modifierAggregations[0]
        assertEquals("Armor Class", acAgg.target)
        assertEquals(-4, acAgg.total) // -2 + -2
        assertEquals(2, acAgg.sources.size) // Two sources: Shaken and Rage
    }

    @Test
    fun testModifierAggregationIgnoresNonNumeric() {
        // Test that non-numeric values are ignored
        val genericEffect = GenericEffect(
            id = "test1",
            name = "Test",
            notes = "Attack Rolls: ADV; Armor Class: -2",
            colorId = EffectColorId.PRIMARY,
            startRound = 1,
            durationRounds = 5,
        )

        val summary = ModifierAggregation.aggregateEnhanced(
            effects = emptyList(),
            genericEffects = listOf(genericEffect),
        )

        // Should only have AC modifier, not ADV
        assertEquals(1, summary.modifierAggregations.size)
        assertEquals("Armor Class", summary.modifierAggregations[0].target)
    }

    @Test
    fun testEmptyEffects() {
        val summary = ModifierAggregation.aggregateEnhanced(
            effects = emptyList(),
            genericEffects = emptyList(),
        )

        assertEquals(0, summary.activeConditionsCount)
        assertEquals(0, summary.modifierAggregations.size)
    }

    @Test
    fun testConditionCounting() {
        val effect = Effect(
            id = "test1",
            name = "Blinded",
            system = GameSystem.PF1,
            description = "Test",
            remainingRounds = 5,
            type = EffectType.CONDITION,
            modifiers = emptyList(),
            startRound = 1,
            endRound = 6,
        )

        val genericEffect = GenericEffect(
            id = "test2",
            name = "Rage",
            notes = "Strength: +4",
            colorId = EffectColorId.PRIMARY,
            startRound = 1,
            durationRounds = 10,
        )

        val summary = ModifierAggregation.aggregateEnhanced(
            effects = listOf(effect),
            genericEffects = listOf(genericEffect),
        )

        // Should count both: 1 Effect with type CONDITION + 1 GenericEffect
        assertEquals(2, summary.activeConditionsCount)
    }
}
