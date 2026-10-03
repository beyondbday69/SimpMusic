package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.maxrave.simpmusic.ui.theme.LocalForceDarkText
import com.maxrave.simpmusic.ui.theme.LocalForcedDarkColorScheme

/**
 * Color set for overlay surfaces (bottom sheets & dialogs) that dynamically inherit Material 3
 * colors from the screen that opened them.
 *
 * When opened from a force-dark screen (Artist/Album/Playlist/LocalPlaylist/NowPlaying/
 * FullscreenPlayer/Podcast), it resolves against [LocalForcedDarkColorScheme], dynamically tinted
 * by the current track's album art seed / dynamic palette.
 * When opened from a normal screen, it follows the app theme [MaterialTheme.colorScheme].
 */
@Immutable
data class SurfaceDarkColors(
    val container: Color, // sheet/dialog background + inner cards
    val handle: Color, // drag handle
    val content: Color, // primary text + icons
    val subtitle: Color, // secondary text
    val disabled: Color, // disabled text/icons + dividers
)

@Composable
fun rememberSurfaceDarkColors(): SurfaceDarkColors {
    val cs = MaterialTheme.colorScheme
    val darkScheme = LocalForcedDarkColorScheme.current ?: cs
    return if (LocalForceDarkText.current) {
        SurfaceDarkColors(
            container = darkScheme.surfaceContainer,
            handle = darkScheme.onSurfaceVariant.copy(alpha = 0.4f),
            content = darkScheme.onSurface,
            subtitle = darkScheme.onSurfaceVariant,
            disabled = darkScheme.onSurface.copy(alpha = 0.38f),
        )
    } else {
        SurfaceDarkColors(
            container = cs.surfaceContainerLow,
            handle = cs.onSurfaceVariant.copy(alpha = 0.4f),
            content = cs.onSurface,
            subtitle = cs.onSurfaceVariant,
            disabled = cs.onSurfaceVariant.copy(alpha = 0.38f),
        )
    }
}
