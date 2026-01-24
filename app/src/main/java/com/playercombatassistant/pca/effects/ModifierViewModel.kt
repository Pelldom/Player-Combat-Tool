package com.playercombatassistant.pca.effects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing modifier definitions loaded from assets.
 *
 * Responsibilities:
 * - Load modifier definitions from Pf1ConditionRepository
 * - Expose modifiers as LiveData for UI observation
 * - Handle loading by game system
 * - Cache loaded modifiers in memory
 *
 * Rules:
 * - Uses viewModelScope for coroutine lifecycle management
 * - Loads data on background thread, updates LiveData on main thread
 * - No memory leaks (repository is cached, ViewModel is lifecycle-aware)
 */
class ModifierViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Pf1ConditionRepository(application.applicationContext)

    private val _modifiers = MutableLiveData<List<ModifierDefinition>>(emptyList())
    val modifiers: LiveData<List<ModifierDefinition>> = _modifiers

    /**
     * Loads modifier definitions for the specified game system.
     * Updates the [modifiers] LiveData with the loaded modifiers.
     *
     * @param system The game system to load modifiers for
     */
    fun loadModifiers(system: GameSystem) {
        viewModelScope.launch {
            val loadedModifiers = withContext(Dispatchers.IO) {
                repository.getModifiersBySystem(system)
            }
            _modifiers.value = loadedModifiers
        }
    }
}
