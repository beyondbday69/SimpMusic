package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.PlatformCastButton
import com.maxrave.simpmusic.expect.ui.isPlatformCastAvailable

/**
 * A Cast button slot that owns its own availability gate.
 *
 * [PlatformCastButton] already hides itself when Cast is unavailable, but it cannot hide the
 * container a caller wraps it in — so every call site previously had to repeat the
 * [isPlatformCastAvailable] check around its own wrapper, or the empty container kept taking up
 * space (and, in a connected button group, a slot) after the button inside it had vanished. This
 * composable performs that gate once: it renders nothing at all when Cast is not supported on the
 * platform, so callers never lay out a phantom slot.
 *
 * The container is deliberately NOT clickable. The platform button is a real `MediaRouteButton`
 * embedded through `AndroidView`, and it dispatches its own touch events; a clickable wrapper
 * (a `Surface` with `onClick`, a `Box` with `clickable`, …) would swallow the tap before the View
 * underneath ever saw it. Styling therefore goes through plain `clip`/`background`/`border` on a
 * [Box] rather than a Material 3 [androidx.compose.material3.Surface]: this draws only what is
 * asked for and imposes no minimum-touch-size floor of its own, so the 40dp slots some callers use
 * keep their height and the layouts around them do not shift.
 *
 * Use this overload when you want the standard styled container. Pass [content] to the other
 * overload when the container is bespoke (e.g. a slot in a connected button group) and you only
 * need the availability gate.
 *
 * @param tint colour of the glyph; callers flip it to signal an active Cast session.
 * @param shape shape of the container's clip, background and border. `null` leaves the bounds
 *   rectangular.
 * @param color container fill. Transparent by default, so an unstyled slot draws nothing behind the
 *   glyph.
 * @param border optional stroke drawn inside [shape]. `null` draws none.
 * @param iconSize size of the glyph itself, independent of the container.
 */
@Composable
fun CastSlot(
    tint: Color,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    color: Color = Color.Transparent,
    border: BorderStroke? = null,
    iconSize: Dp = 24.dp,
) {
    if (!isPlatformCastAvailable()) return

    val containerShape = shape ?: RectangleShape
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .let { if (shape != null) it.clip(shape) else it }
            .let { if (color != Color.Transparent) it.background(color, containerShape) else it }
            .let { if (border != null) it.border(border, containerShape) else it },
    ) {
        PlatformCastButton(
            modifier = Modifier.size(iconSize),
            tint = tint,
        )
    }
}

/**
 * A Cast slot that only contributes the availability gate, leaving the container to [content].
 *
 * Renders nothing when Cast is not supported on the platform, so a bespoke container never outlives
 * the button it was built around. As with the styled overload, nothing here is clickable — see that
 * overload's documentation for why the platform button must receive its own touches.
 *
 * Example:
 * ```
 * CastSlot(
 *     content = {
 *         ConnectedSlot(shape = middleShape, onClick = null) {
 *             PlatformCastButton(tint = tint)
 *         }
 *     },
 * )
 * ```
 */
@Composable
fun CastSlot(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!isPlatformCastAvailable()) return

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        content()
    }
}
