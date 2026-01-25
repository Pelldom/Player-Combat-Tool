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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playercombatassistant.pca.effects.GenericEffect
import com.playercombatassistant.pca.effects.toColor
import kotlin.math.max
import kotlin.math.min

/**
 * A vertical bar that displays combat rounds with numbered labels.
 *
 * Features:
 * - Shows a sliding window of 10 rounds based on current round
 * - Numbered labels instead of hash marks
 * - Denser spacing to fit 10 rounds
 * - Current round: bold, larger, high-contrast, highlighted
 * - Future rounds: neutral styling
 * - Past rounds: not displayed (windowed out)
 */
@Composable
fun RoundTrackerBar(
    currentRound: Int,
    effects: List<GenericEffect>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()
    
    // Calculate sliding window: show currentRound through currentRound + 9 (10 rounds total)
    val minRound = currentRound
    val maxRound = currentRound + 9
    val visibleRounds = (minRound..maxRound).toList()
    val totalVisibleRounds = visibleRounds.size
    
    Canvas(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .semantics {
                contentDescription = "Round tracker: Current round $currentRound, showing rounds $minRound through $maxRound"
            },
    ) { // This lambda has DrawScope as receiver
        val canvasHeight = size.height
        val canvasWidth = size.width
        
        // Draw subtle background
        drawRect(
            color = colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.3f else 0.2f),
            topLeft = Offset.Zero,
            size = Size(width = canvasWidth, height = canvasHeight),
        )
        
        // Calculate spacing between rounds (denser to fit 10 rounds)
        val roundSpacing = if (totalVisibleRounds > 1) {
            canvasHeight / totalVisibleRounds
        } else {
            canvasHeight
        }
        
        // Helper function to convert round number to Y position (centered in its slot)
        fun roundToY(round: Int): Float {
            val index = visibleRounds.indexOf(round)
            if (index < 0) return -1f // Round not in visible window
            return (index * roundSpacing) + (roundSpacing / 2f)
        }
        
        // Filter effects that overlap with visible window
        // Effects are visually clamped to the visible window for display purposes
        val overlappingEffects = effects.filter { effect ->
            val effectEnd = effect.endRound ?: Int.MAX_VALUE // Indefinite effects extend beyond window
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
                // Visually clamp effect spans to the visible window
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
        
        // Draw round numbers (on top of effects)
        visibleRounds.forEachIndexed { index, round ->
            val y = (index * roundSpacing) + (roundSpacing / 2f)
            
            val isCurrentRound = round == currentRound
            // In a windowed view, all visible rounds are current or future (past rounds are windowed out)
            
            // Visual states
            val textSize = if (isCurrentRound) {
                14.sp
            } else {
                12.sp
            }
            
            val textColor = when {
                isCurrentRound -> colorScheme.primary
                else -> colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.7f else 0.7f)
            }
            
            val fontWeight = if (isCurrentRound) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
            
            if (isCurrentRound) {
                // Draw background highlight for current round
                val highlightHeight = roundSpacing * 0.8f
                val highlightY = y - (highlightHeight / 2f)
                drawRect(
                    color = colorScheme.primaryContainer.copy(alpha = if (isDark) 0.5f else 0.4f),
                    topLeft = Offset(x = 0f, y = highlightY.coerceAtLeast(0f)),
                    size = Size(
                        width = canvasWidth,
                        height = highlightHeight.coerceAtMost(canvasHeight - highlightY.coerceAtLeast(0f))
                    ),
                )
                
                // Draw accent line for current round
                drawRect(
                    color = colorScheme.primary,
                    topLeft = Offset(x = 0f, y = y - (roundSpacing * 0.15f)),
                    size = Size(width = 2.dp.toPx(), height = roundSpacing * 0.3f),
                )
            }
            
            // Measure and draw text
            val textLayoutResult = textMeasurer.measure(
                text = round.toString(),
                style = TextStyle(
                    color = textColor,
                    fontSize = textSize,
                    fontWeight = fontWeight,
                ),
            )
            
            // Draw round number centered horizontally
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = (canvasWidth - textLayoutResult.size.width) / 2f,
                    y = y - (textLayoutResult.size.height / 2f)
                ),
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
        // For indefinite effects, use Int.MAX_VALUE to represent unbounded end
        val effectEnd = effect.endRound ?: Int.MAX_VALUE
        val effectStart = effect.startRound
        
        // Find all effects that overlap with this effect
        for (other in effects) {
            if (processed.contains(other)) continue
            
            // For indefinite effects, use Int.MAX_VALUE to represent unbounded end
            val otherEnd = other.endRound ?: Int.MAX_VALUE
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
