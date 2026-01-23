package com.playercombatassistant.pca.improvised

import android.content.Context
import android.util.Log
import com.playercombatassistant.pca.improvised.imports.SashaTableAssetLoader
import com.playercombatassistant.pca.improvised.imports.SashaTableTransformer

/**
 * Repository for improvised weapon tables.
 *
 * Responsibilities:
 * - Load `sasha_tables.json` from assets.
 * - Transform Sasha import models into PCA domain models.
 * - Validate expected structure (30 tables, 20 entries each).
 * - Cache the result (load once per process).
 *
 * Failure behavior:
 * - Never throw to callers (no crashes).
 * - Logs a clear error and returns a disabled state with a user-visible message (no silent failure).
 */
class ImprovisedWeaponRepository(
    private val context: Context,
) {
    sealed class LoadState {
        data class Ready(val tables: List<LocationTable>) : LoadState()
        data class Disabled(val userMessage: String) : LoadState()
    }

    fun load(): LoadState {
        cached?.let { return it }
        synchronized(lock) {
            cached?.let { return it }

            val state = runCatching {
                val file = SashaTableAssetLoader(context).load()
                val tables = SashaTableTransformer.transformAll(file)

                validateOrThrow(tables)
                LoadState.Ready(tables)
            }.getOrElse { e ->
                val msg =
                    "Improvised weapon tables failed to load/validate. Weapon rolling is disabled."
                runCatching { Log.e(TAG, msg, e) }
                LoadState.Disabled(msg)
            }

            cached = state
            return state
        }
    }

    private fun validateOrThrow(tables: List<LocationTable>) {
        if (tables.size != EXPECTED_TABLES) {
            throw IllegalStateException("Expected $EXPECTED_TABLES tables, got ${tables.size}.")
        }

        val bad = tables
            .map { it.id to it.items.size }
            .filter { (_, count) -> count != EXPECTED_ENTRIES_PER_TABLE }

        if (bad.isNotEmpty()) {
            val preview = bad.take(5).joinToString { (id, count) -> "id=$id count=$count" }
            throw IllegalStateException(
                "Expected $EXPECTED_ENTRIES_PER_TABLE entries per table; " +
                    "badTables=${bad.size} (first=$preview).",
            )
        }
    }

    private companion object {
        private const val TAG = "ImprovisedWeaponRepo"
        private const val EXPECTED_TABLES = 30
        private const val EXPECTED_ENTRIES_PER_TABLE = 20

        private val lock = Any()

        @Volatile
        private var cached: LoadState? = null
    }
}

