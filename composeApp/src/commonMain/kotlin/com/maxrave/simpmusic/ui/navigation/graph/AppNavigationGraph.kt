package com.maxrave.simpmusic.ui.navigation.graph

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maxrave.simpmusic.ui.navigation.destination.home.AnalyticsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.WrappedDestination
import com.maxrave.simpmusic.ui.theme.ForceDarkContent
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.MixForYouDestination
import com.maxrave.simpmusic.ui.navigation.destination.player.FullscreenDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination
import com.maxrave.simpmusic.ui.screen.home.HomeScreen
import com.maxrave.simpmusic.ui.screen.home.analytics.AnalyticsScreen
import com.maxrave.simpmusic.ui.screen.home.wrapped.WrappedScreen
import com.maxrave.simpmusic.ui.screen.library.LibraryScreen
import com.maxrave.simpmusic.ui.screen.library.MixForYouScreen
import com.maxrave.simpmusic.ui.screen.other.SearchScreen
import com.maxrave.simpmusic.ui.screen.player.FullscreenPlayer

private fun getTopLevelTabIndex(destination: NavDestination?): Int {
    if (destination == null) return -1
    return when {
        destination.hierarchy.any { it.hasRoute(HomeDestination::class) } -> 0
        destination.hierarchy.any { it.hasRoute(SearchDestination::class) } -> 1
        destination.hierarchy.any { it.hasRoute(LibraryDestination::class) } -> 2
        destination.hierarchy.any { it.hasRoute(MixForYouDestination::class) } -> 3
        destination.hierarchy.any { it.hasRoute(AnalyticsDestination::class) } -> 4
        else -> -1
    }
}

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppNavigationGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    startDestination: Any = HomeDestination,
    hideNavBar: () -> Unit = { },
    showNavBar: (shouldShowNowPlayingSheet: Boolean) -> Unit = { },
    showNowPlayingSheet: () -> Unit = {},
    onScrolling: (onTop: Boolean) -> Unit = {},
) {
    val topLevelEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        val initialIdx = getTopLevelTabIndex(initialState.destination)
        val targetIdx = getTopLevelTabIndex(targetState.destination)
        if (initialIdx >= 0 && targetIdx >= 0 && initialIdx != targetIdx) {
            val direction = if (targetIdx > initialIdx) 1 else -1
            slideInHorizontally(
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> (fullWidth * 0.08f * direction).toInt() },
            ) + fadeIn(animationSpec = tween(160, easing = FastOutSlowInEasing))
        } else {
            fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing))
        }
    }
    val topLevelExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        val initialIdx = getTopLevelTabIndex(initialState.destination)
        val targetIdx = getTopLevelTabIndex(targetState.destination)
        if (initialIdx >= 0 && targetIdx >= 0 && initialIdx != targetIdx) {
            val direction = if (targetIdx > initialIdx) -1 else 1
            slideOutHorizontally(
                animationSpec = tween(160, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> (fullWidth * 0.08f * direction).toInt() },
            ) + fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing))
        } else {
            fadeOut(animationSpec = tween(140, easing = FastOutSlowInEasing))
        }
    }

    NavHost(
        navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it / 4 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { -it / 4 }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { -it / 4 }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it / 4 }
        },
    ) {
        // Bottom bar destinations with snappy, lightweight fade transitions
        composable<HomeDestination>(
            enterTransition = topLevelEnterTransition,
            exitTransition = topLevelExitTransition,
            popEnterTransition = topLevelEnterTransition,
            popExitTransition = topLevelExitTransition,
        ) {
            HomeScreen(
                onScrolling = onScrolling,
                navController = navController,
            )
        }
        composable<SearchDestination>(
            enterTransition = topLevelEnterTransition,
            exitTransition = topLevelExitTransition,
            popEnterTransition = topLevelEnterTransition,
            popExitTransition = topLevelExitTransition,
        ) {
            SearchScreen(
                navController = navController,
            )
        }
        composable<LibraryDestination>(
            enterTransition = topLevelEnterTransition,
            exitTransition = topLevelExitTransition,
            popEnterTransition = topLevelEnterTransition,
            popExitTransition = topLevelExitTransition,
        ) {
            LibraryScreen(
                innerPadding = innerPadding,
                navController = navController,
                onScrolling = onScrolling,
            )
        }
        // Only reachable as a tab while signed in to YouTube
        composable<MixForYouDestination>(
            enterTransition = topLevelEnterTransition,
            exitTransition = topLevelExitTransition,
            popEnterTransition = topLevelEnterTransition,
            popExitTransition = topLevelExitTransition,
        ) {
            MixForYouScreen(
                innerPadding = innerPadding,
                navController = navController,
                onScrolling = onScrolling,
            )
        }
        // Only reachable as a tab while local tracking is enabled.
        composable<AnalyticsDestination>(
            enterTransition = topLevelEnterTransition,
            exitTransition = topLevelExitTransition,
            popEnterTransition = topLevelEnterTransition,
            popExitTransition = topLevelExitTransition,
        ) {
            ForceDarkContent {
                AnalyticsScreen(
                    navController = navController,
                    innerPadding = innerPadding,
                )
            }
        }
        // Reached only from the Analytics screen's entry banner, so it inherits that screen's
        // gate on local tracking. ForceDarkContent for a different reason than Analytics: the reel
        // is drawn on its own near-black ground whatever the user's theme is, because it is an
        // event and because every card is also a share image that has to survive leaving the app.
        composable<WrappedDestination> {
            ForceDarkContent {
                WrappedScreen(
                    navController = navController,
                    hideNavBar = hideNavBar,
                    // Deliberately not the fullscreen player's `showNavBar(true)` + open sheet:
                    // leaving the reel goes back to Analytics, and raising the Now Playing sheet
                    // over it would be a screen the user never asked for.
                    showNavBar = { showNavBar(false) },
                )
            }
        }
        composable<FullscreenDestination> {
            ForceDarkContent {
                FullscreenPlayer(
                    navController,
                    hideNavBar = hideNavBar,
                    showNavBar = {
                        showNavBar.invoke(true)
                        showNowPlayingSheet.invoke()
                    },
                )
            }
        }
        // Home screen graph
        homeScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // Library screen graph
        libraryScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // List screen graph
        listScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // Login screen graph
        loginScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomBar = hideNavBar,
            showBottomBar = {
                showNavBar(false)
            },
        )
    }
}