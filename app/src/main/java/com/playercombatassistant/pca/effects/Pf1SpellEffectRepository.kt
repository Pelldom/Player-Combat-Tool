package com.playercombatassistant.pca.effects

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository for loading Pathfinder 1e spell effects from JSON assets.
 *
 * Responsibilities:
 * - Load PF1 spell effects JSON file from assets/spells/pf1_spell_effects.json
 * - Parse JSON into SpellEffectDefinition data models
 * - Validate required keys exist
 * - Cache loaded data in memory
 * - Provide access functions for spell effects
 *
 * Rules:
 * - Data-only (no UI, no business logic)
 * - Fail gracefully with logs (never throw to callers)
 * - Load once per process (cached)
 * - Validate keys but don't crash on missing optional fields
 */
class Pf1SpellEffectRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Gets all spell effect definitions loaded from the PF1 spell effects file.
     * Uses cached data if available, otherwise loads from assets.
     *
     * @return List of SpellEffectDefinition objects, or empty list if loading fails
     */
    fun getAllSpellEffects(): List<SpellEffectDefinition> {
        loadAndCacheIfNeeded()
        return cachedSpellEffects
    }

    /**
     * Gets spell effect definitions for a specific game system.
     * For Pf1SpellEffectRepository, only returns data if system is PF1.
     *
     * @param system The game system
     * @return List of SpellEffectDefinition objects for that system, or empty list
     */
    fun getSpellEffectsBySystem(system: GameSystem): List<SpellEffectDefinition> {
        if (system != GameSystem.PF1) {
            return emptyList()
        }
        return getAllSpellEffects()
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
            cachedSpellEffects = loaded
            cached = true
        }
    }

    /**
     * Loads spell effects from assets and parses into data models.
     * Returns empty list on any error (graceful failure).
     *
     * @return List of SpellEffectDefinition objects
     */
    private fun loadFromAssets(): List<SpellEffectDefinition> {
        return try {
            val path = "$ASSET_DIR/$SPELL_EFFECTS_FILE"
            val raw = context.assets.open(path).bufferedReader().use { it.readText() }
            val entries = json.decodeFromString<List<Pf1SpellEffectEntry>>(raw)

            val spellEffects = entries
                .filter { it.system == "PF1" } // Only load PF1 spell effects
                .map { entry ->
                    val colorId = mapSpellColorToEffectColorId(entry.colorId ?: "PRIMARY")
                    SpellEffectDefinition(
                        id = entry.id,
                        name = entry.name,
                        system = GameSystem.PF1,
                        spellLevel = entry.spellLevel,
                        description = entry.description,
                        defaultDuration = entry.durationRounds,
                        defaultColorId = colorId,
                        modifiers = entry.modifiers,
                    )
                }
                .sortedBy { it.name } // Sort alphabetically by name

            Log.d(TAG, "Loaded ${spellEffects.size} PF1 spell effects from ${entries.size} entries")
            if (spellEffects.isEmpty() && entries.isNotEmpty()) {
                Log.w(TAG, "No PF1 spell effects found after filtering. All entries had system: ${entries.map { it.system }.distinct()}")
            }
            spellEffects
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read PF1 spell effects file from path: $ASSET_DIR/$SPELL_EFFECTS_FILE", e)
            emptyList()
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse PF1 spell effects file. Error: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading PF1 spell effects file: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Maps string color names from JSON to EffectColorId enum values.
     * Handles both exact matches and PF1-specific color names.
     */
    private fun mapSpellColorToEffectColorId(colorName: String?): EffectColorId {
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

            // PF1 spell color names mapped to the closest available palette
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
        private const val TAG = "Pf1SpellEffectRepository"
        private const val ASSET_DIR = "spells"
        private const val SPELL_EFFECTS_FILE = "pf1_spell_effects.json"

        private val lock = Any()

        @Volatile
        private var cached = false

        @Volatile
        private var cachedSpellEffects: List<SpellEffectDefinition> = emptyList()
    }
}
