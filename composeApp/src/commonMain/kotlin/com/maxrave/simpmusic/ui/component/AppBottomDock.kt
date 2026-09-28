package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
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
 * A modern, clean floating bottom-center dock.
 * Uses graphicsLayer-only animations to avoid triggering layout/measure passes on every frame.
 * The selected-item pill is implemented via AnimatedVisibility expand/shrink — no
 * onGloballyPositioned state writes — so there is no recomposition during animation.
 */
@Composable
fun AppBottomDock(
    startDestination: Any = HomeDestination,
    navController: NavController,
    showAnalyticsTab: Boolean = false,
    showMixForYouTab: Boolean = false,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit = { _ -> },
    onItemClick: () -> Unit = {},
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

    // Immediate optimistic selection state for 0ms instantaneous touch response
    var selectedOrdinal by rememberSaveable {
        mutableIntStateOf(
            when (startDestination) {
                is HomeDestination -> BottomNavScreen.Home.ordinal
                is SearchDestination -> BottomNavScreen.Search.ordinal
                is LibraryDestination -> BottomNavScreen.Library.ordinal
                is AnalyticsDestination -> BottomNavScreen.Analytics.ordinal
                is MixForYouDestination -> BottomNavScreen.MixForYou.ordinal
                else -> BottomNavScreen.Home.ordinal
            }
        )
    }
    var previousOrdinal by rememberSaveable {
        mutableIntStateOf(selectedOrdinal)
    }

    // Keep optimistic state synchronized when destination changes from gestures or deep links
    LaunchedEffect(currentDestination) {
        val matching = bottomNavScreens.firstOrNull { screen ->
            currentDestination?.hierarchy?.any { it.hasRoute(screen.destination::class) } == true
        }
        if (matching != null) {
            if (selectedOrdinal != matching.ordinal) {
                previousOrdinal = selectedOrdinal
                selectedOrdinal = matching.ordinal
            }
        } else if (currentDestination?.hierarchy?.any { it.hasRoute(SettingsDestination::class) } == true) {
            if (selectedOrdinal != -1) {
                previousOrdinal = selectedOrdinal
                selectedOrdinal = -1
            }
        }
    }

    val selectTab: (BottomNavScreen) -> Unit = { screen ->
        onItemClick()
        if (selectedOrdinal == screen.ordinal) {
            if (currentDestination?.hierarchy?.any {
                    it.hasRoute(screen.destination::class)
                } == true
            ) {
                reloadDestinationIfNeeded(screen.destination::class)
            } else {
                navController.navigate(screen.destination)
            }
        } else {
            // Immediate 0ms visual feedback on tap
            previousOrdinal = selectedOrdinal
            selectedOrdinal = screen.ordinal
            navController.navigate(screen.destination) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Cache typo once per composition — avoids re-allocating Typography on every item in the loop
    val labelStyle = typo().labelMedium

    val currentScreenIndex = bottomNavScreens.indexOfFirst { it.ordinal == selectedOrdinal }.let { if (it == -1) bottomNavScreens.size else it }
    val previousScreenIndex = bottomNavScreens.indexOfFirst { it.ordinal == previousOrdinal }.let { if (it == -1) bottomNavScreens.size else it }
    val isMovingForward = currentScreenIndex >= previousScreenIndex

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier =
            Modifier
                .wrapContentWidth()
                .height(58.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            bottomNavScreens.forEach { screen ->
                val selected = selectedOrdinal == screen.ordinal
                AppBottomDockItem(
                    screen = screen,
                    selected = selected,
                    isMovingForward = isMovingForward,
                    labelStyle = labelStyle,
                    onSelect = selectTab
                )
            }

            // Settings tab separator (invisible spacing)
            Box(modifier = Modifier.size(4.dp))

            val isSettingsSelected = currentDestination?.hierarchy?.any { it.hasRoute(SettingsDestination::class) } == true
            val settingsInteractionSource = remember { MutableInteractionSource() }
            val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()

            val settingsPressScale by animateFloatAsState(
                targetValue = if (isSettingsPressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 600f),
                label = "settingsPressScale",
            )
            val settingsContentColor by animateColorAsState(
                targetValue =
                    if (isSettingsSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "settingsContentColor",
            )

            val settingsPillColor = MaterialTheme.colorScheme.primaryContainer
            Box(
                modifier =
                    Modifier
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(if (isSettingsSelected) settingsPillColor else androidx.compose.ui.graphics.Color.Transparent)
                        .graphicsLayer {
                            scaleX = settingsPressScale
                            scaleY = settingsPressScale
                        }
                        .clickable(
                            interactionSource = settingsInteractionSource,
                            indication = null,
                        ) {
                            onItemClick()
                            if (isSettingsSelected) {
                                reloadDestinationIfNeeded(SettingsDestination::class)
                            } else {
                                previousOrdinal = selectedOrdinal
                                selectedOrdinal = -1
                                navController.navigate(SettingsDestination) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides settingsContentColor) {
                    Icon(SimpIcons.Settings, contentDescription = "Settings")
                }
            }
        }
    }
}

@Composable
private fun AppBottomDockItem(
    screen: BottomNavScreen,
    selected: Boolean,
    isMovingForward: Boolean,
    labelStyle: androidx.compose.ui.text.TextStyle,
    onSelect: (BottomNavScreen) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 600f),
        label = "dockPressScale",
    )
    val contentColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "dockContentColor",
    )

    val pillColor = MaterialTheme.colorScheme.primaryContainer
    val expandFrom = if (isMovingForward) Alignment.Start else Alignment.End
    val shrinkTowards = if (isMovingForward) Alignment.Start else Alignment.End
    Box(
        modifier =
            Modifier
                .height(44.dp)
                .clip(CircleShape)
                .background(if (selected) pillColor else androidx.compose.ui.graphics.Color.Transparent)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onSelect(screen) }
                .padding(horizontal = if (selected) 14.dp else 10.dp),
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
                enter =
                    fadeIn(tween(160)) +
                        expandHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f),
                            expandFrom = expandFrom,
                            clip = true,
                        ),
                exit =
                    fadeOut(tween(120)) +
                        shrinkHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f),
                            shrinkTowards = shrinkTowards,
                            clip = true,
                        ),
            ) {
                Text(
                    text = stringResource(screen.title),
                    style = labelStyle,
                    color = contentColor,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }
    }
}
