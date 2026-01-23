package com.playercombatassistant.pca.improvised.imports

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Asset loader for `sasha_tables.json`.
 *
 * Responsibilities:
 * - Loads JSON from assets exactly as shipped (no transformation).
 * - Deserializes into [SashaTablesFile] (the file is a top-level JSON array).
 * - Logs clear errors if loading fails.
 * - Caches results in-memory (load once per process).
 *
 * Rules:
 * - No fallback hardcoded data.
 */
class SashaTableAssetLoader(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Loads and caches the tables file. Subsequent calls return the cached value.
     *
     * @throws IllegalStateException if the asset cannot be read or parsed.
     */
    fun load(): SashaTablesFile {
        cached?.let { return it }
        synchronized(lock) {
            cached?.let { return it }

            val raw = try {
                context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to read asset '$ASSET_NAME'. Ensure it exists under app/src/main/assets.", e)
                throw IllegalStateException("Failed to read asset '$ASSET_NAME'.", e)
            }

            val tables = try {
                json.decodeFromString(
                    ListSerializer(SashaLocationTable.serializer()),
                    raw,
                )
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to parse '$ASSET_NAME' as JSON array of SashaLocationTable.", e)
                throw IllegalStateException("Failed to parse '$ASSET_NAME'.", e)
            }

            val result = SashaTablesFile(tables)
            cached = result
            return result
        }
    }

    fun tables(): List<SashaLocationTable> = load().tables

    private companion object {
        private const val TAG = "SashaTableAssetLoader"
        private const val ASSET_NAME = "sasha_tables.json"

        private val lock = Any()

        @Volatile
        private var cached: SashaTablesFile? = null
    }
}

