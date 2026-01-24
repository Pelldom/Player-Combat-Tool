package com.playercombatassistant.pca.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TabRow
import com.playercombatassistant.pca.ui.components.CollapsibleContainer
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID
import com.playercombatassistant.pca.combat.CombatViewModel
import com.playercombatassistant.pca.effects.Effect
import com.playercombatassistant.pca.effects.EffectColorId
import com.playercombatassistant.pca.effects.toColor
import com.playercombatassistant.pca.effects.EffectType
import com.playercombatassistant.pca.effects.EffectsViewModel
import com.playercombatassistant.pca.effects.GameSystem
import com.playercombatassistant.pca.effects.GenericEffect
import com.playercombatassistant.pca.effects.ModifierAggregation
import com.playercombatassistant.pca.effects.Pf1ConditionRepository
import com.playercombatassistant.pca.effects.ModifierTarget
import com.playercombatassistant.pca.effects.getDisplayLabel
import com.playercombatassistant.pca.improvised.ImprovisedItem
import com.playercombatassistant.pca.modifiers.ModifierRepository
import com.playercombatassistant.pca.modifiers.ModifierType
import com.playercombatassistant.pca.improvised.ImprovisedWeaponResult
import com.playercombatassistant.pca.improvised.ImprovisedWeaponViewModel
import com.playercombatassistant.pca.improvised.LocationTable
import com.playercombatassistant.pca.improvised.Rarity
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin
import com.playercombatassistant.pca.settings.SettingsViewModel
import com.playercombatassistant.pca.ui.adaptive.LocalWindowSizeClass
import com.playercombatassistant.pca.ui.components.RoundTrackerBar
import com.playercombatassistant.pca.ui.icons.ImprovisedWeaponIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatScreen(
    modifier: Modifier = Modifier,
    viewModel: CombatViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    improvisedWeaponViewModel: ImprovisedWeaponViewModel = viewModel(),
    effectsViewModel: EffectsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val widthClass = LocalWindowSizeClass.current?.widthSizeClass ?: WindowWidthSizeClass.Compact

    val availableTables by improvisedWeaponViewModel.availableTables.collectAsStateWithLifecycle()
    val currentLocation by improvisedWeaponViewModel.currentLocation.collectAsStateWithLifecycle()
    val lastD30Roll by improvisedWeaponViewModel.lastD30Roll.collectAsStateWithLifecycle()
    val lastWeaponResult by improvisedWeaponViewModel.lastWeaponResult.collectAsStateWithLifecycle()
    val weaponRollingDisabledMessage by improvisedWeaponViewModel.weaponRollingDisabledMessage.collectAsStateWithLifecycle()

    val activeEffects by effectsViewModel.activeEffects.collectAsStateWithLifecycle()
    val activeGenericEffects by effectsViewModel.activeGenericEffects.collectAsStateWithLifecycle()
    val isPf = activeEffects.any { it.system == GameSystem.PF1 || it.system == GameSystem.PF2 }
    val showModifierSummary = settings.showModifierSummary && isPf

    var showAddEffectSheet by remember { mutableStateOf(false) }
    val addEffectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colorScheme = MaterialTheme.colorScheme

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
            onStartCombat = {
                viewModel.startCombat()
                // Update effects view model to round 1 when combat starts
                effectsViewModel.processNextRound(1)
            },
            onNextRound = {
                val nextRound = state.round + 1
                viewModel.nextRound()
                // Update effects view model when round advances (nextRound increments by 1)
                effectsViewModel.processNextRound(nextRound)
            },
            onEndCombat = {
                viewModel.endCombat()
                // Clear all effects when combat ends
                effectsViewModel.clearEffects()
            },
            showModifierSummary = showModifierSummary,
            modifierSummary = if (showModifierSummary) ModifierAggregation.aggregateNumeric(activeEffects) else emptyList(),
            activeEffects = activeEffects,
            activeGenericEffects = activeGenericEffects,
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
            onAddGenericEffect = { showAddEffectSheet = true },
            gameSystem = settings.gameSystem,
        )

        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded -> CombatTabletLayout(
            modifier = rootModifier,
            stateLabel = if (state.inCombat) "In Combat" else "Not In Combat",
            round = state.round,
            canStart = !state.inCombat,
            canNextRound = state.inCombat,
            canEnd = state.inCombat,
            onStartCombat = {
                viewModel.startCombat()
                // Update effects view model to round 1 when combat starts
                effectsViewModel.processNextRound(1)
            },
            onNextRound = {
                val nextRound = state.round + 1
                viewModel.nextRound()
                // Update effects view model when round advances (nextRound increments by 1)
                effectsViewModel.processNextRound(nextRound)
            },
            onEndCombat = {
                viewModel.endCombat()
                // Clear all effects when combat ends
                effectsViewModel.clearEffects()
            },
            showModifierSummary = showModifierSummary,
            modifierSummary = if (showModifierSummary) ModifierAggregation.aggregateNumeric(activeEffects) else emptyList(),
            activeEffects = activeEffects,
            activeGenericEffects = activeGenericEffects,
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
            onAddGenericEffect = { showAddEffectSheet = true },
            gameSystem = settings.gameSystem,
        )
        else -> CombatTabletLayout(
            modifier = rootModifier,
            stateLabel = if (state.inCombat) "In Combat" else "Not In Combat",
            round = state.round,
            canStart = !state.inCombat,
            canNextRound = state.inCombat,
            canEnd = state.inCombat,
            onStartCombat = {
                viewModel.startCombat()
                // Update effects view model to round 1 when combat starts
                effectsViewModel.processNextRound(1)
            },
            onNextRound = {
                val nextRound = state.round + 1
                viewModel.nextRound()
                // Update effects view model when round advances (nextRound increments by 1)
                effectsViewModel.processNextRound(nextRound)
            },
            onEndCombat = {
                viewModel.endCombat()
                // Clear all effects when combat ends
                effectsViewModel.clearEffects()
            },
            showModifierSummary = showModifierSummary,
            modifierSummary = if (showModifierSummary) ModifierAggregation.aggregateNumeric(activeEffects) else emptyList(),
            activeEffects = activeEffects,
            activeGenericEffects = activeGenericEffects,
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
            onAddGenericEffect = { showAddEffectSheet = true },
            gameSystem = settings.gameSystem,
        )
    }

    // Add Generic Effect bottom sheet (shared across all layouts)
    if (showAddEffectSheet) {
        AddGenericEffectSheet(
            sheetState = addEffectSheetState,
            colorScheme = colorScheme,
            currentGameSystem = settings.gameSystem,
            currentRound = state.round,
            effectsViewModel = effectsViewModel,
            onDismiss = { showAddEffectSheet = false },
            onAdd = { name, duration, colorId, notes, modifierType, modifierTarget, modifierValue ->
                val durationRounds = duration
                effectsViewModel.addGenericEffect(
                    name = name,
                    notes = notes,
                    colorId = colorId,
                    durationRounds = durationRounds,
                    round = state.round,
                    modifierType = modifierType,
                    modifierTarget = modifierTarget,
                    modifierValue = modifierValue,
                )
                showAddEffectSheet = false
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
    activeEffects: List<Effect>,
    activeGenericEffects: List<GenericEffect>,
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
    onAddGenericEffect: () -> Unit,
    gameSystem: GameSystem,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Left content (75% width) - scrollable
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxHeight()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val phoneContainerModifier = Modifier.fillMaxWidth()

            CombatStatusAndControlsCard(
                stateLabel = stateLabel,
                round = round,
                canStart = canStart,
                canNextRound = canNextRound,
                canEnd = canEnd,
                onStartCombat = onStartCombat,
                onNextRound = onNextRound,
                onEndCombat = onEndCombat,
                activeEffects = activeEffects,
                activeGenericEffects = activeGenericEffects,
                gameSystem = gameSystem,
                modifier = phoneContainerModifier,
            )
            CollapsibleContainer(
                title = "Improvised Weapon",
                stateKey = "combat_improvised_weapon",
                modifier = phoneContainerModifier,
            ) {
                ImprovisedWeaponSection(
                    modifier = Modifier.fillMaxWidth(),
                    collapsible = false,
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
            }
            CollapsibleContainer(
                title = "Active Effects",
                stateKey = "combat_active_effects",
                modifier = phoneContainerModifier,
            ) {
                ActiveEffectsCard(
                    modifier = Modifier.fillMaxWidth(),
                    activeEffects = activeEffects,
                    activeGenericEffects = activeGenericEffects,
                    currentRound = round,
                    onAddGenericEffect = onAddGenericEffect,
                )
            }
            CollapsibleContainer(
                title = "Spell Slot Tracker",
                stateKey = "combat_spell_slot_tracker",
                modifier = phoneContainerModifier,
            ) {
                Text(
                    text = "Spell Slot tracking will be added in a future update.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showModifierSummary) {
                ModifierSummaryCard(
                    modifier = phoneContainerModifier,
                    summary = modifierSummary,
                )
            }
        }

        // Right side (25% width) - RoundTrackerBar (fills available height)
        RoundTrackerBar(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight(),
            currentRound = round,
            effects = activeGenericEffects,
        )
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
    activeEffects: List<Effect>,
    activeGenericEffects: List<GenericEffect>,
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
    onAddGenericEffect: () -> Unit,
    gameSystem: GameSystem,
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
                activeEffects = activeEffects,
                activeGenericEffects = activeGenericEffects,
                gameSystem = gameSystem,
            )
            CollapsibleContainer(
                title = "Improvised Weapon",
                stateKey = "combat_improvised_weapon_tablet",
            ) {
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
        }

        Column(
            modifier = Modifier.weight(0.5f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CollapsibleContainer(
                title = "Active Effects",
                stateKey = "combat_active_effects_tablet",
                modifier = Modifier.fillMaxWidth(),
            ) {
                ActiveEffectsCard(
                    modifier = Modifier.fillMaxWidth(),
                    activeEffects = activeEffects,
                    activeGenericEffects = activeGenericEffects,
                    currentRound = round,
                    onAddGenericEffect = onAddGenericEffect,
                )
            }
            CollapsibleContainer(
                title = "Spell Slot Tracker",
                stateKey = "combat_spell_slot_tracker_tablet",
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Spell Slot tracking will be added in a future update.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showModifierSummary) {
                ModifierSummaryCard(
                    modifier = Modifier.weight(1f),
                    summary = modifierSummary,
                )
            } else {
                ModifierSummaryUnavailableCard()
            }
        }

        // RoundTrackerBar on the right side for tablet
        Column(
            modifier = Modifier.weight(0.05f),
        ) {
            RoundTrackerBar(
                modifier = Modifier.fillMaxSize(),
                currentRound = round,
                effects = activeGenericEffects,
            )
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
    activeEffects: List<Effect>,
    activeGenericEffects: List<GenericEffect>,
    gameSystem: GameSystem,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val modifierRepository = remember { ModifierRepository(context) }
    
    // Aggregate modifiers with stacking rules
    val modifierTotals = remember(
        activeEffects,
        activeGenericEffects,
        gameSystem,
    ) {
        ModifierAggregation.aggregateWithStacking(
            activeEffects = activeEffects,
            activeGenericEffects = activeGenericEffects,
            gameSystem = gameSystem,
            modifierRepository = modifierRepository,
        )
    }
    
    // Filter to only non-zero targets and format for display
    val displayTargets = listOf(
        "Armor Class" to (modifierTotals["Armor Class"] ?: 0),
        "Attack Rolls" to (modifierTotals["Attack Rolls"] ?: 0),
        "Saving Throws" to (modifierTotals["Saving Throws"] ?: 0),
        "Skill Checks" to (modifierTotals["Skill Checks"] ?: 0),
        "Initiative" to (modifierTotals["Initiative"] ?: 0),
    ).filter { (_, value) -> value != 0 }
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
                
                // Modifier totals display (single line, read-only)
                if (displayTargets.isNotEmpty()) {
                    Text(
                        text = displayTargets.joinToString("   ") { (target, value) ->
                            val sign = if (value >= 0) "+" else ""
                            "$target $sign$value"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
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
    activeEffects: List<Effect>,
    activeGenericEffects: List<GenericEffect>,
    currentRound: Int,
    onAddGenericEffect: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Active Effects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                FilledTonalButton(onClick = onAddGenericEffect) {
                    Text("Add Effect")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val allEffectsCount = activeEffects.size + activeGenericEffects.size
            if (allEffectsCount == 0) {
                Text(
                    text = "No active effects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Scrollable list of effects (independent scrolling if needed)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(activeEffects, key = { it.id }) { effect ->
                        EffectListItem(effect = effect)
                    }
                    items(activeGenericEffects, key = { it.id }) { genericEffect ->
                        GenericEffectListItem(
                            genericEffect = genericEffect,
                            currentRound = currentRound,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectListItem(effect: Effect) {
    val colorScheme = MaterialTheme.colorScheme
    val effectTypeLabel = when (effect.type) {
        EffectType.CONDITION -> "Condition"
        EffectType.TIMER -> "Timer"
    }
    val roundsText = effect.remainingRounds?.let { "$it rounds remaining" } ?: "Indefinite duration"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${effect.name}, $effectTypeLabel effect, ${effect.system.name}, $roundsText"
            },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Color indicator (left edge) - enhanced for better visibility
            val effectColorId = when (effect.type) {
                EffectType.CONDITION -> EffectColorId.ERROR
                EffectType.TIMER -> EffectColorId.PRIMARY
            }
            Surface(
                modifier = Modifier
                    .width(5.dp)
                    .height(40.dp)
                    .semantics { contentDescription = "$effectTypeLabel indicator" },
                color = effectColorId.toColor(colorScheme),
                shape = MaterialTheme.shapes.small,
            ) {}

            // Effect content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Name
                Text(
                    text = effect.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )

                // System summary text
                Text(
                    text = effect.system.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Description (if present)
                if (effect.description.isNotBlank()) {
                    Text(
                        text = effect.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Remaining rounds indicator
            if (effect.remainingRounds != null) {
                Surface(
                    color = colorScheme.secondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "${effect.remainingRounds}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = "$roundsText"
                            },
                    )
                }
            } else {
                // Indefinite effect indicator
                Surface(
                    color = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "∞",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = "$roundsText"
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun GenericEffectListItem(
    genericEffect: GenericEffect,
    currentRound: Int,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val effectColor = genericEffect.colorId.toColor(colorScheme)
    val remainingRounds = genericEffect.remainingRounds
    val roundsText = remainingRounds?.let { 
        if (it > 0) "$it rounds remaining" else "Expired"
    } ?: "Indefinite duration"
    
    // Load modifier types to get labels
    val modifierRepository = remember { ModifierRepository(context) }
    val allModifierTypes = remember { modifierRepository.getAllModifierTypes() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${genericEffect.name}, generic effect, $roundsText"
            },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Color indicator (left edge)
            Surface(
                modifier = Modifier
                    .width(5.dp)
                    .height(40.dp)
                    .semantics { contentDescription = "generic effect indicator" },
                color = effectColor,
                shape = MaterialTheme.shapes.small,
            ) {}

            // Effect content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Name
                Text(
                    text = genericEffect.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )

                // Modifier information (if present)
                if (genericEffect.modifierTarget != null || genericEffect.modifierValue != null) {
                    val modifierParts = mutableListOf<String>()
                    if (genericEffect.modifierTarget != null) {
                        modifierParts.add(genericEffect.modifierTarget.getDisplayLabel())
                    }
                    if (genericEffect.modifierValue != null) {
                        val valueText = if (genericEffect.modifierValue >= 0) {
                            "+${genericEffect.modifierValue}"
                        } else {
                            genericEffect.modifierValue.toString()
                        }
                        modifierParts.add(valueText)
                    }
                    if (genericEffect.modifierType != null) {
                        // Try to get the label from all types, or just show the ID
                        val typeLabel = allModifierTypes.find { it.id == genericEffect.modifierType }?.label
                            ?: genericEffect.modifierType
                        modifierParts.add("($typeLabel)")
                    }
                    Text(
                        text = modifierParts.joinToString(" "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Notes (if present)
                if (!genericEffect.notes.isNullOrBlank()) {
                    Text(
                        text = genericEffect.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Remaining rounds indicator
            if (remainingRounds != null) {
                Surface(
                    color = colorScheme.secondaryContainer,
                    contentColor = colorScheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "$remainingRounds",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = roundsText
                            },
                    )
                }
            } else {
                // Indefinite effect indicator
                Surface(
                    color = colorScheme.tertiaryContainer,
                    contentColor = colorScheme.onTertiaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "∞",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics {
                                contentDescription = roundsText
                            },
                    )
                }
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGenericEffectSheet(
    sheetState: androidx.compose.material3.SheetState,
    colorScheme: androidx.compose.material3.ColorScheme,
    currentGameSystem: GameSystem,
    currentRound: Int,
    effectsViewModel: EffectsViewModel,
    onDismiss: () -> Unit,
    onAdd: (String, Int?, EffectColorId, String?, String?, ModifierTarget?, Int?) -> Unit,
) {
    val context = LocalContext.current
    val conditions = remember {
        Pf1ConditionRepository(context).getAllConditions()
            .sortedBy { it.name }
    }
    
    // Load modifier types filtered by current game system
    val modifierRepository = remember { ModifierRepository(context) }
    val availableModifierTypes = remember(currentGameSystem) {
        modifierRepository.getModifierTypesBySystem(currentGameSystem)
            .sortedBy { it.label }
    }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf(
        "Generic Effect",
        "Conditions",
        "Spell Effects",
        "Feats & Abilities",
    )

    // Form state
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(EffectColorId.PRIMARY) }
    var duration by remember { mutableStateOf(1) }
    var isIndefinite by remember { mutableStateOf(false) }
    var conditionDuration by remember { mutableStateOf(1) }
    var selectedModifierType by remember { mutableStateOf<ModifierType?>(null) }
    var modifierTypeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedModifierTarget by remember { mutableStateOf<ModifierTarget?>(null) }
    var modifierTargetDropdownExpanded by remember { mutableStateOf(false) }
    var modifierValue by remember { mutableStateOf(0) }

    // Validation
    val isNameValid = name.isNotBlank()
    val isDurationValid = duration >= 1
    val canAdd = isNameValid && isDurationValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Add Effect",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                    )
                }
            }

            if (selectedTabIndex == 0) {
            // Modifier fields in a horizontal layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Modifier Type dropdown (optional)
                if (availableModifierTypes.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modifier Type",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { modifierTypeDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = selectedModifierType?.label ?: "None",
                                maxLines = 1,
                            )
                        }
                        DropdownMenu(
                            expanded = modifierTypeDropdownExpanded,
                            onDismissRequest = { modifierTypeDropdownExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (optional)") },
                                onClick = {
                                    selectedModifierType = null
                                    modifierTypeDropdownExpanded = false
                                },
                            )
                            for (modifierType in availableModifierTypes) {
                                DropdownMenuItem(
                                    text = { Text(modifierType.label) },
                                    onClick = {
                                        selectedModifierType = modifierType
                                        modifierTypeDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                
                // Modifier Target dropdown (optional)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modifier Target",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { modifierTargetDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = selectedModifierTarget?.getDisplayLabel() ?: "None",
                            maxLines = 1,
                        )
                    }
                    DropdownMenu(
                        expanded = modifierTargetDropdownExpanded,
                        onDismissRequest = { modifierTargetDropdownExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (optional)") },
                            onClick = {
                                selectedModifierTarget = null
                                modifierTargetDropdownExpanded = false
                            },
                        )
                        for (target in ModifierTarget.entries) {
                            DropdownMenuItem(
                                text = { Text(target.getDisplayLabel()) },
                                onClick = {
                                    selectedModifierTarget = target
                                    modifierTargetDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
                
                // Modifier Value input (optional)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Modifier Value",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        // Display current value next to label
                        Surface(
                            color = colorScheme.secondaryContainer,
                            contentColor = colorScheme.onSecondaryContainer,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                text = if (modifierValue >= 0) "+$modifierValue" else modifierValue.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { modifierValue = modifierValue - 1 },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("-")
                        }
                        Text(
                            text = if (modifierValue >= 0) "+$modifierValue" else modifierValue.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center,
                        )
                        OutlinedButton(
                            onClick = { modifierValue = modifierValue + 1 },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("+")
                        }
                    }
                }
            }
            
            // Name field (required)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !isNameValid && name.isNotEmpty(),
                supportingText = if (!isNameValid && name.isNotEmpty()) {
                    { Text("Name is required") }
                } else null,
            )

            // Duration field with +/- controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { duration = (duration - 1).coerceAtLeast(1) },
                        modifier = Modifier.width(56.dp),
                    ) {
                        Text("-")
                    }
                    Text(
                        text = if (isIndefinite) "Indefinite" else duration.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = { duration = duration + 1 },
                        modifier = Modifier.width(56.dp),
                        enabled = !isIndefinite,
                    ) {
                        Text("+")
                    }
                }
                FilledTonalButton(
                    onClick = {
                        isIndefinite = !isIndefinite
                        if (!isIndefinite && duration < 1) duration = 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isIndefinite) "Set Finite Duration" else "Set Indefinite Duration")
                }
            }

            // Color picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelLarge,
                )
                // Color palette grid
                val colors = EffectColorId.defaultPalette()
                val columns = 4
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in colors.chunked(columns)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (color in row) {
                                val isSelected = selectedColor == color
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable { selectedColor = color },
                                    color = color.toColor(colorScheme),
                                    shape = MaterialTheme.shapes.medium,
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(
                                            width = 3.dp,
                                            color = colorScheme.primary,
                                        )
                                    } else null,
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Selected",
                                                tint = colorScheme.onPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                            // Fill remaining columns if needed
                            repeat(columns - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Notes field (optional)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )

            // Add Effect button
            Button(
                onClick = {
                    val finalName = name.trim()
                    val finalNotes = notes.takeIf { it.isNotBlank() }?.trim()
                    val finalDuration = if (isIndefinite) null else duration
                    val finalModifierType = selectedModifierType?.id
                    val finalModifierTarget = selectedModifierTarget
                    val finalModifierValue = if (modifierValue != 0) modifierValue else null
                    onAdd(
                        finalName,
                        finalDuration,
                        selectedColor,
                        finalNotes,
                        finalModifierType,
                        finalModifierTarget,
                        finalModifierValue,
                    )
                },
                enabled = canAdd,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Effect")
            }
            } else if (selectedTabIndex == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Duration (rounds)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { conditionDuration = (conditionDuration - 1).coerceAtLeast(1) },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("-")
                        }
                        Text(
                            text = conditionDuration.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = { conditionDuration = conditionDuration + 1 },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("+")
                        }
                    }
                    conditions.forEach { condition ->
                        OutlinedButton(
                            onClick = {
                                // Convert condition modifiers to Effect.Modifier format
                                val effectModifiers = condition.modifiers.map { modEntry ->
                                    com.playercombatassistant.pca.effects.Modifier(
                                        target = modEntry.target,
                                        value = modEntry.value,
                                        source = condition.name,
                                    )
                                }
                                
                                // Create an Effect object for the condition (not GenericEffect)
                                // This preserves all modifiers from the condition
                                val conditionEffect = Effect(
                                    id = UUID.randomUUID().toString(),
                                    name = condition.name,
                                    system = condition.system,
                                    description = condition.shortDescription,
                                    remainingRounds = conditionDuration,
                                    type = EffectType.CONDITION,
                                    modifiers = effectModifiers,
                                    startRound = currentRound,
                                    endRound = currentRound + conditionDuration - 1,
                                )
                                
                                // Add as Effect (not GenericEffect) to preserve modifiers
                                effectsViewModel.addEffect(currentRound, conditionEffect)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(condition.name)
                        }
                    }
                }
            } else if (selectedTabIndex == 2) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Spell effects will be added in a future update.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Feats and abilities will be added in a future update.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
