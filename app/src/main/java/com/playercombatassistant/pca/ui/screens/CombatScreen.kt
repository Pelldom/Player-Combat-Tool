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
import androidx.compose.material3.HorizontalDivider
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
import com.playercombatassistant.pca.effects.Pf1SpellEffectRepository
import com.playercombatassistant.pca.effects.SpellEffectDefinition
import com.playercombatassistant.pca.effects.Pf1FeatAbilityRepository
import com.playercombatassistant.pca.effects.FeatAbilityDefinition
import com.playercombatassistant.pca.effects.PinnedEffectsViewModel
import com.playercombatassistant.pca.effects.ConditionDefinition
import com.playercombatassistant.pca.effects.ModifierTarget
import com.playercombatassistant.pca.spells.SpellcastingSourceViewModel
import com.playercombatassistant.pca.effects.getDisplayLabel
import com.playercombatassistant.pca.improvised.ImprovisedItem
import com.playercombatassistant.pca.modifiers.ModifierRepository
import com.playercombatassistant.pca.modifiers.ModifierType
import com.playercombatassistant.pca.improvised.Handedness
import com.playercombatassistant.pca.improvised.ImprovisedWeaponResult
import com.playercombatassistant.pca.improvised.ImprovisedWeaponViewModel
import com.playercombatassistant.pca.improvised.LocationTable
import com.playercombatassistant.pca.improvised.Rarity
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin
import com.playercombatassistant.pca.history.CombatHistoryStore
import com.playercombatassistant.pca.history.CombatHistoryEvent
import com.playercombatassistant.pca.settings.SettingsViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
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
    val context = LocalContext.current
    val historyStore = remember { CombatHistoryStore(context) }
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val widthClass = LocalWindowSizeClass.current?.widthSizeClass ?: WindowWidthSizeClass.Compact

    val availableTables by improvisedWeaponViewModel.availableTables.collectAsStateWithLifecycle()
    val currentLocation by improvisedWeaponViewModel.currentLocation.collectAsStateWithLifecycle()
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
                // Set round to 1 without processing effects (no duration decrement)
                effectsViewModel.setCurrentRound(1)
            },
            onNextRound = {
                val nextRound = state.round + 1
                viewModel.nextRound()
                // Update effects view model when round advances (nextRound increments by 1)
                val expiredResult = effectsViewModel.processNextRound(nextRound)
                // Record expired effects in history
                val sessionId = state.sessionId
                if (sessionId != null) {
                    val now = System.currentTimeMillis()
                    coroutineScope.launch {
                        for (expired in expiredResult.expiredEffects) {
                            historyStore.recordEvent(
                                sessionId = sessionId,
                                event = CombatHistoryEvent.EffectExpired(
                                    timestampMillis = now,
                                    round = nextRound,
                                    effect = expired,
                                ),
                                startedAtMillisIfNew = now,
                            )
                        }
                        for (expired in expiredResult.expiredGenericEffects) {
                            // Convert GenericEffect to Effect for history
                            val effect = Effect(
                                id = expired.id,
                                name = expired.name,
                                system = GameSystem.GENERIC,
                                description = expired.notes ?: "",
                                remainingRounds = expired.remainingRounds,
                                type = EffectType.TIMER,
                                modifiers = emptyList(),
                                startRound = expired.startRound,
                                endRound = expired.endRound,
                                colorId = expired.colorId,
                            )
                            historyStore.recordEvent(
                                sessionId = sessionId,
                                event = CombatHistoryEvent.EffectExpired(
                                    timestampMillis = now,
                                    round = nextRound,
                                    effect = effect,
                                ),
                                startedAtMillisIfNew = now,
                            )
                        }
                    }
                }
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
            effectsViewModel = effectsViewModel,
            historyStore = historyStore,
            sessionId = state.sessionId,
            currentRound = state.round,
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
                // Set round to 1 without processing effects (no duration decrement)
                effectsViewModel.setCurrentRound(1)
            },
            onNextRound = {
                val nextRound = state.round + 1
                viewModel.nextRound()
                // Update effects view model when round advances (nextRound increments by 1)
                val expiredResult = effectsViewModel.processNextRound(nextRound)
                // Record expired effects in history
                val sessionId = state.sessionId
                if (sessionId != null) {
                    val now = System.currentTimeMillis()
                    coroutineScope.launch {
                        for (expired in expiredResult.expiredEffects) {
                            historyStore.recordEvent(
                                sessionId = sessionId,
                                event = CombatHistoryEvent.EffectExpired(
                                    timestampMillis = now,
                                    round = nextRound,
                                    effect = expired,
                                ),
                                startedAtMillisIfNew = now,
                            )
                        }
                        for (expired in expiredResult.expiredGenericEffects) {
                            // Convert GenericEffect to Effect for history
                            val effect = Effect(
                                id = expired.id,
                                name = expired.name,
                                system = GameSystem.GENERIC,
                                description = expired.notes ?: "",
                                remainingRounds = expired.remainingRounds,
                                type = EffectType.TIMER,
                                modifiers = emptyList(),
                                startRound = expired.startRound,
                                endRound = expired.endRound,
                                colorId = expired.colorId,
                            )
                            historyStore.recordEvent(
                                sessionId = sessionId,
                                event = CombatHistoryEvent.EffectExpired(
                                    timestampMillis = now,
                                    round = nextRound,
                                    effect = effect,
                                ),
                                startedAtMillisIfNew = now,
                            )
                        }
                    }
                }
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
            effectsViewModel = effectsViewModel,
            historyStore = historyStore,
            sessionId = state.sessionId,
            currentRound = state.round,
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
                // Set round to 1 without processing effects (no duration decrement)
                effectsViewModel.setCurrentRound(1)
            },
            onNextRound = {
                val nextRound = state.round + 1
                viewModel.nextRound()
                // Update effects view model when round advances (nextRound increments by 1)
                val expiredResult = effectsViewModel.processNextRound(nextRound)
                // Record expired effects in history
                val sessionId = state.sessionId
                if (sessionId != null) {
                    val now = System.currentTimeMillis()
                    coroutineScope.launch {
                        for (expired in expiredResult.expiredEffects) {
                            historyStore.recordEvent(
                                sessionId = sessionId,
                                event = CombatHistoryEvent.EffectExpired(
                                    timestampMillis = now,
                                    round = nextRound,
                                    effect = expired,
                                ),
                                startedAtMillisIfNew = now,
                            )
                        }
                        for (expired in expiredResult.expiredGenericEffects) {
                            // Convert GenericEffect to Effect for history
                            val effect = Effect(
                                id = expired.id,
                                name = expired.name,
                                system = GameSystem.GENERIC,
                                description = expired.notes ?: "",
                                remainingRounds = expired.remainingRounds,
                                type = EffectType.TIMER,
                                modifiers = emptyList(),
                                startRound = expired.startRound,
                                endRound = expired.endRound,
                                colorId = expired.colorId,
                            )
                            historyStore.recordEvent(
                                sessionId = sessionId,
                                event = CombatHistoryEvent.EffectExpired(
                                    timestampMillis = now,
                                    round = nextRound,
                                    effect = effect,
                                ),
                                startedAtMillisIfNew = now,
                            )
                        }
                    }
                }
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
            effectsViewModel = effectsViewModel,
            historyStore = historyStore,
            sessionId = state.sessionId,
            currentRound = state.round,
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
            historyStore = historyStore,
            sessionId = state.sessionId,
            inCombat = state.inCombat,
            onDismiss = { showAddEffectSheet = false },
            onAdd = { name, duration, colorId, notes, modifierType, modifierTarget, modifierValue ->
                val durationRounds = duration
                val currentRound = state.round
                effectsViewModel.addGenericEffect(
                    name = name,
                    notes = notes,
                    colorId = colorId,
                    durationRounds = durationRounds,
                    round = currentRound,
                    modifierType = modifierType,
                    modifierTarget = modifierTarget,
                    modifierValue = modifierValue,
                )
                // Record effect applied in history
                val sessionId = state.sessionId
                if (sessionId != null && state.inCombat) {
                    val now = System.currentTimeMillis()
                    coroutineScope.launch {
                        // Convert GenericEffect to Effect for history
                        val effect = Effect(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            system = GameSystem.GENERIC,
                            description = notes ?: "",
                            remainingRounds = durationRounds,
                            type = EffectType.TIMER,
                            modifiers = emptyList(),
                            startRound = currentRound,
                            endRound = durationRounds?.let { currentRound + it },
                            colorId = colorId,
                        )
                        historyStore.recordEvent(
                            sessionId = sessionId,
                            event = CombatHistoryEvent.EffectApplied(
                                timestampMillis = now,
                                round = currentRound,
                                effect = effect,
                            ),
                            startedAtMillisIfNew = now,
                        )
                    }
                }
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
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
    inCombat: Boolean,
    onAddGenericEffect: () -> Unit,
    gameSystem: GameSystem,
    effectsViewModel: EffectsViewModel,
    historyStore: CombatHistoryStore?,
    sessionId: String?,
    currentRound: Int,
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
                    effectsViewModel = effectsViewModel,
                )
            }
            SpellSlotTrackerContainer(
                modifier = phoneContainerModifier,
                historyStore = historyStore,
                sessionId = sessionId,
                currentRound = currentRound,
                inCombat = inCombat,
            )
            if (showModifierSummary) {
                ModifierSummaryCard(
                    modifier = phoneContainerModifier,
                    summary = modifierSummary,
                )
            }
        }

        // Right side (25% width) - RoundTrackerBar (fills available height)
        // Convert Effect objects to GenericEffect for display
        val allEffectsForTracker = remember(activeEffects, activeGenericEffects) {
            val convertedEffects = activeEffects.map { effect ->
                GenericEffect(
                    id = effect.id,
                    name = effect.name,
                    notes = effect.description.takeIf { it.isNotBlank() },
                    colorId = effect.colorId,
                    startRound = effect.startRound,
                    durationRounds = effect.remainingRounds,
                    endRound = effect.endRound,
                    remainingRounds = effect.remainingRounds,
                )
            }
            activeGenericEffects + convertedEffects
        }
        RoundTrackerBar(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight(),
            currentRound = round,
            effects = allEffectsForTracker,
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
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
    inCombat: Boolean,
    onAddGenericEffect: () -> Unit,
    gameSystem: GameSystem,
    effectsViewModel: EffectsViewModel,
    historyStore: CombatHistoryStore?,
    sessionId: String?,
    currentRound: Int,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // LEFT COLUMN: Combat Controls, Aggregated modifiers, Active Effects, Spell Slot Tracker, Improvised Weapons
        val leftScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(leftScrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val tabletContainerModifier = Modifier.fillMaxWidth()

            // Combat Controls (includes aggregated modifier display)
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
                modifier = tabletContainerModifier,
            )

            // Active Effects
            CollapsibleContainer(
                title = "Active Effects",
                stateKey = "combat_active_effects_tablet",
                modifier = tabletContainerModifier,
            ) {
                ActiveEffectsCard(
                    modifier = Modifier.fillMaxWidth(),
                    activeEffects = activeEffects,
                    activeGenericEffects = activeGenericEffects,
                    currentRound = round,
                    onAddGenericEffect = onAddGenericEffect,
                    effectsViewModel = effectsViewModel,
                )
            }

            // Spell Slot Tracker
            SpellSlotTrackerContainer(
                modifier = tabletContainerModifier,
                historyStore = historyStore,
                sessionId = sessionId,
                currentRound = currentRound,
                inCombat = inCombat,
            )

            // Improvised Weapons
            CollapsibleContainer(
                title = "Improvised Weapon",
                stateKey = "combat_improvised_weapon_tablet",
                modifier = tabletContainerModifier,
            ) {
            ImprovisedWeaponSection(
                    modifier = Modifier.fillMaxWidth(),
                collapsible = false,
                pinControls = true,
                currentLocationName = currentLocationName,
                lastWeaponResult = lastWeaponResult,
                weaponRollingDisabledMessage = weaponRollingDisabledMessage,
                availableTables = availableTables,
                onSelectLocation = onSelectLocation,
                onRollRandomLocation = onRollRandomLocation,
                onRollNewWeapon = onRollNewWeapon,
            )
            }
        }

        // RIGHT COLUMN: Combat Round Tracker only
        // Convert Effect objects to GenericEffect for display
        val allEffectsForTrackerTablet = remember(activeEffects, activeGenericEffects) {
            val convertedEffects = activeEffects.map { effect ->
                GenericEffect(
                    id = effect.id,
                    name = effect.name,
                    notes = effect.description.takeIf { it.isNotBlank() },
                    colorId = effect.colorId,
                    startRound = effect.startRound,
                    durationRounds = effect.remainingRounds,
                    endRound = effect.endRound,
                    remainingRounds = effect.remainingRounds,
                )
            }
            activeGenericEffects + convertedEffects
        }

        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight(),
        ) {
            RoundTrackerBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                currentRound = round,
                effects = allEffectsForTrackerTablet,
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
    lastWeaponResult: ImprovisedWeaponResult?,
    weaponRollingDisabledMessage: String?,
    availableTables: List<LocationTable>,
    onSelectLocation: (Int) -> Unit,
    onRollRandomLocation: () -> Unit,
    onRollNewWeapon: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top section: Controls - all buttons stacked vertically
            var showLocationPicker by remember { mutableStateOf(false) }
            val isDisabled = weaponRollingDisabledMessage != null

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 1) Location selector
                FilledTonalButton(
                    onClick = { showLocationPicker = true },
                    enabled = !isDisabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditLocationAlt,
                        contentDescription = "Select location",
                        modifier = Modifier.width(18.dp).height(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentLocationName ?: "Select Location",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                // 2) Random Location button
                OutlinedButton(
                    onClick = onRollRandomLocation,
                    enabled = !isDisabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Casino,
                        contentDescription = "Random location",
                        modifier = Modifier.width(18.dp).height(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Random")
                }

                // 3) Generate Weapon button (primary action)
                Button(
                    onClick = onRollNewWeapon,
                    enabled = !isDisabled && currentLocationName != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Generate Weapon")
                }
            }

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

            // Error message if disabled
        if (weaponRollingDisabledMessage != null) {
            Text(
                text = weaponRollingDisabledMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

            // Weapon result display
            if (lastWeaponResult != null) {
                val item = lastWeaponResult.item
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Weapon name (bold)
        Text(
                            text = item.name.ifBlank { "Improvised Weapon" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        // Damage and handedness
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (item.damage.isNotBlank()) {
            Text(
                                    text = "Damage: ${item.damage}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
            Text(
                                text = when (item.handedness) {
                                    Handedness.ONE_HANDED -> "One-handed"
                                    Handedness.TWO_HANDED -> "Two-handed"
                                    Handedness.VERSATILE -> "Versatile"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Description
                        if (item.description.isNotBlank()) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        // Notes if present
                        if (item.notes != null && item.notes.isNotBlank()) {
                            Text(
                                text = item.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                } else {
                // Placeholder when no weapon generated
                Text(
                    text = "No improvised weapon selected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
    effectsViewModel: EffectsViewModel,
) {
    var editingEffect: Effect? by remember { mutableStateOf(null) }
    var editingGenericEffect: GenericEffect? by remember { mutableStateOf(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Add Effect button (primary action) - full-width at top
            Button(
                onClick = onAddGenericEffect,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Effect")
            }

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
                        EffectListItem(
                            effect = effect,
                            onClick = { editingEffect = effect },
                        )
                    }
                    items(activeGenericEffects, key = { it.id }) { genericEffect ->
                        GenericEffectListItem(
                            genericEffect = genericEffect,
                            currentRound = currentRound,
                            onClick = { editingGenericEffect = genericEffect },
                        )
                    }
                }
            }
        }
    }

    // Edit dialogs
    editingEffect?.let { effect ->
        EditEffectDialog(
            effect = effect,
            onDismiss = { editingEffect = null },
            onSave = { remainingRounds ->
                effectsViewModel.updateEffect(effect.id, remainingRounds)
                editingEffect = null
            },
        )
    }

    editingGenericEffect?.let { genericEffect ->
        EditGenericEffectDialog(
            genericEffect = genericEffect,
            onDismiss = { editingGenericEffect = null },
            onSave = { name, notes, remainingRounds ->
                effectsViewModel.updateGenericEffect(genericEffect.id, name, notes, remainingRounds)
                editingGenericEffect = null
            },
        )
    }
}

@Composable
private fun EffectListItem(
    effect: Effect,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectTypeLabel = when (effect.type) {
        EffectType.CONDITION -> "Condition"
        EffectType.TIMER -> "Timer"
    }
    val roundsText = effect.remainingRounds?.let { "$it rounds remaining" } ?: "Indefinite duration"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
    onClick: () -> Unit,
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
            .clickable(onClick = onClick)
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
                    text = "No active modifiers.",
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
    historyStore: CombatHistoryStore? = null,
    sessionId: String? = null,
    inCombat: Boolean = false,
    onDismiss: () -> Unit,
    onAdd: (String, Int?, EffectColorId, String?, String?, ModifierTarget?, Int?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pinnedEffectsViewModel: PinnedEffectsViewModel = viewModel()
    val pinnedEffectIds by pinnedEffectsViewModel.pinnedEffectIds.collectAsStateWithLifecycle()
    val conditions = remember {
        try {
            Pf1ConditionRepository(context).getAllConditions()
                .sortedBy { it.name }
        } catch (e: Exception) {
            emptyList<ConditionDefinition>()
        }
    }
    
    // Load spell effects (repository already filters to PF1 only)
    val allSpellEffects = remember {
        try {
            Pf1SpellEffectRepository(context).getAllSpellEffects()
        } catch (e: Exception) {
            emptyList<SpellEffectDefinition>()
        }
    }
    
    // Load feats/abilities (repository already filters to PF1 only)
    val allFeatAbilities = remember {
        try {
            Pf1FeatAbilityRepository(context).getAllFeatAbilities()
        } catch (e: Exception) {
            emptyList<FeatAbilityDefinition>()
        }
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
    var spellEffectDuration by remember { mutableStateOf(1) }
    var spellEffectSearchText by remember { mutableStateOf("") }
    var selectedSpellLevel by remember { mutableStateOf<Int?>(null) }
    var featAbilitySearchText by remember { mutableStateOf("") }
    var featAbilityDuration by remember { mutableStateOf(1) }
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
            
            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Pinned section (only show if there are pinned effects)
            val pinnedConditions = remember(conditions, pinnedEffectIds) {
                conditions.filter { it.id in pinnedEffectIds }
            }
            val pinnedSpellEffects = remember(allSpellEffects, pinnedEffectIds) {
                allSpellEffects.filter { it.id in pinnedEffectIds }
            }
            val pinnedFeatAbilities = remember(allFeatAbilities, pinnedEffectIds) {
                allFeatAbilities.filter { it.id in pinnedEffectIds }
            }
            val hasPinnedEffects = pinnedConditions.isNotEmpty() || pinnedSpellEffects.isNotEmpty() || pinnedFeatAbilities.isNotEmpty()

            if (hasPinnedEffects) {
                Column(
                modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Pinned",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    
                    // Show pinned conditions
                    pinnedConditions.forEach { condition ->
                        PinnedEffectItem(
                            name = condition.name,
                            description = condition.shortDescription,
                            isPinned = true,
                            onTogglePin = { pinnedEffectsViewModel.togglePin(condition.id) },
                            onApply = {
                                val effectModifiers = condition.modifiers.map { modEntry ->
                                    com.playercombatassistant.pca.effects.Modifier(
                                        target = modEntry.target,
                                        value = modEntry.value,
                                        source = condition.name,
                                    )
                                }
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
                                    colorId = condition.defaultColorId ?: EffectColorId.PRIMARY,
                                )
                                effectsViewModel.addEffect(currentRound, conditionEffect)
                                // Record history
                                if (inCombat && sessionId != null && historyStore != null) {
                                    val now = System.currentTimeMillis()
                                    coroutineScope.launch {
                                        historyStore.recordEvent(
                                            sessionId = sessionId,
                                            event = CombatHistoryEvent.EffectApplied(
                                                timestampMillis = now,
                                                round = currentRound,
                                                effect = conditionEffect,
                                            ),
                                            startedAtMillisIfNew = now,
                                        )
                                    }
                                }
                                onDismiss()
                            },
                        )
                    }
                    
                    // Show pinned spell effects
                    pinnedSpellEffects.forEach { spellEffect ->
                        PinnedEffectItem(
                            name = spellEffect.name,
                            description = "Level ${spellEffect.spellLevel} - ${spellEffect.description}",
                            isPinned = true,
                            onTogglePin = { pinnedEffectsViewModel.togglePin(spellEffect.id) },
                            onApply = {
                                val effectModifiers = spellEffect.modifiers.map { modEntry ->
                                    com.playercombatassistant.pca.effects.Modifier(
                                        target = modEntry.modifierTarget,
                                        value = modEntry.modifierValue.toString(),
                                        source = spellEffect.name,
                                    )
                                }
                                    val spellEffectInstance = Effect(
                                        id = UUID.randomUUID().toString(),
                                        name = spellEffect.name,
                                        system = spellEffect.system,
                                        description = spellEffect.description,
                                        remainingRounds = spellEffectDuration,
                                        type = EffectType.CONDITION,
                                        modifiers = effectModifiers,
                                        startRound = currentRound,
                                        endRound = currentRound + spellEffectDuration - 1,
                                        colorId = spellEffect.defaultColorId ?: EffectColorId.PRIMARY,
                                    )
                                effectsViewModel.addEffect(currentRound, spellEffectInstance)
                                onDismiss()
                            },
                        )
                    }
                    
                    // Show pinned feats/abilities
                    pinnedFeatAbilities.forEach { featAbility ->
                        val isPassive = featAbility.defaultDuration == 0
                        PinnedEffectItem(
                            name = featAbility.name,
                            description = if (isPassive) {
                                "Passive - ${featAbility.description}"
                            } else {
                                "Timed - ${featAbility.description}"
                            },
                            isPinned = true,
                            onTogglePin = { pinnedEffectsViewModel.togglePin(featAbility.id) },
                            onApply = {
                                val effectModifiers = featAbility.modifiers.map { modEntry ->
                                    com.playercombatassistant.pca.effects.Modifier(
                                        target = modEntry.modifierTarget,
                                        value = modEntry.modifierValue.toString(),
                                        source = featAbility.name,
                                    )
                                }
                                val finalDuration = if (isPassive) 0 else featAbilityDuration
                                    val featAbilityInstance = Effect(
                                        id = UUID.randomUUID().toString(),
                                        name = featAbility.name,
                                        system = featAbility.system,
                                        description = featAbility.description,
                                        remainingRounds = if (isPassive) null else finalDuration,
                                        type = EffectType.CONDITION,
                                        modifiers = effectModifiers,
                                        startRound = currentRound,
                                        endRound = if (isPassive) null else (currentRound + finalDuration - 1),
                                        colorId = featAbility.defaultColorId ?: EffectColorId.PRIMARY,
                                    )
                                effectsViewModel.addEffect(currentRound, featAbilityInstance)
                                onDismiss()
                            },
                        )
                    }
                }
                
                // Divider
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
                            modifier = Modifier
                                .width(56.dp)
                                .height(48.dp),
                        ) {
                            Text("-")
                        }
                        Text(
                            text = if (modifierValue >= 0) "+$modifierValue" else modifierValue.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )
                        OutlinedButton(
                            onClick = { modifierValue = modifierValue + 1 },
                            modifier = Modifier
                                .width(56.dp)
                                .height(48.dp),
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
                        modifier = Modifier
                            .width(56.dp)
                            .height(48.dp),
                    ) {
                        Text("-")
                    }
                    Text(
                        text = if (isIndefinite) "Indefinite" else duration.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = { duration = duration + 1 },
                        modifier = Modifier
                            .width(56.dp)
                            .height(48.dp),
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
                        .fillMaxWidth(),
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
                        val isPinned = condition.id in pinnedEffectIds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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
                                    colorId = condition.defaultColorId ?: EffectColorId.PRIMARY,
                                )
                                
                                // Add as Effect (not GenericEffect) to preserve modifiers
                                effectsViewModel.addEffect(currentRound, conditionEffect)
                                // Record history
                                if (inCombat && sessionId != null && historyStore != null) {
                                    val now = System.currentTimeMillis()
                                    coroutineScope.launch {
                                        historyStore.recordEvent(
                                            sessionId = sessionId,
                                            event = CombatHistoryEvent.EffectApplied(
                                                timestampMillis = now,
                                                round = currentRound,
                                                effect = conditionEffect,
                                            ),
                                            startedAtMillisIfNew = now,
                                        )
                                    }
                                }
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(condition.name)
                        }
                            IconButton(
                                onClick = { pinnedEffectsViewModel.togglePin(condition.id) },
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isPinned) "Unpin" else "Pin",
                                    tint = if (isPinned) colorScheme.primary else colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else if (selectedTabIndex == 2) {
                // Spell Effects tab
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Duration selector
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
                            onClick = { spellEffectDuration = (spellEffectDuration - 1).coerceAtLeast(1) },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("-")
                        }
            Text(
                            text = spellEffectDuration.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
            OutlinedButton(
                            onClick = { spellEffectDuration = spellEffectDuration + 1 },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("+")
                        }
                    }
                    
                    // Search field
                    OutlinedTextField(
                        value = spellEffectSearchText,
                        onValueChange = { spellEffectSearchText = it },
                        label = { Text("Search spell name") },
                modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    
                    // Spell level filter
                    var spellLevelDropdownExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                            text = "Filter by level:",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        OutlinedButton(
                            onClick = { spellLevelDropdownExpanded = true },
                    modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = selectedSpellLevel?.let { "Level $it" } ?: "All Levels",
                                maxLines = 1,
                )
            }
            DropdownMenu(
                            expanded = spellLevelDropdownExpanded,
                            onDismissRequest = { spellLevelDropdownExpanded = false },
            ) {
                    DropdownMenuItem(
                                text = { Text("All Levels") },
                                onClick = {
                                    selectedSpellLevel = null
                                    spellLevelDropdownExpanded = false
                                },
                            )
                            // Get unique spell levels from all spell effects
                            val uniqueLevels = allSpellEffects.map { it.spellLevel }.distinct().sorted()
                            for (level in uniqueLevels) {
                                DropdownMenuItem(
                                    text = { Text("Level $level") },
                                    onClick = {
                                        selectedSpellLevel = level
                                        spellLevelDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    
                    // Filter and search spell effects
                    val filteredSpellEffects = remember(allSpellEffects, spellEffectSearchText, selectedSpellLevel) {
                        allSpellEffects
                            .filter { spell ->
                                // Filter by search text
                                val matchesSearch = spellEffectSearchText.isBlank() || 
                                    spell.name.contains(spellEffectSearchText, ignoreCase = true)
                                
                                // Filter by spell level
                                val matchesLevel = selectedSpellLevel == null || spell.spellLevel == selectedSpellLevel
                                
                                matchesSearch && matchesLevel
                            }
                            .sortedBy { it.name }
                    }
                    
                    // Spell effects list
                    filteredSpellEffects.forEach { spellEffect ->
                        val isPinned = spellEffect.id in pinnedEffectIds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Convert spell effect modifiers to Effect.Modifier format
                                    val effectModifiers = spellEffect.modifiers.map { modEntry ->
                                        com.playercombatassistant.pca.effects.Modifier(
                                            target = modEntry.modifierTarget,
                                            value = modEntry.modifierValue.toString(),
                                            source = spellEffect.name,
                                        )
                                    }
                                    
                                    // Create an Effect object for the spell effect (not GenericEffect)
                                    // This preserves all modifiers from the spell effect
                                    val spellEffectInstance = Effect(
                                        id = UUID.randomUUID().toString(),
                                        name = spellEffect.name,
                                        system = spellEffect.system,
                                        description = spellEffect.description,
                                        remainingRounds = spellEffectDuration,
                                        type = EffectType.CONDITION, // Use CONDITION type for spell effects
                                        modifiers = effectModifiers,
                                        startRound = currentRound,
                                        endRound = currentRound + spellEffectDuration - 1,
                                        colorId = spellEffect.defaultColorId,
                                    )
                                    
                                    // Add as Effect (not GenericEffect) to preserve modifiers
                                    effectsViewModel.addEffect(currentRound, spellEffectInstance)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                ) {
                                Text(
                                        text = spellEffect.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                        text = "Level ${spellEffect.spellLevel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { pinnedEffectsViewModel.togglePin(spellEffect.id) },
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isPinned) "Unpin" else "Pin",
                                    tint = if (isPinned) colorScheme.primary else colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    
                    if (filteredSpellEffects.isEmpty()) {
                        Text(
                            text = if (spellEffectSearchText.isNotBlank() || selectedSpellLevel != null) {
                                "No spell effects found matching your filters."
                            } else {
                                "No spell effects available."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            } else {
                // Feats & Abilities tab
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Duration selector (only for timed abilities)
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
                            onClick = { featAbilityDuration = (featAbilityDuration - 1).coerceAtLeast(1) },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("-")
                        }
                        Text(
                            text = featAbilityDuration.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(
                            onClick = { featAbilityDuration = featAbilityDuration + 1 },
                            modifier = Modifier.width(56.dp),
                        ) {
                            Text("+")
                        }
                    }
                    
                    // Search field
                    OutlinedTextField(
                        value = featAbilitySearchText,
                        onValueChange = { featAbilitySearchText = it },
                        label = { Text("Search feat/ability name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    
                    // Filter and search feats/abilities
                    val filteredFeatAbilities = remember(allFeatAbilities, featAbilitySearchText) {
                        allFeatAbilities
                            .filter { feat ->
                                // Filter by search text
                                featAbilitySearchText.isBlank() || 
                                    feat.name.contains(featAbilitySearchText, ignoreCase = true)
                            }
                            .sortedBy { it.name }
                    }
                    
                    // Feats/abilities list
                    filteredFeatAbilities.forEach { featAbility ->
                        val isPassive = featAbility.defaultDuration == 0
                        val isPinned = featAbility.id in pinnedEffectIds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Convert feat/ability modifiers to Effect.Modifier format
                                    val effectModifiers = featAbility.modifiers.map { modEntry ->
                                        com.playercombatassistant.pca.effects.Modifier(
                                            target = modEntry.modifierTarget,
                                            value = modEntry.modifierValue.toString(),
                                            source = featAbility.name,
                                        )
                                    }
                                    
                                    // Determine duration: 0 for passive, user-selected for timed
                                    val finalDuration = if (isPassive) {
                                        0 // Passive effects don't decrement
        } else {
                                        featAbilityDuration
                                    }
                                    
                                    // Create an Effect object for the feat/ability (not GenericEffect)
                                    // This preserves all modifiers from the feat/ability
                                    val featAbilityInstance = Effect(
                                        id = UUID.randomUUID().toString(),
                                        name = featAbility.name,
                                        system = featAbility.system,
                                        description = featAbility.description,
                                        remainingRounds = if (isPassive) null else finalDuration, // null for passive (indefinite)
                                        type = EffectType.CONDITION, // Use CONDITION type for feats/abilities
                                        modifiers = effectModifiers,
                                        startRound = currentRound,
                                        endRound = if (isPassive) null else (currentRound + finalDuration - 1), // null for passive
                                        colorId = featAbility.defaultColorId,
                                    )
                                    
                                    // Add as Effect (not GenericEffect) to preserve modifiers
                                    effectsViewModel.addEffect(currentRound, featAbilityInstance)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
    ) {
        Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                ) {
            Text(
                                        text = featAbility.name,
                style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = if (isPassive) {
                                            "Passive (persists until End Combat)"
                } else {
                                            "Timed (${featAbility.defaultDuration} rounds default)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            IconButton(
                                onClick = { pinnedEffectsViewModel.togglePin(featAbility.id) },
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isPinned) "Unpin" else "Pin",
                                    tint = if (isPinned) colorScheme.primary else colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    
                    if (filteredFeatAbilities.isEmpty()) {
            Text(
                            text = if (featAbilitySearchText.isNotBlank()) {
                                "No feats/abilities found matching your search."
                } else {
                                "No feats/abilities available."
                },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
            )
                    }
                }
            }
            } // End of scrollable Column
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEffectDialog(
    effect: Effect,
    onDismiss: () -> Unit,
    onSave: (Int?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var remainingRoundsText by rememberSaveable { mutableStateOf(effect.remainingRounds?.toString() ?: "") }
    var isIndefinite by rememberSaveable { mutableStateOf(effect.remainingRounds == null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Edit Effect: ${effect.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            // Duration input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = remainingRoundsText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            remainingRoundsText = newValue
                            isIndefinite = false
                        }
                    },
                    label = { Text("Remaining Rounds") },
                    enabled = !isIndefinite,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        val current = remainingRoundsText.toIntOrNull() ?: 1
                        if (current > 1) {
                            remainingRoundsText = (current - 1).toString()
                        }
                    },
                    enabled = !isIndefinite && (remainingRoundsText.toIntOrNull() ?: 1) > 1,
                ) {
                    Text("-", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(
                    onClick = {
                        val current = remainingRoundsText.toIntOrNull() ?: 0
                        remainingRoundsText = (current + 1).toString()
                    },
                    enabled = !isIndefinite,
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Indefinite checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Checkbox(
                    checked = isIndefinite,
                    onCheckedChange = { checked ->
                        isIndefinite = checked
                        if (checked) {
                            remainingRoundsText = ""
                        } else {
                            remainingRoundsText = "1"
                        }
                    },
                )
            Text(
                    text = "Indefinite duration",
                    modifier = Modifier.clickable { isIndefinite = !isIndefinite },
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val rounds = if (isIndefinite) null else remainingRoundsText.toIntOrNull()?.coerceAtLeast(1)
                        onSave(rounds)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGenericEffectDialog(
    genericEffect: GenericEffect,
    onDismiss: () -> Unit,
    onSave: (String?, String?, Int?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nameText by rememberSaveable { mutableStateOf(genericEffect.name) }
    var notesText by rememberSaveable { mutableStateOf(genericEffect.notes ?: "") }
    var remainingRoundsText by rememberSaveable { mutableStateOf(genericEffect.remainingRounds?.toString() ?: "") }
    var isIndefinite by rememberSaveable { mutableStateOf(genericEffect.remainingRounds == null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Edit Effect",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            // Name input
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            // Notes input
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            // Duration input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = remainingRoundsText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            remainingRoundsText = newValue
                            isIndefinite = false
                        }
                    },
                    label = { Text("Remaining Rounds") },
                    enabled = !isIndefinite,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                        onClick = {
                        val current = remainingRoundsText.toIntOrNull() ?: 1
                        if (current > 1) {
                            remainingRoundsText = (current - 1).toString()
                        }
                    },
                    enabled = !isIndefinite && (remainingRoundsText.toIntOrNull() ?: 1) > 1,
                ) {
                    Text("-", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(
                    onClick = {
                        val current = remainingRoundsText.toIntOrNull() ?: 0
                        remainingRoundsText = (current + 1).toString()
                    },
                    enabled = !isIndefinite,
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Indefinite checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Checkbox(
                    checked = isIndefinite,
                    onCheckedChange = { checked ->
                        isIndefinite = checked
                        if (checked) {
                            remainingRoundsText = ""
                            } else {
                            remainingRoundsText = "1"
                        }
                    },
                )
                Text(
                    text = "Indefinite duration",
                    modifier = Modifier.clickable { isIndefinite = !isIndefinite },
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val rounds = if (isIndefinite) null else remainingRoundsText.toIntOrNull()?.coerceAtLeast(1)
                        onSave(
                            nameText.takeIf { it.isNotBlank() }, // Preserve original if blank
                            notesText.ifBlank { null }, // Allow clearing notes
                            rounds,
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun PinnedEffectItem(
    name: String,
    description: String,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    onApply: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onApply,
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(
            onClick = onTogglePin,
        ) {
            Icon(
                imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isPinned) "Unpin" else "Pin",
                tint = if (isPinned) colorScheme.primary else colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpellSlotTrackerContainer(
    modifier: Modifier = Modifier,
    historyStore: CombatHistoryStore? = null,
    sessionId: String? = null,
    currentRound: Int = 0,
    inCombat: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: SpellcastingSourceViewModel = viewModel()
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    
    val colorScheme = MaterialTheme.colorScheme
    
    CollapsibleContainer(
        title = "Spell Slots",
        stateKey = "combat_spell_slot_tracker",
        modifier = modifier,
    ) {
        if (sources.isEmpty()) {
            Text(
                text = "Configure spellcasting sources in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Display slots grouped by source
                sources.forEach { source ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Source name with color indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp),
                                color = source.color,
                                shape = MaterialTheme.shapes.small,
                            ) {}
                            Text(
                                text = source.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        
                        // Display slots for each level (0-9)
                        for (level in 0..9) {
                            val levelSlots = source.slotsByLevel[level] ?: emptyList()
                            if (levelSlots.isNotEmpty()) {
            Column(
                                    modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                                        text = "Level $level",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        levelSlots.forEachIndexed { index, isAvailable ->
                                            SpellSlotButton(
                                                isAvailable = isAvailable,
                                                onClick = {
                                                    // Record history before toggling
                                                    if (inCombat && sessionId != null && historyStore != null) {
                                                        val now = System.currentTimeMillis()
                                                        val event = if (isAvailable) {
                                                            CombatHistoryEvent.SpellSlotUsed(
                                                                timestampMillis = now,
                                                                round = currentRound,
                                                                sourceName = source.name,
                                                                level = level,
                                                            )
                                                        } else {
                                                            CombatHistoryEvent.SpellSlotRecovered(
                                                                timestampMillis = now,
                                                                round = currentRound,
                                                                sourceName = source.name,
                                                                level = level,
                                                            )
                                                        }
                                                        coroutineScope.launch {
                                                            historyStore.recordEvent(
                                                                sessionId = sessionId,
                                                                event = event,
                                                                startedAtMillisIfNew = now,
                                                            )
                                                        }
                                                    }
                                                    viewModel.toggleSlot(source.id, level, index)
                                                },
                                                modifier = Modifier.weight(1f),
                                                sourceColor = source.color,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Reset All Slots button
                Button(
                    onClick = { viewModel.resetAllSlots() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset All Slots")
                }
            }
        }
    }
}

@Composable
private fun SpellSlotButton(
    isAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sourceColor: androidx.compose.ui.graphics.Color,
) {
    val colorScheme = MaterialTheme.colorScheme
    
    androidx.compose.material3.FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isAvailable) {
                sourceColor.copy(alpha = 0.3f)
            } else {
                colorScheme.surfaceVariant
            },
            contentColor = if (isAvailable) {
                sourceColor
            } else {
                colorScheme.onSurfaceVariant
            },
        ),
    ) {
        Text(
            text = if (isAvailable) "●" else "○",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
