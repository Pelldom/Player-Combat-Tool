package com.playercombatassistant.pca.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.playercombatassistant.pca.effects.Effect
import com.playercombatassistant.pca.effects.EffectColor
import com.playercombatassistant.pca.effects.EffectType
import kotlin.math.max
import kotlin.math.min

/**
 * A vertical bar that displays combat rounds with effect spans.
 *
 * Features:
 * - One tick per round (within visible window)
 * - Highlights current round
 * - Displays a sliding window of rounds
 * - Draws colored spans for effects across rounds
 *
 * Constraints:
 * - No text labels inside the bar
 * - Colors come from effect definitions (EffectType)
 */
@Composable
fun RoundTrackerBar(
    modifier: Modifier = Modifier,
    currentRound: Int,
    activeEffects: List<Effect>,
    /**
     * The minimum round to display (inclusive).
     * If null, automatically calculates based on effects and current round.
     */
    minRound: Int? = null,
    /**
     * The maximum round to display (inclusive).
     * If null, automatically calculates based on effects and current round.
     */
    maxRound: Int? = null,
    /**
     * Width of the bar.
     */
    barWidth: Dp = 48.dp,
    /**
     * Width of each round tick.
     */
    tickWidth: Dp = 4.dp,
) {
    // Calculate visible round range (sliding window)
    val visibleMinRound = minRound ?: calculateMinRound(currentRound, activeEffects)
    val visibleMaxRound = maxRound ?: calculateMaxRound(currentRound, activeEffects)
    val visibleRoundCount = visibleMaxRound - visibleMinRound + 1

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    Canvas(
        modifier = modifier
            .width(barWidth)
            .fillMaxHeight()
            .semantics {
                contentDescription = "Round tracker: Current round $currentRound, ${activeEffects.size} active effects"
            },
    ) {
        val canvasHeight = size.height
        val canvasWidth = size.width
        val tickWidthPx = tickWidth.toPx()

        // Calculate spacing between ticks
        val tickSpacing = if (visibleRoundCount > 1) {
            (canvasHeight - tickWidthPx) / (visibleRoundCount - 1)
        } else {
            0f
        }

        // Draw effect spans first (behind ticks)
        // Group effects by overlapping ranges to handle stacking
        val effectRanges = activeEffects.mapNotNull { effect ->
            val effectStartRound = effect.startRound
            val effectEndRound = effect.effectEndRound(visibleMaxRound)
            
            if (effectEndRound != null && effectEndRound >= visibleMinRound && effectStartRound <= visibleMaxRound) {
                val drawStartRound = max(effectStartRound, visibleMinRound)
                val drawEndRound = min(effectEndRound, visibleMaxRound)
                EffectRange(effect, drawStartRound, drawEndRound, false)
            } else if (effect.endRound == null && effect.remainingRounds == null && effectStartRound <= visibleMaxRound) {
                val drawStartRound = max(effectStartRound, visibleMinRound)
                EffectRange(effect, drawStartRound, visibleMaxRound, true)
            } else {
                null
            }
        }

        // Draw effects with offset for overlapping ones
        val overlappingGroups = groupOverlappingEffects(effectRanges)
        for ((groupIndex, group) in overlappingGroups.withIndex()) {
            for ((effectIndex, effectRange) in group.withIndex()) {
                val startY = roundToY(effectRange.startRound, visibleMinRound, tickSpacing, tickWidthPx, canvasHeight)
                val endY = if (effectRange.isIndefinite) {
                    canvasHeight
                } else {
                    roundToY(effectRange.endRound, visibleMinRound, tickSpacing, tickWidthPx, canvasHeight)
                }

                // Offset overlapping effects horizontally for visibility
                val offsetX = if (group.size > 1) {
                    (effectIndex * (canvasWidth * 0.15f)).coerceAtMost(canvasWidth * 0.4f)
                } else {
                    0f
                }
                val effectWidth = if (group.size > 1) canvasWidth * 0.6f else canvasWidth

                val effectColor = getEffectColor(effectRange.effect.type, colorScheme)
                
                // Draw effect span with better contrast
                val fillAlpha = if (isDark) 0.4f else 0.25f
                drawRect(
                    color = effectColor.copy(alpha = fillAlpha),
                    topLeft = Offset(x = offsetX, y = startY),
                    size = Size(width = effectWidth, height = endY - startY),
                )

                // Draw effect border with better visibility
                val borderWidth = if (group.size > 1) 1.5f.dp.toPx() else 1.dp.toPx()
                drawRect(
                    color = effectColor,
                    topLeft = Offset(x = offsetX, y = startY),
                    size = Size(width = effectWidth, height = endY - startY),
                    style = Stroke(width = borderWidth),
                )
            }
        }

        // Draw background for better contrast
        drawRect(
            color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
            topLeft = Offset.Zero,
            size = Size(width = canvasWidth, height = canvasHeight),
        )

        // Draw round ticks
        for (round in visibleMinRound..visibleMaxRound) {
            val y = roundToY(round, visibleMinRound, tickSpacing, tickWidthPx, canvasHeight)
            val isCurrentRound = round == currentRound

            // Draw tick with better contrast
            val tickColor = if (isCurrentRound) {
                colorScheme.primary
            } else {
                // Use onSurface with better opacity for contrast
                colorScheme.onSurface.copy(alpha = if (isDark) 0.6f else 0.4f)
            }

            val currentTickWidth = if (isCurrentRound) tickWidthPx * 1.5f else tickWidthPx
            val currentTickHeight = if (isCurrentRound) tickWidthPx * 1.5f else tickWidthPx

            drawRect(
                color = tickColor,
                topLeft = Offset(x = canvasWidth - currentTickWidth, y = y),
                size = Size(width = currentTickWidth, height = currentTickHeight),
            )

            // Highlight current round with a more prominent indicator
            if (isCurrentRound) {
                // Draw full-width highlight
                drawRect(
                    color = colorScheme.primaryContainer.copy(alpha = if (isDark) 0.4f else 0.3f),
                    topLeft = Offset(x = 0f, y = y),
                    size = Size(width = canvasWidth, height = currentTickHeight),
                )
                // Draw accent line
                drawRect(
                    color = colorScheme.primary,
                    topLeft = Offset(x = 0f, y = y),
                    size = Size(width = 2.dp.toPx(), height = currentTickHeight),
                )
            }
        }
    }
}

/**
 * Calculate the Y position for a given round.
 */
private fun roundToY(
    round: Int,
    minRound: Int,
    tickSpacing: Float,
    tickWidth: Float,
    canvasHeight: Float,
): Float {
    val roundIndex = round - minRound
    return if (roundIndex == 0) {
        0f
    } else {
        roundIndex * tickSpacing
    }.coerceIn(0f, canvasHeight - tickWidth)
}

/**
 * Get the effective end round for an effect (clamped to visible max if indefinite).
 */
private fun Effect.effectEndRound(visibleMaxRound: Int): Int? {
    return endRound ?: if (remainingRounds == null) {
        // Indefinite effect - use visible max as a reasonable display limit for display
        visibleMaxRound
    } else {
        // Calculate from startRound + remainingRounds if endRound wasn't set
        startRound + remainingRounds
    }
}

/**
 * Calculate minimum visible round based on current round and effects.
 */
private fun calculateMinRound(currentRound: Int, effects: List<Effect>): Int {
    val effectMinRound = effects.minOfOrNull { it.startRound } ?: currentRound
    // Show at least 5 rounds before current, or start from round 1
    return max(1, min(effectMinRound, currentRound - 5))
}

/**
 * Calculate maximum visible round based on current round and effects.
 */
private fun calculateMaxRound(currentRound: Int, effects: List<Effect>): Int {
    val effectMaxRound = effects.maxOfOrNull { effect ->
        effect.endRound ?: effect.startRound + (effect.remainingRounds ?: 10)
    } ?: currentRound
    // Show at least 5 rounds after current, or up to the furthest effect
    return max(currentRound + 5, effectMaxRound)
}

/**
 * Data class to represent an effect's visual range.
 */
private data class EffectRange(
    val effect: Effect,
    val startRound: Int,
    val endRound: Int,
    val isIndefinite: Boolean,
)

/**
 * Group overlapping effects together for visual stacking.
 */
private fun groupOverlappingEffects(effectRanges: List<EffectRange>): List<List<EffectRange>> {
    if (effectRanges.isEmpty()) return emptyList()
    
    val groups = mutableListOf<List<EffectRange>>()
    val sorted = effectRanges.sortedBy { it.startRound }
    
    var currentGroup = mutableListOf<EffectRange>()
    for (range in sorted) {
        if (currentGroup.isEmpty()) {
            currentGroup.add(range)
        } else {
            // Check if this range overlaps with any in current group
            val overlaps = currentGroup.any { existing ->
                range.startRound <= existing.endRound && range.endRound >= existing.startRound
            }
            
            if (overlaps) {
                currentGroup.add(range)
            } else {
                groups.add(currentGroup)
                currentGroup = mutableListOf(range)
            }
        }
    }
    if (currentGroup.isNotEmpty()) {
        groups.add(currentGroup)
    }
    
    return groups
}

/**
 * Get color for an effect based on its type.
 * Uses centralized EffectColor model for consistent colors.
 * 
 * Default mapping:
 * - CONDITION -> ERROR (red)
 * - TIMER -> PRIMARY (theme color)
 */
private fun getEffectColor(effectType: EffectType, colorScheme: androidx.compose.material3.ColorScheme): Color {
    val effectColor = when (effectType) {
        EffectType.CONDITION -> EffectColor.ERROR
        EffectType.TIMER -> EffectColor.PRIMARY
    }
    return effectColor.resolveColor(colorScheme)
}
