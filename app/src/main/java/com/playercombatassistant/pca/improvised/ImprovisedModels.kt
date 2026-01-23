package com.playercombatassistant.pca.improvised

import kotlinx.serialization.Serializable

/**
 * Display-only rarity marker for improvised item tables.
 *
 * Notes:
 * - Informational only (no enforcement, no stacking/validation rules).
 * - Stored in history for display.
 */
@Serializable
enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
}

/**
 * Display-only handedness tag for an improvised weapon entry.
 *
 * Notes:
 * - Informational only (no rules interpretation).
 */
@Serializable
enum class Handedness {
    ONE_HANDED,
    TWO_HANDED,
    VERSATILE,
}

/**
 * An entry in a location table of possible improvised weapons/items.
 *
 * Constraints (not enforced here):
 * - [id] should be stable across app versions so history can reference it.
 * - [weight] is intended for later weighted d100 selection (no selection logic here).
 */
@Serializable
data class ImprovisedItem(
    val id: String,
    /**
     * Display name, e.g. "Broken Bottle".
     */
    val name: String = "",
    val description: String,
    /**
     * Display-only damage string, e.g. "1d4".
     */
    val damage: String = "",
    /**
     * Display-only damage type string, e.g. "piercing".
     */
    val damageType: String = "",
    val handedness: Handedness = Handedness.ONE_HANDED,
    /**
     * Optional display-only notes, e.g. "Fragile" / "Improvised thrown weapon".
     */
    val notes: String? = null,
    val weight: Int,
    val rarity: Rarity,
)

/**
 * A named table of improvised items for a specific location/environment.
 *
 * Constraints (not enforced here):
 * - [id] is intended to be in the range 1..30.
 * - Tables may share/duplicate items across locations by reusing the same [ImprovisedItem] values.
 */
@Serializable
data class LocationTable(
    val id: Int,
    val name: String,
    val items: List<ImprovisedItem>,
)

/**
 * A display-ready result of selecting an improvised item from a location table.
 *
 * Notes:
 * - No generation/selection logic is included here.
 * - [d100Roll] is stored for transparency/history display.
 */
@Serializable
data class ImprovisedWeaponResult(
    val locationId: Int,
    val locationName: String,
    val d100Roll: Int,
    val item: ImprovisedItem,
)
