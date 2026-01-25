package com.playercombatassistant.pca.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TabletLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Tablet layout: content uses full screen width without constraints
    Box(modifier = modifier.fillMaxSize()) {
        content()
    }
}

