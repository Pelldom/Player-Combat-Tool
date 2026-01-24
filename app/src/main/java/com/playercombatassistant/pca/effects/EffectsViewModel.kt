package com.playercombatassistant.pca.effects

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * ViewModel for managing combat effects with round tracking.
 *
 * Responsibilities:
 * - Add new effects at current round (sets startRound, calculates endRound)
 * - Track remaining rounds (updates remainingRounds based on endRound vs currentRound)
 * - Expire effects when endRound < current round
 * - Expose activeEffects and expiredEffects
 * - Support generic effects (system-agnostic)
 *
 * Rules:
 * - No rule enforcement (display-only tracking)
 * - No system logic (no automatic calculations beyond round tracking)
 * - Driven entirely by Next Round events (processNextRound must be called)
 */
class EffectsViewModel(application: Application) : AndroidViewModel(application) {
    private val _activeEffects = MutableStateFlow<List<Effect>>(emptyList())
    val activeEffects: StateFlow<List<Effect>> = _activeEffects.asStateFlow()

    private val _expiredEffects = MutableStateFlow<List<Effect>>(emptyList())
    val expiredEffects: StateFlow<List<Effect>> = _expiredEffects.asStateFlow()

    /**
     * Active generic effects (system-agnostic), backed by Compose state.
     *
     * We intentionally avoid storing derived state (remaining rounds, endRound)
     * on the model itself. Instead, those values are computed on demand from
     * [GenericEffect.startRound] and [GenericEffect.durationRounds] via helper
     * functions on the model.
     */
    var activeGenericEffects by mutableStateOf<List<GenericEffect>>(emptyList())
        private set

    private var currentRound: Int = 0

    /**
     * Add a new effect at the current round.
     *
     * Rules:
     * - startRound is set to [round] (overrides any existing value)
     * - endRound = startRound + duration (if duration is not null)
     * - endRound = null (if duration is null, indefinite effect)
     * - remainingRounds is set to duration (initial value, preserved from input)
     */
    fun addEffect(
        round: Int,
        effect: Effect,
    ) {
        currentRound = round

        // Use remainingRounds as the duration to calculate endRound
        val duration = effect.remainingRounds
        val startRound = round
        val endRound = duration?.let { startRound + it }

        val newEffect = effect.copy(
            startRound = startRound,
            endRound = endRound,
            // Preserve the original remainingRounds as the initial duration
            // It will be updated by processNextRound() based on endRound vs currentRound
            remainingRounds = duration,
        )

        _activeEffects.update { it + newEffect }
    }

    /**
     * Add a new generic effect at the current round.
     *
     * Rules:
     * - Generates a unique ID automatically
     * - startRound is set to [round] (current combat round)
     * - endRound = startRound + durationRounds (if durationRounds is not null)
     * - endRound = null (if durationRounds is null, indefinite effect)
     * - Effect is added to activeGenericEffects
     * - Updates currentRound to match the provided round
     * - State updates trigger recomposition via StateFlow
     *
     * Constraints:
     * - No system logic
     * - No enforcement
     * - No expiration yet (expiration handled by processNextRound)
     */
    fun addGenericEffect(
        name: String,
        notes: String? = null,
        colorId: EffectColorId,
        durationRounds: Int?,
        round: Int,
    ) {
        val id = UUID.randomUUID().toString()
        val startRound = round

        val newEffect = GenericEffect(
            id = id,
            name = name,
            notes = notes,
            colorId = colorId,
            startRound = startRound,
            durationRounds = durationRounds,
        )

        // Update currentRound to match the provided round
        currentRound = round

        // Update state - this will trigger recomposition
        activeGenericEffects = activeGenericEffects + newEffect
    }

    /**
     * Process a Next Round event for generic effects.
     *
     * Behavior:
     * - Filters [activeGenericEffects] based on [GenericEffect.isActiveAt]
     *   using the new [currentRound].
     *
     * Rules:
     * - No mutation of derived state on the model
     * - Indefinite effects (durationRounds == null) never expire automatically
     * - No system-specific logic
     */
    fun onNextRound(currentRound: Int) {
        this.currentRound = currentRound
        // Filter out any effects that are no longer active at this round
        activeGenericEffects = activeGenericEffects.filter { it.isActiveAt(currentRound) }
    }

    /**
     * Remove an effect by ID.
     *
     * Searches both system-specific effects and generic effects.
     * Removes the first matching effect found.
     *
     * Returns true if an effect was removed, false otherwise.
     */
    fun removeEffect(effectId: String): Boolean {
        var removed = false

        // Try to remove from system-specific effects
        _activeEffects.update { effects ->
            val found = effects.find { it.id == effectId }
            if (found != null) {
                removed = true
                effects - found
            } else {
                effects
            }
        }

        // Try to remove from generic effects if not found yet
        if (!removed) {
            val beforeSize = activeGenericEffects.size
            activeGenericEffects = activeGenericEffects.filterNot { it.id == effectId }
            removed = beforeSize != activeGenericEffects.size
        }

        return removed
    }

    /**
     * Update an existing generic effect.
     *
     * Finds the effect by ID and replaces it with the updated version.
     * Preserves the original startRound (does not reset it).
     *
     * Returns true if the effect was found and updated, false otherwise.
     */
    fun updateGenericEffect(
        effectId: String,
        name: String,
        notes: String? = null,
        colorId: EffectColorId,
        durationRounds: Int?,
    ): Boolean {
        val existing = activeGenericEffects.find { it.id == effectId } ?: return false
        
        val updated = existing.copy(
            name = name,
            notes = notes,
            colorId = colorId,
            durationRounds = durationRounds,
            // Preserve startRound - don't reset it when editing
        )
        
        activeGenericEffects = activeGenericEffects.map { if (it.id == effectId) updated else it }
        return true
    }

    /**
     * Process a Next Round event.
     *
     * Responsibilities:
     * - Update currentRound
     * - Expire effects where endRound < currentRound (both system and generic)
     * - Update remainingRounds for active system effects (endRound - currentRound, or null if indefinite)
     * - Move expired effects to expiredEffects lists
     *
     * Rules:
     * - Driven entirely by Next Round events (no automatic ticking)
     * - No rule enforcement (just tracking)
     * - Generic effects don't have remainingRounds, they just expire when endRound < currentRound
     */
    fun processNextRound(newRound: Int) {
        currentRound = newRound

        // Process system-specific effects
        val active = mutableListOf<Effect>()
        val expired = mutableListOf<Effect>()

        for (effect in _activeEffects.value) {
            val endRound = effect.endRound

            // Expire if endRound is set and currentRound has passed it
            if (endRound != null && currentRound > endRound) {
                expired += effect
            } else {
                // Update remainingRounds: endRound - currentRound (or null if indefinite)
                // Ensure remainingRounds is never negative (clamp to 0 minimum)
                val updatedRemainingRounds = endRound?.let { (it - currentRound).coerceAtLeast(0) }
                val updated = effect.copy(remainingRounds = updatedRemainingRounds)
                active += updated
            }
        }

        _activeEffects.value = active
        _expiredEffects.update { it + expired }

        // Process generic effects using onNextRound for round-based filtering
        onNextRound(newRound)
    }

    /**
     * Clear all effects (e.g., when combat ends).
     * Clears both system-specific and generic effects.
     */
    fun clearEffects() {
        _activeEffects.value = emptyList()
        _expiredEffects.value = emptyList()
        activeGenericEffects = emptyList()
        currentRound = 0
    }
}
