package com.maxrave.simpmusic.ui.screen.other

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kyant.backdrop.highlight.Highlight
import com.maxrave.common.Config
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.home.Content
import com.maxrave.domain.data.model.searchResult.songs.Artist
import com.maxrave.domain.data.player.GenericCastState
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.simpmusic.expect.shareUrl
import com.maxrave.simpmusic.expect.ui.MediaPlayerView
import com.maxrave.simpmusic.expect.ui.PlatformCastButton
import com.maxrave.simpmusic.expect.ui.isPlatformCastAvailable
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.getStringBlocking
import com.maxrave.simpmusic.extension.hexToColorOrNull
import com.maxrave.simpmusic.extension.rgbFactor
import com.maxrave.simpmusic.extension.toImmersiveBackground
import com.maxrave.simpmusic.extension.toSquareThumbnailUrl
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.component.DescriptionView
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.HomeItemArtist
import com.maxrave.simpmusic.ui.component.HomeItemContentPlaylist
import com.maxrave.simpmusic.ui.component.HomeItemVideo
import com.maxrave.simpmusic.ui.component.LiquidGlassIconButton
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionState
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Check
import com.maxrave.simpmusic.ui.icon.IosShare
import com.maxrave.simpmusic.ui.icon.Movie
import com.maxrave.simpmusic.ui.icon.MovieOff
import com.maxrave.simpmusic.ui.icon.PersonAdd
import com.maxrave.simpmusic.ui.icon.Sensors
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.ArtistScreenState
import com.maxrave.simpmusic.viewModel.ArtistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.albums
import simpmusic.composeapp.generated.resources.baseline_favorite_24
import simpmusic.composeapp.generated.resources.description
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.featured_inArtist
import simpmusic.composeapp.generated.resources.liked_songs
import simpmusic.composeapp.generated.resources.liked_songs_by
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.more
import simpmusic.composeapp.generated.resources.no_description
import simpmusic.composeapp.generated.resources.popular
import simpmusic.composeapp.generated.resources.radio
import simpmusic.composeapp.generated.resources.related_artists
import simpmusic.composeapp.generated.resources.share
import simpmusic.composeapp.generated.resources.shuffle
import simpmusic.composeapp.generated.resources.singles
import simpmusic.composeapp.generated.resources.unknown
import simpmusic.composeapp.generated.resources.videos

@Composable
@ExperimentalMaterial3Api
fun ArtistScreen(
    channelId: String,
    viewModel: ArtistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
) {
    val artistScreenState by viewModel.artistScreenState.collectAsStateWithLifecycle()
    val isFollowed by viewModel.followed.collectAsStateWithLifecycle()
    val canvasUrl by viewModel.canvasUrl.collectAsStateWithLifecycle()
    // Header shows the canvas video by default; the top-right toggle swaps it for the artist's
    // picture. Keyed on the canvas so a different artist's canvas starts as video again.
    var showCanvasVideo by rememberSaveable(canvasUrl?.first) { mutableStateOf(true) }
    val headerCanvas = canvasUrl?.takeIf { showCanvasVideo }
    val shareTitle = stringResource(Res.string.share)
    val artistLogo by viewModel.artistLogo.collectAsStateWithLifecycle()

    val playingTrack by remember {
        sharedViewModel.nowPlayingState.map { it?.track?.videoId }
    }.collectAsState(null)

    // Choosing song to show Bottom sheet
    var choosingTrack by remember {
        mutableStateOf<Track?>(null)
    }
    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(channelId) {
        if (channelId != artistScreenState.data.channelId) {
            viewModel.browseArtist(channelId)
        }
    }

    // Apple Music-inspired immersive treatment. The header adapts to the window's aspect ratio
    // alone, not to the platform: a portrait window keeps the square artwork frame, a landscape
    // one (including every desktop window) uses a half-viewport-tall frame instead.
    val screenInfo = getScreenSizeInfo()
    val isPortrait = screenInfo.wDP < screenInfo.hDP

    // Palette extraction from the artist artwork (portrait Apple-style only).
    val paletteState = com.kmpalette.rememberPaletteState()
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var paletteGeneratedFor by remember { mutableStateOf<String?>(null) }
    val currentImageUrl = (artistScreenState as? ArtistScreenState.Success)?.data?.imageUrl

    LaunchedEffect(bitmap) {
        val bm = bitmap
        if (bm != null && currentImageUrl != null && paletteGeneratedFor != currentImageUrl) {
            paletteState.generate(bm)
            paletteGeneratedFor = currentImageUrl
        }
    }

    // Apple Music-style page background from the artwork's dominant tone (see UIExt.toImmersiveBackground).
    val mutedPaletteBg = paletteState.palette.toImmersiveBackground()
    // Tint for the description card, matching the non-portrait CollapsingToolbar color.
    val sectionTint = paletteState.palette.getColorFromPalette()

    // Accent color for the action buttons, sourced from the artist name-logo image's dominant
    // color (hidden catalog). Falls back to the theme's primary when no logo exists — a real
    // tonal colour rather than flat white, so the row still reads on light artwork.
    val logoAccent = artistLogo?.bgColorHex?.hexToColorOrNull()
    val themePrimary = MaterialTheme.colorScheme.primary
    val accentSeed = remember(logoAccent, themePrimary) {
        logoAccent ?: themePrimary
    }
    // Glide between accents when the artist changes rather than snapping: the logo colour
    // arrives after the screen has already opened, and a hard swap reads as a flicker.
    val accentAnimatable = remember { Animatable(accentSeed) }
    LaunchedEffect(accentSeed) {
        accentAnimatable.animateTo(
            targetValue = accentSeed,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        )
    }
    val artistAccent = accentAnimatable.value
    // Cast session state, so the row's Cast slot tints to signal an active route.
    val castState by sharedViewModel.castState.collectAsStateWithLifecycle()
    val lazyState = rememberLazyListState()
    val firstItemVisible by remember {
        derivedStateOf { lazyState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    Crossfade(artistScreenState) { state ->
        when (state) {
            is ArtistScreenState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CenterLoadingBox(
                        Modifier
                            .align(Alignment.Center),
                    )
                }
            }

            is ArtistScreenState.Success -> {
                // ---- Apple Music style (mobile portrait only) ----
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(mutedPaletteBg)
                                ,
                        state = lazyState,
                    ) {
                        item(contentType = "header") {
                            Column(
                                // Negative spacing pulls the action row up into the header AND
                                // shrinks the layout, so there's no leftover gap before "Popular"
                                // (unlike Modifier.offset, which only moves pixels, not layout).
                                verticalArrangement = Arrangement.spacedBy((-36).dp),
                            ) {
                                // Edge-to-edge artwork (canvas plays on top of it when available).
                                // Glass back button MUST be a sibling of the backdrop source
                                // (not a child) to avoid render feedback loop / RuntimeShader crash.
                                val artworkBackdrop = rememberBackdrop(Color.Black)
                                // Portrait fills a SQUARE frame, so the URL is clamped to a square
                                // size there (logic from commit 5e596c5b). Landscape keeps YouTube's
                                // own wide banner (e.g. w2880-h1200) instead: squaring the source
                                // first would make the Crop below throw away most of its height.
                                val headerImageUrl =
                                    state.data.imageUrl?.let {
                                        if (isPortrait) it.toSquareThumbnailUrl() else it
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (isPortrait) {
                                                    Modifier.aspectRatio(1f)
                                                } else {
                                                    // The banner's own shape, so it is shown whole instead of
                                                    // cropped into a fixed hDP/2 strip. 2.4:1 (YouTube's
                                                    // w2880-h1200) until it decodes; a square bitmap left over
                                                    // from portrait is ignored so the frame never goes square.
                                                    Modifier.aspectRatio(
                                                        bitmap?.takeIf { it.width > it.height }?.let { it.width.toFloat() / it.height } ?: 2.4f,
                                                    )
                                                },
                                            ),
                                ) {
                                    // Inner Box — backdrop SOURCE (artwork + canvas + overlays, NO glass)
                                    Box(modifier = Modifier.fillMaxSize().clipToBounds().layerBackdrop(artworkBackdrop)) {
                                        // Media layer (artwork + canvas).
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalPlatformContext.current)
                                                        .data(headerImageUrl)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(headerImageUrl)
                                                        .memoryCacheKey(headerImageUrl)
                                                        .crossfade(false)
                                                        .build(),
                                                placeholder = rememberHolderPainter(),
                                                error = rememberHolderPainter(),
                                                contentDescription = null,
                                                // Crop properly fills the frame in both portrait (1:1 square)
                                                // and landscape/tablet modes without letterboxing or empty bands.
                                                contentScale = ContentScale.Crop,
                                                // Always decoded so the page background color can be extracted
                                                // from the artwork palette, even when a canvas is playing.
                                                onSuccess = {
                                                    bitmap = it.result.image.toImageBitmap()
                                                },
                                                // Hidden (but still decoded above) while a canvas is present —
                                                // the canvas is shown instead. No canvas -> artwork is shown.
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .alpha(if (headerCanvas != null) 0f else 1f),
                                            )
                                            // Canvas (Spotify) plays AS the background when present;
                                            // otherwise the static artwork above is the fallback.
                                            headerCanvas?.let { canvas ->
                                                // Canvas is a tall/portrait video. cropToBounds center
                                                // scale-to-covers it into the header frame (ContentScale.Crop):
                                                // true video aspect ratio, no stretch, overflow clipped.
                                                MediaPlayerView(
                                                    url = canvas.first,
                                                    modifier = Modifier.fillMaxSize(),
                                                    cropToBounds = true,
                                                )
                                            }
                                        } // end media layer
                                        // 5% black over the artwork/canvas, under the scrim, so
                                        // a bright photo sits back a little behind the title.
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.05f)),
                                        )
                                        // Color scrim: smoothly fades into the page background (mutedPaletteBg)
                                        // over the bottom 40% of the header, keeping the artist title and
                                        // controls legible while leaving the artist picture clear and unblocked.
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(0.40f)
                                                    .align(Alignment.BottomCenter)
                                                    .background(artworkScrimBrush(mutedPaletteBg)),
                                        )
                                        // Artist name (TEXT for now — logo image is roadmap) + subscriber · view
                                        Column(
                                            modifier =
                                                Modifier
                                                    .align(Alignment.BottomCenter)
                                                    // Lift the name + subtitle together with the action row.
                                                    .offset(y = (-36).dp)
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp)
                                                    .padding(bottom = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            val logo = artistLogo
                                            if (logo != null) {
                                                // Artist name rendered as a logo image (hidden catalog),
                                                // in place of the plain-text title.
                                                AsyncImage(
                                                    model = logo.logoUrl,
                                                    contentDescription = state.data.title,
                                                    contentScale = ContentScale.Fit,
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth(0.7f)
                                                            .heightIn(max = 84.dp),
                                                )
                                            } else {
                                                Text(
                                                    text = state.data.title ?: stringResource(Res.string.unknown),
                                                    style = typo().titleLarge,
                                                    color = Color.White,
                                                    maxLines = 2,
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                            val meta =
                                                listOfNotNull(
                                                    state.data.subscribers?.takeIf { it.isNotBlank() },
                                                    state.data.playCount?.takeIf { it.isNotBlank() },
                                                ).joinToString(" · ")
                                            if (meta.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = meta,
                                                    style = typo().bodyMedium,
                                                    color = Color(0xC4FFFFFF),
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                    }
                                    // Back button — liquid glass, sibling of the backdrop source.
                                    LiquidGlassIconButton(
                                        backdrop = artworkBackdrop,
                                        imageVector = SimpIcons.ArrowBackIosNew,
                                        shape = RoundedCornerShape(24.dp),
                                        // Matching the other three headers: the pill-style directional rim, thickened
                                        // from the 0.5.dp default so it stays visible around a 48dp circle.
                                        highlight = Highlight(width = 1.dp),
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .padding(12.dp)
                                                .windowInsetsPadding(WindowInsets.statusBars)
                                                .size(48.dp),
                                    ) {
                                        navController.navigateUp()
                                    }
                                    // Top-right pill mirroring the back button, shaped like the
                                    // Playlist header's: [canvas ⇄ picture] when a canvas exists, then
                                    // share. A sibling of the backdrop source, like the back button.
                                    Row(
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(12.dp)
                                                .windowInsetsPadding(WindowInsets.statusBars)
                                                .height(48.dp)
                                                .liquidGlass(artworkBackdrop, RoundedCornerShape(24.dp)),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (canvasUrl != null) {
                                            IconButton(onClick = { showCanvasVideo = !showCanvasVideo }) {
                                                Icon(
                                                    imageVector = if (showCanvasVideo) SimpIcons.MovieOff else SimpIcons.Movie,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { shareUrl(shareTitle, "https://music.youtube.com/channel/$channelId") },
                                        ) {
                                            Icon(SimpIcons.IosShare, contentDescription = shareTitle, tint = Color.White)
                                        }
                                    }
                                }

                                // Material 3 Expressive action row: [Radio][Cast][Shuffle Pill CTA][Follow]
                                // Built with tactile spring physics, morphing shapes, and ease-in-out transitions.
                                ArtistActionRow(
                                    state = state,
                                    artistAccent = artistAccent,
                                    mutedPaletteBg = mutedPaletteBg,
                                    isFollowed = isFollowed,
                                    castState = castState,
                                    viewModel = viewModel,
                                )
                            }
                        }
                        item(contentType = "sections") {
                            ArtistSections(
                                channelId = channelId,
                                state = state,
                                selectionState = selectionState,
                                playingTrack = playingTrack,
                                descriptionTint = sectionTint,
                                navController = navController,
                                viewModel = viewModel,
                                sharedViewModel = sharedViewModel,
                                onTrackMore = { track ->
                                    choosingTrack = track
                                    showBottomSheet = true
                                },
                            )
                        }
                    }

                    // Haze top bar appears once the header scrolls away.
                    AnimatedVisibility(
                        visible = shouldHideTopBar && !selectionState.isActive,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = state.data.title ?: "",
                                    style = typo().titleMedium,
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
                            },
                            navigationIcon = {
                                Box(Modifier.padding(horizontal = 5.dp)) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = SimpIcons.ArrowBackIosNew,
                                            contentDescription = "Back",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            },
                            colors =
                                TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                ),
                            modifier =
                                Modifier.background(mutedPaletteBg),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = selectionState.isActive,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    SongSelectionTopAppBar(
                        state = selectionState,
                        onSelectAll = {
                            selectionState.toggleSelectAll(
                                (artistScreenState as? ArtistScreenState.Success)
                                    ?.data
                                    ?.popularSongs
                                    ?.map { it.videoId }
                                    ?: emptyList(),
                            )
                        },
                        onOpenActions = { showSelectionSheet = true },
                        containerColor = Color.Black,
                    )
                }
                if (showSelectionSheet) {
                    val selectedIds = selectionState.selected.toList()
                    SelectedSongsBottomSheet(
                        count = selectedIds.size,
                        onDismiss = { showSelectionSheet = false },
                        onPlayNext = {
                            selectionViewModel.playNext(selectedIds)
                            selectionState.exit()
                        },
                        onAddToQueue = {
                            selectionViewModel.addToQueue(selectedIds)
                            selectionState.exit()
                        },
                        onAddToPlaylist = { showSelectionAddToPlaylist = true },
                        onDownload = {
                            selectionViewModel.download(selectedIds)
                            selectionState.exit()
                        },
                        onAddToFavorite = {
                            selectionViewModel.addToFavorite(selectedIds)
                            selectionState.exit()
                        },
                    )
                }
                if (showSelectionAddToPlaylist) {
                    val selectedIds = selectionState.selected.toList()
                    val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
                    AddToPlaylistModalBottomSheet(
                        isBottomSheetVisible = true,
                        listLocalPlaylist = localPlaylists,
                        listYouTubePlaylist = emptyList(),
                        onDismiss = { showSelectionAddToPlaylist = false },
                        onClick = { playlist ->
                            selectionViewModel.addToPlaylist(playlist.id, selectedIds)
                            selectionState.exit()
                        },
                        onYTPlaylistClick = {},
                    )
                }
                if (showBottomSheet && choosingTrack != null) {
                    NowPlayingBottomSheet(
                        onDismiss = {
                            showBottomSheet = false
                            choosingTrack = null
                        },
                        navController = navController,
                        song = choosingTrack?.toSongEntity(),
                    )
                }
            }

            is ArtistScreenState.Error -> {
                viewModel.makeToast(state.message ?: stringResource(Res.string.error))
                navController.navigateUp()
            }
        }
    }
}

/**
 * Material 3 Expressive Action Row: [Radio][Cast][Shuffle Pill CTA][Follow].
 *
 * Implements M3 Expressive motion physics:
 * - Tactile bouncy spring physics on press (dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow).
 * - Staggered spring entrance: each button pops in 70ms apart, scale 0 → 1 with a bouncy
 *   spring, so the row assembles itself when the artist page opens.
 * - Smooth ease-in-out shape morphing (26dp pill ↔ 16dp squircle) with FastOutSlowInEasing.
 * - Dynamic color transitions with FastOutSlowInEasing.
 * - Bouncy icon scale and ease-in-out fade transitions for the Follow toggle state.
 *
 * The Cast slot renders only where Cast exists ([isPlatformCastAvailable]); the surrounding
 * container belongs to the row, so it is gated here rather than by the button itself.
 */
@Composable
private fun ArtistActionRow(
    state: ArtistScreenState.Success,
    artistAccent: Color,
    mutedPaletteBg: Color,
    isFollowed: Boolean,
    castState: GenericCastState,
    viewModel: ArtistViewModel,
    modifier: Modifier = Modifier,
) {
    // === Entrance: staggered spring pop-in, one button per 70ms ===
    val radioAppearance = remember { Animatable(0f) }
    val shuffleAppearance = remember { Animatable(0f) }
    val followAppearance = remember { Animatable(0f) }
    LaunchedEffect(state.data.channelId) {
        // Re-run whenever the artist changes.
        radioAppearance.snapTo(0f)
        shuffleAppearance.snapTo(0f)
        followAppearance.snapTo(0f)
        val appearanceSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
        // Cast rides the Radio step: it is adjacent to it in the row, so the two land together
        // and the row never appears to grow a slot after it has already assembled.
        launch {
            radioAppearance.animateTo(1f, appearanceSpring)
        }
        launch {
            delay(ENTRANCE_STAGGER_STEP_MS)
            shuffleAppearance.animateTo(1f, appearanceSpring)
        }
        launch {
            delay(ENTRANCE_STAGGER_STEP_MS * 2)
            followAppearance.animateTo(1f, appearanceSpring)
        }
    }

    val radioInteraction = remember { MutableInteractionSource() }
    val isRadioPressed by radioInteraction.collectIsPressedAsState()
    val radioScale by animateFloatAsState(
        targetValue = if (isRadioPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "artistRadioScale",
    )
    val radioCorner by animateDpAsState(
        targetValue = if (isRadioPressed) 16.dp else 26.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "artistRadioCorner",
    )
    val radioContainerColor by animateColorAsState(
        targetValue = artistAccent.copy(alpha = if (isRadioPressed) 0.22f else 0.10f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "artistRadioContainer",
    )
    val radioBorderColor by animateColorAsState(
        targetValue = artistAccent.copy(alpha = if (isRadioPressed) 0.85f else 0.40f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "artistRadioBorder",
    )

    // Cast shares the Radio button's container language — same squircle in the same row — but
    // its colour and border also react to an active route, so the session state is legible
    // without the glyph alone.
    val isCastRemote = castState.isRemote
    val castCorner by animateDpAsState(
        targetValue = if (isCastRemote) 16.dp else 26.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "artistCastCorner",
    )
    val castContainerColor by animateColorAsState(
        targetValue = if (isCastRemote) artistAccent.copy(alpha = 0.28f) else artistAccent.copy(alpha = 0.10f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "artistCastContainer",
    )
    val castBorderColor by animateColorAsState(
        targetValue = if (isCastRemote) artistAccent.copy(alpha = 0.85f) else artistAccent.copy(alpha = 0.40f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "artistCastBorder",
    )

    val shuffleInteraction = remember { MutableInteractionSource() }
    val isShufflePressed by shuffleInteraction.collectIsPressedAsState()
    val shuffleScale by animateFloatAsState(
        targetValue = if (isShufflePressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "artistShuffleScale",
    )
    val shuffleCorner by animateDpAsState(
        targetValue = if (isShufflePressed) 16.dp else 26.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "artistShuffleCorner",
    )

    val followInteraction = remember { MutableInteractionSource() }
    val isFollowPressed by followInteraction.collectIsPressedAsState()
    val followScale by animateFloatAsState(
        targetValue = if (isFollowPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "artistFollowScale",
    )
    val followCorner by animateDpAsState(
        targetValue = if (isFollowPressed) 16.dp else 26.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "artistFollowCorner",
    )
    val followContainerColor by animateColorAsState(
        targetValue = if (isFollowed) artistAccent else artistAccent.copy(alpha = if (isFollowPressed) 0.22f else 0.10f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "artistFollowContainer",
    )
    val followBorderColor by animateColorAsState(
        targetValue = if (isFollowed) Color.Transparent else artistAccent.copy(alpha = if (isFollowPressed) 0.85f else 0.40f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "artistFollowBorder",
    )
    val followContentColor by animateColorAsState(
        targetValue = if (isFollowed) mutedPaletteBg else artistAccent,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "artistFollowContent",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Radio / Stations Button
        Surface(
            onClick = {
                val param = state.data.radioParam
                if (param != null) {
                    viewModel.onRadioClick(param)
                } else {
                    viewModel.makeToast(runBlocking { getString(Res.string.error) })
                }
            },
            shape = RoundedCornerShape(radioCorner),
            color = radioContainerColor,
            border = BorderStroke(1.5.dp, radioBorderColor),
            interactionSource = radioInteraction,
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer {
                    scaleX = radioScale * radioAppearance.value
                    scaleY = radioScale * radioAppearance.value
                    alpha = radioAppearance.value
                },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = SimpIcons.Sensors,
                    contentDescription = stringResource(Res.string.radio),
                    tint = artistAccent,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Cast Button — the platform button owns its own click handling and hides itself when
        // no receiver is reachable, so it is hosted in a NON-clickable container gated on the
        // same availability flag (a clickable wrapper would swallow its taps). Tint signals an
        // active session.
        if (isPlatformCastAvailable()) {
            Surface(
                shape = RoundedCornerShape(castCorner),
                color = castContainerColor,
                border = BorderStroke(1.5.dp, castBorderColor),
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        scaleX = radioAppearance.value
                        scaleY = radioAppearance.value
                        alpha = radioAppearance.value
                    },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    PlatformCastButton(
                        modifier = Modifier.size(24.dp),
                        tint = if (isCastRemote) artistAccent else artistAccent.copy(alpha = 0.85f),
                    )
                }
            }
        }

        // Shuffle Primary Hero CTA Pill Button
        Surface(
            onClick = {
                val param = state.data.shuffleParam
                if (param != null) {
                    viewModel.onShuffleClick(param)
                } else {
                    viewModel.makeToast(runBlocking { getString(Res.string.error) })
                }
            },
            shape = RoundedCornerShape(shuffleCorner),
            color = artistAccent,
            contentColor = mutedPaletteBg,
            interactionSource = shuffleInteraction,
            modifier = Modifier
                .height(52.dp)
                .graphicsLayer {
                    scaleX = shuffleScale * shuffleAppearance.value
                    scaleY = shuffleScale * shuffleAppearance.value
                    alpha = shuffleAppearance.value
                },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = SimpIcons.Shuffle,
                    contentDescription = stringResource(Res.string.shuffle),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.shuffle),
                    style = typo().labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }

        // Follow Toggle Button with Bouncy Icon Scale & Morphing
        Surface(
            onClick = {
                state.data.channelId?.let { chId ->
                    viewModel.updateFollowed(
                        if (isFollowed) 0 else 1,
                        chId,
                    )
                }
            },
            shape = RoundedCornerShape(followCorner),
            color = followContainerColor,
            border = if (!isFollowed) BorderStroke(1.5.dp, followBorderColor) else null,
            interactionSource = followInteraction,
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer {
                    scaleX = followScale * followAppearance.value
                    scaleY = followScale * followAppearance.value
                    alpha = followAppearance.value
                },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = isFollowed,
                    transitionSpec = {
                        (fadeIn(
                            animationSpec = tween(220, delayMillis = 30, easing = FastOutSlowInEasing),
                        ) + scaleIn(
                            initialScale = 0.65f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        )).togetherWith(
                            fadeOut(
                                animationSpec = tween(150, easing = FastOutSlowInEasing),
                            ) + scaleOut(
                                targetScale = 0.65f,
                                animationSpec = tween(150, easing = FastOutSlowInEasing),
                            ),
                        )
                    },
                    label = "artistFollowIconMorph",
                ) { followed ->
                    Icon(
                        imageVector = if (followed) SimpIcons.Check else SimpIcons.PersonAdd,
                        contentDescription = if (followed) "Followed" else "Follow",
                        tint = followContentColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/** Per-button step of [ArtistActionRow]'s staggered entrance. */
private const val ENTRANCE_STAGGER_STEP_MS = 70L

/**
 * Shared artist body (Popular → Description). Used by both the portrait Apple-Music layout
 * and the existing CollapsingToolbar layout, so the sections themselves stay untouched.
 */
@Composable
private fun ArtistSections(
    channelId: String,
    state: ArtistScreenState.Success,
    selectionState: SongSelectionState,
    playingTrack: String?,
    descriptionTint: Color,
    navController: NavController,
    viewModel: ArtistViewModel,
    sharedViewModel: SharedViewModel,
    onTrackMore: (Track) -> Unit,
) {
    val likedSongCount by viewModel.likedSongCount.collectAsStateWithLifecycle()
    Column {
        // Liked songs by this artist (issue #2524), shaped like Spotify's section: a heading, then
        // the artist's picture wearing the liked heart beside the count. Shown only once at least
        // one song is liked; opens the full list with the route's channelId, the id it was counted by.
        androidx.compose.animation.AnimatedVisibility(likedSongCount > 0) {
            LikedSongsSection(
                imageUrl = state.data.imageUrl?.toSquareThumbnailUrl(),
                count = likedSongCount,
                artistName = state.data.title.orEmpty(),
                onClick = {
                    navController.navigate(
                        LibraryDynamicPlaylistDestination(
                            type = LibraryDynamicPlaylistType.ArtistLiked(channelId).toStringParams(),
                        ),
                    )
                },
            )
        }

        // Popular Songs
        AnimatedVisibility(state.data.popularSongs.isNotEmpty()) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.popular),
                        style = typo().labelMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            val id = state.data.listSongParam
                            if (id != null) {
                                navController.navigate(PlaylistDestination(id))
                            } else {
                                viewModel.makeToast(runBlocking { getString(Res.string.error) })
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = Color.White,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                state.data.popularSongs.forEach { song ->
                    SongFullWidthItems(
                        forceDark = true,                        track = song,
                        isPlaying = song.videoId == playingTrack,
                        modifier = Modifier.fillMaxWidth(),
                        onMoreClickListener = {
                            onTrackMore(song)
                        },
                        onClickListener = {
                            val firstQueue: Track = song
                            viewModel.setQueueData(
                                QueueData.Data(
                                    listTracks = arrayListOf(firstQueue),
                                    firstPlayedTrack = firstQueue,
                                    playlistId = "RDAMVM${song.videoId}",
                                    playlistName = "\"${state.data.title ?: ""}\" ${getStringBlocking(Res.string.popular)}",
                                    playlistType = PlaylistType.RADIO,
                                    continuation = null,
                                ),
                            )
                            viewModel.loadMediaItem(
                                firstQueue,
                                type = Config.SONG_CLICK,
                            )
                        },
                        onAddToQueue = {
                            sharedViewModel.addListToQueue(
                                arrayListOf(song),
                            )
                        },
                        selectionMode = selectionState.isActive,
                        isSelected = selectionState.isSelected(song.videoId),
                        onLongClick = { selectionState.start(it) },
                        onSelectToggle = { selectionState.toggle(it) },
                    )
                }
            }
        }

        // Singles
        AnimatedVisibility(
            state.data.singles != null &&
                state.data.singles!!
                    .results
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.singles),
                        style = typo().labelMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            if (state.data.channelId != null) {
                                val id = "MPAD${state.data.channelId}"
                                navController.navigate(
                                    MoreAlbumsDestination(
                                        id = id,
                                        type = MoreAlbumsDestination.SINGLE_TYPE,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(getStringBlocking(Res.string.error))
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = Color.White,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.singles?.results ?: emptyList()) { single ->
                        HomeItemContentPlaylist(
                            forceDark = true,                            onClick = {
                                navController.navigate(
                                    AlbumDestination(
                                        single.browseId,
                                    ),
                                )
                            },
                            data = single,
                            thumbSize = 180.dp,
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Albums
        AnimatedVisibility(
            state.data.albums != null &&
                state.data.albums!!
                    .results
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.albums),
                        style = typo().labelMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            if (state.data.channelId != null) {
                                val id = "MPAD${state.data.channelId}"
                                navController.navigate(
                                    MoreAlbumsDestination(
                                        id = id,
                                        type = MoreAlbumsDestination.ALBUM_TYPE,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(getStringBlocking(Res.string.error))
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = Color.White,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.albums?.results ?: emptyList()) { album ->
                        HomeItemContentPlaylist(
                            forceDark = true,                            onClick = {
                                navController.navigate(
                                    AlbumDestination(
                                        browseId = album.browseId,
                                    ),
                                )
                            },
                            data = album,
                            thumbSize = 180.dp,
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Videos
        AnimatedVisibility(
            state.data.video != null &&
                state.data.video!!
                    .video
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.videos),
                        style = typo().labelMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            val videoListParam = state.data.video?.videoListParam
                            if (videoListParam != null) {
                                navController.navigate(
                                    PlaylistDestination(
                                        videoListParam,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(getStringBlocking(Res.string.error))
                            }
                        },
                        colors =
                            ButtonDefaults
                                .textButtonColors()
                                .copy(
                                    contentColor = Color.White,
                                ),
                    ) {
                        Text(stringResource(Res.string.more), style = typo().bodySmall)
                    }
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.video?.video ?: emptyList()) { video ->
                        HomeItemVideo(
                            forceDark = true,                            onClick = {
                                val firstQueue: Track = video
                                viewModel.setQueueData(
                                    QueueData.Data(
                                        listTracks = arrayListOf(firstQueue),
                                        firstPlayedTrack = firstQueue,
                                        playlistId = "RDAMVM${video.videoId}",
                                        playlistName = (state.data.title ?: "") + getStringBlocking(Res.string.videos),
                                        playlistType = PlaylistType.RADIO,
                                        continuation = null,
                                    ),
                                )
                                viewModel.loadMediaItem(
                                    firstQueue,
                                    type = Config.VIDEO_CLICK,
                                )
                            },
                            onLongClick = {
                                onTrackMore(video)
                            },
                            data =
                                Content(
                                    album = null,
                                    artists = video.artists,
                                    description = null,
                                    isExplicit = video.isExplicit,
                                    playlistId = null,
                                    browseId = null,
                                    thumbnails = video.thumbnails ?: emptyList(),
                                    title = video.title,
                                    videoId = video.videoId,
                                    views = video.views,
                                ),
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Feature on
        AnimatedVisibility(state.data.featuredOn.isNotEmpty()) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.featured_inArtist),
                        style = typo().labelMedium,
                        color = Color.White,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp),
                    )
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.featuredOn) { feature ->
                        HomeItemContentPlaylist(
                            forceDark = true,                            onClick = {
                                navController.navigate(
                                    PlaylistDestination(
                                        feature.id,
                                    ),
                                )
                            },
                            data = feature,
                            thumbSize = 180.dp,
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }

        // Related
        AnimatedVisibility(
            state.data.related != null &&
                state.data.related!!
                    .results
                    .isNotEmpty(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.related_artists),
                        style = typo().labelMedium,
                        color = Color.White,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp),
                    )
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                    items(state.data.related?.results ?: emptyList()) { related ->
                        HomeItemArtist(
                            forceDark = true,                            onClick = {
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = related.browseId,
                                    ),
                                )
                            },
                            data =
                                Content(
                                    album = null,
                                    artists =
                                        listOf(
                                            Artist(
                                                id = related.browseId,
                                                name = related.title,
                                            ),
                                        ),
                                    description = related.subscribers,
                                    isExplicit = null,
                                    playlistId = null,
                                    browseId = related.browseId,
                                    thumbnails = related.thumbnails,
                                    title = related.title,
                                    videoId = null,
                                    views = null,
                                    durationSeconds = null,
                                    radio = null,
                                ),
                        )
                    }
                    item {
                        Spacer(Modifier.size(10.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Text(
                text = stringResource(Res.string.description),
                style = typo().labelMedium,
                color = Color.White,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
            )
        }
        val urlHandler = LocalUriHandler.current
        Card(
            modifier = Modifier.padding(horizontal = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors =
                CardDefaults.elevatedCardColors().copy(
                    containerColor = descriptionTint.rgbFactor(0.5f),
                ),
        ) {
            DescriptionView(
                modifier = Modifier.padding(16.dp),
                text = state.data.description ?: stringResource(Res.string.no_description),
                limitLine = 5,
                onTimeClicked = {},
                onURLClicked = { url ->
                    urlHandler.openUri(url)
                },
            )
        }
        EndOfPage()
    }
}

/**
 * "Liked songs" as a section of its own: the heading, then the artist's picture wearing the liked
 * heart beside "3 songs" / "By <artist>". The whole block opens
 * [LibraryDynamicPlaylistType.ArtistLiked]. Heading and text colours follow the sections around it,
 * which draw white on the artwork-tinted page.
 */
@Composable
private fun LikedSongsSection(
    imageUrl: String?,
    count: Int,
    artistName: String,
    onClick: () -> Unit,
) {
    Column {
        // No "More" button beside it, so the padding stands in for the height the TextButton gives
        // the Popular and Singles headings.
        Text(
            text = stringResource(Res.string.liked_songs),
            style = typo().labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Box(modifier = Modifier.size(48.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
                Image(
                    painter = painterResource(Res.drawable.baseline_favorite_24),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .padding(3.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = pluralStringResource(Res.plurals.liked_songs_count, count, count),
                    style = typo().titleSmall,
                    color = Color.White,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(Res.string.liked_songs_by, artistName),
                    style = typo().bodySmall,
                    color = Color(0xC4FFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
