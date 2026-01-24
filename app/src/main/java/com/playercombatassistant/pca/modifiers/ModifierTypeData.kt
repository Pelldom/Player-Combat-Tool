package com.playercombatassistant.pca.modifiers

import kotlinx.serialization.Serializable

/**
 * Data model for a modifier type loaded from modifier_types.json
 */
@Serializable
data class ModifierType(
    val id: String,
    val label: String,
    val systems: List<String>,
)

/**
 * Data model for stacking rules loaded from stacking_rules.json
 */
@Serializable
data class StackingRule(
    val rule: String,
    val description: String,
    val stackableTypes: List<String>? = null,
)

/**
 * Wrapper for the stacking_rules.json file structure
 * The JSON is a map keyed by system name
 */
typealias StackingRulesFile = Map<String, StackingRule>
