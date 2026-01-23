package com.playercombatassistant.pca.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.playercombatassistant.pca.ui.screens.HistoryScreen
import com.playercombatassistant.pca.ui.screens.SettingsScreen
import com.playercombatassistant.pca.ui.screens.CombatScreen

@Composable
fun PcaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = PcaRoutes.COMBAT,
        modifier = modifier,
    ) {
        composable(PcaRoutes.COMBAT) { CombatScreen() }
        composable(PcaRoutes.HISTORY) { HistoryScreen() }
        composable(PcaRoutes.SETTINGS) { SettingsScreen() }
    }
}

