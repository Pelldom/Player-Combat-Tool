package com.playercombatassistant.pca.improvised

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ImprovisedWeaponSelectionTest {

    @Test
    fun selectByRoll_mapsD100ToTotalWeightByScaling() {
        // totalWeight = 20 (not 100)
        val a = ImprovisedItem(id = "a", name = "A", description = "A", weight = 10, rarity = Rarity.COMMON)
        val b = ImprovisedItem(id = "b", name = "B", description = "B", weight = 10, rarity = Rarity.RARE)
        val table = LocationTable(id = 1, name = "Test", items = listOf(a, b))

        // With scaling normalization:
        // - roll 1..50 => A
        // - roll 51..100 => B
        assertEquals(a, ImprovisedWeaponSelection.selectByRoll(table, 1))
        assertEquals(a, ImprovisedWeaponSelection.selectByRoll(table, 50))
        assertEquals(b, ImprovisedWeaponSelection.selectByRoll(table, 51))
        assertEquals(b, ImprovisedWeaponSelection.selectByRoll(table, 100))
    }

    @Test
    fun rollAndSelect_preservesOriginalD100Roll() {
        val a = ImprovisedItem(id = "a", name = "A", description = "A", weight = 1, rarity = Rarity.COMMON)
        val table = LocationTable(id = 7, name = "Dock", items = listOf(a))

        val result = ImprovisedWeaponSelection.rollAndSelect(table, random = Random(0))
        assertEquals(7, result.locationId)
        assertEquals("Dock", result.locationName)
        assertTrue(result.d100Roll in 1..100)
        assertEquals(a, result.item)
    }

    @Test
    fun demo_printsExampleRolls() {
        val items = listOf(
            ImprovisedItem(id = "bottle", name = "Broken Bottle", description = "Broken bottle", weight = 25, rarity = Rarity.COMMON),
            ImprovisedItem(id = "chair", name = "Chair Leg", description = "Chair leg", weight = 50, rarity = Rarity.COMMON),
            ImprovisedItem(id = "crowbar", name = "Rusty Crowbar", description = "Rusty crowbar", weight = 10, rarity = Rarity.UNCOMMON),
            ImprovisedItem(id = "saber", name = "Decorative Saber", description = "Decorative saber", weight = 5, rarity = Rarity.RARE),
        )
        val table = LocationTable(id = 12, name = "Tavern", items = items)

        val rng = Random(1234)
        repeat(5) {
            val r = ImprovisedWeaponSelection.rollAndSelect(table, random = rng)
            // "Demo" logging (shows what would be displayed to the user).
            println("d100=${r.d100Roll} -> ${r.item.description} [${r.item.rarity}] (table=${r.locationName})")
        }
    }
}

