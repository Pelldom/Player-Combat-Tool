package com.playercombatassistant.pca.improvised.imports

import kotlinx.serialization.Serializable

/**
 * Import-only models for deserializing `sasha_tables.json`.
 *
 * IMPORTANT:
 * - These types are **ONLY** for reading the source JSON as-is.
 * - Do NOT reuse/alias PCA domain models (`ImprovisedItem`, `LocationTable`, etc.) here.
 * - No parsing/validation/business logic belongs in this layer.
 */

/**
 * Top-level file shape for `sasha_tables.json`.
 *
 * The JSON is a top-level array, so this value class serializes/deserializes as the underlying list directly.
 * This keeps the JSON shape exact while still providing a named container type in Kotlin.
 */
@Serializable
@JvmInline
value class SashaTablesFile(
    val tables: List<SashaLocationTable>,
)

/**
 * A location table entry as represented in `sasha_tables.json`.
 *
 * Notes:
 * - [entries] are raw display strings from the source file (no parsing into structured weapon fields here).
 */
@Serializable
data class SashaLocationTable(
    val id: Int,
    val name: String,
    val entries: List<String>,
)

