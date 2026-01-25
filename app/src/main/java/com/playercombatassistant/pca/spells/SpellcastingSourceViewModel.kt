package com.playercombatassistant.pca.spells

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing spellcasting sources and their spell slots.
 */
class SpellcastingSourceViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SpellcastingSourceStore(application.applicationContext)

    /**
     * StateFlow of all spellcasting sources.
     */
    val sources: StateFlow<List<SpellcastingSource>> =
        store.sources.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    /**
     * Add a new spellcasting source.
     */
    fun addSource(name: String, color: Color, slotsByLevel: Map<Int, List<Boolean>>) {
        viewModelScope.launch {
            val source = SpellcastingSource(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color,
                slotsByLevel = slotsByLevel,
            )
            store.addSource(source)
        }
    }

    /**
     * Update an existing spellcasting source.
     */
    fun updateSource(source: SpellcastingSource) {
        viewModelScope.launch {
            store.updateSource(source)
        }
    }

    /**
     * Delete a spellcasting source.
     */
    fun deleteSource(sourceId: String) {
        viewModelScope.launch {
            store.deleteSource(sourceId)
        }
    }

    /**
     * Toggle a slot state.
     */
    fun toggleSlot(sourceId: String, level: Int, slotIndex: Int) {
        viewModelScope.launch {
            store.toggleSlot(sourceId, level, slotIndex)
        }
    }

    /**
     * Reset all slots for all sources.
     */
    fun resetAllSlots() {
        viewModelScope.launch {
            store.resetAllSlots()
        }
    }

    /**
     * Clear all sources (for migration).
     */
    fun clearAll() {
        viewModelScope.launch {
            store.clearAll()
        }
    }

    /**
     * Check if old spell slot data exists and needs migration.
     * Returns true if old data exists.
     */
    suspend fun hasOldData(): Boolean {
        // Check if old store has data
        val oldStore = SpellSlotStore(getApplication<Application>().applicationContext)
        return try {
            val oldMaxLevel = oldStore.getMaxSpellLevel()
            val oldSlotsPerLevel = oldStore.getSlotsPerLevel()
            oldMaxLevel > 0 || oldSlotsPerLevel > 0
        } catch (e: Exception) {
            false
        }
    }
}
