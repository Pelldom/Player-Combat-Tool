package com.playercombatassistant.pca.combat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playercombatassistant.pca.effects.Effect
import com.playercombatassistant.pca.history.CombatHistoryEvent
import com.playercombatassistant.pca.history.CombatHistoryStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class CombatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(CombatState())
    val state: StateFlow<CombatState> = _state.asStateFlow()

    private val historyStore = CombatHistoryStore(application.applicationContext)

    fun startCombat() {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()
        _state.value = CombatState(
            inCombat = true,
            round = 1,
            sessionId = sessionId,
            // Rule: weaponLocked must be false at combat start (locks on first roll in PER_COMBAT).
            weaponLocked = false,
            weaponLockMode = _state.value.weaponLockMode,
        )

        viewModelScope.launch {
            historyStore.recordEvent(
                sessionId = sessionId,
                event = CombatHistoryEvent.StartCombat(timestampMillis = now, round = 1),
                startedAtMillisIfNew = now,
            )
        }
    }

    fun nextRound() {
        _state.update { current ->
            val currentSessionId = current.sessionId
            val nextRound = current.round + 1
            val tickResult = tickEffects(current.effects)
            val updated = current.copy(
                inCombat = true,
                round = nextRound,
                effects = tickResult.remaining,
                // PER_ROUND: weapon automatically unlocks each round.
                weaponLocked = if (current.weaponLockMode == WeaponLockMode.PER_ROUND) false else current.weaponLocked,
            )

            if (currentSessionId != null) {
                val now = System.currentTimeMillis()
                viewModelScope.launch {
                    historyStore.recordEvent(
                        sessionId = currentSessionId,
                        event = CombatHistoryEvent.NextRound(timestampMillis = now, round = nextRound),
                        startedAtMillisIfNew = now,
                    )
                    for (expired in tickResult.expired) {
                        historyStore.recordEvent(
                            sessionId = currentSessionId,
                            event = CombatHistoryEvent.EffectExpired(
                                timestampMillis = now,
                                round = nextRound,
                                effect = expired,
                            ),
                            startedAtMillisIfNew = now,
                        )
                    }
                }
            }

            updated
        }
    }

    fun endCombat() {
        val now = System.currentTimeMillis()
        val current = _state.value
        val sessionId = current.sessionId
        val round = current.round
        // Rule: weaponLocked must be false outside combat.
        _state.value = CombatState(weaponLockMode = current.weaponLockMode)

        if (sessionId != null) {
            viewModelScope.launch {
                historyStore.recordEvent(
                    sessionId = sessionId,
                    event = CombatHistoryEvent.EndCombat(timestampMillis = now, round = round),
                    startedAtMillisIfNew = now,
                    endedAtMillisIfEnding = now,
                )
            }
        }
    }

    /**
     * Call when an improvised weapon roll is completed.
     *
     * Rules:
     * - If not in combat: rolling is always allowed and weaponLocked must remain false.
     * - If in combat and roll succeeded:
     *   - PER_COMBAT: lock after roll, until endCombat()
     *   - PER_ROUND: lock after roll, until nextRound() unlocks
     *   - HYBRID: lock after roll, until a future manual unlock action
     *
     * Note: this is deterministic and does not assume any UI state.
     */
    fun onImprovisedWeaponRolled(success: Boolean) {
        if (!success) return

        _state.update { current ->
            if (!current.inCombat) {
                // Rule: weaponLocked=false outside combat.
                current.copy(weaponLocked = false)
            } else {
                // In combat: lock after roll for all modes; PER_ROUND auto-unlocks on nextRound().
                // HYBRID can be manually unlocked (see unlockWeapon()).
                current.copy(weaponLocked = true)
            }
        }
    }

    /**
     * Hybrid-mode manual unlock.
     *
     * Rules:
     * - Only available when inCombat=true AND weaponLockMode=HYBRID.
     * - No-ops otherwise.
     */
    fun unlockWeapon() {
        _state.update { current ->
            if (current.inCombat && current.weaponLockMode == WeaponLockMode.HYBRID) {
                current.copy(weaponLocked = false)
            } else {
                current
            }
        }
    }

    private data class TickResult(
        val remaining: List<Effect>,
        val expired: List<Effect>,
    )

    private fun tickEffects(effects: List<Effect>): TickResult {
        // Effects tick down only on nextRound(). No validation/enforcement.
        val remaining = mutableListOf<Effect>()
        val expired = mutableListOf<Effect>()

        for (effect in effects) {
            val r = effect.remainingRounds
            if (r == null) {
                remaining += effect
            } else {
                val next = r - 1
                if (next <= 0) expired += effect else remaining += effect.copy(remainingRounds = next)
            }
        }

        return TickResult(remaining = remaining, expired = expired)
    }
}

