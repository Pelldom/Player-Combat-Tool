package com.playercombatassistant.pca.spells

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing spell slot configuration and states.
 */
class SpellSlotViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SpellSlotStore(application.applicationContext)

    /**
     * StateFlow of maximum spell level.
     */
    val maxSpellLevel: StateFlow<Int> =
        store.maxSpellLevel.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            9,
        )

    /**
     * StateFlow of slots per level.
     */
    val slotsPerLevel: StateFlow<Int> =
        store.slotsPerLevel.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0,
        )

    /**
     * StateFlow of spell slot states.
     */
    val slotStates: StateFlow<SpellSlotStates> =
        store.slotStates.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SpellSlotStates(),
        )

    /**
     * Set maximum spell level.
     */
    fun setMaxSpellLevel(level: Int) {
        viewModelScope.launch {
            store.setMaxSpellLevel(level)
        }
    }

    /**
     * Set slots per level.
     */
    fun setSlotsPerLevel(count: Int) {
        viewModelScope.launch {
            store.setSlotsPerLevel(count)
        }
    }

    /**
     * Toggle slot state for a specific level and slot index.
     */
    fun toggleSlot(level: Int, slotIndex: Int) {
        viewModelScope.launch {
            val current = slotStates.value
            val levelSlots = current.slots[level] ?: return@launch
            if (slotIndex >= 0 && slotIndex < levelSlots.size) {
                val currentState = levelSlots[slotIndex]
                store.setSlotState(level, slotIndex, !currentState)
            }
        }
    }

    /**
     * Initialize slots based on current configuration.
     */
    fun initializeSlots() {
        viewModelScope.launch {
            store.initializeSlots()
        }
    }

    /**
     * Reset all slots to available.
     */
    fun resetAllSlots() {
        viewModelScope.launch {
            store.resetAllSlots()
        }
    }
}
