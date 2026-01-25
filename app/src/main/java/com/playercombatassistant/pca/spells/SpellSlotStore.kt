package com.playercombatassistant.pca.spells

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DATASTORE_NAME = "pca_spell_slots"
private val Context.spellSlotDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private val KEY_MAX_SPELL_LEVEL = intPreferencesKey("max_spell_level")
private val KEY_SLOTS_PER_LEVEL = intPreferencesKey("slots_per_level")
private val KEY_SLOT_STATES_JSON = stringPreferencesKey("slot_states_json")

/**
 * Data model for spell slot states.
 * Maps spell level to a list of boolean values (true = available, false = used).
 */
@Serializable
data class SpellSlotStates(
    val slots: Map<Int, List<Boolean>> = emptyMap(),
)

/**
 * Store for managing spell slot configuration and states.
 */
class SpellSlotStore(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Flow of maximum spell level configuration.
     */
    val maxSpellLevel: Flow<Int> =
        context.spellSlotDataStore.data.map { prefs ->
            prefs[KEY_MAX_SPELL_LEVEL] ?: 9
        }

    /**
     * Flow of slots per level configuration.
     */
    val slotsPerLevel: Flow<Int> =
        context.spellSlotDataStore.data.map { prefs ->
            prefs[KEY_SLOTS_PER_LEVEL] ?: 0
        }

    /**
     * Flow of spell slot states.
     */
    val slotStates: Flow<SpellSlotStates> =
        context.spellSlotDataStore.data.map { prefs ->
            val raw = prefs[KEY_SLOT_STATES_JSON]
            if (raw != null) {
                runCatching { json.decodeFromString<SpellSlotStates>(raw) }
                    .getOrElse { SpellSlotStates() }
            } else {
                SpellSlotStates()
            }
        }

    /**
     * Get current snapshot of maximum spell level.
     */
    suspend fun getMaxSpellLevel(): Int = maxSpellLevel.first()

    /**
     * Get current snapshot of slots per level.
     */
    suspend fun getSlotsPerLevel(): Int = slotsPerLevel.first()

    /**
     * Get current snapshot of slot states.
     */
    suspend fun getSlotStates(): SpellSlotStates = slotStates.first()

    /**
     * Set maximum spell level.
     */
    suspend fun setMaxSpellLevel(level: Int) {
        context.spellSlotDataStore.edit { prefs ->
            prefs[KEY_MAX_SPELL_LEVEL] = level.coerceIn(1, 9)
        }
    }

    /**
     * Set slots per level.
     */
    suspend fun setSlotsPerLevel(count: Int) {
        context.spellSlotDataStore.edit { prefs ->
            prefs[KEY_SLOTS_PER_LEVEL] = count.coerceAtLeast(0)
        }
    }

    /**
     * Update slot state for a specific level and slot index.
     */
    suspend fun setSlotState(level: Int, slotIndex: Int, isAvailable: Boolean) {
        context.spellSlotDataStore.edit { prefs ->
            val current = slotStates.first()
            val levelSlots = current.slots[level]?.toMutableList() ?: mutableListOf()
            
            // Ensure slot index is valid
            if (slotIndex >= 0 && slotIndex < levelSlots.size) {
                levelSlots[slotIndex] = isAvailable
                val updated = current.slots.toMutableMap()
                updated[level] = levelSlots
                val newStates = SpellSlotStates(updated)
                prefs[KEY_SLOT_STATES_JSON] = json.encodeToString(newStates)
            }
        }
    }

    /**
     * Initialize slots for all levels based on configuration.
     * Creates slots with all available (true) state.
     */
    suspend fun initializeSlots() {
        context.spellSlotDataStore.edit { prefs ->
            val maxLevel = prefs[KEY_MAX_SPELL_LEVEL] ?: 9
            val slotsPerLevel = prefs[KEY_SLOTS_PER_LEVEL] ?: 0
            
            val newSlots = mutableMapOf<Int, List<Boolean>>()
            for (level in 1..maxLevel) {
                newSlots[level] = List(slotsPerLevel) { true } // All available
            }
            
            val newStates = SpellSlotStates(newSlots)
            prefs[KEY_SLOT_STATES_JSON] = json.encodeToString(newStates)
        }
    }

    /**
     * Reset all slots to available (true).
     */
    suspend fun resetAllSlots() {
        context.spellSlotDataStore.edit { prefs ->
            val current = slotStates.first()
            val resetSlots = current.slots.mapValues { (_, slots) ->
                slots.map { true } // Reset all to available
            }
            val newStates = SpellSlotStates(resetSlots)
            prefs[KEY_SLOT_STATES_JSON] = json.encodeToString(newStates)
        }
    }
}
