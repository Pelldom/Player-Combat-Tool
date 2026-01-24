package com.playercombatassistant.pca.effects

import kotlinx.serialization.Serializable

/**
 * Generic effect data model for simple, system-agnostic effect tracking.
 *
 * **Intended Use:**
 * - User-created effects that don't require system-specific rules
 * - Simple duration tracking without modifiers
 * - Custom effects that don't fit into game system categories
 * - Quick effect notes and reminders
 *
 * **Key Characteristics:**
 * - No system-specific logic (works across all game systems)
 * - No modifiers (pure duration tracking)
 * - Pure data object (no persisted derived state)
 * - Color-coded for visual distinction on round tracker
 *
 * **Duration Rules:**
 * - [durationRounds] is the number of rounds the effect lasts
 * - If [durationRounds] is null, the effect is indefinite
 * - [endRound] is derived as [startRound] + [durationRounds] - 1 (if durationRounds is not null)
 *   This matches condition behavior: effects expire when currentRound > endRound
 * - [endRound] is null if [durationRounds] is null (indefinite effect)
 * - Effects expire when currentRound > startRound + durationRounds - 1
 *
 * Derived values such as remaining rounds and end round are computed at read time
 * to avoid duplicated, unstable state in the model itself.
 */
@Serializable
data class GenericEffect(
    /**
     * Unique identifier for this effect instance.
     */
    val id: String,
    
    /**
     * Display name of the effect.
     */
    val name: String,
    
    /**
     * Optional notes or description about the effect.
     * Can be used for reminders, details, or custom annotations.
     */
    val notes: String? = null,
    
    /**
     * Color ID to use when displaying this effect on the round tracker bar.
     * Uses the centralized EffectColorId palette for consistency.
     * Resolve to actual Color using EffectColorId.toColor() in UI.
     */
    val colorId: EffectColorId,
    
    /**
     * The combat round when this effect was added/started.
     * Set when the effect is created.
     */
    val startRound: Int,
    
    /**
     * The number of rounds this effect lasts.
     * 
     * Rules:
     * - If not null: effect has finite duration
     * - If null: effect is indefinite (no expiration)
     */
    val durationRounds: Int?,
) {
    /**
     * The combat round when this effect ends (inclusive).
     *
     * Derived from [startRound] and [durationRounds]:
     * - If [durationRounds] is not null (finite duration):
     *   endRound = startRound + durationRounds - 1
     *   (Effect expires when currentRound > endRound, matching condition behavior)
     * - If [durationRounds] is null (indefinite effect):
     *   endRound = null
     *
     * Note: This calculation ensures effects expire at the same time as conditions
     * that decrement remainingRounds. For example, if startRound=1 and durationRounds=3,
     * the effect is active in rounds 1, 2, 3 and expires at round 4.
     */
    val endRound: Int?
        get() = durationRounds?.let { startRound + it - 1 }

    /**
     * Check if this effect is still active at the given round.
     *
     * Matches condition behavior: effects expire when currentRound > startRound + durationRounds - 1
     *
     * Rules:
     * - Returns true if endRound is null (indefinite)
     * - Returns true if currentRound <= endRound
     * - Returns false if currentRound > endRound (effect has expired)
     */
    fun isActiveAt(currentRound: Int): Boolean {
        val end = endRound
        return end == null || currentRound <= end
    }
    
    /**
     * Calculate remaining rounds at the given current round.
     *
     * This matches the behavior of condition ticking where remainingRounds decrements
     * each round. For example, if startRound=1, durationRounds=3:
     * - Round 1: remainingRounds = 3
     * - Round 2: remainingRounds = 2
     * - Round 3: remainingRounds = 1
     * - Round 4: remainingRounds = 0 (expired)
     *
     * Returns:
     * - null if effect is indefinite (durationRounds is null)
     * - 0 if the effect expires on or before [currentRound]
     * - Positive number of rounds remaining otherwise
     */
    fun remainingRounds(currentRound: Int): Int? {
        val duration = durationRounds ?: return null
        // Calculate remaining: duration - (currentRound - startRound)
        // Clamp to 0 minimum to handle expired effects
        val elapsed = currentRound - startRound
        return (duration - elapsed).coerceAtLeast(0)
    }
}
