package com.playercombatassistant.pca.improvised

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LocationSelectorTest {

    @Test
    fun selectLocation_returnsNullWhenNoTables() {
        val selector = LocationSelector(emptyList())
        assertNull(selector.selectLocation(1))
    }

    @Test
    fun selectLocation_wrapsIntoRangeAndFindsNextExistingId() {
        val t1 = LocationTable(id = 1, name = "One", items = emptyList())
        val t5 = LocationTable(id = 5, name = "Five", items = emptyList())
        val t30 = LocationTable(id = 30, name = "Thirty", items = emptyList())
        val selector = LocationSelector(listOf(t1, t5, t30))

        // Exact hit
        assertEquals(t5, selector.selectLocation(5))

        // Missing id scans forward to next existing id
        assertEquals(t5, selector.selectLocation(2))

        // Wrap into 1..30 then scan (31 -> 1)
        assertEquals(t1, selector.selectLocation(31))

        // Wrap and scan forward across boundary (29 -> 30)
        assertEquals(t30, selector.selectLocation(29))

        // Wrap negative (0 -> 30)
        assertEquals(t30, selector.selectLocation(0))
    }

    @Test
    fun rollRandomLocation_returnsVisibleD30RollAndResolvedTable() {
        val t10 = LocationTable(id = 10, name = "Ten", items = emptyList())
        val t20 = LocationTable(id = 20, name = "Twenty", items = emptyList())
        val selector = LocationSelector(listOf(t10, t20))

        val (roll, table) = selector.rollRandomLocation(Random(0))
        assertTrue(roll in 1..30)
        // Table must be one of the provided tables (resolved with wrapping if needed).
        assertTrue(table == t10 || table == t20)
    }
}

