package com.maxrave.simpmusic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.graphicsLayer
import com.maxrave.simpmusic.expect.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalPlatformContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.toUri
import com.maxrave.domain.data.player.GenericMediaItem
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.expect.Orientation
import com.maxrave.simpmusic.expect.currentOrientation
import com.maxrave.simpmusic.expect.openUrl
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.extension.copy
import com.maxrave.simpmusic.ui.component.AppBottomDock
import com.maxrave.simpmusic.ui.component.AppNavigationRail
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.AnalyticsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.NotificationDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.WrappedDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.MixForYouDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.player.FullscreenDestination
import com.maxrave.simpmusic.ui.navigation.graph.AppNavigationGraph
import com.maxrave.simpmusic.ui.screen.MiniPlayer
import com.maxrave.simpmusic.ui.screen.other.UnofficialBuildScreen
import com.maxrave.simpmusic.ui.screen.player.NowPlayingScreen
import com.maxrave.simpmusic.ui.screen.player.NowPlayingScreenContent
import com.maxrave.simpmusic.ui.screen.player.content.LocalNowPlayingArtworkBounds
import com.maxrave.simpmusic.ui.screen.player.content.LocalNowPlayingMorphProgress
import com.maxrave.simpmusic.ui.theme.AppTheme
import com.maxrave.simpmusic.ui.theme.ForceDarkContent
import com.maxrave.simpmusic.ui.theme.desktopPanelDark
import com.maxrave.simpmusic.ui.theme.desktopWindowDark
import com.maxrave.simpmusic.ui.theme.desktopWindowLight
import com.maxrave.simpmusic.ui.theme.fontFamily
import com.maxrave.simpmusic.ui.theme.parseThemeColorHex
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.utils.VersionManager
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.do_not_show_again
import simpmusic.composeapp.generated.resources.download
import simpmusic.composeapp.generated.resources.good_night
import simpmusic.composeapp.generated.resources.notification
import simpmusic.composeapp.generated.resources.settings
import simpmusic.composeapp.generated.resources.sleep_timer_off
import simpmusic.composeapp.generated.resources.this_app_needs_to_access_your_notification
import simpmusic.composeapp.generated.resources.this_link_is_not_supported
import simpmusic.composeapp.generated.resources.unknown
import simpmusic.composeapp.generated.resources.update_available
import simpmusic.composeapp.generated.resources.update_message
import simpmusic.composeapp.generated.resources.version_format
import simpmusic.composeapp.generated.resources.yes
import kotlin.time.ExperimentalTime
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.reflect.KClass

@Composable
fun AppMiniPlayer(
    isShowMiniPlayer: Boolean,
    isTablet: Boolean,
    backdrop: PlatformBackdrop,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onArtworkPositioned: ((Rect) -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = isShowMiniPlayer,
        enter = fadeIn(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(180)),
    ) {
        MiniPlayer(
            modifier = Modifier.padding(bottom = 6.dp),
            isTablet = isTablet,
            backdrop = backdrop,
            onClick = onClick,
            onClose = onClose,
            onArtworkPositioned = onArtworkPositioned,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalFoundationApi::class)
@Composable
fun App(
    viewModel: SharedViewModel = koinInject(),
    showDesktopNotificationPermissionDialog: Boolean = false,
    onDismissDesktopNotificationPermissionDialog: (doNotShowAgain: Boolean) -> Unit = {},
    onOpenDesktopNotificationSettings: (doNotShowAgain: Boolean) -> Unit = {},
) {
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val navController = rememberNavController()
    val isDesktopShell = getPlatform() == Platform.Desktop

    val sleepTimerStateState = viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val nowPlayingDataState = viewModel.nowPlayingState.collectAsStateWithLifecycle()
    val updateData by viewModel.updateResponse.collectAsStateWithLifecycle()
    val intent by viewModel.intent.collectAsStateWithLifecycle()
    val showNotificationPermissionDialog by viewModel.showNotificationPermissionDialog.collectAsStateWithLifecycle()

    val isLiquidGlassEnabled by viewModel.getEnableLiquidGlass().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    // Analytics only makes sense with local tracking on, so its tab follows that setting.
    val isLocalTrackingEnabled by viewModel.getLocalTrackingEnabled().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val showAnalyticsTab = isLocalTrackingEnabled == TRUE
    // Mix for you comes from the signed-in YouTube account, so its tab follows the session — the
    // same condition that used to hide the chip inside Library.
    val isYouTubeLoggedIn by viewModel.getYouTubeLoggedIn().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val showMixForYouTab = isYouTubeLoggedIn == TRUE

    val themeMode by viewModel.getThemeMode().collectAsStateWithLifecycle(DataStoreManager.THEME_MODE_DARK)
    val themeColorSource by viewModel.getThemeColorSource().collectAsStateWithLifecycle(DataStoreManager.THEME_COLOR_DEFAULT)
    val customThemeColorHex by viewModel.getCustomThemeColor().collectAsStateWithLifecycle(DataStoreManager.DEFAULT_THEME_COLOR_HEX)
    val isOfficialBuild by viewModel.isOfficialBuild.collectAsStateWithLifecycle()
    // MiniPlayer visibility: derived, never stored.
    //
    // This used to be a rememberSaveable Boolean written by a LaunchedEffect. Two things went
    // wrong with that. The effect only runs AFTER the first composition, so the first frame drew
    // whatever the initial value said — and rememberSaveable RESTORES a previously saved value,
    // so flipping that initial value from true to false changed nothing on a process that had
    // already saved true. The bar therefore showed, hid, and showed again on every start.
    //
    // Reading it straight from nowPlayingData removes both failure modes: there is no first-frame
    // guess to be wrong, and no saved copy to disagree with the source.
    val isShowMiniPlayer by remember {
        derivedStateOf {
            val item = nowPlayingDataState.value?.mediaItem
            item != null && item != GenericMediaItem.EMPTY
        }
    }

    // Now playing screen
    var isShowNowPlaylistScreen by rememberSaveable {
        mutableStateOf(false)
    }
    val miniPlayerArtworkBoundsState = remember { mutableStateOf<Rect?>(null) }
    val nowPlayingArtworkBoundsState = remember { mutableStateOf<Rect?>(null) }

    // Fullscreen
    var isInFullscreen by rememberSaveable {
        mutableStateOf(false)
    }

    var isNavBarVisible by rememberSaveable {
        mutableStateOf(true)
    }

    var shouldShowUpdateDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var isScrolledToTop by rememberSaveable {
        mutableStateOf(false)
    }


    LaunchedEffect(intent) {
        val intent = intent ?: return@LaunchedEffect
        val data = intent.data
        Logger.d("MainActivity", "onCreate: $data")
        if (data != null) {
            if (data == "simpmusic://notification".toUri()) {
                viewModel.setIntent(null)
                navController.navigate(
                    NotificationDestination,
                )
            } else if (data.scheme == "wordbyword" && data.host == "lastfm-auth") {
                // Last.fm sends the user back here after they approve access, carrying the request
                // token: wordbyword://lastfm-auth?token=xxx. The callback is fixed on the API
                // account, which is why the scheme is not "simpmusic".
                val token = data.getQueryParameter("token")
                Logger.d("MainActivity", "Last.fm callback, token present: ${!token.isNullOrEmpty()}")
                viewModel.setIntent(null)
                // Deliberately no navigation: the login screen is almost certainly already open —
                // the browser was opened from it — and navigating would stack a second copy on top
                // of it. The token is handed straight to the shared view model, and the screen
                // closes itself when it sees a session key appear.
                token?.let { viewModel.completeLastfmLogin(it) }
            } else if (data.host == "simpmusic.org" || data.scheme == "simpmusic") {
                // https://simpmusic.org/app/watch?v=VIDEO_ID
                // https://simpmusic.org/app/playlist?list=PLAYLIST_ID
                // https://simpmusic.org/app/channel/CHANNEL_ID
                // simpmusic://watch?v=VIDEO_ID  (host="watch", no path)
                // simpmusic://playlist?list=PLAYLIST_ID
                // simpmusic://channel/CHANNEL_ID
                val segments = data.pathSegments
                // For simpmusic.org: segments = ["app", "watch"] → appPath = segments[1]
                // For simpmusic://: host IS the appPath (e.g. host="watch"), segments = []
                val appPath =
                    if (data.scheme == "simpmusic") {
                        data.host
                    } else {
                        segments.getOrNull(1)
                    }
                Logger.d("MainActivity", "simpmusic.org deep link, appPath: $appPath")
                viewModel.setIntent(null)
                when (appPath) {
                    "watch" -> {
                        data.getQueryParameter("v")?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }

                    "playlist" -> {
                        data.getQueryParameter("list")?.let { playlistId ->
                            if (playlistId.startsWith("OLAK5uy_")) {
                                navController.navigate(AlbumDestination(browseId = playlistId))
                            } else if (playlistId.startsWith("VL")) {
                                navController.navigate(PlaylistDestination(playlistId = playlistId))
                            } else {
                                navController.navigate(PlaylistDestination(playlistId = "VL$playlistId"))
                            }
                        }
                    }

                    "channel", "c" -> {
                        // simpmusic://channel/UCxxx → segments = ["UCxxx"]
                        // simpmusic.org/app/channel/UCxxx → segments = ["app", "channel", "UCxxx"]
                        val artistId =
                            if (data.scheme == "simpmusic") {
                                segments.firstOrNull()
                            } else {
                                segments.getOrNull(2)
                            }
                        artistId?.let {
                            if (it.startsWith("UC")) {
                                navController.navigate(ArtistDestination(channelId = it))
                            } else {
                                viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                            }
                        }
                    }

                    "album" -> {
                        data.getQueryParameter("id")?.let { albumId ->
                            navController.navigate(AlbumDestination(browseId = albumId))
                        }
                    }

                    // simpmusic://library                     → the Library tab
                    // simpmusic://library?type=favorite       → one of its collections
                    // Added for the Playlists widget, whose shortcuts have to reach these
                    // screens from the home screen without the app already running.
                    "library" -> {
                        val type = data.getQueryParameter("type")
                        if (type.isNullOrBlank()) {
                            navController.navigate(LibraryDestination)
                        } else {
                            navController.navigate(LibraryDynamicPlaylistDestination(type = type))
                        }
                    }

                    else -> {
                        viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                    }
                }
            } else {
                Logger.d("MainActivity", "onCreate: $data")
                when (val path = data.pathSegments.firstOrNull()) {
                    "playlist" -> {
                        data
                            .getQueryParameter("list")
                            ?.let { playlistId ->
                                viewModel.setIntent(null)
                                if (playlistId.startsWith("OLAK5uy_")) {
                                    navController.navigate(
                                        AlbumDestination(
                                            browseId = playlistId,
                                        ),
                                    )
                                } else if (playlistId.startsWith("VL")) {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = playlistId,
                                        ),
                                    )
                                } else {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = "VL$playlistId",
                                        ),
                                    )
                                }
                            }
                    }

                    "channel", "c" -> {
                        data.lastPathSegment?.let { artistId ->
                            if (artistId.startsWith("UC")) {
                                viewModel.setIntent(null)
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = artistId,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(
                                    getString(
                                        Res.string.this_link_is_not_supported,
                                    ),
                                )
                            }
                        }
                    }

                    else -> {
                        when {
                            path == "watch" -> data.getQueryParameter("v")
                            data.host == "youtu.be" -> path
                            else -> null
                        }?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(updateData) {
        val response = updateData ?: return@LaunchedEffect
        if (viewModel.showedUpdateDialog &&
            response.tagName != getString(Res.string.version_format, VersionManager.getVersionName())
        ) {
            shouldShowUpdateDialog = true
        }
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            Logger.d("MainActivity", "Current destination: ${entry.destination.route}")
            if (entry.destination.route?.contains("FullscreenDestination") == true) {
                isShowNowPlaylistScreen = false
            }
            isInFullscreen = entry.destination.hierarchy.any {
                it.hasRoute(FullscreenDestination::class) || it.hasRoute(WrappedDestination::class)
            }
            
            if (!showAnalyticsTab &&
                entry.destination.hierarchy.any {
                    it.hasRoute(AnalyticsDestination::class)
                }
            ) {
                navController.navigate(HomeDestination) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            
            if (!showMixForYouTab &&
                entry.destination.hierarchy.any {
                    it.hasRoute(MixForYouDestination::class)
                }
            ) {
                navController.navigate(HomeDestination) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val isTablet = windowSize.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isTabletLandscape = isTablet && currentOrientation() == Orientation.LANDSCAPE

    val morphProgress by animateFloatAsState(
        targetValue = if (isShowNowPlaylistScreen && !isTabletLandscape) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = if (isShowNowPlaylistScreen) 340 else 280,
                easing = FastOutSlowInEasing,
            ),
        label = "nowPlayingMorphProgress",
    )
    val isMorphActive = isShowNowPlaylistScreen || morphProgress > 0.001f

    AppTheme(
        themeMode = themeMode,
        themeColorSource = themeColorSource,
        customThemeColor = parseThemeColorHex(customThemeColorHex),
        // Desktop is unconditionally true — the liquid-glass setting row is Android-only, and the
        // Desktop capsule player is glass by design. Same rule as MiniPlayer's useGlassSurface.
        liquidGlassEnabled = isLiquidGlassEnabled == TRUE || getPlatform() == Platform.Desktop,
    ) {
        // Backdrop base must match the theme: white page → white glass, dark/AMOLED → black glass.
        // Read inside AppTheme so MaterialTheme reflects the resolved scheme (light background is #FFFFFF).
        val isLightScheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
        val backdrop = rememberBackdrop(if (isLightScheme) Color.White else Color.Black)

        // The desktop shell is a window colour with panels floating on it. The two schemes mirror
        // each other: the window takes the extreme (pure black / pure white) and the panel steps
        // one shade back towards the middle, so the panels read as raised either way.
        val desktopWindow = if (isLightScheme) desktopWindowLight else desktopWindowDark
        val desktopPanel =
            if (isLightScheme) MaterialTheme.colorScheme.surfaceContainer else desktopPanelDark
        CompositionLocalProvider(
            LocalNowPlayingMorphProgress provides morphProgress,
            LocalNowPlayingArtworkBounds provides nowPlayingArtworkBoundsState,
        ) {
            Box(Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor =
                        if (isDesktopShell) desktopWindow else MaterialTheme.colorScheme.background,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = isNavBarVisible && !isInFullscreen,
                            enter =
                                fadeIn(animationSpec = tween(260)) +
                                    slideInVertically(
                                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                                        initialOffsetY = { it },
                                    ),
                            exit =
                                fadeOut(animationSpec = tween(200)) +
                                    slideOutVertically(
                                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                                        targetOffsetY = { it },
                                    ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                AppMiniPlayer(
                                    isShowMiniPlayer = isShowMiniPlayer && !isShowNowPlaylistScreen,
                                    isTablet = isTablet,
                                    backdrop = backdrop,
                                    onClick = { isShowNowPlaylistScreen = true },
                                    onClose = {
                                        viewModel.stopPlayer()
                                        viewModel.isServiceRunning = false
                                    },
                                    onArtworkPositioned = { miniPlayerArtworkBoundsState.value = it },
                                )
                        // Sleek modern floating dock with solid color rendering
                        val reloadDestination = remember(viewModel) {
                            { klass: KClass<*> -> viewModel.reloadDestination(klass) }
                        }
                        AppBottomDock(
                            navController = navController,
                            showAnalyticsTab = showAnalyticsTab,
                            showMixForYouTab = showMixForYouTab,
                            reloadDestinationIfNeeded = reloadDestination,
                            onItemClick = { isShowNowPlaylistScreen = false },
                        )
                    }
                }
            },
            content = { innerPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (isLiquidGlassEnabled == TRUE && !isTablet) {
                                Modifier.layerBackdrop(backdrop)
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                    ) {
                        // Desktop only: the content sits in its own rounded panel floating on a
                        // pure black window, Spotify style, while the rail stays flat black
                        // outside it. Phones keep one continuous surface — the inset only reads
                        // as deliberate when there is a window frame around it.
                        Box(
                            Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .then(
                                    if (isDesktopShell) {
                                        Modifier
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(desktopPanel)
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .then(
                                        // Desktop is unconditional: the floating capsule player is ALWAYS
                                        // liquid glass there, and glass with no recorded source draws as
                                        // plain transparency. Gating the source on the setting while the
                                        // capsule ignored it was exactly the nested-flag split that kept
                                        // the capsule see-through.
                                        if ((isLiquidGlassEnabled == TRUE || getPlatform() == Platform.Desktop) &&
                                            isTablet &&
                                            !isInFullscreen
                                        ) {
                                            Modifier.layerBackdrop(backdrop)
                                        } else {
                                            Modifier
                                        },
                                    ),
                            ) {
                                AppNavigationGraph(
                                    innerPadding = innerPadding,
                                    navController = navController,
                                    hideNavBar = {
                                        isNavBarVisible = false
                                    },
                                    showNavBar = {
                                        isNavBarVisible = true
                                    },
                                    showNowPlayingSheet = {
                                        isShowNowPlaylistScreen = true
                                    },
                                    onScrolling = {
                                        isScrolledToTop = it
                                    },
                                )
                            }
                        }
                        if (isTablet && isTabletLandscape && !isInFullscreen) {
                            BackHandler(enabled = isShowNowPlaylistScreen) {
                                isShowNowPlaylistScreen = false
                            }
                            AnimatedVisibility(
                                isShowNowPlaylistScreen,
                                enter =
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        initialOffsetX = { it },
                                    ) + fadeIn(animationSpec = tween(300)),
                                exit =
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        targetOffsetX = { it },
                                    ) + fadeOut(animationSpec = tween(300)),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxHeight()
                                        .widthIn(min = 360.dp, max = 460.dp)
                                        .fillMaxWidth(0.38f)
                                        .graphicsLayer { clip = true },
                                ) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        Modifier
                                            .padding(
                                                innerPadding.copy(
                                                    start = 0.dp,
                                                    top = 0.dp,
                                                    bottom = 0.dp,
                                                ),
                                            ).then(
                                                // Matches the inset of the content panel so the two
                                                // read as a pair of floating cards, not one panel
                                                // with a seam down the middle.
                                                if (isDesktopShell) {
                                                    Modifier.padding(top = 8.dp, end = 8.dp, bottom = 8.dp)
                                                } else {
                                                    Modifier
                                                },
                                            ).clip(
                                                RoundedCornerShape(12.dp),
                                            ).then(
                                                if (isDesktopShell) {
                                                    Modifier.background(desktopPanel)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                    ) {
                                        ForceDarkContent {
                                            NowPlayingScreenContent(
                                                navController = navController,
                                                sharedViewModel = viewModel,
                                                isExpanded = true,
                                                dismissIcon = SimpIcons.ArrowForwardIos,
                                            ) {
                                                isShowNowPlaylistScreen = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isMorphActive && !isTabletLandscape) {
                    BackHandler(enabled = isShowNowPlaylistScreen) {
                        isShowNowPlaylistScreen = false
                    }
                    val contentAlpha = ((morphProgress - 0.06f) / 0.94f).coerceIn(0f, 1f)
                    ForceDarkContent {
                        if (isTablet) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = contentAlpha }
                                    .background(Color.Black.copy(alpha = 0.55f * contentAlpha))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { isShowNowPlaylistScreen = false },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 600.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {},
                                        ),
                                ) {
                                    NowPlayingScreen(
                                        navController = navController,
                                    ) {
                                        isShowNowPlaylistScreen = false
                                    }
                                }
                            }
                        } else {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = contentAlpha },
                            ) {
                                NowPlayingScreen(
                                    navController = navController,
                                ) {
                                    isShowNowPlaylistScreen = false
                                }
                            }
                        }
                    }
                }

                val isSleepTimerDone by remember {
                    derivedStateOf { sleepTimerStateState.value.isDone }
                }
                if (isSleepTimerDone) {
                    Logger.w("MainActivity", "Sleep Timer Done")
                    AlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            viewModel.stopSleepTimer()
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.stopSleepTimer()
                            }) {
                                Text(
                                    stringResource(Res.string.yes),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        text = {
                            Text(
                                stringResource(Res.string.sleep_timer_off),
                                style = typo().labelSmall,
                            )
                        },
                        title = {
                            Text(
                                stringResource(Res.string.good_night),
                                style = typo().bodySmall,
                            )
                        },
                    )
                }

                if (shouldShowUpdateDialog) {
                    val response = updateData ?: return@Scaffold
                    AlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            shouldShowUpdateDialog = false
                            viewModel.showedUpdateDialog = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    shouldShowUpdateDialog = false
                                    viewModel.showedUpdateDialog = false
                                    openUrl("https://simpmusic.org/download")
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.download),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    shouldShowUpdateDialog = false
                                    viewModel.showedUpdateDialog = false
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.cancel),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        title = {
                            Text(
                                stringResource(Res.string.update_available),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            val formatted =
                                response.releaseTime?.let { input ->
                                    try {
                                        val instant = kotlin.time.Instant.parse(input)
                                        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                        dateTime.format(
                                            LocalDateTime.Format {
                                                day()
                                                char(' ')
                                                monthName(MonthNames.ENGLISH_ABBREVIATED)
                                                char(' ')
                                                year()
                                                char(' ')
                                                hour()
                                                char(':')
                                                minute()
                                                char(':')
                                                second()
                                            },
                                        )
                                    } catch (e: Exception) {
                                        stringResource(Res.string.unknown)
                                    }
                                } ?: stringResource(Res.string.unknown)

                            val updateMessage =
                                runBlocking {
                                    getString(
                                        Res.string.update_message,
                                        response.tagName,
                                        formatted,
                                    )
                                }
                            Column(
                                Modifier
                                    .heightIn(
                                        max = 400.dp,
                                    ).verticalScroll(
                                        rememberScrollState(),
                                    ),
                            ) {
                                Text(
                                    text = updateMessage,
                                    style = typo().labelMedium,
                                    modifier =
                                        Modifier.padding(
                                            vertical = 8.dp,
                                        ),
                                )
                                Markdown(
                                    response.body,
                                    typography =
                                        markdownTypography(
                                            h1 = typo().labelLarge,
                                            h2 = typo().labelMedium,
                                            h3 = typo().labelSmall,
                                            text = typo().bodySmall,
                                            bullet = typo().bodySmall,
                                            paragraph = typo().bodySmall,
                                            textLink =
                                                TextLinkStyles(
                                                    SpanStyle(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        fontFamily = fontFamily(),
                                                        textDecoration = TextDecoration.Underline,
                                                    ),
                                                ),
                                        ),
                                )
                            }
                        },
                    )
                }

                if (showNotificationPermissionDialog || showDesktopNotificationPermissionDialog) {
                    var doNotShowAgain by remember { mutableStateOf(false) }
                    val dismissPermissionDialog = {
                        if (showDesktopNotificationPermissionDialog) {
                            onDismissDesktopNotificationPermissionDialog(doNotShowAgain)
                        } else {
                            viewModel.dismissNotificationPermissionDialog(doNotShowAgain)
                        }
                    }
                    AlertDialog(
                        onDismissRequest = {
                            dismissPermissionDialog()
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (showDesktopNotificationPermissionDialog) {
                                        onOpenDesktopNotificationSettings(doNotShowAgain)
                                    } else {
                                        viewModel.dismissNotificationPermissionDialog(doNotShowAgain)
                                    }
                                },
                            ) {
                                Text(
                                    stringResource(
                                        if (showDesktopNotificationPermissionDialog) {
                                            Res.string.settings
                                        } else {
                                            Res.string.yes
                                        },
                                    ),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        dismissButton =
                            if (showDesktopNotificationPermissionDialog) {
                                {
                                    TextButton(onClick = dismissPermissionDialog) {
                                        Text(
                                            stringResource(Res.string.cancel),
                                            style = typo().bodySmall,
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        title = {
                            Text(
                                stringResource(Res.string.notification),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    stringResource(Res.string.this_app_needs_to_access_your_notification),
                                    style = typo().bodySmall,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .clickable { doNotShowAgain = !doNotShowAgain }
                                            .fillMaxWidth(),
                                ) {
                                    Checkbox(
                                        checked = doNotShowAgain,
                                        onCheckedChange = { doNotShowAgain = it },
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        stringResource(Res.string.do_not_show_again),
                                        style = typo().bodySmall,
                                    )
                                }
                            }
                        },
                    )
                }
            },
                )
                val currentArtworkUrl =
                    nowPlayingDataState.value?.songEntity?.thumbnails
                        ?: nowPlayingDataState.value?.mediaItem?.metadata?.artworkUri
                MorphingArtworkOverlay(
                    artworkUrl = currentArtworkUrl,
                    progress = morphProgress,
                    fromBounds = miniPlayerArtworkBoundsState.value,
                    toBounds = nowPlayingArtworkBoundsState.value,
                )
            }
        }
    }
}

@Composable
private fun MorphingArtworkOverlay(
    artworkUrl: String?,
    progress: Float,
    fromBounds: Rect?,
    toBounds: Rect?,
) {
    if (progress !in 0.001f..0.999f || fromBounds == null || toBounds == null || fromBounds.width <= 0f || toBounds.width <= 0f) {
        return
    }

    val density = LocalDensity.current
    val currentLeft = lerp(fromBounds.left, toBounds.left, progress)
    val currentTop = lerp(fromBounds.top, toBounds.top, progress)
    val currentWidth = lerp(fromBounds.width, toBounds.width, progress)
    val currentHeight = lerp(fromBounds.height, toBounds.height, progress)

    // Corner radius: from half the miniplayer size (circle) to 28.dp rounded card
    val startRadius = fromBounds.width / 2f
    val targetRadius = with(density) { 28.dp.toPx() }
    val currentRadius = lerp(startRadius, targetRadius, progress)
    val shape = RoundedCornerShape(with(density) { currentRadius.toDp() })

    // Parabolic elevation peaking at mid-flight for tactile physical lift
    val elevationDp = (sin(progress * PI.toFloat()) * 16f).coerceAtLeast(0f).dp

    // Cross-fade opacity at the end of the flight (0.96 to 1.0) so handoff to resting card is perfectly seamless
    val alpha = if (progress >= 0.96f) ((1f - progress) / 0.04f).coerceIn(0f, 1f) else 1f

    Box(
        modifier =
            Modifier
                .offset { IntOffset(currentLeft.roundToInt(), currentTop.roundToInt()) }
                .size(
                    width = with(density) { currentWidth.toDp() },
                    height = with(density) { currentHeight.toDp() },
                ).shadow(
                    elevation = elevationDp,
                    shape = shape,
                    clip = false,
                ).clip(shape)
                .graphicsLayer { this.alpha = alpha },
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalPlatformContext.current)
                    .data(artworkUrl)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(artworkUrl)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = rememberHolderPainter(),
            error = rememberHolderPainter(),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

