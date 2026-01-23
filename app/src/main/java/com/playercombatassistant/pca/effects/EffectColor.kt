package com.playercombatassistant.pca.effects

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Stable color palette for effects.
 *
 * Provides a fixed set of ~8 colors with good contrast in both light and dark modes.
 * All colors are Material 3 compatible and work well on the round tracker bar.
 */
@Serializable
enum class EffectColor(
    val id: String,
    val displayName: String,
) {
    PRIMARY("primary", "Primary"),
    SECONDARY("secondary", "Secondary"),
    TERTIARY("tertiary", "Tertiary"),
    ERROR("error", "Error"),
    PRIMARY_CONTAINER("primary_container", "Primary Container"),
    SECONDARY_CONTAINER("secondary_container", "Secondary Container"),
    TERTIARY_CONTAINER("tertiary_container", "Tertiary Container"),
    ERROR_CONTAINER("error_container", "Error Container"),
    ;

    /**
     * Resolve the actual Color from a Material 3 ColorScheme.
     * This ensures colors adapt to light/dark mode and dynamic color schemes.
     */
    fun resolveColor(colorScheme: ColorScheme): Color {
        return when (this) {
            PRIMARY -> colorScheme.primary
            SECONDARY -> colorScheme.secondary
            TERTIARY -> colorScheme.tertiary
            ERROR -> colorScheme.error
            PRIMARY_CONTAINER -> colorScheme.primaryContainer
            SECONDARY_CONTAINER -> colorScheme.secondaryContainer
            TERTIARY_CONTAINER -> colorScheme.tertiaryContainer
            ERROR_CONTAINER -> colorScheme.errorContainer
        }
    }

    companion object {
        /**
         * Get an EffectColor by ID, or return PRIMARY as default.
         */
        fun fromId(id: String): EffectColor {
            return values().find { it.id == id } ?: PRIMARY
        }

        /**
         * Get the default color palette (all available colors).
         */
        fun defaultPalette(): List<EffectColor> {
            return values().toList()
        }
    }
}
