package com.playercombatassistant.pca.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.playercombatassistant.pca.settings.PcaSettingsStore

private const val DATASTORE_NAME = "pca_history"
private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private val HISTORY_JSON_KEY = stringPreferencesKey("combat_history_json")

class CombatHistoryStore(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    },
) {
    // Configurable later (Settings screen now controls session limit).
    private val maxEventsPerSession: Int = 500
    private val settingsStore = PcaSettingsStore(context)

    val sessions: Flow<List<CombatSessionHistory>> =
        context.historyDataStore.data.map { prefs ->
            val raw = prefs[HISTORY_JSON_KEY] ?: return@map emptyList()
            runCatching { json.decodeFromString(CombatHistory.serializer(), raw).sessions }
                .getOrElse { emptyList() }
        }

    suspend fun recordEvent(
        sessionId: String,
        event: CombatHistoryEvent,
        startedAtMillisIfNew: Long,
        endedAtMillisIfEnding: Long? = null,
    ) {
        val maxSessionsToKeep = settingsStore.getSnapshot().historySessionLimit

        context.historyDataStore.edit { prefs ->
            val currentHistory = prefs[HISTORY_JSON_KEY]
                ?.let { raw -> runCatching { json.decodeFromString(CombatHistory.serializer(), raw) }.getOrNull() }
                ?: CombatHistory()

            val updatedSessions = upsertSession(
                sessions = currentHistory.sessions,
                sessionId = sessionId,
                startedAtMillisIfNew = startedAtMillisIfNew,
                event = event,
                endedAtMillisIfEnding = endedAtMillisIfEnding,
            )
                .takeLast(maxSessionsToKeep)

            prefs[HISTORY_JSON_KEY] = json.encodeToString(CombatHistory(sessions = updatedSessions))
        }
    }

    suspend fun clearAllHistory() {
        context.historyDataStore.edit { prefs ->
            prefs.remove(HISTORY_JSON_KEY)
        }
    }

    private fun upsertSession(
        sessions: List<CombatSessionHistory>,
        sessionId: String,
        startedAtMillisIfNew: Long,
        event: CombatHistoryEvent,
        endedAtMillisIfEnding: Long?,
    ): List<CombatSessionHistory> {
        val idx = sessions.indexOfLast { it.id == sessionId }
        val updated = if (idx >= 0) {
            val existing = sessions[idx]
            val newEvents = (existing.events + event).takeLast(maxEventsPerSession)
            existing.copy(
                endedAtMillis = endedAtMillisIfEnding ?: existing.endedAtMillis,
                events = newEvents,
            )
        } else {
            CombatSessionHistory(
                id = sessionId,
                startedAtMillis = startedAtMillisIfNew,
                endedAtMillis = endedAtMillisIfEnding,
                events = listOf(event),
            )
        }

        val mutable = sessions.toMutableList()
        if (idx >= 0) mutable[idx] = updated else mutable.add(updated)
        return mutable
    }
}

