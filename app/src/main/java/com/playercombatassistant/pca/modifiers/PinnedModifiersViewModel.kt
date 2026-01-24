package com.playercombatassistant.pca.modifiers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playercombatassistant.pca.effects.ModifierDefinition
import com.playercombatassistant.pca.effects.Pf1ConditionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing pinned modifiers.
 *
 * Responsibilities:
 * - Load modifier definitions from repository
 * - Track which modifiers are pinned
 * - Expose pinned modifiers as StateFlow
 * - Provide functions to pin/unpin modifiers
 *
 * Rules:
 * - No rule enforcement
 * - Pure UI + storage
 */
class PinnedModifiersViewModel(application: Application) : AndroidViewModel(application) {
    private val store = PinnedModifiersStore(application.applicationContext)
    private val repository = Pf1ConditionRepository(application.applicationContext)

    // Load all modifiers once (repository caches internally)
    private val allModifiers = repository.getAllModifiers()

    /**
     * StateFlow of pinned modifier definitions.
     * Combines pinned IDs with available modifiers to provide full ModifierDefinition objects.
     */
    val pinnedModifiers: StateFlow<List<ModifierDefinition>> =
        store.pinnedModifierIds.map { pinnedIds ->
            // Filter modifiers to only those that are pinned, maintaining order
            pinnedIds.mapNotNull { id ->
                allModifiers.find { it.id == id }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    /**
     * Pin a modifier by ID.
     *
     * @param modifierId The modifier ID to pin
     */
    fun pinModifier(modifierId: String) {
        viewModelScope.launch {
            store.pinModifier(modifierId)
        }
    }

    /**
     * Unpin a modifier by ID.
     *
     * @param modifierId The modifier ID to unpin
     */
    fun unpinModifier(modifierId: String) {
        viewModelScope.launch {
            store.unpinModifier(modifierId)
        }
    }

    /**
     * Check if a modifier is pinned.
     *
     * @param modifierId The modifier ID to check
     * @return StateFlow<Boolean> indicating if the modifier is pinned
     */
    fun isPinned(modifierId: String): kotlinx.coroutines.flow.Flow<Boolean> {
        return store.pinnedModifierIds.map { it.contains(modifierId) }
    }
}
