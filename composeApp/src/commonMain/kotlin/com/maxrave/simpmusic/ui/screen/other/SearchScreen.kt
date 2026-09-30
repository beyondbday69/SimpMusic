package com.maxrave.simpmusic.ui.screen.other

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.maxrave.common.Config
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.intent.GenericIntent
import com.maxrave.domain.data.model.searchResult.albums.AlbumsResult
import com.maxrave.domain.data.model.searchResult.artists.ArtistsResult
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.model.searchResult.songs.SongsResult
import com.maxrave.domain.data.model.searchResult.videos.VideosResult
import com.maxrave.domain.data.type.SearchResultType
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.connectArtists
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.AudioPlayingIndicator
import com.maxrave.simpmusic.ui.component.MoodCategoryCard
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.extension.getStringBlocking
import com.maxrave.simpmusic.extension.toAppDeepLinkOrNull
import com.maxrave.simpmusic.ui.component.ArtistFullWidthItems
import com.maxrave.simpmusic.ui.component.Chip
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.PlaylistFullWidthItems
import com.maxrave.simpmusic.ui.component.ShimmerSearchItem
import com.maxrave.simpmusic.ui.component.SimpMusicChartButton
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
import com.maxrave.simpmusic.ui.icon.ArrowOutward
import com.maxrave.simpmusic.ui.icon.Check
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.Error
import com.maxrave.simpmusic.ui.icon.History
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.MoodDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PodcastDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.SearchScreenUIState
import com.maxrave.simpmusic.viewModel.SearchType
import com.maxrave.simpmusic.viewModel.SearchViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import com.maxrave.simpmusic.viewModel.toStringRes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.album
import simpmusic.composeapp.generated.resources.albums
import simpmusic.composeapp.generated.resources.artists
import simpmusic.composeapp.generated.resources.clear_search_history
import simpmusic.composeapp.generated.resources.error_occurred
import simpmusic.composeapp.generated.resources.everything_you_need
import simpmusic.composeapp.generated.resources.in_search
import simpmusic.composeapp.generated.resources.no_results_found
import simpmusic.composeapp.generated.resources.playlist
import simpmusic.composeapp.generated.resources.playlists
import simpmusic.composeapp.generated.resources.podcasts
import simpmusic.composeapp.generated.resources.popular
import simpmusic.composeapp.generated.resources.retry
import simpmusic.composeapp.generated.resources.search_for
import simpmusic.composeapp.generated.resources.search_for_songs_artists_albums_playlists_and_more
import simpmusic.composeapp.generated.resources.song
import simpmusic.composeapp.generated.resources.suggest
import simpmusic.composeapp.generated.resources.videos
import simpmusic.composeapp.generated.resources.what_do_you_want_to_listen_to

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = koinInject(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
) {
    val uriHandler = LocalUriHandler.current
    val focusManager = LocalFocusManager.current
    val motionScheme = MaterialTheme.motionScheme
    val searchScreenState by searchViewModel.searchScreenState.collectAsStateWithLifecycle()
    val uiState by searchViewModel.searchScreenUIState.collectAsStateWithLifecycle()
    val searchHistory by searchViewModel.searchHistory.collectAsStateWithLifecycle()
    val moodAndGenres by searchViewModel.moodAndGenres.collectAsStateWithLifecycle()
    val moodArtwork by searchViewModel.moodArtwork.collectAsStateWithLifecycle()

    var searchUIType by rememberSaveable { mutableStateOf(SearchUIType.EMPTY) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isSearchSubmitted by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    var isFocused by rememberSaveable { mutableStateOf(false) }

    // The bar floats OVER the content (a Box, not a Column) so there is something behind it to
    // blur — same arrangement HomeScreen uses. Each branch owns a scroll state, hoisted here so
    // the bar can tell whether the branch currently on screen is scrolled away from the top.
    // Two columns only on a phone held upright. Anywhere wider — tablet, landscape, desktop — two
    // columns stretch each tile to half the window, and since the tile keeps a 2:1 ratio it grows
    // absurdly tall with it.
    val screenInfo = getScreenSizeInfo()
    val isMobilePortrait = getPlatform() == Platform.Android && screenInfo.wDP < screenInfo.hDP
    val moodGridColumns = if (isMobilePortrait) 2 else 4
    val suggestionsState = rememberLazyListState()
    val historyState = rememberLazyListState()
    val moodGridState = rememberLazyGridState()
    val resultsState = rememberLazyListState()
    var searchBarHeightPx by remember { mutableIntStateOf(0) }
    val searchBarHeight = with(LocalDensity.current) { searchBarHeightPx.toDp() }
    val isContentAtTop by remember {
        derivedStateOf {
            when (searchUIType) {
                SearchUIType.EMPTY ->
                    moodGridState.firstVisibleItemIndex == 0 && moodGridState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_HISTORY ->
                    historyState.firstVisibleItemIndex == 0 && historyState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_SUGGESTIONS ->
                    suggestionsState.firstVisibleItemIndex == 0 && suggestionsState.firstVisibleItemScrollOffset == 0
                SearchUIType.SEARCH_RESULTS ->
                    resultsState.firstVisibleItemIndex == 0 && resultsState.firstVisibleItemScrollOffset == 0
            }
        }
    }

    val searchForString = stringResource(Res.string.search_for)
    val songString = stringResource(Res.string.song).lowercase()
    val artistString = stringResource(Res.string.artists).lowercase()
    val albumString = stringResource(Res.string.albums).lowercase()
    val playlistString = stringResource(Res.string.playlists).lowercase()
    val videoString = stringResource(Res.string.videos).lowercase()
    val podcastString = stringResource(Res.string.podcasts).lowercase()

    // Animated Placeholder
    val placeholderTexts =
        remember {
            listOf(
                "$searchForString $songString...",
                "$searchForString $artistString...",
                "$searchForString $albumString...",
                "$searchForString $playlistString...",
                "$searchForString $videoString...",
                "$searchForString $podcastString...",
            )
        }

    var currentPlaceholderIndex by remember { mutableIntStateOf(0) }

    // Animate placeholder - pause when focused
    LaunchedEffect(isFocused) {
        while (!isFocused) {
            delay(3000) // Change every 3 seconds
            currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholderTexts.size
        }
    }

    var sheetSong by remember { mutableStateOf<SongEntity?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    val currentVideoId by searchViewModel.nowPlayingVideoId.collectAsStateWithLifecycle()
    val chipRowState = rememberScrollState()
    val pullToRefreshState = rememberPullToRefreshState()

    val onMoreClick: (SongEntity) -> Unit = { song ->
        sheetSong = song
        showBottomSheet = true
    }

    LaunchedEffect(searchText) {
        if (isFocused) {
            isSearchSubmitted = false
            isExpanded = true
        }
        if (searchText.isNotEmpty() && isFocused) {
            searchViewModel.suggestQuery(searchText)
        }
    }

    LaunchedEffect(isSearchSubmitted) {
        if (isSearchSubmitted) {
            isExpanded = false
        }
    }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            isExpanded = true
        }
    }

    LaunchedEffect(isExpanded, searchText, isFocused) {
        searchUIType =
            if (searchText.isNotEmpty() && isExpanded) {
                SearchUIType.SEARCH_SUGGESTIONS
            } else if (isFocused && isExpanded) {
                SearchUIType.SEARCH_HISTORY
            } else if (searchText.isEmpty()) {
                SearchUIType.EMPTY
            } else {
                SearchUIType.SEARCH_RESULTS
            }
    }

    //On search icon click while on search screen, open keyboard. Android only feature
    if (getPlatform() == Platform.Android) {
        val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(reloadDestination) {
            if (reloadDestination == SearchDestination::class) {
                if (!selectionState.isActive && searchUIType == SearchUIType.EMPTY) {
                    isExpanded = true
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                sharedViewModel.reloadDestinationDone()
            }
        }
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
    if (showBottomSheet) {
        NowPlayingBottomSheet(
            onDismiss = {
                showBottomSheet = false
                sheetSong = null
            },
            navController = navController,
            song = sheetSong,
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Transparent),
    ) {
        // Content scrolls under the bar (it is the haze source), so it needs top padding
        // equal to the bar's measured height to keep its first item clear of it.
        Crossfade(
            targetState = searchUIType,
            animationSpec = motionScheme.fastEffectsSpec(),
            modifier = Modifier.fillMaxSize(),
        ) {
            when (it) {
                SearchUIType.SEARCH_SUGGESTIONS -> {
                    val suggestSongs = remember(searchScreenState.suggestYTItems) {
                        searchScreenState.suggestYTItems.filter { it is SongsResult || it is VideosResult }
                    }
                    val suggestOtherEntities = remember(searchScreenState.suggestYTItems) {
                        searchScreenState.suggestYTItems.filter { it !is SongsResult && it !is VideosResult }
                    }

                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        state = suggestionsState,
                        contentPadding = PaddingValues(top = searchBarHeight + 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // 1. Songs & Playable Tracks FIRST
                        if (suggestSongs.isNotEmpty()) {
                            item(key = "header_songs") {
                                Text(
                                    text = stringResource(Res.string.song),
                                    style = typo().titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 2.dp)
                                            .animateItem(
                                                fadeInSpec = motionScheme.fastEffectsSpec(),
                                                fadeOutSpec = motionScheme.fastEffectsSpec(),
                                                placementSpec = motionScheme.defaultSpatialSpec(),
                                            ),
                                )
                            }
                            itemsIndexed(
                                items = suggestSongs,
                                key = { index, item ->
                                    val baseKey =
                                        when (item) {
                                            is SongsResult -> "song_${item.videoId}"
                                            is VideosResult -> "video_${item.videoId}"
                                            else -> item.hashCode().toString()
                                        }
                                    "${baseKey}_$index"
                                },
                            ) { _, item ->
                                SuggestItemRow(
                                    searchResult = item,
                                    onItemClick = { clickedItem ->
                                        when (clickedItem) {
                                            is SongsResult, is VideosResult -> {
                                                val firstTrack: Track = (clickedItem as? SongsResult)?.toTrack() ?: (clickedItem as VideosResult).toTrack()
                                                searchViewModel.setQueueData(
                                                    QueueData.Data(
                                                        listTracks = arrayListOf(firstTrack),
                                                        firstPlayedTrack = firstTrack,
                                                        playlistId = "RDAMVM${firstTrack.videoId}",
                                                        playlistName = "\"${searchText}\" ${getStringBlocking(Res.string.in_search)}",
                                                        playlistType = PlaylistType.RADIO,
                                                        continuation = null,
                                                    ),
                                                )
                                                searchViewModel.loadMediaItem(firstTrack, type = Config.SONG_CLICK)
                                            }

                                            is ArtistsResult -> {
                                                navController.navigate(
                                                    ArtistDestination(clickedItem.browseId),
                                                )
                                            }

                                            is AlbumsResult -> {
                                                navController.navigate(
                                                    AlbumDestination(clickedItem.browseId),
                                                )
                                            }

                                            is PlaylistsResult -> {
                                                navController.navigate(
                                                    PlaylistDestination(
                                                        clickedItem.browseId,
                                                    ),
                                                )
                                            }
                                        }
                                    },
                                    modifier =
                                        Modifier.animateItem(
                                            fadeInSpec = motionScheme.fastEffectsSpec(),
                                            fadeOutSpec = motionScheme.fastEffectsSpec(),
                                            placementSpec = motionScheme.defaultSpatialSpec(),
                                        ),
                                )
                            }
                        }

                        // 2. Suggestions Keywords SECOND
                        if (searchScreenState.suggestQueries.isNotEmpty()) {
                            item(key = "header_suggestions") {
                                Text(
                                    text = stringResource(Res.string.suggest),
                                    style = typo().titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 4.dp,
                                                end = 4.dp,
                                                top = if (suggestSongs.isNotEmpty()) 12.dp else 4.dp,
                                                bottom = 2.dp,
                                            )
                                            .animateItem(
                                                fadeInSpec = motionScheme.fastEffectsSpec(),
                                                fadeOutSpec = motionScheme.fastEffectsSpec(),
                                                placementSpec = motionScheme.defaultSpatialSpec(),
                                            ),
                                )
                            }
                            itemsIndexed(
                                items = searchScreenState.suggestQueries,
                                key = { index, query -> "query_${query}_$index" },
                            ) { _, suggestion ->
                                SuggestQueryRow(
                                    suggestion = suggestion,
                                    onQueryClick = {
                                        searchText = suggestion
                                        focusManager.clearFocus()
                                        isSearchSubmitted = true
                                        searchViewModel.insertSearchHistory(suggestion)
                                        when (searchScreenState.searchType) {
                                            SearchType.ALL -> searchViewModel.searchAll(suggestion)
                                            SearchType.SONGS -> searchViewModel.searchSongs(suggestion)
                                            SearchType.VIDEOS -> searchViewModel.searchVideos(suggestion)
                                            SearchType.ALBUMS -> searchViewModel.searchAlbums(suggestion)
                                            SearchType.ARTISTS -> searchViewModel.searchArtists(suggestion)
                                            SearchType.PLAYLISTS -> searchViewModel.searchPlaylists(suggestion)
                                            SearchType.FEATURED_PLAYLISTS -> searchViewModel.searchFeaturedPlaylist(suggestion)
                                            SearchType.PODCASTS -> searchViewModel.searchPodcast(suggestion)
                                        }
                                    },
                                    onInsertClick = {
                                        searchText = suggestion
                                        focusRequester.requestFocus()
                                    },
                                    modifier =
                                        Modifier.animateItem(
                                            fadeInSpec = motionScheme.fastEffectsSpec(),
                                            fadeOutSpec = motionScheme.fastEffectsSpec(),
                                            placementSpec = motionScheme.defaultSpatialSpec(),
                                        ),
                                )
                            }
                        }

                        // 3. Other Entities (Artists, Albums, Playlists) THIRD
                        if (suggestOtherEntities.isNotEmpty()) {
                            item(key = "header_other_entities") {
                                Text(
                                    text = stringResource(Res.string.popular),
                                    style = typo().titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 4.dp,
                                                end = 4.dp,
                                                top = 12.dp,
                                                bottom = 2.dp,
                                            )
                                            .animateItem(
                                                fadeInSpec = motionScheme.fastEffectsSpec(),
                                                fadeOutSpec = motionScheme.fastEffectsSpec(),
                                                placementSpec = motionScheme.defaultSpatialSpec(),
                                            ),
                                )
                            }
                            itemsIndexed(
                                items = suggestOtherEntities,
                                key = { index, item ->
                                    val baseKey =
                                        when (item) {
                                            is ArtistsResult -> "artist_${item.browseId}"
                                            is AlbumsResult -> "album_${item.browseId}"
                                            is PlaylistsResult -> "playlist_${item.browseId}"
                                            else -> item.hashCode().toString()
                                        }
                                    "${baseKey}_$index"
                                },
                            ) { _, item ->
                                SuggestItemRow(
                                    searchResult = item,
                                    onItemClick = { clickedItem ->
                                        when (clickedItem) {
                                            is SongsResult, is VideosResult -> {
                                                val firstTrack: Track = (clickedItem as? SongsResult)?.toTrack() ?: (clickedItem as VideosResult).toTrack()
                                                searchViewModel.setQueueData(
                                                    QueueData.Data(
                                                        listTracks = arrayListOf(firstTrack),
                                                        firstPlayedTrack = firstTrack,
                                                        playlistId = "RDAMVM${firstTrack.videoId}",
                                                        playlistName = "\"${searchText}\" ${getStringBlocking(Res.string.in_search)}",
                                                        playlistType = PlaylistType.RADIO,
                                                        continuation = null,
                                                    ),
                                                )
                                                searchViewModel.loadMediaItem(firstTrack, type = Config.SONG_CLICK)
                                            }

                                            is ArtistsResult -> {
                                                navController.navigate(
                                                    ArtistDestination(clickedItem.browseId),
                                                )
                                            }

                                            is AlbumsResult -> {
                                                navController.navigate(
                                                    AlbumDestination(clickedItem.browseId),
                                                )
                                            }

                                            is PlaylistsResult -> {
                                                navController.navigate(
                                                    PlaylistDestination(
                                                        clickedItem.browseId,
                                                    ),
                                                )
                                            }
                                        }
                                    },
                                    modifier =
                                        Modifier.animateItem(
                                            fadeInSpec = motionScheme.fastEffectsSpec(),
                                            fadeOutSpec = motionScheme.fastEffectsSpec(),
                                            placementSpec = motionScheme.defaultSpatialSpec(),
                                        ),
                                )
                            }
                        }

                        item(key = "end_of_page") {
                            Box(
                                modifier =
                                    Modifier.animateItem(
                                        fadeInSpec = motionScheme.fastEffectsSpec(),
                                        fadeOutSpec = motionScheme.fastEffectsSpec(),
                                        placementSpec = motionScheme.defaultSpatialSpec(),
                                    ),
                            ) {
                                EndOfPage(
                                    withoutCredit = true,
                                )
                            }
                        }
                    }
                }

                SearchUIType.SEARCH_HISTORY -> {
                    // Search history state
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                    ) {
                        LazyColumn(
                            state = historyState,
                            contentPadding = PaddingValues(top = searchBarHeight, bottom = 10.dp),
                        ) {
                            stickyHeader {
                                Crossfade(
                                    targetState = searchHistory.isNotEmpty(),
                                ) {
                                    if (it) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.background),
                                        ) {
                                            TextButton(
                                                onClick = { searchViewModel.deleteSearchHistory() },
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.clear_search_history),
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            itemsIndexed(
                                items = searchHistory,
                                key = { index, historyItem -> "hist_${historyItem}_$index" },
                            ) { _, historyItem ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchText = historyItem
                                                focusManager.clearFocus()
                                                isSearchSubmitted = true
                                                searchViewModel.insertSearchHistory(historyItem)
                                                when (searchScreenState.searchType) {
                                                    SearchType.ALL -> searchViewModel.searchAll(historyItem)
                                                    SearchType.SONGS -> searchViewModel.searchSongs(historyItem)
                                                    SearchType.VIDEOS -> searchViewModel.searchVideos(historyItem)
                                                    SearchType.ALBUMS -> searchViewModel.searchAlbums(historyItem)
                                                    SearchType.ARTISTS -> searchViewModel.searchArtists(historyItem)
                                                    SearchType.PLAYLISTS -> searchViewModel.searchPlaylists(historyItem)
                                                    SearchType.FEATURED_PLAYLISTS -> searchViewModel.searchFeaturedPlaylist(historyItem)
                                                    SearchType.PODCASTS -> searchViewModel.searchPodcast(historyItem)
                                                }
                                            }.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.History,
                                        contentDescription = "Search history",
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                                    Text(
                                        text = historyItem,
                                        style = typo().bodyMedium,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = {
                                            searchText = historyItem
                                            focusRequester.requestFocus()
                                        },
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.ArrowOutward,
                                            contentDescription = "Search suggestion",
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                            item {
                                EndOfPage(
                                    withoutCredit = true,
                                )
                            }
                        }
                    }
                }

                SearchUIType.EMPTY -> {
                    val mood = moodAndGenres
                    if (mood == null) {
                        // First run only: the repository serves its cached copy before hitting the
                        // network, so this spinner is never seen again after the first fetch.
                        CenterLoadingBox(Modifier.fillMaxSize())
                    } else {
                        // Capped and centred: on a wide desktop window the grid would otherwise
                        // span the whole width, stretching four tiles into long bars. 1100.dp
                        // keeps a tile near 250.dp, which is its natural size.
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(moodGridColumns),
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .widthIn(max = 1100.dp)
                                    .padding(horizontal = 16.dp),
                            state = moodGridState,
                            contentPadding = PaddingValues(top = searchBarHeight),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            // Breathing room on both sides of this block: above it
                                            // sits the floating search bar, below it the tile grid.
                                            .padding(top = 36.dp, bottom = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.everything_you_need),
                                        style = typo().titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(Res.string.search_for_songs_artists_albums_playlists_and_more),
                                        style = typo().bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    SimpMusicChartButton(
                                        modifier = Modifier.padding(top = 10.dp),
                                    ) {
                                        uriHandler.openUri("https://chart.simpmusic.org")
                                    }
                                }
                            }
                            mood.sections.forEachIndexed { index, section ->
                                // First section runs straight on from the header block above it,
                                // so its own heading would just be a second title in a row.
                                if (index > 0) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            // Section titles come from YouTube already localised,
                                            // so there is no string resource to pick here.
                                            text = section.title,
                                            style = typo().titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                    }
                                }
                                // Key must include the section: every section lives in this ONE
                                // grid, and "For you" repeats categories that also appear under
                                // Moods or Genres, so params alone collides.
                                items(section.items, key = { "${section.title}/${it.params}" }) { item ->
                                    // LazyVerticalGrid only composes tiles inside the viewport, so
                                    // putting the request here IS the laziness — a category the
                                    // user never scrolls to never costs a browse.
                                    LaunchedEffect(item.params) {
                                        searchViewModel.loadMoodArtwork(item.params)
                                    }
                                    MoodCategoryCard(
                                        title = item.title,
                                        artworkUrl = moodArtwork[item.params],
                                    ) {
                                        navController.navigate(MoodDestination(item.params))
                                    }
                                }
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EndOfPage()
                            }
                        }
                        }
                    }
                }

                SearchUIType.SEARCH_RESULTS -> {
                    // Content area — chips now live in the blurred bar block above.
                    Column(modifier = Modifier.fillMaxSize()) {
                        PullToRefreshBox(
                            modifier = Modifier.fillMaxSize(),
                            state = pullToRefreshState,
                            onRefresh = {
                                val query = searchText.trim()
                                if (query.isNotEmpty()) {
                                    isSearchSubmitted = true
                                    searchViewModel.insertSearchHistory(query)
                                    when (searchScreenState.searchType) {
                                        SearchType.ALL -> searchViewModel.searchAll(query)
                                        SearchType.SONGS -> searchViewModel.searchSongs(query)
                                        SearchType.VIDEOS -> searchViewModel.searchVideos(query)
                                        SearchType.ALBUMS -> searchViewModel.searchAlbums(query)
                                        SearchType.ARTISTS -> searchViewModel.searchArtists(query)
                                        SearchType.PLAYLISTS -> searchViewModel.searchPlaylists(query)
                                        SearchType.FEATURED_PLAYLISTS -> searchViewModel.searchFeaturedPlaylist(query)
                                        SearchType.PODCASTS -> searchViewModel.searchPodcast(query)
                                    }
                                }
                            },
                            isRefreshing = uiState is SearchScreenUIState.Loading,
                            indicator = {
                                PullToRefreshDefaults.Indicator(
                                    state = pullToRefreshState,
                                    isRefreshing = uiState is SearchScreenUIState.Loading,
                                    // Anchored to the top of the box, which now starts under the
                                    // bar — without this offset the spinner sits behind the bar
                                    // and only its top sliver shows.
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = searchBarHeight),
                                    containerColor = PullToRefreshDefaults.indicatorContainerColor,
                                    color = PullToRefreshDefaults.indicatorColor,
                                    maxDistance = PullToRefreshDefaults.PositionalThreshold - 5.dp,
                                )
                            },
                        ) {
                            Crossfade(
                                targetState = uiState,
                                animationSpec = motionScheme.fastEffectsSpec(),
                            ) { uiState ->
                                when (uiState) {
                                    is SearchScreenUIState.Loading -> {
                                        // Loading state — same top inset as the results list, or
                                        // the first shimmer row hides behind the bar and chips.
                                        LazyColumn(
                                            contentPadding =
                                                PaddingValues(
                                                    start = 8.dp,
                                                    end = 8.dp,
                                                    top = searchBarHeight + 8.dp,
                                                    bottom = 120.dp,
                                                ),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            items(10) {
                                                Box(
                                                    modifier =
                                                        Modifier.animateItem(
                                                            fadeInSpec = motionScheme.fastEffectsSpec(),
                                                            fadeOutSpec = motionScheme.fastEffectsSpec(),
                                                            placementSpec = motionScheme.defaultSpatialSpec(),
                                                        ),
                                                ) {
                                                    ShimmerSearchItem()
                                                }
                                            }
                                        }
                                    }

                                    is SearchScreenUIState.Success -> {
                                        // Success state with results
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            // Search Results List
                                            val currentResults =
                                                when (searchScreenState.searchType) {
                                                    SearchType.ALL -> searchScreenState.searchAllResult
                                                    SearchType.SONGS -> searchScreenState.searchSongsResult
                                                    SearchType.VIDEOS -> searchScreenState.searchVideosResult
                                                    SearchType.ALBUMS -> searchScreenState.searchAlbumsResult
                                                    SearchType.ARTISTS -> searchScreenState.searchArtistsResult
                                                    SearchType.PLAYLISTS -> searchScreenState.searchPlaylistsResult
                                                    SearchType.FEATURED_PLAYLISTS -> searchScreenState.searchFeaturedPlaylistsResult
                                                    SearchType.PODCASTS -> searchScreenState.searchPodcastsResult
                                                }

                                            Crossfade(
                                                targetState = currentResults.isNotEmpty(),
                                                animationSpec = motionScheme.fastEffectsSpec(),
                                            ) { hasResults ->
                                                if (hasResults) {
                                                    LazyColumn(
                                                        contentPadding =
                                                            PaddingValues(
                                                                start = 12.dp,
                                                                end = 12.dp,
                                                                top = searchBarHeight + 8.dp,
                                                                bottom = 120.dp,
                                                            ),
                                                        state = resultsState,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    ) {
                                                        itemsIndexed(
                                                            items = currentResults,
                                                            key = { index, result ->
                                                                val baseKey =
                                                                    when (result) {
                                                                        is SongsResult -> "song_${result.videoId}"
                                                                        is VideosResult -> "video_${result.videoId}"
                                                                        is AlbumsResult -> "album_${result.browseId}"
                                                                        is ArtistsResult -> "artist_${result.browseId}"
                                                                        is PlaylistsResult -> "playlist_${result.browseId}"
                                                                        else -> result.hashCode().toString()
                                                                    }
                                                                "${baseKey}_$index"
                                                            },
                                                        ) { _, result ->
                                                            SearchResultItemRow(
                                                                searchResult = result,
                                                                isPlaying =
                                                                    when (result) {
                                                                        is SongsResult -> result.videoId == currentVideoId
                                                                        is VideosResult -> result.videoId == currentVideoId
                                                                        else -> false
                                                                    },
                                                                selectionMode = selectionState.isActive,
                                                                isSelected =
                                                                    when (result) {
                                                                        is SongsResult -> selectionState.isSelected(result.videoId)
                                                                        is VideosResult -> selectionState.isSelected(result.videoId)
                                                                        else -> false
                                                                    },
                                                                onItemClick = {
                                                                    if (selectionState.isActive) {
                                                                        when (result) {
                                                                            is SongsResult -> selectionState.toggle(result.videoId)
                                                                            is VideosResult -> selectionState.toggle(result.videoId)
                                                                            else -> {}
                                                                        }
                                                                    } else {
                                                                        when (result) {
                                                                            is SongsResult -> {
                                                                                val firstTrack = result.toTrack()
                                                                                searchViewModel.setQueueData(
                                                                                    QueueData.Data(
                                                                                        listTracks = arrayListOf(firstTrack),
                                                                                        firstPlayedTrack = firstTrack,
                                                                                        playlistId = "RDAMVM${result.videoId}",
                                                                                        playlistName =
                                                                                            "\"${searchText}\" ${
                                                                                                getStringBlocking(
                                                                                                    Res.string.in_search,
                                                                                                )
                                                                                            }",
                                                                                        playlistType = PlaylistType.RADIO,
                                                                                        continuation = null,
                                                                                    ),
                                                                                )
                                                                                searchViewModel.loadMediaItem(firstTrack, Config.SONG_CLICK)
                                                                            }

                                                                            is VideosResult -> {
                                                                                val firstTrack = result.toTrack()
                                                                                searchViewModel.setQueueData(
                                                                                    QueueData.Data(
                                                                                        listTracks = arrayListOf(firstTrack),
                                                                                        firstPlayedTrack = firstTrack,
                                                                                        playlistId = "RDAMVM${result.videoId}",
                                                                                        playlistName =
                                                                                            "\"${searchText}\" ${
                                                                                                getStringBlocking(
                                                                                                    Res.string.in_search,
                                                                                                )
                                                                                            }",
                                                                                        playlistType = PlaylistType.RADIO,
                                                                                        continuation = null,
                                                                                    ),
                                                                                )
                                                                                searchViewModel.loadMediaItem(firstTrack, Config.VIDEO_CLICK)
                                                                            }

                                                                            is AlbumsResult -> {
                                                                                navController.navigate(
                                                                                    AlbumDestination(
                                                                                        result.browseId,
                                                                                    ),
                                                                                )
                                                                            }

                                                                            is ArtistsResult -> {
                                                                                navController.navigate(
                                                                                    ArtistDestination(
                                                                                        result.browseId,
                                                                                    ),
                                                                                )
                                                                            }

                                                                            is PlaylistsResult -> {
                                                                                if (result.resultType == "Podcast") {
                                                                                    navController.navigate(
                                                                                        PodcastDestination(
                                                                                            result.browseId,
                                                                                        ),
                                                                                    )
                                                                                } else {
                                                                                    navController.navigate(
                                                                                        PlaylistDestination(
                                                                                            result.browseId,
                                                                                        ),
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                onLongClick = {
                                                                    when (result) {
                                                                        is SongsResult -> selectionState.start(result.videoId)
                                                                        is VideosResult -> selectionState.start(result.videoId)
                                                                        else -> {}
                                                                    }
                                                                },
                                                                onMoreClick = {
                                                                    when (result) {
                                                                        is SongsResult -> onMoreClick(result.toTrack().toSongEntity())
                                                                        is VideosResult -> onMoreClick(result.toTrack().toSongEntity())
                                                                        else -> {}
                                                                    }
                                                                },
                                                                onPlayClick = {
                                                                    when (result) {
                                                                        is SongsResult -> {
                                                                            val firstTrack = result.toTrack()
                                                                            searchViewModel.setQueueData(
                                                                                QueueData.Data(
                                                                                    listTracks = arrayListOf(firstTrack),
                                                                                    firstPlayedTrack = firstTrack,
                                                                                    playlistId = "RDAMVM${result.videoId}",
                                                                                    playlistName =
                                                                                        "\"${searchText}\" ${
                                                                                            getStringBlocking(
                                                                                                Res.string.in_search,
                                                                                            )
                                                                                        }",
                                                                                    playlistType = PlaylistType.RADIO,
                                                                                    continuation = null,
                                                                                ),
                                                                            )
                                                                            searchViewModel.loadMediaItem(firstTrack, Config.SONG_CLICK)
                                                                        }

                                                                        is VideosResult -> {
                                                                            val firstTrack = result.toTrack()
                                                                            searchViewModel.setQueueData(
                                                                                QueueData.Data(
                                                                                    listTracks = arrayListOf(firstTrack),
                                                                                    firstPlayedTrack = firstTrack,
                                                                                    playlistId = "RDAMVM${result.videoId}",
                                                                                    playlistName =
                                                                                        "\"${searchText}\" ${
                                                                                            getStringBlocking(
                                                                                                Res.string.in_search,
                                                                                            )
                                                                                        }",
                                                                                    playlistType = PlaylistType.RADIO,
                                                                                    continuation = null,
                                                                                ),
                                                                            )
                                                                            searchViewModel.loadMediaItem(firstTrack, Config.VIDEO_CLICK)
                                                                        }

                                                                        else -> {}
                                                                    }
                                                                },
                                                                modifier =
                                                                    Modifier.animateItem(
                                                                        fadeInSpec = motionScheme.fastEffectsSpec(),
                                                                        fadeOutSpec = motionScheme.fastEffectsSpec(),
                                                                        placementSpec = motionScheme.defaultSpatialSpec(),
                                                                    ),
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxSize()
                                                                .padding(horizontal = 24.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Column(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(24.dp))
                                                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                                    .padding(32.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                        ) {
                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .size(56.dp)
                                                                        .clip(CircleShape)
                                                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Icon(
                                                                    imageVector = SimpIcons.Search,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(28.dp),
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.height(16.dp))
                                                            Text(
                                                                text = stringResource(Res.string.no_results_found),
                                                                style = typo().titleMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                textAlign = TextAlign.Center,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    is SearchScreenUIState.Error -> {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 24.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                        .padding(32.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .size(56.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.errorContainer),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = SimpIcons.Error,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.size(28.dp),
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = stringResource(Res.string.error_occurred),
                                                    style = typo().titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Button(onClick = {
                                                    if (searchText.isNotEmpty()) {
                                                        searchViewModel.searchAll(searchText)
                                                    }
                                                }) {
                                                    Text(text = stringResource(Res.string.retry))
                                                }
                                            }
                                        }
                                    }

                                    SearchScreenUIState.Empty -> {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 24.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                                        .padding(32.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .size(56.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = SimpIcons.Search,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(28.dp),
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = stringResource(Res.string.no_results_found),
                                                    style = typo().titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val motionScheme = MaterialTheme.motionScheme

        // Material 3 Expressive SearchBar morphing physics:
        // Pill (28dp) when idle -> softly rounded (16dp) when focused
        val searchBarCorner by animateDpAsState(
            targetValue = if (isFocused) 16.dp else 28.dp,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "searchBarCorner",
        )
        // Horizontal padding eases in/out: expands outward from 16dp to 8dp on focus
        val searchBarPaddingHorizontal by animateDpAsState(
            targetValue = if (isFocused) 8.dp else 16.dp,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "searchBarPaddingHorizontal",
        )
        // M3 tonal container color transition
        val searchBarContainerColor by animateColorAsState(
            targetValue =
                if (isFocused) {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                } else if (!isContentAtTop) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            animationSpec = motionScheme.fastEffectsSpec(),
            label = "searchBarContainerColor",
        )

        AnimatedContent(
            targetState = isContentAtTop,
            transitionSpec = {
                fadeIn(animationSpec = motionScheme.fastEffectsSpec()).togetherWith(
                    fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
                )
            },
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .onGloballyPositioned { searchBarHeightPx = it.size.height },
            label = "search_bar_scrim",
        ) { atTop ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (atTop) {
                                Modifier.background(Color.Transparent)
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.background)
                            },
                        ).windowInsetsPadding(WindowInsets.statusBars)
                        .padding(vertical = 10.dp),
            ) {
                AnimatedVisibility(visible = selectionState.isActive) {
                    SongSelectionTopAppBar(
                        state = selectionState,
                        onSelectAll = {
                            val visible =
                                when (searchScreenState.searchType) {
                                    SearchType.SONGS -> searchScreenState.searchSongsResult.map { it.videoId }
                                    SearchType.VIDEOS -> searchScreenState.searchVideosResult.map { it.videoId }
                                    SearchType.ALL ->
                                        searchScreenState.searchAllResult.mapNotNull {
                                            (it as? SongsResult)?.videoId ?: (it as? VideosResult)?.videoId
                                        }
                                    else -> emptyList()
                                }
                            selectionState.toggleSelectAll(visible)
                        },
                        onOpenActions = { showSelectionSheet = true },
                        containerColor = Color.Transparent,
                        // Zero here AND on the SearchBar below: the Column that holds them both consumes
                        // the status bar once, for the whole stack. Leaving it on either child reserves it
                        // a second time — which is the slab of padding this screen used to show.
                        windowInsets = WindowInsets(0),
                    )
                }
                // Material 3 SearchBar with motion physics
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchText,
                            onQueryChange = { newText ->
                                searchText = newText
                            },
                            onSearch = { query ->
                                // A pasted YouTube link is a destination, not a query. Translating it into
                                // the app's own deep link hands it to the same intent flow that handles
                                // shared links, so it plays or opens straight away instead of being
                                // searched for as text. Anything else falls through to a normal search.
                                val deepLink = query.toAppDeepLinkOrNull()
                                if (deepLink != null) {
                                    focusManager.clearFocus()
                                    sharedViewModel.setIntent(GenericIntent(data = deepLink))
                                } else if (query.isNotEmpty()) {
                                    isSearchSubmitted = true
                                    focusManager.clearFocus()
                                    searchViewModel.insertSearchHistory(query)
                                    when (searchScreenState.searchType) {
                                        SearchType.ALL -> searchViewModel.searchAll(query)
                                        SearchType.SONGS -> searchViewModel.searchSongs(query)
                                        SearchType.VIDEOS -> searchViewModel.searchVideos(query)
                                        SearchType.ALBUMS -> searchViewModel.searchAlbums(query)
                                        SearchType.ARTISTS -> searchViewModel.searchArtists(query)
                                        SearchType.PLAYLISTS -> searchViewModel.searchPlaylists(query)
                                        SearchType.FEATURED_PLAYLISTS -> searchViewModel.searchFeaturedPlaylist(query)
                                        SearchType.PODCASTS -> searchViewModel.searchPodcast(query)
                                    }
                                }
                            },
                            expanded = false,
                            onExpandedChange = {},
                            enabled = true,
                            placeholder = {
                                // Animated placeholder text with M3 motion physics
                                AnimatedContent(
                                    targetState = currentPlaceholderIndex,
                                    transitionSpec = {
                                        (
                                            fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                                                slideInVertically(animationSpec = motionScheme.defaultSpatialSpec()) { height -> height / 2 }
                                        ).togetherWith(
                                            fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                                                slideOutVertically(animationSpec = motionScheme.defaultSpatialSpec()) { height -> -height / 2 },
                                        )
                                    },
                                    label = "placeholder_animation",
                                ) { index ->
                                    Text(
                                        text = placeholderTexts[index],
                                        style = typo().bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            leadingIcon = {
                                AnimatedContent(
                                    targetState = isFocused,
                                    transitionSpec = {
                                        (
                                            fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                                                scaleIn(animationSpec = motionScheme.fastSpatialSpec(), initialScale = 0.8f)
                                        ).togetherWith(
                                            fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                                                scaleOut(animationSpec = motionScheme.fastSpatialSpec(), targetScale = 0.8f)
                                        )
                                    },
                                    label = "search_leading_icon",
                                ) { focused ->
                                    if (focused) {
                                        IconButton(
                                            onClick = {
                                                focusManager.clearFocus()
                                                isExpanded = false
                                            },
                                        ) {
                                            Icon(
                                                imageVector = SimpIcons.ArrowBackIosNew,
                                                contentDescription = "Back",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.size(48.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = SimpIcons.Search,
                                                contentDescription = "Search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = searchText.isNotEmpty(),
                                    enter =
                                        fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                                            scaleIn(animationSpec = motionScheme.fastSpatialSpec(), initialScale = 0.7f),
                                    exit =
                                        fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                                            scaleOut(animationSpec = motionScheme.fastSpatialSpec(), targetScale = 0.7f),
                                ) {
                                    IconButton(
                                        modifier = Modifier.clip(CircleShape),
                                        onClick = {
                                            searchText = ""
                                            isSearchSubmitted = false
                                        },
                                    ) {
                                        Icon(
                                            imageVector = SimpIcons.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier =
                        Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = searchBarPaddingHorizontal)
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                isFocused = it.isFocused
                            },
                    shape = RoundedCornerShape(searchBarCorner),
                    colors =
                        SearchBarDefaults.colors(
                            containerColor = searchBarContainerColor,
                        ),
                    // See the note on SongSelectionTopAppBar above — the Column owns the status-bar inset.
                    windowInsets = WindowInsets(0),
                    content = {},
                )
                // Filter chips ride along inside the blurred block instead of sitting in the
                // results branch. That way searchBarHeight covers them too, results scroll
                // underneath the whole thing, and the glass has something to blur.
                AnimatedVisibility(
                    visible = searchUIType == SearchUIType.SEARCH_RESULTS,
                    enter =
                        fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                            expandVertically(animationSpec = motionScheme.defaultSpatialSpec()),
                    exit =
                        fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                            shrinkVertically(animationSpec = motionScheme.defaultSpatialSpec()),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier =
                            Modifier
                                .widthIn(max = 720.dp)
                                .horizontalScroll(chipRowState)
                                .padding(top = 10.dp)
                                .padding(horizontal = 12.dp),
                    ) {
                        SearchType.entries.forEachIndexed { index, id ->
                            val isSelected = id == searchScreenState.searchType
                            SegmentedButton(
                                selected = isSelected,
                                onClick = { searchViewModel.setSearchType(id) },
                                shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index = index, count = SearchType.entries.size)
                            ) {
                                Text(stringResource(id.toStringRes()))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestItemRow(
    searchResult: SearchResultType,
    onItemClick: (SearchResultType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionScheme = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
    )

    val url =
        when (searchResult) {
            is SongsResult -> {
                searchResult.thumbnails?.lastOrNull()?.url
            }

            is AlbumsResult -> {
                searchResult.thumbnails.lastOrNull()?.url
            }

            is ArtistsResult -> {
                searchResult.thumbnails.lastOrNull()?.url
            }

            is PlaylistsResult -> {
                searchResult.thumbnails.lastOrNull()?.url
            }

            is VideosResult -> {
                searchResult.thumbnails?.lastOrNull()?.url
            }

            else -> {
                null
            }
        }

    val title =
        when (searchResult) {
            is SongsResult -> {
                searchResult.title
            }

            is AlbumsResult -> {
                searchResult.title
            }

            is ArtistsResult -> {
                searchResult.artist
            }

            is PlaylistsResult -> {
                searchResult.title
            }

            is VideosResult -> {
                searchResult.title
            }

            else -> {
                null
            }
        } ?: "Unknown"

    val subtitle =
        when (searchResult) {
            is SongsResult -> searchResult.artists?.map { it.name }?.connectArtists()
            is AlbumsResult -> searchResult.artists.map { it.name }.connectArtists()
            is PlaylistsResult -> searchResult.author.ifEmpty { "YouTube Music" }
            is ArtistsResult -> null
            is VideosResult -> searchResult.artists?.map { it.name }?.connectArtists()
            else -> null
        } ?: ""

    val badgeText =
        when (searchResult) {
            is SongsResult -> stringResource(Res.string.song)
            is ArtistsResult -> stringResource(Res.string.artists)
            is AlbumsResult -> stringResource(Res.string.album)
            is PlaylistsResult -> stringResource(Res.string.playlist)
            is VideosResult -> stringResource(Res.string.videos)
            else -> null
        }

    val isPlayable = searchResult is SongsResult || searchResult is VideosResult

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isPressed) MaterialTheme.colorScheme.surfaceContainerHigh
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = { onItemClick(searchResult) },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(
                        if (searchResult is ArtistsResult) {
                            CircleShape
                        } else {
                            RoundedCornerShape(12.dp)
                        },
                    ),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            if (badgeText != null) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = badgeText,
                        style = typo().labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }

            Text(
                text = title,
                style = typo().titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = typo().bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (isPlayable) {
            Box(
                modifier =
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SimpIcons.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Box(
                modifier =
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SimpIcons.ArrowForwardIos,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestQueryRow(
    suggestion: String,
    onQueryClick: () -> Unit,
    onInsertClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionScheme = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isPressed) MaterialTheme.colorScheme.surfaceContainerHigh
                    else MaterialTheme.colorScheme.surfaceContainerLowest,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onQueryClick,
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SimpIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = suggestion,
            style = typo().bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onInsertClick,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = SimpIcons.ArrowOutward,
                contentDescription = "Insert suggestion",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchResultItemRow(
    searchResult: SearchResultType,
    isPlaying: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onPlayClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val motionScheme = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
    )

    val url =
        when (searchResult) {
            is SongsResult -> searchResult.thumbnails?.lastOrNull()?.url
            is AlbumsResult -> searchResult.thumbnails.lastOrNull()?.url
            is ArtistsResult -> searchResult.thumbnails.lastOrNull()?.url
            is PlaylistsResult -> searchResult.thumbnails.lastOrNull()?.url
            is VideosResult -> searchResult.thumbnails?.lastOrNull()?.url
            else -> null
        }

    val title =
        when (searchResult) {
            is SongsResult -> searchResult.title
            is AlbumsResult -> searchResult.title
            is ArtistsResult -> searchResult.artist
            is PlaylistsResult -> searchResult.title
            is VideosResult -> searchResult.title
            else -> null
        } ?: "Unknown"

    val subtitle =
        when (searchResult) {
            is SongsResult -> searchResult.artists?.map { it.name }?.connectArtists()
            is AlbumsResult -> searchResult.artists.map { it.name }.connectArtists()
            is PlaylistsResult -> searchResult.author.ifEmpty { "YouTube Music" }
            is ArtistsResult -> null
            is VideosResult -> searchResult.artists?.map { it.name }?.connectArtists()
            else -> null
        } ?: ""

    val badgeText =
        when (searchResult) {
            is SongsResult -> stringResource(Res.string.song)
            is ArtistsResult -> stringResource(Res.string.artists)
            is AlbumsResult -> stringResource(Res.string.album)
            is PlaylistsResult -> {
                if (searchResult.resultType == "Podcast") {
                    stringResource(Res.string.podcasts)
                } else {
                    stringResource(Res.string.playlist)
                }
            }
            is VideosResult -> stringResource(Res.string.videos)
            else -> null
        }

    val isPlayable = searchResult is SongsResult || searchResult is VideosResult

    val containerColor =
        when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            isPlaying -> MaterialTheme.colorScheme.surfaceContainerHighest
            isPressed -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onItemClick,
                    onLongClick = onLongClick,
                )
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = selectionMode,
            enter = fadeIn(motionScheme.fastEffectsSpec()) + expandHorizontally(motionScheme.fastSpatialSpec()),
            exit = fadeOut(motionScheme.fastEffectsSpec()) + shrinkHorizontally(motionScheme.fastSpatialSpec()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                shape = CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = SimpIcons.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
        }

        Box(
            modifier =
                Modifier
                    .size(54.dp)
                    .clip(
                        if (searchResult is ArtistsResult) {
                            CircleShape
                        } else {
                            RoundedCornerShape(12.dp)
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (isPlaying) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AudioPlayingIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = typo().titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (badgeText != null) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = badgeText,
                            style = typo().labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = typo().bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (!selectionMode) {
            if (isPlayable) {
                if (onPlayClick != null) {
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable(onClick = onPlayClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = SimpIcons.PlayArrow,
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (onMoreClick != null) {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = SimpIcons.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SimpIcons.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

enum class SearchUIType {
    EMPTY,
    SEARCH_HISTORY,
    SEARCH_SUGGESTIONS,
    SEARCH_RESULTS,
}