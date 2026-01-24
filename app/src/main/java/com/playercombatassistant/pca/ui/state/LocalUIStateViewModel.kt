package com.playercombatassistant.pca.ui.state

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal for accessing the UIStateViewModel.
 * This ViewModel manages session-only UI state (e.g., collapsible container states).
 */
val LocalUIStateViewModel = staticCompositionLocalOf<UIStateViewModel> {
    error("No UIStateViewModel provided")
}
