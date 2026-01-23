package com.playercombatassistant.pca.ui.adaptive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveScaffold(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
    ) { innerPadding ->
        CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
            val layoutModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            val contentModifier = Modifier.fillMaxSize()

            when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> PhoneLayout(
                    modifier = layoutModifier,
                    content = { content(contentModifier) },
                )

                WindowWidthSizeClass.Medium,
                WindowWidthSizeClass.Expanded -> TabletLayout(
                    modifier = layoutModifier,
                    content = { content(contentModifier) },
                )
                else -> TabletLayout(
                    modifier = layoutModifier,
                    content = { content(contentModifier) },
                )
            }
        }
    }
}

