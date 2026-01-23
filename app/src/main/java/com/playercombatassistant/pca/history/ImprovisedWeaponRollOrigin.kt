package com.playercombatassistant.pca.history

import kotlinx.serialization.Serializable

/**
 * Indicates whether an improvised weapon roll was a "standard" roll or a hybrid manual reroll.
 *
 * Notes:
 * - This is display-only metadata; no enforcement.
 */
@Serializable
enum class ImprovisedWeaponRollOrigin {
    AUTOMATIC,
    MANUAL,
}

