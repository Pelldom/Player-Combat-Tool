package com.playercombatassistant.pca.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playercombatassistant.pca.history.CombatHistoryEvent
import com.playercombatassistant.pca.history.CombatSessionHistory
import com.playercombatassistant.pca.history.HistoryViewModel
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin
import com.playercombatassistant.pca.settings.SettingsViewModel
import com.playercombatassistant.pca.ui.icons.ImprovisedWeaponIcon

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to clear all combat history? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    },
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (sessions.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear History")
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "No combat history yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            return@LazyColumn
        }

        itemsIndexed(
            items = sessions.asReversed(),
            key = { _, s -> s.id },
        ) { index, session ->
            SessionCard(
                sessionNumber = sessions.size - index,
                session = session,
                showRarity = settings.showRarity,
            )
        }
    }
}

@Composable
private fun SessionCard(
    sessionNumber: Int,
    session: CombatSessionHistory,
    showRarity: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Combat Session $sessionNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (session.events.isEmpty()) {
                Text(
                    text = "No events recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (event in session.events) {
                    EventRow(event = event, showRarity = showRarity)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CombatHistoryEvent, showRarity: Boolean) {
    val roundPrefix = event.round?.let { "R$it • " } ?: ""

    val isImprovised = event is CombatHistoryEvent.ImprovisedWeaponRolled || event is CombatHistoryEvent.ImprovisedSelected
    val line = when (event) {
        is CombatHistoryEvent.StartCombat -> "${roundPrefix}Start Combat"
        is CombatHistoryEvent.NextRound -> "${roundPrefix}Next Round"
        is CombatHistoryEvent.EndCombat -> "${roundPrefix}End Combat"
        is CombatHistoryEvent.EffectApplied -> "${roundPrefix}Effect applied: ${event.effect.name}"
        is CombatHistoryEvent.EffectExpired -> "${roundPrefix}Effect expired: ${event.effect.name}"
        is CombatHistoryEvent.ImprovisedSelected ->
            if (showRarity) {
                "${roundPrefix}Improvised: ${event.item.description} (${event.item.rarity})"
            } else {
                "${roundPrefix}Improvised: ${event.item.description}"
            }
        is CombatHistoryEvent.ImprovisedWeaponRolled -> {
            // Keep out-of-combat rolls unchanged (same formatting as before lock metadata existed).
            if (event.combatId == null) {
                val rarityPart = if (showRarity) " (${event.item.rarity})" else ""
                val d30Part = event.d30Roll?.let { " d30=$it •" } ?: ""
                "${roundPrefix}Improvised Weapon •${d30Part} d100=${event.d100Roll} • ${event.locationName} → ${event.item.description}$rarityPart"
            } else {
                val rarityPart = if (showRarity) " (${event.item.rarity})" else ""
                val d30Part = event.d30Roll?.let { " d30=$it •" } ?: ""
                val lockPart = event.lockMode?.name?.let { " • $it" } ?: ""
                val originPart = if (event.origin == ImprovisedWeaponRollOrigin.MANUAL) " • Hybrid reroll" else ""
                "${roundPrefix}Improvised Weapon${originPart} •${d30Part} d100=${event.d100Roll} • ${event.locationName} → ${event.item.description}$rarityPart$lockPart"
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isImprovised) {
                ImprovisedWeaponIcon(
                    contentDescription = "Improvised weapon event",
                    useMonochrome = event is CombatHistoryEvent.ImprovisedSelected,
                    tint = if (event is CombatHistoryEvent.ImprovisedWeaponRolled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = line,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        when (event) {
            is CombatHistoryEvent.EffectApplied -> {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.effect.description.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is CombatHistoryEvent.EffectExpired -> {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.effect.description.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> Unit
        }
    }
}

