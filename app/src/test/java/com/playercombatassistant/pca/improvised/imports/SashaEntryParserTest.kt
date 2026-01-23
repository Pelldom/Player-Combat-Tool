package com.playercombatassistant.pca.improvised.imports

import com.playercombatassistant.pca.improvised.Handedness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SashaEntryParserTest {

    /**
     * These strings are intended to be representative of real entries found in `sasha_tables.json`.
     * They do not need to cover every format—just enough to validate defensive behavior.
     */
    private val sampleEntries = listOf(
        "Bottle – 1d4 bludgeoning (light). Shatters on a critical hit.",
        "Chair Leg – 1d6 bludgeoning (1H). Counts as a club for display only.",
        "Table – 1d8 bludgeoning (2H). Awkward and bulky.",
        "Frying Pan – 1d6 bludgeoning (versatile). Loud on impact.",
        "Sack of Bricks – 1d4 bludgeoning +1d4 bludgeoning (2H). Heavy and unpredictable.",
        "Weird Thing (Special) – No effect. Just… weird.",
        "Rock - 1d4 bludgeoning (light). Common anywhere.",
        "",
        "   ",
    )

    @Test
    fun parse_neverThrows_forRepresentativeSamples() {
        sampleEntries.forEach { raw ->
            val result = SashaEntryParser.parse(raw)
            assertNotNull(result)
        }
    }

    @Test
    fun parse_extractsNameDamageTypeHandednessAndNotes_basic() {
        val raw = "Bottle – 1d4 bludgeoning (light). Shatters on a critical hit."
        val parsed = SashaEntryParser.parse(raw)

        assertEquals("Bottle", parsed.name)
        assertEquals("1d4", parsed.damage)
        assertEquals("bludgeoning", parsed.damageType)
        assertEquals(Handedness.ONE_HANDED, parsed.handedness)
        assertEquals("Shatters on a critical hit.", parsed.notes)
        assertFalse(parsed.ambiguous)
    }

    @Test
    fun parse_supportsHyphenDelimiterAsFallback() {
        val raw = "Rock - 1d4 bludgeoning (light). Common anywhere."
        val parsed = SashaEntryParser.parse(raw)

        assertEquals("Rock", parsed.name)
        assertEquals("1d4", parsed.damage)
        assertEquals("bludgeoning", parsed.damageType)
        assertEquals(Handedness.ONE_HANDED, parsed.handedness)
        assertEquals("Common anywhere.", parsed.notes)
        assertFalse(parsed.ambiguous)
    }

    @Test
    fun parse_setsAmbiguousAndPreservesFullOriginalNotes_whenMultipleDice() {
        val raw = "Sack of Bricks – 1d4 bludgeoning +1d4 bludgeoning (2H). Heavy and unpredictable."
        val parsed = SashaEntryParser.parse(raw)

        // We can still extract the name and first dice, but the presence of multiple dice is ambiguous.
        assertEquals("Sack of Bricks", parsed.name)
        assertEquals("1d4", parsed.damage)
        assertTrue(parsed.ambiguous)
        assertEquals(raw.trim(), parsed.notes)
    }

    @Test
    fun parse_leavesFieldsEmpty_whenDiceOrDelimiterNotFound_andTreatsAsAmbiguous() {
        val raw = "Weird Thing (Special) – No effect. Just… weird."
        val parsed = SashaEntryParser.parse(raw)

        // Name is present (delimiter is present), but dice is not.
        assertEquals("Weird Thing (Special)", parsed.name)
        assertEquals("", parsed.damage)
        assertEquals("", parsed.damageType)
        // Parenthetical is "Special" -> no handedness
        assertEquals(null, parsed.handedness)
        // Notes come from after first period because ambiguity is false (delimiter ok, single/no dice ok)
        assertFalse(parsed.ambiguous)
        assertEquals("Just… weird.", parsed.notes)
    }

    @Test
    fun parse_emptyString_returnsAllEmptyAndNotAmbiguous() {
        val parsed = SashaEntryParser.parse("")
        assertEquals("", parsed.name)
        assertEquals("", parsed.damage)
        assertEquals("", parsed.damageType)
        assertEquals(null, parsed.handedness)
        assertEquals("", parsed.notes)
        assertFalse(parsed.ambiguous)
    }
}

