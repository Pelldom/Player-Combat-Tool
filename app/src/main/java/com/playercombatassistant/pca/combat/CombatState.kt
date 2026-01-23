package com.playercombatassistant.pca.combat

import com.playercombatassistant.pca.effects.Effect

data class CombatState(
    val inCombat: Boolean = false,
    val round: Int = 0,
    val sessionId: String? = null,
    val effects: List<Effect> = emptyList(),
    /**
     * Whether improvised weapon rolling is currently locked.
     *
     * Rule: must be false outside combat.
     */
    val weaponLocked: Boolean = false,
    val weaponLockMode: WeaponLockMode = WeaponLockMode.PER_COMBAT,
)

