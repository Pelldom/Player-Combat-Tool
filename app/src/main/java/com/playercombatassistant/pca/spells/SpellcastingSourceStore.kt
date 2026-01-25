package com.playercombatassistant.pca.spells

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private const val DATASTORE_NAME = "pca_spellcasting_sources"
private val Context.spellcastingSourceDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private val KEY_SOURCES_JSON = stringPreferencesKey("spellcasting_sources_json")

/**
 * Store for managing spellcasting sources and their spell slots.
 */
class SpellcastingSourceStore(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    /**
     * Flow of all spellcasting sources.
     */
    val sources: Flow<List<SpellcastingSource>> =
        context.spellcastingSourceDataStore.data.map { prefs ->
            val raw = prefs[KEY_SOURCES_JSON]
            if (raw != null) {
                runCatching {
                    json.decodeFromString<List<SpellcastingSource>>(raw)
                }.getOrElse { emptyList() }
            } else {
                emptyList()
            }
        }

    /**
     * Get current snapshot of all sources.
     */
    suspend fun getSources(): List<SpellcastingSource> = sources.first()

    /**
     * Add a new spellcasting source.
     */
    suspend fun addSource(source: SpellcastingSource) {
        context.spellcastingSourceDataStore.edit { prefs ->
            val current = getSources().toMutableList()
            current.add(source)
            prefs[KEY_SOURCES_JSON] = json.encodeToString(current)
        }
    }

    /**
     * Update an existing spellcasting source.
     */
    suspend fun updateSource(updatedSource: SpellcastingSource) {
        context.spellcastingSourceDataStore.edit { prefs ->
            val current = getSources().toMutableList()
            val index = current.indexOfFirst { it.id == updatedSource.id }
            if (index >= 0) {
                current[index] = updatedSource
                prefs[KEY_SOURCES_JSON] = json.encodeToString(current)
            }
        }
    }

    /**
     * Delete a spellcasting source by ID.
     */
    suspend fun deleteSource(sourceId: String) {
        context.spellcastingSourceDataStore.edit { prefs ->
            val current = getSources().toMutableList()
            current.removeAll { it.id == sourceId }
            prefs[KEY_SOURCES_JSON] = json.encodeToString(current)
        }
    }

    /**
     * Toggle a slot state for a specific source, level, and slot index.
     */
    suspend fun toggleSlot(sourceId: String, level: Int, slotIndex: Int) {
        context.spellcastingSourceDataStore.edit { prefs ->
            val current = getSources().toMutableList()
            val sourceIndex = current.indexOfFirst { it.id == sourceId }
            if (sourceIndex >= 0) {
                val source = current[sourceIndex]
                val levelSlots = source.slotsByLevel[level]?.toMutableList() ?: return@edit
                
                if (slotIndex >= 0 && slotIndex < levelSlots.size) {
                    levelSlots[slotIndex] = !levelSlots[slotIndex]
                    val updatedSlots = source.slotsByLevel.toMutableMap()
                    updatedSlots[level] = levelSlots
                    current[sourceIndex] = source.copy(slotsByLevel = updatedSlots)
                    prefs[KEY_SOURCES_JSON] = json.encodeToString(current)
                }
            }
        }
    }

    /**
     * Reset all slots for all sources to available.
     */
    suspend fun resetAllSlots() {
        context.spellcastingSourceDataStore.edit { prefs ->
            val current = getSources().map { source ->
                val resetSlots = source.slotsByLevel.mapValues { (_, slots) ->
                    slots.map { true } // Reset all to available
                }
                source.copy(slotsByLevel = resetSlots)
            }
            prefs[KEY_SOURCES_JSON] = json.encodeToString(current)
        }
    }

    /**
     * Clear all sources (for migration/cleanup).
     */
    suspend fun clearAll() {
        context.spellcastingSourceDataStore.edit { prefs ->
            prefs.remove(KEY_SOURCES_JSON)
        }
    }
}
