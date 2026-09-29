package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
 * Uses uniform selected item widths and coordinated spring animations to ensure the dock container
 * remains perfectly stable without sub-pixel jitter or 1px resizing when switching options.
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

    // Keep optimistic state synchronized when destination changes from gestures or deep links
    LaunchedEffect(currentDestination) {
        val matching = bottomNavScreens.firstOrNull { screen ->
            currentDestination?.hierarchy?.any { it.hasRoute(screen.destination::class) } == true
        }
        if (matching != null) {
            if (selectedOrdinal != matching.ordinal) {
                selectedOrdinal = matching.ordinal
            }
        } else if (currentDestination?.hierarchy?.any { it.hasRoute(SettingsDestination::class) } == true) {
            if (selectedOrdinal != -1) {
                selectedOrdinal = -1
            }
        }
    }

    val selectTab: (BottomNavScreen) -> Unit = { screen ->
        onItemClick()
        val isScreenSelected =
            currentDestination?.hierarchy?.any {
                it.hasRoute(screen.destination::class)
            } == true
        if (isScreenSelected) {
            reloadDestinationIfNeeded(screen.destination::class)
        } else {
            // Immediate 0ms visual feedback on tap
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

    // Uniform pill width across all tabs keeps the total dock width perfectly constant while switching
    val selectedItemWidth =
        when {
            bottomNavScreens.size <= 3 -> 106.dp
            bottomNavScreens.size == 4 -> 98.dp
            else -> 90.dp
        }

    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Main Navigation Dock Pill (Left)
        Surface(
            shape =
                RoundedCornerShape(
                    topStart = 29.dp,
                    bottomStart = 29.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                ),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier =
                Modifier
                    .wrapContentWidth()
                    .height(58.dp)
                    .animateContentSize(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = 500f,
                            ),
                    ),
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                bottomNavScreens.forEach { screen ->
                    val selected = selectedOrdinal == screen.ordinal
                    AppBottomDockItem(
                        screen = screen,
                        selected = selected,
                        selectedWidth = selectedItemWidth,
                        labelStyle = labelStyle,
                        onSelect = selectTab,
                    )
                }
            }
        }

        // Settings Button Pill (Right) - Compact slice with smoothed cut edge & morph animation
        val isSettingsSelected = currentDestination?.hierarchy?.any { it.hasRoute(SettingsDestination::class) } == true
        val settingsInteractionSource = remember { MutableInteractionSource() }
        val isSettingsPressed by settingsInteractionSource.collectIsPressedAsState()

        val settingsPressScale by animateFloatAsState(
            targetValue = if (isSettingsPressed) 0.90f else 1.0f,
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

        // Whole pill background fills with primaryContainer when selected
        val settingsSurfaceColor by animateColorAsState(
            targetValue =
                if (isSettingsSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            label = "settingsSurfaceColor",
        )

        // Dynamic corner morph on the cut face (16dp -> 24dp)
        val settingsCutCorner by animateDpAsState(
            targetValue = if (isSettingsSelected) 24.dp else 16.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
            label = "settingsCutCorner",
        )

        // Tactile width morph (54dp -> 58dp)
        val settingsWidth by animateDpAsState(
            targetValue = if (isSettingsSelected) 58.dp else 54.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
            label = "settingsWidth",
        )

        // Gear icon fluid 90-degree elastic turn on selection
        val gearRotation by animateFloatAsState(
            targetValue = if (isSettingsSelected) 90f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 320f),
            label = "settingsGearRotation",
        )
        val gearScale by animateFloatAsState(
            targetValue = if (isSettingsSelected) 1.08f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
            label = "settingsGearScale",
        )

        Surface(
            shape =
                RoundedCornerShape(
                    topStart = settingsCutCorner,
                    bottomStart = settingsCutCorner,
                    topEnd = 29.dp,
                    bottomEnd = 29.dp,
                ),
            color = settingsSurfaceColor,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            modifier =
                Modifier
                    .height(58.dp)
                    .width(settingsWidth),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
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
                                selectedOrdinal = -1
                                navController.navigate(SettingsDestination) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides settingsContentColor) {
                    Icon(
                        imageVector = SimpIcons.Settings,
                        contentDescription = "Settings",
                        modifier =
                            Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    rotationZ = gearRotation
                                    scaleX = gearScale
                                    scaleY = gearScale
                                },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomDockItem(
    screen: BottomNavScreen,
    selected: Boolean,
    selectedWidth: Dp,
    labelStyle: androidx.compose.ui.text.TextStyle,
    onSelect: (BottomNavScreen) -> Unit,
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
    val pillBackgroundColor by animateColorAsState(
        targetValue =
            if (selected) {
                pillColor
            } else {
                androidx.compose.ui.graphics.Color.Transparent
            },
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "dockPillBackgroundColor",
    )

    // Coordinated item width animation: as one collapses from selectedWidth to 44dp,
    // the other expands from 44dp to selectedWidth with the exact same spec, ensuring
    // their sum is constant and eliminating 1px resize jitter completely.
    val itemWidth by animateDpAsState(
        targetValue = if (selected) selectedWidth else 44.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 500f,
        ),
        label = "dockItemWidth",
    )

    Box(
        modifier =
            Modifier
                .height(44.dp)
                .width(itemWidth)
                .clip(CircleShape)
                .background(pillBackgroundColor)
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onSelect(screen) },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                screen.icon()
            }
            AnimatedVisibility(
                visible = selected,
                enter =
                    fadeIn(tween(140, delayMillis = 40)) +
                        expandHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f),
                            expandFrom = Alignment.Start,
                            clip = true,
                        ),
                exit =
                    fadeOut(tween(90)) +
                        shrinkHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f),
                            shrinkTowards = Alignment.Start,
                            clip = true,
                        ),
            ) {
                Text(
                    text = stringResource(screen.title),
                    style = labelStyle,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
