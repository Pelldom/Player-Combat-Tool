package com.playercombatassistant.pca.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.playercombatassistant.pca.effects.GameSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "pca_settings"
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private val KEY_GAME_SYSTEM = stringPreferencesKey("game_system")
private val KEY_DEFAULT_COMBAT_MODE = stringPreferencesKey("default_combat_mode")
private val KEY_SHOW_MODIFIER_SUMMARY = booleanPreferencesKey("show_modifier_summary")
private val KEY_SHOW_RARITY = booleanPreferencesKey("show_rarity")
private val KEY_HISTORY_SESSION_LIMIT = intPreferencesKey("history_session_limit")

class PcaSettingsStore(
    private val context: Context,
) {
    val settings: Flow<PcaSettings> =
        context.settingsDataStore.data.map { prefs ->
            val gameSystem = prefs[KEY_GAME_SYSTEM]
                ?.let { runCatching { GameSystem.valueOf(it) }.getOrNull() }
                ?: GameSystem.GENERIC

            val defaultCombatMode = prefs[KEY_DEFAULT_COMBAT_MODE]
                ?.let { runCatching { DefaultCombatMode.valueOf(it) }.getOrNull() }
                ?: DefaultCombatMode.NOT_IN_COMBAT

            val showModifierSummary = prefs[KEY_SHOW_MODIFIER_SUMMARY] ?: true
            val showRarity = prefs[KEY_SHOW_RARITY] ?: true
            val historySessionLimit = prefs[KEY_HISTORY_SESSION_LIMIT] ?: 50

            PcaSettings(
                gameSystem = gameSystem,
                defaultCombatMode = defaultCombatMode,
                showModifierSummary = showModifierSummary,
                showRarity = showRarity,
                historySessionLimit = historySessionLimit,
            )
        }

    suspend fun getSnapshot(): PcaSettings = settings.first()

    suspend fun setGameSystem(system: GameSystem) {
        context.settingsDataStore.edit { it[KEY_GAME_SYSTEM] = system.name }
    }

    suspend fun setDefaultCombatMode(mode: DefaultCombatMode) {
        context.settingsDataStore.edit { it[KEY_DEFAULT_COMBAT_MODE] = mode.name }
    }

    suspend fun setShowModifierSummary(value: Boolean) {
        context.settingsDataStore.edit { it[KEY_SHOW_MODIFIER_SUMMARY] = value }
    }

    suspend fun setShowRarity(value: Boolean) {
        context.settingsDataStore.edit { it[KEY_SHOW_RARITY] = value }
    }

    suspend fun setHistorySessionLimit(value: Int) {
        context.settingsDataStore.edit { it[KEY_HISTORY_SESSION_LIMIT] = value }
    }
}

