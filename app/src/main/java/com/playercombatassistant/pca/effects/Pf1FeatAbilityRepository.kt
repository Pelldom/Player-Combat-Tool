package com.playercombatassistant.pca.effects

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository for loading Pathfinder 1e feats and abilities from JSON assets.
 *
 * Responsibilities:
 * - Load PF1 feats/abilities JSON file from assets/feats/pf1_feats_abilities.json
 * - Parse JSON into FeatAbilityDefinition data models
 * - Validate required keys exist
 * - Cache loaded data in memory
 * - Provide access functions for feats/abilities
 *
 * Rules:
 * - Data-only (no UI, no business logic)
 * - Fail gracefully with logs (never throw to callers)
 * - Load once per process (cached)
 * - Validate keys but don't crash on missing optional fields
 */
class Pf1FeatAbilityRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Gets all feat/ability definitions loaded from the PF1 feats/abilities file.
     * Uses cached data if available, otherwise loads from assets.
     *
     * @return List of FeatAbilityDefinition objects, or empty list if loading fails
     */
    fun getAllFeatAbilities(): List<FeatAbilityDefinition> {
        loadAndCacheIfNeeded()
        return cachedFeatAbilities
    }

    /**
     * Gets feat/ability definitions for a specific game system.
     * For Pf1FeatAbilityRepository, only returns data if system is PF1.
     *
     * @param system The game system
     * @return List of FeatAbilityDefinition objects for that system, or empty list
     */
    fun getFeatAbilitiesBySystem(system: GameSystem): List<FeatAbilityDefinition> {
        if (system != GameSystem.PF1) {
            return emptyList()
        }
        return getAllFeatAbilities()
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

            val loaded = loadFromAssets()
            cachedFeatAbilities = loaded
            cached = true
        }
    }

    /**
     * Loads feats/abilities from assets and parses into data models.
     * Returns empty list on any error (graceful failure).
     *
     * @return List of FeatAbilityDefinition objects
     */
    private fun loadFromAssets(): List<FeatAbilityDefinition> {
        return try {
            val path = "$ASSET_DIR/$FEATS_ABILITIES_FILE"
            val raw = context.assets.open(path).bufferedReader().use { it.readText() }
            val entries = json.decodeFromString<List<Pf1FeatAbilityEntry>>(raw)

            val featAbilities = entries
                .filter { it.system == "PF1" } // Only load PF1 feats/abilities
                .map { entry ->
                    val colorId = mapFeatColorToEffectColorId(entry.colorId ?: "PRIMARY")
                    FeatAbilityDefinition(
                        id = entry.id,
                        name = entry.name,
                        system = GameSystem.PF1,
                        description = entry.description,
                        defaultDuration = entry.durationRounds,
                        defaultColorId = colorId,
                        modifiers = entry.modifiers,
                    )
                }
                .sortedBy { it.name } // Sort alphabetically by name

            Log.d(TAG, "Loaded ${featAbilities.size} PF1 feats/abilities from ${entries.size} entries")
            if (featAbilities.isEmpty() && entries.isNotEmpty()) {
                Log.w(TAG, "No PF1 feats/abilities found after filtering. All entries had system: ${entries.map { it.system }.distinct()}")
            }
            featAbilities
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read PF1 feats/abilities file from path: $ASSET_DIR/$FEATS_ABILITIES_FILE", e)
            emptyList()
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse PF1 feats/abilities file. Error: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading PF1 feats/abilities file: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Maps string color names from JSON to EffectColorId enum values.
     * Handles both exact matches and PF1-specific color names.
     */
    private fun mapFeatColorToEffectColorId(colorName: String?): EffectColorId {
        if (colorName == null) return EffectColorId.PRIMARY
        
        val normalized = colorName.trim().uppercase()
        return when (normalized) {
            // Exact EffectColorId names
            "PRIMARY" -> EffectColorId.PRIMARY
            "SECONDARY" -> EffectColorId.SECONDARY
            "TERTIARY" -> EffectColorId.TERTIARY
            "ERROR" -> EffectColorId.ERROR
            "PRIMARY_CONTAINER" -> EffectColorId.PRIMARY_CONTAINER
            "SECONDARY_CONTAINER" -> EffectColorId.SECONDARY_CONTAINER
            "TERTIARY_CONTAINER" -> EffectColorId.TERTIARY_CONTAINER
            "ERROR_CONTAINER" -> EffectColorId.ERROR_CONTAINER

            // PF1 feat color names mapped to the closest available palette
            "BROWN" -> EffectColorId.SECONDARY_CONTAINER
            "ORANGE" -> EffectColorId.ERROR_CONTAINER
            "RED" -> EffectColorId.ERROR
            "BLUE" -> EffectColorId.SECONDARY
            "YELLOW" -> EffectColorId.TERTIARY_CONTAINER
            "DARK_GREEN" -> EffectColorId.SECONDARY
            "GREEN" -> EffectColorId.SECONDARY_CONTAINER
            "PURPLE" -> EffectColorId.TERTIARY
            "MAUVE" -> EffectColorId.TERTIARY_CONTAINER
            "PALE_BLUE" -> EffectColorId.PRIMARY_CONTAINER
            "TEAL" -> EffectColorId.PRIMARY_CONTAINER
            "PINK" -> EffectColorId.TERTIARY
            else -> EffectColorId.PRIMARY
        }
    }

    private companion object {
        private const val TAG = "Pf1FeatAbilityRepository"
        private const val ASSET_DIR = "feats"
        private const val FEATS_ABILITIES_FILE = "pf1_feats_abilities.json"

        private val lock = Any()

        @Volatile
        private var cached = false

        @Volatile
        private var cachedFeatAbilities: List<FeatAbilityDefinition> = emptyList()
    }
}
