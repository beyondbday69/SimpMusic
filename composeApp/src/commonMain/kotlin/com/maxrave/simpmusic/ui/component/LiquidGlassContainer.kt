package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sign

/**
 * Applies the SimpMusic liquid-glass effect to any element.
 *
 * This is the single primitive behind the glass buttons that used to be hand-wired
 * inline on every screen. It encapsulates the per-surface
 * [androidx.compose.ui.graphics.layer.GraphicsLayer], the Kyant `drawBackdrop`
 * effect stack and the press/hold "liquid" interaction (a slight scale-up, deeper
 * refraction and a radial glow that follows the pointer, springing back on release).
 * The press gesture is observe-only, so wrapped click handlers keep working.
 *
 * The element MUST be a sibling of the backdrop source (the box carrying
 * [com.maxrave.simpmusic.expect.ui.layerBackdrop]); nesting it inside the source
 * creates a render-feedback loop that crashes the RuntimeShader.
 *
 * Runs on Android and desktop alike: Kyant's backdrop is a KMP artifact whose desktop
 * variant renders through skiko. The press interaction is driven by `pointerInput`, so
 * a mouse button triggers it exactly like a finger.
 *
 * @param interactive set false for a static glass surface.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: PlatformBackdrop,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    return this
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
}

/**
 * Overload of [liquidGlass] for surfaces that sample their own background luminance.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    blurScale: Float = 1f,
    minScrim: Float = 0.12f,
    maxScrim: Float = 0.5f,
): Modifier {
    return this
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
}

/**
 * A liquid-glass surface wrapping arbitrary [content] (e.g. a pill of icon
 * buttons). Thin convenience over [liquidGlass].
 */
@Composable
fun LiquidGlassContainer(
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(backdrop, shape, interactive, highlight),
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * Convenience wrapper around [LiquidGlassContainer] for the common single-icon
 * case (e.g. the circular back button shared by the detail screens).
 */
@Composable
fun LiquidGlassIconButton(
    backdrop: PlatformBackdrop,
    imageVector: ImageVector,
    modifier: Modifier = Modifier.size(48.dp),
    shape: Shape = CircleShape,
    tint: Color = Color.White,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    onClick: () -> Unit,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        modifier = modifier,
        shape = shape,
        interactive = interactive,
        highlight = highlight,
    ) {
        RippleIconButton(
            imageVector = imageVector,
            tint = tint,
            onClick = onClick,
        )
    }
}

/**
 * Press/hold state holder for a single liquid-glass surface.
 *
 * Project-local, dependency-light reimplementation of Kyant's catalog
 * `InteractiveHighlight`: instead of the library-internal AGSL highlight shader
 * (whose public helpers are not exposed in backdrop 2.0.0) we drive a plain
 * [Brush.radialGradient] from [touchPosition] and a spring-animated
 * [pressProgress]. The drag detection is observe-only so wrapped buttons keep
 * receiving their own clicks.
 */
class GlassInteraction(
    private val animationScope: CoroutineScope,
) {
    private val pressSpec = spring(dampingRatio = 0.5f, stiffness = 300f, visibilityThreshold = 0.001f)
    private val pressAnimation = Animatable(0f, 0.001f)

    /** 0f at rest, animating to 1f while pressed. Read in draw/effect/layer blocks. */
    val pressProgress: Float get() = pressAnimation.value

    /** Local-space touch point used as the centre of the press glow. */
    var touchPosition by mutableStateOf(Offset.Zero)
        private set

    suspend fun detectPress(pointer: PointerInputScope) =
        with(pointer) {
            inspectDragGestures(
                onDragStart = { down ->
                    touchPosition = down.position
                    animationScope.launch { pressAnimation.animateTo(1f, pressSpec) }
                },
                onDragEnd = { animationScope.launch { pressAnimation.animateTo(0f, pressSpec) } },
                onDragCancel = { animationScope.launch { pressAnimation.animateTo(0f, pressSpec) } },
            ) { change, _ ->
                touchPosition = change.position
            }
        }
}

@Composable
fun rememberGlassInteraction(): GlassInteraction {
    val scope = rememberCoroutineScope()
    return remember(scope) { GlassInteraction(scope) }
}

/**
 * Draws the liquid-glass effect with the same look as the legacy
 * `drawBackdropCustomShape`, plus an optional press response driven by
 * [interaction]: the surface scales up a touch, the refraction/blur deepen and a
 * radial glow follows the pointer. Pass `interaction = null` for a static surface.
 *
 * [luminanceAnimation] keeps the brightness/contrast curve of the original
 * wrapper (the bottom navigation bar animates it; static surfaces pass `0.5f`).
 *
 * [blurScale] multiplies the luminance-driven blur radius and [minScrim]/[maxScrim]
 * are the ends of the darkening ramp. The defaults are the values this surface has
 * always drawn with, so every existing caller is unaffected; the Desktop capsule
 * raises both because at the shared settings (7–11dp of blur over a 0.12 scrim floor)
 * the artwork behind it stays legible instead of dissolving the way Apple's does.
 */
fun Modifier.drawInteractiveGlass(
    isDark: Boolean,
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape,
    interaction: GlassInteraction?,
    pressedScale: Float = 1.12f,
    highlight: Highlight = Highlight.Default,
    blurScale: Float = 1f,
    minScrim: Float = 0.12f,
    maxScrim: Float = 0.5f,
): Modifier =
    this
        .clip(shape)
        .background(if (isDark) Color(0xFF2B2B2B) else Color(0xFFE8E8E8))

/**
 * Observe-only drag/press recogniser ported from Kyant's catalog
 * `DragGestureInspector`. It never consumes events, so a glass surface can react
 * to a press while the buttons it wraps still handle their own taps.
 */
internal suspend fun PointerInputScope.inspectDragGestures(
    onDragStart: (down: PointerInputChange) -> Unit = {},
    onDragEnd: (change: PointerInputChange) -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

        val down = awaitFirstDown(requireUnconsumed = false)

        onDragStart(down)
        onDrag(down, Offset.Zero)
        val upEvent =
            drag(
                pointerId = down.id,
                onDrag = { onDrag(it, it.positionChange()) },
            )
        if (upEvent == null) {
            onDragCancel()
        } else {
            onDragEnd(upEvent)
        }
    }
}

private suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): PointerInputChange? {
    val isPointerUp = currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true
    if (isPointerUp) {
        return null
    }
    var pointer = pointerId
    while (true) {
        val change = awaitDragOrUp(pointer) ?: return null
        if (change.isConsumed) {
            return null
        }
        if (change.changedToUpIgnoreConsumed()) {
            return change
        }
        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.awaitDragOrUp(pointerId: PointerId): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                return dragEvent
            } else {
                pointer = otherDown.id
            }
        } else {
            val hasDragged = dragEvent.previousPosition != dragEvent.position
            if (hasDragged) {
                return dragEvent
            }
        }
    }
}
