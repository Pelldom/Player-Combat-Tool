package com.playercombatassistant.pca.improvised

import kotlin.random.Random

/**
 * Location selection helper for improvised weapon tables.
 *
 * Intent:
 * - Centralizes how we resolve a "location id" (1..30) into a [LocationTable] without hardcoding any tables.
 * - Supports manual selection and random selection (d30) while always exposing the rolled value.
 *
 * Rules:
 * - Manual: [selectLocation] returns the resolved table or null if there are no tables available.
 * - Random: [rollRandomLocation] rolls d30 (1..30) and returns the roll plus the resolved table.
 * - If the exact location id doesn't exist, we "wrap safely" by scanning forward (circularly) to the next
 *   existing location id within 1..30.
 *
 * Future integration (combat modes):
 * - A future "combat mode" can pick which table list is active (e.g., by biome/scene), then use this selector
 *   to resolve a manual location choice or a random location roll. The selected [LocationTable] + visible roll
 *   can then be recorded to history, and passed into improvised item selection (d100) when needed.
 *
 * Combat locking / hybrid mode (later):
 * - When combat locking is introduced, callers can prevent invoking [selectLocation] / [rollRandomLocation] while
 *   locked (or only allow changes at specific times). This selector intentionally has no knowledge of combat state.
 * - Hybrid mode can combine a random d30 location with a manual override location id, while still keeping the roll
 *   visible for display/history.
 */
class LocationSelector(
    tables: List<LocationTable>,
) {
    private val byId: Map<Int, LocationTable> = tables.associateBy { it.id }

    /**
     * Manual selection by id.
     *
     * - Returns null if there are no tables.
     * - If [id] doesn't exist, wraps safely to the next existing id (circular within 1..30).
     */
    fun selectLocation(id: Int): LocationTable? {
        if (byId.isEmpty()) return null
        return resolveWrapped(id)
    }

    /**
     * Random selection via d30 (1..30). The rolled d30 value is always returned for display/history.
     *
     * If the rolled id doesn't exist, wraps safely to the next existing id (circular within 1..30).
     *
     * @throws IllegalStateException if no tables are available.
     */
    fun rollRandomLocation(): Pair<Int, LocationTable> = rollRandomLocation(Random.Default)

    fun rollRandomLocation(random: Random): Pair<Int, LocationTable> {
        check(byId.isNotEmpty()) { "No location tables available." }
        val roll = random.nextInt(from = 1, until = 31) // 1..30
        val table = resolveWrapped(roll)
        return roll to table
    }

    private fun resolveWrapped(id: Int): LocationTable {
        val normalized = normalizeToD30(id)
        // Scan forward through 1..30, wrapping around, to find the next existing id.
        for (offset in 0 until 30) {
            val candidate = ((normalized - 1 + offset) % 30) + 1
            val table = byId[candidate]
            if (table != null) return table
        }
        // Should be impossible if byId is not empty and ids are within 1..30, but we don't enforce that.
        return byId.values.first()
    }

    private fun normalizeToD30(id: Int): Int {
        // Maps any int into 1..30.
        return ((id - 1) % 30 + 30) % 30 + 1
    }
}

