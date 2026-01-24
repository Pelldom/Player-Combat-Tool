package com.playercombatassistant.pca.modifiers

import android.content.Context
import android.util.Log
import com.playercombatassistant.pca.effects.GameSystem
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository for loading modifier types and stacking rules from JSON assets.
 *
 * Responsibilities:
 * - Load modifier_types.json from assets/modifiers/modifier_types.json
 * - Load stacking_rules.json from assets/modifiers/stacking_rules.json
 * - Parse JSON into ModifierType and StackingRule data models
 * - Filter modifier types by game system
 * - Cache loaded data in memory
 *
 * Rules:
 * - Data-only (no UI, no business logic)
 * - Fail gracefully with logs (never throw to callers)
 * - Load once per process (cached)
 */
class ModifierRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Gets all modifier types loaded from the modifier_types.json file.
     * Uses cached data if available, otherwise loads from assets.
     *
     * @return List of ModifierType objects, or empty list if loading fails
     */
    fun getAllModifierTypes(): List<ModifierType> {
        loadAndCacheIfNeeded()
        return cachedModifierTypes
    }

    /**
     * Gets modifier types filtered by the specified game system.
     * Only returns types whose "systems" array contains the system name.
     *
     * @param system The game system to filter by
     * @return List of ModifierType objects for that system, or empty list if loading fails
     */
    fun getModifierTypesBySystem(system: GameSystem): List<ModifierType> {
        loadAndCacheIfNeeded()
        val systemName = mapGameSystemToJsonName(system)
        return cachedModifierTypes.filter { it.systems.contains(systemName) }
    }

    /**
     * Gets all stacking rules loaded from the stacking_rules.json file.
     * Uses cached data if available, otherwise loads from assets.
     *
     * @return Map of system name to StackingRule, or empty map if loading fails
     */
    fun getAllStackingRules(): Map<String, StackingRule> {
        loadAndCacheIfNeeded()
        return cachedStackingRules
    }

    /**
     * Gets stacking rule for a specific game system.
     *
     * @param system The game system
     * @return StackingRule for that system, or null if not found
     */
    fun getStackingRuleBySystem(system: GameSystem): StackingRule? {
        loadAndCacheIfNeeded()
        val systemName = mapGameSystemToJsonName(system)
        return cachedStackingRules[systemName]
    }

    /**
     * Maps GameSystem enum to the JSON system name format.
     * JSON uses: "PF1", "PF2", "5e", "SavageW", "DCC"
     * Enum uses: PF1, PF2, DND5E, SAVAGE_WORLDS, DCC, GENERIC
     */
    private fun mapGameSystemToJsonName(system: GameSystem): String {
        return when (system) {
            GameSystem.PF1 -> "PF1"
            GameSystem.PF2 -> "PF2"
            GameSystem.DND5E -> "5e"
            GameSystem.SAVAGE_WORLDS -> "SavageW"
            GameSystem.DCC -> "DCC"
            GameSystem.GENERIC -> "PF1" // Default to PF1 for GENERIC
        }
    }

    /**
     * Loads and caches data if not already cached.
     * Thread-safe and idempotent.
     */
    private fun loadAndCacheIfNeeded() {
        if (cached) {
            return
        }
        synchronized(lock) {
            if (cached) {
                return
            }

            val (types, rules) = loadFromAssets()
            cachedModifierTypes = types
            cachedStackingRules = rules
            cached = true
        }
    }

    /**
     * Loads modifier types and stacking rules from assets and parses into data models.
     * Returns empty lists/maps on any error (graceful failure).
     *
     * @return Pair of (modifier types list, stacking rules map)
     */
    private fun loadFromAssets(): Pair<List<ModifierType>, Map<String, StackingRule>> {
        val types = try {
            val assetPath = "$ASSET_DIR/$MODIFIER_TYPES_FILE"
            val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            json.decodeFromString<List<ModifierType>>(raw)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read modifier_types.json asset file.", e)
            emptyList()
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse modifier_types.json asset file.", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading modifier_types.json asset file.", e)
            emptyList()
        }

        val rules = try {
            val assetPath = "$ASSET_DIR/$STACKING_RULES_FILE"
            val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            json.decodeFromString<StackingRulesFile>(raw)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read stacking_rules.json asset file.", e)
            emptyMap()
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse stacking_rules.json asset file.", e)
            emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading stacking_rules.json asset file.", e)
            emptyMap()
        }

        Log.d(TAG, "Loaded ${types.size} modifier types and ${rules.size} stacking rules")
        return Pair(types, rules)
    }

    private companion object {
        private const val TAG = "ModifierRepository"
        private const val ASSET_DIR = "modifiers"
        private const val MODIFIER_TYPES_FILE = "modifier_types.json"
        private const val STACKING_RULES_FILE = "stacking_rules.json"

        private val lock = Any()

        @Volatile
        private var cached = false

        @Volatile
        private var cachedModifierTypes: List<ModifierType> = emptyList()

        @Volatile
        private var cachedStackingRules: Map<String, StackingRule> = emptyMap()
    }
}
