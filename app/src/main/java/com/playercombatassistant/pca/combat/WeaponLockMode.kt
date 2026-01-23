package com.playercombatassistant.pca.combat

import kotlinx.serialization.Serializable

/**
 * Controls how improvised weapon rolls are "locked" during combat.
 *
 * Notes:
 * - This is a state model only; UI/persistence come later.
 * - Actual "lock on roll" triggering will be wired when improvised-weapon rolling is combat-aware.
 */
@Serializable
enum class WeaponLockMode {
    PER_COMBAT,
    PER_ROUND,
    HYBRID,
}

