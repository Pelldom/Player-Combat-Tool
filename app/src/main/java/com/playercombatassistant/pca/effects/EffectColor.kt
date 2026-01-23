package com.playercombatassistant.pca.effects

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Stable color ID enum for effects.
 *
 * Provides a fixed set of ~8 color IDs with good contrast in both light and dark modes.
 * All colors are Material 3 compatible and work well on the round tracker bar.
 *
 * This enum stores ONLY the color ID and display name - no Compose Color objects.
 * Use the @Composable extension function `toColor()` to resolve the actual Color
 * from MaterialTheme at composition time.
 */
@Serializable
enum class EffectColorId(
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

    companion object {
        /**
         * Get an EffectColorId by ID, or return PRIMARY as default.
         */
        fun fromId(id: String): EffectColorId {
            return values().find { it.id == id } ?: PRIMARY
        }

        /**
         * Get the default color palette (all available color IDs).
         */
        fun defaultPalette(): List<EffectColorId> {
            return values().toList()
        }
    }
}

/**
 * Resolve the actual Color from a Material 3 ColorScheme.
 * This ensures colors adapt to light/dark mode and dynamic color schemes.
 *
 * This is a @Composable function that must be called from a composable context
 * to access MaterialTheme.colorScheme.
 */
@Composable
fun EffectColorId.toColor(): Color {
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    return when (this) {
        EffectColorId.PRIMARY -> colorScheme.primary
        EffectColorId.SECONDARY -> colorScheme.secondary
        EffectColorId.TERTIARY -> colorScheme.tertiary
        EffectColorId.ERROR -> colorScheme.error
        EffectColorId.PRIMARY_CONTAINER -> colorScheme.primaryContainer
        EffectColorId.SECONDARY_CONTAINER -> colorScheme.secondaryContainer
        EffectColorId.TERTIARY_CONTAINER -> colorScheme.tertiaryContainer
        EffectColorId.ERROR_CONTAINER -> colorScheme.errorContainer
    }
}

/**
 * Resolve the actual Color from a provided ColorScheme.
 * Use this when you already have a ColorScheme instance (e.g., in a non-composable context).
 */
fun EffectColorId.toColor(colorScheme: ColorScheme): Color {
    return when (this) {
        EffectColorId.PRIMARY -> colorScheme.primary
        EffectColorId.SECONDARY -> colorScheme.secondary
        EffectColorId.TERTIARY -> colorScheme.tertiary
        EffectColorId.ERROR -> colorScheme.error
        EffectColorId.PRIMARY_CONTAINER -> colorScheme.primaryContainer
        EffectColorId.SECONDARY_CONTAINER -> colorScheme.secondaryContainer
        EffectColorId.TERTIARY_CONTAINER -> colorScheme.tertiaryContainer
        EffectColorId.ERROR_CONTAINER -> colorScheme.errorContainer
    }
}
