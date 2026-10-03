package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.ui.component.ExplicitBadge
import com.maxrave.simpmusic.ui.component.HeartCheckBox
import com.maxrave.simpmusic.ui.component.PlayerControlLayout
import com.maxrave.simpmusic.ui.icon.GraphicEq
import com.maxrave.simpmusic.ui.icon.KeyboardArrowDown
import com.maxrave.simpmusic.ui.icon.Lyrics
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.PlaylistAdd
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingContentAmbientGlow(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val morphProgress = LocalNowPlayingMorphProgress.current
    val artworkBoundsHolder = LocalNowPlayingArtworkBounds.current
    val textBoundsHolder = LocalNowPlayingTextBounds.current

    val restingAlpha =
        if (morphProgress < 0.85f) 0f
        else if (morphProgress < 1f) ((morphProgress - 0.85f) / 0.15f).coerceIn(0f, 1f)
        else 1f

    val startColor = state.startColor.value
    val endColor = state.endColor.value

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0E)),
    ) {
        // Ambient mesh lighting. Three radial sources breathe on independent phases — distinct
        // periods and start offsets — so the canvas never lands in the same configuration twice
        // and never reads as a mechanical pulse. All of this is animation-clock driven; nothing
        // here touches the audio path, so there is no PCM overhead regardless of what is playing.
        AmbientOrb(
            size = 360.dp,
            color = startColor,
            periodMs = 5400,
            startOffsetMs = 0,
            alpha = 0.42f,
            modifier = Modifier.align(Alignment.TopStart),
        )
        AmbientOrb(
            size = 380.dp,
            color = endColor,
            periodMs = 4600,
            startOffsetMs = 1500,
            alpha = 0.35f,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
        // The palette's midpoint, centred behind the artwork and slowest of the three, is what
        // makes the field read as one connected mesh rather than two blobs orbiting a spare.
        AmbientOrb(
            size = 430.dp,
            color = lerp(startColor, endColor, 0.5f),
            periodMs = 6800,
            startOffsetMs = 3100,
            alpha = 0.30f,
            modifier = Modifier.align(Alignment.Center),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Bar
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { actions.onDismiss() }) {
                    Icon(
                        imageVector = SimpIcons.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "AMBIENT GLOW",
                        style = typo().labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                IconButton(onClick = { actions.onShowMoreSheet() }) {
                    Icon(
                        imageVector = SimpIcons.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // Floating Glass Artwork Card (Registered for Morphing)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                        .alpha(restingAlpha)
                        .onGloballyPositioned { coordinates ->
                            artworkBoundsHolder?.value = coordinates.boundsInRoot()
                        },
            ) {
                val artworkUrl = state.screenData.thumbnailURL
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalPlatformContext.current)
                            .data(artworkUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .crossfade(false)
                            .build(),
                    contentDescription = "Album Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // Song Info & Like (Registered for Morphing)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .alpha(restingAlpha)
                            .onGloballyPositioned { coordinates ->
                                textBoundsHolder?.value = coordinates.boundsInRoot()
                            },
                ) {
                    Text(
                        text = state.screenData.nowPlayingTitle,
                        style = typo().titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp),
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.screenData.isExplicit) {
                            ExplicitBadge(modifier = Modifier.size(18.dp).padding(end = 4.dp))
                        }
                        Text(
                            text = state.screenData.artistName,
                            style = typo().bodyMedium.copy(fontSize = 14.sp),
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { actions.onNavigateToArtist() },
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                HeartCheckBox(checked = state.controllerState.isLiked, size = 32) {
                    actions.onUIEvent(UIEvent.ToggleLike)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Floating Synced Lyrics / Codec Capsule
            val currentLyric = state.screenData.lyricsData?.lyrics?.lines?.getOrNull(state.currentLyricLineIndex)?.words
            val lyricOrCodec =
                if (!currentLyric.isNullOrBlank()) {
                    currentLyric
                } else {
                    state.audioCodecLabel ?: "Lossless High-Res"
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                        .clickable { actions.onShowFullscreenLyrics() }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (!currentLyric.isNullOrBlank()) SimpIcons.Lyrics else SimpIcons.GraphicEq,
                        contentDescription = "Lyrics indicator",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = lyricOrCodec,
                        style = typo().bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glassmorphism Slider
            Slider(
                value = (state.sliderValue / 100f).coerceIn(0f, 1f),
                onValueChange = { actions.onSliderChange(it * 100f) },
                onValueChangeFinished = { actions.onSliderChangeFinished() },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = startColor,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                    ),
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(12.dp, 12.dp),
                        colors = SliderDefaults.colors(thumbColor = Color.White),
                    )
                },
            )

            // Duration Labels
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDuration((state.timelineState.total * (state.sliderValue / 100f)).roundToLong()),
                    style = typo().bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
                Text(
                    text = formatDuration(state.timelineState.total),
                    style = typo().bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Frosted Glass Transport Dock
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(32.dp))
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlayerControlLayout(state.controllerState) {
                    actions.onUIEvent(it)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Utilities
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { actions.onShowQueue() }) {
                    Icon(imageVector = SimpIcons.QueueMusic, contentDescription = "Queue", tint = Color.White.copy(alpha = 0.7f))
                }
                IconButton(onClick = { actions.onShowFullscreenLyrics() }) {
                    Icon(imageVector = SimpIcons.Lyrics, contentDescription = "Lyrics", tint = Color.White.copy(alpha = 0.7f))
                }
                IconButton(onClick = { actions.onShowAddToPlaylist() }) {
                    Icon(imageVector = SimpIcons.PlaylistAdd, contentDescription = "Add to playlist", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

/**
 * One radial light source in the ambient mesh behind [NowPlayingContentAmbientGlow].
 *
 * Breathing is driven by [rememberInfiniteTransition] on the animation clock — no audio data is
 * consulted, so a source costs nothing extra while music plays. Each instance owns its own
 * transition, so callers desynchronise the sources simply by handing them different [periodMs]
 * and [startOffsetMs]; three sources on coprime-ish periods never resync into a visible loop.
 *
 * The orb breathes in two independent ways. [breathScale] pulses the whole box (as the original
 * two orbs did), while [breathCentreX]/[breathCentreY] drift the gradient's focal point inside a
 * box that does not move — the light leans rather than just inflating, which is what makes the
 * field feel like light moving in a room instead of a balloon pumping. The two axes run at
 * different periods so the drift path is itself non-repeating.
 *
 * The gradient is multi-stop: full colour at the centre, a soft shoulder partway out, then
 * transparent. The shoulder is what stops the light from cutting off at the edge of its box.
 */
@Composable
private fun AmbientOrb(
    size: Dp,
    color: Color,
    periodMs: Int,
    startOffsetMs: Int,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "ambientOrb")

    val breathScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(periodMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(startOffsetMs),
            ),
        label = "orbScale",
    )

    // Drift the focal point over roughly a quarter of the box. The two axes use unrelated periods
    // so the light never retraces the same path.
    val breathCentreX by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.62f,
        animationSpec =
            infiniteRepeatable(
                animation = tween((periodMs * 1.7f).roundToInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(startOffsetMs + 900),
            ),
        label = "orbCentreX",
    )
    val breathCentreY by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.58f,
        animationSpec =
            infiniteRepeatable(
                animation = tween((periodMs * 1.3f).roundToInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(startOffsetMs + 1700),
            ),
        label = "orbCentreY",
    )

    // The box is square, so a single side length resolves the gradient's centre and radius in the
    // same coordinate space.
    val side = with(LocalDensity.current) { size.toPx() }
    val centre = Offset(side * breathCentreX, side * breathCentreY)

    Box(
        modifier =
            modifier
                .size(size)
                .scale(breathScale)
                .background(
                    Brush.radialGradient(
                        colors =
                            listOf(
                                color.copy(alpha = alpha),
                                color.copy(alpha = alpha * 0.45f),
                                Color.Transparent,
                            ),
                        center = centre,
                        radius = side * 0.72f,
                    ),
                ),
    )
}
