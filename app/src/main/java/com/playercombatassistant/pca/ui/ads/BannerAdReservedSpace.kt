package com.playercombatassistant.pca.ui.ads

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reserved bottom area for an always-on banner ad (v1 placeholder).
 * Intentionally contains no ad implementation.
 */
@Composable
fun BannerAdReservedSpace(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    )
}

