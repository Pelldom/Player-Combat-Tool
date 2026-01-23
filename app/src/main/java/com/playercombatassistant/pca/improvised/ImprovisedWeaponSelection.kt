package com.playercombatassistant.pca.improvised

import kotlin.random.Random

/**
 * Weighted d100 selection for improvised weapons.
 *
 * Notes / constraints:
 * - Uses Kotlin [Random] directly (no randomness abstraction yet).
 * - Selection is cumulative based on [ImprovisedItem.weight].
 * - Total weight does NOT have to equal 100.
 *   - If totalWeight != 100, the d100 roll is still shown to the user as-is.
 *   - For selection, we map the d100 roll onto 1..totalWeight by scaling:
 *       normalized = floor(((roll - 1) * totalWeight) / 100) + 1
 *     This keeps selection driven purely by the weights (no table-specific probability constants),
 *     while still always selecting an item when totalWeight > 0.
 * - No persistence, no UI.
 * - No validation/enforcement (e.g., negative/zero weights are treated as-is; if totalWeight <= 0, we fall back).
 *
 * Combat locking / hybrid mode / effects (later):
 * - Combat locking: callers can prevent rolling while locked; selection remains purely data-driven.
 * - Hybrid mode: callers can combine a location d30 roll (optional) with a weapon d100 roll and record both.
 * - Effects: a future layer may attach effects/modifiers to the selected item for display, without enforcement.
 */
object ImprovisedWeaponSelection {

    fun rollAndSelect(
        table: LocationTable,
        random: Random = Random.Default,
    ): ImprovisedWeaponResult {
        val roll = random.nextInt(from = 1, until = 101) // 1..100
        val item = selectByRoll(table, roll)
        return ImprovisedWeaponResult(
            locationId = table.id,
            locationName = table.name,
            d100Roll = roll,
            item = item,
        )
    }

    /**
     * Deterministic selection given a d100 [roll] (typically 1..100).
     * Used by [rollAndSelect] and unit tests.
     */
    fun selectByRoll(table: LocationTable, roll: Int): ImprovisedItem {
        val items = table.items
        if (items.isEmpty()) {
            // No enforcement; but we must return an item. Callers should avoid empty tables.
            return ImprovisedItem(
                id = "empty",
                name = "No items",
                description = "No items",
                weight = 0,
                rarity = Rarity.COMMON,
            )
        }

        val totalWeight = items.sumOf { it.weight }
        if (totalWeight <= 0) {
            // Fallback: pick first item if weights are unusable.
            return items.first()
        }

        // Map d100 roll (any int) onto 1..totalWeight for cumulative selection.
        // - We intentionally keep the original d100 roll for display/history.
        // - We normalize for selection only, without requiring totalWeight == 100.
        val normalizedRoll = ((roll - 1) % 100 + 100) % 100 // 0..99
        val normalized = (normalizedRoll * totalWeight) / 100 + 1 // 1..totalWeight

        var cursor = 0
        for (item in items) {
            cursor += item.weight
            if (normalized <= cursor) return item
        }

        // Defensive fallback (shouldn't happen with totalWeight computed above)
        return items.last()
    }
}

