package com.playercombatassistant.pca.combat

sealed class CombatEvent {
    data object StartCombat : CombatEvent()
    data object NextRound : CombatEvent()
    data object EndCombat : CombatEvent()
}

