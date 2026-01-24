package com.playercombatassistant.pca.modifiers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "pca_pinned_modifiers"
private val Context.pinnedModifiersDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private val KEY_PINNED_MODIFIER_IDS = stringSetPreferencesKey("pinned_modifier_ids")

/**
 * Store for managing pinned modifier IDs persistently.
 *
 * Responsibilities:
 * - Persist set of pinned modifier IDs
 * - Expose pinned IDs as Flow
 * - Provide functions to add/remove pins
 *
 * Rules:
 * - No business logic
 * - No validation
 * - Pure storage layer
 */
class PinnedModifiersStore(
    private val context: Context,
) {
    /**
     * Flow of pinned modifier IDs.
     * Emits a Set<String> of modifier IDs that are currently pinned.
     */
    val pinnedModifierIds: Flow<Set<String>> =
        context.pinnedModifiersDataStore.data.map { prefs ->
            prefs[KEY_PINNED_MODIFIER_IDS] ?: emptySet()
        }

    /**
     * Add a modifier ID to the pinned set.
     *
     * @param modifierId The modifier ID to pin
     */
    suspend fun pinModifier(modifierId: String) {
        context.pinnedModifiersDataStore.edit { prefs ->
            val current = prefs[KEY_PINNED_MODIFIER_IDS] ?: emptySet()
            prefs[KEY_PINNED_MODIFIER_IDS] = current + modifierId
        }
    }

    /**
     * Remove a modifier ID from the pinned set.
     *
     * @param modifierId The modifier ID to unpin
     */
    suspend fun unpinModifier(modifierId: String) {
        context.pinnedModifiersDataStore.edit { prefs ->
            val current = prefs[KEY_PINNED_MODIFIER_IDS] ?: emptySet()
            prefs[KEY_PINNED_MODIFIER_IDS] = current - modifierId
        }
    }

    /**
     * Check if a modifier ID is currently pinned.
     *
     * @param modifierId The modifier ID to check
     * @return true if the modifier is pinned, false otherwise
     */
    suspend fun isPinned(modifierId: String): Boolean {
        val current = context.pinnedModifiersDataStore.data.first()
        val pinnedIds = current[KEY_PINNED_MODIFIER_IDS] ?: emptySet()
        return pinnedIds.contains(modifierId)
    }
}
