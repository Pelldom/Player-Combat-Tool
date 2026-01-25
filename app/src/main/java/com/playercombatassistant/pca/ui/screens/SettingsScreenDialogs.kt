package com.playercombatassistant.pca.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import com.playercombatassistant.pca.spells.SpellcastingSource

@Composable
fun AddSpellcastingSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Color, Map<Int, List<Boolean>>) -> Unit,
) {
    var nameText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF6200EE)) }
    var showSlotConfig by remember { mutableStateOf(false) }
    
    if (!showSlotConfig) {
        // Name and color selection
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Spellcasting Source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Source Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    
                    Text("Color:", style = MaterialTheme.typography.labelLarge)
                    ColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            showSlotConfig = true
                        }
                    },
                    enabled = nameText.isNotBlank(),
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
        )
    } else {
        // Slot configuration
        SlotConfigurationDialog(
            sourceName = nameText,
            onDismiss = { showSlotConfig = false },
            onConfirm = { slotsByLevel ->
                onConfirm(nameText, selectedColor, slotsByLevel)
            },
        )
    }
}

@Composable
fun EditSpellcastingSourceDialog(
    source: SpellcastingSource,
    onDismiss: () -> Unit,
    onConfirm: (SpellcastingSource) -> Unit,
) {
    var nameText by remember { mutableStateOf(source.name) }
    var selectedColor by remember { mutableStateOf(source.color) }
    var showSlotConfig by remember { mutableStateOf(false) }
    
    if (!showSlotConfig) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Spellcasting Source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Source Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    
                    Text("Color:", style = MaterialTheme.typography.labelLarge)
                    ColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            showSlotConfig = true
                        }
                    },
                    enabled = nameText.isNotBlank(),
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
        )
    } else {
        SlotConfigurationDialog(
            sourceName = nameText,
            initialSlots = source.slotsByLevel,
            onDismiss = { showSlotConfig = false },
            onConfirm = { slotsByLevel ->
                onConfirm(source.copy(name = nameText, color = selectedColor, slotsByLevel = slotsByLevel))
            },
        )
    }
}

@Composable
fun SlotConfigurationDialog(
    sourceName: String,
    initialSlots: Map<Int, List<Boolean>> = emptyMap(),
    onDismiss: () -> Unit,
    onConfirm: (Map<Int, List<Boolean>>) -> Unit,
) {
    val slotCounts = remember { mutableStateMapOf<Int, Int>() }
    
    // Initialize from existing slots or default to 0
    LaunchedEffect(initialSlots) {
        for (level in 0..9) {
            slotCounts[level] = initialSlots[level]?.size ?: 0
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Slots: $sourceName") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (level in 0..9) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Level $level:",
                            modifier = Modifier.width(80.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedTextField(
                            value = (slotCounts[level] ?: 0).toString(),
                            onValueChange = { newValue ->
                                slotCounts[level] = newValue.filter { it.isDigit() }.toIntOrNull() ?: 0
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        OutlinedButton(
                            onClick = {
                                val current = slotCounts[level] ?: 0
                                slotCounts[level] = (current - 1).coerceAtLeast(0)
                            },
                        ) {
                            Text("-")
                        }
                        OutlinedButton(
                            onClick = {
                                val current = slotCounts[level] ?: 0
                                slotCounts[level] = current + 1
                            },
                        ) {
                            Text("+")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val slotsByLevel = slotCounts.mapValues { (_, count) ->
                        List(count) { true } // All slots start as available
                    }
                    onConfirm(slotsByLevel)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
) {
    val colors = listOf(
        Color(0xFF6200EE), // Purple
        Color(0xFF03DAC6), // Teal
        Color(0xFF018786), // Dark Teal
        Color(0xFFBB86FC), // Light Purple
        Color(0xFFCF6679), // Pink
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Turquoise
        Color(0xFF45B7D1), // Blue
        Color(0xFF96CEB4), // Green
        Color(0xFFFFEAA7), // Yellow
        Color(0xFFDDA15E), // Orange
        Color(0xFF6C5CE7), // Indigo
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val columns = 4
        for (row in colors.chunked(columns)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (color in row) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onColorSelected(color) },
                        color = color,
                        shape = MaterialTheme.shapes.medium,
                        border = if (color == selectedColor) {
                            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                        } else null,
                    ) {}
                }
            }
        }
    }
}
