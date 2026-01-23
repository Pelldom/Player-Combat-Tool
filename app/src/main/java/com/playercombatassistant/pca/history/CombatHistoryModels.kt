package com.playercombatassistant.pca.history

import com.playercombatassistant.pca.effects.Effect
import com.playercombatassistant.pca.improvised.ImprovisedItem
import com.playercombatassistant.pca.combat.WeaponLockMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CombatHistory(
    val sessions: List<CombatSessionHistory> = emptyList(),
)

@Serializable
data class CombatSessionHistory(
    val id: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val events: List<CombatHistoryEvent> = emptyList(),
)

@Serializable
sealed class CombatHistoryEvent {
    abstract val timestampMillis: Long
    abstract val round: Int?

    @Serializable
    @SerialName("start_combat")
    data class StartCombat(
        override val timestampMillis: Long,
        override val round: Int = 1,
    ) : CombatHistoryEvent()

    @Serializable
    @SerialName("next_round")
    data class NextRound(
        override val timestampMillis: Long,
        override val round: Int,
    ) : CombatHistoryEvent()

    @Serializable
    @SerialName("end_combat")
    data class EndCombat(
        override val timestampMillis: Long,
        override val round: Int?,
    ) : CombatHistoryEvent()

    @Serializable
    @SerialName("effect_applied")
    data class EffectApplied(
        override val timestampMillis: Long,
        override val round: Int?,
        val effect: Effect,
    ) : CombatHistoryEvent()

    @Serializable
    @SerialName("effect_expired")
    data class EffectExpired(
        override val timestampMillis: Long,
        override val round: Int?,
        val effect: Effect,
    ) : CombatHistoryEvent()

    @Serializable
    @SerialName("improvised_selected")
    data class ImprovisedSelected(
        override val timestampMillis: Long,
        override val round: Int?,
        val item: ImprovisedItem,
    ) : CombatHistoryEvent()

    /**
     * Improvised weapon roll event (explicitly includes location + visible rolls for history display).
     *
     * Notes:
     * - [d30Roll] is nullable because a location may be selected manually.
     * - [d100Roll] is always present and always shown to the user.
     * - [item] contains description + rarity for display; no rule enforcement/validation.
     */
    @Serializable
    @SerialName("improvised_weapon_rolled")
    data class ImprovisedWeaponRolled(
        override val timestampMillis: Long,
        override val round: Int?,
        /**
         * Combat session id when the roll occurred during combat.
         * Null for out-of-combat rolls (to keep those entries unchanged).
         */
        val combatId: String? = null,
        val lockMode: WeaponLockMode? = null,
        val origin: ImprovisedWeaponRollOrigin = ImprovisedWeaponRollOrigin.AUTOMATIC,
        val locationId: Int,
        val locationName: String,
        val d30Roll: Int? = null,
        val d100Roll: Int,
        val item: ImprovisedItem,
    ) : CombatHistoryEvent()
}

