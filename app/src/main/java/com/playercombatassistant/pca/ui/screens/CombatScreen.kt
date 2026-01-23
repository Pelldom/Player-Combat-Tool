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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playercombatassistant.pca.combat.CombatViewModel
import com.playercombatassistant.pca.effects.GameSystem
import com.playercombatassistant.pca.effects.ModifierAggregation
import com.playercombatassistant.pca.improvised.ImprovisedItem
import com.playercombatassistant.pca.improvised.ImprovisedWeaponResult
import com.playercombatassistant.pca.improvised.ImprovisedWeaponViewModel
import com.playercombatassistant.pca.improvised.LocationTable
import com.playercombatassistant.pca.improvised.Rarity
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin
import com.playercombatassistant.pca.settings.SettingsViewModel
import com.playercombatassistant.pca.ui.adaptive.LocalWindowSizeClass
import com.playercombatassistant.pca.ui.icons.ImprovisedWeaponIcon

@Composable
fun CombatScreen(
    modifier: Modifier = Modifier,
    viewModel: CombatViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    improvisedWeaponViewModel: ImprovisedWeaponViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val widthClass = LocalWindowSizeClass.current?.widthSizeClass ?: WindowWidthSizeClass.Compact

    val availableTables by improvisedWeaponViewModel.availableTables.collectAsStateWithLifecycle()
    val currentLocation by improvisedWeaponViewModel.currentLocation.collectAsStateWithLifecycle()
    val lastD30Roll by improvisedWeaponViewModel.lastD30Roll.collectAsStateWithLifecycle()
    val lastWeaponResult by improvisedWeaponViewModel.lastWeaponResult.collectAsStateWithLifecycle()
    val weaponRollingDisabledMessage by improvisedWeaponViewModel.weaponRollingDisabledMessage.collectAsStateWithLifecycle()

    val isPf = state.effects.any { it.system == GameSystem.PF1 || it.system == GameSystem.PF2 }
    val showModifierSummary = settings.showModifierSummary && isPf

    val rootModifier = modifier
        .fillMaxSize()
        .padding(16.dp)

    when (widthClass) {
        WindowWidthSizeClass.Compact -> CombatPhoneLayout(
            modifier = rootModifier,
            stateLabel = if (state.inCombat) "In Combat" else "Not In Combat",
            round = state.round,
            canStart = !state.inCombat,
            canNextRound = state.inCombat,
            canEnd = state.inCombat,
            onStartCombat = viewModel::startCombat,
            onNextRound = viewModel::nextRound,
            onEndCombat = viewModel::endCombat,
            effectsCount = state.effects.size,
            showModifierSummary = showModifierSummary,
            modifierSummary = if (showModifierSummary) ModifierAggregation.aggregateNumeric(state.effects) else emptyList(),
            currentLocationName = currentLocation?.name,
            locationWasRandom = lastD30Roll != null,
            lastD30Roll = lastD30Roll,
            lastWeaponResult = lastWeaponResult,
            weaponRollingDisabledMessage = weaponRollingDisabledMessage,
            availableTables = availableTables,
            inCombat = state.inCombat,
            onSelectLocation = improvisedWeaponViewModel::setLocationManually,
            onRollRandomLocation = improvisedWeaponViewModel::rollRandomLocation,
            onRollNewWeapon = {
                val combatId = state.sessionId
                val result = if (state.inCombat && combatId != null) {
                    // In combat, record improvised rolls against the combat session so history groups correctly.
                    improvisedWeaponViewModel.rollNewWeaponInCombat(
                        combatId = combatId,
                        round = state.round,
                        origin = ImprovisedWeaponRollOrigin.AUTOMATIC,
                    )
                } else {
                    // Out of combat, keep behavior/history unchanged (not tied to combat sessions).
                    improvisedWeaponViewModel.rollNewWeapon()
                }
            },
        )

        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> CombatTabletLayout(
            modifier = rootModifier,
            stateLabel = if (state.inCombat) "In Combat" else "Not In Combat",
            round = state.round,
            canStart = !state.inCombat,
            canNextRound = state.inCombat,
            canEnd = state.inCombat,
            onStartCombat = viewModel::startCombat,
            onNextRound = viewModel::nextRound,
            onEndCombat = viewModel::endCombat,
            effectsCount = state.effects.size,
            showModifierSummary = showModifierSummary,
            modifierSummary = if (showModifierSummary) ModifierAggregation.aggregateNumeric(state.effects) else emptyList(),
            currentLocationName = currentLocation?.name,
            locationWasRandom = lastD30Roll != null,
            lastD30Roll = lastD30Roll,
            lastWeaponResult = lastWeaponResult,
            weaponRollingDisabledMessage = weaponRollingDisabledMessage,
            availableTables = availableTables,
            inCombat = state.inCombat,
            onSelectLocation = improvisedWeaponViewModel::setLocationManually,
            onRollRandomLocation = improvisedWeaponViewModel::rollRandomLocation,
            onRollNewWeapon = {
                val combatId = state.sessionId
                val result = if (state.inCombat && combatId != null) {
                    improvisedWeaponViewModel.rollNewWeaponInCombat(
                        combatId = combatId,
                        round = state.round,
                        origin = ImprovisedWeaponRollOrigin.AUTOMATIC,
                    )
                } else {
                    improvisedWeaponViewModel.rollNewWeapon()
                }
            },
        )
        else -> CombatTabletLayout(
            modifier = rootModifier,
            stateLabel = if (state.inCombat) "In Combat" else "Not In Combat",
            round = state.round,
            canStart = !state.inCombat,
            canNextRound = state.inCombat,
            canEnd = state.inCombat,
            onStartCombat = viewModel::startCombat,
            onNextRound = viewModel::nextRound,
            onEndCombat = viewModel::endCombat,
            effectsCount = state.effects.size,
            showModifierSummary = showModifierSummary,
            modifierSummary = if (showModifierSummary) ModifierAggregation.aggregateNumeric(state.effects) else emptyList(),
            currentLocationName = currentLocation?.name,
            locationWasRandom = lastD30Roll != null,
            lastD30Roll = lastD30Roll,
            lastWeaponResult = lastWeaponResult,
            weaponRollingDisabledMessage = weaponRollingDisabledMessage,
            availableTables = availableTables,
            inCombat = state.inCombat,
            onSelectLocation = improvisedWeaponViewModel::setLocationManually,
            onRollRandomLocation = improvisedWeaponViewModel::rollRandomLocation,
            onRollNewWeapon = {
                val combatId = state.sessionId
                val result = if (state.inCombat && combatId != null) {
                    improvisedWeaponViewModel.rollNewWeaponInCombat(
                        combatId = combatId,
                        round = state.round,
                        origin = ImprovisedWeaponRollOrigin.AUTOMATIC,
                    )
                } else {
                    improvisedWeaponViewModel.rollNewWeapon()
                }
            },
        )
    }
}

@Composable
private fun CombatPhoneLayout(
    modifier: Modifier,
    stateLabel: String,
    round: Int,
    canStart: Boolean,
    canNextRound: Boolean,
    canEnd: Boolean,
    onStartCombat: () -> Unit,
    onNextRound: () -> Unit,
    onEndCombat: () -> Unit,
    effectsCount: Int,
    showModifierSummary: Boolean,
    modifierSummary: List<ModifierAggregation.TargetAggregation>,
    currentLocationName: String?,
    locationWasRandom: Boolean,
    lastD30Roll: Int?,
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
    inCombat: Boolean,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val phoneContainerModifier = Modifier
            .fillMaxWidth(0.75f)

        CombatStatusAndControlsCard(
            stateLabel = stateLabel,
            round = round,
            canStart = canStart,
            canNextRound = canNextRound,
            canEnd = canEnd,
            onStartCombat = onStartCombat,
            onNextRound = onNextRound,
            onEndCombat = onEndCombat,
            modifier = phoneContainerModifier,
        )
        ImprovisedWeaponSection(
            modifier = phoneContainerModifier,
            collapsible = true,
            pinControls = false,
            currentLocationName = currentLocationName,
            locationWasRandom = locationWasRandom,
            lastD30Roll = lastD30Roll,
            lastWeaponResult = lastWeaponResult,
            weaponRollingDisabledMessage = weaponRollingDisabledMessage,
            availableTables = availableTables,
            onSelectLocation = onSelectLocation,
            onRollRandomLocation = onRollRandomLocation,
            onRollNewWeapon = onRollNewWeapon,
        )
        ActiveEffectsCard(
            modifier = phoneContainerModifier,
            effectsCount = effectsCount,
        )
        if (showModifierSummary) {
            ModifierSummaryCard(
                modifier = phoneContainerModifier,
                summary = modifierSummary,
            )
        }
    }
}

@Composable
private fun CombatTabletLayout(
    modifier: Modifier,
    stateLabel: String,
    round: Int,
    canStart: Boolean,
    canNextRound: Boolean,
    canEnd: Boolean,
    onStartCombat: () -> Unit,
    onNextRound: () -> Unit,
    onEndCombat: () -> Unit,
    effectsCount: Int,
    showModifierSummary: Boolean,
    modifierSummary: List<ModifierAggregation.TargetAggregation>,
    currentLocationName: String?,
    locationWasRandom: Boolean,
    lastD30Roll: Int?,
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
    inCombat: Boolean,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(0.45f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CombatStatusAndControlsCard(
                stateLabel = stateLabel,
                round = round,
                canStart = canStart,
                canNextRound = canNextRound,
                canEnd = canEnd,
                onStartCombat = onStartCombat,
                onNextRound = onNextRound,
                onEndCombat = onEndCombat,
            )
            ImprovisedWeaponSection(
                collapsible = false,
                pinControls = true,
                currentLocationName = currentLocationName,
                locationWasRandom = locationWasRandom,
                lastD30Roll = lastD30Roll,
                lastWeaponResult = lastWeaponResult,
                weaponRollingDisabledMessage = weaponRollingDisabledMessage,
                availableTables = availableTables,
                onSelectLocation = onSelectLocation,
                onRollRandomLocation = onRollRandomLocation,
                onRollNewWeapon = onRollNewWeapon,
            )
        }

        Column(
            modifier = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActiveEffectsCard(
                modifier = Modifier.weight(1f),
                effectsCount = effectsCount,
            )
            if (showModifierSummary) {
                ModifierSummaryCard(
                    modifier = Modifier.weight(1f),
                    summary = modifierSummary,
                )
            } else {
                ModifierSummaryUnavailableCard()
            }
        }
    }
}

@Composable
private fun CombatStatusAndControlsCard(
    stateLabel: String,
    round: Int,
    canStart: Boolean,
    canNextRound: Boolean,
    canEnd: Boolean,
    onStartCombat: () -> Unit,
    onNextRound: () -> Unit,
    onEndCombat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Combat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stateLabel,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = if (round > 0) "Round $round" else "Round —",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Controls (stacked vertically, full-width). No behavior changes—only layout + visual hierarchy.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onStartCombat,
                    enabled = canStart,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start Combat")
                }

                FilledTonalButton(
                    onClick = onEndCombat,
                    enabled = canEnd,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("End Combat")
                }

                Button(
                    onClick = onNextRound,
                    enabled = canNextRound,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next Round")
                }
            }
        }
    }
}

@Composable
private fun ImprovisedWeaponSection(
    modifier: Modifier = Modifier,
    collapsible: Boolean,
    pinControls: Boolean,
    currentLocationName: String?,
    locationWasRandom: Boolean,
    lastD30Roll: Int?,
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
) {
    // Phone: collapsible to keep the screen compact. Tablet: always expanded/persistently visible.
    var expanded by rememberSaveable(collapsible) { mutableStateOf(!collapsible) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImprovisedWeaponIcon(
                    contentDescription = "Improvised weapon",
                    useMonochrome = weaponRollingDisabledMessage != null,
                    modifier = Modifier.width(20.dp).height(20.dp),
                    tint = if (weaponRollingDisabledMessage != null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Improvised Weapon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (collapsible) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                        )
                    }
                }
            }

            // Always-visible summary row(s) (same data; layout-only difference).
            Text(
                text = "Location: ${currentLocationName ?: "—"}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Last d100: " + (lastWeaponResult?.d100Roll?.toString() ?: "—"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (expanded) {
                val scrollState = rememberScrollState()

                // Details may scroll on tablet; controls stay visible (pinned).
                val detailsModifier = if (pinControls) {
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                } else {
                    Modifier
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImprovisedWeaponDetails(
                        modifier = detailsModifier,
                        currentLocationName = currentLocationName,
                        locationWasRandom = locationWasRandom,
                        lastD30Roll = lastD30Roll,
                        lastWeaponResult = lastWeaponResult,
                        weaponRollingDisabledMessage = weaponRollingDisabledMessage,
                    )

                    ImprovisedWeaponControls(
                        weaponRollingDisabledMessage = weaponRollingDisabledMessage,
                        availableTables = availableTables,
                        onSelectLocation = onSelectLocation,
                        onRollRandomLocation = onRollRandomLocation,
                        onRollNewWeapon = onRollNewWeapon,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImprovisedWeaponDetails(
    modifier: Modifier = Modifier,
    currentLocationName: String?,
    locationWasRandom: Boolean,
    lastD30Roll: Int?,
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (weaponRollingDisabledMessage != null) {
            Text(
                text = weaponRollingDisabledMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Text(
            text = "Mode: " + if (currentLocationName == null) "—" else if (locationWasRandom) "Random (d30)" else "Manual",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (lastD30Roll != null) {
            Text(
                text = "Last d30: $lastD30Roll",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

            Spacer(modifier = Modifier.height(4.dp))

            val item: ImprovisedItem? = lastWeaponResult?.item
            Text(
                text = item?.description ?: "No weapon rolled yet.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item != null) FontWeight.Medium else FontWeight.Normal,
            )

            if (item != null) {
                RarityBadge(rarity = item.rarity)
            }
    }
}

@Composable
private fun ImprovisedWeaponControls(
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
) {
    var showLocationPicker by remember { mutableStateOf(false) }
    val isDisabled = weaponRollingDisabledMessage != null

    if (showLocationPicker) {
        LocationPickerBottomSheet(
            availableTables = availableTables,
            onPick = { table ->
                onSelectLocation(table.id)
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false },
        )
    }

    // Unified vertical button group with consistent sizing and spacing
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Select Location - FilledTonalButton
        FilledTonalButton(
            onClick = { showLocationPicker = true },
            enabled = !isDisabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Filled.EditLocationAlt,
                contentDescription = "Select location",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Location")
        }

        // Random Location - FilledTonalButton (same style as Select Location)
        FilledTonalButton(
            onClick = onRollRandomLocation,
            enabled = !isDisabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = "Roll random location",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Random Location")
        }

        // Random Weapon - Button (primary, alternate color scheme)
        Button(
            onClick = onRollNewWeapon,
            enabled = !isDisabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ImprovisedWeaponIcon(
                contentDescription = "Roll improvised weapon",
                useMonochrome = isDisabled,
                tint = if (isDisabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Random Weapon")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerBottomSheet(
    availableTables: List<LocationTable>,
    onPick: (LocationTable) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Select Location",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            if (availableTables.isEmpty()) {
                Text(
                    text = "No locations available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                return@ModalBottomSheet
            }

            // Scrollable list (inside the sheet) with user-friendly location names only.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(availableTables, key = { it.id }) { table ->
                    FilledTonalButton(
                        onClick = { onPick(table) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(table.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    val style = when (rarity) {
        Rarity.COMMON -> RarityBadgeStyle(
            icon = Icons.Outlined.FiberManualRecord,
            label = "Common",
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconSize = 14.dp,
        )
        Rarity.UNCOMMON -> RarityBadgeStyle(
            icon = Icons.Outlined.StarBorder,
            label = "Uncommon",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            iconSize = 16.dp,
        )
        Rarity.RARE -> RarityBadgeStyle(
            icon = Icons.Filled.Star,
            label = "Rare",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            iconSize = 16.dp,
        )
    }

    Surface(
        color = style.containerColor,
        contentColor = style.contentColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                modifier = Modifier.width(style.iconSize).height(style.iconSize),
                tint = style.contentColor,
            )
            Text(
                text = style.label,
                style = MaterialTheme.typography.labelMedium,
                color = style.contentColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private data class RarityBadgeStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val iconSize: Dp,
)

@Composable
private fun ActiveEffectsCard(
    modifier: Modifier = Modifier,
    effectsCount: Int,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Active Effects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reserved panel — $effectsCount active",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ModifierSummaryCard(
    modifier: Modifier = Modifier,
    summary: List<ModifierAggregation.TargetAggregation>,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Modifier Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (summary.isEmpty()) {
                Text(
                    text = "No numeric modifiers to summarize (display-only).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            // Use a simple Column here to avoid nested scrolling/layout constraint issues
            // when the parent screen is scrollable (e.g., compact phone layout).
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (targetAgg in summary) {
                    Column {
                        Text(
                            text = "${targetAgg.target}: ${targetAgg.total}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        for (src in targetAgg.sources) {
                            Text(
                                text = "• ${src.source}: ${src.sum} (${src.values.joinToString(", ")})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModifierSummaryUnavailableCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Modifier Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Reserved panel (PF1/PF2 only)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

