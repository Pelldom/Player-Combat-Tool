package com.playercombatassistant.pca.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.playercombatassistant.pca.R

/**
 * Semantic icon for improvised-weapon related UI.
 *
 * - Uses the full-color vector by default.
 * - Uses the monochrome variant for secondary/disabled presentation when desired.
 * - Tint is always supplied via [MaterialTheme.colorScheme] (no hardcoded colors).
 */
@Composable
fun ImprovisedWeaponIcon(
    contentDescription: String,
    modifier: Modifier = Modifier,
    useMonochrome: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val painter = painterResource(
        id = if (useMonochrome) R.drawable.ic_improvised_weapon_monochrome else R.drawable.ic_improvised_weapon,
    )
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

