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
 * - Pure data object (no behavior, no calculations)
 * - Color-coded for visual distinction on round tracker
 *
 * **Duration Rules:**
 * - [durationRounds] is the number of rounds the effect lasts
 * - If [durationRounds] is null, the effect is indefinite
 * - [endRound] = [startRound] + [durationRounds] (if durationRounds is not null)
 * - [endRound] = null if [durationRounds] is null (indefinite effect)
 *
 * **Example Use Cases:**
 * - "Poisoned - 3 rounds remaining"
 * - "Blessed - indefinite"
 * - "Custom buff - 5 rounds"
 * - "Reminder note about environmental effect"
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
    
    /**
     * The combat round when this effect ends (inclusive).
     * 
     * Rules:
     * - If [durationRounds] is not null (finite duration):
     *   endRound = startRound + durationRounds
     * - If [durationRounds] is null (indefinite effect):
     *   endRound = null
     * 
     * This field is calculated from [startRound] and [durationRounds],
     * but stored for convenience and querying.
     */
    val endRound: Int?,
    
    /**
     * The number of rounds remaining for this effect.
     * 
     * Rules:
     * - Initially set to [durationRounds] when effect is created
     * - Decremented each round via onNextRound()
     * - If null: effect is indefinite (never expires)
     * - If <= 0: effect has expired and should be removed
     */
    val remainingRounds: Int?,
) {
    /**
     * Check if this effect is still active at the given round.
     * 
     * Rules:
     * - Returns true if endRound is null (indefinite)
     * - Returns true if currentRound <= endRound
     * - Returns false if currentRound > endRound
     */
    fun isActiveAt(currentRound: Int): Boolean {
        return endRound == null || currentRound <= endRound
    }
    
    /**
     * Calculate remaining rounds at the given current round.
     * 
     * Returns:
     * - null if effect is indefinite (endRound is null)
     * - 0 or positive number if effect has rounds remaining
     * - negative number if effect has expired (should not happen in normal use)
     * 
     * Note: This method calculates from endRound, but [remainingRounds] field
     * is the authoritative source that gets decremented each round.
     */
    fun remainingRoundsAt(currentRound: Int): Int? {
        return endRound?.let { it - currentRound }
    }
}
