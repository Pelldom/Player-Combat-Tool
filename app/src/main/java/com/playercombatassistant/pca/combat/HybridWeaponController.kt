package com.playercombatassistant.pca.combat

import com.playercombatassistant.pca.improvised.ImprovisedWeaponResult
import com.playercombatassistant.pca.improvised.ImprovisedWeaponViewModel
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin

/**
 * Non-UI orchestration helper for hybrid-mode manual weapon controls.
 *
 * Why this exists:
 * - The lock state belongs to [CombatViewModel] (combat model).
 * - Rolling weapons belongs to [ImprovisedWeaponViewModel] (improvised system).
 * - Hybrid manual reroll must coordinate both, without UI assumptions.
 *
 * Rules:
 * - Only performs actions when (inCombat == true && weaponLockMode == HYBRID).
 * - Manual reroll must: unlock -> roll new weapon -> re-lock.
 */
class HybridWeaponController(
    private val combatViewModel: CombatViewModel,
    private val improvisedWeaponViewModel: ImprovisedWeaponViewModel,
) {
    fun rerollWeaponManually(): ImprovisedWeaponResult? {
        val combatState = combatViewModel.state.value
        if (!combatState.inCombat || combatState.weaponLockMode != WeaponLockMode.HYBRID) return null
        // If we can't associate the reroll with an active combat session, do nothing.
        // This avoids "unlocking" without actually rerolling.
        val sessionId = combatState.sessionId ?: return null

        // Unlock weapon (HYBRID-only).
        combatViewModel.unlockWeapon()

        // Roll new weapon and record it as a MANUAL hybrid reroll in the combat history.
        val result = improvisedWeaponViewModel.rollNewWeaponInCombat(
            combatId = sessionId,
            round = combatState.round,
            origin = ImprovisedWeaponRollOrigin.MANUAL,
        )

        // Re-lock weapon (only if a roll actually occurred)
        combatViewModel.onImprovisedWeaponRolled(success = result != null)

        return result
    }
}

