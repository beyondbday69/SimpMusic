package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.maxrave.simpmusic.ui.icon.Settings
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.AnalyticsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.SettingsDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.MixForYouDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.*
import kotlin.reflect.KClass

/**
 * A modern, clean floating bottom-center dock for phones, foldables, and tablets.
 * Provides quick access to all core navigation destinations and settings in a sleek,
 * elevated pill with lightweight, high-performance animations (60/120 FPS).
 */
@Composable
fun AppBottomDock(
    startDestination: Any = HomeDestination,
    navController: NavController,
    showAnalyticsTab: Boolean = false,
    showMixForYouTab: Boolean = false,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit = { _ -> },
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val bottomNavScreens =
        remember(showAnalyticsTab, showMixForYouTab) {
            listOfNotNull(
                BottomNavScreen.Home,
                BottomNavScreen.Search,
                BottomNavScreen.Library,
                BottomNavScreen.MixForYou.takeIf { showMixForYouTab },
                BottomNavScreen.Analytics.takeIf { showAnalyticsTab },
            )
        }

    val currentDestination = currentBackStackEntry?.destination
    val activeScreen = remember(currentDestination, bottomNavScreens) {
        bottomNavScreens.firstOrNull { screen ->
            currentDestination?.hierarchy?.any { it.hasRoute(screen.destination::class) } == true
        } ?: bottomNavScreens.firstOrNull() ?: BottomNavScreen.Home
    }

    val selectTab: (BottomNavScreen) -> Unit = { screen ->
        if (activeScreen.ordinal == screen.ordinal) {
            if (currentDestination?.hierarchy?.any {
                    it.hasRoute(screen.destination::class)
                } == true
            ) {
                reloadDestinationIfNeeded(screen.destination::class)
            } else {
                navController.navigate(screen.destination)
            }
        } else {
            navController.navigate(screen.destination) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        modifier =
            Modifier
                .wrapContentWidth()
                .height(58.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            bottomNavScreens.forEach { screen ->
                val selected = activeScreen.ordinal == screen.ordinal
                val indicatorColor by animateColorAsState(
                    targetValue =
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        },
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "dockIndicatorColor",
                )
                val contentColor by animateColorAsState(
                    targetValue =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "dockContentColor",
                )

                Box(
                    modifier =
                        Modifier
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                            .clickable { selectTab(screen) }
                            .padding(horizontal = if (selected) 14.dp else 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CompositionLocalProvider(LocalContentColor provides contentColor) {
                            screen.icon()
                        }
                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn(tween(160, delayMillis = 30)) +
                                expandHorizontally(
                                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                                    expandFrom = Alignment.Start,
                                    clip = true,
                                ),
                            exit = fadeOut(tween(120)) +
                                shrinkHorizontally(
                                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                                    shrinkTowards = Alignment.Start,
                                    clip = true,
                                ),
                        ) {
                            Text(
                                text = stringResource(screen.title),
                                style = typo().labelMedium,
                                color = contentColor,
                                maxLines = 1,
                                modifier = Modifier.padding(start = 2.dp),
                            )
                        }
                    }
                }
            }

            VerticalDivider(
                modifier =
                    Modifier
                        .height(20.dp)
                        .padding(horizontal = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )

            // Dedicated Settings access right in the dock
            val isSettingsSelected =
                currentDestination?.hierarchy?.any {
                    it.hasRoute(SettingsDestination::class)
                } == true
            val settingsIndicator by animateColorAsState(
                targetValue =
                    if (isSettingsSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                label = "settingsIndicatorColor",
            )
            val settingsContentColor by animateColorAsState(
                targetValue =
                    if (isSettingsSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                label = "settingsContentColor",
            )

            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(settingsIndicator)
                        .clickable {
                            navController.navigate(SettingsDestination) {
                                launchSingleTop = true
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides settingsContentColor) {
                    Icon(
                        imageVector = SimpIcons.Settings,
                        contentDescription = stringResource(Res.string.settings),
                    )
                }
            }
        }
    }
}
