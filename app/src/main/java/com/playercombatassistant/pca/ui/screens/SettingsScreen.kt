package com.playercombatassistant.pca.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.playercombatassistant.pca.effects.GameSystem
import com.playercombatassistant.pca.settings.DefaultCombatMode
import com.playercombatassistant.pca.settings.SettingsViewModel
import com.playercombatassistant.pca.spells.SpellcastingSourceViewModel
import com.playercombatassistant.pca.spells.SpellcastingSource
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import com.playercombatassistant.pca.ui.screens.AddSpellcastingSourceDialog
import com.playercombatassistant.pca.ui.screens.EditSpellcastingSourceDialog

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                EnumDropdownRow(
                    label = "Game System",
                    currentLabel = settings.gameSystem.name,
                    options = GameSystem.entries,
                    optionLabel = { it.name },
                    onSelect = viewModel::setGameSystem,
                )

                EnumDropdownRow(
                    label = "Default Combat Mode",
                    currentLabel = settings.defaultCombatMode.name,
                    options = DefaultCombatMode.entries,
                    optionLabel = { it.name },
                    onSelect = viewModel::setDefaultCombatMode,
                )

                ToggleRow(
                    label = "Show modifier summary (PF1/PF2)",
                    checked = settings.showModifierSummary,
                    onCheckedChange = viewModel::setShowModifierSummary,
                )

                ToggleRow(
                    label = "Show rarity",
                    checked = settings.showRarity,
                    onCheckedChange = viewModel::setShowRarity,
                )

                HistoryLimitRow(
                    value = settings.historySessionLimit,
                    onChange = viewModel::setHistorySessionLimit,
                )
            }
        }

        // Spellcasting Sources
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Spellcasting Sources",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                SpellcastingSourcesSection()
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HistoryLimitRow(
    value: Int,
    onChange: (Int) -> Unit,
) {
    // Configurable later; for now keep it simple and safe.
    val min = 10
    val max = 200
    val clamped = value.coerceIn(min, max)
    if (clamped != value) onChange(clamped)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "History size limit (sessions)", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = { onChange((clamped - 10).coerceAtLeast(min)) }) { Text("-10") }
            Text(text = clamped.toString(), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { onChange((clamped + 10).coerceAtMost(max)) }) { Text("+10") }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "View-only; older sessions are trimmed when new events are recorded.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun <T> EnumDropdownRow(
    label: String,
    currentLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = currentLabel)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (opt in options) {
                DropdownMenuItem(
                    text = { Text(optionLabel(opt)) },
                    onClick = {
                        expanded = false
                        onSelect(opt)
                    },
                )
            }
        }
    }
}


@Composable
private fun SpellcastingSourcesSection() {
    val viewModel: SpellcastingSourceViewModel = viewModel()
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSource by remember { mutableStateOf<SpellcastingSource?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // List existing sources
        sources.forEach { source ->
            SpellcastingSourceRow(
                source = source,
                onEdit = { editingSource = source },
                onDelete = { showDeleteConfirm = source.id },
            )
        }

        // Add new source button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add Spellcasting Source")
        }
    }

    // Add source dialog
    if (showAddDialog) {
        AddSpellcastingSourceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color, slotsByLevel ->
                viewModel.addSource(name, color, slotsByLevel)
                showAddDialog = false
            },
        )
    }

    // Edit source dialog
    editingSource?.let { source ->
        EditSpellcastingSourceDialog(
            source = source,
            onDismiss = { editingSource = null },
            onConfirm = { updatedSource ->
                viewModel.updateSource(updatedSource)
                editingSource = null
            },
        )
    }

    // Delete confirmation
    showDeleteConfirm?.let { sourceId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Source?") },
            text = { Text("This will permanently delete this spellcasting source and all its slots.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSource(sourceId)
                        showDeleteConfirm = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = null },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SpellcastingSourceRow(
    source: SpellcastingSource,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Color swatch
        Surface(
            modifier = Modifier
                .height(32.dp)
                .width(32.dp),
            color = source.color,
            shape = MaterialTheme.shapes.small,
        ) {}
        
        // Source name
        Text(
            text = source.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        
        // Edit button
        OutlinedButton(onClick = onEdit) {
            Text("Edit")
        }
        
        // Delete button
        OutlinedButton(onClick = onDelete) {
            Text("Delete")
        }
    }
}
