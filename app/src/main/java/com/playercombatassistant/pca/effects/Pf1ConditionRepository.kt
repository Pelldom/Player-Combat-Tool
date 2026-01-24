package com.playercombatassistant.pca.effects

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository for loading Pathfinder 1e conditions and modifiers from JSON assets.
 *
 * Responsibilities:
 * - Load PF1 conditions JSON file from assets/conditions/pf1_conditions.json
 * - Parse JSON into ConditionDefinition and ModifierDefinition data models
 * - Validate required keys exist
 * - Cache loaded data in memory
 * - Provide access functions for conditions and modifiers
 *
 * Rules:
 * - Data-only (no UI, no business logic)
 * - Fail gracefully with logs (never throw to callers)
 * - Load once per process (cached)
 * - Validate keys but don't crash on missing optional fields
 */
class Pf1ConditionRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Gets all condition definitions loaded from the PF1 conditions file.
     * Uses cached data if available, otherwise loads from assets.
     *
     * @return List of ConditionDefinition objects, or empty list if loading fails
     */
    fun getAllConditions(): List<ConditionDefinition> {
        loadAndCacheIfNeeded()
        return cachedConditions
    }

    /**
     * Gets all modifier definitions from all PF1 conditions.
     * Uses cached data if available, otherwise loads from assets.
     *
     * Note: PF1 JSON structure stores modifiers within conditions, not as standalone entities.
     * This function extracts all ModifierEntry objects from all conditions.
     *
     * @return List of ModifierDefinition objects, or empty list if loading fails
     */
    fun getAllModifiers(): List<ModifierDefinition> {
        loadAndCacheIfNeeded()
        // Extract all ModifierEntry objects from conditions and convert to ModifierDefinition
        // Since PF1 JSON doesn't have standalone modifier definitions with id/name/system,
        // we create synthetic ModifierDefinition objects from the modifier entries
        return cachedConditions.flatMap { condition ->
            condition.modifiers.mapIndexed { index, modifierEntry ->
                ModifierDefinition(
                    id = "${condition.id}_modifier_$index",
                    name = "${condition.name} - ${modifierEntry.target}",
                    system = condition.system,
                    effects = listOf(
                        EffectEntry(
                            target = modifierEntry.target,
                            value = modifierEntry.value,
                        ),
                    ),
                )
            }
        }
    }

    /**
     * Gets condition definitions for a specific game system.
     * For PF1ConditionRepository, only returns data if system is PF1.
     *
     * @param system The game system
     * @return List of ConditionDefinition objects for that system, or empty list
     */
    fun getConditionsBySystem(system: GameSystem): List<ConditionDefinition> {
        if (system != GameSystem.PF1) {
            return emptyList()
        }
        return getAllConditions()
    }

    /**
     * Gets modifier definitions for a specific game system.
     * For PF1ConditionRepository, only returns data if system is PF1.
     *
     * @param system The game system
     * @return List of ModifierDefinition objects for that system, or empty list
     */
    fun getModifiersBySystem(system: GameSystem): List<ModifierDefinition> {
        if (system != GameSystem.PF1) {
            return emptyList()
        }
        return getAllModifiers()
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

            val (conditions, modifiers) = loadFromAssets()
            cachedConditions = conditions
            cachedModifiers = modifiers
            cached = true
        }
    }

    /**
     * Loads PF1 conditions from assets and parses into data models.
     * Returns empty lists on any error (graceful failure).
     *
     * @return Pair of (conditions list, modifiers list)
     * Note: Modifiers are now embedded in conditions, so modifiers list is derived from conditions
     */
    private fun loadFromAssets(): Pair<List<ConditionDefinition>, List<ModifierDefinition>> {
        return try {
            val assetPath = "$ASSET_DIR/$ASSET_FILE"
            val raw = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val fileData = json.decodeFromString<Pf1ConditionsFile>(raw)

            val conditions = mutableListOf<ConditionDefinition>()

            for (entry in fileData.conditions) {
                // Validate required fields
                if (entry.id.isBlank()) {
                    Log.w(TAG, "Skipping condition with blank id")
                    continue
                }
                if (entry.name.isBlank()) {
                    Log.w(TAG, "Skipping condition '${entry.id}' with blank name")
                    continue
                }
                if (entry.shortDescription.isBlank()) {
                    Log.w(TAG, "Skipping condition '${entry.id}' with blank shortDescription")
                    continue
                }

                // Parse system enum
                val system = try {
                    GameSystem.valueOf(entry.system)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Invalid system '${entry.system}' for condition '${entry.id}', defaulting to PF1")
                    GameSystem.PF1
                }

                // Validate modifiers
                val validModifiers = entry.modifiers.filter { modifier ->
                    if (modifier.target.isBlank()) {
                        Log.w(TAG, "Skipping modifier with blank target in condition '${entry.id}'")
                        false
                    } else if (modifier.value.isBlank()) {
                        Log.w(TAG, "Skipping modifier with blank value in condition '${entry.id}'")
                        false
                    } else {
                        true
                    }
                }

                // Create ConditionDefinition (mapping from JSON structure)
                // Note: PF1 conditions JSON doesn't have defaultDuration, defaultColorId, or tags
                // We'll use defaults for these fields
                val condition = ConditionDefinition(
                    id = entry.id,
                    name = entry.name,
                    system = system,
                    shortDescription = entry.shortDescription,
                    detailedDescription = null, // Not in JSON
                    defaultDuration = null, // Not in JSON
                    defaultColorId = EffectColorId.PRIMARY, // Default color
                    tags = emptyList(), // Not in JSON
                    modifiers = validModifiers.map { ModifierEntry(target = it.target, value = it.value) },
                )

                conditions.add(condition)
            }

            // Modifiers are now embedded in conditions, so we derive the modifiers list
            val allModifiers = conditions.flatMap { condition ->
                condition.modifiers.mapIndexed { index, modifierEntry ->
                    ModifierDefinition(
                        id = "${condition.id}_modifier_$index",
                        name = "${condition.name} - ${modifierEntry.target}",
                        system = condition.system,
                        effects = listOf(
                            EffectEntry(
                                target = modifierEntry.target,
                                value = modifierEntry.value,
                            ),
                        ),
                    )
                }
            }

            Log.d(TAG, "Loaded ${conditions.size} conditions and ${allModifiers.size} modifiers from PF1 conditions file")
            Pair(conditions, allModifiers)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read PF1 conditions asset file '$ASSET_FILE'.", e)
            Pair(emptyList(), emptyList())
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse PF1 conditions asset file '$ASSET_FILE'.", e)
            Pair(emptyList(), emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading PF1 conditions asset file '$ASSET_FILE'.", e)
            Pair(emptyList(), emptyList())
        }
    }

    private companion object {
        private const val TAG = "Pf1ConditionRepository"
        private const val ASSET_DIR = "conditions"
        private const val ASSET_FILE = "pf1_conditions.json"

        private val lock = Any()

        @Volatile
        private var cached = false

        @Volatile
        private var cachedConditions: List<ConditionDefinition> = emptyList()

        @Volatile
        private var cachedModifiers: List<ModifierDefinition> = emptyList()
    }
}
