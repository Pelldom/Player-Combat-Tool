package com.playercombatassistant.pca.effects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing pinned effects.
 * Provides reactive state for pinned effect IDs and methods to pin/unpin effects.
 */
class PinnedEffectsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = PinnedEffectsStore(application.applicationContext)

    /**
     * StateFlow of pinned effect IDs.
     */
    val pinnedEffectIds: StateFlow<Set<String>> =
        store.pinnedEffectIds.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptySet(),
        )

    /**
     * Pin an effect by its ID.
     */
    fun pinEffect(effectId: String) {
        viewModelScope.launch {
            store.pinEffect(effectId)
        }
    }

    /**
     * Unpin an effect by its ID.
     */
    fun unpinEffect(effectId: String) {
        viewModelScope.launch {
            store.unpinEffect(effectId)
        }
    }

    /**
     * Toggle pinned state of an effect.
     * Returns true if effect is now pinned, false if unpinned.
     */
    fun togglePin(effectId: String) {
        viewModelScope.launch {
            store.togglePin(effectId)
        }
    }

    /**
     * Check if an effect is pinned.
     */
    fun isPinned(effectId: String): Boolean {
        return effectId in pinnedEffectIds.value
    }
}
