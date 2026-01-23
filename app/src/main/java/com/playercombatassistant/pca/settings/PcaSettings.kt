package com.playercombatassistant.pca.settings

import com.playercombatassistant.pca.effects.GameSystem

enum class DefaultCombatMode {
    NOT_IN_COMBAT,
    IN_COMBAT,
}

data class PcaSettings(
    val gameSystem: GameSystem = GameSystem.GENERIC,
    val defaultCombatMode: DefaultCombatMode = DefaultCombatMode.NOT_IN_COMBAT,
    val showModifierSummary: Boolean = true,
    val showRarity: Boolean = true,
    val historySessionLimit: Int = 50,
)

