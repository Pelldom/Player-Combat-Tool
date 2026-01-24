package com.playercombatassistant.pca.effects

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository for loading system-specific condition definitions from JSON assets.
 *
 * Responsibilities:
 * - Load condition JSON files from assets/conditions/
 * - Deserialize into ConditionDefinition objects
 * - Expose conditions grouped by GameSystem
 * - Cache in memory after first load
 *
 * Rules:
 * - Data-only (no UI, no business logic)
 * - Fail gracefully with logs (never throw to callers)
 * - Load once per process (cached)
 */
class ConditionRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Loads all condition definitions and groups them by game system.
     *
     * Returns a map where keys are GameSystem values and values are lists of
     * ConditionDefinition objects for that system.
     *
     * If a system's file fails to load, it will be logged and that system
     * will have an empty list in the result (graceful failure).
     *
     * @return Map of GameSystem to List<ConditionDefinition>
     */
    fun loadAllConditions(): Map<GameSystem, List<ConditionDefinition>> {
        cached?.let { return it }
        synchronized(lock) {
            cached?.let { return it }

            val result = mutableMapOf<GameSystem, List<ConditionDefinition>>()

            // Load conditions for each supported system
            for (system in SUPPORTED_SYSTEMS) {
                val conditions = loadConditionsForSystem(system)
                result[system] = conditions
            }

            cached = result
            return result
        }
    }

    /**
     * Loads condition definitions for a specific game system.
     *
     * @param system The game system to load conditions for
     * @return List of ConditionDefinition objects, or empty list if loading fails
     */
    fun loadConditionsForSystem(system: GameSystem): List<ConditionDefinition> {
        val assetName = getAssetNameForSystem(system)
        if (assetName == null) {
            Log.w(TAG, "No asset file defined for system: ${system.name}")
            return emptyList()
        }

        return try {
            val assetPath = "$ASSET_DIR/$assetName"
            val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            json.decodeFromString(
                ListSerializer(ConditionDefinition.serializer()),
                raw,
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read condition asset '$assetName' for system ${system.name}.", e)
            emptyList()
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse condition asset '$assetName' for system ${system.name}.", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading condition asset '$assetName' for system ${system.name}.", e)
            emptyList()
        }
    }

    /**
     * Gets all conditions for a specific game system.
     * Uses cached data if available, otherwise loads from assets.
     *
     * @param system The game system
     * @return List of ConditionDefinition objects for that system
     */
    fun getConditionsForSystem(system: GameSystem): List<ConditionDefinition> {
        return loadAllConditions()[system] ?: emptyList()
    }

    /**
     * Gets the asset file name for a given game system.
     *
     * @param system The game system
     * @return Asset file name (e.g., "pf1_conditions.json") or null if not supported
     */
    private fun getAssetNameForSystem(system: GameSystem): String? {
        return when (system) {
            GameSystem.PF1 -> "pf1_conditions.json"
            GameSystem.PF2 -> "pf2_conditions.json"
            GameSystem.DND5E -> "dnd5e_conditions.json"
            GameSystem.SAVAGE_WORLDS -> "savage_worlds_conditions.json"
            GameSystem.DCC -> "dcc_conditions.json"
            GameSystem.GENERIC -> null // Generic system has no predefined conditions
        }
    }

    private companion object {
        private const val TAG = "ConditionRepository"
        private const val ASSET_DIR = "conditions"

        /**
         * List of game systems that have condition definition files.
         */
        private val SUPPORTED_SYSTEMS = listOf(
            GameSystem.PF1,
            GameSystem.PF2,
            GameSystem.DND5E,
            GameSystem.SAVAGE_WORLDS,
            GameSystem.DCC,
        )

        private val lock = Any()

        @Volatile
        private var cached: Map<GameSystem, List<ConditionDefinition>>? = null
    }
}
