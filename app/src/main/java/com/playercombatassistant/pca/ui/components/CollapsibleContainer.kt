package com.playercombatassistant.pca.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.playercombatassistant.pca.ui.state.LocalUIStateViewModel

/**
 * A reusable collapsible container that can wrap any UI content.
 *
 * Features:
 * - Header row with title and expand/collapse icon
 * - Tapping header toggles expanded/collapsed state
 * - Collapsed state shows only the title row
 * - Expanded state shows the full content
 * - Smooth animations for expand/collapse transitions
 * - State persists across screen navigation (session-only, not across app restarts)
 *
 * Usage:
 * ```
 * CollapsibleContainer(
 *     title = "Improvised Weapons",
 *     stateKey = "improvised_weapons",
 *     initiallyExpanded = true,
 * ) {
 *     // Your content here
 * }
 * ```
 *
 * @param title The title text displayed in the header
 * @param stateKey Unique key for this collapsible container to maintain state across navigation
 * @param modifier Modifier for the container
 * @param initiallyExpanded Whether the container starts expanded if no saved state exists (default: false)
 * @param onExpandedChange Optional callback when expanded state changes
 * @param content The composable content to display when expanded
 */
@Composable
fun CollapsibleContainer(
    title: String,
    stateKey: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val uiStateViewModel = LocalUIStateViewModel.current
    val savedState by uiStateViewModel.collapsibleStates.collectAsStateWithLifecycle()
    
    // Get initial state: check ViewModel directly first (for immediate access),
    // then fall back to Flow state, then to initiallyExpanded
    // Use remember to persist the initial value across recompositions
    var expanded by remember(stateKey) {
        val viewModelState = uiStateViewModel.getCollapsibleState(stateKey)
        val initialState = viewModelState ?: initiallyExpanded
        mutableStateOf(initialState)
    }
    
    // Sync local state with ViewModel state when it changes (e.g., from navigation or Flow updates)
    // This ensures state is consistent across screen switches
    // Use the saved state value directly as the key to avoid unnecessary recompositions
    val savedExpandedState = savedState[stateKey]
    LaunchedEffect(savedExpandedState) {
        savedExpandedState?.let { saved ->
            if (expanded != saved) {
                expanded = saved
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Header row - always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        // Save state to ViewModel for session persistence
                        uiStateViewModel.setCollapsibleState(stateKey, expanded)
                        onExpandedChange?.invoke(expanded)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Content - animated visibility
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(
                    expandFrom = Alignment.Top,
                    initialHeight = { 0 },
                ),
                exit = fadeOut() + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    targetHeight = { 0 },
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                ) {
                    content()
                }
            }
        }
    }
}
