package com.playercombatassistant.pca.improvised.imports

import android.util.Log
import com.playercombatassistant.pca.BuildConfig
import com.playercombatassistant.pca.improvised.Handedness
import com.playercombatassistant.pca.improvised.ImprovisedItem
import com.playercombatassistant.pca.improvised.LocationTable
import com.playercombatassistant.pca.improvised.Rarity
import java.security.MessageDigest

/**
 * Transforms import-only Sasha models into PCA improvised weapon domain models.
 *
 * Responsibilities:
 * - Convert [SashaLocationTable] -> [LocationTable] (preserving table id/name).
 * - Convert each raw entry string -> [ImprovisedItem] using [SashaEntryParser].
 *
 * Defaults:
 * - rarity = COMMON
 * - weight = 5
 * - handedness = ONE_HANDED if unknown
 *
 * Rules:
 * - Generate stable item ids (content-based).
 * - Log parsing warnings in debug builds only.
 * - No UI, no combat logic, no balancing/rules interpretation.
 */
object SashaTableTransformer {

    fun transformTable(table: SashaLocationTable): LocationTable {
        val items = table.entries
            .mapIndexedNotNull { index, raw ->
                val trimmed = raw.trim()
                if (trimmed.isEmpty()) return@mapIndexedNotNull null
                entryToImprovisedItem(tableId = table.id, entryIndex = index, raw = trimmed)
            }

        return LocationTable(
            id = table.id,
            name = table.name,
            items = items,
        )
    }

    fun transformAll(file: SashaTablesFile): List<LocationTable> = file.tables.map(::transformTable)

    private fun entryToImprovisedItem(
        tableId: Int,
        entryIndex: Int,
        raw: String,
    ): ImprovisedItem {
        val parsed = SashaEntryParser.parse(raw)

        val itemId = stableItemId(tableId = tableId, entryIndex = entryIndex, raw = raw)

        val handedness = parsed.handedness ?: Handedness.ONE_HANDED
        val notes = parsed.notes.trim().ifEmpty { null }

        if (BuildConfig.DEBUG) {
            val warn =
                parsed.ambiguous ||
                    parsed.name.isBlank() ||
                    parsed.damage.isBlank()

            if (warn) {
                // android.util.Log calls can throw in local JVM unit tests ("Method ... not mocked").
                // Warnings are best-effort and must never break parsing/transformation.
                runCatching {
                    Log.w(
                        TAG,
                        "Parsing warning (tableId=$tableId, entryIndex=$entryIndex): " +
                            "name='${parsed.name}', damage='${parsed.damage}', damageType='${parsed.damageType}', " +
                            "handedness='${parsed.handedness}', ambiguous=${parsed.ambiguous}. raw='$raw'",
                    )
                }
            }
        }

        return ImprovisedItem(
            id = itemId,
            name = parsed.name,
            description = raw,
            damage = parsed.damage,
            damageType = parsed.damageType,
            handedness = handedness,
            notes = notes,
            weight = DEFAULT_WEIGHT,
            rarity = DEFAULT_RARITY,
        )
    }

    /**
     * Stable id generation for history-friendly references.
     *
     * We hash the raw entry string so identical entries across different tables can share the same id.
     * If the entry is blank (should already be filtered), fall back to a deterministic per-table index id.
     */
    private fun stableItemId(
        tableId: Int,
        entryIndex: Int,
        raw: String,
    ): String {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return "sasha_t${tableId}_i$entryIndex"

        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
        val hex = digest.toHexString()
        return "sasha_${hex.take(12)}"
    }

    private fun ByteArray.toHexString(): String {
        val out = StringBuilder(this.size * 2)
        for (b in this) {
            out.append(HEX_CHARS[(b.toInt() shr 4) and 0xF])
            out.append(HEX_CHARS[b.toInt() and 0xF])
        }
        return out.toString()
    }

    private const val DEFAULT_WEIGHT = 5
    private val DEFAULT_RARITY = Rarity.COMMON

    private const val TAG = "SashaTableTransformer"
    private val HEX_CHARS = "0123456789abcdef".toCharArray()
}

