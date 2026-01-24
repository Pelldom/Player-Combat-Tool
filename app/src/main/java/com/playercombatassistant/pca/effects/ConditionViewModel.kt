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
 * ViewModel for managing condition definitions loaded from assets.
 *
 * Responsibilities:
 * - Load condition definitions from Pf1ConditionRepository
 * - Expose conditions as LiveData for UI observation
 * - Handle loading by game system
 * - Cache loaded conditions in memory
 *
 * Rules:
 * - Uses viewModelScope for coroutine lifecycle management
 * - Loads data on background thread, updates LiveData on main thread
 * - No memory leaks (repository is cached, ViewModel is lifecycle-aware)
 */
class ConditionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Pf1ConditionRepository(application.applicationContext)

    private val _conditions = MutableLiveData<List<ConditionDefinition>>(emptyList())
    val conditions: LiveData<List<ConditionDefinition>> = _conditions

    /**
     * Loads condition definitions for the specified game system.
     * Updates the [conditions] LiveData with the loaded conditions.
     *
     * @param system The game system to load conditions for
     */
    fun loadConditions(system: GameSystem) {
        viewModelScope.launch {
            val loadedConditions = withContext(Dispatchers.IO) {
                repository.getConditionsBySystem(system)
            }
            _conditions.value = loadedConditions
        }
    }
}
