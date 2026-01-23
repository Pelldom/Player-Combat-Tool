package com.playercombatassistant.pca.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TabletLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Placeholder tablet structure. Content stays identical to phone for now.
    Row(modifier = modifier) {
        // Two-pane structure so resizing is visually confirmable without adding extra UI/logic.
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

