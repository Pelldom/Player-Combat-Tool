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
import androidx.compose.ui.unit.dp
import com.playercombatassistant.pca.effects.GenericEffect
import com.playercombatassistant.pca.effects.toColor
import kotlin.math.max
import kotlin.math.min

/**
 * A vertical bar that displays combat rounds with tick marks.
 *
 * Features:
 * - Sliding window of rounds based on current round
 * - Visible rounds: from (currentRound - 2) to (currentRound + 7)
 * - Does not render rounds < 1
 * - Even spacing between rounds
 * - Current round visually emphasized
 */
@Composable
fun RoundTrackerBar(
    currentRound: Int,
    effects: List<GenericEffect>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    
    // Calculate visible round range (sliding window)
    val minRound = maxOf(1, currentRound - 2)
    val maxRound = currentRound + 7
    val visibleRounds = (minRound..maxRound).toList()
    val totalVisibleRounds = visibleRounds.size
    
    Canvas(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .semantics {
                contentDescription = "Round tracker: Current round $currentRound, showing rounds $minRound to $maxRound"
            },
    ) {
        val canvasHeight = size.height
        val canvasWidth = size.width
        
        // Draw subtle background
        drawRect(
            color = colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.3f else 0.2f),
            topLeft = Offset.Zero,
            size = Size(width = canvasWidth, height = canvasHeight),
        )
        
        // Calculate spacing between ticks
        val tickWidth = 4.dp.toPx()
        val tickSpacing = if (totalVisibleRounds > 1) {
            (canvasHeight - tickWidth) / (totalVisibleRounds - 1)
        } else {
            0f
        }
        
        // Helper function to convert round number to Y position
        fun roundToY(round: Int): Float {
            val index = visibleRounds.indexOf(round)
            if (index < 0) return -1f // Round not in visible window
            return if (index == 0) {
                0f
            } else {
                index * tickSpacing
            }.coerceIn(0f, canvasHeight - tickWidth)
        }
        
        // Filter effects that overlap with visible window
        val overlappingEffects = effects.filter { effect ->
            val effectEnd = effect.endRound ?: maxRound // Indefinite effects extend to maxRound
            effect.startRound <= maxRound && effectEnd >= minRound
        }
        
        // Group overlapping effects for horizontal offset
        val effectGroups = groupOverlappingEffects(overlappingEffects, minRound, maxRound)
        
        // Draw effect spans (behind ticks)
        val effectBarWidth = canvasWidth * 0.6f // Use 60% of width for effects
        
        effectGroups.forEach { group ->
            val maxOverlaps = group.size
            val effectSpacing = if (maxOverlaps > 1) {
                effectBarWidth / maxOverlaps
            } else {
                effectBarWidth
            }
            
            group.forEachIndexed { groupIndex, effect ->
                val effectStart = max(effect.startRound, minRound)
                val effectEnd = effect.endRound?.let { min(it, maxRound) } ?: maxRound
                
                val startY = roundToY(effectStart)
                val endY = roundToY(effectEnd)
                
                if (startY >= 0f && endY >= 0f) {
                    val offsetX = if (maxOverlaps > 1) {
                        groupIndex * effectSpacing
                    } else {
                        0f
                    }
                    
                    val effectColor = effect.colorId.toColor(colorScheme)
                    
                    // Adjust alpha for better contrast in light/dark mode
                    val fillAlpha = if (isDark) 0.4f else 0.25f
                    val borderAlpha = if (isDark) 0.9f else 0.8f
                    
                    // Draw effect span with semi-transparent fill
                    drawRect(
                        color = effectColor.copy(alpha = fillAlpha),
                        topLeft = Offset(x = offsetX, y = startY),
                        size = Size(width = effectSpacing, height = endY - startY),
                    )
                    
                    // Draw effect border with better visibility
                    drawRect(
                        color = effectColor.copy(alpha = borderAlpha),
                        topLeft = Offset(x = offsetX, y = startY),
                        size = Size(width = effectSpacing, height = endY - startY),
                        style = Stroke(width = 1.5f.dp.toPx()),
                    )
                }
            }
        }
        
        // Draw round ticks (on top of effects)
        visibleRounds.forEachIndexed { index, round ->
            val y = if (index == 0) {
                0f
            } else {
                index * tickSpacing
            }.coerceIn(0f, canvasHeight - tickWidth)
            
            val isCurrentRound = round == currentRound
            
            if (isCurrentRound) {
                // Draw background highlight for current round
                val highlightHeight = tickWidth * 2.5f
                val highlightY = (y - (highlightHeight - tickWidth) / 2).coerceAtLeast(0f)
                drawRect(
                    color = colorScheme.primaryContainer.copy(alpha = if (isDark) 0.5f else 0.4f),
                    topLeft = Offset(x = 0f, y = highlightY),
                    size = Size(width = canvasWidth, height = highlightHeight.coerceAtMost(canvasHeight - highlightY)),
                )
                
                // Draw accent line for current round
                drawRect(
                    color = colorScheme.primary,
                    topLeft = Offset(x = 0f, y = y),
                    size = Size(width = 2.dp.toPx(), height = tickWidth * 1.5f),
                )
            }
            
            // Tick colors with better contrast
            val tickColor = if (isCurrentRound) {
                colorScheme.primary
            } else {
                colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.6f else 0.5f)
            }
            
            val tickSize = if (isCurrentRound) {
                tickWidth * 1.8f
            } else {
                tickWidth
            }
            
            // Draw tick mark
            drawRect(
                color = tickColor,
                topLeft = Offset(x = canvasWidth - tickSize, y = y),
                size = Size(width = tickSize, height = tickSize),
            )
        }
    }
}

/**
 * Group effects that overlap in time for horizontal offset rendering.
 * Effects that overlap are placed in the same group and offset horizontally.
 * 
 * Algorithm: For each effect, find all other effects that overlap with it,
 * and group them together. Each group represents effects that need horizontal offset.
 */
private fun groupOverlappingEffects(
    effects: List<GenericEffect>,
    minRound: Int,
    maxRound: Int,
): List<List<GenericEffect>> {
    if (effects.isEmpty()) return emptyList()
    
    val groups = mutableListOf<MutableList<GenericEffect>>()
    val processed = mutableSetOf<GenericEffect>()
    
    for (effect in effects) {
        if (processed.contains(effect)) continue
        
        val group = mutableListOf<GenericEffect>()
        val effectEnd = effect.endRound ?: maxRound
        val effectStart = effect.startRound
        
        // Find all effects that overlap with this effect
        for (other in effects) {
            if (processed.contains(other)) continue
            
            val otherEnd = other.endRound ?: maxRound
            val otherStart = other.startRound
            
            // Check if effects overlap
            val overlaps = effectStart <= otherEnd && effectEnd >= otherStart
            
            if (overlaps) {
                group.add(other)
                processed.add(other)
            }
        }
        
        if (group.isNotEmpty()) {
            groups.add(group)
        }
    }
    
    return groups
}
