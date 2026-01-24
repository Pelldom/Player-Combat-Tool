package com.playercombatassistant.pca.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playercombatassistant.pca.combat.CombatViewModel
import com.playercombatassistant.pca.effects.ConditionDefinition
import com.playercombatassistant.pca.effects.ConditionRepository
import com.playercombatassistant.pca.effects.Pf1ConditionRepository
import com.playercombatassistant.pca.effects.ModifierDefinition
import com.playercombatassistant.pca.effects.Effect
import com.playercombatassistant.pca.modifiers.PinnedModifiersViewModel
import com.playercombatassistant.pca.ui.components.PinnedModifiersWidget
import com.playercombatassistant.pca.effects.EffectColorId
import com.playercombatassistant.pca.effects.toColor
import com.playercombatassistant.pca.effects.EffectType
import com.playercombatassistant.pca.effects.EffectsViewModel
import com.playercombatassistant.pca.effects.GameSystem
import com.playercombatassistant.pca.effects.GenericEffect
import com.playercombatassistant.pca.effects.ModifierAggregation
import com.playercombatassistant.pca.improvised.ImprovisedItem
import com.playercombatassistant.pca.improvised.ImprovisedWeaponResult
import com.playercombatassistant.pca.improvised.ImprovisedWeaponViewModel
import com.playercombatassistant.pca.improvised.LocationTable
import com.playercombatassistant.pca.improvised.Rarity
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin
import com.playercombatassistant.pca.settings.SettingsViewModel
import com.playercombatassistant.pca.ui.adaptive.LocalWindowSizeClass
import com.playercombatassistant.pca.ui.components.RoundTrackerBar
import com.playercombatassistant.pca.ui.components.CollapsibleContainer
import com.playercombatassistant.pca.ui.icons.ImprovisedWeaponIcon
import com.playercombatassistant.pca.modifiers.UserModifier
import com.playercombatassistant.pca.modifiers.ModifierType
import com.playercombatassistant.pca.modifiers.defaultColorId
import com.playercombatassistant.pca.modifiers.getDisplayName
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatScreen(
    modifier: Modifier = Modifier,
    viewModel: CombatViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    improvisedWeaponViewModel: ImprovisedWeaponViewModel = viewModel(),
    effectsViewModel: EffectsViewModel = viewModel(),
    pinnedModifiersViewModel: PinnedModifiersViewModel = viewModel(),
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
    // Generic effects are backed by Compose state in the ViewModel, so we can
    // read them directly without collectAsState.
    val activeGenericEffects = effectsViewModel.activeGenericEffects
    val isPf = activeEffects.any { it.system == GameSystem.PF1 || it.system == GameSystem.PF2 }
    val showModifierSummary = settings.showModifierSummary && isPf

    val pinnedModifiers by pinnedModifiersViewModel.pinnedModifiers.collectAsStateWithLifecycle()

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
            modifierSummary = if (showModifierSummary) {
                ModifierAggregation.aggregateEnhanced(activeEffects, activeGenericEffects)
            } else {
                ModifierAggregation.EnhancedSummary(0, emptyList())
            },
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
            pinnedModifiers = pinnedModifiers,
            onPinnedModifierTap = { modifierDef ->
                // Add modifier as a generic effect
                val modifierNames = modifierDef.name
                val effectsSummary = modifierDef.effects.joinToString("; ") { effect ->
                    "${effect.target}: ${effect.valueOrDescription}"
                }
                effectsViewModel.addGenericEffect(
                    name = modifierNames,
                    notes = effectsSummary,
                    colorId = EffectColorId.PRIMARY,
                    durationRounds = null, // Indefinite by default
                    round = state.round,
                )
            },
            onUnpinModifier = { modifierDef ->
                pinnedModifiersViewModel.unpinModifier(modifierDef.id)
            },
            effectsViewModel = effectsViewModel,
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
            modifierSummary = if (showModifierSummary) {
                ModifierAggregation.aggregateEnhanced(activeEffects, activeGenericEffects)
            } else {
                ModifierAggregation.EnhancedSummary(0, emptyList())
            },
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
            pinnedModifiers = pinnedModifiers,
            onPinnedModifierTap = { modifierDef ->
                // Add modifier as a generic effect
                val modifierNames = modifierDef.name
                val effectsSummary = modifierDef.effects.joinToString("; ") { effect ->
                    "${effect.target}: ${effect.valueOrDescription}"
                }
                effectsViewModel.addGenericEffect(
                    name = modifierNames,
                    notes = effectsSummary,
                    colorId = EffectColorId.PRIMARY,
                    durationRounds = null, // Indefinite by default
                    round = state.round,
                )
            },
            onUnpinModifier = { modifierDef ->
                pinnedModifiersViewModel.unpinModifier(modifierDef.id)
            },
            effectsViewModel = effectsViewModel,
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
            modifierSummary = if (showModifierSummary) {
                ModifierAggregation.aggregateEnhanced(activeEffects, activeGenericEffects)
            } else {
                ModifierAggregation.EnhancedSummary(0, emptyList())
            },
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
            pinnedModifiers = pinnedModifiers,
            onPinnedModifierTap = { modifierDef ->
                // Add modifier as a generic effect
                val modifierNames = modifierDef.name
                val effectsSummary = modifierDef.effects.joinToString("; ") { effect ->
                    "${effect.target}: ${effect.valueOrDescription}"
                }
                effectsViewModel.addGenericEffect(
                    name = modifierNames,
                    notes = effectsSummary,
                    colorId = EffectColorId.PRIMARY,
                    durationRounds = null, // Indefinite by default
                    round = state.round,
                )
            },
            onUnpinModifier = { modifierDef ->
                pinnedModifiersViewModel.unpinModifier(modifierDef.id)
            },
            effectsViewModel = effectsViewModel,
        )
    }

    // Add Generic Effect bottom sheet (shared across all layouts)
    if (showAddEffectSheet) {
        AddGenericEffectSheet(
            sheetState = addEffectSheetState,
            colorScheme = colorScheme,
            currentRound = state.round,
            onDismiss = { showAddEffectSheet = false },
            onAdd = { name, duration, colorId, notes ->
                val durationRounds = duration
                effectsViewModel.addGenericEffect(name, notes, colorId, durationRounds, state.round)
                showAddEffectSheet = false
            },
            onAddModifier = { userModifier ->
                // Convert UserModifier to GenericEffect using the built-in method
                val genericEffect = userModifier.toGenericEffect()
                // Add the effect using the view model
                effectsViewModel.addGenericEffect(
                    name = genericEffect.name,
                    notes = genericEffect.notes,
                    colorId = genericEffect.colorId,
                    durationRounds = genericEffect.durationRounds,
                    round = genericEffect.startRound,
                )
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
    modifierSummary: ModifierAggregation.EnhancedSummary,
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
    pinnedModifiers: List<ModifierDefinition>,
    onPinnedModifierTap: (ModifierDefinition) -> Unit,
    onUnpinModifier: (ModifierDefinition) -> Unit,
    effectsViewModel: EffectsViewModel,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
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

            // Pinned Modifiers Widget (if present)
            if (pinnedModifiers.isNotEmpty()) {
                PinnedModifiersWidget(
                    pinnedModifiers = pinnedModifiers,
                    onModifierTap = onPinnedModifierTap,
                    onUnpinModifier = onUnpinModifier,
                    modifier = phoneContainerModifier,
                )
            }

            // Combat Status and Controls - Always expanded at top
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

            // All other sections in collapsible containers, collapsed by default
            ImprovisedWeaponSection(
                modifier = phoneContainerModifier,
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

            CollapsibleContainer(
                title = "Effects",
                stateKey = "combat_effects",
                modifier = phoneContainerModifier,
                initiallyExpanded = false,
            ) {
                ActiveEffectsContent(
                    modifier = Modifier.fillMaxWidth(),
                    activeEffects = activeEffects,
                    activeGenericEffects = activeGenericEffects,
                    currentRound = round,
                    onAddGenericEffect = onAddGenericEffect,
                    effectsViewModel = effectsViewModel,
                )
            }

            if (showModifierSummary) {
                CollapsibleContainer(
                    title = "Modifier Summary",
                    stateKey = "combat_modifier_summary",
                    modifier = phoneContainerModifier,
                    initiallyExpanded = false,
                ) {
                    ModifierSummaryCard(
                        modifier = Modifier.fillMaxWidth(),
                        summary = modifierSummary,
                    )
                }
            }

            CollapsibleContainer(
                title = "Spell Slots",
                stateKey = "combat_spell_slots",
                modifier = phoneContainerModifier,
                initiallyExpanded = false,
            ) {
                SpellSlotsPlaceholderContent()
            }

            CollapsibleContainer(
                title = "Modifier Builder",
                stateKey = "combat_modifier_builder",
                modifier = phoneContainerModifier,
                initiallyExpanded = false,
            ) {
                ModifierBuilderPlaceholderContent()
            }

            CollapsibleContainer(
                title = "Condition Presets",
                stateKey = "combat_condition_presets",
                modifier = phoneContainerModifier,
                initiallyExpanded = false,
            ) {
                ConditionPresetsPlaceholderContent()
            }
        }

        // Right side (25% width) - RoundTrackerBar (vertically aligned with left content)
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
    modifierSummary: ModifierAggregation.EnhancedSummary,
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
    pinnedModifiers: List<ModifierDefinition>,
    onPinnedModifierTap: (ModifierDefinition) -> Unit,
    onUnpinModifier: (ModifierDefinition) -> Unit,
    effectsViewModel: EffectsViewModel,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Left column - scrollable with all sections
        val leftScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .verticalScroll(leftScrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val tabletContainerModifier = Modifier.fillMaxWidth()

            // Pinned Modifiers Widget (if present)
            if (pinnedModifiers.isNotEmpty()) {
                PinnedModifiersWidget(
                    pinnedModifiers = pinnedModifiers,
                    onModifierTap = onPinnedModifierTap,
                    onUnpinModifier = onUnpinModifier,
                    modifier = tabletContainerModifier,
                )
            }

            // Combat Status and Controls - Always expanded at top
            CombatStatusAndControlsCard(
                stateLabel = stateLabel,
                round = round,
                canStart = canStart,
                canNextRound = canNextRound,
                canEnd = canEnd,
                onStartCombat = onStartCombat,
                onNextRound = onNextRound,
                onEndCombat = onEndCombat,
                modifier = tabletContainerModifier,
            )

            // All other sections in collapsible containers, collapsed by default
            ImprovisedWeaponSection(
                modifier = tabletContainerModifier,
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

        // Right column - scrollable with all sections
        val rightScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .verticalScroll(rightScrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val tabletContainerModifier = Modifier.fillMaxWidth()

            CollapsibleContainer(
                title = "Effects",
                stateKey = "combat_effects",
                modifier = tabletContainerModifier,
                initiallyExpanded = false,
            ) {
                ActiveEffectsContent(
                    modifier = Modifier.fillMaxWidth(),
                    activeEffects = activeEffects,
                    activeGenericEffects = activeGenericEffects,
                    currentRound = round,
                    onAddGenericEffect = onAddGenericEffect,
                    effectsViewModel = effectsViewModel,
                )
            }

            if (showModifierSummary) {
                CollapsibleContainer(
                    title = "Modifier Summary",
                    stateKey = "combat_modifier_summary",
                    modifier = tabletContainerModifier,
                    initiallyExpanded = false,
                ) {
                    ModifierSummaryCard(
                        modifier = Modifier.fillMaxWidth(),
                        summary = modifierSummary,
                    )
                }
            }

            CollapsibleContainer(
                title = "Spell Slots",
                stateKey = "combat_spell_slots",
                modifier = tabletContainerModifier,
                initiallyExpanded = false,
            ) {
                SpellSlotsPlaceholderContent()
            }

            CollapsibleContainer(
                title = "Modifier Builder",
                stateKey = "combat_modifier_builder",
                modifier = tabletContainerModifier,
                initiallyExpanded = false,
            ) {
                ModifierBuilderPlaceholderContent()
            }

            CollapsibleContainer(
                title = "Condition Presets",
                stateKey = "combat_condition_presets",
                modifier = tabletContainerModifier,
                initiallyExpanded = false,
            ) {
                ConditionPresetsPlaceholderContent()
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
    // All sections collapse by default for cleaner UI
    CollapsibleContainer(
        title = "Improvised Weapons",
        stateKey = "combat_improvised_weapons",
        modifier = modifier,
        initiallyExpanded = false,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Summary row(s) - always visible when expanded
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Location: ${currentLocationName ?: "—"}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Last d100: " + (lastWeaponResult?.d100Roll?.toString() ?: "—"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

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
    ActiveEffectsContent(
        modifier = modifier,
        activeEffects = activeEffects,
        activeGenericEffects = activeGenericEffects,
        currentRound = currentRound,
        onAddGenericEffect = onAddGenericEffect,
        effectsViewModel = null, // Effects card doesn't have access to viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveEffectsContent(
    modifier: Modifier = Modifier,
    activeEffects: List<Effect>,
    activeGenericEffects: List<GenericEffect>,
    currentRound: Int,
    onAddGenericEffect: () -> Unit,
    effectsViewModel: EffectsViewModel? = null,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            // Non-nested scroll: the outer combat column scrolls; this list itself is non-scrollable
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                activeEffects.forEach { effect ->
                    EffectListItem(effect = effect)
                }
                // Edit state management
                var editingEffect by remember { mutableStateOf<GenericEffect?>(null) }
                val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val colorScheme = MaterialTheme.colorScheme

                activeGenericEffects.forEach { genericEffect ->
                    GenericEffectListItem(
                        genericEffect = genericEffect,
                        currentRound = currentRound,
                        onEdit = if (effectsViewModel != null) {
                            { editingEffect = it }
                        } else null,
                    )
                }

                // Edit sheet for modifier-based effects
                if (editingEffect != null && effectsViewModel != null) {
                    EditModifierEffectSheet(
                        sheetState = editSheetState,
                        colorScheme = colorScheme,
                        currentRound = currentRound,
                        effect = editingEffect!!,
                        onDismiss = { editingEffect = null },
                        onSave = { updatedEffect ->
                            effectsViewModel.updateGenericEffect(
                                effectId = updatedEffect.id,
                                name = updatedEffect.name,
                                notes = updatedEffect.notes,
                                colorId = updatedEffect.colorId,
                                durationRounds = updatedEffect.durationRounds,
                            )
                            editingEffect = null
                        },
                    )
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

/**
 * Parsed information from a modifier-based GenericEffect.
 * Used to display modifier effects distinctively in the UI.
 */
private data class ParsedModifierEffect(
    val modifierType: ModifierType,
    val value: Int?,
    val sign: Int, // 1 for positive, -1 for negative
    val freeText: String?,
    val displayValue: String, // e.g., "+2", "-3", or null if freeText only
)

/**
 * Attempt to parse a GenericEffect to determine if it's from a UserModifier.
 * Returns null if it doesn't match the expected pattern.
 */
private fun parseModifierEffect(genericEffect: GenericEffect): ParsedModifierEffect? {
    val name = genericEffect.name.trim()
    val notes = genericEffect.notes?.trim() ?: ""
    
    // Try to match patterns from UserModifier.getDisplayName() and getNotes()
    // Pattern 1: "TypeName +value" or "TypeName -value"
    val valuePattern = Regex("""^(.+?)\s+([+-]?\d+)$""")
    val valueMatch = valuePattern.find(name)
    
    // Pattern 2: "TypeName: freeText"
    val freeTextPattern = Regex("""^(.+?):\s*(.+)$""")
    val freeTextMatch = freeTextPattern.find(name)
    
    // Try to find matching ModifierType
    val modifierType = ModifierType.entries.find { type ->
        val typeName = type.getDisplayName()
        when {
            valueMatch != null -> valueMatch.groupValues[1].trim() == typeName
            freeTextMatch != null -> freeTextMatch.groupValues[1].trim() == typeName
            else -> name == typeName
        }
    } ?: return null
    
    return when {
        valueMatch != null -> {
            // Has numeric value
            val valueStr = valueMatch.groupValues[2]
            val value = valueStr.toIntOrNull() ?: return null
            val sign = if (value >= 0) 1 else -1
            val absValue = kotlin.math.abs(value)
            
            // Check notes for freeText: "TypeName +value. freeText"
            val freeText = if (notes.isNotEmpty() && notes != name) {
                val notesPattern = Regex("""^$name\.\s*(.+)$""")
                notesPattern.find(notes)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
            } else null
            
            ParsedModifierEffect(
                modifierType = modifierType,
                value = absValue,
                sign = sign,
                freeText = freeText,
                displayValue = if (value >= 0) "+$value" else "$value",
            )
        }
        freeTextMatch != null -> {
            // Has freeText only
            val freeText = freeTextMatch.groupValues[2].trim()
            ParsedModifierEffect(
                modifierType = modifierType,
                value = null,
                sign = 1,
                freeText = freeText,
                displayValue = "",
            )
        }
        else -> {
            // Just type name, no value or freeText
            null
        }
    }
}

@Composable
private fun GenericEffectListItem(
    genericEffect: GenericEffect,
    currentRound: Int,
    onEdit: ((GenericEffect) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectColor = genericEffect.colorId.toColor(colorScheme)
    val remainingRounds = genericEffect.remainingRounds(currentRound)
    val roundsText = remainingRounds?.let { 
        if (it > 0) "$it rounds remaining" else "Expired"
    } ?: "Indefinite duration"
    
    // Try to parse as modifier-based effect
    val parsedModifier = parseModifierEffect(genericEffect)
    val isModifierBased = parsedModifier != null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (isModifierBased) {
                    val modifierDesc = parsedModifier?.let { 
                        "${it.modifierType.getDisplayName()} ${it.displayValue}"
                    } ?: genericEffect.name
                    "$modifierDesc, modifier effect, $roundsText"
                } else {
                    "${genericEffect.name}, generic effect, $roundsText"
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Color indicator (left edge) - uses assigned colorId
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .semantics { 
                        contentDescription = if (isModifierBased) "modifier effect indicator" else "generic effect indicator"
                    },
                color = effectColor,
                shape = MaterialTheme.shapes.small,
            ) {}

            // Effect content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isModifierBased && parsedModifier != null) {
                    // Modifier-based effect: show type and value prominently
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = parsedModifier.modifierType.getDisplayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (parsedModifier.displayValue.isNotEmpty()) {
                            Text(
                                text = parsedModifier.displayValue,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = effectColor,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        // Rounds remaining inline
                        if (remainingRounds != null && remainingRounds > 0) {
                            Text(
                                text = "$remainingRounds rounds left",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else if (remainingRounds == null) {
                            Text(
                                text = "Indefinite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    
                    // Free text in secondary line if present
                    if (!parsedModifier.freeText.isNullOrBlank()) {
                        Text(
                            text = parsedModifier.freeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                } else {
                    // Regular generic effect: show name and notes
                    Text(
                        text = genericEffect.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )

                    // Notes (if present)
                    if (!genericEffect.notes.isNullOrBlank()) {
                        Text(
                            text = genericEffect.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            // Edit button (if onEdit callback provided)
            if (onEdit != null) {
                IconButton(
                    onClick = { onEdit(genericEffect) },
                    modifier = Modifier.semantics {
                        contentDescription = "Edit effect"
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Remaining rounds indicator (only show if not modifier-based, as it's shown inline)
            if (!isModifierBased) {
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
}

@Composable
private fun ModifierSummaryCard(
    modifier: Modifier = Modifier,
    summary: ModifierAggregation.EnhancedSummary,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Effect Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            // Active conditions count
            Text(
                text = "Active Conditions: ${summary.activeConditionsCount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )

            // Modifier aggregations
            if (summary.modifierAggregations.isEmpty()) {
                Text(
                    text = "No numeric modifiers to summarize (display-only).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (targetAgg in summary.modifierAggregations) {
                        // Format: "Target: value (source), value (source)"
                        val modifierText = targetAgg.sources.joinToString(", ") { source ->
                            "${source.sum} (${source.source})"
                        }
                        Text(
                            text = "${targetAgg.target}: $modifierText",
                            style = MaterialTheme.typography.bodyMedium,
                        )
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

@Composable
private fun SpellSlotsPlaceholderContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Spell Slots tracking will be implemented here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModifierBuilderPlaceholderContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Modifier Builder will be implemented here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConditionPresetsPlaceholderContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Condition Presets will be implemented here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Mode for adding effects.
 */
private enum class EffectMode {
    GENERIC,
    CONDITION,
    MODIFIER,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGenericEffectSheet(
    sheetState: androidx.compose.material3.SheetState,
    colorScheme: androidx.compose.material3.ColorScheme,
    currentRound: Int,
    onDismiss: () -> Unit,
    onAdd: (String, Int?, EffectColorId, String?) -> Unit,
    onAddModifier: (UserModifier) -> Unit,
) {
    // Mode selection state (default to Generic)
    var selectedMode by remember { mutableStateOf(EffectMode.GENERIC) }

    // Form state
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(EffectColorId.PRIMARY) }
    var duration by remember { mutableStateOf(1) }
    var isIndefinite by remember { mutableStateOf(false) }

    // Validation
    val isNameValid = name.isNotBlank()
    val isDurationValid = isIndefinite || duration >= 1
    val canAdd = isNameValid && isDurationValid

    // Modifier builder sheet state
    var showModifierBuilder by remember { mutableStateOf(false) }
    val modifierBuilderSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            // Mode selector (Generic vs Condition vs Modifier)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SegmentedButton(
                    selected = selectedMode == EffectMode.GENERIC,
                    onClick = {
                        selectedMode = EffectMode.GENERIC
                        // Clear auto-fill when switching back to Generic mode
                        name = ""
                        notes = ""
                        selectedColor = EffectColorId.PRIMARY
                        duration = 1
                        isIndefinite = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                ) {
                    Text("Generic")
                }
                SegmentedButton(
                    selected = selectedMode == EffectMode.CONDITION,
                    onClick = {
                        selectedMode = EffectMode.CONDITION
                        // Clear auto-fill when switching modes
                        name = ""
                        notes = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                ) {
                    Text("Condition")
                }
                SegmentedButton(
                    selected = selectedMode == EffectMode.MODIFIER,
                    onClick = {
                        selectedMode = EffectMode.MODIFIER
                        // Clear auto-fill when switching modes
                        name = ""
                        notes = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                ) {
                    Text("Modifier")
                }
            }

            Text(
                text = when (selectedMode) {
                    EffectMode.GENERIC -> "Add Generic Effect"
                    EffectMode.CONDITION -> "Add Condition"
                    EffectMode.MODIFIER -> "Add Modifier"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            // Condition mode: Condition selector (PF1 only)
            if (selectedMode == EffectMode.CONDITION) {
                ConditionModeContent(
                    onConditionSelected = { condition: ConditionDefinition ->
                        // Auto-fill fields from condition (user may override)
                        name = condition.name
                        notes = condition.shortDescription
                        selectedColor = condition.defaultColorId
                        if (condition.defaultDuration != null) {
                            duration = condition.defaultDuration
                            isIndefinite = false
                        } else {
                            isIndefinite = true
                        }
                    },
                )
                HorizontalDivider()
            }

            // Modifier mode: Modifier selector (PF1 only, multiple selection)
            if (selectedMode == EffectMode.MODIFIER) {
                ModifierModeContent(
                    onModifiersSelected = { selectedModifiers ->
                        // Auto-fill fields from selected modifiers
                        if (selectedModifiers.isNotEmpty()) {
                            val modifierNames = selectedModifiers.joinToString(", ") { it.name }
                            name = modifierNames
                            
                            // Create summary of effects
                            val effectsSummary = selectedModifiers.flatMap { modifier ->
                                modifier.effects.map { effect ->
                                    "${effect.target}: ${effect.valueOrDescription}"
                                }
                            }.joinToString("; ")
                            notes = effectsSummary
                        } else {
                            name = ""
                            notes = ""
                        }
                    },
                )
                HorizontalDivider()
            }

            // Name field (required) - shown for Generic mode, pre-filled for Condition mode
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

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Add Modifier button (always visible)
                FilledTonalButton(
                    onClick = { showModifierBuilder = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add Modifier")
                }

                // Add Effect button
                Button(
                    onClick = {
                        val finalName = name.trim()
                        val finalNotes = notes.takeIf { it.isNotBlank() }?.trim()
                        val finalDuration = if (isIndefinite) null else duration
                        onAdd(finalName, finalDuration, selectedColor, finalNotes)
                    },
                    enabled = canAdd,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add Effect")
                }
            }
        }
    }

    // Modifier Builder Sheet
    if (showModifierBuilder) {
        ModifierBuilderSheet(
            sheetState = modifierBuilderSheetState,
            colorScheme = colorScheme,
            currentRound = currentRound,
            onDismiss = { showModifierBuilder = false },
            onBuild = { userModifier ->
                // Convert UserModifier to GenericEffect and add it
                onAddModifier(userModifier)
                showModifierBuilder = false
            },
        )
    }
}

/**
 * Content for Condition mode: System selector and Condition list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionModeContent(
    onConditionSelected: (ConditionDefinition) -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { Pf1ConditionRepository(context) }
    
    // Load PF1 conditions once (repository caches internally, so this is safe)
    val pf1Conditions = remember {
        repository.getConditionsBySystem(GameSystem.PF1)
    }
    
    // Selected condition state
    var selectedCondition by remember { mutableStateOf<ConditionDefinition?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    // Condition selector dropdown
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Condition",
            style = MaterialTheme.typography.labelLarge,
        )
        if (pf1Conditions.isEmpty()) {
            Text(
                text = "No PF1 conditions available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = selectedCondition?.name ?: "Select Condition",
                    modifier = Modifier.weight(1f),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                pf1Conditions.forEach { condition ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = condition.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = condition.shortDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                        },
                        onClick = {
                            selectedCondition = condition
                            expanded = false
                            onConditionSelected(condition)
                        },
                    )
                }
            }
        }
    }
}

/**
 * List item for a condition definition.
 */
@Composable
private fun ConditionListItem(
    condition: ConditionDefinition,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Condition name
            Text(
                text = condition.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            // Short description as subtitle
            Text(
                text = condition.shortDescription,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/**
 * Content for Modifier mode: Modifier list with multiple selection.
 */
@Composable
private fun ModifierModeContent(
    onModifiersSelected: (List<ModifierDefinition>) -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { Pf1ConditionRepository(context) }
    
    // Load PF1 modifiers once (repository caches internally, so this is safe)
    val pf1Modifiers = remember {
        repository.getModifiersBySystem(GameSystem.PF1)
    }
    
    // Selected modifiers state (multiple selection)
    var selectedModifiers by remember { mutableStateOf<Set<ModifierDefinition>>(emptySet()) }
    
    // Update parent when selection changes
    LaunchedEffect(selectedModifiers) {
        onModifiersSelected(selectedModifiers.toList())
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Modifiers",
            style = MaterialTheme.typography.labelLarge,
        )
        if (pf1Modifiers.isEmpty()) {
            Text(
                text = "No PF1 modifiers available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Scrollable list of modifiers with checkboxes
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(
                    items = pf1Modifiers,
                    key = { it.id },
                ) { modifier ->
                    ModifierListItem(
                        modifier = modifier,
                        isSelected = selectedModifiers.contains(modifier),
                        onClick = {
                            selectedModifiers = if (selectedModifiers.contains(modifier)) {
                                selectedModifiers - modifier
                            } else {
                                selectedModifiers + modifier
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * List item for a modifier definition with checkbox.
 */
@Composable
private fun ModifierListItem(
    modifier: ModifierDefinition,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // Create effects summary (e.g., "+4 STR, -2 AC")
    val effectsSummary = modifier.effects.joinToString(", ") { effect ->
        "${effect.valueOrDescription} ${effect.target}"
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Modifier name
                Text(
                    text = modifier.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                // Effects summary
                if (effectsSummary.isNotEmpty()) {
                    Text(
                        text = effectsSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet for building user-defined modifiers.
 * 
 * Allows users to create custom modifiers with:
 * - Modifier type selection
 * - Sign (+ or -)
 * - Numeric value or free text
 * - Duration
 * - Color override
 * - Optional notes
 * 
 * On confirm, creates a UserModifier instance and passes it to the callback.
 * The caller is responsible for applying the modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierBuilderSheet(
    sheetState: androidx.compose.material3.SheetState,
    colorScheme: androidx.compose.material3.ColorScheme,
    currentRound: Int,
    onDismiss: () -> Unit,
    onBuild: (UserModifier) -> Unit,
) {
    // Form state
    var selectedModifierType by remember { mutableStateOf(ModifierType.OTHER) }
    var isPositive by remember { mutableStateOf(true) }
    var valueText by remember { mutableStateOf("") }
    var freeText by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(1) }
    var isIndefinite by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<EffectColorId?>(null) }
    var notes by remember { mutableStateOf("") }

    // Validation: at least one of value or freeText must be provided
    val value = valueText.toIntOrNull()
    val hasValue = value != null && value > 0
    val hasFreeText = freeText.isNotBlank()
    val isValid = hasValue || hasFreeText
    val isDurationValid = isIndefinite || duration >= 1
    val canBuild = isValid && isDurationValid

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
                text = "Build Modifier",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            // Modifier Type dropdown
            var typeExpanded by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Modifier Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = selectedModifierType.getDisplayName(),
                        modifier = Modifier.weight(1f),
                    )
                }
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ModifierType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.getDisplayName()) },
                            onClick = {
                                selectedModifierType = type
                                typeExpanded = false
                                // Reset color to default for new type
                                selectedColor = null
                            },
                        )
                    }
                }
            }

            // Sign toggle (+ or -)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sign",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SegmentedButton(
                        selected = isPositive,
                        onClick = { isPositive = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) {
                        Text("+")
                    }
                    SegmentedButton(
                        selected = !isPositive,
                        onClick = { isPositive = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) {
                        Text("-")
                    }
                }
            }

            // Value field (numeric)
            OutlinedTextField(
                value = valueText,
                onValueChange = { newValue ->
                    // Only allow numeric input
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        valueText = newValue
                    }
                },
                label = { Text("Value (numeric)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                isError = !hasValue && !hasFreeText && (valueText.isNotEmpty() || freeText.isNotEmpty()),
                supportingText = if (!hasValue && !hasFreeText) {
                    {
                        if (valueText.isNotEmpty() || freeText.isNotEmpty()) {
                            Text("Enter a valid value or free text")
                        } else {
                            Text("Enter a value or free text")
                        }
                    }
                } else null,
            )

            // Free Text field (optional, for special modifiers)
            OutlinedTextField(
                value = freeText,
                onValueChange = { freeText = it },
                label = { Text("Free Text (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !hasValue && !hasFreeText && (valueText.isNotEmpty() || freeText.isNotEmpty()),
                supportingText = if (!hasValue && !hasFreeText) {
                    {
                        if (valueText.isNotEmpty() || freeText.isNotEmpty()) {
                            Text("Enter a valid value or free text")
                        } else {
                            Text("Enter a value or free text")
                        }
                    }
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
                        enabled = !isIndefinite,
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

            // Color picker (optional override)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    // Show current/default color indicator
                    val currentColor = selectedColor ?: selectedModifierType.defaultColorId()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(24.dp)
                                .height(24.dp),
                            color = currentColor.toColor(colorScheme),
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = colorScheme.outline.copy(alpha = 0.5f),
                            ),
                        ) {}
                        if (selectedColor != null) {
                            TextButton(onClick = { selectedColor = null }) {
                                Text("Reset to Default")
                            }
                        }
                    }
                }
                // Color palette grid
                val colors = EffectColorId.defaultPalette()
                val columns = 4
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in colors.chunked(columns)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (color in row) {
                                val isSelected = (selectedColor ?: selectedModifierType.defaultColorId()) == color
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
                                    } else {
                                        androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = colorScheme.outline.copy(alpha = 0.2f),
                                        )
                                    },
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

            // Build Modifier button
            Button(
                onClick = {
                    val finalValue = valueText.toIntOrNull()
                    val finalFreeText = freeText.takeIf { it.isNotBlank() }?.trim()
                    val finalDuration = if (isIndefinite) null else duration
                    val finalColor = selectedColor ?: selectedModifierType.defaultColorId()
                    val finalNotes = notes.takeIf { it.isNotBlank() }?.trim()

                    // Create UserModifier instance
                    // Note: UserModifier doesn't have a separate notes field,
                    // but notes can be included in freeText if needed
                    val combinedFreeText = when {
                        finalFreeText != null && finalNotes != null -> "$finalFreeText. $finalNotes"
                        finalFreeText != null -> finalFreeText
                        finalNotes != null -> finalNotes
                        else -> null
                    }

                    val userModifier = UserModifier(
                        id = UUID.randomUUID().toString(),
                        modifierType = selectedModifierType,
                        sign = if (isPositive) 1 else -1,
                        value = finalValue,
                        freeText = combinedFreeText,
                        durationRounds = finalDuration,
                        startRound = currentRound,
                        colorId = finalColor,
                    )

                    onBuild(userModifier)
                },
                enabled = canBuild,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Build Modifier")
            }
        }
    }
}

/**
 * Bottom sheet for editing modifier-based GenericEffects.
 * 
 * Supports editing:
 * - Value (for numeric modifiers)
 * - Free Text (for special modifiers)
 * - Duration
 * - Color
 * 
 * Works with both modifier-based effects (parsed from UserModifier) and regular generic effects.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModifierEffectSheet(
    sheetState: androidx.compose.material3.SheetState,
    colorScheme: androidx.compose.material3.ColorScheme,
    currentRound: Int,
    effect: GenericEffect,
    onDismiss: () -> Unit,
    onSave: (GenericEffect) -> Unit,
) {
    // Parse the effect to see if it's modifier-based
    val parsedModifier = parseModifierEffect(effect)
    val isModifierBased = parsedModifier != null

    // Form state - initialize from parsed modifier or effect
    var selectedModifierType by remember { 
        mutableStateOf(parsedModifier?.modifierType ?: ModifierType.OTHER) 
    }
    var isPositive by remember { 
        mutableStateOf(parsedModifier?.sign ?: 1 >= 0) 
    }
    var valueText by remember { 
        mutableStateOf(parsedModifier?.value?.toString() ?: "") 
    }
    var freeText by remember { 
        mutableStateOf(parsedModifier?.freeText ?: "") 
    }
    var duration by remember { 
        mutableStateOf(effect.durationRounds ?: 1) 
    }
    var isIndefinite by remember { 
        mutableStateOf(effect.durationRounds == null) 
    }
    var selectedColor by remember { 
        mutableStateOf<EffectColorId?>(effect.colorId) 
    }
    var notes by remember { 
        mutableStateOf(
            if (isModifierBased) {
                // For modifier-based, notes might contain freeText, extract it
                parsedModifier?.freeText ?: ""
            } else {
                effect.notes ?: ""
            }
        )
    }

    // Validation: at least one of value or freeText must be provided (for modifier-based)
    // For regular effects, name is always required
    val value = valueText.toIntOrNull()
    val hasValue = value != null && value > 0
    val hasFreeText = freeText.isNotBlank()
    val isValid = if (isModifierBased) {
        hasValue || hasFreeText
    } else {
        effect.name.isNotBlank()
    }
    val isDurationValid = isIndefinite || duration >= 1
    val canSave = isValid && isDurationValid

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
                text = if (isModifierBased) "Edit Modifier" else "Edit Effect",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            if (isModifierBased) {
                // Modifier Type dropdown (read-only for existing effects)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Modifier Type",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    OutlinedButton(
                        onClick = { /* Read-only */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                    ) {
                        Text(
                            text = selectedModifierType.getDisplayName(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Sign toggle (+ or -)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sign",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SegmentedButton(
                            selected = isPositive,
                            onClick = { isPositive = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        ) {
                            Text("+")
                        }
                        SegmentedButton(
                            selected = !isPositive,
                            onClick = { isPositive = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        ) {
                            Text("-")
                        }
                    }
                }

                // Value field (numeric)
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { newValue ->
                        // Only allow numeric input
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            valueText = newValue
                        }
                    },
                    label = { Text("Value (numeric)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    supportingText = if (!hasValue && !hasFreeText) {
                        { Text("Enter a value or free text") }
                    } else null,
                )

                // Free Text field (optional, for special modifiers)
                OutlinedTextField(
                    value = freeText,
                    onValueChange = { freeText = it },
                    label = { Text("Free Text (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = if (!hasValue && !hasFreeText) {
                        { Text("Enter a value or free text") }
                    } else null,
                )
            } else {
                // Regular generic effect: show name and notes fields
                OutlinedTextField(
                    value = effect.name,
                    onValueChange = { /* Read-only for now */ },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = false,
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )
            }

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
                        enabled = !isIndefinite,
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

            // Color picker (optional override)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (isModifierBased && selectedColor != null && selectedColor != selectedModifierType.defaultColorId()) {
                        TextButton(onClick = { selectedColor = selectedModifierType.defaultColorId() }) {
                            Text("Reset to Default")
                        }
                    }
                }
                // Color palette grid
                val colors = EffectColorId.defaultPalette()
                val columns = 4
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in colors.chunked(columns)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            for (color in row) {
                                val defaultColor = if (isModifierBased) {
                                    selectedModifierType.defaultColorId()
                                } else {
                                    effect.colorId
                                }
                                val isSelected = (selectedColor ?: defaultColor) == color
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

            // Save button
            Button(
                onClick = {
                    val finalColor = selectedColor ?: if (isModifierBased) {
                        selectedModifierType.defaultColorId()
                    } else {
                        effect.colorId
                    }
                    val finalDuration = if (isIndefinite) null else duration

                    val updatedEffect = if (isModifierBased) {
                        // Reconstruct name and notes from modifier fields
                        val finalValue = valueText.toIntOrNull()
                        val finalFreeText = freeText.takeIf { it.isNotBlank() }?.trim()
                        val combinedFreeText = when {
                            finalFreeText != null && notes.isNotBlank() -> "$finalFreeText. ${notes.trim()}"
                            finalFreeText != null -> finalFreeText
                            notes.isNotBlank() -> notes.trim()
                            else -> null
                        }

                        // Reconstruct name using UserModifier pattern
                        val displayName = when {
                            finalValue != null -> {
                                val modifierValue = (if (isPositive) 1 else -1) * finalValue
                                val signStr = if (modifierValue >= 0) "+" else ""
                                "${selectedModifierType.getDisplayName()} $signStr$modifierValue"
                            }
                            finalFreeText != null -> "${selectedModifierType.getDisplayName()}: $finalFreeText"
                            else -> selectedModifierType.getDisplayName()
                        }

                        // Reconstruct notes using UserModifier pattern
                        val displayNotes = when {
                            finalValue != null && combinedFreeText != null -> {
                                val modifierValue = (if (isPositive) 1 else -1) * finalValue
                                val signStr = if (modifierValue >= 0) "+" else ""
                                "${selectedModifierType.getDisplayName()} $signStr$modifierValue. $combinedFreeText"
                            }
                            finalValue != null -> {
                                val modifierValue = (if (isPositive) 1 else -1) * finalValue
                                val signStr = if (modifierValue >= 0) "+" else ""
                                "${selectedModifierType.getDisplayName()} $signStr$modifierValue"
                            }
                            combinedFreeText != null -> "${selectedModifierType.getDisplayName()}: $combinedFreeText"
                            else -> selectedModifierType.getDisplayName()
                        }

                        effect.copy(
                            name = displayName,
                            notes = displayNotes.takeIf { it != displayName },
                            colorId = finalColor,
                            durationRounds = finalDuration,
                        )
                    } else {
                        // Regular generic effect - just update editable fields
                        effect.copy(
                            notes = notes.takeIf { it.isNotBlank() }?.trim(),
                            colorId = finalColor,
                            durationRounds = finalDuration,
                        )
                    }

                    onSave(updatedEffect)
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Changes")
            }
        }
    }
}
