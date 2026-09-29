package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.domain.mediaservice.handler.ControlState
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PauseCircle
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.PlayCircle
import com.maxrave.simpmusic.ui.icon.Repeat
import com.maxrave.simpmusic.ui.icon.RepeatOne
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.SkipNext
import com.maxrave.simpmusic.ui.icon.SkipPrevious
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.viewModel.UIEvent

@Composable
fun PlayerControlLayout(
    controllerState: ControlState,
    isSmallSize: Boolean = false,
    // Bare ▶ / ⏸ glyphs instead of the disc-enclosed PlayCircle/PauseCircle pair.
    // The desktop capsule asks for these; Now Playing keeps the discs.
    plainPlayPause: Boolean = false,
    // The capsule already pads its own edges; stacking this 20dp on top of that
    // read as a hole at both ends of the transport cluster.
    horizontalPadding: Dp = 20.dp,
    // Tint for the ACTIVE shuffle/repeat state. The default keeps the raw seed (#8ECAE6) every
    // existing call site had; the capsule passes a theme-aware colour because pastel seed on a
    // light glass surface is nearly invisible.
    activeColor: Color = seed,
    contentColor: Color = Color.White,
    onUIEvent: (UIEvent) -> Unit,
) {
    val height = if (isSmallSize) 48.dp else 96.dp
    val smallIcon = if (isSmallSize) 20.dp to 28.dp else 32.dp to 42.dp
    val mediumIcon = if (isSmallSize) 28.dp to 38.dp else 42.dp to 52.dp
    val bigIcon = if (isSmallSize) 38.dp to 48.dp else 72.dp to 96.dp

    val shuffleInteractionSource = remember { MutableInteractionSource() }
    val shufflePressed by shuffleInteractionSource.collectIsPressedAsState()

    val prevInteractionSource = remember { MutableInteractionSource() }
    val prevPressed by prevInteractionSource.collectIsPressedAsState()

    val playPauseInteractionSource = remember { MutableInteractionSource() }
    val playPausePressed by playPauseInteractionSource.collectIsPressedAsState()

    val nextInteractionSource = remember { MutableInteractionSource() }
    val nextPressed by nextInteractionSource.collectIsPressedAsState()

    val repeatInteractionSource = remember { MutableInteractionSource() }
    val repeatPressed by repeatInteractionSource.collectIsPressedAsState()

    val motionScheme = MaterialTheme.motionScheme
    val scope = rememberCoroutineScope()

    val shufflePulse = remember { Animatable(1f) }
    val prevPulse = remember { Animatable(1f) }
    val playPausePulse = remember { Animatable(1f) }
    val nextPulse = remember { Animatable(1f) }
    val repeatPulse = remember { Animatable(1f) }

    // Wave impulse function: compresses the whole clicked button and ripples through surrounded buttons
    val triggerWave: (Int) -> Unit = { centerIndex ->
        scope.launch {
            // 0: Shuffle, 1: Prev, 2: Play/Pause, 3: Next, 4: Repeat
            val pulses = listOf(shufflePulse, prevPulse, playPausePulse, nextPulse, repeatPulse)
            pulses.forEachIndexed { index, pulse ->
                val distance = abs(index - centerIndex)
                val targetScale = when (distance) {
                    0 -> 0.78f // Main pressed button gets full punchy compression
                    1 -> 0.88f // Directly surrounded neighbor buttons get distinct bounce
                    2 -> 0.94f // Secondary neighbor ripple
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
    LaunchedEffect(controllerState.repeatState) {
        if (firstRepeatMount) {
            firstRepeatMount = false
            return@LaunchedEffect
        }
        triggerWave(4)
    }

    var firstShuffleMount by remember { mutableStateOf(true) }
    LaunchedEffect(controllerState.isShuffle) {
        if (firstShuffleMount) {
            firstShuffleMount = false
            return@LaunchedEffect
        }
        triggerWave(0)
    }

    var firstPlayMount by remember { mutableStateOf(true) }
    LaunchedEffect(controllerState.isPlaying) {
        if (firstPlayMount) {
            firstPlayMount = false
            return@LaunchedEffect
        }
        triggerWave(2)
    }

    // Coupled scales: 1 button pressed causes adjacent buttons to bounce a little bit
    // Shuffle (function button: pops on X axis, scaleY = 1f)
    val shuffleTargetScale = when {
        shufflePressed -> 0.82f
        prevPressed -> 0.93f
        playPausePressed -> 0.96f
        else -> 1f
    }
    val shuffleScale by animateFloatAsState(
        targetValue = shuffleTargetScale,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "shuffle_scale",
    )

    // Previous
    val prevTargetScale = when {
        prevPressed && controllerState.isPreviousAvailable -> 0.86f
        shufflePressed -> 0.93f
        playPausePressed -> 0.94f
        nextPressed -> 0.98f
        else -> 1f
    }
    val prevScale by animateFloatAsState(
        targetValue = prevTargetScale,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "prev_scale",
    )

    // Play/Pause
    val playPauseTargetScale = when {
        playPausePressed -> 0.88f
        prevPressed || nextPressed -> 0.94f
        shufflePressed || repeatPressed -> 0.97f
        else -> 1f
    }
    val playPauseScale by animateFloatAsState(
        targetValue = playPauseTargetScale,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "play_pause_scale",
    )

    // Next
    val nextTargetScale = when {
        nextPressed && controllerState.isNextAvailable -> 0.86f
        repeatPressed -> 0.93f
        playPausePressed -> 0.94f
        prevPressed -> 0.98f
        else -> 1f
    }
    val nextScale by animateFloatAsState(
        targetValue = nextTargetScale,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "next_scale",
    )

    // Repeat (function button / loop: pops on X axis, scaleY = 1f)
    val repeatTargetScale = when {
        repeatPressed -> 0.82f
        nextPressed -> 0.93f
        playPausePressed -> 0.96f
        else -> 1f
    }
    val repeatScale by animateFloatAsState(
        targetValue = repeatTargetScale,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "repeat_scale",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = horizontalPadding),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(smallIcon.second)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = shuffleScale * shufflePulse.value
                            scaleY = 1f // pop X axis not Y and Z
                        }
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = shuffleInteractionSource,
                            indication = ripple(bounded = false, radius = smallIcon.second / 2),
                        ) {
                            triggerWave(0)
                            onUIEvent(UIEvent.Shuffle)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(
                    targetState = controllerState.isShuffle,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "Shuffle Button",
                ) { isShuffle ->
                    Icon(
                        imageVector = SimpIcons.Shuffle,
                        tint = if (isShuffle) activeColor else contentColor,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(smallIcon.first),
                    )
                }
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(mediumIcon.second)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = prevScale * prevPulse.value
                            scaleY = prevScale * prevPulse.value
                        }
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = prevInteractionSource,
                            indication = ripple(bounded = false, radius = mediumIcon.second / 2),
                            enabled = controllerState.isPreviousAvailable,
                        ) {
                            triggerWave(1)
                            onUIEvent(UIEvent.Previous)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SimpIcons.SkipPrevious,
                    tint = if (controllerState.isPreviousAvailable) contentColor else contentColor.copy(alpha = 0.4f),
                    contentDescription = "",
                    modifier = Modifier.size(mediumIcon.first),
                )
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(bigIcon.second)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = playPauseScale * playPausePulse.value
                            scaleY = playPauseScale * playPausePulse.value
                        }
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = playPauseInteractionSource,
                            indication = ripple(bounded = false, radius = bigIcon.second / 2),
                        ) {
                            triggerWave(2)
                            onUIEvent(UIEvent.PlayPause)
                        },
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = controllerState.isPlaying,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.88f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                        ) + fadeIn(
                            animationSpec = motionScheme.fastEffectsSpec(),
                        )).togetherWith(
                            scaleOut(
                                targetScale = 0.88f,
                                animationSpec = motionScheme.fastSpatialSpec(),
                            ) + fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec(),
                            ),
                        )
                    },
                    label = "playPauseAnimatedContent",
                ) { isPlaying ->
                    if (!isPlaying) {
                        Icon(
                            imageVector = if (plainPlayPause) SimpIcons.PlayArrow else SimpIcons.PlayCircle,
                            tint = contentColor,
                            contentDescription = "",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    } else {
                        Icon(
                            imageVector = if (plainPlayPause) SimpIcons.Pause else SimpIcons.PauseCircle,
                            tint = contentColor,
                            contentDescription = "",
                            modifier = Modifier.size(bigIcon.first),
                        )
                    }
                }
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(mediumIcon.second)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = nextScale * nextPulse.value
                            scaleY = nextScale * nextPulse.value
                        }
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = nextInteractionSource,
                            indication = ripple(bounded = false, radius = mediumIcon.second / 2),
                            enabled = controllerState.isNextAvailable,
                        ) {
                            triggerWave(3)
                            onUIEvent(UIEvent.Next)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SimpIcons.SkipNext,
                    tint = if (controllerState.isNextAvailable) contentColor else contentColor.copy(alpha = 0.4f),
                    contentDescription = "",
                    modifier = Modifier.size(mediumIcon.first),
                )
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(smallIcon.second)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = repeatScale * repeatPulse.value
                            scaleY = 1f // pop X axis not Y and Z
                        }
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = repeatInteractionSource,
                            indication = ripple(bounded = false, radius = smallIcon.second / 2),
                        ) {
                            triggerWave(4)
                            onUIEvent(UIEvent.Repeat)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(
                    targetState = controllerState.repeatState,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "Repeat Button",
                ) { rs ->
                    val (icon, tint) = when (rs) {
                        is RepeatState.None -> SimpIcons.Repeat to contentColor
                        RepeatState.All -> SimpIcons.Repeat to activeColor
                        RepeatState.One -> SimpIcons.RepeatOne to activeColor
                    }
                    Icon(
                        imageVector = icon,
                        tint = tint,
                        contentDescription = "Repeat",
                        modifier = Modifier.size(smallIcon.first),
                    )
                }
            }
        }
    }
}