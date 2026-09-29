package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.ui.PlatformCastButton
import com.maxrave.simpmusic.expect.ui.isPlatformCastAvailable
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.isElementVisible
import com.maxrave.simpmusic.extension.smoothScrimBrush
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.ExplicitBadge
import com.maxrave.simpmusic.ui.component.heartBurst
import com.maxrave.simpmusic.ui.component.rememberHeartBurstState
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.icon.AddCircleOutline
import com.maxrave.simpmusic.ui.icon.CheckCircle
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.FavoriteBorder
import com.maxrave.simpmusic.ui.icon.Info
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.PlaylistAdd
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.Repeat
import com.maxrave.simpmusic.ui.icon.RepeatOne
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.screen.player.content.expressive.ExpressiveTransportRow
import com.maxrave.simpmusic.ui.screen.player.content.expressive.WavySeekBar
import com.maxrave.simpmusic.ui.theme.LocalForcedDarkColorScheme
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.UIEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxrave.simpmusic.viewModel.SharedViewModel
import org.koin.compose.koinInject
import kotlin.math.roundToLong
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.crossfading
import simpmusic.composeapp.generated.resources.now_playing_upper

/**
 * The Material 3 Expressive ("Tonal pills") Now Playing style.
 *
 * A full dark tonal scheme is derived from the artwork palette (materialKolor Vibrant) and
 * everything renders through Material color roles — no black gradient, no hardcoded whites
 * on semantic surfaces. Layout skeleton (scroll, artwork pager sync, vertical rhythm,
 * below-the-fold cards, sticky toolbar) mirrors [NowPlayingContentSpotify] one-to-one so
 * both styles behave identically; only the presentation differs.
 *
 * Canvas mode behaves exactly like Classic: when canvas data exists the page goes flat
 * black, the canvas plays fullscreen as the bottom layer of the current pager page and the
 * artwork card hides while keeping its layout slot. The info block fades with
 * [NowPlayingContentState.controlLayoutAlpha], a tap on the page toggles it via
 * [NowPlayingContentActions.onToggleControls], and while hidden
 * ([NowPlayingContentState.showControlLayout] false) the Classic "unfocused" metadata
 * overlay renders over the info area. The shell owns the auto-hide state machine.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingContentM3Expressive(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    NowPlayingExpressiveTheme(state = state) {
        NowPlayingM3ExpressiveLayout(state = state, actions = actions)
    }
}

/**
 * This style's colour system, shared with the fullscreen lyrics landscape layout, which renders
 * this style's track row and playback controls and must colour them the same way.
 */
@Composable
internal fun NowPlayingExpressiveTheme(
    state: NowPlayingContentState,
    content: @Composable () -> Unit,
) {
    // === 1. Color system: full dark scheme derived from the artwork ===
    // startColor is animated by the shell from the track's cached palette color (or fallback)
    // to any subsequent track palette; fall back to the active theme's primary while unseeded.
    val paletteColor = state.startColor.value
    val activeDarkScheme = LocalForcedDarkColorScheme.current ?: MaterialTheme.colorScheme
    val fallbackSeed = activeDarkScheme.primary
    val seedColor = if (paletteColor == Color.Black) fallbackSeed else paletteColor

    var hasReceivedTrackColor by remember { mutableStateOf(paletteColor != Color.Black) }
    LaunchedEffect(paletteColor) {
        if (paletteColor != Color.Black) {
            hasReceivedTrackColor = true
        }
    }

    // Track changes must GLIDE between palettes: 800ms matches the palette crossfade feel.
    // Initial color application snaps immediately so opening the player never flashes default colors.
    val animatedSeedColor by animateColorAsState(
        targetValue = seedColor,
        animationSpec = if (!hasReceivedTrackColor) snap() else tween(durationMillis = 800),
        label = "m3eSeedColor",
    )
    val derivedScheme =
        rememberDynamicColorScheme(
            seedColor = animatedSeedColor,
            isDark = true,
            isAmoled = false,
            style = PaletteStyle.Vibrant,
        )
    MaterialExpressiveTheme(colorScheme = derivedScheme) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NowPlayingM3ExpressiveLayout(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val screenInfo = getScreenSizeInfo()
    val localDensity = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    val isRepeatOne = state.controllerState.repeatState is RepeatState.One

    // Canvas mode fades the info block in and out. The shell's shared 500ms linear alpha exposes
    // a long half-blended phase in which container-backed buttons pick up the bright canvas
    // behind them and read lighter than the header's (which never fades, like Classic) — the
    // "top bar buttons are darker" report. Fading IN fast makes that frame effectively
    // invisible; fading OUT keeps Classic's relaxed 500ms feel. Steady states are untouched.
    val m3eControlsAlpha by animateFloatAsState(
        targetValue = if (state.showControlLayout) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = if (state.showControlLayout) 180 else 500,
                easing = LinearEasing,
            ),
        label = "m3eControlsAlpha",
    )

    // === 2. Vertical rhythm — identical mechanism to NowPlayingContentSpotify ===
    // The pre-fold section fills exactly one screen: measured header/artwork/info heights,
    // and the artwork sits between two equal gaps of `middleLayoutPaddingDp`.
    var topAppBarHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var middleLayoutHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var infoLayoutHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var middleLayoutPaddingDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    val minimumPaddingDp by rememberSaveable {
        mutableIntStateOf(
            30,
        )
    }
    LaunchedEffect(
        topAppBarHeightDp,
        // Unlike Classic, the M3E artwork frame CHANGES height (square ↔ 16:9 while a video
        // plays), so the fold math must re-run when the measured middle height moves too —
        // without this key the gap keeps the previous track's numbers and the layout drifts.
        middleLayoutHeightDp,
        screenInfo,
        infoLayoutHeightDp,
        minimumPaddingDp,
    ) {
        if (topAppBarHeightDp > 0 && middleLayoutHeightDp > 0 && infoLayoutHeightDp > 0 && screenInfo.hDP > 0) {
            val result = (screenInfo.hDP - topAppBarHeightDp - middleLayoutHeightDp - infoLayoutHeightDp - minimumPaddingDp) / 2
            middleLayoutPaddingDp =
                if (result > minimumPaddingDp) {
                    result
                } else {
                    minimumPaddingDp
                }
        }
    }

    // Video overlay controls (fullscreen / ±5s / subtitles) auto-hide after 3s.
    var showHideFullscreenOverlay by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = showHideFullscreenOverlay) {
        if (showHideFullscreenOverlay) {
            delay(3000)
            showHideFullscreenOverlay = false
        }
    }

    Box {
        Column(
            Modifier
                .verticalScroll(
                    state.mainScrollState,
                    enabled = state.isExpanded,
                )
                // Tonal design: the page is a plain surface — the artwork card carries the
                // color. While a canvas is active (showHideMiddleLayout is false exactly
                // then) the page goes flat black instead, exactly like Classic.
                .background(if (state.showHideMiddleLayout) colorScheme.surface else Color.Black),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // === 4. Artwork pager — same state/sync as Classic, card presentation ===
                HorizontalPager(
                    state = state.artworkPagerState,
                    modifier =
                        Modifier
                            .height(screenInfo.hDP.dp)
                            .fillMaxWidth(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = !isRepeatOne && state.artworkQueue.isNotEmpty(),
                    key = { idx ->
                        val vid = state.artworkQueue.getOrNull(idx)?.videoId.orEmpty()
                        "artwork_${vid}_$idx"
                    },
                ) { page ->
                    ExpressiveArtworkCardPage(
                        state = state,
                        actions = actions,
                        page = page,
                        topAppBarHeightDp = topAppBarHeightDp,
                        middleLayoutPaddingDp = middleLayoutPaddingDp,
                        showHideFullscreenOverlay = showHideFullscreenOverlay,
                        onToggleFullscreenOverlay = {
                            showHideFullscreenOverlay = !showHideFullscreenOverlay
                        },
                    )
                }

                // === 3. Header row ===
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .onGloballyPositioned {
                                topAppBarHeightDp =
                                    with(localDensity) {
                                        it.size.height
                                            .toDp()
                                            .value
                                            .toInt()
                                    }
                            }.padding(
                                top = with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() },
                            ).padding(top = 8.dp, start = 20.dp, end = 20.dp),
                ) {
                    IconButton(
                        onClick = { actions.onDismiss() },
                        shape = CircleShape,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = colorScheme.surfaceContainerHigh,
                                contentColor = colorScheme.onSurface,
                            ),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = state.dismissIcon,
                            contentDescription = "",
                        )
                    }
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.now_playing_upper),
                            style = typo().bodyMedium,
                            color = Color.White,
                        )
                        Text(
                            text = state.screenData.playlistName,
                            style = typo().labelMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .basicMarquee(
                                        iterations = Int.MAX_VALUE,
                                        animationMode = MarqueeAnimationMode.Immediately,
                                    ).focusable(),
                        )
                    }
                    IconButton(
                        onClick = { actions.onShowMoreSheet() },
                        shape = RoundedCornerShape(14.dp),
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = colorScheme.surfaceContainerHigh,
                                contentColor = colorScheme.onSurface,
                            ),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = SimpIcons.MoreVert,
                            contentDescription = "",
                        )
                    }
                }

                Column {
                    Spacer(
                        modifier =
                            Modifier.height(
                                topAppBarHeightDp.dp,
                            ),
                    )
                    Column(
                        Modifier
                            .fillMaxWidth(),
                    ) {
                        Spacer(
                            modifier =
                                Modifier
                                    .animateContentSize()
                                    .height(
                                        middleLayoutPaddingDp.dp,
                                    ).fillMaxWidth(),
                        )

                        // The artwork card is rendered by the pager above; reserve the same
                        // vertical space so the info layout keeps its Y position. Spacer has
                        // no pointer input so pager swipes fall through. Its ratio MUST match
                        // the card's (16:9 while a video plays, else square) or the fold math
                        // drifts from what the pager actually draws.
                        Spacer(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .onGloballyPositioned { coords ->
                                        middleLayoutHeightDp =
                                            with(localDensity) {
                                                coords.size.height
                                                    .toDp()
                                                    .value
                                                    .toInt()
                                            }
                                    }.aspectRatio(
                                        if (state.screenData.isVideo && state.shouldShowVideo) 16f / 9 else 1f,
                                    ),
                        )

                        // === 5. Inline current-lyric line, centered in the lower gap ===
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .animateContentSize()
                                    .height(
                                        middleLayoutPaddingDp.dp,
                                    ).fillMaxWidth(),
                        ) {
                            val inlineLyrics = state.screenData.lyricsData?.lyrics
                            val hasSyncedLyrics =
                                inlineLyrics != null &&
                                    inlineLyrics.syncType != null &&
                                    inlineLyrics.syncType != "UNSYNCED" &&
                                    inlineLyrics.lines != null
                            val currentLyricLineText =
                                if (!hasSyncedLyrics ||
                                    state.screenData.canvasData != null ||
                                    state.currentLyricLineIndex < 0
                                ) {
                                    ""
                                } else {
                                    inlineLyrics
                                        ?.lines
                                        ?.getOrNull(state.currentLyricLineIndex)
                                        ?.words
                                        ?.stripRichSyncTimestamps()
                                        .orEmpty()
                                }
                            Crossfade(
                                targetState = currentLyricLineText,
                                animationSpec = tween(durationMillis = 300),
                                label = "inlineLyricLineExpressive",
                            ) { lineText ->
                                Text(
                                    text = lineText,
                                    style = typo().labelSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            }
                        }

                        // === 6. Info block ===
                        Box {
                            Column(
                                Modifier
                                    .alpha(m3eControlsAlpha)
                                    .onGloballyPositioned {
                                        infoLayoutHeightDp =
                                            with(localDensity) {
                                                it.size.height
                                                    .toDp()
                                                    .value
                                                    .toInt()
                                            }
                                    },
                            ) {
                                ExpressiveTrackInfoRow(state = state, actions = actions)
                                if (getPlatform() == Platform.Android) {
                                    ExpressivePlaybackControls(
                                        state = state,
                                        actions = actions,
                                        sliderModifier =
                                            Modifier.isElementVisible {
                                                actions.onToolbarVisibilityChange(!it && state.isExpanded && state.mainScrollState.value > 0)
                                            },
                                    )
                                } else {
                                    Spacer(Modifier.height(16.dp))
                                }
                                Spacer(Modifier.height(12.dp))
                                ExpressiveConnectedGroup(state = state, actions = actions)
                            }
                            // Canvas-unfocused overlay — Classic verbatim: covers the info
                            // area, a tap re-shows the controls, and the metadata row (plus
                            // the current lyric line) sits over a black-fading scrim.
                            this@Column.AnimatedVisibility(
                                visible = !state.showControlLayout,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .height(
                                                infoLayoutHeightDp.dp,
                                            ).fillMaxWidth()
                                            .clickable(
                                                onClick = {
                                                    if (state.mainScrollState.value == 0) {
                                                        actions.onToggleControls()
                                                    }
                                                },
                                                indication = null,
                                                interactionSource =
                                                    remember {
                                                        MutableInteractionSource()
                                                    },
                                            ),
                                    contentAlignment = Alignment.BottomStart,
                                ) {
                                    // Gradient backdrop — transparent at top so the canvas
                                    // shows through, fading to dark under the metadata row.
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .background(
                                                    smoothScrimBrush(
                                                        from = Color.Black.copy(alpha = 0f),
                                                        to = Color.Black.copy(alpha = 0.85f),
                                                    ),
                                                ),
                                    )

                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .animateContentSize(),
                                    ) {
                                        this@Column.AnimatedVisibility(
                                            visible = state.currentLyricLineIndex > -1,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically(),
                                        ) {
                                            // Canvas subtitle — lyric line above the metadata
                                            // row. White/yellow kept from Classic: the backdrop
                                            // is black in both styles.
                                            val lineText =
                                                state.screenData.lyricsData
                                                    ?.lyrics
                                                    ?.lines
                                                    ?.getOrNull(state.currentLyricLineIndex)
                                                    ?.words
                                                    ?.stripRichSyncTimestamps()
                                            if (!lineText.isNullOrBlank()) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Text(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 20.dp)
                                                                .padding(bottom = 4.dp)
                                                                .basicMarquee(
                                                                    iterations = Int.MAX_VALUE,
                                                                    animationMode = MarqueeAnimationMode.Immediately,
                                                                ).focusable(),
                                                        text = lineText,
                                                        style = typo().bodyMedium,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                    )
                                                    val translatedLineText =
                                                        state.screenData.lyricsData
                                                            ?.translatedLyrics
                                                            ?.first
                                                            ?.lines
                                                            ?.getOrNull(state.currentLyricLineIndex)
                                                            ?.words
                                                            ?.stripRichSyncTimestamps()
                                                    if (!translatedLineText.isNullOrBlank()) {
                                                        Text(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 20.dp)
                                                                    .padding(bottom = 8.dp)
                                                                    .basicMarquee(
                                                                        iterations = Int.MAX_VALUE,
                                                                        animationMode = MarqueeAnimationMode.Immediately,
                                                                    ).focusable(),
                                                            text = translatedLineText,
                                                            style = typo().bodyMedium,
                                                            color = Color.Yellow,
                                                            maxLines = 1,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        ExpressiveTrackInfoRow(state = state, actions = actions)
                                    }
                                }
                            }
                        }
                    }
                    // === 7. Below the fold ===
                    ExpressiveBelowTheFold(state = state, actions = actions)
                }
            }
        }
        // === 8. Collapsed sticky toolbar ===
        ExpressiveCollapsedToolbar(state = state, actions = actions)
    }
}

/**
 * Title + artist + the two 48dp tonal circles (YouTube liked, favourite heart).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ExpressiveTrackInfoRow(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    showCanvasThumbnail: Boolean = true,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // While a canvas hides the big artwork, a small thumbnail joins the row — Classic
        // verbatim (its shared NowPlayingTrackInfoRow does exactly this). Switched off by the
        // fullscreen lyrics landscape layout, which shows the full artwork right above the row.
        AnimatedVisibility(showCanvasThumbnail && state.screenData.canvasData != null) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(state.screenData.thumbnailURL)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(state.screenData.thumbnailURL + "BIGGER")
                        
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier =
                    Modifier
                        .heightIn(0.dp, 55.dp)
                        .width(55.dp)
                        .padding(end = 10.dp)
                        .clip(
                            RoundedCornerShape(4.dp),
                        ).align(Alignment.CenterVertically),
            )
        }

        val morphProgress = LocalNowPlayingMorphProgress.current
        val textBoundsHolder = LocalNowPlayingTextBounds.current
        val restingTextAlpha =
            if (morphProgress < 0.85f) 0f
            else if (morphProgress < 1f) ((morphProgress - 0.85f) / 0.15f).coerceIn(0f, 1f)
            else 1f
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .alpha(restingTextAlpha)
                    .onGloballyPositioned { coordinates ->
                        textBoundsHolder?.value = coordinates.boundsInRoot()
                    },
        ) {
            Text(
                text = state.screenData.nowPlayingTitle,
                style = typo().titleMedium,
                color = Color.White,
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                        ).focusable(),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(visible = state.screenData.isExplicit) {
                    ExplicitBadge(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                    )
                }
                Text(
                    text = state.screenData.artistName,
                    style = typo().bodyMedium,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .weight(1f)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable()
                            .clickable {
                                actions.onNavigateToArtist()
                            },
                )
            }
        }
        if (state.isUserLoggedIn) {
            Spacer(modifier = Modifier.size(12.dp))
            IconButton(
                onClick = { actions.onAddToYouTubeLiked() },
                shape = CircleShape,
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                    ),
                modifier = Modifier.size(48.dp),
            ) {
                Crossfade(targetState = state.likeStatus) { liked ->
                    Icon(
                        imageVector = if (liked) SimpIcons.CheckCircle else SimpIcons.AddCircleOutline,
                        contentDescription = "",
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        val likeBurst = rememberHeartBurstState()
        FilledIconToggleButton(
            checked = state.controllerState.isLiked,
            onCheckedChange = {
                // Fire on the TAP that likes, never on the state — see HeartBurstState's doc.
                if (!state.controllerState.isLiked) likeBurst.fire()
                actions.onUIEvent(UIEvent.ToggleLike)
            },
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledIconToggleButtonColors(
                    containerColor = colorScheme.surfaceContainerHigh,
                    contentColor = colorScheme.onSurfaceVariant,
                    checkedContainerColor = colorScheme.primaryContainer,
                    checkedContentColor = colorScheme.onPrimaryContainer,
                ),
            // The burst draws outside the 48dp bounds; the button's own shape clip is internal
            // (on its Surface), so sparks fired from this outer modifier are not trimmed.
            modifier = Modifier.size(48.dp).heartBurst(likeBurst),
        ) {
            Crossfade(targetState = state.controllerState.isLiked) { liked ->
                Icon(
                    imageVector = if (liked) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
                    contentDescription = "",
                )
            }
        }
    }
}

/**
 * Wavy seek bar + time row + transport — the playback half of the info block, shared with the
 * fullscreen lyrics landscape layout. Must be composed inside [NowPlayingExpressiveTheme]. Emits
 * straight into the caller's Column: isElementVisible (passed in through [sliderModifier]) compares
 * the seek bar against its PARENT layout, so a wrapper here would change what it compares against.
 */
@Composable
internal fun ColumnScope.ExpressivePlaybackControls(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    sliderModifier: Modifier = Modifier,
    showShuffleAndRepeat: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val sharedViewModel = koinInject<SharedViewModel>()
    val waveStyle by sharedViewModel.getWaveStyle().collectAsStateWithLifecycle(SharedViewModel.WAVE_STYLE_EXPRESSIVE)
    Box(
        Modifier
            .padding(
                top = 15.dp,
            ).padding(horizontal = 20.dp)
            .then(sliderModifier),
    ) {
        WavySeekBar(
            progressFraction = state.sliderValue / 100f,
            isPlaying = state.controllerState.isPlaying,
            // Classic swaps the slider color to the rainbow while
            // crossfading (state.sliderTrackColor); tonal primary
            // otherwise.
            activeColor =
                if (state.timelineState.isCrossfading) {
                    state.sliderTrackColor
                } else {
                    colorScheme.primary
                },
            trackColor = colorScheme.secondaryContainer,
            thumbColor = colorScheme.primary,
            onSliderChange = actions.onSliderChange,
            onSliderChangeFinished = actions.onSliderChangeFinished,
            waveStyle = waveStyle,
        )
    }
    // Time row — same math and negative guard as Classic
    // (formatDuration renders any negative as NA:NA).
    Row(
        Modifier
            .fillMaxWidth()
            // Drawn 8dp closer to the wave without shrinking the
            // seekbar's 40dp touch target or moving the layout slot:
            // the same 8dp visually opens the gap to the transport
            // row below (owner: times sat too far from the slider,
            // too close to the controls).
            .offset(y = (-8).dp)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = formatDuration((state.timelineState.total * (state.sliderValue / 100f)).roundToLong()),
            style = typo().bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Left,
        )
        // Sweep head for the "Crossfading" shimmer, 0..1. Runs
        // unconditionally: behind the crossfade check it would
        // restart from zero each time the label appears (same
        // rationale as the desktop MiniPlayer's crossfadeSweep).
        val sweepTransition = rememberInfiniteTransition(label = "m3eCrossfadeSweep")
        val crossfadeSweep by sweepTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(3200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "m3eSweepHead",
        )
        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = state.timelineState.isCrossfading,
        ) {
            // Same effect as the desktop MiniPlayer label: a
            // highlight sweeping through the glyphs via a text
            // brush — no overlay, no clipping.
            val shimmerSpan = 140f
            val shimmerHead = crossfadeSweep * (shimmerSpan * 3f) - shimmerSpan
            val labelColor = typo().bodyMedium.color
            Text(
                text = stringResource(Res.string.crossfading),
                style =
                    typo().bodyMedium.copy(
                        brush =
                            Brush.horizontalGradient(
                                0f to labelColor.copy(alpha = 0.45f),
                                // The sweep head is PURE white, not the resting label colour — the label
                                // colour is an adaptive grey, and a grey gleam reads as no gleam at all.
                                0.5f to Color.White,
                                1f to labelColor.copy(alpha = 0.45f),
                                startX = shimmerHead,
                                endX = shimmerHead + shimmerSpan,
                                tileMode = TileMode.Clamp,
                            ),
                    ),
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = formatDuration(state.timelineState.total),
            style = typo().bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right,
        )
    }
    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(8.dp),
    )
    ExpressiveTransportRow(
        controllerState = state.controllerState,
        loading = state.timelineState.loading,
        onUIEvent = actions.onUIEvent,
        modifier = Modifier.padding(horizontal = 20.dp),
        showShuffleAndRepeat = showShuffleAndRepeat,
    )
}

/**
 * Connected button group: six 48dp slots with 3dp gaps and rounded end caps —
 * Info | Cast | Shuffle | Repeat | Add-to-playlist | Queue.
 */
@Composable
private fun ExpressiveConnectedGroup(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    val scope = rememberCoroutineScope()

    val startCap = RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomEnd = 6.dp, bottomStart = 24.dp)
    val endCap = RoundedCornerShape(topStart = 6.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 6.dp)
    val middle = RoundedCornerShape(6.dp)

    val infoInteraction = remember { MutableInteractionSource() }
    val castInteraction = remember { MutableInteractionSource() }
    val shuffleInteraction = remember { MutableInteractionSource() }
    val repeatInteraction = remember { MutableInteractionSource() }
    val playlistInteraction = remember { MutableInteractionSource() }
    val queueInteraction = remember { MutableInteractionSource() }

    val infoPressed by infoInteraction.collectIsPressedAsState()
    val castPressed by castInteraction.collectIsPressedAsState()
    val shufflePressed by shuffleInteraction.collectIsPressedAsState()
    val repeatPressed by repeatInteraction.collectIsPressedAsState()
    val playlistPressed by playlistInteraction.collectIsPressedAsState()
    val queuePressed by queueInteraction.collectIsPressedAsState()

    val anyPressed = infoPressed || castPressed || shufflePressed || repeatPressed || playlistPressed || queuePressed

    // Interactive momentum pulses for the whole button and surrounded buttons on click
    val infoPulse = remember { Animatable(1f) }
    val castPulse = remember { Animatable(1f) }
    val shufflePulse = remember { Animatable(1f) }
    val repeatPulse = remember { Animatable(1f) }
    val playlistPulse = remember { Animatable(1f) }
    val queuePulse = remember { Animatable(1f) }

    // Wave impulse function: pressed button stretches outward while surrounded buttons contract inward
    val triggerWave: (Int) -> Unit = { centerIndex ->
        scope.launch {
            // 0: Info, 1: Cast, 2: Shuffle, 3: Repeat, 4: Playlist, 5: Queue
            val pulses = listOf(infoPulse, castPulse, shufflePulse, repeatPulse, playlistPulse, queuePulse)
            pulses.forEachIndexed { index, pulse ->
                val distance = abs(index - centerIndex)
                val targetScale = when (distance) {
                    0 -> 1.20f // Pressed button STRETCHES OUTWARDS (spreads around outer side)
                    1 -> 0.86f // Directly adjacent buttons CONTRACT INWARD to make room
                    2 -> 0.93f // Secondary neighbor contraction
                    else -> 0.98f
                }
                val damping = if (distance == 0) 0.55f else 0.6f
                val stiffness = if (distance == 0) 800f else 900f
                launch {
                    pulse.snapTo(targetScale)
                    pulse.animateTo(1f, spring(dampingRatio = damping, stiffness = stiffness))
                }
            }
        }
    }

    var firstRepeatMount by remember { mutableStateOf(true) }
    LaunchedEffect(state.controllerState.repeatState) {
        if (firstRepeatMount) {
            firstRepeatMount = false
            return@LaunchedEffect
        }
        triggerWave(3)
    }

    var firstShuffleMount by remember { mutableStateOf(true) }
    LaunchedEffect(state.controllerState.isShuffle) {
        if (firstShuffleMount) {
            firstShuffleMount = false
            return@LaunchedEffect
        }
        triggerWave(2)
    }

    // Dynamic weight distribution: pressed slot expands to 1.28f while peers contract to 0.94f
    val infoWeight by animateFloatAsState(
        targetValue = if (infoPressed) 1.28f else if (anyPressed) 0.94f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "infoWeight",
    )
    val castWeight by animateFloatAsState(
        targetValue = if (castPressed) 1.28f else if (anyPressed) 0.94f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "castWeight",
    )
    val shuffleWeight by animateFloatAsState(
        targetValue = if (shufflePressed) 1.28f else if (anyPressed) 0.94f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "shuffleWeight",
    )
    val repeatWeight by animateFloatAsState(
        targetValue = if (repeatPressed) 1.28f else if (anyPressed) 0.94f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "repeatWeight",
    )
    val playlistWeight by animateFloatAsState(
        targetValue = if (playlistPressed) 1.28f else if (anyPressed) 0.94f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "playlistWeight",
    )
    val queueWeight by animateFloatAsState(
        targetValue = if (queuePressed) 1.28f else if (anyPressed) 0.94f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "queueWeight",
    )

    // Coupled X-axis scales: pressed button stretches outward while neighbors contract inward
    val infoScaleX by animateFloatAsState(
        targetValue = when {
            infoPressed -> 1.15f
            castPressed -> 0.88f
            shufflePressed -> 0.94f
            anyPressed -> 0.96f
            else -> 1f
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "infoScaleX",
    )
    val castScaleX by animateFloatAsState(
        targetValue = when {
            castPressed -> 1.15f
            infoPressed || shufflePressed -> 0.88f
            repeatPressed -> 0.94f
            anyPressed -> 0.96f
            else -> 1f
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "castScaleX",
    )
    val shuffleScaleX by animateFloatAsState(
        targetValue = when {
            shufflePressed -> 1.16f // function button stretches out on X axis!
            castPressed || repeatPressed -> 0.88f // direct neighbors contract inward!
            infoPressed || playlistPressed -> 0.94f
            anyPressed -> 0.96f
            else -> 1f
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "shuffleScaleX",
    )
    val repeatScaleX by animateFloatAsState(
        targetValue = when {
            repeatPressed -> 1.16f // function button stretches out on X axis!
            shufflePressed || playlistPressed -> 0.88f // direct neighbors contract inward!
            castPressed || queuePressed -> 0.94f
            anyPressed -> 0.96f
            else -> 1f
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "repeatScaleX",
    )
    val playlistScaleX by animateFloatAsState(
        targetValue = when {
            playlistPressed -> 1.15f
            repeatPressed || queuePressed -> 0.88f
            shufflePressed -> 0.94f
            anyPressed -> 0.96f
            else -> 1f
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "playlistScaleX",
    )
    val queueScaleX by animateFloatAsState(
        targetValue = when {
            queuePressed -> 1.15f
            playlistPressed -> 0.88f
            repeatPressed -> 0.94f
            anyPressed -> 0.96f
            else -> 1f
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "queueScaleX",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp),
    ) {
        ExpressiveConnectedSlot(
            shape = startCap,
            weight = infoWeight,
            scaleX = infoScaleX * infoPulse.value,
            interactionSource = infoInteraction,
            onClick = {
                triggerWave(0)
                actions.onShowInfo()
            },
        ) {
            Icon(
                imageVector = SimpIcons.Info,
                contentDescription = "Info",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        if (isPlatformCastAvailable()) {
            ExpressiveConnectedSlot(
                shape = middle,
                weight = castWeight,
                scaleX = castScaleX * castPulse.value,
                interactionSource = castInteraction,
                onClick = null,
            ) {
                PlatformCastButton(
                    modifier = Modifier.size(24.dp),
                    tint = if (state.castState.isRemote) colorScheme.primary else colorScheme.onSurfaceVariant,
                )
            }
        }
        ExpressiveConnectedSlot(
            shape = middle,
            active = state.controllerState.isShuffle,
            weight = shuffleWeight,
            scaleX = shuffleScaleX * shufflePulse.value,
            interactionSource = shuffleInteraction,
            onClick = {
                triggerWave(2)
                actions.onUIEvent(UIEvent.Shuffle)
            },
        ) {
            Crossfade(
                targetState = state.controllerState.isShuffle,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "expressiveShuffleAnim",
            ) { isShuffle ->
                Icon(
                    imageVector = SimpIcons.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        ExpressiveConnectedSlot(
            shape = middle,
            active = state.controllerState.repeatState !is RepeatState.None,
            weight = repeatWeight,
            scaleX = repeatScaleX * repeatPulse.value,
            interactionSource = repeatInteraction,
            onClick = {
                triggerWave(3)
                actions.onUIEvent(UIEvent.Repeat)
            },
        ) {
            Crossfade(
                targetState = state.controllerState.repeatState,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "expressiveRepeatAnim",
            ) { rs ->
                val icon = when (rs) {
                    is RepeatState.None -> SimpIcons.Repeat
                    RepeatState.All -> SimpIcons.Repeat
                    RepeatState.One -> SimpIcons.RepeatOne
                }
                val iconTint = if (rs !is RepeatState.None) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        ExpressiveConnectedSlot(
            shape = middle,
            weight = playlistWeight,
            scaleX = playlistScaleX * playlistPulse.value,
            interactionSource = playlistInteraction,
            onClick = {
                triggerWave(4)
                actions.onShowAddToPlaylist()
            },
        ) {
            Icon(
                imageVector = SimpIcons.PlaylistAdd,
                contentDescription = "Add to Playlist",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        ExpressiveConnectedSlot(
            shape = endCap,
            weight = queueWeight,
            scaleX = queueScaleX * queuePulse.value,
            interactionSource = queueInteraction,
            onClick = {
                triggerWave(5)
                actions.onShowQueue()
            },
        ) {
            Icon(
                imageVector = SimpIcons.QueueMusic,
                contentDescription = "Queue",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun RowScope.ExpressiveConnectedSlot(
    shape: Shape,
    onClick: (() -> Unit)?,
    active: Boolean = false,
    weight: Float = 1f,
    scaleX: Float = 1f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    val containerColor by animateColorAsState(
        targetValue = if (active) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh,
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "slotContainerColor",
    )
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = containerColor,
            interactionSource = interactionSource,
            modifier =
                Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .graphicsLayer {
                        this.scaleX = scaleX
                        this.scaleY = 1.0f // function buttons (repeat, loop) pop on X axis, not Y and Z!
                        transformOrigin = TransformOrigin.Center
                    },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    } else {
        Surface(
            shape = shape,
            color = containerColor,
            modifier =
                Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .graphicsLayer {
                        this.scaleX = scaleX
                        this.scaleY = 1.0f // function buttons pop on X axis, not Y and Z!
                        transformOrigin = TransformOrigin.Center
                    },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
