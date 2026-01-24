package com.playercombatassistant.pca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.Modifier
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playercombatassistant.pca.R
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.playercombatassistant.pca.ui.adaptive.AdaptiveScaffold
import com.playercombatassistant.pca.ui.ads.BannerAdReservedSpace
import com.playercombatassistant.pca.ui.chrome.PcaTopAppBar
import com.playercombatassistant.pca.ui.navigation.PcaNavHost
import com.playercombatassistant.pca.ui.navigation.PcaRoutes
import com.playercombatassistant.pca.ui.state.LocalUIStateViewModel
import com.playercombatassistant.pca.ui.state.UIStateViewModel
import com.playercombatassistant.pca.ui.theme.PCATheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PCATheme {
                // Create UIStateViewModel at Activity level for session-only persistence
                val uiStateViewModel: UIStateViewModel = viewModel()
                
                CompositionLocalProvider(LocalUIStateViewModel provides uiStateViewModel) {
                    val windowSizeClass = calculateWindowSizeClass(activity = this)
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: PcaRoutes.COMBAT
                    val showBackArrow = currentRoute == PcaRoutes.HISTORY || currentRoute == PcaRoutes.SETTINGS
                    val title = when (currentRoute) {
                        PcaRoutes.HISTORY -> "History"
                        PcaRoutes.SETTINGS -> "Settings"
                        else -> stringResource(R.string.app_name)
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AdaptiveScaffold(
                        windowSizeClass = windowSizeClass,
                        topBar = {
                            PcaTopAppBar(
                                title = title,
                                onHistoryClick = {
                                    navController.navigate(PcaRoutes.HISTORY) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                    }
                                },
                                onSettingsClick = {
                                    navController.navigate(PcaRoutes.SETTINGS) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                    }
                                },
                                showBackArrow = showBackArrow,
                                onBackClick = { navController.popBackStack() },
                            )
                        },
                        bottomBar = {
                            BannerAdReservedSpace()
                        },
                    ) { contentModifier ->
                        PcaNavHost(
                            navController = navController,
                            modifier = contentModifier,
                        )
                    }
                    }
                }
            }
        }
    }
}
