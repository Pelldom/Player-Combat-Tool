package com.playercombatassistant.pca.improvised

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.playercombatassistant.pca.improvised.ImprovisedWeaponRepository.LoadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImprovisedWeaponWiringInstrumentedTest {

    @Test
    fun sashaTables_loadTransformValidate_andSelectionAndRollingWork() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = ImprovisedWeaponRepository(app.applicationContext)

        val state = repo.load()
        assertTrue("Repo should load tables; got $state", state is LoadState.Ready)

        val tables = (state as LoadState.Ready).tables
        assertEquals(30, tables.size)
        assertTrue(tables.all { it.items.size == 20 })

        // Manual location selection should work for valid IDs.
        val selector = LocationSelector(tables)
        val manual = selector.selectLocation(1)
        assertNotNull("Manual selectLocation(1) should succeed with 30 valid tables", manual)

        // Random d30 roll should always return a table and a visible roll in 1..30.
        val (d30, randomTable) = selector.rollRandomLocation()
        assertTrue(d30 in 1..30)
        assertNotNull(randomTable)

        // d100 weighted roll should always return a result with roll visible and item populated.
        val result = ImprovisedWeaponSelection.rollAndSelect(randomTable)
        assertTrue(result.d100Roll in 1..100)
        assertEquals(randomTable.id, result.locationId)
        assertEquals(randomTable.name, result.locationName)

        val item = result.item
        assertTrue("Item id should be stable/non-empty", item.id.isNotBlank())
        assertTrue("Item description should preserve source string", item.description.isNotBlank())

        // These fields are used by UI; they should be present (may be empty if parser couldn't extract).
        // Ensure they exist and do not crash. At least handedness has a default.
        assertNotNull(item.handedness)
    }
}

