package com.playercombatassistant.pca.effects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Result of processing a round, containing expired effects for history recording.
 */
data class RoundProcessingResult(
    val expiredEffects: List<Effect>,
    val expiredGenericEffects: List<GenericEffect>,
)

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

    private val _activeGenericEffects = MutableStateFlow<List<GenericEffect>>(emptyList())
    val activeGenericEffects: StateFlow<List<GenericEffect>> = _activeGenericEffects.asStateFlow()

    private val _expiredGenericEffects = MutableStateFlow<List<GenericEffect>>(emptyList())
    val expiredGenericEffects: StateFlow<List<GenericEffect>> = _expiredGenericEffects.asStateFlow()

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
        modifierType: String? = null,
        modifierTarget: ModifierTarget? = null,
        modifierValue: Int? = null,
    ) {
        val id = UUID.randomUUID().toString()
        val startRound = round
        val endRound = durationRounds?.let { startRound + it }

        val newEffect = GenericEffect(
            id = id,
            name = name,
            notes = notes,
            colorId = colorId,
            startRound = startRound,
            durationRounds = durationRounds,
            endRound = endRound,
            remainingRounds = durationRounds, // Initially set to duration
            modifierType = modifierType,
            modifierTarget = modifierTarget,
            modifierValue = modifierValue,
        )

        // Update currentRound to match the provided round
        currentRound = round

        // Update state - this will trigger recomposition
        _activeGenericEffects.update { it + newEffect }
    }

    /**
     * Process a Next Round event for generic effects.
     *
     * Behavior:
     * - For each active generic effect:
     *   - Decrement remainingRounds by 1
     *   - If remainingRounds <= 0:
     *     - Mark effect as expired
     *     - Remove from active list
     *
     * Rules:
     * - Expiration occurs AFTER round advances
     * - Indefinite effects (remainingRounds == null) are never decremented
     * - No system-specific logic
     * - State updates trigger recomposition via StateFlow
     */
    /**
     * Process generic effects for a new round.
     * Decrements remainingRounds and expires effects that reach 0.
     *
     * @return List of expired generic effects for history recording
     */
    fun onNextRound(currentRound: Int): List<GenericEffect> {
        this.currentRound = currentRound

        val activeGeneric = mutableListOf<GenericEffect>()
        val expiredGeneric = mutableListOf<GenericEffect>()

        for (effect in _activeGenericEffects.value) {
            val remainingRounds = effect.remainingRounds

            if (remainingRounds == null) {
                // Indefinite effect - never expires
                activeGeneric += effect
            } else {
                // Decrement remainingRounds
                val newRemainingRounds = remainingRounds - 1

                // If remainingRounds <= 0, mark as expired
                if (newRemainingRounds <= 0) {
                    expiredGeneric += effect
                } else {
                    // Update effect with decremented remainingRounds
                    val updated = effect.copy(remainingRounds = newRemainingRounds)
                    activeGeneric += updated
                }
            }
        }

        // Update state - this will trigger recomposition
        _activeGenericEffects.value = activeGeneric
        _expiredGenericEffects.update { it + expiredGeneric }

        return expiredGeneric
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
            _activeGenericEffects.update { effects ->
                val found = effects.find { it.id == effectId }
                if (found != null) {
                    removed = true
                    effects - found
                } else {
                    effects
                }
            }
        }

        return removed
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
     *
     * @return RoundProcessingResult containing expired effects for history recording
     */
    fun processNextRound(newRound: Int): RoundProcessingResult {
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

        // Process generic effects using onNextRound for round-based ticking
        val expiredGeneric = onNextRound(newRound)

        return RoundProcessingResult(
            expiredEffects = expired,
            expiredGenericEffects = expiredGeneric,
        )
    }

    /**
     * Set the current round without processing effects.
     * Used when combat starts to initialize round to 1 without decrementing durations.
     */
    fun setCurrentRound(round: Int) {
        currentRound = round
    }

    /**
     * Update an existing effect by ID.
     * Only updates duration (remainingRounds).
     * Recalculates endRound based on new remainingRounds and currentRound.
     */
    fun updateEffect(
        effectId: String,
        remainingRounds: Int?,
    ): Boolean {
        var updated = false

        // Try to update system-specific effect
        _activeEffects.update { effects ->
            val found = effects.find { it.id == effectId }
            if (found != null) {
                updated = true
                val newEndRound = remainingRounds?.let { currentRound + it }
                effects.map { effect ->
                    if (effect.id == effectId) {
                        effect.copy(
                            remainingRounds = remainingRounds,
                            endRound = newEndRound,
                        )
                    } else {
                        effect
                    }
                }
            } else {
                effects
            }
        }

        return updated
    }

    /**
     * Update an existing generic effect by ID.
     * Updates name, notes, and duration (remainingRounds).
     * Recalculates endRound and durationRounds based on new remainingRounds.
     */
    fun updateGenericEffect(
        effectId: String,
        name: String? = null,
        notes: String? = null,
        remainingRounds: Int?,
    ): Boolean {
        var updated = false

        _activeGenericEffects.update { effects ->
            val found = effects.find { it.id == effectId }
            if (found != null) {
                updated = true
                val newEndRound = remainingRounds?.let { currentRound + it }
                effects.map { effect ->
                    if (effect.id == effectId) {
                        effect.copy(
                            name = name ?: effect.name,
                            notes = notes, // null means clear notes, non-null means set to that value
                            remainingRounds = remainingRounds,
                            durationRounds = remainingRounds,
                            endRound = newEndRound,
                        )
                    } else {
                        effect
                    }
                }
            } else {
                effects
            }
        }

        return updated
    }

    /**
     * Clear all effects (e.g., when combat ends).
     * Clears both system-specific and generic effects.
     */
    fun clearEffects() {
        _activeEffects.value = emptyList()
        _expiredEffects.value = emptyList()
        _activeGenericEffects.value = emptyList()
        _expiredGenericEffects.value = emptyList()
        currentRound = 0
    }
}
