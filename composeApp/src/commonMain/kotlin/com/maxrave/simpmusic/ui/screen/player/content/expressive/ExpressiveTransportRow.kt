package com.maxrave.simpmusic.ui.screen.player.content.expressive

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.maxrave.domain.mediaservice.handler.ControlState
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Repeat
import com.maxrave.simpmusic.ui.icon.RepeatOne
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.SkipNext
import com.maxrave.simpmusic.ui.icon.SkipPrevious
import com.maxrave.simpmusic.viewModel.UIEvent

// Base weights: prev 0.55 | play 1.2 | next 0.55. A pressed button grows ×1.15; because Row
// normalizes weights, the other two shrink proportionally without any extra bookkeeping.
private const val SIDE_WEIGHT = 0.55f
private const val PLAY_WEIGHT = 1.2f
private const val PRESS_GROWTH = 1.15f

// Shuffle and repeat, when the row carries them: narrower than prev/next, so the transport keeps
// the middle of the row.
private const val TOGGLE_WEIGHT = 0.4f

// Extra room between a toggle and the transport, on top of the row's own 8dp either side (24dp in
// all), so shuffle and repeat read as their own pair rather than two more transport buttons.
private val TOGGLE_SEPARATION = 8.dp

/**
 * M3-Expressive transport: three pill buttons in a 68dp row.
 *
 * - Play/pause corner radius morphs 22dp (playing squircle) ↔ 34dp (paused pill) ↔ 16dp (pressed)
 *   using [MaterialTheme.motionScheme]'s spatial springs.
 * - Tactile press compression physics: button compresses on touch and bounces back with fast spatial recoil.
 * - Play/pause icon swap runs on AnimatedContent with fast spatial scale bounce and fast effects fade.
 * - The pressed button's weight grows ×1.15 on the fast spatial spring while the
 *   neighbours shrink proportionally (Row weight normalization).
 * - While [loading], a small CircularProgressIndicator replaces the play/pause icon with spring transition.
 * - Prev/next respect [ControlState.isPreviousAvailable]/[ControlState.isNextAvailable] with
 *   spring shape morphing and tactile press recoil.
 *
 * Sends [UIEvent.PlayPause] / [UIEvent.Previous] / [UIEvent.Next] exactly like
 * [com.maxrave.simpmusic.ui.component.PlayerControlLayout].
 */
@Composable
fun ExpressiveTransportRow(
    controllerState: ControlState,
    loading: Boolean,
    onUIEvent: (UIEvent) -> Unit,
    modifier: Modifier = Modifier,
    // Only the fullscreen lyrics page asks for these: its layout leaves out the connected group
    // below, which is where Now Playing keeps shuffle and repeat.
    showShuffleAndRepeat: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme

    val prevInteraction = remember { MutableInteractionSource() }
    val playInteraction = remember { MutableInteractionSource() }
    val nextInteraction = remember { MutableInteractionSource() }
    val prevPressed by prevInteraction.collectIsPressedAsState()
    val playPressed by playInteraction.collectIsPressedAsState()
    val nextPressed by nextInteraction.collectIsPressedAsState()

    // Weight expansion: pressed button grows x1.15 on fast spatial spring
    val prevWeight by animateFloatAsState(
        targetValue = if (prevPressed) SIDE_WEIGHT * PRESS_GROWTH else SIDE_WEIGHT,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "prevWeight",
    )
    val playWeight by animateFloatAsState(
        targetValue = if (playPressed) PLAY_WEIGHT * PRESS_GROWTH else PLAY_WEIGHT,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "playWeight",
    )
    val nextWeight by animateFloatAsState(
        targetValue = if (nextPressed) SIDE_WEIGHT * PRESS_GROWTH else SIDE_WEIGHT,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "nextWeight",
    )

    // Tactile press compression physics: 0.90f - 0.92f scale with fast spatial recoil
    val prevScale by animateFloatAsState(
        targetValue = if (prevPressed && controllerState.isPreviousAvailable) 0.92f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "prevScale",
    )
    val playScale by animateFloatAsState(
        targetValue = if (playPressed) 0.90f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "playScale",
    )
    val nextScale by animateFloatAsState(
        targetValue = if (nextPressed && controllerState.isNextAvailable) 0.92f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "nextScale",
    )

    // Shape morphing:
    // Play: 34dp (paused pill) <-> 22dp (playing squircle) <-> 16dp (pressed more square)
    val targetPlayCorner = when {
        playPressed -> 16.dp
        controllerState.isPlaying -> 22.dp
        else -> 34.dp
    }
    val playCorner by animateDpAsState(
        targetValue = targetPlayCorner,
        animationSpec = if (playPressed) motionScheme.fastSpatialSpec() else motionScheme.defaultSpatialSpec(),
        label = "playCorner",
    )

    // Prev/Next: 34dp pill <-> 22dp pressed
    val prevCorner by animateDpAsState(
        targetValue = if (prevPressed && controllerState.isPreviousAvailable) 22.dp else 34.dp,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "prevCorner",
    )
    val nextCorner by animateDpAsState(
        targetValue = if (nextPressed && controllerState.isNextAvailable) 22.dp else 34.dp,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "nextCorner",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(68.dp),
    ) {
        if (showShuffleAndRepeat) {
            ExpressiveToggleButton(
                icon = SimpIcons.Shuffle,
                active = controllerState.isShuffle,
                onClick = { onUIEvent(UIEvent.Shuffle) },
            )
            Spacer(modifier = Modifier.width(TOGGLE_SEPARATION))
        }
        // Previous — pill on secondaryContainer, morphs and compresses on press.
        Surface(
            onClick = {
                if (controllerState.isPreviousAvailable) {
                    onUIEvent(UIEvent.Previous)
                }
            },
            shape = RoundedCornerShape(prevCorner),
            color = colorScheme.secondaryContainer,
            interactionSource = prevInteraction,
            modifier =
                Modifier
                    .weight(prevWeight)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = prevScale
                        scaleY = prevScale
                        transformOrigin = TransformOrigin.Center
                    },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = SimpIcons.SkipPrevious,
                    contentDescription = "",
                    tint =
                        colorScheme.onSecondaryContainer.copy(
                            alpha = if (controllerState.isPreviousAvailable) 1f else 0.4f,
                        ),
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        // Play / Pause — primary container, corner radius morphs with playback state and press, tactile spring recoil.
        Surface(
            onClick = {
                if (!loading) {
                    onUIEvent(UIEvent.PlayPause)
                }
            },
            shape = RoundedCornerShape(playCorner),
            color = colorScheme.primary,
            interactionSource = playInteraction,
            modifier =
                Modifier
                    .weight(playWeight)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = playScale
                        scaleY = playScale
                        transformOrigin = TransformOrigin.Center
                    },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = loading,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.8f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                        ) + fadeIn(
                            animationSpec = motionScheme.fastEffectsSpec(),
                        )).togetherWith(
                            scaleOut(
                                targetScale = 0.8f,
                                animationSpec = motionScheme.fastSpatialSpec(),
                            ) + fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec(),
                            ),
                        )
                    },
                    label = "playLoadingAnimation",
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { isLoading ->
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = colorScheme.onPrimary,
                            strokeWidth = 3.dp,
                        )
                    } else {
                        AnimatedContent(
                            targetState = controllerState.isPlaying,
                            transitionSpec = {
                                (scaleIn(
                                    initialScale = 0.65f,
                                    animationSpec = motionScheme.fastSpatialSpec(),
                                ) + fadeIn(
                                    animationSpec = motionScheme.fastEffectsSpec(),
                                )).togetherWith(
                                    scaleOut(
                                        targetScale = 0.65f,
                                        animationSpec = motionScheme.fastSpatialSpec(),
                                    ) + fadeOut(
                                        animationSpec = motionScheme.fastEffectsSpec(),
                                    ),
                                )
                            },
                            label = "playPauseIconAnimation",
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) { isPlaying ->
                            Icon(
                                imageVector = if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                }
            }
        }
        // Next — mirror of Previous.
        Surface(
            onClick = {
                if (controllerState.isNextAvailable) {
                    onUIEvent(UIEvent.Next)
                }
            },
            shape = RoundedCornerShape(nextCorner),
            color = colorScheme.secondaryContainer,
            interactionSource = nextInteraction,
            modifier =
                Modifier
                    .weight(nextWeight)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = nextScale
                        scaleY = nextScale
                        transformOrigin = TransformOrigin.Center
                    },
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = SimpIcons.SkipNext,
                    contentDescription = "",
                    tint =
                        colorScheme.onSecondaryContainer.copy(
                            alpha = if (controllerState.isNextAvailable) 1f else 0.4f,
                        ),
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        if (showShuffleAndRepeat) {
            Spacer(modifier = Modifier.width(TOGGLE_SEPARATION))
            val repeatState = controllerState.repeatState
            ExpressiveToggleButton(
                icon = if (repeatState is RepeatState.One) SimpIcons.RepeatOne else SimpIcons.Repeat,
                active = repeatState !is RepeatState.None,
                onClick = { onUIEvent(UIEvent.Repeat) },
            )
        }
    }
}

/**
 * Shuffle or repeat as a pill beside the transport, in the connected group's colours: primary
 * container while on, surfaceContainerHigh while off — with tactile compression and fast effects transitions.
 */
@Composable
private fun RowScope.ExpressiveToggleButton(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "toggleScale",
    )
    val corner by animateDpAsState(
        targetValue = if (isPressed) 20.dp else 34.dp,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "toggleCorner",
    )
    val containerColor by animateColorAsState(
        targetValue = if (active) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh,
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "toggleContainerColor",
    )
    val iconColor by animateColorAsState(
        targetValue = if (active) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "toggleIconColor",
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        color = containerColor,
        interactionSource = interactionSource,
        modifier =
            Modifier
                .weight(TOGGLE_WEIGHT)
                .fillMaxHeight()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin.Center
                },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
