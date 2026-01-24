package com.playercombatassistant.pca.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.playercombatassistant.pca.effects.ModifierDefinition

/**
 * Widget displaying pinned modifiers in a horizontal scrollable list.
 *
 * Features:
 * - Horizontal list of pinned modifiers
 * - Tap to add modifier to active effects
 * - Long press or overflow menu to remove pin
 * - Material 3 Card styling
 */
@Composable
fun PinnedModifiersWidget(
    pinnedModifiers: List<ModifierDefinition>,
    onModifierTap: (ModifierDefinition) -> Unit,
    onUnpinModifier: (ModifierDefinition) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pinnedModifiers.isEmpty()) {
        return // Don't show widget if no pinned modifiers
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Pinned Modifiers",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = pinnedModifiers,
                    key = { it.id },
                ) { modifierDef ->
                    PinnedModifierChip(
                        modifier = modifierDef,
                        onClick = { onModifierTap(modifierDef) },
                        onUnpin = { onUnpinModifier(modifierDef) },
                    )
                }
            }
        }
    }
}

/**
 * Individual chip for a pinned modifier.
 */
@Composable
private fun PinnedModifierChip(
    modifier: ModifierDefinition,
    onClick: () -> Unit,
    onUnpin: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = modifier.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Show effects summary
                val effectsSummary = modifier.effects.take(2).joinToString(", ") { effect ->
                    "${effect.valueOrDescription} ${effect.target}"
                }
                if (effectsSummary.isNotEmpty()) {
                    Text(
                        text = effectsSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Overflow menu for unpin
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.width(32.dp).height(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Unpin") },
                    onClick = {
                        showMenu = false
                        onUnpin()
                    },
                )
            }
        }
    }
}
