package com.playercombatassistant.pca.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing UI state that should persist across screen navigation
 * but not across app restarts (session-only persistence).
 *
 * Currently manages:
 * - Collapsible container expanded/collapsed states
 */
class UIStateViewModel(application: Application) : AndroidViewModel(application) {
    private val _collapsibleStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val collapsibleStates: StateFlow<Map<String, Boolean>> = _collapsibleStates.asStateFlow()

    /**
     * Get the expanded state for a collapsible container with the given key.
     * Returns null if the state hasn't been set yet (use initiallyExpanded in that case).
     */
    fun getCollapsibleState(key: String): Boolean? {
        return _collapsibleStates.value[key]
    }

    /**
     * Set the expanded state for a collapsible container with the given key.
     */
    fun setCollapsibleState(key: String, expanded: Boolean) {
        _collapsibleStates.update { current ->
            current + (key to expanded)
        }
    }

    /**
     * Toggle the expanded state for a collapsible container with the given key.
     * If the state doesn't exist, it will be set to true.
     */
    fun toggleCollapsibleState(key: String) {
        val current = _collapsibleStates.value[key] ?: false
        setCollapsibleState(key, !current)
    }
}
