package com.playercombatassistant.pca.improvised

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playercombatassistant.pca.history.CombatHistoryEvent
import com.playercombatassistant.pca.history.CombatHistoryStore
import com.playercombatassistant.pca.history.ImprovisedWeaponRollOrigin
import com.playercombatassistant.pca.combat.WeaponLockMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for improvised-weapon table usage.
 *
 * Responsibilities:
 * - Holds the available [LocationTable] list in-memory (no persistence yet, no hardcoded tables here).
 * - Tracks the current location selection (manual or random d30).
 * - Generates [ImprovisedWeaponResult] using weighted d100 selection.
 *
 * Notes / future integration:
 * - **Combat locking (later)**:
 *   - In "locked" modes, you may restrict changing location/weapon while in combat, or lock changes to round
 *     boundaries. This ViewModel intentionally does not enforce any of that today.
 * - **Hybrid mode (later)**:
 *   - A hybrid flow can allow either: manual location + d100 weapon roll, or random d30 location + d100 weapon roll,
 *     depending on user preference/settings.
 * - **Effects integration (later)**:
 *   - Weapon rolls could optionally attach an Effect (e.g., "Improvised: Shard" with a short duration) or create
 *     display-only modifiers; any such behavior should be driven by data, not enforced rules.
 *
 * Design principles:
 * - No rule enforcement; the ViewModel only performs selection and exposes state for display.
 * - No combat assumptions; usable outside combat.
 * - Clean separation:
 *   - data models: `ImprovisedModels.kt`
 *   - logic: `LocationSelection.kt`, `ImprovisedWeaponSelection.kt`
 *   - UI: `CombatScreen.kt`
 */
class ImprovisedWeaponViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImprovisedWeaponRepository(application.applicationContext)

    private val _availableTables = MutableStateFlow<List<LocationTable>>(emptyList())
    val availableTables: StateFlow<List<LocationTable>> = _availableTables.asStateFlow()

    private val _weaponRollingDisabledMessage = MutableStateFlow<String?>(null)
    /**
     * If non-null, improvised weapon rolling is disabled and the UI should show this message.
     */
    val weaponRollingDisabledMessage: StateFlow<String?> = _weaponRollingDisabledMessage.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationTable?>(null)
    val currentLocation: StateFlow<LocationTable?> = _currentLocation.asStateFlow()

    private val _lastD30Roll = MutableStateFlow<Int?>(null)
    val lastD30Roll: StateFlow<Int?> = _lastD30Roll.asStateFlow()

    private val _lastWeaponResult = MutableStateFlow<ImprovisedWeaponResult?>(null)
    val lastWeaponResult: StateFlow<ImprovisedWeaponResult?> = _lastWeaponResult.asStateFlow()

    private val historyStore = CombatHistoryStore(application.applicationContext)
    private val historySessionId = UUID.randomUUID().toString()
    private val historySessionStartedAtMillis = System.currentTimeMillis()

    init {
        // Load + transform improvised tables at startup, with validation and clear failure messaging.
        when (val state = repository.load()) {
            is ImprovisedWeaponRepository.LoadState.Ready -> {
                _weaponRollingDisabledMessage.value = null
                setAvailableTables(state.tables)
            }

            is ImprovisedWeaponRepository.LoadState.Disabled -> {
                _weaponRollingDisabledMessage.value = state.userMessage
                setAvailableTables(emptyList())
                _currentLocation.value = null
                _lastD30Roll.value = null
                _lastWeaponResult.value = null
            }
        }
    }

    /**
     * In-memory table injection point (persistence later).
     *
     * This does not select a location automatically; callers can choose to do so via
     * [setLocationManually] or [rollRandomLocation].
     */
    fun setAvailableTables(tables: List<LocationTable>) {
        _availableTables.value = tables
        // If the current location no longer exists, clear it (no UI assumptions).
        _currentLocation.update { current ->
            val stillValid = current?.let { c -> tables.any { it.id == c.id } } ?: false
            if (stillValid) current else null
        }
    }

    /**
     * Manual location selection by id.
     *
     * Rules:
     * - Wrap safely if the id does not exist (handled by [LocationSelector]).
     * - Clears [lastD30Roll] because this is not a roll.
     */
    fun setLocationManually(id: Int) {
        val tables = _availableTables.value
        if (tables.isEmpty()) {
            _currentLocation.value = null
            _lastD30Roll.value = null
            return
        }

        val selector = LocationSelector(tables)
        _currentLocation.value = selector.selectLocation(id)
        _lastD30Roll.value = null
    }

    /**
     * Random location selection via a visible d30 roll.
     *
     * Rules:
     * - d30 roll is always exposed in [lastD30Roll].
     * - If a location id does not exist, wraps safely (handled by [LocationSelector]).
     */
    fun rollRandomLocation() {
        val tables = _availableTables.value
        if (tables.isEmpty()) {
            _currentLocation.value = null
            _lastD30Roll.value = null
            return
        }

        val selector = LocationSelector(tables)
        val (roll, table) = selector.rollRandomLocation()
        _currentLocation.value = table
        _lastD30Roll.value = roll
    }

    /**
     * Rolls a new improvised weapon for the current location (if available).
     *
     * Rules:
     * - Uses weighted d100 selection (see [ImprovisedWeaponSelection]).
     * - If no location is selected, attempts to resolve location id 1 (wrapped) from available tables.
     * - If no tables exist, leaves [lastWeaponResult] as null.
     */
    fun rollNewWeapon(): ImprovisedWeaponResult? {
        if (_weaponRollingDisabledMessage.value != null) {
            _lastWeaponResult.value = null
            return null
        }

        val table = _currentLocation.value
            ?: LocationSelector(_availableTables.value).selectLocation(1)
            ?: run {
                _lastWeaponResult.value = null
                return null
            }

        _currentLocation.value = table
        val result = ImprovisedWeaponSelection.rollAndSelect(table)
        _lastWeaponResult.value = result

        // Append-only history entry (no editing/deletion).
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            historyStore.recordEvent(
                sessionId = historySessionId,
                event = CombatHistoryEvent.ImprovisedWeaponRolled(
                    timestampMillis = now,
                    round = null, // not tied to combat rounds yet
                    combatId = null, // out-of-combat roll
                    lockMode = null,
                    origin = ImprovisedWeaponRollOrigin.AUTOMATIC,
                    locationId = result.locationId,
                    locationName = result.locationName,
                    d30Roll = _lastD30Roll.value,
                    d100Roll = result.d100Roll,
                    item = result.item,
                ),
                startedAtMillisIfNew = historySessionStartedAtMillis,
            )
        }

        return result
    }

    /**
     * Combat-aware roll that records into the combat's history session with round + lock metadata.
     *
     * Used to keep in-combat history entries grouped with the combat session, while leaving
     * out-of-combat rolls unchanged (still stored under this ViewModel's own session).
     */
    fun rollNewWeaponInCombat(
        combatId: String,
        round: Int,
        origin: ImprovisedWeaponRollOrigin,
    ): ImprovisedWeaponResult? {
        if (_weaponRollingDisabledMessage.value != null) {
            _lastWeaponResult.value = null
            return null
        }

        val table = _currentLocation.value
            ?: LocationSelector(_availableTables.value).selectLocation(1)
            ?: run {
                _lastWeaponResult.value = null
                return null
            }

        _currentLocation.value = table
        val result = ImprovisedWeaponSelection.rollAndSelect(table)
        _lastWeaponResult.value = result

        val now = System.currentTimeMillis()
        viewModelScope.launch {
            historyStore.recordEvent(
                sessionId = combatId,
                event = CombatHistoryEvent.ImprovisedWeaponRolled(
                    timestampMillis = now,
                    round = round,
                    combatId = combatId,
                    lockMode = null,
                    origin = origin,
                    locationId = result.locationId,
                    locationName = result.locationName,
                    d30Roll = _lastD30Roll.value,
                    d100Roll = result.d100Roll,
                    item = result.item,
                ),
                startedAtMillisIfNew = now,
            )
        }

        return result
    }
}

