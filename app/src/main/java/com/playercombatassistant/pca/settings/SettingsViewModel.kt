package com.playercombatassistant.pca.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playercombatassistant.pca.effects.GameSystem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = PcaSettingsStore(application.applicationContext)

    val settings: StateFlow<PcaSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PcaSettings())

    fun setGameSystem(system: GameSystem) {
        viewModelScope.launch { store.setGameSystem(system) }
    }

    fun setDefaultCombatMode(mode: DefaultCombatMode) {
        viewModelScope.launch { store.setDefaultCombatMode(mode) }
    }

    fun setShowModifierSummary(value: Boolean) {
        viewModelScope.launch { store.setShowModifierSummary(value) }
    }

    fun setShowRarity(value: Boolean) {
        viewModelScope.launch { store.setShowRarity(value) }
    }

    fun setHistorySessionLimit(value: Int) {
        viewModelScope.launch { store.setHistorySessionLimit(value) }
    }
}

