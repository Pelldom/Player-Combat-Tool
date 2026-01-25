package com.playercombatassistant.pca.effects

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "pca_pinned_effects"
private val Context.pinnedEffectsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private val KEY_PINNED_EFFECT_IDS = stringSetPreferencesKey("pinned_effect_ids")

/**
 * Store for managing pinned effect IDs.
 * Pinned effects are stored as a set of effect IDs (from JSON definitions).
 */
class PinnedEffectsStore(
    private val context: Context,
) {
    /**
     * Flow of pinned effect IDs.
     */
    val pinnedEffectIds: Flow<Set<String>> =
        context.pinnedEffectsDataStore.data.map { prefs ->
            prefs[KEY_PINNED_EFFECT_IDS] ?: emptySet()
        }

    /**
     * Get current snapshot of pinned effect IDs.
     */
    suspend fun getSnapshot(): Set<String> = pinnedEffectIds.first()

    /**
     * Add an effect ID to pinned set.
     */
    suspend fun pinEffect(effectId: String) {
        context.pinnedEffectsDataStore.edit { prefs ->
            val current = prefs[KEY_PINNED_EFFECT_IDS] ?: emptySet()
            prefs[KEY_PINNED_EFFECT_IDS] = current + effectId
        }
    }

    /**
     * Remove an effect ID from pinned set.
     */
    suspend fun unpinEffect(effectId: String) {
        context.pinnedEffectsDataStore.edit { prefs ->
            val current = prefs[KEY_PINNED_EFFECT_IDS] ?: emptySet()
            prefs[KEY_PINNED_EFFECT_IDS] = current - effectId
        }
    }

    /**
     * Toggle pinned state of an effect ID.
     * Returns true if effect is now pinned, false if unpinned.
     */
    suspend fun togglePin(effectId: String): Boolean {
        var isPinned = false
        context.pinnedEffectsDataStore.edit { prefs ->
            val current = prefs[KEY_PINNED_EFFECT_IDS] ?: emptySet()
            if (effectId in current) {
                prefs[KEY_PINNED_EFFECT_IDS] = current - effectId
                isPinned = false
            } else {
                prefs[KEY_PINNED_EFFECT_IDS] = current + effectId
                isPinned = true
            }
        }
        return isPinned
    }

    /**
     * Clear all pinned effects.
     */
    suspend fun clearAll() {
        context.pinnedEffectsDataStore.edit { prefs ->
            prefs.remove(KEY_PINNED_EFFECT_IDS)
        }
    }
}
