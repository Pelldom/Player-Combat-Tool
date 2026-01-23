package com.playercombatassistant.pca.improvised.imports

import com.playercombatassistant.pca.improvised.Handedness
import com.playercombatassistant.pca.improvised.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SashaTableTransformerTest {

    @Test
    fun transformTable_preservesIdAndName_andAppliesDefaults() {
        val source = SashaLocationTable(
            id = 7,
            name = "Test Location",
            entries = listOf(
                "Bottle – 1d4 bludgeoning (light). Shatters on a critical hit.",
                "Mystery Object – ???. Unknown.",
            ),
        )

        val table = SashaTableTransformer.transformTable(source)

        assertEquals(7, table.id)
        assertEquals("Test Location", table.name)
        assertEquals(2, table.items.size)

        val bottle = table.items[0]
        assertEquals("Bottle", bottle.name)
        assertEquals("1d4", bottle.damage)
        assertEquals("bludgeoning", bottle.damageType)
        assertEquals(Handedness.ONE_HANDED, bottle.handedness)
        assertEquals(Rarity.COMMON, bottle.rarity)
        assertEquals(5, bottle.weight)
        assertTrue(bottle.description.contains("Bottle"))
    }

    @Test
    fun transformTable_defaultsHandednessToOneHanded_whenUnknown() {
        val source = SashaLocationTable(
            id = 1,
            name = "No Parens",
            entries = listOf("Stick – 1d4 bludgeoning. A plain stick."),
        )

        val table = SashaTableTransformer.transformTable(source)
        assertEquals(1, table.items.size)
        assertEquals(Handedness.ONE_HANDED, table.items[0].handedness)
    }

    @Test
    fun transformTable_generatesStableIds_basedOnEntryContent() {
        val entry = "Bottle – 1d4 bludgeoning (light). Shatters on a critical hit."
        val tableA = SashaLocationTable(id = 1, name = "A", entries = listOf(entry))
        val tableB = SashaLocationTable(id = 2, name = "B", entries = listOf(entry))

        val aId = SashaTableTransformer.transformTable(tableA).items[0].id
        val bId = SashaTableTransformer.transformTable(tableB).items[0].id

        // Same entry string should produce the same id, even across tables.
        assertEquals(aId, bId)

        val differentEntry = "Rock – 1d4 bludgeoning (light). Common anywhere."
        val cId = SashaTableTransformer.transformTable(
            SashaLocationTable(id = 3, name = "C", entries = listOf(differentEntry)),
        ).items[0].id

        assertNotEquals(aId, cId)
    }
}

